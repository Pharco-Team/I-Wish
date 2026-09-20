## Wishlist & Friends View (Amir)

This part implements the data layer for **Wish List management**
and **viewing a friend's wish list**.

It covers spec items:

* **Item 4** — Create / Update / Delete my Wish List
* **Item 5** — View my Friends list
* **Item 6** — View a specific Friend's Wish List

### Files in this part

| File                   | Responsibility                                                                                                                                        |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Item.java`            | Model for a catalog item (`item_id`, `item_name`, `item_description`, `price`)                                                                        |
| `WishlistItem.java`    | Model representing a user's wishlist entry together with its catalog item data                                                                        |
| `WishlistItemDAO.java` | Database access for listing the catalog, adding items to a wishlist, updating item details, deleting wishlist items, and retrieving a user's wishlist |
| `Friend.java`          | Model representing a friend                                                                                                                           |
| `FriendViewDAO.java`   | Database access for listing friends and viewing a specific friend's wishlist                                                                          |

### Database tables used

This part uses the following tables from `src/main/resources/schema.sql`:

* **`items`** — stores the shared catalog of available items.
* **`wishlist_items`** — connects a user to an item in the catalog. It also stores the contributed amount, status, and soft-delete flag.
* **`friends`** — stores the relationship between users and its status.

A friend's wishlist can only be viewed when the friendship status is **`accepted`**.

> Updating an item's name, description, or price changes the corresponding row in
> `items`. Therefore, the updated information is reflected wherever that catalog
> item is referenced.

### Database

The project uses **Apache Derby** as an embedded database.

The database is created automatically using:

`src/main/resources/schema.sql`

The Derby database is stored in the project directory as:

`iwishdb/`

No separate MySQL server is required.

### Testing

The wishlist and friends functionality can be tested through the project's
server using the following actions:

| Action             | Purpose                               |
| ------------------ | ------------------------------------- |
| `wishlist.catalog` | View available catalog items          |
| `wishlist.mine`    | View the current user's wishlist      |
| `wishlist.add`     | Add an item to a user's wishlist      |
| `wishlist.update`  | Update catalog item information       |
| `wishlist.delete`  | Remove an item from a user's wishlist |
| `friends.list`     | View the user's friends               |
| `friends.wishlist` | View a friend's wishlist              |

For example:

```java
out.writeObject(new Request("wishlist.mine").put("userId", 3));
out.flush();

Response response = (Response) in.readObject();
```

The server hangit checkout amirdles these requests through `RequestHandler` and the corresponding
DAO classes.
