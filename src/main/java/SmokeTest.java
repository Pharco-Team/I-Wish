import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class SmokeTest {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start();

        try (Socket socket = new Socket("localhost", Server.PORT)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            check(send(out, in, new Request("session.bind").put("userId", 1)).isOk(),
                    "session.bind");

            check(send(out, in, new Request("admin.addItem")
                    .put("itemName", "Smoke Test Item")
                    .put("itemDescription", "added by the smoke test")
                    .put("price", 199.0)).isOk(), "admin.addItem");

            Response catalog = send(out, in, new Request("wishlist.catalog"));
            check(catalog.isOk() && !((List<?>) catalog.getData()).isEmpty(), "wishlist.catalog");

            check(send(out, in, new Request("friends.list").put("userId", 1)).isOk(),
                    "friends.list");

            check(!send(out, in, new Request("nope")).isOk(), "unknown action is rejected");
        }

        server.stop();
        System.out.println("All checks passed.");
    }

    private static Response send(ObjectOutputStream out, ObjectInputStream in, Request request)
            throws Exception {
        out.writeObject(request);
        out.flush();
        return (Response) in.readObject();
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }
        System.out.println("ok - " + name);
    }
}
