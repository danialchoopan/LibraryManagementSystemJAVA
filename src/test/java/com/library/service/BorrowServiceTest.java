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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private Book testBook;
    private Member testMember;
    private BorrowRecord testBorrowRecord;

    @BeforeEach
    void setUp() {
        testBook = new Book("Test Book", "Test Author", "1234567890", 2020, 5, 5);
        testBook.setId(1L);

        testMember = new Member("John Doe", "1234567890", "+1-555-0101", LocalDate.of(2023, 1, 15));
        testMember.setId(1L);

        testBorrowRecord = new BorrowRecord(1L, 1L, LocalDate.now(), BorrowStatus.BORROWED);
        testBorrowRecord.setId(1L);
    }

    @Test
    void borrowBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowRecordRepository.countActiveBorrowsByMemberId(1L)).thenReturn(0L);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(testBorrowRecord);

        BorrowRecord result = borrowService.borrowBook(1L, 1L);

        assertNotNull(result);
        assertEquals(BorrowStatus.BORROWED, result.getStatus());
        verify(bookRepository).findById(1L);
        verify(memberRepository).findById(1L);
        verify(borrowRecordRepository).countActiveBorrowsByMemberId(1L);
        verify(bookRepository).save(testBook);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void borrowBook_BookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> borrowService.borrowBook(1L, 1L));
        verify(bookRepository).findById(1L);
        verify(memberRepository, never()).findById(any());
    }

    @Test
    void borrowBook_MemberNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> borrowService.borrowBook(1L, 1L));
        verify(bookRepository).findById(1L);
        verify(memberRepository).findById(1L);
    }

    @Test
    void borrowBook_BookNotAvailable() {
        testBook.setAvailableQuantity(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        assertThrows(BookNotAvailableException.class, () -> borrowService.borrowBook(1L, 1L));
        verify(bookRepository).findById(1L);
        verify(memberRepository).findById(1L);
        verify(borrowRecordRepository, never()).countActiveBorrowsByMemberId(any());
    }

    @Test
    void borrowBook_BorrowLimitExceeded() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowRecordRepository.countActiveBorrowsByMemberId(1L)).thenReturn(3L);

        assertThrows(BorrowLimitExceededException.class, () -> borrowService.borrowBook(1L, 1L));
        verify(bookRepository).findById(1L);
        verify(memberRepository).findById(1L);
        verify(borrowRecordRepository).countActiveBorrowsByMemberId(1L);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void returnBook_Success() {
        testBorrowRecord.setBorrowDate(LocalDate.now().minusDays(5));
        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(testBorrowRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(testBorrowRecord);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BorrowRecord result = borrowService.returnBook(1L);

        assertNotNull(result);
        assertEquals(BorrowStatus.RETURNED, result.getStatus());
        assertEquals(LocalDate.now(), result.getReturnDate());
        verify(borrowRecordRepository).findById(1L);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(testBook);
    }

    @Test
    void returnBook_Overdue() {
        testBorrowRecord.setBorrowDate(LocalDate.now().minusDays(20));
        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(testBorrowRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(testBorrowRecord);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BorrowRecord result = borrowService.returnBook(1L);

        assertNotNull(result);
        assertEquals(BorrowStatus.OVERDUE, result.getStatus());
        assertEquals(LocalDate.now(), result.getReturnDate());
        verify(borrowRecordRepository).findById(1L);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void getBorrowHistoryByMemberId() {
        List<BorrowRecord> records = Arrays.asList(testBorrowRecord);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowRecordRepository.findByMemberId(1L)).thenReturn(records);

        List<BorrowRecord> result = borrowService.getBorrowHistoryByMemberId(1L);

        assertEquals(1, result.size());
        verify(memberRepository).findById(1L);
        verify(borrowRecordRepository).findByMemberId(1L);
    }

    @Test
    void getBorrowHistoryByMemberId_MemberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> borrowService.getBorrowHistoryByMemberId(1L));
        verify(memberRepository).findById(1L);
        verify(borrowRecordRepository, never()).findByMemberId(any());
    }

    @Test
    void getActiveBorrows() {
        List<BorrowRecord> records = Arrays.asList(testBorrowRecord);
        when(borrowRecordRepository.findByStatus(BorrowStatus.BORROWED)).thenReturn(records);

        List<BorrowRecord> result = borrowService.getActiveBorrows();

        assertEquals(1, result.size());
        verify(borrowRecordRepository).findByStatus(BorrowStatus.BORROWED);
    }
}