package iwish.server;

import iwish.common.NotificationDTO;
import iwish.common.Request;
import iwish.common.Response;
import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final ServerCore server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile int userId = -1;

    private final ContributionDAO contributionDAO = new ContributionDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public ClientHandler(Socket socket, ServerCore server) { this.socket = socket; this.server = server; }

    /** Call this from your LOGIN handler (task 1) once credentials are verified. */
    public void onLoginSuccess(int id) { this.userId = id; server.register(id, this); }

    @Override public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Request req = (Request) in.readObject();
                send(handle(req));
            }
        } catch (EOFException | SocketException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.unregister(userId, this);
            close();
        }
    }

    private Response handle(Request r) {
        try {
            if (userId < 0) return Response.fail("Not signed in.");
            switch (r.action) {
                case "CONTRIBUTE": {                                           // task 7
                    int wishId = (Integer) r.get("wishId");
                    BigDecimal amount = (BigDecimal) r.get("amount");
                    ContributionDAO.Result res = contributionDAO.contribute(userId, wishId, amount);
                    for (NotificationDTO n : res.notifications) server.push(n); // tasks 8 & 9 (live)
                    return Response.ok(res.completed ? "Item fully funded!" : "Contribution added.",
                                       new BigDecimal[]{res.collected, res.price});
                }
                case "GET_NOTIFICATIONS":                                      // offline ones on login
                    return Response.ok("OK", notificationDAO.getUnread(userId));
                case "MARK_NOTIFICATIONS_READ":
                    notificationDAO.markAllRead(userId);
                    return Response.ok("OK", null);
                default:
                    return Response.fail("Unknown action: " + r.action);
            }
        } catch (ContributionDAO.BusinessException e) {
            return Response.fail(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.fail("Database error.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Response.fail("Bad request.");
        }
    }

    /** Thread-safe: responses and live pushes share one stream. */
    public synchronized void send(Object o) throws IOException {
        out.writeObject(o);
        out.flush();
        out.reset();
    }

    public void close() { try { socket.close(); } catch (IOException ignored) {} }
}
