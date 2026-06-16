package com.library.service;

import com.library.entity.Book;
import java.util.List;

public interface BookService {
    Book addBook(Book book);
    Book updateBook(Book book);
    void deleteBook(Long id);
    Book getBookById(Long id);
    Book getBookByIsbn(String isbn);
    List<Book> getAllBooks();
    List<Book> searchBooks(String keyword);
    List<Book> getBooksByTitle(String title);
    List<Book> getBooksByAuthor(String author);
    boolean isIsbnAvailable(String isbn);
}