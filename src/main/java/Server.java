import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public static final int PORT = 5000;

    private static final Map<Integer, ClientHandler> ONLINE = new ConcurrentHashMap<>();

    private final List<ClientHandler> clients = new ArrayList<>();
    private ServerSocket serverSocket;
    private volatile boolean running;

    public void start() throws IOException {
        if (running) {
            System.out.println("Server is already running.");
            return;
        }
        DBConnection.initSchema();
        serverSocket = new ServerSocket(PORT);
        running = true;
        new Thread(this::acceptLoop).start();
        System.out.println("Server started on port " + PORT);
    }

    public void stop() {
        if (!running) {
            System.out.println("Server is not running.");
            return;
        }
        running = false;

        List<ClientHandler> connected;
        synchronized (clients) {
            connected = new ArrayList<>(clients);
            clients.clear();
        }
        for (ClientHandler client : connected) {
            client.close();
        }
        ONLINE.clear();

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DBConnection.shutdown();
        System.out.println("Server stopped.");
    }

    public boolean isRunning() {
        return running;
    }

    public static void notifyUser(int userId, Notification notification) {
        ClientHandler client = ONLINE.get(userId);
        if (client != null) {
            client.send(notification);
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket, this);
                synchronized (clients) {
                    clients.add(client);
                }
                new Thread(client).start();
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void register(int userId, ClientHandler client) {
        ONLINE.put(userId, client);
    }

    static void unregister(int userId, ClientHandler client) {
        ONLINE.remove(userId, client);
    }

    void remove(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
        }
    }
}
