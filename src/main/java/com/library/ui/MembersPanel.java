package com.library.ui;

import com.library.entity.Member;
import com.library.i18n.MessageManager;
import com.library.service.BorrowService;
import com.library.service.MemberService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MembersPanel extends JPanel implements Refreshable {
    private final MemberService memberService;
    private final BorrowService borrowService;
    private final MessageManager msg;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MembersPanel(MemberService memberService, BorrowService borrowService) {
        this.memberService = memberService;
        this.borrowService = borrowService;
        this.msg = MessageManager.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        String[] columns = {
                msg.getMessage("gui.members.col.id"),
                msg.getMessage("gui.members.col.name"),
                msg.getMessage("gui.members.col.natCode"),
                msg.getMessage("gui.members.col.phone"),
                msg.getMessage("gui.members.col.joinDate"),
                msg.getMessage("gui.members.col.activeBorrows")
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
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        searchField = new JTextField();
        searchField.setToolTipText(msg.getMessage("gui.members.search"));
        searchField.addActionListener(e -> searchMembers());

        JButton searchBtn = new JButton(msg.getMessage("gui.members.searchBtn"));
        searchBtn.addActionListener(e -> searchMembers());
        JButton refreshBtn = new JButton(msg.getMessage("gui.members.refresh"));
        refreshBtn.addActionListener(e -> refresh());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchPanel.add(refreshBtn);
        searchPanel.add(searchBtn);
        searchPanel.add(searchField);

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel(msg.getMessage("gui.members.title"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topRightPanel.add(titleLabel);

        topPanel.add(topRightPanel, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JButton addBtn = new JButton(msg.getMessage("gui.members.add"));
        JButton editBtn = new JButton(msg.getMessage("gui.members.edit"));
        JButton deleteBtn = new JButton(msg.getMessage("gui.members.delete"));
        JButton detailBtn = new JButton(msg.getMessage("gui.members.viewDetails"));

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

    private void loadMembers() {
        tableModel.setRowCount(0);
        List<Member> members = memberService.getAllMembers();
        for (Member m : members) {
            long activeCount = 0;
            try {
                activeCount = borrowService.getActiveBorrowCountByMemberId(m.getId());
            } catch (Exception ignored) {}
            tableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getNationalCode(),
                    m.getPhoneNumber(),
                    m.getJoinDate() != null ? m.getJoinDate().format(DATE_FMT) : "",
                    activeCount
            });
        }
    }

    @Override
    public void refresh() {
        loadMembers();
    }

    private void searchMembers() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Member> members = keyword.isEmpty() ? memberService.getAllMembers() : memberService.searchMembers(keyword);
        for (Member m : members) {
            long activeCount = 0;
            try {
                activeCount = borrowService.getActiveBorrowCountByMemberId(m.getId());
            } catch (Exception ignored) {}
            tableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getNationalCode(),
                    m.getPhoneNumber(),
                    m.getJoinDate() != null ? m.getJoinDate().format(DATE_FMT) : "",
                    activeCount
            });
        }
    }

    private void addMember() {
        JTextField nameField = new JTextField();
        JTextField natCodeField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField joinDateField = new JTextField(LocalDate.now().format(DATE_FMT));

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(msg.getMessage("gui.members.dialog.name") + ":"));
        panel.add(nameField);
        panel.add(new JLabel(msg.getMessage("gui.members.dialog.natCode") + ":"));
        panel.add(natCodeField);
        panel.add(new JLabel(msg.getMessage("gui.members.dialog.phone") + ":"));
        panel.add(phoneField);
        panel.add(new JLabel(msg.getMessage("gui.members.dialog.joinDate") + ":"));
        panel.add(joinDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.members.dialog.add"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Member member = new Member(
                        nameField.getText().trim(),
                        natCodeField.getText().trim(),
                        phoneField.getText().trim(),
                        LocalDate.parse(joinDateField.getText().trim(), DATE_FMT)
                );
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
        if (row == -1) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.selectRow"), msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
        Member member = memberService.getMemberById(id);

        JTextField nameField = new JTextField(member.getName());
        JTextField natCodeField = new JTextField(member.getNationalCode());
        JTextField phoneField = new JTextField(member.getPhoneNumber());
        JTextField joinDateField = new JTextField(member.getJoinDate() != null ? member.getJoinDate().format(DATE_FMT) : "");

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(msg.getMessage("gui.members.dialog.name") + ":"));
        panel.add(nameField);
        panel.add(new JLabel(msg.getMessage("gui.members.dialog.natCode") + ":"));
        panel.add(natCodeField);
        panel.add(new JLabel(msg.getMessage("gui.members.dialog.phone") + ":"));
        panel.add(phoneField);
        panel.add(new JLabel(msg.getMessage("gui.members.dialog.joinDate") + ":"));
        panel.add(joinDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.members.dialog.edit"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                member.setName(nameField.getText().trim());
                member.setNationalCode(natCodeField.getText().trim());
                member.setPhoneNumber(phoneField.getText().trim());
                member.setJoinDate(joinDateField.getText().trim().isEmpty() ? null : LocalDate.parse(joinDateField.getText().trim(), DATE_FMT));
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
        if (row == -1) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.selectRow"), msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
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
        if (row == -1) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.selectRow"), msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
        Member member = memberService.getMemberById(id);
        long activeCount = 0;
        try {
            activeCount = borrowService.getActiveBorrowCountByMemberId(id);
        } catch (Exception ignored) {}

        String details = msg.getMessage("gui.members.dialog.detailTitle") + ":\n" +
                "─────────────────────\n" +
                msg.getMessage("gui.members.col.id") + ": " + member.getId() + "\n" +
                msg.getMessage("gui.members.dialog.name") + ": " + member.getName() + "\n" +
                msg.getMessage("gui.members.dialog.natCode") + ": " + member.getNationalCode() + "\n" +
                msg.getMessage("gui.members.dialog.phone") + ": " + member.getPhoneNumber() + "\n" +
                msg.getMessage("gui.members.dialog.joinDate") + ": " + (member.getJoinDate() != null ? member.getJoinDate().format(DATE_FMT) : "-") + "\n" +
                msg.getMessage("gui.members.col.activeBorrows") + ": " + activeCount + " / 3";

        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setForeground(UIManager.getColor("Panel.foreground"));

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), msg.getMessage("gui.members.dialog.detailTitle"), JOptionPane.INFORMATION_MESSAGE);
    }
}
