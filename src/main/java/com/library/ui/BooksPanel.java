package com.library.ui;

import com.library.entity.Book;
import com.library.i18n.MessageManager;
import com.library.service.BookService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BooksPanel extends JPanel implements Refreshable {
    private final BookService bookService;
    private final MessageManager msg;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    public BooksPanel(BookService bookService) {
        this.bookService = bookService;
        this.msg = MessageManager.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        String[] columns = {
                msg.getMessage("gui.books.col.id"),
                msg.getMessage("gui.books.col.title"),
                msg.getMessage("gui.books.col.author"),
                msg.getMessage("gui.books.col.isbn"),
                msg.getMessage("gui.books.col.year"),
                msg.getMessage("gui.books.col.qty"),
                msg.getMessage("gui.books.col.available")
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
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        searchField = new JTextField();
        searchField.setToolTipText(msg.getMessage("gui.books.search"));
        searchField.addActionListener(e -> searchBooks());
        searchField.setColumns(20);

        JButton searchBtn = new JButton(msg.getMessage("gui.books.searchBtn"));
        searchBtn.addActionListener(e -> searchBooks());
        JButton refreshBtn = new JButton(msg.getMessage("gui.books.refresh"));
        refreshBtn.addActionListener(e -> refresh());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchPanel.add(refreshBtn);
        searchPanel.add(searchBtn);
        searchPanel.add(searchField);

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel(msg.getMessage("gui.books.title"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topRightPanel.add(titleLabel);

        topPanel.add(topRightPanel, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JButton addBtn = new JButton(msg.getMessage("gui.books.add"));
        JButton editBtn = new JButton(msg.getMessage("gui.books.edit"));
        JButton deleteBtn = new JButton(msg.getMessage("gui.books.delete"));
        JButton detailBtn = new JButton(msg.getMessage("gui.books.viewDetails"));

        addBtn.addActionListener(e -> addBook());
        editBtn.addActionListener(e -> editBook());
        deleteBtn.addActionListener(e -> deleteBook());
        detailBtn.addActionListener(e -> viewDetails());

        buttonPanel.add(detailBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(addBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        loadBooks();
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        List<Book> books = bookService.getAllBooks();
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                    b.getPublishedYear(), b.getQuantity(), b.getAvailableQuantity()
            });
        }
    }

    @Override
    public void refresh() {
        loadBooks();
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Book> books = keyword.isEmpty() ? bookService.getAllBooks() : bookService.searchBooks(keyword);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                    b.getPublishedYear(), b.getQuantity(), b.getAvailableQuantity()
            });
        }
    }

    private void addBook() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField qtyField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.title") + ":"));
        panel.add(titleField);
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.author") + ":"));
        panel.add(authorField);
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.isbn") + ":"));
        panel.add(isbnField);
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.year") + ":"));
        panel.add(yearField);
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.qty") + ":"));
        panel.add(qtyField);

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.books.dialog.add"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Book book = new Book(
                        titleField.getText().trim(),
                        authorField.getText().trim(),
                        isbnField.getText().trim(),
                        yearField.getText().trim().isEmpty() ? null : Integer.parseInt(yearField.getText().trim()),
                        Integer.parseInt(qtyField.getText().trim()),
                        Integer.parseInt(qtyField.getText().trim())
                );
                bookService.addBook(book);
                loadBooks();
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.success"), msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.error") + ": " + ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editBook() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.selectRow"), msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
        Book book = bookService.getBookById(id);

        JTextField titleField = new JTextField(book.getTitle());
        JTextField authorField = new JTextField(book.getAuthor());
        JTextField isbnField = new JTextField(book.getIsbn());
        JTextField yearField = new JTextField(book.getPublishedYear() != null ? String.valueOf(book.getPublishedYear()) : "");
        JTextField qtyField = new JTextField(String.valueOf(book.getQuantity()));

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.title") + ":"));
        panel.add(titleField);
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.author") + ":"));
        panel.add(authorField);
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.isbn") + ":"));
        panel.add(isbnField);
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.year") + ":"));
        panel.add(yearField);
        panel.add(new JLabel(msg.getMessage("gui.books.dialog.qty") + ":"));
        panel.add(qtyField);

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.books.dialog.edit"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                book.setTitle(titleField.getText().trim());
                book.setAuthor(authorField.getText().trim());
                book.setIsbn(isbnField.getText().trim());
                book.setPublishedYear(yearField.getText().trim().isEmpty() ? null : Integer.parseInt(yearField.getText().trim()));
                book.setQuantity(Integer.parseInt(qtyField.getText().trim()));
                bookService.updateBook(book);
                loadBooks();
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.success"), msg.getMessage("gui.msg.info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.error") + ": " + ex.getMessage(), msg.getMessage("gui.msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteBook() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, msg.getMessage("gui.msg.selectRow"), msg.getMessage("gui.msg.warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
        int confirm = JOptionPane.showConfirmDialog(this, msg.getMessage("gui.msg.confirmDeleteBook"), msg.getMessage("gui.msg.warning"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookService.deleteBook(id);
                loadBooks();
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
        Book book = bookService.getBookById(id);

        String details = msg.getMessage("gui.books.dialog.detailTitle") + ":\n" +
                "─────────────────────\n" +
                msg.getMessage("gui.books.col.id") + ": " + book.getId() + "\n" +
                msg.getMessage("gui.books.dialog.title") + ": " + book.getTitle() + "\n" +
                msg.getMessage("gui.books.dialog.author") + ": " + book.getAuthor() + "\n" +
                msg.getMessage("gui.books.dialog.isbn") + ": " + book.getIsbn() + "\n" +
                msg.getMessage("gui.books.dialog.year") + ": " + (book.getPublishedYear() != null ? book.getPublishedYear() : "-") + "\n" +
                msg.getMessage("gui.books.dialog.qty") + ": " + book.getQuantity() + "\n" +
                msg.getMessage("gui.books.col.available") + ": " + book.getAvailableQuantity();

        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setForeground(UIManager.getColor("Panel.foreground"));

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), msg.getMessage("gui.books.dialog.detailTitle"), JOptionPane.INFORMATION_MESSAGE);
    }
}
