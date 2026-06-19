package com.library.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.library.i18n.Language;
import com.library.i18n.MessageManager;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.MemberService;

import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame {
    private final BookService bookService;
    private final MemberService memberService;
    private final BorrowService borrowService;
    private final MessageManager msg;
    private JTabbedPane tabbedPane;
    private JPanel toolbar;

    public MainGUI(BookService bookService, MemberService memberService, BorrowService borrowService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.borrowService = borrowService;
        this.msg = MessageManager.getInstance();
        initUI();
    }

    private void initUI() {
        setTitle(msg.getMessage("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);
        applyOrientation();

        buildToolbar();
        buildMenuBar();
        buildTabs();
        buildStatusBar();
    }

    private void applyOrientation() {
        ComponentOrientation orientation = isRTL()
                ? ComponentOrientation.RIGHT_TO_LEFT
                : ComponentOrientation.LEFT_TO_RIGHT;
        applyOrientation(this, orientation);
    }

    private boolean isRTL() {
        return msg.getCurrentLanguage() == Language.FA;
    }

    private void applyOrientation(Component c, ComponentOrientation o) {
        c.setComponentOrientation(o);
        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyOrientation(child, o);
            }
        }
    }

    private void buildToolbar() {
        toolbar = new JPanel(new FlowLayout(isRTL() ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 2));
        toolbar.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        JButton newBtn = createToolbarButton(msg.getMessage("gui.toolbar.new") + " +", e -> {
            int idx = tabbedPane.getSelectedIndex();
            Component c = tabbedPane.getComponentAt(idx);
            if (c instanceof Refreshable r) r.refresh();
        });
        JButton refreshBtn = createToolbarButton(msg.getMessage("gui.toolbar.refresh") + " F5", e -> refreshCurrentTab());
        JButton exportBtn = createToolbarButton(msg.getMessage("gui.toolbar.export"), e -> {
            int idx = tabbedPane.getSelectedIndex();
            Component c = tabbedPane.getComponentAt(idx);
            if (c instanceof Exportable e2) e2.exportCSV();
        });

        JToggleButton langBtn = new JToggleButton(msg.getMessage("gui.toolbar.langToggle"));
        langBtn.setToolTipText("FA / EN");
        langBtn.addActionListener(e -> toggleLanguage());

        toolbar.add(newBtn);
        toolbar.add(refreshBtn);
        toolbar.add(exportBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(langBtn);

        add(toolbar, BorderLayout.NORTH);
    }

    private JButton createToolbarButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.addActionListener(action);
        return btn;
    }

    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        JMenu fileMenu = new JMenu(isRTL() ? "فایل" : "File");
        JMenuItem exitItem = new JMenuItem(isRTL() ? "خروج" : "Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu viewMenu = new JMenu(msg.getMessage("menu.settings"));
        JMenuItem refreshItem = new JMenuItem(msg.getMessage("gui.toolbar.refresh"));
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshCurrentTab());
        viewMenu.add(refreshItem);
        menuBar.add(viewMenu);

        JMenu helpMenu = new JMenu(isRTL() ? "راهنما" : "Help");
        JMenuItem aboutItem = new JMenuItem(msg.getMessage("gui.about.title"));
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void buildTabs() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        tabbedPane.addTab(msg.getMessage("gui.tab.dashboard"), new DashboardPanel(bookService, memberService, borrowService));
        tabbedPane.addTab(msg.getMessage("gui.tab.books"), new BooksPanel(bookService));
        tabbedPane.addTab(msg.getMessage("gui.tab.members"), new MembersPanel(memberService, borrowService));
        tabbedPane.addTab(msg.getMessage("gui.tab.borrows"), new BorrowsPanel(borrowService, bookService, memberService));

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx >= 0) {
                Component c = tabbedPane.getComponentAt(idx);
                if (c instanceof Refreshable r) r.refresh();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void buildStatusBar() {
        JPanel statusPanel = new JPanel(new FlowLayout(isRTL() ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 2));
        statusPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        JLabel statusLabel = new JLabel(msg.getMessage("app.welcome"));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void refreshCurrentTab() {
        int idx = tabbedPane.getSelectedIndex();
        if (idx >= 0) {
            Component c = tabbedPane.getComponentAt(idx);
            if (c instanceof Refreshable r) r.refresh();
        }
    }

    private void toggleLanguage() {
        Language current = msg.getCurrentLanguage();
        msg.setLanguage(current == Language.FA ? Language.EN : Language.FA);
        dispose();
        launch(bookService, memberService, borrowService);
    }

    private void showAboutDialog() {
        String msg_text = msg.getMessage("gui.about.appName") + "\n"
                + msg.getMessage("gui.about.version") + "\n"
                + msg.getMessage("gui.about.java") + ": " + System.getProperty("java.version") + "\n\n"
                + msg.getMessage("gui.about.description");
        JTextArea area = new JTextArea(msg_text);
        area.setEditable(false);
        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setBackground(UIManager.getColor("Panel.background"));
        area.setForeground(UIManager.getColor("Panel.foreground"));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                msg.getMessage("gui.about.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    public static void launch(BookService bookService, MemberService memberService, BorrowService borrowService) {
        FlatDarkLaf.setup();
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", true);
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);

        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI(bookService, memberService, borrowService);
            gui.setVisible(true);
        });
    }
}
