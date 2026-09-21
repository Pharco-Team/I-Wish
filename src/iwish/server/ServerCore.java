package iwish.server;

import iwish.common.NotificationDTO;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCore {
    private ServerSocket serverSocket;
    private volatile boolean running;
    private final Map<Integer, ClientHandler> online = new ConcurrentHashMap<>();

    public synchronized void start(int port) throws IOException {
        if (running) return;
        serverSocket = new ServerSocket(port);
        running = true;
        new Thread(() -> {
            while (running) {
                try {
                    Socket s = serverSocket.accept();
                    new ClientHandler(s, this).start();
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        }, "accept-loop").start();
    }

    public synchronized void stop() {
        running = false;
        try { serverSocket.close(); } catch (Exception ignored) {}
        online.values().forEach(ClientHandler::close);
        online.clear();
    }

    public void register(int userId, ClientHandler h)   { online.put(userId, h); }
    public void unregister(int userId, ClientHandler h) { online.remove(userId, h); }

    /** Live push if the user is online; otherwise it stays unread in the DB until next login. */
    public void push(NotificationDTO n) {
        ClientHandler h = online.get(n.userId);
        if (h == null) return;
        try { h.send(n); } catch (IOException e) { unregister(n.userId, h); }
    }

    public static void main(String[] args) throws IOException {
        new ServerCore().start(5005);
        System.out.println("iWish server running on port 5005");
    }
}
