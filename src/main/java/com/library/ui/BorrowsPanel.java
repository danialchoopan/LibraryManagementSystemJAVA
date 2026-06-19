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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BorrowsPanel extends JPanel implements Refreshable, Exportable {
    private final BorrowService borrowService;
    private final BookService bookService;
    private final MemberService memberService;
    private final MessageManager msg;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> filterCombo;
    private final JTextField memberHistoryField;
    private final TableRowSorter<DefaultTableModel> sorter;

    private enum FilterType { ACTIVE, OVERDUE, ALL, HISTORY }

    public BorrowsPanel(BorrowService borrowService, BookService bookService, MemberService memberService) {
        this.borrowService = borrowService;
        this.bookService = bookService;
        this.memberService = memberService;
        this.msg = MessageManager.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        String[] columns = {
                msg.getMessage("gui.borrows.col.id"), msg.getMessage("gui.borrows.col.book"),
                msg.getMessage("gui.borrows.col.member"), msg.getMessage("gui.borrows.col.borrowDate"),
                msg.getMessage("gui.borrows.col.returnDate"), msg.getMessage("gui.borrows.col.status"),
                msg.getMessage("gui.borrows.col.dueDate")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(isRTL() ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        topPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        JLabel filterLabel = new JLabel(msg.getMessage("gui.borrows.filter"));
        filterLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        topPanel.add(filterLabel);

        filterCombo = new JComboBox<>(new String[]{
                msg.getMessage("gui.borrows.filter.active"),
                msg.getMessage("gui.borrows.filter.overdue"),
                msg.getMessage("gui.borrows.filter.all"),
                msg.getMessage("gui.borrows.filter.history")
        });
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

        JButton exportBtn = new JButton(msg.getMessage("gui.borrows.export"));
        exportBtn.addActionListener(e -> exportCSV());
        topPanel.add(exportBtn);

        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(isRTL() ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        buttonPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        JButton returnBtn = new JButton(msg.getMessage("gui.borrows.return"));
        JButton borrowBtn = new JButton(msg.getMessage("gui.borrows.borrow"));
        returnBtn.addActionListener(e -> returnBook());
        borrowBtn.addActionListener(e -> borrowBook());

        buttonPanel.add(returnBtn);
        buttonPanel.add(borrowBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        filterCombo.addActionListener(e -> onFilterChange());
        loadActiveBorrows();
    }

    private boolean isRTL() { return msg.getCurrentLanguage() == com.library.i18n.Language.FA; }

    private FilterType getSelectedFilter() {
        int idx = filterCombo.getSelectedIndex();
        return switch (idx) {
            case 0 -> FilterType.ACTIVE;
            case 1 -> FilterType.OVERDUE;
            case 2 -> FilterType.ALL;
            case 3 -> FilterType.HISTORY;
            default -> FilterType.ACTIVE;
        };
    }

    private void onFilterChange() {
        FilterType filter = getSelectedFilter();
        if (filter == FilterType.HISTORY) return;
        tableModel.setRowCount(0);
        switch (filter) {
            case ACTIVE -> loadRecords(borrowService.getActiveBorrows());
            case OVERDUE -> loadRecords(borrowService.getOverdueBooks());
            case ALL -> {
                List<BorrowRecord> all = new ArrayList<>(borrowService.getActiveBorrows());
                all.addAll(borrowService.getOverdueBooks());
                loadRecords(all);
            }
        }
    }

    private void loadActiveBorrows() {
        tableModel.setRowCount(0);
        loadRecords(borrowService.getActiveBorrows());
    }

    private void loadRecords(List<BorrowRecord> records) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = UIConstants.DATE_FMT;
        for (BorrowRecord r : records) {
            String bookTitle = "#" + r.getBookId();
            String memberName = "#" + r.getMemberId();
            try { bookTitle = bookService.getBookById(r.getBookId()).getTitle(); } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("Book {} not found", r.getBookId());
            }
            try { memberName = memberService.getMemberById(r.getMemberId()).getName(); } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("Member {} not found", r.getMemberId());
            }
            String dueDate = r.getBorrowDate().plusDays(UIConstants.MAX_BORROW_DAYS).format(fmt);
            tableModel.addRow(new Object[]{
                    r.getId(), bookTitle, memberName,
                    r.getBorrowDate() != null ? r.getBorrowDate().format(fmt) : "",
                    r.getReturnDate() != null ? r.getReturnDate().format(fmt) : "",
                    r.getStatus().name(), dueDate
            });
        }
    }

    private void loadHistory() {
        String text = memberHistoryField.getText().trim();
        if (text.isEmpty()) return;
        try {
            Long memberId = Long.parseLong(text);
            List<BorrowRecord> records = borrowService.getBorrowHistoryByMemberId(memberId);
            loadRecords(records);
            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.noHistory"), msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.borrows.error.invalidMemberId"), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.error") + ": " + ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() { onFilterChange(); }

    @Override
    public void exportCSV() { CSVExporter.exportTableToCSV(table, this); }

    private void borrowBook() {
        List<Book> books = bookService.getAllBooks();
        List<Member> members = memberService.getAllMembers();

        if (books.isEmpty() || members.isEmpty()) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.borrows.error.noBooksMembers"), msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] bookNames = books.stream()
                .map(b -> b.getId() + " - " + b.getTitle() + " (" + b.getAvailableQuantity() + ")")
                .toArray(String[]::new);
        String[] memberNames = members.stream()
                .map(m -> m.getId() + " - " + m.getName())
                .toArray(String[]::new);

        JComboBox<String> bookCombo = new JComboBox<>(bookNames);
        bookCombo.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        JComboBox<String> memberCombo = new JComboBox<>(memberNames);
        memberCombo.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
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
        Long id = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(row), 0)).longValue();
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
