public class RequestHandler {

    private static final WishlistItemDAO WISHLIST = new WishlistItemDAO();
    private static final FriendViewDAO FRIENDS = new FriendViewDAO();
    private static final AdminDAO ADMIN = new AdminDAO();

    public static Response handle(Request request, ClientHandler client) {
        try {
            switch (request.getAction()) {

                case "session.bind":
                    client.bind(request.getInt("userId"));
                    return Response.message("Session bound.");

                case "wishlist.catalog":
                    return Response.ok(WISHLIST.getCatalog());

                case "wishlist.mine":
                    return Response.ok(WISHLIST.getMyWishlist(request.getInt("userId")));

                case "wishlist.add":
                    return WISHLIST.addItemFromCatalog(request.getInt("userId"), request.getInt("itemId"))
                            ? Response.message("Item added to the wish list.")
                            : Response.fail("Could not add the item.");

                case "wishlist.update":
                    return WISHLIST.updateCatalogItem(request.getInt("itemId"),
                            request.getString("itemName"),
                            request.getString("itemDescription"),
                            request.getDouble("price"))
                            ? Response.message("Item updated.")
                            : Response.fail("Could not update the item.");

                case "wishlist.delete":
                    return WISHLIST.deleteItem(request.getInt("wishlistItemId"), request.getInt("userId"))
                            ? Response.message("Item removed from the wish list.")
                            : Response.fail("Could not remove the item.");

                case "friends.list":
                    return Response.ok(FRIENDS.getFriendsList(request.getInt("userId")));

                case "friends.wishlist":
                    return Response.ok(FRIENDS.getFriendWishlist(request.getInt("userId"),
                            request.getInt("friendId")));

                case "admin.addItem":
                    return ADMIN.addCatalogItem(request.getString("itemName"),
                            request.getString("itemDescription"),
                            request.getDouble("price"))
                            ? Response.message("Item added to the catalog.")
                            : Response.fail("Could not add the item.");

                default:
                    return Response.fail("Unknown action: " + request.getAction());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("Server error: " + e.getMessage());
        }
    }
}
