import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.CardLayout;

public class IWishGUI extends JFrame {

    static final Color BG = new Color(246, 248, 255);
    static final Color CARD = Color.WHITE;
    static final Color TEXT = new Color(25, 35, 60);
    static final Color MUTED = new Color(115, 125, 150);
    static final Color PRIMARY = new Color(91, 78, 245);
    static final Color LIGHT_PURPLE = new Color(238, 236, 255);
    static final Color PINK = new Color(245, 92, 150);
    static final Color GREEN = new Color(46, 190, 130);
    static final Color GOLD = new Color(244, 178, 45);
    static final Color BORDER = new Color(232, 235, 244);
    static final Font UI = new Font("SansSerif", Font.PLAIN, 13);
    static final Font UI_BOLD = new Font("SansSerif", Font.BOLD, 13);

    private static final Color[] AVATAR_COLORS = {
        new Color(91, 145, 245), new Color(75, 190, 160), new Color(245, 160, 80),
        new Color(220, 95, 145), new Color(120, 100, 210), new Color(70, 170, 230)
    };

    private final String host;
    private ServerConnection server;
    private User me;

    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final CardLayout pageCards = new CardLayout();
    private final JPanel pageContent = new JPanel(pageCards);
    private final JLabel pageTitle = new JLabel();
    private final List<JButton> navButtons = new ArrayList<>();
    private final JButton bell = new JButton("🔔");
    private final JLabel sidebarName = new JLabel();
    private final Avatar sidebarAvatar = new Avatar("?", PRIMARY);
    private final Avatar headerAvatar = new Avatar("?", PRIMARY);
    private Timer welcomeTimer;

    private final JPanel homeBody = new JPanel(new BorderLayout(0, 18));
    private final JPanel friendsBody = new JPanel(new BorderLayout(0, 16));
    private final JPanel wishBody = new JPanel(new BorderLayout(0, 16));
    private final JPanel notificationsBody = new JPanel(new BorderLayout());

    private final JTextField signInUsername = new JTextField();
    private final JPasswordField signInPassword = new JPasswordField();
    private final JTextField registerUsername = new JTextField();
    private final JTextField registerEmail = new JTextField();
    private final JPasswordField registerPassword = new JPasswordField();

    private String currentPage = "Home";
    private int viewingFriendId;
    private String viewingFriendName;

    public IWishGUI(String host) {
        this.host = host;
        setTitle("i-Wish · Make wishes come true");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 700));
        setSize(1320, 820);
        setLocationRelativeTo(null);
        setContentPane(root);

        root.add(createWelcomePage(), "Welcome");
        root.add(createAuthPage(), "Auth");
        root.add(createAppPage(), "App");
        rootCards.show(root, "Welcome");
    }

    // ---------- Welcome page ----------
    private JPanel createWelcomePage() {
        WelcomePanel welcome = new WelcomePanel();
        welcome.setLayout(new BorderLayout());
        welcome.setBorder(new EmptyBorder(30, 42, 30, 42));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel logo = new JLabel("✦  i-Wish");
        logo.setFont(new Font("SansSerif", Font.BOLD, 25));
        logo.setForeground(TEXT);
        top.add(logo, BorderLayout.WEST);

        JLabel status = new JLabel("●  YOUR HAPPY PLACE");
        status.setFont(new Font("SansSerif", Font.BOLD, 10));
        status.setForeground(GREEN);
        top.add(status, BorderLayout.EAST);
        welcome.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 0, 7, 0);
        c.gridx = 0;
        c.gridy = 0;

        JLabel eyebrow = new JLabel("A LITTLE MAGIC FOR EVERY WISH");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
        eyebrow.setForeground(PRIMARY);
        center.add(eyebrow, c);

        c.gridy++;
        JLabel heading = new JLabel("<html><center>Make wishes.<br><span style='color:#5b4ef5'>Make memories.</span></center></html>");
        heading.setFont(new Font("SansSerif", Font.BOLD, 52));
        heading.setForeground(TEXT);
        center.add(heading, c);

        c.gridy++;
        JLabel subtitle = new JLabel("<html><center>i-Wish brings your people together to turn<br>small dreams into unforgettable moments.</center></html>");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(MUTED);
        center.add(subtitle, c);

        c.gridy++;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 11, 0));
        actions.setOpaque(false);
        JButton start = primaryButton("Enter i-Wish  →");
        start.setPreferredSize(new Dimension(160, 46));
        start.addActionListener(e -> showAuth());
        actions.add(start);

        JButton explore = new RoundedButton("How it works  ?", Color.WHITE, PRIMARY);
        explore.setFont(UI_BOLD);
        explore.setPreferredSize(new Dimension(145, 46));
        explore.addActionListener(e -> showMessage("How i-Wish works",
                "Create a wish list, invite your friends, and let everyone\n"
                + "contribute a little to the things you love.\n\nReady to make your first wish?"));
        actions.add(explore);
        center.add(actions, c);

        c.gridy++;
        JLabel note = new JLabel("✨  Share the joy · Collect the memories · Celebrate together");
        note.setFont(new Font("SansSerif", Font.ITALIC, 12));
        note.setForeground(new Color(150, 140, 190));
        center.add(note, c);
        welcome.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        bottom.setOpaque(false);
        bottom.add(feature("🎁", "Wish together", "Dream bigger with friends"));
        bottom.add(feature("💜", "Feel supported", "Every pound counts"));
        bottom.add(feature("✨", "Celebrate more", "Make moments matter"));
        welcome.add(bottom, BorderLayout.SOUTH);

        welcomeTimer = new Timer(70, e -> welcome.repaint());
        welcomeTimer.start();
        return welcome;
    }

    private JPanel feature(String emoji, String title, String caption) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(UI_BOLD);
        t.setForeground(TEXT);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel d = new JLabel(caption, SwingConstants.CENTER);
        d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        d.setForeground(MUTED);
        d.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(icon);
        p.add(Box.createVerticalStrut(3));
        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(d);
        return p;
    }

    private void showAuth() {
        if (welcomeTimer != null) {
            welcomeTimer.stop();
        }
        rootCards.show(root, "Auth");
    }

    // ---------- Auth page ----------
    private JPanel createAuthPage() {
        JPanel page = new JPanel(new GridLayout(1, 2));
        page.setBackground(Color.WHITE);

        JPanel art = new AuthArtPanel();
        art.setBorder(new EmptyBorder(38, 52, 38, 52));
        art.setLayout(new BorderLayout());
        JLabel logo = new JLabel("✦  i-Wish");
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(new Color(23, 35, 72));
        art.add(logo, BorderLayout.NORTH);

        JPanel artCenter = new JPanel();
        artCenter.setOpaque(false);
        artCenter.setLayout(new BoxLayout(artCenter, BoxLayout.Y_AXIS));
        JLabel copy = new JLabel("<html><center><b>Small contributions...<br><font color='#5b4ef5'>Big happiness!</font></b></center></html>");
        copy.setFont(new Font("SansSerif", Font.PLAIN, 23));
        copy.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel gift = new JLabel("🎁");
        gift.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 130));
        gift.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel hint = new JLabel("Every wish starts with a little magic");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hint.setForeground(MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        artCenter.add(Box.createVerticalGlue());
        artCenter.add(copy);
        artCenter.add(Box.createVerticalStrut(18));
        artCenter.add(gift);
        artCenter.add(Box.createVerticalStrut(8));
        artCenter.add(hint);
        artCenter.add(Box.createVerticalGlue());
        art.add(artCenter, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(54, 62, 35, 62));

        JButton back = new JButton("← Back to welcome");
        back.setBorderPainted(false);
        back.setContentAreaFilled(false);
        back.setHorizontalAlignment(SwingConstants.LEFT);
        back.setForeground(MUTED);
        back.setFont(new Font("SansSerif", Font.PLAIN, 11));
        back.addActionListener(e -> rootCards.show(root, "Welcome"));
        right.add(back, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel tabs = new JPanel(new GridLayout(1, 2));
        tabs.setOpaque(false);
        JButton signInTab = new JButton("Sign In");
        JButton registerTab = new JButton("Register");
        styleAuthTab(signInTab, true);
        styleAuthTab(registerTab, false);
        tabs.add(signInTab);
        tabs.add(registerTab);

        JPanel forms = new JPanel(new CardLayout());
        forms.setOpaque(false);
        forms.add(signInForm(), "Sign In");
        forms.add(registerForm(), "Register");
        CardLayout authCards = (CardLayout) forms.getLayout();

        signInTab.addActionListener(e -> {
            authCards.show(forms, "Sign In");
            styleAuthTab(signInTab, true);
            styleAuthTab(registerTab, false);
        });
        registerTab.addActionListener(e -> {
            authCards.show(forms, "Register");
            styleAuthTab(signInTab, false);
            styleAuthTab(registerTab, true);
        });

        body.add(Box.createVerticalStrut(28));
        body.add(tabs);
        body.add(Box.createVerticalStrut(22));
        body.add(forms);
        right.add(body, BorderLayout.CENTER);

        page.add(art);
        page.add(right);
        return page;
    }

    private void styleAuthTab(JButton button, boolean selected) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(selected ? PRIMARY : MUTED);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createMatteBorder(0, 0, selected ? 2 : 1, 0, selected ? PRIMARY : BORDER));
        button.setFocusPainted(false);
    }

    private JPanel signInForm() {
        JPanel p = formPanel("Sign In", "Pick up where your wishes left off.");
        p.add(field("Username", signInUsername));
        p.add(Box.createVerticalStrut(12));
        p.add(field("Password", signInPassword));
        p.add(Box.createVerticalStrut(18));

        JButton submit = primaryButton("Sign In  →");
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submit.addActionListener(e -> signIn());
        signInPassword.addActionListener(e -> signIn());
        p.add(submit);
        return p;
    }

    private JPanel registerForm() {
        JPanel p = formPanel("Create your account", "Start turning little wishes into big moments.");
        p.add(field("Username", registerUsername));
        p.add(Box.createVerticalStrut(12));
        p.add(field("Email", registerEmail));
        p.add(Box.createVerticalStrut(12));
        p.add(field("Password", registerPassword));
        p.add(Box.createVerticalStrut(18));

        JButton submit = primaryButton("Create Account  →");
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submit.addActionListener(e -> registerAccount());
        registerPassword.addActionListener(e -> registerAccount());
        p.add(submit);
        return p;
    }

    private JPanel formPanel(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 25));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(titleLabel);
        p.add(Box.createVerticalStrut(5));
        p.add(sub);
        p.add(Box.createVerticalStrut(20));
        return p;
    }

    private JPanel field(String title, JTextField input) {
        JPanel group = new JPanel(new BorderLayout(0, 5));
        group.setOpaque(false);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 63));
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(TEXT);
        input.setFont(new Font("SansSerif", Font.PLAIN, 12));
        input.setForeground(TEXT);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(9, 10, 9, 10)));
        group.add(label, BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        return group;
    }

    private void signIn() {
        String username = signInUsername.getText().trim();
        String password = new String(signInPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Sign in", "Please enter your username and password.");
            return;
        }
        if (!connect()) {
            return;
        }

        Response response = call(new Request("user.login").put("username", username).put("password", password));
        if (!response.isOk()) {
            showMessage("Sign in", response.getMessage());
            return;
        }

        me = (User) response.getData();
        signInPassword.setText("");
        afterSignIn();
    }

    private void registerAccount() {
        String username = registerUsername.getText().trim();
        String password = new String(registerPassword.getPassword());

        if (!connect()) {
            return;
        }

        Response response = call(new Request("user.register")
                .put("username", username)
                .put("email", registerEmail.getText().trim())
                .put("password", password));

        if (!response.isOk()) {
            showMessage("Register", response.getMessage());
            return;
        }

        registerPassword.setText("");
        signInUsername.setText(username);
        signInPassword.setText(password);
        signIn();
    }

    private boolean connect() {
        if (server != null) {
            return true;
        }
        try {
            server = new ServerConnection(host, Server.PORT);
            server.setNotificationListener(notification ->
                    SwingUtilities.invokeLater(() -> onNotification(notification)));
            return true;
        } catch (IOException e) {
            showMessage("i-Wish", "Cannot reach the server at " + host + ":" + Server.PORT
                    + ".\nStart the server first (run ServerMain).");
            return false;
        }
    }

    private void afterSignIn() {
        sidebarName.setText(me.getUsername());
        sidebarAvatar.set(initial(me.getUsername()), colorFor(me.getUsername()));
        headerAvatar.set(initial(me.getUsername()), colorFor(me.getUsername()));
        viewingFriendId = 0;
        viewingFriendName = null;
        rootCards.show(root, "App");
        showPage("Home");
    }

    private void logout() {
        if (server != null) {
            server.close();
            server = null;
        }
        me = null;
        signInUsername.setText("");
        signInPassword.setText("");
        rootCards.show(root, "Auth");
    }

    private void onNotification(Notification notification) {
        toast(notification);
        refresh();
    }

    // ---------- App shell ----------
    private JPanel createAppPage() {
        JPanel app = new JPanel(new BorderLayout());
        app.setBackground(BG);
        app.add(createSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(27, 30, 27, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        pageTitle.setBorder(new EmptyBorder(0, 0, 0, 12));
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        pageTitle.setForeground(TEXT);
        header.add(pageTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        bell.setFont(new Font("SansSerif", Font.BOLD, 20));
        bell.setForeground(TEXT);
        bell.setBorderPainted(false);
        bell.setContentAreaFilled(false);
        bell.setFocusPainted(false);
        bell.addActionListener(e -> showPage("Notifications"));
        right.add(bell);
        headerAvatar.setPreferredSize(new Dimension(40, 40));
        right.add(headerAvatar);
        header.add(right, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        homeBody.setOpaque(false);
        friendsBody.setOpaque(false);
        wishBody.setOpaque(false);
        notificationsBody.setOpaque(false);

        pageContent.setOpaque(false);
        pageContent.setBorder(new EmptyBorder(22, 0, 0, 0));
        pageContent.add(homeBody, "Home");
        pageContent.add(friendsBody, "Friends");
        pageContent.add(wishBody, "Wish Lists");
        pageContent.add(notificationsBody, "Notifications");
        main.add(pageContent, BorderLayout.CENTER);

        app.add(main, BorderLayout.CENTER);
        return app;
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(235, 0));
        side.setBackground(Color.WHITE);
        side.setBorder(new EmptyBorder(28, 18, 22, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("  ✦  i-Wish");
        logo.setFont(new Font("SansSerif", Font.BOLD, 25));
        logo.setForeground(TEXT);
        logo.setBorder(new EmptyBorder(0, 8, 35, 0));
        top.add(logo);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        addNav(nav, "Home", "🏠");
        addNav(nav, "Friends", "👥");
        addNav(nav, "Wish Lists", "🎁");
        addNav(nav, "Notifications", "🔔");
        top.add(nav);
        side.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        bottom.setOpaque(false);

        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setOpaque(false);
        sidebarAvatar.setPreferredSize(new Dimension(42, 42));
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        sidebarName.setBorder(new EmptyBorder(0, 0, 0, 10));
        sidebarName.setFont(new Font("SansSerif", Font.BOLD, 13));
        sidebarName.setForeground(TEXT);
        JLabel online = new JLabel("● Online");
        online.setFont(new Font("SansSerif", Font.PLAIN, 11));
        online.setForeground(GREEN);
        names.add(sidebarName);
        names.add(Box.createVerticalStrut(3));
        names.add(online);
        profile.add(sidebarAvatar, BorderLayout.WEST);
        profile.add(names, BorderLayout.CENTER);

        JButton logoutButton = new RoundedButton("Log out", LIGHT_PURPLE, PRIMARY);
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutButton.setPreferredSize(new Dimension(195, 38));
        logoutButton.addActionListener(e -> logout());

        bottom.add(profile, BorderLayout.NORTH);
        bottom.add(logoutButton, BorderLayout.SOUTH);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private void addNav(JPanel nav, String name, String icon) {
        JButton b = new JButton("  " + icon + "   " + name);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setPreferredSize(new Dimension(195, 46));
        b.setFont(UI_BOLD);
        b.setForeground(MUTED);
        b.setBackground(Color.WHITE);
        b.setBorder(new EmptyBorder(0, 10, 0, 0));
        b.setFocusPainted(false);
        b.setName(name);
        b.addActionListener(e -> showPage(name));
        nav.add(b);
        nav.add(Box.createVerticalStrut(6));
        navButtons.add(b);
    }

    private void showPage(String page) {
        currentPage = page;
        pageCards.show(pageContent, page);

        if (page.equals("Home")) {
            pageTitle.setText("Welcome back, " + me.getUsername() + "!");
        } else if (page.equals("Wish Lists")) {
            pageTitle.setText("Wish Lists");
        } else {
            pageTitle.setText(page);
        }

        for (JButton button : navButtons) {
            boolean selected = page.equals(button.getName());
            button.setBackground(selected ? LIGHT_PURPLE : Color.WHITE);
            button.setForeground(selected ? PRIMARY : MUTED);
        }
        refresh();
    }

    private void refresh() {
        if (me == null) {
            return;
        }
        if (currentPage.equals("Home")) {
            refreshHome();
        } else if (currentPage.equals("Friends")) {
            refreshFriends();
        } else if (currentPage.equals("Wish Lists")) {
            refreshWishes();
        } else {
            refreshNotifications();
        }
    }

    // ---------- Home ----------
    private void refreshHome() {
        List<Friend> friends = fetch(new Request("friends.list"));
        List<Friend> requests = fetch(new Request("friends.requests"));
        List<WishlistItem> mine = fetch(new Request("wishlist.mine"));
        List<Notification> notifications = fetch(new Request("notifications.list"));
        updateBell(notifications);

        homeBody.removeAll();

        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 0));
        stats.setOpaque(false);
        stats.add(statCard("👥", String.valueOf(friends.size()), "Friends", new Color(65, 170, 240)));
        stats.add(statCard("🎁", String.valueOf(mine.size()), "My Wishes", new Color(160, 105, 245)));
        stats.add(statCard("📩", String.valueOf(requests.size()), "Requests", GOLD));
        stats.add(statCard("🔔", String.valueOf(unread(notifications)), "Unread", PINK));

        JPanel middle = new JPanel(new BorderLayout(18, 0));
        middle.setOpaque(false);

        JPanel activity = cardPanel();
        activity.setLayout(new BorderLayout(0, 12));
        activity.add(sectionTitle("Recent Activity"), BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(0, 1, 0, 2));
        rows.setOpaque(false);
        if (notifications.isEmpty()) {
            rows.add(hint("Nothing has happened yet. Add a friend to get started!"));
        } else {
            for (int i = 0; i < notifications.size() && i < 5; i++) {
                rows.add(notificationRow(notifications.get(i)));
            }
        }
        activity.add(rows, BorderLayout.NORTH);

        GradientCard promo = new GradientCard();
        promo.setLayout(new GridBagLayout());
        JPanel promoInner = new JPanel();
        promoInner.setOpaque(false);
        promoInner.setLayout(new BoxLayout(promoInner, BoxLayout.Y_AXIS));
        JLabel gift = new JLabel("🎁");
        gift.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 63));
        gift.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel text = new JLabel("<html><center><b>Together<br>we make wishes<br>come true</b></center></html>");
        text.setFont(new Font("SansSerif", Font.BOLD, 17));
        text.setForeground(TEXT);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        promoInner.add(gift);
        promoInner.add(Box.createVerticalStrut(8));
        promoInner.add(text);
        promo.add(promoInner);
        promo.setPreferredSize(new Dimension(260, 0));

        middle.add(activity, BorderLayout.CENTER);
        middle.add(promo, BorderLayout.EAST);

        homeBody.add(stats, BorderLayout.NORTH);
        homeBody.add(middle, BorderLayout.CENTER);
        homeBody.revalidate();
        homeBody.repaint();
    }

    // ---------- Friends ----------
    private void refreshFriends() {
        List<Friend> friends = fetch(new Request("friends.list"));
        List<Friend> requests = fetch(new Request("friends.requests"));

        friendsBody.removeAll();

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(sectionTitle("My Friends (" + friends.size() + ")"), BorderLayout.WEST);
        JButton add = primaryButton("+  Add Friend");
        add.setPreferredSize(new Dimension(150, 40));
        add.addActionListener(e -> addFriendDialog());
        top.add(add, BorderLayout.EAST);

        ScrollPanel column = new ScrollPanel(new BorderLayout(0, 14));

        if (!requests.isEmpty()) {
            JPanel card = cardPanel();
            card.setLayout(new BorderLayout(0, 12));
            card.add(sectionTitle("Friend Requests (" + requests.size() + ")"), BorderLayout.NORTH);
            JPanel rows = new JPanel(new GridLayout(0, 1, 0, 6));
            rows.setOpaque(false);
            for (Friend friend : requests) {
                rows.add(requestRow(friend));
            }
            card.add(rows, BorderLayout.CENTER);
            column.add(card, BorderLayout.NORTH);
        }

        JPanel list = cardPanel();
        list.setLayout(new BorderLayout(0, 12));
        JPanel rows = new JPanel(new GridLayout(0, 1, 0, 6));
        rows.setOpaque(false);
        if (friends.isEmpty()) {
            rows.add(hint("You have no friends yet. Use Add Friend to send your first request."));
        } else {
            for (Friend friend : friends) {
                rows.add(friendRow(friend));
            }
        }
        list.add(rows, BorderLayout.NORTH);
        column.add(list, BorderLayout.CENTER);

        friendsBody.add(top, BorderLayout.NORTH);
        friendsBody.add(scroll(column), BorderLayout.CENTER);
        friendsBody.revalidate();
        friendsBody.repaint();
    }

    private JPanel friendRow(Friend friend) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(9, 4, 9, 4));

        Avatar avatar = new Avatar(initial(friend.getUsername()), colorFor(friend.getUsername()));
        avatar.setPreferredSize(new Dimension(43, 43));

        JLabel name = new JLabel(friend.getUsername());
        name.setFont(new Font("SansSerif", Font.BOLD, 14));
        name.setForeground(TEXT);
        name.setBorder(new EmptyBorder(0, 0, 0, 10));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton wishes = smallButton("View Wishes");
        wishes.addActionListener(e -> {
            viewingFriendId = friend.getUserId();
            viewingFriendName = friend.getUsername();
            showPage("Wish Lists");
        });
        JButton remove = smallButton("Remove");
        remove.addActionListener(e -> {
            if (confirm("Remove " + friend.getUsername() + " from your friends?")
                    && perform(new Request("friends.remove").put("friendId", friend.getUserId()))) {
                refreshFriends();
            }
        });
        buttons.add(wishes);
        buttons.add(remove);

        row.add(avatar, BorderLayout.WEST);
        row.add(name, BorderLayout.CENTER);
        row.add(buttons, BorderLayout.EAST);
        return row;
    }

    private JPanel requestRow(Friend friend) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(9, 4, 9, 4));

        Avatar avatar = new Avatar(initial(friend.getUsername()), colorFor(friend.getUsername()));
        avatar.setPreferredSize(new Dimension(43, 43));

        JLabel name = new JLabel(friend.getUsername() + " wants to be your friend");
        name.setFont(new Font("SansSerif", Font.BOLD, 14));
        name.setForeground(TEXT);
        name.setBorder(new EmptyBorder(0, 0, 0, 10));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton accept = primaryButton("Accept");
        accept.setPreferredSize(new Dimension(95, 34));
        accept.addActionListener(e -> {
            if (perform(new Request("friends.accept").put("friendId", friend.getUserId()))) {
                refreshFriends();
            }
        });
        JButton decline = smallButton("Decline");
        decline.addActionListener(e -> {
            if (perform(new Request("friends.decline").put("friendId", friend.getUserId()))) {
                refreshFriends();
            }
        });
        buttons.add(accept);
        buttons.add(decline);

        row.add(avatar, BorderLayout.WEST);
        row.add(name, BorderLayout.CENTER);
        row.add(buttons, BorderLayout.EAST);
        return row;
    }

    private void addFriendDialog() {
        String username = JOptionPane.showInputDialog(this,
                "Enter the username of the friend:", "Add Friend", JOptionPane.PLAIN_MESSAGE);
        if (username != null && !username.trim().isEmpty()
                && perform(new Request("friends.add").put("username", username.trim()))) {
            refreshFriends();
        }
    }

    // ---------- Wish lists ----------
    private void refreshWishes() {
        boolean mine = viewingFriendId == 0;
        List<Friend> friends = fetch(new Request("friends.list"));
        List<WishlistItem> items = mine
                ? fetch(new Request("wishlist.mine"))
                : fetch(new Request("friends.wishlist").put("friendId", viewingFriendId));

        wishBody.removeAll();

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(sectionTitle(mine ? "My Wish List" : viewingFriendName + "'s Wish List"), BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JComboBox<String> whose = new JComboBox<>();
        whose.addItem("My Wish List");
        for (Friend friend : friends) {
            whose.addItem(friend.getUsername());
        }
        whose.setSelectedIndex(0);
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).getUserId() == viewingFriendId) {
                whose.setSelectedIndex(i + 1);
            }
        }
        whose.setFont(UI);
        whose.setPreferredSize(new Dimension(190, 38));
        whose.addActionListener(e -> {
            int index = whose.getSelectedIndex();
            if (index <= 0) {
                viewingFriendId = 0;
                viewingFriendName = null;
            } else {
                viewingFriendId = friends.get(index - 1).getUserId();
                viewingFriendName = friends.get(index - 1).getUsername();
            }
            refreshWishes();
        });
        controls.add(whose);

        if (mine) {
            JButton add = primaryButton("+  Add Wish");
            add.setPreferredSize(new Dimension(140, 38));
            add.addActionListener(e -> addWishDialog());
            controls.add(add);
        }
        top.add(controls, BorderLayout.EAST);

        ScrollPanel grid = new ScrollPanel(new GridLayout(0, 2, 14, 14));
        if (items.isEmpty()) {
            JPanel empty = cardPanel();
            empty.setLayout(new BorderLayout());
            empty.add(hint(mine
                    ? "Your wish list is empty. Press Add Wish to pick something from the catalog."
                    : viewingFriendName + " has not added any wishes yet."), BorderLayout.NORTH);
            grid.add(empty);
        } else {
            for (WishlistItem item : items) {
                grid.add(wishCard(item, mine));
            }
        }

        wishBody.add(top, BorderLayout.NORTH);
        wishBody.add(scroll(grid), BorderLayout.CENTER);
        wishBody.revalidate();
        wishBody.repaint();
    }

    private JPanel wishCard(WishlistItem item, boolean mine) {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(10, 8));

        JLabel picture = new JLabel(emojiFor(item.getItemName()), SwingConstants.CENTER);
        picture.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        picture.setOpaque(true);
        picture.setBackground(new Color(245, 246, 252));
        picture.setPreferredSize(new Dimension(85, 85));

        boolean completed = "completed".equals(item.getStatus());

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(item.getItemName());
        name.setFont(new Font("SansSerif", Font.BOLD, 15));
        name.setForeground(TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel price = new JLabel(money(item.getPrice()));
        price.setFont(new Font("SansSerif", Font.BOLD, 13));
        price.setForeground(MUTED);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);

        JProgressBar bar = new JProgressBar(0, (int) Math.round(item.getPrice()));
        bar.setValue((int) Math.round(item.getContributedAmount()));
        bar.setPreferredSize(new Dimension(100, 8));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setForeground(completed ? GREEN : PRIMARY);
        bar.setBackground(new Color(231, 233, 243));
        bar.setBorderPainted(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel progress = new JLabel(completed
                ? "✓  Fully funded"
                : money(item.getContributedAmount()) + " of " + money(item.getPrice()));
        progress.setFont(new Font("SansSerif", Font.PLAIN, 10));
        progress.setForeground(completed ? GREEN : MUTED);
        progress.setAlignmentX(Component.LEFT_ALIGNMENT);

        name.setBorder(new EmptyBorder(0, 0, 0, 10));
        price.setBorder(new EmptyBorder(0, 0, 0, 10));
        progress.setBorder(new EmptyBorder(0, 0, 0, 10));
        info.add(name);
        info.add(Box.createVerticalStrut(5));
        info.add(price);
        info.add(Box.createVerticalStrut(9));
        info.add(bar);
        info.add(Box.createVerticalStrut(4));
        info.add(progress);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);

        if (mine) {
            JButton edit = smallButton("Edit");
            edit.addActionListener(e -> editWishDialog(item));
            JButton delete = smallButton("Delete");
            delete.addActionListener(e -> {
                if (confirm("Remove " + item.getItemName() + " from your wish list?")
                        && perform(new Request("wishlist.delete").put("wishlistItemId", item.getWishlistItemId()))) {
                    refreshWishes();
                }
            });
            buttons.add(edit);
            buttons.add(delete);
        } else {
            JButton contribute = primaryButton("Contribute");
            contribute.setPreferredSize(new Dimension(110, 36));
            contribute.setEnabled(!completed);
            contribute.addActionListener(e -> contributeDialog(item));
            buttons.add(contribute);
        }

        card.add(picture, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void addWishDialog() {
        List<Item> catalog = fetch(new Request("wishlist.catalog"));
        if (catalog.isEmpty()) {
            showMessage("Add Wish", "The catalog is empty. Ask the admin to add items on the server.");
            return;
        }

        JComboBox<Item> items = new JComboBox<>(catalog.toArray(new Item[0]));
        items.setFont(UI);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Pick something you wish for:"), BorderLayout.NORTH);
        panel.add(items, BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(this, panel, "Add Wish",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        Item chosen = (Item) items.getSelectedItem();
        if (chosen != null && perform(new Request("wishlist.add").put("itemId", chosen.getItemId()))) {
            refreshWishes();
        }
    }

    private void editWishDialog(WishlistItem item) {
        JTextField name = new JTextField(item.getItemName());
        JTextField description = new JTextField(item.getItemDescription());
        JTextField price = new JTextField(String.valueOf(item.getPrice()));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Name:"));
        panel.add(name);
        panel.add(new JLabel("Description:"));
        panel.add(description);
        panel.add(new JLabel("Price:"));
        panel.add(price);

        if (JOptionPane.showConfirmDialog(this, panel, "Edit Wish",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        double newPrice;
        try {
            newPrice = Double.parseDouble(price.getText().trim());
        } catch (NumberFormatException e) {
            showMessage("Edit Wish", "Please type the price as a number.");
            return;
        }

        if (perform(new Request("wishlist.update")
                .put("itemId", item.getItemId())
                .put("itemName", name.getText().trim())
                .put("itemDescription", description.getText().trim())
                .put("price", newPrice))) {
            refreshWishes();
        }
    }

    private void contributeDialog(WishlistItem item) {
        double remaining = Math.round((item.getPrice() - item.getContributedAmount()) * 100) / 100.0;
        if (remaining <= 0) {
            showMessage("Contribute", "This gift is already fully funded.");
            return;
        }

        JSpinner amount = new JSpinner(new SpinnerNumberModel(
                Math.min(50.0, remaining), 0.01, remaining, 10.0));
        amount.setEditor(new JSpinner.NumberEditor(amount, "0.00"));

        JButton payRest = new JButton("Pay the rest");
        payRest.addActionListener(e -> amount.setValue(remaining));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Gift:"));
        panel.add(new JLabel(item.getItemName()));
        panel.add(new JLabel("Price:"));
        panel.add(new JLabel(money(item.getPrice())));
        panel.add(new JLabel("Collected:"));
        panel.add(new JLabel(money(item.getContributedAmount())));
        panel.add(new JLabel("Remaining:"));
        panel.add(new JLabel(money(remaining)));
        panel.add(new JLabel("Your amount:"));
        panel.add(amount);
        panel.add(new JLabel());
        panel.add(payRest);

        if (JOptionPane.showConfirmDialog(this, panel, "Contribute to a Wish",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        double value = ((Number) amount.getValue()).doubleValue();
        if (perform(new Request("contribute")
                .put("wishlistItemId", item.getWishlistItemId())
                .put("amount", value))) {
            refreshWishes();
        }
    }

    // ---------- Notifications ----------
    private void refreshNotifications() {
        List<Notification> notifications = fetch(new Request("notifications.list"));
        updateBell(notifications);

        notificationsBody.removeAll();

        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionTitle("Notifications"), BorderLayout.WEST);
        JButton markRead = smallButton("Mark all as read");
        markRead.addActionListener(e -> {
            call(new Request("notifications.read"));
            refreshNotifications();
        });
        header.add(markRead, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        ScrollPanel rows = new ScrollPanel(new GridLayout(0, 1, 0, 2));
        if (notifications.isEmpty()) {
            rows.add(hint("No notifications yet."));
        } else {
            for (Notification notification : notifications) {
                rows.add(notificationRow(notification));
            }
        }
        card.add(scroll(rows), BorderLayout.CENTER);

        notificationsBody.add(card, BorderLayout.CENTER);
        notificationsBody.revalidate();
        notificationsBody.repaint();
    }

    private JPanel notificationRow(Notification notification) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));

        Avatar avatar = new Avatar("🎁", notification.isRead() ? MUTED : PRIMARY);
        avatar.setPreferredSize(new Dimension(40, 40));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel message = new JLabel(notification.getMessage());
        message.setFont(new Font("SansSerif", notification.isRead() ? Font.PLAIN : Font.BOLD, 12));
        message.setForeground(TEXT);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel time = new JLabel(when(notification.getCreatedAt()));
        time.setFont(new Font("SansSerif", Font.PLAIN, 10));
        time.setForeground(MUTED);
        time.setAlignmentX(Component.LEFT_ALIGNMENT);
        message.setBorder(new EmptyBorder(0, 0, 0, 10));
        time.setBorder(new EmptyBorder(0, 0, 0, 10));
        info.add(message);
        info.add(Box.createVerticalStrut(4));
        info.add(time);

        row.add(avatar, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        return row;
    }

    private void updateBell(List<Notification> notifications) {
        int unread = unread(notifications);
        bell.setText(unread == 0 ? "🔔" : "🔔 " + unread);
        bell.setForeground(unread == 0 ? TEXT : PRIMARY);
    }

    private static int unread(List<Notification> notifications) {
        int count = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    private void toast(Notification notification) {
        JWindow window = new JWindow();
        JLabel label = new JLabel("<html><body style='width:260px'>🎁  " + notification.getMessage() + "</body></html>");
        label.setBorder(new EmptyBorder(12, 14, 12, 14));
        label.setOpaque(true);
        label.setBackground(new Color(255, 248, 225));
        label.setFont(UI_BOLD);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(GOLD, 2));
        panel.add(label);
        window.setContentPane(panel);
        window.pack();

        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        window.setLocation(screen.x + screen.width - window.getWidth() - 16,
                screen.y + screen.height - window.getHeight() - 16);
        window.setAlwaysOnTop(true);
        window.setVisible(true);

        Timer timer = new Timer(6000, e -> window.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    // ---------- Talking to the server ----------
    private Response call(Request request) {
        if (server == null) {
            return Response.fail(null);
        }
        try {
            return server.send(request);
        } catch (IOException e) {
            showMessage("i-Wish", "Lost the connection to the server. Please sign in again.");
            logout();
            return Response.fail(null);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> fetch(Request request) {
        Response response = call(request);
        return response.isOk() ? (List<T>) response.getData() : new ArrayList<T>();
    }

    private boolean perform(Request request) {
        Response response = call(request);
        if (response.getMessage() != null) {
            showMessage(response.isOk() ? "i-Wish" : "Oops", response.getMessage());
        }
        return response.isOk();
    }

    // ---------- Small helpers ----------
    private void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(this, question, "i-Wish",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static JLabel hint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.ITALIC, 12));
        label.setForeground(MUTED);
        label.setBorder(new EmptyBorder(10, 4, 10, 4));
        return label;
    }

    private static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(TEXT);
        label.setBorder(new EmptyBorder(0, 0, 0, 10));
        return label;
    }

    private static JPanel cardPanel() {
        JPanel p = new RoundedPanel(CARD, 18);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    private static JPanel statCard(String icon, String number, String label, Color iconColor) {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(12, 0));
        JLabel i = new JLabel(icon, SwingConstants.CENTER);
        i.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        i.setForeground(iconColor);
        i.setOpaque(true);
        i.setBackground(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 28));
        i.setPreferredSize(new Dimension(52, 52));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel n = new JLabel(number);
        n.setFont(new Font("SansSerif", Font.BOLD, 22));
        n.setForeground(TEXT);
        n.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        n.setBorder(new EmptyBorder(0, 0, 0, 10));
        l.setBorder(new EmptyBorder(0, 0, 0, 10));
        info.add(n);
        info.add(Box.createVerticalStrut(3));
        info.add(l);

        card.add(i, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private static JButton primaryButton(String text) {
        JButton b = new RoundedButton(text, PRIMARY, Color.WHITE);
        b.setFont(UI_BOLD);
        b.setFocusPainted(false);
        return b;
    }

    private static JButton smallButton(String text) {
        JButton b = new RoundedButton(text, LIGHT_PURPLE, PRIMARY);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setPreferredSize(new Dimension(105, 34));
        b.setFocusPainted(false);
        return b;
    }

    private static JScrollPane scroll(JComponent view) {
        JScrollPane pane = new JScrollPane(view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setOpaque(false);
        pane.getViewport().setOpaque(false);
        pane.setBorder(null);
        pane.getVerticalScrollBar().setUnitIncrement(16);
        return pane;
    }

    private static String initial(String name) {
        return name == null || name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
    }

    private static Color colorFor(String name) {
        return AVATAR_COLORS[Math.abs(name.hashCode()) % AVATAR_COLORS.length];
    }

    private static String emojiFor(String itemName) {
        String name = itemName.toLowerCase();
        if (name.contains("headphone") || name.contains("earbud")) return "🎧";
        if (name.contains("keyboard") || name.contains("mouse")) return "⌨";
        if (name.contains("watch")) return "⌚";
        if (name.contains("coffee") || name.contains("mug")) return "☕";
        if (name.contains("backpack") || name.contains("bag")) return "🎒";
        if (name.contains("lamp") || name.contains("light")) return "💡";
        if (name.contains("laptop") || name.contains("computer")) return "💻";
        if (name.contains("book")) return "📚";
        if (name.contains("phone")) return "📱";
        if (name.contains("shoe") || name.contains("sneaker")) return "👟";
        return "🎁";
    }

    private static String money(double amount) {
        return amount == Math.floor(amount)
                ? String.format("%,.0f EGP", amount)
                : String.format("%,.2f EGP", amount);
    }

    private static String when(Timestamp time) {
        return time == null ? "" : new SimpleDateFormat("d MMM yyyy · HH:mm").format(time);
    }

    // ---------- Custom painting (design by Bichoy) ----------
    static class ScrollPanel extends JPanel implements Scrollable {
        ScrollPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return 64; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    static class AuthArtPanel extends JPanel {
        AuthArtPanel() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(239, 235, 255), getWidth(), getHeight(), new Color(247, 239, 255)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillOval(-130, 260, 400, 250);
            g2.setColor(new Color(255, 190, 220, 75));
            g2.fillOval(getWidth() - 230, getHeight() - 170, 300, 220);
            g2.setColor(new Color(244, 178, 45, 70));
            g2.fillOval(getWidth() - 175, 105, 75, 75);
            g2.dispose();
        }
    }

    static class RoundedPanel extends JPanel {
        final Color color;
        final int radius;

        RoundedPanel(Color color, int radius) {
            this.color = color;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fill(new RoundRectangle2D.Double(2, 3, getWidth() - 4, getHeight() - 4, radius, radius));
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 4, getHeight() - 4, radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class GradientCard extends JPanel {
        GradientCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(235, 232, 255), getWidth(), getHeight(), new Color(255, 235, 245)));
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 22, 22);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class WelcomePanel extends JPanel {
        private float phase;

        WelcomePanel() {
            setOpaque(true);
            setBackground(BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            phase += .012f;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintOrb(g2, 90, 100, 240, new Color(205, 198, 255, 85));
            paintOrb(g2, getWidth() - 190, getHeight() - 160, 300, new Color(255, 197, 222, 80));
            paintOrb(g2, getWidth() / 2 + (int) (Math.sin(phase) * 20), 80, 85, new Color(244, 178, 45, 75));
            for (int i = 0; i < 12; i++) {
                double angle = phase * (i % 2 == 0 ? 1 : -1) + i * .57;
                int x = 35 + (int) ((getWidth() - 70) * ((i * 71 % 100) / 100.0));
                int y = 95 + (int) ((getHeight() - 170) * ((i * 43 % 100) / 100.0));
                int size = 3 + i % 4;
                g2.setColor(i % 3 == 0 ? new Color(91, 78, 245, 90) : new Color(245, 92, 150, 75));
                g2.fill(new Ellipse2D.Double(x + Math.sin(angle) * 7, y + Math.cos(angle) * 7, size, size));
            }
            g2.dispose();
        }

        private void paintOrb(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            g2.fillOval(x - size / 2, y - size / 2, size, size);
        }
    }

    static class RoundedButton extends JButton {
        final Color bg;
        final Color fg;

        RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            this.fg = fg;
            setContentAreaFilled(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = getModel().isPressed() ? bg.darker() : (getModel().isRollover() ? bg.brighter() : bg);
            if (!isEnabled()) {
                c = BORDER;
            }
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(isEnabled() ? fg : MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    static class Avatar extends JPanel {
        private String letter;
        private Color color;

        Avatar(String letter, Color color) {
            this.letter = letter;
            this.color = color;
            setOpaque(false);
        }

        void set(String letter, Color color) {
            this.letter = letter;
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(1, 1, getWidth() - 3, getHeight() - 3);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, getWidth() / 2)));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letter, (getWidth() - fm.stringWidth(letter)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new IWishGUI(host).setVisible(true));
    }
}
