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
