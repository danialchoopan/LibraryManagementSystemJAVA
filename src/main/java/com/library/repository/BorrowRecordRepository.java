package com.library.repository;

import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository {
    BorrowRecord save(BorrowRecord borrowRecord);
    Optional<BorrowRecord> findById(Long id);
    List<BorrowRecord> findAll();
    List<BorrowRecord> findByBookId(Long bookId);
    List<BorrowRecord> findByMemberId(Long memberId);
    List<BorrowRecord> findByStatus(BorrowStatus status);
    List<BorrowRecord> findActiveBorrowsByMemberId(Long memberId);
    List<BorrowRecord> findOverdueRecords(LocalDate currentDate);
    long countActiveBorrowsByMemberId(Long memberId);
    boolean existsActiveBorrowByBookId(Long bookId);
    boolean existsActiveBorrowByMemberId(Long memberId);
}