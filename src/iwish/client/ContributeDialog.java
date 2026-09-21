package iwish.client;

import iwish.common.Request;
import iwish.common.Response;
import iwish.common.WishItemDTO;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.*;

/** Task 7. Open from the "friend's wish list" screen: new ContributeDialog(frame, item, this::reload).setVisible(true) */
public class ContributeDialog extends JDialog {

    public ContributeDialog(Frame owner, WishItemDTO item, Runnable onSuccess) {
        super(owner, "Contribute to gift", true);
        BigDecimal remaining = item.remaining();

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(
                Math.min(10.0, remaining.doubleValue()), 0.01, remaining.doubleValue(), 1.0));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00"));

        JPanel info = new JPanel(new GridLayout(0, 2, 8, 8));
        info.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        info.add(new JLabel("Item:"));        info.add(new JLabel(item.itemName));
        info.add(new JLabel("Price:"));       info.add(new JLabel(item.price.toPlainString()));
        info.add(new JLabel("Collected:"));   info.add(new JLabel(item.collected.toPlainString()));
        info.add(new JLabel("Remaining:"));   info.add(new JLabel(remaining.toPlainString()));
        info.add(new JLabel("Your amount:")); info.add(spinner);

        JButton contribute = new JButton("Contribute");
        JButton payRest = new JButton("Pay remaining");
        JButton cancel = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(payRest); buttons.add(contribute); buttons.add(cancel);

        payRest.addActionListener(e -> spinner.setValue(remaining.doubleValue()));
        cancel.addActionListener(e -> dispose());

        contribute.addActionListener(e -> {
            BigDecimal amount = BigDecimal.valueOf(((Number) spinner.getValue()).doubleValue())
                                          .setScale(2, RoundingMode.HALF_UP);
            contribute.setEnabled(false);
            new SwingWorker<Response, Void>() {
                @Override protected Response doInBackground() throws Exception {
                    return ServerConnection.get().send(
                        new Request("CONTRIBUTE").put("wishId", item.wishId).put("amount", amount));
                }
                @Override protected void done() {
                    try {
                        Response r = get();
                        JOptionPane.showMessageDialog(ContributeDialog.this, r.message,
                            r.ok ? "Done" : "Error",
                            r.ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                        if (r.ok) { if (onSuccess != null) onSuccess.run(); dispose(); }
                        else contribute.setEnabled(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ContributeDialog.this,
                            "Connection problem: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        contribute.setEnabled(true);
                    }
                }
            }.execute();
        });

        setLayout(new BorderLayout());
        add(info, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }
}
