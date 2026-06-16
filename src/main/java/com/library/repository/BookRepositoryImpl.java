package com.library.repository;

import com.library.entity.Book;
import com.library.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookRepositoryImpl implements BookRepository {
    private static final Logger logger = LoggerFactory.getLogger(BookRepositoryImpl.class);

    @Override
    public Book save(Book book) {
        if (book.getId() == null) {
            return insert(book);
        } else {
            return update(book);
        }
    }

    private Book insert(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, published_year, quantity, available_quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setObject(4, book.getPublishedYear());
            stmt.setInt(5, book.getQuantity());
            stmt.setInt(6, book.getAvailableQuantity());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating book failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating book failed, no ID obtained.");
                }
            }
            logger.info("Book inserted with ID: {}", book.getId());
            return book;
        } catch (SQLException e) {
            logger.error("Error inserting book", e);
            throw new RuntimeException("Error inserting book", e);
        }
    }

    private Book update(Book book) {
        String sql = "UPDATE books SET title=?, author=?, isbn=?, published_year=?, quantity=?, available_quantity=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setObject(4, book.getPublishedYear());
            stmt.setInt(5, book.getQuantity());
            stmt.setInt(6, book.getAvailableQuantity());
            stmt.setLong(7, book.getId());

            stmt.executeUpdate();
            logger.info("Book updated with ID: {}", book.getId());
            return book;
        } catch (SQLException e) {
            logger.error("Error updating book", e);
            throw new RuntimeException("Error updating book", e);
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding book by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding book by ISBN", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Book> findAll() {
        String sql = "SELECT * FROM books ORDER BY title";
        return executeQuery(sql);
    }

    @Override
    public List<Book> findByTitle(String title) {
        String sql = "SELECT * FROM books WHERE title LIKE ? ORDER BY title";
        return executeQueryWithLike(sql, "%" + title + "%");
    }

    @Override
    public List<Book> findByAuthor(String author) {
        String sql = "SELECT * FROM books WHERE author LIKE ? ORDER BY author";
        return executeQueryWithLike(sql, "%" + author + "%");
    }

    @Override
    public List<Book> findByTitleOrAuthor(String keyword) {
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY title";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding books by title or author", e);
        }
        return books;
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        String sql = "SELECT COUNT(*) FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking ISBN existence", e);
        }
        return false;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            logger.info("Book deleted with ID: {}, affected rows: {}", id, affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting book", e);
            return false;
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting books", e);
        }
        return 0;
    }

    private List<Book> executeQuery(String sql) {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            logger.error("Error executing query", e);
        }
        return books;
    }

    private List<Book> executeQueryWithLike(String sql, String param) {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing query with like", e);
        }
        return books;
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublishedYear(rs.getInt("published_year"));
        book.setQuantity(rs.getInt("quantity"));
        book.setAvailableQuantity(rs.getInt("available_quantity"));
        book.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        book.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return book;
    }
}