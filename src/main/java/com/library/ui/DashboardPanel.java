package com.library.ui;

import com.library.entity.BorrowRecord;
import com.library.i18n.MessageManager;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.MemberService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel implements Refreshable {
    private final BookService bookService;
    private final MemberService memberService;
    private final BorrowService borrowService;
    private final MessageManager msg;

    private final JLabel bookCountLabel;
    private final JLabel memberCountLabel;
    private final JLabel activeBorrowLabel;
    private final JLabel overdueLabel;
    private final DefaultListModel<String> activityModel;

    public DashboardPanel(BookService bookService, MemberService memberService, BorrowService borrowService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.borrowService = borrowService;
        this.msg = MessageManager.getInstance();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        JLabel title = new JLabel(msg.getMessage("gui.dashboard.title"));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        statsPanel.setOpaque(false);

        bookCountLabel = new JLabel("0", SwingConstants.CENTER);
        memberCountLabel = new JLabel("0", SwingConstants.CENTER);
        activeBorrowLabel = new JLabel("0", SwingConstants.CENTER);
        overdueLabel = new JLabel("0", SwingConstants.CENTER);

        statsPanel.add(createStatCard(msg.getMessage("gui.dashboard.totalBooks"), bookCountLabel, new Color(0x3498DB)));
        statsPanel.add(createStatCard(msg.getMessage("gui.dashboard.totalMembers"), memberCountLabel, new Color(0x2ECC71)));
        statsPanel.add(createStatCard(msg.getMessage("gui.dashboard.activeBorrows"), activeBorrowLabel, new Color(0xF39C12)));
        statsPanel.add(createStatCard(msg.getMessage("gui.dashboard.overdueBooks"), overdueLabel, new Color(0xE74C3C)));

        add(statsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        bottomPanel.setOpaque(false);

        JLabel activityTitle = new JLabel(msg.getMessage("gui.dashboard.recentActivity"));
        activityTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottomPanel.add(activityTitle, BorderLayout.NORTH);

        activityModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(activityModel);
        activityList.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        activityList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bottomPanel.add(new JScrollPane(activityList), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
        refresh();
    }

    private boolean isRTL() {
        return msg.getCurrentLanguage() == com.library.i18n.Language.FA;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setBackground(color.darker().darker().darker());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refresh() {
        bookCountLabel.setText(String.valueOf(bookService.getAllBooks().size()));
        memberCountLabel.setText(String.valueOf(memberService.getAllMembers().size()));

        List<BorrowRecord> active = borrowService.getActiveBorrows();
        activeBorrowLabel.setText(String.valueOf(active.size()));

        List<BorrowRecord> overdue = borrowService.getOverdueBooks();
        overdueLabel.setText(String.valueOf(overdue.size()));

        activityModel.clear();
        DateTimeFormatter fmt = UIConstants.DATE_FMT;
        if (!active.isEmpty()) {
            int count = Math.min(active.size(), 10);
            for (int i = 0; i < count; i++) {
                BorrowRecord r = active.get(i);
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), r.getBorrowDate().plusDays(UIConstants.MAX_BORROW_DAYS));
                String status = daysLeft < 0
                        ? "\u26a0\ufe0f " + msg.getMessage("gui.borrows.dialog.status.overdue") + " (" + Math.abs(daysLeft) + ")"
                        : "\u23f3 " + daysLeft;
                activityModel.addElement("#" + r.getId() + " | " + msg.getMessage("gui.borrows.col.borrowDate") + ": " + r.getBorrowDate().format(fmt) + " | " + status);
            }
        } else {
            activityModel.addElement(msg.getMessage("gui.dashboard.noActivity"));
        }
    }
}
