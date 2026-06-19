package com.library.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.MemberService;

import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame {
    private final BookService bookService;
    private final MemberService memberService;
    private final BorrowService borrowService;

    public MainGUI(BookService bookService, MemberService memberService, BorrowService borrowService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.borrowService = borrowService;
        initUI();
    }

    private void initUI() {
        setTitle("\u0633\u06cc\u0633\u062a\u0645 \u0645\u062f\u06cc\u0631\u06cc\u062a \u06a9\u062a\u0627\u0628\u062e\u0627\u0646\u0647");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        DashboardPanel dashboard = new DashboardPanel(bookService, memberService, borrowService);
        tabbedPane.addTab("\uD83D\uDCCA \u062f\u0627\u0634\u0628\u0648\u0631\u062f", dashboard);
        tabbedPane.addTab("\uD83D\uDCDA \u06a9\u062a\u0627\u0628\u200c\u0647\u0627", new BooksPanel(bookService));
        tabbedPane.addTab("\uD83D\uDC65 \u0627\u0639\u0636\u0627", new MembersPanel(memberService, borrowService));
        tabbedPane.addTab("\uD83D\uDCD6 \u0627\u0645\u0627\u0646\u062a\u200c\u0647\u0627", new BorrowsPanel(borrowService, bookService, memberService));

        setContentPane(tabbedPane);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JMenu viewMenu = new JMenu("\u0646\u0645\u0627\u06cc\u0634");
        viewMenu.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JMenuItem refreshItem = new JMenuItem("\u0628\u0631\u0648\u0632\u0631\u0633\u0627\u0646\u06cc \u0647\u0645\u0647");
        refreshItem.addActionListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            Component c = tabbedPane.getComponentAt(idx);
            if (c instanceof Refreshable r) {
                r.refresh();
            }
        });
        viewMenu.add(refreshItem);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);

        statusBar(dashboard);
    }

    private void statusBar(DashboardPanel dashboard) {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        statusPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JLabel statusLabel = new JLabel("\u0622\u0645\u0627\u062f\u0647 \u0628\u0647 \u0633\u06cc\u0633\u062a\u0645 \u0645\u062f\u06cc\u0631\u06cc\u062a \u06a9\u062a\u0627\u0628\u062e\u0627\u0646\u0647 \u062e\u0648\u0634 \u0622\u0645\u062f\u06cc\u062f");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    public static void launch(BookService bookService, MemberService memberService, BorrowService borrowService) {
        FlatDarkLaf.setup();
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", true);
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Table.showDeclaredHeadings", false);

        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI(bookService, memberService, borrowService);
            gui.setVisible(true);
        });
    }
}
