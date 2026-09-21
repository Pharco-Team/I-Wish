package iwish.client;

import iwish.common.NotificationDTO;
import iwish.common.Request;
import iwish.common.Response;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** One socket, one reader thread: Responses go to the caller, NotificationDTOs go to the listener. */
public class ServerConnection {
    private static ServerConnection instance;
    public static synchronized ServerConnection get() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final BlockingQueue<Response> responses = new LinkedBlockingQueue<>();
    private volatile Consumer<NotificationDTO> notificationListener;

    public synchronized void connect(String host, int port) throws IOException {
        Socket s = new Socket(host, port);
        out = new ObjectOutputStream(s.getOutputStream());
        out.flush();
        in = new ObjectInputStream(s.getInputStream());
        Thread reader = new Thread(this::readLoop, "server-reader");
        reader.setDaemon(true);
        reader.start();
    }

    private void readLoop() {
        try {
            while (true) {
                Object o = in.readObject();
                if (o instanceof NotificationDTO) {
                    Consumer<NotificationDTO> l = notificationListener;
                    if (l != null) l.accept((NotificationDTO) o);
                } else if (o instanceof Response) {
                    responses.put((Response) o);
                }
            }
        } catch (Exception e) { /* connection closed */ }
    }

    public void setNotificationListener(Consumer<NotificationDTO> l) { this.notificationListener = l; }

    public synchronized Response send(Request r) throws IOException, InterruptedException {
        responses.clear();
        out.writeObject(r);
        out.flush();
        Response res = responses.poll(15, TimeUnit.SECONDS);
        if (res == null) throw new IOException("Server timeout.");
        return res;
    }
}
