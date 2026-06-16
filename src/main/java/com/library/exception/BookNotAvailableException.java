package com.library.exception;

public class BookNotAvailableException extends LibraryException {
    public BookNotAvailableException(String message) {
        super(message);
    }

    public BookNotAvailableException(Long bookId) {
        super("Book with ID " + bookId + " is not available for borrowing");
    }
}