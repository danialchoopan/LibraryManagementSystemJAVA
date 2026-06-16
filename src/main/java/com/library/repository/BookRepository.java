package com.library.repository;

import com.library.entity.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(Long id);
    Optional<Book> findByIsbn(String isbn);
    List<Book> findAll();
    List<Book> findByTitle(String title);
    List<Book> findByAuthor(String author);
    List<Book> findByTitleOrAuthor(String keyword);
    boolean existsByIsbn(String isbn);
    boolean deleteById(Long id);
    long count();
}