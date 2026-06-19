package com.library.repository;

import com.library.entity.Book;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookRepositoryIntegrationTest {

    private static BookRepositoryImpl repo;

    @BeforeAll
    static void setUp() {
        repo = new BookRepositoryImpl();
    }

    @Test
    @Order(1)
    void insertBook() {
        Book book = new Book("Test Book", "Test Author", "TEST-ISBN-001", 2024, 5, 5);
        Book saved = repo.save(book);

        assertNotNull(saved.getId());
        assertEquals("Test Book", saved.getTitle());
        assertEquals("Test Author", saved.getAuthor());
        assertEquals("TEST-ISBN-001", saved.getIsbn());
        assertEquals(5, saved.getQuantity());
        assertEquals(5, saved.getAvailableQuantity());
    }

    @Test
    @Order(2)
    void findById() {
        Book book = new Book("Find Me", "Author", "FIND-ISBN-001", 2023, 3, 3);
        Book saved = repo.save(book);

        Optional<Book> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
    }

    @Test
    @Order(3)
    void findByIsbn() {
        Book book = new Book("ISBN Book", "Author", "UNIQUE-ISBN-001", 2022, 2, 2);
        repo.save(book);

        Optional<Book> found = repo.findByIsbn("UNIQUE-ISBN-001");
        assertTrue(found.isPresent());
        assertEquals("ISBN Book", found.get().getTitle());
    }

    @Test
    @Order(4)
    void updateBook() {
        Book book = new Book("Original", "Author", "UPDATE-ISBN-001", 2020, 10, 10);
        Book saved = repo.save(book);

        saved.setTitle("Updated Title");
        saved.setQuantity(20);
        saved.setAvailableQuantity(15);
        repo.save(saved);

        Optional<Book> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Title", found.get().getTitle());
        assertEquals(20, found.get().getQuantity());
        assertEquals(15, found.get().getAvailableQuantity());
    }

    @Test
    @Order(5)
    void deleteBook() {
        Book book = new Book("Delete Me", "Author", "DELETE-ISBN-001", 2021, 1, 1);
        Book saved = repo.save(book);

        boolean deleted = repo.deleteById(saved.getId());
        assertTrue(deleted);

        Optional<Book> found = repo.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @Order(6)
    void findAll() {
        int before = repo.findAll().size();
        repo.save(new Book("List1", "A", "LIST-ISBN-001", 2024, 1, 1));
        repo.save(new Book("List2", "B", "LIST-ISBN-002", 2024, 1, 1));

        List<Book> all = repo.findAll();
        assertTrue(all.size() >= before + 2);
    }

    @Test
    @Order(7)
    void searchByTitle() {
        repo.save(new Book("UniqueSearchXYZ", "Author", "SEARCH-ISBN-001", 2024, 1, 1));

        List<Book> results = repo.findByTitleOrAuthor("UniqueSearchXYZ");
        assertFalse(results.isEmpty());
        assertEquals("UniqueSearchXYZ", results.get(0).getTitle());
    }

    @Test
    @Order(8)
    void existsByIsbn() {
        repo.save(new Book("Exists", "Author", "EXISTS-ISBN-001", 2024, 1, 1));

        assertTrue(repo.existsByIsbn("EXISTS-ISBN-001"));
        assertFalse(repo.existsByIsbn("NOPE-ISBN-999"));
    }

    @Test
    @Order(9)
    void count() {
        long count = repo.count();
        assertTrue(count > 0);
    }

    @Test
    @Order(10)
    void insertDuplicateIsbn_ThrowsException() {
        repo.save(new Book("Dup1", "Author", "DUP-ISBN-001", 2024, 1, 1));
        assertThrows(RuntimeException.class, () ->
                repo.save(new Book("Dup2", "Author", "DUP-ISBN-001", 2024, 1, 1)));
    }
}
