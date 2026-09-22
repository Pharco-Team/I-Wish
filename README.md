# I-Wish
I-Wish Project

## Server (Youssef Islam Sayeh)

Run `ServerMain` and use the console: `start`, `stop`, `additem`, `exit`.
Database is embedded Apache Derby, created automatically from `src/main/resources/schema.sql`
on first run. `SmokeTest` starts the server, talks to it over a socket and checks the replies.

### Talking to the server

Clients connect to port 5000 and exchange serialized `Request` / `Response` objects.
Open the `ObjectOutputStream` **before** the `ObjectInputStream`, or both sides block.

```java
Socket socket = new Socket("localhost", Server.PORT);
ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

out.writeObject(new Request("wishlist.mine").put("userId", 3));
out.flush();
Response response = (Response) in.readObject();
```

| Action | Parameters | Returns |
|---|---|---|
| `session.bind` | userId | confirmation (needed to receive notifications) |
| `wishlist.catalog` | - | `List<Item>` |
| `wishlist.mine` | userId | `List<WishlistItem>` |
| `wishlist.add` | userId, itemId | confirmation |
| `wishlist.update` | itemId, itemName, itemDescription, price | confirmation |
| `wishlist.delete` | wishlistItemId, userId | confirmation |
| `friends.list` | userId | `List<Friend>` |
| `friends.wishlist` | userId, friendId | `List<WishlistItem>` |
| `admin.addItem` | itemName, itemDescription, price | confirmation |

Unknown actions come back as a failed `Response`, so adding new ones is just a new `case`
in `RequestHandler`.

### Sending a notification to a user

For spec items 8 and 9, call `Server.notifyUser(userId, "your message")` from anywhere on the
server. It pushes a `Response` to that user if they are connected and bound.

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

The server handles these requests through `RequestHandler` and the corresponding
DAO classes.
