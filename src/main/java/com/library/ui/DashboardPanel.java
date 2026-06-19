package com.library.ui;

import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
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

    private final JLabel bookCountLabel;
    private final JLabel memberCountLabel;
    private final JLabel activeBorrowLabel;
    private final JLabel overdueLabel;
    private final DefaultListModel<String> activityModel;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DashboardPanel(BookService bookService, MemberService memberService, BorrowService borrowService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.borrowService = borrowService;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel title = new JLabel("\uD83D\uDCCA \u062f\u0627\u0634\u0628\u0648\u0631\u062f \u0645\u062f\u06cc\u0631\u06cc\u062a \u06a9\u062a\u0627\u0628\u062e\u0627\u0646\u0647");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        bookCountLabel = new JLabel("0", SwingConstants.CENTER);
        memberCountLabel = new JLabel("0", SwingConstants.CENTER);
        activeBorrowLabel = new JLabel("0", SwingConstants.CENTER);
        overdueLabel = new JLabel("0", SwingConstants.CENTER);

        statsPanel.add(createStatCard("\uD83D\uDCDA \u062a\u0639\u062f\u0627\u062f \u06a9\u0644 \u06a9\u062a\u0627\u0628\u200c\u0647\u0627", bookCountLabel, new Color(0x3498DB)));
        statsPanel.add(createStatCard("\uD83D\uDC65 \u062a\u0639\u062f\u0627\u062f \u06a9\u0644 \u0627\u0639\u0636\u0627", memberCountLabel, new Color(0x2ECC71)));
        statsPanel.add(createStatCard("\uD83D\uDCD6 \u0627\u0645\u0627\u0646\u062a\u200c\u0647\u0627\u06cc \u0641\u0639\u0627\u0644", activeBorrowLabel, new Color(0xF39C12)));
        statsPanel.add(createStatCard("\u26a0\ufe0f \u06a9\u062a\u0627\u0628\u200c\u0647\u0627\u06cc \u0645\u0639\u0648\u0642", overdueLabel, new Color(0xE74C3C)));

        add(statsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel activityTitle = new JLabel("\uD83D\uDCCB \u0641\u0639\u0627\u0644\u06cc\u062a\u200c\u0647\u0627\u06cc \u0627\u062e\u06cc\u0631");
        activityTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottomPanel.add(activityTitle, BorderLayout.NORTH);

        activityModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(activityModel);
        activityList.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        activityList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bottomPanel.add(new JScrollPane(activityList), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
        refresh();
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
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
        if (!active.isEmpty()) {
            int count = Math.min(active.size(), 10);
            for (int i = 0; i < count; i++) {
                BorrowRecord r = active.get(i);
                String dueDate = r.getBorrowDate().plusDays(14).format(FMT);
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), r.getBorrowDate().plusDays(14));
                String status = daysLeft < 0 ? "\u26a0\ufe0f \u0645\u0639\u0648\u0642 (" + Math.abs(daysLeft) + " \u0631\u0648\u0632)" : "\u23f3 \u0628\u0627\u0642\u06cc \u0645\u0646\u062f (" + daysLeft + " \u0631\u0648\u0632)";
                activityModel.addElement("\u0627\u0645\u0627\u0646\u062a #" + r.getId() + " | \u06a9\u062a\u0627\u0628 #" + r.getBookId() + " | \u0639\u0636\u0648 #" + r.getMemberId() + " | \u0633\u0631\u0633\u0631\u06cc\u062f: " + dueDate + " | " + status);
            }
        } else {
            activityModel.addElement("\u0641\u0639\u0627\u0644\u06cc\u062a\u06cc \u0628\u0631\u0627\u06cc \u0646\u0645\u0627\u06cc\u0634 \u0648\u062c\u0648\u062f \u0646\u062f\u0627\u0631\u062f");
        }
    }
}
