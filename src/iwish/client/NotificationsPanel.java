package iwish.client;

import iwish.common.NotificationDTO;
import iwish.common.Request;
import iwish.common.Response;
import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * Tasks 8 & 9. Add to your main screen, then call loadUnread() right after a successful login
 * (shows notifications received while offline). Live ones arrive as pop-up toasts.
 */
public class NotificationsPanel extends JPanel {
    private final DefaultListModel<String> model = new DefaultListModel<>();

    public NotificationsPanel() {
        super(new BorderLayout(6, 6));
        JList<String> list = new JList<>(model);
        JButton clear = new JButton("Mark all as read");
        clear.addActionListener(e -> markRead());
        add(new JLabel("Notifications"), BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(clear, BorderLayout.SOUTH);

        ServerConnection.get().setNotificationListener(n -> SwingUtilities.invokeLater(() -> {
            model.add(0, n.message);
            showToast(n);
        }));
    }

    @SuppressWarnings("unchecked")
    public void loadUnread() {
        new SwingWorker<Response, Void>() {
            @Override protected Response doInBackground() throws Exception {
                return ServerConnection.get().send(new Request("GET_NOTIFICATIONS"));
            }
            @Override protected void done() {
                try {
                    Response r = get();
                    if (!r.ok) return;
                    model.clear();
                    for (NotificationDTO n : (List<NotificationDTO>) r.data) model.addElement(n.message);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void markRead() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                ServerConnection.get().send(new Request("MARK_NOTIFICATIONS_READ"));
                return null;
            }
            @Override protected void done() { model.clear(); }
        }.execute();
    }

    private static void showToast(NotificationDTO n) {
        JWindow w = new JWindow();
        String icon = NotificationDTO.ITEM_BOUGHT.equals(n.type) ? "\uD83C\uDF81 " : "\u2705 ";
        JLabel l = new JLabel("<html><body style='width:260px'>" + icon + n.message + "</body></html>");
        l.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        l.setOpaque(true);
        l.setBackground(new Color(0xFFF8E1));
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createLineBorder(new Color(0xF9A825), 2));
        p.add(l);
        w.setContentPane(p);
        w.pack();
        Rectangle scr = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        w.setLocation(scr.x + scr.width - w.getWidth() - 16, scr.y + scr.height - w.getHeight() - 16);
        w.setAlwaysOnTop(true);
        w.setVisible(true);
        Timer t = new Timer(6000, e -> w.dispose());
        t.setRepeats(false);
        t.start();
    }
}
