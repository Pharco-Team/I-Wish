import java.util.List;

public class RequestHandler {

    private static final UserDAO USERS = new UserDAO();
    private static final FriendRequestDAO FRIEND_REQUESTS = new FriendRequestDAO();
    private static final FriendViewDAO FRIENDS = new FriendViewDAO();
    private static final WishlistItemDAO WISHLIST = new WishlistItemDAO();
    private static final ContributionDAO CONTRIBUTIONS = new ContributionDAO();
    private static final NotificationDAO NOTIFICATIONS = new NotificationDAO();

    public static Response handle(Request request, ClientHandler client) {
        try {
            String action = request.getAction();

            if (action.equals("user.register")) {
                return register(request);
            }
            if (action.equals("user.login")) {
                return login(request, client);
            }

            User me = client.getUser();
            if (me == null) {
                return Response.fail("Please sign in first.");
            }

            switch (action) {

                case "friends.list":
                    return Response.ok(FRIENDS.getFriendsList(me.getUserId()));

                case "friends.requests":
                    return Response.ok(FRIEND_REQUESTS.getPendingRequests(me.getUserId()));

                case "friends.add":
                    return addFriend(request, me);

                case "friends.accept":
                    return acceptFriend(request, me);

                case "friends.decline":
                    return FRIEND_REQUESTS.declineRequest(me.getUserId(), request.getInt("friendId"))
                            ? Response.message("Friend request declined!")
                            : Response.fail("That request is no longer there.");

                case "friends.remove":
                    return FRIEND_REQUESTS.removeFriend(me.getUserId(), request.getInt("friendId"))
                            ? Response.message("Friend removed successfully!")
                            : Response.fail("Could not remove this friend.");

                case "friends.wishlist":
                    return Response.ok(FRIENDS.getFriendWishlist(me.getUserId(), request.getInt("friendId")));

                case "wishlist.catalog":
                    return Response.ok(WISHLIST.getCatalog());

                case "wishlist.mine":
                    return Response.ok(WISHLIST.getMyWishlist(me.getUserId()));

                case "wishlist.add":
                    return WISHLIST.addItemFromCatalog(me.getUserId(), request.getInt("itemId"))
                            ? Response.message("Item added to your wish list.")
                            : Response.fail("Could not add the item.");

                case "wishlist.update":
                    return WISHLIST.updateCatalogItem(me.getUserId(), request.getInt("itemId"),
                            request.getString("itemName"),
                            request.getString("itemDescription"),
                            request.getDouble("price"))
                            ? Response.message("Item updated.")
                            : Response.fail("You can only edit an item on your own list, and the price "
                                    + "cannot go below what your friends already paid.");

                case "wishlist.delete":
                    return WISHLIST.deleteItem(request.getInt("wishlistItemId"), me.getUserId())
                            ? Response.message("Item removed from your wish list.")
                            : Response.fail("Could not remove the item.");

                case "contribute":
                    return contribute(request, me);

                case "notifications.list":
                    return Response.ok(NOTIFICATIONS.getNotifications(me.getUserId()));

                case "notifications.read":
                    NOTIFICATIONS.markAllRead(me.getUserId());
                    return Response.message("Notifications marked as read.");

                default:
                    return Response.fail("Unknown action: " + action);
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("Server error: " + e.getMessage());
        }
    }

    private static Response register(Request request) {
        String username = trim(request.getString("username"));
        String password = request.getString("password");
        String email = trim(request.getString("email"));

        if (username.isEmpty() || password == null || password.isEmpty()) {
            return Response.fail("Username and password cannot be empty.");
        }
        if (password.length() < 4) {
            return Response.fail("The password must be at least 4 characters.");
        }
        if (USERS.usernameExists(username)) {
            return Response.fail("Username already exists!");
        }

        return USERS.register(username, password, email)
                ? Response.message("Registration successful!")
                : Response.fail("Could not create the account.");
    }

    private static Response login(Request request, ClientHandler client) {
        User user = USERS.login(trim(request.getString("username")), request.getString("password"));
        if (user == null) {
            return Response.fail("Wrong username or password!");
        }
        client.bind(user);
        return Response.ok(user);
    }

    private static Response addFriend(Request request, User me) {
        User friend = USERS.findByUsername(trim(request.getString("username")));

        if (friend == null) {
            return Response.fail("User not found!");
        }
        if (friend.getUserId() == me.getUserId()) {
            return Response.fail("You cannot add yourself!");
        }

        String error = FRIEND_REQUESTS.sendRequest(me.getUserId(), friend.getUserId());
        if (error != null) {
            return Response.fail(error);
        }

        notifyUser(friend.getUserId(), me.getUsername() + " sent you a friend request.");
        return Response.message("Friend request sent!");
    }

    private static Response acceptFriend(Request request, User me) {
        int friendId = request.getInt("friendId");

        if (!FRIEND_REQUESTS.acceptRequest(me.getUserId(), friendId)) {
            return Response.fail("That request is no longer there.");
        }

        notifyUser(friendId, me.getUsername() + " accepted your friend request.");
        return Response.message("Friend request accepted!");
    }

    private static Response contribute(Request request, User me) throws Exception {
        List<Notification> created = CONTRIBUTIONS.contribute(me.getUserId(),
                request.getInt("wishlistItemId"), request.getDouble("amount"));

        for (Notification notification : created) {
            Server.notifyUser(notification.getUserId(), notification);
        }

        return Response.message(created.isEmpty()
                ? "Thank you! Your contribution was added."
                : "The gift is now fully funded!");
    }

    private static void notifyUser(int userId, String message) {
        Notification notification = NOTIFICATIONS.add(userId, message);
        if (notification != null) {
            Server.notifyUser(userId, notification);
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
