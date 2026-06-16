package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Member;
import com.library.exception.BookNotAvailableException;
import com.library.exception.BorrowLimitExceededException;
import com.library.exception.BookNotFoundException;
import com.library.exception.MemberNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.library.util.DatabaseConnection;

public class BorrowServiceImpl implements BorrowService {
    private static final Logger logger = LoggerFactory.getLogger(BorrowServiceImpl.class);
    private static final int MAX_BORROW_LIMIT = 3;
    private static final int MAX_BORROW_DAYS = 14;

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public BorrowServiceImpl(BorrowRecordRepository borrowRecordRepository,
                             BookRepository bookRepository,
                             MemberRepository memberRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public BorrowRecord borrowBook(Long bookId, Long memberId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (!bookOpt.isPresent()) {
                throw new BookNotFoundException(bookId);
            }
            Book book = bookOpt.get();

            Optional<Member> memberOpt = memberRepository.findById(memberId);
            if (!memberOpt.isPresent()) {
                throw new MemberNotFoundException(memberId);
            }

            if (book.getAvailableQuantity() <= 0) {
                throw new BookNotAvailableException(bookId);
            }

            long activeBorrows = borrowRecordRepository.countActiveBorrowsByMemberId(memberId);
            if (activeBorrows >= MAX_BORROW_LIMIT) {
                throw new BorrowLimitExceededException(memberId);
            }

            book.setAvailableQuantity(book.getAvailableQuantity() - 1);
            bookRepository.save(book);

            BorrowRecord record = new BorrowRecord(bookId, memberId, LocalDate.now(), BorrowStatus.BORROWED);
            BorrowRecord savedRecord = borrowRecordRepository.save(record);

            conn.commit();
            logger.info("Book borrowed successfully. Book ID: {}, Member ID: {}", bookId, memberId);
            return savedRecord;

        } catch (SQLException e) {
            rollbackConnection(conn);
            logger.error("Error borrowing book", e);
            throw new RuntimeException("Error borrowing book", e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public BorrowRecord returnBook(Long borrowRecordId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Optional<BorrowRecord> recordOpt = borrowRecordRepository.findById(borrowRecordId);
            if (!recordOpt.isPresent()) {
                throw new IllegalArgumentException("Borrow record not found with ID: " + borrowRecordId);
            }
            BorrowRecord record = recordOpt.get();

            if (record.getStatus() == BorrowStatus.RETURNED) {
                throw new IllegalArgumentException("Book already returned");
            }

            LocalDate returnDate = LocalDate.now();
            record.setReturnDate(returnDate);

            long borrowDays = java.time.temporal.ChronoUnit.DAYS.between(record.getBorrowDate(), returnDate);
            if (borrowDays > MAX_BORROW_DAYS) {
                record.setStatus(BorrowStatus.OVERDUE);
                logger.warn("Book returned with delay. Days borrowed: {}", borrowDays);
                System.out.println("Book returned with delay");
            } else {
                record.setStatus(BorrowStatus.RETURNED);
            }

            borrowRecordRepository.save(record);

            Optional<Book> bookOpt = bookRepository.findById(record.getBookId());
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                book.setAvailableQuantity(book.getAvailableQuantity() + 1);
                bookRepository.save(book);
            }

            conn.commit();
            logger.info("Book returned successfully. Borrow record ID: {}", borrowRecordId);
            return record;

        } catch (SQLException e) {
            rollbackConnection(conn);
            logger.error("Error returning book", e);
            throw new RuntimeException("Error returning book", e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public BorrowRecord getBorrowRecordById(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found with ID: " + id));
    }

    @Override
    public List<BorrowRecord> getBorrowHistoryByMemberId(Long memberId) {
        if (!memberRepository.findById(memberId).isPresent()) {
            throw new MemberNotFoundException(memberId);
        }
        return borrowRecordRepository.findByMemberId(memberId);
    }

    @Override
    public List<BorrowRecord> getActiveBorrows() {
        return borrowRecordRepository.findByStatus(BorrowStatus.BORROWED);
    }

    @Override
    public List<BorrowRecord> getOverdueBooks() {
        return borrowRecordRepository.findOverdueRecords(LocalDate.now());
    }

    @Override
    public long getActiveBorrowCountByMemberId(Long memberId) {
        return borrowRecordRepository.countActiveBorrowsByMemberId(memberId);
    }

    private void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("Error rolling back connection", e);
            }
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }
}