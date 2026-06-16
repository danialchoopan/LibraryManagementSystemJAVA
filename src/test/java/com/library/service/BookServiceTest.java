package com.library.service;

import com.library.entity.Book;
import com.library.exception.BookNotFoundException;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book("Test Book", "Test Author", "1234567890", 2020, 5, 5);
        testBook.setId(1L);
    }

    @Test
    void addBook_Success() {
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book result = bookService.addBook(testBook);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).existsByIsbn("1234567890");
        verify(bookRepository).save(testBook);
    }

    @Test
    void addBook_DuplicateIsbn_ThrowsException() {
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(testBook));
        verify(bookRepository).existsByIsbn("1234567890");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void getBookById_Found() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        Book result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookById_NotFound_ThrowsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBookById(1L));
        verify(bookRepository).findById(1L);
    }

    @Test
    void getAllBooks() {
        List<Book> books = Arrays.asList(testBook, new Book("Another Book", "Author", "0987654321", 2021, 3, 3));
        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertEquals(2, result.size());
        verify(bookRepository).findAll();
    }

    @Test
    void searchBooks() {
        List<Book> books = Arrays.asList(testBook);
        when(bookRepository.findByTitleOrAuthor("Test")).thenReturn(books);

        List<Book> result = bookService.searchBooks("Test");

        assertEquals(1, result.size());
        verify(bookRepository).findByTitleOrAuthor("Test");
    }

    @Test
    void isIsbnAvailable_True() {
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(false);

        assertTrue(bookService.isIsbnAvailable("1234567890"));
        verify(bookRepository).existsByIsbn("1234567890");
    }

    @Test
    void isIsbnAvailable_False() {
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(true);

        assertFalse(bookService.isIsbnAvailable("1234567890"));
        verify(bookRepository).existsByIsbn("1234567890");
    }
}