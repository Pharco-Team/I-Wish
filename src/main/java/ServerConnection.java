import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ServerConnection {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final BlockingQueue<Response> responses = new LinkedBlockingQueue<>();
    private volatile Consumer<Notification> notificationListener;

    public ServerConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        Thread reader = new Thread(this::readLoop);
        reader.setDaemon(true);
        reader.start();
    }

    public void setNotificationListener(Consumer<Notification> listener) {
        this.notificationListener = listener;
    }

    public synchronized Response send(Request request) throws IOException {
        responses.clear();
        out.writeObject(request);
        out.flush();
        out.reset();

        try {
            Response response = responses.poll(15, TimeUnit.SECONDS);
            if (response == null) {
                throw new IOException("The server did not answer.");
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for the server.");
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // one reader thread: replies go to the caller, notifications go to the listener
    private void readLoop() {
        try {
            while (true) {
                Object message = in.readObject();
                if (message instanceof Notification) {
                    Consumer<Notification> listener = notificationListener;
                    if (listener != null) {
                        listener.accept((Notification) message);
                    }
                } else if (message instanceof Response) {
                    responses.put((Response) message);
                }
            }
        } catch (Exception e) {
            // the connection was closed
        }
    }
}
