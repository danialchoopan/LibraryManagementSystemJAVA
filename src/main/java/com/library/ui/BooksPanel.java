package com.library.ui;

import com.library.entity.Book;
import com.library.i18n.MessageManager;
import com.library.service.BookService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class BooksPanel extends JPanel implements Refreshable, Exportable {
    private final BookService bookService;
    private final MessageManager msg;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public BooksPanel(BookService bookService) {
        this.bookService = bookService;
        this.msg = MessageManager.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        String[] columns = {
                msg.getMessage("gui.books.col.id"), msg.getMessage("gui.books.col.title"),
                msg.getMessage("gui.books.col.author"), msg.getMessage("gui.books.col.isbn"),
                msg.getMessage("gui.books.col.year"), msg.getMessage("gui.books.col.qty"),
                msg.getMessage("gui.books.col.available")
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
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);

        StatusCellRenderer.applyAvailabilityColor(table, 6);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        searchField = new JTextField();
        searchField.setToolTipText(msg.getMessage("gui.books.search"));
        searchField.setColumns(20);
        javax.swing.event.DocumentListener searchListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        };
        searchField.getDocument().addDocumentListener(searchListener);

        JButton searchBtn = new JButton(msg.getMessage("gui.books.searchBtn"));
        searchBtn.addActionListener(e -> filterTable());
        JButton refreshBtn = new JButton(msg.getMessage("gui.books.refresh"));
        refreshBtn.addActionListener(e -> refresh());
        JButton exportBtn = new JButton(msg.getMessage("gui.books.export"));
        exportBtn.addActionListener(e -> exportCSV());

        JPanel searchPanel = new JPanel(new FlowLayout(isRTL() ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        searchPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        searchPanel.add(exportBtn);
        searchPanel.add(refreshBtn);
        searchPanel.add(searchBtn);
        searchPanel.add(searchField);

        JLabel titleLabel = new JLabel(msg.getMessage("gui.books.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        topPanel.add(titleLabel, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(isRTL() ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        buttonPanel.setComponentOrientation(isRTL() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);

        JButton detailBtn = new JButton(msg.getMessage("gui.books.viewDetails"));
        JButton deleteBtn = new JButton(msg.getMessage("gui.books.delete"));
        JButton editBtn = new JButton(msg.getMessage("gui.books.edit"));
        JButton addBtn = new JButton(msg.getMessage("gui.books.add"));

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

    private boolean isRTL() { return msg.getCurrentLanguage() == com.library.i18n.Language.FA; }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        for (Book b : bookService.getAllBooks()) {
            tableModel.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                    b.getPublishedYear(), b.getQuantity(), b.getAvailableQuantity()
            });
        }
    }

    @Override
    public void refresh() { loadBooks(); }

    @Override
    public void exportCSV() { CSVExporter.exportTableToCSV(table, this); }

    private void addBook() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField qtyField = new JTextField();

        JPanel panel = createForm(new String[]{
                msg.getMessage("gui.books.dialog.title"),
                msg.getMessage("gui.books.dialog.author"),
                msg.getMessage("gui.books.dialog.isbn"),
                msg.getMessage("gui.books.dialog.year"),
                msg.getMessage("gui.books.dialog.qty")
        }, new JComponent[]{titleField, authorField, isbnField, yearField, qtyField});

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.books.dialog.add"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String isbn = isbnField.getText().trim();
                if (title.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.books.error.titleRequired"));
                if (author.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.books.error.authorRequired"));
                if (isbn.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.books.error.isbnRequired"));

                Integer year = null;
                if (!yearField.getText().trim().isEmpty()) {
                    year = Integer.parseInt(yearField.getText().trim());
                    if (year < 1000 || year > 2099) throw new IllegalArgumentException(msg.getMessage("gui.books.error.yearInvalid"));
                }
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) throw new IllegalArgumentException(msg.getMessage("gui.books.error.qtyInvalid"));

                Book book = new Book(title, author, isbn, year, qty, qty);
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
        if (row == -1) { showWarning(); return; }
        Long id = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(row), 0)).longValue();
        Book book = bookService.getBookById(id);

        JTextField titleField = new JTextField(book.getTitle());
        JTextField authorField = new JTextField(book.getAuthor());
        JTextField isbnField = new JTextField(book.getIsbn());
        JTextField yearField = new JTextField(book.getPublishedYear() != null ? String.valueOf(book.getPublishedYear()) : "");
        JTextField qtyField = new JTextField(String.valueOf(book.getQuantity()));

        JPanel panel = createForm(new String[]{
                msg.getMessage("gui.books.dialog.title"),
                msg.getMessage("gui.books.dialog.author"),
                msg.getMessage("gui.books.dialog.isbn"),
                msg.getMessage("gui.books.dialog.year"),
                msg.getMessage("gui.books.dialog.qty")
        }, new JComponent[]{titleField, authorField, isbnField, yearField, qtyField});

        int result = JOptionPane.showConfirmDialog(this, panel, msg.getMessage("gui.books.dialog.edit"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String isbn = isbnField.getText().trim();
                if (title.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.books.error.titleRequired"));
                if (author.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.books.error.authorRequired"));
                if (isbn.isEmpty()) throw new IllegalArgumentException(msg.getMessage("gui.books.error.isbnRequired"));

                int oldQty = book.getQuantity();
                int oldAvail = book.getAvailableQuantity();
                int newQty = Integer.parseInt(qtyField.getText().trim());
                if (newQty <= 0) throw new IllegalArgumentException(msg.getMessage("gui.books.error.qtyInvalid"));
                int newAvail = oldAvail + (newQty - oldQty);
                if (newAvail < 0) newAvail = 0;

                book.setTitle(title);
                book.setAuthor(author);
                book.setIsbn(isbn);
                book.setPublishedYear(yearField.getText().trim().isEmpty() ? null : Integer.parseInt(yearField.getText().trim()));
                book.setQuantity(newQty);
                book.setAvailableQuantity(newAvail);
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
        if (row == -1) { showWarning(); return; }
        Long id = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(row), 0)).longValue();
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
        if (row == -1) { showWarning(); return; }
        Long id = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(row), 0)).longValue();
        Book book = bookService.getBookById(id);

        String details = msg.getMessage("gui.books.dialog.detailTitle") + ":\n"
                + msg.getMessage("gui.books.col.id") + ": " + book.getId() + "\n"
                + msg.getMessage("gui.books.dialog.title") + ": " + book.getTitle() + "\n"
                + msg.getMessage("gui.books.dialog.author") + ": " + book.getAuthor() + "\n"
                + msg.getMessage("gui.books.dialog.isbn") + ": " + book.getIsbn() + "\n"
                + msg.getMessage("gui.books.dialog.year") + ": " + (book.getPublishedYear() != null ? book.getPublishedYear() : "-") + "\n"
                + msg.getMessage("gui.books.dialog.qty") + ": " + book.getQuantity() + "\n"
                + msg.getMessage("gui.books.col.available") + ": " + book.getAvailableQuantity();

        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setForeground(UIManager.getColor("Panel.foreground"));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), msg.getMessage("gui.books.dialog.detailTitle"), JOptionPane.INFORMATION_MESSAGE);
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
