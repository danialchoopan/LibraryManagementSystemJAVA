package com.library.repository;

import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BorrowRecordRepositoryImpl implements BorrowRecordRepository {
    private static final Logger logger = LoggerFactory.getLogger(BorrowRecordRepositoryImpl.class);

    @Override
    public BorrowRecord save(BorrowRecord borrowRecord) {
        if (borrowRecord.getId() == null) {
            return insert(borrowRecord);
        } else {
            return update(borrowRecord);
        }
    }

    private BorrowRecord insert(BorrowRecord borrowRecord) {
        String sql = "INSERT INTO borrow_records (book_id, member_id, borrow_date, return_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, borrowRecord.getBookId());
            stmt.setLong(2, borrowRecord.getMemberId());
            stmt.setDate(3, Date.valueOf(borrowRecord.getBorrowDate()));
            stmt.setDate(4, borrowRecord.getReturnDate() != null ? Date.valueOf(borrowRecord.getReturnDate()) : null);
            stmt.setString(5, borrowRecord.getStatus().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating borrow record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    borrowRecord.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating borrow record failed, no ID obtained.");
                }
            }
            logger.info("Borrow record inserted with ID: {}", borrowRecord.getId());
            return borrowRecord;
        } catch (SQLException e) {
            logger.error("Error inserting borrow record", e);
            throw new RuntimeException("Error inserting borrow record", e);
        }
    }

    private BorrowRecord update(BorrowRecord borrowRecord) {
        String sql = "UPDATE borrow_records SET book_id=?, member_id=?, borrow_date=?, return_date=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, borrowRecord.getBookId());
            stmt.setLong(2, borrowRecord.getMemberId());
            stmt.setDate(3, Date.valueOf(borrowRecord.getBorrowDate()));
            stmt.setDate(4, borrowRecord.getReturnDate() != null ? Date.valueOf(borrowRecord.getReturnDate()) : null);
            stmt.setString(5, borrowRecord.getStatus().name());
            stmt.setLong(6, borrowRecord.getId());

            stmt.executeUpdate();
            logger.info("Borrow record updated with ID: {}", borrowRecord.getId());
            return borrowRecord;
        } catch (SQLException e) {
            logger.error("Error updating borrow record", e);
            throw new RuntimeException("Error updating borrow record", e);
        }
    }

    @Override
    public Optional<BorrowRecord> findById(Long id) {
        String sql = "SELECT * FROM borrow_records WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBorrowRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding borrow record by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<BorrowRecord> findAll() {
        String sql = "SELECT * FROM borrow_records ORDER BY borrow_date DESC";
        return executeQuery(sql);
    }

    @Override
    public List<BorrowRecord> findByBookId(Long bookId) {
        String sql = "SELECT * FROM borrow_records WHERE book_id = ? ORDER BY borrow_date DESC";
        return executeQueryWithParam(sql, bookId);
    }

    @Override
    public List<BorrowRecord> findByMemberId(Long memberId) {
        String sql = "SELECT * FROM borrow_records WHERE member_id = ? ORDER BY borrow_date DESC";
        return executeQueryWithParam(sql, memberId);
    }

    @Override
    public List<BorrowRecord> findByStatus(BorrowStatus status) {
        String sql = "SELECT * FROM borrow_records WHERE status = ? ORDER BY borrow_date DESC";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToBorrowRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding borrow records by status", e);
        }
        return records;
    }

    @Override
    public List<BorrowRecord> findActiveBorrowsByMemberId(Long memberId) {
        String sql = "SELECT * FROM borrow_records WHERE member_id = ? AND status = 'BORROWED' ORDER BY borrow_date DESC";
        return executeQueryWithParam(sql, memberId);
    }

    @Override
    public List<BorrowRecord> findOverdueRecords(LocalDate currentDate) {
        String sql = "SELECT * FROM borrow_records WHERE status = 'BORROWED' AND DATEADD(DAY, 14, borrow_date) < ? ORDER BY borrow_date";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(currentDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToBorrowRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding overdue records", e);
        }
        return records;
    }

    @Override
    public long countActiveBorrowsByMemberId(Long memberId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE member_id = ? AND status = 'BORROWED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting active borrows", e);
        }
        return 0;
    }

    @Override
    public boolean existsActiveBorrowByBookId(Long bookId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE book_id = ? AND status = 'BORROWED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking active borrow by book ID", e);
        }
        return false;
    }

    @Override
    public boolean existsActiveBorrowByMemberId(Long memberId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE member_id = ? AND status = 'BORROWED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking active borrow by member ID", e);
        }
        return false;
    }

    private List<BorrowRecord> executeQuery(String sql) {
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error executing query", e);
        }
        return records;
    }

    private List<BorrowRecord> executeQueryWithParam(String sql, Long param) {
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToBorrowRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing query with param", e);
        }
        return records;
    }

    private BorrowRecord mapResultSetToBorrowRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getLong("id"));
        record.setBookId(rs.getLong("book_id"));
        record.setMemberId(rs.getLong("member_id"));
        record.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
        Date returnDate = rs.getDate("return_date");
        record.setReturnDate(returnDate != null ? returnDate.toLocalDate() : null);
        record.setStatus(BorrowStatus.valueOf(rs.getString("status")));
        record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        record.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return record;
    }
}