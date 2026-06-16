package com.library.service;

import com.library.entity.BorrowRecord;
import java.time.LocalDate;
import java.util.List;

public interface BorrowService {
    BorrowRecord borrowBook(Long bookId, Long memberId);
    BorrowRecord returnBook(Long borrowRecordId);
    BorrowRecord getBorrowRecordById(Long id);
    List<BorrowRecord> getBorrowHistoryByMemberId(Long memberId);
    List<BorrowRecord> getActiveBorrows();
    List<BorrowRecord> getOverdueBooks();
    long getActiveBorrowCountByMemberId(Long memberId);
}