import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int userId;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // the client must also open its output stream first, or both sides
            // block waiting for the other's stream header
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (!socket.isClosed()) {
                Request request = (Request) in.readObject();
                send(RequestHandler.handle(request, this));
            }
        } catch (IOException | ClassNotFoundException e) {
            // the client disconnected
        } finally {
            close();
        }
    }

    public synchronized void send(Response response) {
        if (out == null) {
            return;
        }
        try {
            out.writeObject(response);
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void bind(int userId) {
        this.userId = userId;
        Server.register(userId, this);
    }

    void close() {
        if (userId != 0) {
            Server.unregister(userId);
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.remove(this);
    }
}
