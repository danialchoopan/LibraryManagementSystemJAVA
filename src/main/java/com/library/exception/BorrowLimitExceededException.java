package com.library.exception;

public class BorrowLimitExceededException extends LibraryException {
    public BorrowLimitExceededException(String message) {
        super(message);
    }

    public BorrowLimitExceededException(Long memberId) {
        super("Member with ID " + memberId + " has exceeded the maximum borrow limit of 3 books");
    }
}