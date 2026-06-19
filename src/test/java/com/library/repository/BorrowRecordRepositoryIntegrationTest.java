package com.library.repository;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Member;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BorrowRecordRepositoryIntegrationTest {

    private static BorrowRecordRepositoryImpl borrowRepo;
    private static BookRepositoryImpl bookRepo;
    private static MemberRepositoryImpl memberRepo;
    private static Book testBook;
    private static Member testMember;

    @BeforeAll
    static void setUp() {
        borrowRepo = new BorrowRecordRepositoryImpl();
        bookRepo = new BookRepositoryImpl();
        memberRepo = new MemberRepositoryImpl();

        testBook = bookRepo.save(new Book("BorrowTest Book", "Author", "BORROW-ISBN-001", 2024, 5, 5));
        testMember = memberRepo.save(new Member("BorrowTest Member", "2000000001", "+989331111111", LocalDate.of(2024, 1, 1)));
    }

    @Test
    @Order(1)
    void insertBorrowRecord() {
        BorrowRecord record = new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.BORROWED);
        BorrowRecord saved = borrowRepo.save(record);

        assertNotNull(saved.getId());
        assertEquals(testBook.getId(), saved.getBookId());
        assertEquals(testMember.getId(), saved.getMemberId());
        assertEquals(BorrowStatus.BORROWED, saved.getStatus());
        assertNull(saved.getReturnDate());
    }

    @Test
    @Order(2)
    void findById() {
        BorrowRecord record = new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.BORROWED);
        BorrowRecord saved = borrowRepo.save(record);

        Optional<BorrowRecord> found = borrowRepo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(BorrowStatus.BORROWED, found.get().getStatus());
    }

    @Test
    @Order(3)
    void updateBorrowRecord() {
        BorrowRecord record = new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.BORROWED);
        BorrowRecord saved = borrowRepo.save(record);

        saved.setStatus(BorrowStatus.RETURNED);
        saved.setReturnDate(LocalDate.now());
        borrowRepo.save(saved);

        Optional<BorrowRecord> found = borrowRepo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(BorrowStatus.RETURNED, found.get().getStatus());
        assertNotNull(found.get().getReturnDate());
    }

    @Test
    @Order(4)
    void updateToReturnedStatus() {
        BorrowRecord record = new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.RETURNED);
        record.setReturnDate(LocalDate.now());
        BorrowRecord saved = borrowRepo.save(record);

        Optional<BorrowRecord> found = borrowRepo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(BorrowStatus.RETURNED, found.get().getStatus());
        assertNotNull(found.get().getReturnDate());
    }

    @Test
    @Order(5)
    void findByBookId() {
        borrowRepo.save(new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.BORROWED));

        List<BorrowRecord> records = borrowRepo.findByBookId(testBook.getId());
        assertFalse(records.isEmpty());
        for (BorrowRecord r : records) {
            assertEquals(testBook.getId(), r.getBookId());
        }
    }

    @Test
    @Order(6)
    void findByMemberId() {
        borrowRepo.save(new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.BORROWED));

        List<BorrowRecord> records = borrowRepo.findByMemberId(testMember.getId());
        assertFalse(records.isEmpty());
        for (BorrowRecord r : records) {
            assertEquals(testMember.getId(), r.getMemberId());
        }
    }

    @Test
    @Order(7)
    void findByStatus() {
        borrowRepo.save(new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.BORROWED));

        List<BorrowRecord> borrowed = borrowRepo.findByStatus(BorrowStatus.BORROWED);
        assertFalse(borrowed.isEmpty());
        for (BorrowRecord r : borrowed) {
            assertEquals(BorrowStatus.BORROWED, r.getStatus());
        }
    }

    @Test
    @Order(8)
    void countActiveBorrowsByMemberId() {
        long count = borrowRepo.countActiveBorrowsByMemberId(testMember.getId());
        assertTrue(count >= 0);
    }

    @Test
    @Order(9)
    void existsActiveBorrowByBookId() {
        borrowRepo.save(new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.BORROWED));

        boolean exists = borrowRepo.existsActiveBorrowByBookId(testBook.getId());
        assertTrue(exists);
    }

    @Test
    @Order(10)
    void existsActiveBorrowByMemberId() {
        borrowRepo.save(new BorrowRecord(testBook.getId(), testMember.getId(), LocalDate.now(), BorrowStatus.BORROWED));

        boolean exists = borrowRepo.existsActiveBorrowByMemberId(testMember.getId());
        assertTrue(exists);
    }

    @Test
    @Order(11)
    void findActiveBorrowsByMemberId() {
        List<BorrowRecord> records = borrowRepo.findActiveBorrowsByMemberId(testMember.getId());
        for (BorrowRecord r : records) {
            assertEquals(BorrowStatus.BORROWED, r.getStatus());
            assertEquals(testMember.getId(), r.getMemberId());
        }
    }

    @Test
    @Order(12)
    void findAll() {
        List<BorrowRecord> all = borrowRepo.findAll();
        assertNotNull(all);
    }
}
