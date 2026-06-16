package com.library.repository;

import com.library.entity.Member;
import com.library.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberRepositoryImpl implements MemberRepository {
    private static final Logger logger = LoggerFactory.getLogger(MemberRepositoryImpl.class);

    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            return insert(member);
        } else {
            return update(member);
        }
    }

    private Member insert(Member member) {
        String sql = "INSERT INTO members (name, national_code, phone_number, join_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getNationalCode());
            stmt.setString(3, member.getPhoneNumber());
            stmt.setDate(4, Date.valueOf(member.getJoinDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating member failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    member.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating member failed, no ID obtained.");
                }
            }
            logger.info("Member inserted with ID: {}", member.getId());
            return member;
        } catch (SQLException e) {
            logger.error("Error inserting member", e);
            throw new RuntimeException("Error inserting member", e);
        }
    }

    private Member update(Member member) {
        String sql = "UPDATE members SET name=?, national_code=?, phone_number=?, join_date=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getNationalCode());
            stmt.setString(3, member.getPhoneNumber());
            stmt.setDate(4, Date.valueOf(member.getJoinDate()));
            stmt.setLong(5, member.getId());

            stmt.executeUpdate();
            logger.info("Member updated with ID: {}", member.getId());
            return member;
        } catch (SQLException e) {
            logger.error("Error updating member", e);
            throw new RuntimeException("Error updating member", e);
        }
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMember(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding member by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Member> findByNationalCode(String nationalCode) {
        String sql = "SELECT * FROM members WHERE national_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nationalCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMember(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding member by national code", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM members ORDER BY name";
        return executeQuery(sql);
    }

    @Override
    public List<Member> findByName(String name) {
        String sql = "SELECT * FROM members WHERE name LIKE ? ORDER BY name";
        return executeQueryWithLike(sql, "%" + name + "%");
    }

    @Override
    public List<Member> findByNationalCodeContaining(String nationalCode) {
        String sql = "SELECT * FROM members WHERE national_code LIKE ? ORDER BY national_code";
        return executeQueryWithLike(sql, "%" + nationalCode + "%");
    }

    @Override
    public boolean existsByNationalCode(String nationalCode) {
        String sql = "SELECT COUNT(*) FROM members WHERE national_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nationalCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking national code existence", e);
        }
        return false;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            logger.info("Member deleted with ID: {}, affected rows: {}", id, affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting member", e);
            return false;
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM members";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting members", e);
        }
        return 0;
    }

    private List<Member> executeQuery(String sql) {
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            logger.error("Error executing query", e);
        }
        return members;
    }

    private List<Member> executeQueryWithLike(String sql, String param) {
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSetToMember(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing query with like", e);
        }
        return members;
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getLong("id"));
        member.setName(rs.getString("name"));
        member.setNationalCode(rs.getString("national_code"));
        member.setPhoneNumber(rs.getString("phone_number"));
        member.setJoinDate(rs.getDate("join_date").toLocalDate());
        member.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        member.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return member;
    }
}