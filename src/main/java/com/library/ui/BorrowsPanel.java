package com.library.ui;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Member;
import com.library.i18n.MessageManager;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.MemberService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BorrowsPanel extends JPanel implements Refreshable {
    private final BorrowService borrowService;
    private final BookService bookService;
    private final MemberService memberService;
    private final MessageManager msg;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> filterCombo;
    private final JTextField memberHistoryField;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BorrowsPanel(BorrowService borrowService, BookService bookService, MemberService memberService) {
        this.borrowService = borrowService;
        this.bookService = bookService;
        this.memberService = memberService;
        this.msg = MessageManager.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        String[] columns = {
                msg.getMessage("gui.borrows.col.id"),
                msg.getMessage("gui.borrows.col.book"),
                msg.getMessage("gui.borrows.col.member"),
                msg.getMessage("gui.borrows.col.borrowDate"),
                msg.getMessage("gui.borrows.col.returnDate"),
                msg.getMessage("gui.borrows.col.status"),
                msg.getMessage("gui.borrows.col.dueDate")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel filterLabel = new JLabel(msg.getMessage("gui.borrows.filter"));
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topPanel.add(filterLabel);

        filterCombo = new JComboBox<>(new String[]{
                msg.getMessage("gui.borrows.filter.active"),
                msg.getMessage("gui.borrows.filter.overdue"),
                msg.getMessage("gui.borrows.filter.all"),
                msg.getMessage("gui.borrows.filter.history")
        });
        filterCombo.addActionListener(e -> onFilterChange());
        topPanel.add(filterCombo);

        memberHistoryField = new JTextField(8);
        memberHistoryField.setToolTipText(msg.getMessage("gui.borrows.memberId"));
        topPanel.add(new JLabel(msg.getMessage("gui.borrows.memberId")));
        topPanel.add(memberHistoryField);

        JButton historyBtn = new JButton(msg.getMessage("gui.borrows.historyBtn"));
        historyBtn.addActionListener(e -> loadHistory());
        topPanel.add(historyBtn);

        JButton refreshBtn = new JButton(msg.getMessage("gui.members.refresh"));
        refreshBtn.addActionListener(e -> refresh());
        topPanel.add(refreshBtn);

        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JButton borrowBtn = new JButton(msg.getMessage("gui.borrows.borrow"));
        JButton returnBtn = new JButton(msg.getMessage("gui.borrows.return"));

        borrowBtn.addActionListener(e -> borrowBook());
        returnBtn.addActionListener(e -> returnBook());

        buttonPanel.add(returnBtn);
        buttonPanel.add(borrowBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        loadActiveBorrows();
    }

    private void onFilterChange() {
        String selected = (String) filterCombo.getSelectedItem();
        if (selected == null) return;
        if (selected.equals(msg.getMessage("gui.borrows.filter.history"))) {
            return;
        }
        tableModel.setRowCount(0);
        if (selected.equals(msg.getMessage("gui.borrows.filter.active"))) {
            loadRecords(borrowService.getActiveBorrows());
        } else if (selected.equals(msg.getMessage("gui.borrows.filter.overdue"))) {
            loadRecords(borrowService.getOverdueBooks());
        } else {
            List<BorrowRecord> all = new ArrayList<>(borrowService.getActiveBorrows());
            all.addAll(borrowService.getOverdueBooks());
            loadRecords(all);
        }
    }

    private void loadActiveBorrows() {
        tableModel.setRowCount(0);
        loadRecords(borrowService.getActiveBorrows());
    }

    private void loadRecords(List<BorrowRecord> records) {
        tableModel.setRowCount(0);
        for (BorrowRecord r : records) {
            String bookTitle = "کتاب #" + r.getBookId();
            String memberName = "عضو #" + r.getMemberId();
            try {
                bookTitle = bookService.getBookById(r.getBookId()).getTitle();
            } catch (Exception ignored) {}
            try {
                memberName = memberService.getMemberById(r.getMemberId()).getName();
            } catch (Exception ignored) {}

            String dueDate = r.getBorrowDate().plusDays(14).format(DATE_FMT);
            String statusStr = statusToString(r.getStatus());

            tableModel.addRow(new Object[]{
                    r.getId(), bookTitle, memberName,
                    r.getBorrowDate() != null ? r.getBorrowDate().format(DATE_FMT) : "",
                    r.getReturnDate() != null ? r.getReturnDate().format(DATE_FMT) : "",
                    statusStr, dueDate
            });
        }
    }

    private void loadHistory() {
        String text = memberHistoryField.getText().trim();
        if (text.isEmpty()) return;
        try {
            Long memberId = Long.parseLong(text);
            List<BorrowRecord> records = borrowService.getBorrowHistoryByMemberId(memberId);
            tableModel.setRowCount(0);
            loadRecords(records);
            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this, "سابقه‌ای یافت نشد", msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "شناسه عضو نامعتبر است", msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        onFilterChange();
    }

    private String statusToString(BorrowStatus status) {
        return switch (status) {
            case BORROWED -> msg.getMessage("gui.borrows.dialog.status.borrowed");
            case RETURNED -> msg.getMessage("gui.borrows.dialog.status.returned");
            case OVERDUE -> msg.getMessage("gui.borrows.dialog.status.overdue");
        };
    }

    private void borrowBook() {
        List<Book> books = bookService.getAllBooks();
        List<Member> members = memberService.getAllMembers();

        if (books.isEmpty() || members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "حداقل یک کتاب و یک عضو لازم است", msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] bookNames = books.stream()
                .map(b -> b.getId() + " - " + b.getTitle() + " (" + b.getAvailableQuantity() + " موجود)")
                .toArray(String[]::new);
        String[] memberNames = members.stream()
                .map(m -> m.getId() + " - " + m.getName())
                .toArray(String[]::new);

        JComboBox<String> bookCombo = new JComboBox<>(bookNames);
        bookCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JComboBox<String> memberCombo = new JComboBox<>(memberNames);
        memberCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(msg.getMessage("gui.borrows.dialog.selectBook") + ":"));
        panel.add(bookCombo);
        panel.add(new JLabel(msg.getMessage("gui.borrows.dialog.selectMember") + ":"));
        panel.add(memberCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.borrows.dialog.borrow"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int bookIdx = bookCombo.getSelectedIndex();
                int memberIdx = memberCombo.getSelectedIndex();
                borrowService.borrowBook(books.get(bookIdx).getId(), members.get(memberIdx).getId());
                refresh();
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.success"), msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.error") + ": " + ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void returnBook() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.selectRow"), msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
        int confirm = JOptionPane.showConfirmDialog(this, msg.getMessage("gui.msg.confirmReturn"), msg.getMessage("gui.msg.warning"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                BorrowRecord record = borrowService.returnBook(id);
                refresh();
                String message = msg.getMessage("gui.msg.success");
                if (record.getStatus() == BorrowStatus.OVERDUE) {
                    message += "\n" + msg.getMessage("gui.msg.overdueWarning");
                }
                JOptionPane.showMessageDialog(this, message, msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.error") + ": " + ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
