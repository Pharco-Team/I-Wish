# I-Wish

A Java desktop application where you add friends, build your wish list, browse your friends'
wish lists, and chip in to buy them a gift. Swing client, socket server, embedded Apache Derby
database.

ITI Orange Belt - Building Java Desktop Applications.

## Team roles and contributions

| Member | GitHub | Spec items | Files |
| --- | --- | --- | --- |
| Michael Ayman | - | 1-3: Register / Sign-in, Add / Remove friend, Accept / Decline requests | `User.java`, `UserDAO.java`, `FriendRequestDAO.java` |
| Amir | Amir4-4 | 4-6: Create / Update / Delete wish list, View friends, View a friend's wish list | `Item.java`, `WishlistItem.java`, `WishlistItemDAO.java`, `Friend.java`, `FriendViewDAO.java` |
| Youhana Ayoub | u7ana | 7-9: Contribute to a gift, notify the buyers, notify the receiver | `ContributionDAO.java`, `NotificationDAO.java`, `Notification.java`, `ServerConnection.java` |
| Bichoy Naguib | - | 10: Friendly GUI | `IWishGUI.java` |
| Youssef Islam Sayeh | Sayeh-01 | 11-14: Start / Stop, database, client connections, client requests | `Server.java`, `ClientHandler.java`, `RequestHandler.java`, `DBConnection.java`, `AdminDAO.java`, `ServerMain.java`, `Request.java`, `Response.java`, `schema.sql`, `demo_data.sql`, `SmokeTest.java` |

## How to run

Open the folder in NetBeans (it is a Maven project) and run the two main classes:

1. `ServerMain` - type `start` in the console. The database is created on the first run.
2. `IWishGUI` - the desktop client. Run it twice to try two users at the same time.

From the command line:

```
mvn package
java -cp "target/classes;target/lib/*" ServerMain
java -cp target/classes IWishGUI
```

The client connects to `localhost` by default. To reach a server on another machine, pass its
address: `java -cp target/classes IWishGUI 192.168.1.5`.

### Demo accounts

The database is created with demo data. Every password is `1234`.

| Username | What you will see |
| --- | --- |
| `ahmed` | 2 friends, 2 friend requests waiting, notifications |
| `sara` | a wish list with a partly funded gift |
| `omar` | a gift that is already fully funded |
| `laila`, `mona` | sent a friend request to ahmed |

### Server console

| Command | What it does |
| --- | --- |
| `start` | starts listening on port 5000 |
| `stop` | closes all clients and shuts the database down |
| `additem` | adds an item to the catalog (admin) |
| `exit` | stops the server and quits |

### Checking that everything works

`SmokeTest` starts the server, connects two clients over real sockets and walks the whole flow:
register, sign in, friend request, accept, add a wish, contribute, and the notifications that
follow. It prints `All checks passed.` when the project is healthy.

## Database

Embedded Apache Derby - no separate database server is needed. The database folder `iwishdb/` is
created next to the running program from `src/main/resources/schema.sql`, then filled with
`src/main/resources/demo_data.sql`.

| Table | Holds |
| --- | --- |
| `users` | accounts (the password is stored as a PBKDF2 hash with a salt, never as plain text) |
| `friends` | one row per relation: `user_id_1` asked, `user_id_2` was asked, `status` is `pending` or `accepted` |
| `items` | the shared catalog users pick their wishes from |
| `wishlist_items` | a user's wish: which item, how much was collected, `open` or `completed` |
| `contributions` | who paid how much toward which wish |
| `notifications` | spec items 8 and 9, kept so a user who was offline still sees them |

## How the client talks to the server

Clients connect to port 5000 and exchange serialized `Request` and `Response` objects. Open the
`ObjectOutputStream` **before** the `ObjectInputStream`, or both sides block.

```java
ServerConnection server = new ServerConnection("localhost", Server.PORT);
server.send(new Request("user.login").put("username", "ahmed").put("password", "1234"));

Response response = server.send(new Request("wishlist.mine"));
List<WishlistItem> wishes = (List<WishlistItem>) response.getData();
```

Everything except `user.register` and `user.login` needs a signed-in session. The server uses the
user of that connection, so a client never sends its own user id and cannot act as somebody else.

| Action | Parameters | Returns |
| --- | --- | --- |
| `user.register` | username, password, email | confirmation |
| `user.login` | username, password | the `User` |
| `friends.list` | - | `List<Friend>` |
| `friends.requests` | - | `List<Friend>` (people waiting for an answer) |
| `friends.add` | username | confirmation |
| `friends.accept` / `friends.decline` | friendId | confirmation |
| `friends.remove` | friendId | confirmation |
| `friends.wishlist` | friendId | `List<WishlistItem>` |
| `wishlist.catalog` | - | `List<Item>` |
| `wishlist.mine` | - | `List<WishlistItem>` |
| `wishlist.add` | itemId | confirmation |
| `wishlist.update` | itemId, itemName, itemDescription, price | confirmation |
| `wishlist.delete` | wishlistItemId | confirmation |
| `contribute` | wishlistItemId, amount | confirmation |
| `notifications.list` | - | `List<Notification>` |
| `notifications.read` | - | confirmation |

Unknown actions come back as a failed `Response`, so adding a new one is just a new `case` in
`RequestHandler`.

### Notifications

When a gift becomes fully funded the server writes a `Notification` for every buyer (item 8) and
one for the receiver naming who bought it (item 9). Notifications are pushed down the same socket
to whoever is online, and the client shows them as a pop-up. Anyone who was offline sees them on
the Notifications page after signing in.
