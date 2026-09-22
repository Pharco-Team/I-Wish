import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmokeTest {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start();

        String tag = String.valueOf(System.currentTimeMillis() % 1000000);
        String first = "test_first_" + tag;
        String second = "test_second_" + tag;

        ServerConnection one = new ServerConnection("localhost", Server.PORT);
        ServerConnection two = new ServerConnection("localhost", Server.PORT);

        List<Notification> pushed = Collections.synchronizedList(new ArrayList<Notification>());
        two.setNotificationListener(pushed::add);

        check(!one.send(new Request("friends.list")).isOk(), "requests are refused before signing in");

        check(one.send(new Request("user.register")
                .put("username", first).put("password", "1234").put("email", "first@iwish.com")).isOk(),
                "register");
        check(!one.send(new Request("user.register")
                .put("username", first).put("password", "1234")).isOk(),
                "the same username is refused twice");
        check(!one.send(new Request("user.login")
                .put("username", first).put("password", "wrong")).isOk(),
                "a wrong password is refused");
        check(one.send(new Request("user.login")
                .put("username", first).put("password", "1234")).isOk(),
                "sign in");

        check(two.send(new Request("user.register")
                .put("username", second).put("password", "1234").put("email", "second@iwish.com")).isOk(),
                "register the second user");
        Response secondLogin = two.send(new Request("user.login")
                .put("username", second).put("password", "1234"));
        check(secondLogin.isOk(), "the second user signs in");
        User secondUser = (User) secondLogin.getData();

        check(!one.send(new Request("friends.add").put("username", first)).isOk(),
                "you cannot add yourself");
        check(one.send(new Request("friends.add").put("username", second)).isOk(),
                "send a friend request");
        check(!one.send(new Request("friends.add").put("username", second)).isOk(),
                "the same request cannot be sent twice");

        List<Friend> requests = list(two.send(new Request("friends.requests")));
        check(requests.size() == 1, "the friend request arrived");
        check(two.send(new Request("friends.accept").put("friendId", requests.get(0).getUserId())).isOk(),
                "accept the friend request");
        check(list(one.send(new Request("friends.list"))).size() == 1, "the friends list shows the new friend");

        List<Item> catalog = list(two.send(new Request("wishlist.catalog")));
        check(!catalog.isEmpty(), "the catalog has items");
        Item wanted = catalog.get(catalog.size() - 1);
        check(two.send(new Request("wishlist.add").put("itemId", wanted.getItemId())).isOk(), "add a wish");

        List<WishlistItem> wishes = list(one.send(new Request("friends.wishlist")
                .put("friendId", secondUser.getUserId())));
        check(wishes.size() == 1, "a friend's wish list is visible");
        WishlistItem wish = wishes.get(0);

        check(!one.send(new Request("contribute")
                .put("wishlistItemId", wish.getWishlistItemId())
                .put("amount", wish.getPrice() + 1)).isOk(),
                "paying more than the price is refused");
        check(one.send(new Request("contribute")
                .put("wishlistItemId", wish.getWishlistItemId())
                .put("amount", wish.getPrice())).isOk(),
                "contribute the full price");
        check(!one.send(new Request("contribute")
                .put("wishlistItemId", wish.getWishlistItemId())
                .put("amount", 10.0)).isOk(),
                "a funded gift takes no more money");

        Thread.sleep(500);
        check(anyContains(pushed, "has been bought by"), "the receiver was notified that the gift was bought");

        List<WishlistItem> mine = list(two.send(new Request("wishlist.mine")));
        check("completed".equals(mine.get(0).getStatus()), "the wish is marked as completed");
        check(list(two.send(new Request("notifications.list"))).size() > 0, "notifications are stored");

        one.close();
        two.close();
        server.stop();
        System.out.println("All checks passed.");
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> list(Response response) {
        return (List<T>) response.getData();
    }

    private static boolean anyContains(List<Notification> notifications, String text) {
        synchronized (notifications) {
            for (Notification notification : notifications) {
                if (notification.getMessage().contains(text)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }
        System.out.println("ok - " + name);
    }
}
