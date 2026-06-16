package com.library.ui;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Member;
import com.library.exception.BookNotAvailableException;
import com.library.exception.BorrowLimitExceededException;
import com.library.exception.BookNotFoundException;
import com.library.exception.MemberNotFoundException;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BookService bookService;
    private final MemberService memberService;
    private final BorrowService borrowService;
    private final Scanner scanner;

    public ConsoleUI(BookService bookService, MemberService memberService, BorrowService borrowService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.borrowService = borrowService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to Library Management System");
        System.out.println("==================================");

        while (true) {
            showMainMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    bookMenu();
                    break;
                case 2:
                    memberMenu();
                    break;
                case 3:
                    borrowMenu();
                    break;
                case 0:
                    System.out.println("Thank you for using Library Management System!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Book Management");
        System.out.println("2. Member Management");
        System.out.println("3. Borrow Management");
        System.out.println("0. Exit");
    }

    private void bookMenu() {
        while (true) {
            System.out.println("\n--- Book Management ---");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. Search Books");
            System.out.println("5. View All Books");
            System.out.println("6. View Book by ISBN");
            System.out.println("0. Back to Main Menu");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    updateBook();
                    break;
                case 3:
                    deleteBook();
                    break;
                case 4:
                    searchBooks();
                    break;
                case 5:
                    viewAllBooks();
                    break;
                case 6:
                    viewBookByIsbn();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void memberMenu() {
        while (true) {
            System.out.println("\n--- Member Management ---");
            System.out.println("1. Register Member");
            System.out.println("2. Update Member");
            System.out.println("3. Delete Member");
            System.out.println("4. Search Members");
            System.out.println("5. View All Members");
            System.out.println("0. Back to Main Menu");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    addMember();
                    break;
                case 2:
                    updateMember();
                    break;
                case 3:
                    deleteMember();
                    break;
                case 4:
                    searchMembers();
                    break;
                case 5:
                    viewAllMembers();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void borrowMenu() {
        while (true) {
            System.out.println("\n--- Borrow Management ---");
            System.out.println("1. Borrow Book");
            System.out.println("2. Return Book");
            System.out.println("3. View Borrow History");
            System.out.println("4. View Active Borrows");
            System.out.println("5. View Overdue Books");
            System.out.println("0. Back to Main Menu");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    borrowBook();
                    break;
                case 2:
                    returnBook();
                    break;
                case 3:
                    viewBorrowHistory();
                    break;
                case 4:
                    viewActiveBorrows();
                    break;
                case 5:
                    viewOverdueBooks();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void addBook() {
        System.out.println("\n--- Add New Book ---");
        String title = getStringInput("Enter title: ");
        String author = getStringInput("Enter author: ");
        String isbn = getStringInput("Enter ISBN: ");
        Integer publishedYear = getIntInputOrNull("Enter published year (or press Enter to skip): ");
        int quantity = getIntInput("Enter quantity: ");

        try {
            Book book = new Book(title, author, isbn, publishedYear, quantity, quantity);
            bookService.addBook(book);
            System.out.println("Book added successfully with ID: " + book.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateBook() {
        System.out.println("\n--- Update Book ---");
        Long id = getLongInput("Enter book ID to update: ");

        try {
            Book existingBook = bookService.getBookById(id);
            System.out.println("Current book details:");
            System.out.println(existingBook);

            String title = getStringInput("Enter new title (or press Enter to keep current): ");
            String author = getStringInput("Enter new author (or press Enter to keep current): ");
            String isbn = getStringInput("Enter new ISBN (or press Enter to keep current): ");
            Integer publishedYear = getIntInputOrNull("Enter new published year (or press Enter to keep current): ");
            Integer quantity = getIntInputOrNull("Enter new quantity (or press Enter to keep current): ");

            if (!title.isEmpty()) existingBook.setTitle(title);
            if (!author.isEmpty()) existingBook.setAuthor(author);
            if (!isbn.isEmpty()) existingBook.setIsbn(isbn);
            if (publishedYear != null) existingBook.setPublishedYear(publishedYear);
            if (quantity != null) existingBook.setQuantity(quantity);

            bookService.updateBook(existingBook);
            System.out.println("Book updated successfully");
        } catch (BookNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteBook() {
        System.out.println("\n--- Delete Book ---");
        Long id = getLongInput("Enter book ID to delete: ");

        try {
            bookService.deleteBook(id);
            System.out.println("Book deleted successfully");
        } catch (BookNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void searchBooks() {
        System.out.println("\n--- Search Books ---");
        String keyword = getStringInput("Enter search keyword (title or author): ");

        List<Book> books = bookService.searchBooks(keyword);
        if (books.isEmpty()) {
            System.out.println("No books found matching the keyword");
        } else {
            System.out.println("Found " + books.size() + " book(s):");
            books.forEach(System.out::println);
        }
    }

    private void viewAllBooks() {
        System.out.println("\n--- All Books ---");
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books available");
        } else {
            System.out.println("Total books: " + books.size());
            books.forEach(System.out::println);
        }
    }

    private void viewBookByIsbn() {
        System.out.println("\n--- View Book by ISBN ---");
        String isbn = getStringInput("Enter ISBN: ");

        try {
            Book book = bookService.getBookByIsbn(isbn);
            System.out.println(book);
        } catch (BookNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addMember() {
        System.out.println("\n--- Register New Member ---");
        String name = getStringInput("Enter name: ");
        String nationalCode = getStringInput("Enter national code: ");
        String phoneNumber = getStringInput("Enter phone number: ");
        LocalDate joinDate = getDateInput("Enter join date (yyyy-MM-dd): ");

        try {
            Member member = new Member(name, nationalCode, phoneNumber, joinDate);
            memberService.addMember(member);
            System.out.println("Member registered successfully with ID: " + member.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateMember() {
        System.out.println("\n--- Update Member ---");
        Long id = getLongInput("Enter member ID to update: ");

        try {
            Member existingMember = memberService.getMemberById(id);
            System.out.println("Current member details:");
            System.out.println(existingMember);

            String name = getStringInput("Enter new name (or press Enter to keep current): ");
            String nationalCode = getStringInput("Enter new national code (or press Enter to keep current): ");
            String phoneNumber = getStringInput("Enter new phone number (or press Enter to keep current): ");
            LocalDate joinDate = getDateInputOrNull("Enter new join date (yyyy-MM-dd or press Enter to keep current): ");

            if (!name.isEmpty()) existingMember.setName(name);
            if (!nationalCode.isEmpty()) existingMember.setNationalCode(nationalCode);
            if (!phoneNumber.isEmpty()) existingMember.setPhoneNumber(phoneNumber);
            if (joinDate != null) existingMember.setJoinDate(joinDate);

            memberService.updateMember(existingMember);
            System.out.println("Member updated successfully");
        } catch (MemberNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteMember() {
        System.out.println("\n--- Delete Member ---");
        Long id = getLongInput("Enter member ID to delete: ");

        try {
            memberService.deleteMember(id);
            System.out.println("Member deleted successfully");
        } catch (MemberNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void searchMembers() {
        System.out.println("\n--- Search Members ---");
        String keyword = getStringInput("Enter search keyword (name): ");

        List<Member> members = memberService.searchMembers(keyword);
        if (members.isEmpty()) {
            System.out.println("No members found matching the keyword");
        } else {
            System.out.println("Found " + members.size() + " member(s):");
            members.forEach(System.out::println);
        }
    }

    private void viewAllMembers() {
        System.out.println("\n--- All Members ---");
        List<Member> members = memberService.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members registered");
        } else {
            System.out.println("Total members: " + members.size());
            members.forEach(System.out::println);
        }
    }

    private void borrowBook() {
        System.out.println("\n--- Borrow Book ---");
        Long bookId = getLongInput("Enter book ID: ");
        Long memberId = getLongInput("Enter member ID: ");

        try {
            BorrowRecord record = borrowService.borrowBook(bookId, memberId);
            System.out.println("Book borrowed successfully. Borrow record ID: " + record.getId());
        } catch (BookNotFoundException | MemberNotFoundException | BookNotAvailableException | BorrowLimitExceededException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void returnBook() {
        System.out.println("\n--- Return Book ---");
        Long borrowRecordId = getLongInput("Enter borrow record ID: ");

        try {
            BorrowRecord record = borrowService.returnBook(borrowRecordId);
            System.out.println("Book returned successfully");
            if (record.getStatus().name().equals("OVERDUE")) {
                System.out.println("Note: Book was returned with delay");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewBorrowHistory() {
        System.out.println("\n--- Borrow History ---");
        Long memberId = getLongInput("Enter member ID: ");

        try {
            List<BorrowRecord> records = borrowService.getBorrowHistoryByMemberId(memberId);
            if (records.isEmpty()) {
                System.out.println("No borrow history found for this member");
            } else {
                System.out.println("Borrow history for member ID " + memberId + ":");
                records.forEach(System.out::println);
            }
        } catch (MemberNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewActiveBorrows() {
        System.out.println("\n--- Active Borrows ---");
        List<BorrowRecord> records = borrowService.getActiveBorrows();
        if (records.isEmpty()) {
            System.out.println("No active borrows");
        } else {
            System.out.println("Active borrows: " + records.size());
            records.forEach(System.out::println);
        }
    }

    private void viewOverdueBooks() {
        System.out.println("\n--- Overdue Books ---");
        List<BorrowRecord> records = borrowService.getOverdueBooks();
        if (records.isEmpty()) {
            System.out.println("No overdue books");
        } else {
            System.out.println("Overdue books: " + records.size());
            records.forEach(System.out::println);
        }
    }

    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
    }

    private Long getLongInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                long value = Long.parseLong(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }
    }

    private Integer getIntInputOrNull(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number, keeping current value");
            return null;
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private LocalDate getDateInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd");
            }
        }
    }

    private LocalDate getDateInputOrNull(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(input, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format, keeping current value");
            return null;
        }
    }
}