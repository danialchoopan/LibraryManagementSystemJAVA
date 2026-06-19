package com.library.ui;

import com.library.entity.Member;
import com.library.i18n.MessageManager;
import com.library.service.BorrowService;
import com.library.service.MemberService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MembersPanel extends JPanel implements Refreshable, Exportable {
    private final MemberService memberService;
    private final BorrowService borrowService;
    private final MessageManager msg;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final TableRowSorter<DefaultTableModel> sorter;

    public MembersPanel(MemberService memberService, BorrowService borrowService) {
        this.memberService = memberService;
        this.borrowService = borrowService;
        this.msg = MessageManager.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        String[] columns = {
                msg.getMessage("gui.members.col.id"), msg.getMessage("gui.members.col.name"),
                msg.getMessage("gui.members.col.natCode"), msg.getMessage("gui.members.col.phone"),
                msg.getMessage("gui.members.col.joinDate"), msg.getMessage("gui.members.col.activeBorrows")
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
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        searchField = new JTextField();
        searchField.setToolTipText(msg.getMessage("gui.members.search"));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JButton searchBtn = new JButton(msg.getMessage("gui.members.searchBtn"));
        searchBtn.addActionListener(e -> filterTable());
        JButton refreshBtn = new JButton(msg.getMessage("gui.members.refresh"));
        refreshBtn.addActionListener(e -> refresh());
        JButton exportBtn = new JButton(msg.getMessage("gui.members.export"));
        exportBtn.addActionListener(e -> exportCSV());

        JPanel searchPanel = new JPanel(new FlowLayout(isRTL() ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        searchPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        searchPanel.add(exportBtn);
        searchPanel.add(refreshBtn);
        searchPanel.add(searchBtn);
        searchPanel.add(searchField);

        JLabel titleLabel = new JLabel(msg.getMessage("gui.members.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        topPanel.add(titleLabel, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(isRTL() ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        buttonPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        JButton detailBtn = new JButton(msg.getMessage("gui.members.viewDetails"));
        JButton deleteBtn = new JButton(msg.getMessage("gui.members.delete"));
        JButton editBtn = new JButton(msg.getMessage("gui.members.edit"));
        JButton addBtn = new JButton(msg.getMessage("gui.members.add"));

        addBtn.addActionListener(e -> addMember());
        editBtn.addActionListener(e -> editMember());
        deleteBtn.addActionListener(e -> deleteMember());
        detailBtn.addActionListener(e -> viewDetails());

        buttonPanel.add(detailBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(addBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        loadMembers();
    }

    private boolean isRTL() { return msg.getCurrentLanguage() == com.library.i18n.Language.FA; }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
    }

    private void loadMembers() {
        tableModel.setRowCount(0);
        for (Member m : memberService.getAllMembers()) {
            long activeCount = 0;
            try {
                activeCount = borrowService.getActiveBorrowCountByMemberId(m.getId());
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("Failed to get active borrow count for member {}", m.getId(), e);
            }
            tableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getNationalCode(), m.getPhoneNumber(),
                    m.getJoinDate() != null ? m.getJoinDate().format(UIConstants.DATE_FMT) : "",
                    activeCount + " / " + UIConstants.MAX_BORROW_LIMIT
            });
        }
    }

    @Override
    public void refresh() { loadMembers(); }

    @Override
    public void exportCSV() { CSVExporter.exportTableToCSV(table, this); }

    private void addMember() {
        JTextField nameField = new JTextField();
        JTextField natCodeField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField joinDateField = new JTextField(LocalDate.now().format(UIConstants.DATE_FMT));

        JPanel panel = createForm(new String[]{
                msg.getMessage("gui.members.dialog.name"),
                msg.getMessage("gui.members.dialog.natCode"),
                msg.getMessage("gui.members.dialog.phone"),
                msg.getMessage("gui.members.dialog.joinDate")
        }, new JComponent[]{nameField, natCodeField, phoneField, joinDateField});

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.members.dialog.add"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String natCode = natCodeField.getText().trim();
                String phone = phoneField.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.members.error.nameRequired"));
                if (natCode.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.members.error.natCodeRequired"));
                if (phone.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.members.error.phoneRequired"));

                Member member = new Member(name, natCode, phone, LocalDate.parse(joinDateField.getText().trim(), UIConstants.DATE_FMT));
                memberService.addMember(member);
                loadMembers();
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.success"), msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.error") + ": " + ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editMember() {
        int row = table.getSelectedRow();
        if (row == -1) { showWarning(); return; }
        Long id = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(row), 0)).longValue();
        Member member = memberService.getMemberById(id);

        JTextField nameField = new JTextField(member.getName());
        JTextField natCodeField = new JTextField(member.getNationalCode());
        JTextField phoneField = new JTextField(member.getPhoneNumber());
        JTextField joinDateField = new JTextField(member.getJoinDate() != null ? member.getJoinDate().format(UIConstants.DATE_FMT) : "");

        JPanel panel = createForm(new String[]{
                msg.getMessage("gui.members.dialog.name"),
                msg.getMessage("gui.members.dialog.natCode"),
                msg.getMessage("gui.members.dialog.phone"),
                msg.getMessage("gui.members.dialog.joinDate")
        }, new JComponent[]{nameField, natCodeField, phoneField, joinDateField});

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.members.dialog.edit"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String natCode = natCodeField.getText().trim();
                String phone = phoneField.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.members.error.nameRequired"));
                if (natCode.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.members.error.natCodeRequired"));
                if (phone.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.members.error.phoneRequired"));

                member.setName(name);
                member.setNationalCode(natCode);
                member.setPhoneNumber(phone);
                member.setJoinDate(joinDateField.getText().trim().isEmpty() ? null : LocalDate.parse(joinDateField.getText().trim(), UIConstants.DATE_FMT));
                memberService.updateMember(member);
                loadMembers();
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.success"), msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.error") + ": " + ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteMember() {
        int row = table.getSelectedRow();
        if (row == -1) { showWarning(); return; }
        Long id = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(row), 0)).longValue();
        int confirm = JOptionPane.showConfirmDialog(this, msg.getMessage("gui.msg.confirmDeleteMember"), msg.getMessage("gui.msg.warning"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                memberService.deleteMember(id);
                loadMembers();
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.success"), msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.error") + ": " + ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row == -1) { showWarning(); return; }
        Long id = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(row), 0)).longValue();
        Member member = memberService.getMemberById(id);
        long activeCount = 0;
        try { activeCount = borrowService.getActiveBorrowCountByMemberId(id); } catch (Exception ignored) {}

        String details = msg.getMessage("gui.members.dialog.detailTitle") + ":\n"
                + msg.getMessage("gui.members.col.id") + ": " + member.getId() + "\n"
                + msg.getMessage("gui.members.dialog.name") + ": " + member.getName() + "\n"
                + msg.getMessage("gui.members.dialog.natCode") + ": " + member.getNationalCode() + "\n"
                + msg.getMessage("gui.members.dialog.phone") + ": " + member.getPhoneNumber() + "\n"
                + msg.getMessage("gui.members.dialog.joinDate") + ": " + (member.getJoinDate() != null ? member.getJoinDate().format(UIConstants.DATE_FMT) : "-") + "\n"
                + msg.getMessage("gui.members.col.activeBorrows") + ": " + activeCount + " / " + UIConstants.MAX_BORROW_LIMIT;

        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setForeground(UIManager.getColor("Panel.foreground"));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), msg.getMessage("gui.members.dialog.detailTitle"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning() {
        JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.selectRow"), msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
    }

    private JPanel createForm(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        panel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i] + ":"));
            panel.add(fields[i]);
        }
        return panel;
    }
}
