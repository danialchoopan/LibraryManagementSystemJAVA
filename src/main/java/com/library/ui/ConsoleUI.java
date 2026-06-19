package com.library.ui;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Member;
import com.library.exception.BookNotAvailableException;
import com.library.exception.BorrowLimitExceededException;
import com.library.exception.BookNotFoundException;
import com.library.exception.MemberNotFoundException;
import com.library.i18n.Language;
import com.library.i18n.MessageManager;
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
    private final MessageManager msg;

    public ConsoleUI(BookService bookService, MemberService memberService, BorrowService borrowService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.borrowService = borrowService;
        this.scanner = new Scanner(System.in);
        this.msg = MessageManager.getInstance();
    }

    public void start() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║          " + msg.getMessage("app.title") + "          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();

        while (true) {
            showMainMenu();
            int choice = getIntInput(msg.getMessage("menu.choice"));

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
                case 4:
                    settingsMenu();
                    break;
                case 0:
                    System.out.println();
                    System.out.println("╔══════════════════════════════════════════════════════╗");
                    System.out.println("║          " + msg.getMessage("menu.exit") + "                          ║");
                    System.out.println("╚══════════════════════════════════════════════════════╝");
                    return;
                default:
                    System.out.println(msg.getMessage("common.invalidInput"));
            }
        }
    }

    private void showMainMenu() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│              " + msg.getMessage("menu.main") + "               │");
        System.out.println("├─────────────────────────────────────────────────────┤");
        System.out.println("│  " + msg.getMessage("menu.books") + "                       │");
        System.out.println("│  " + msg.getMessage("menu.members") + "                      │");
        System.out.println("│  " + msg.getMessage("menu.borrows") + "                     │");
        System.out.println("│  " + msg.getMessage("menu.settings") + "                        │");
        System.out.println("│  " + msg.getMessage("menu.exit") + "                            │");
        System.out.println("└─────────────────────────────────────────────────────┘");
    }

    private void bookMenu() {
        while (true) {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────────────┐");
            System.out.println("│            " + msg.getMessage("book.menu") + "           │");
            System.out.println("├─────────────────────────────────────────────────────┤");
            System.out.println("│  " + msg.getMessage("book.add") + "                          │");
            System.out.println("│  " + msg.getMessage("book.update") + "                       │");
            System.out.println("│  " + msg.getMessage("book.delete") + "                       │");
            System.out.println("│  " + msg.getMessage("book.search") + "                        │");
            System.out.println("│  " + msg.getMessage("book.viewAll") + "                      │");
            System.out.println("│  " + msg.getMessage("book.viewByIsbn") + "                    │");
            System.out.println("│  " + msg.getMessage("book.back") + "                         │");
            System.out.println("└─────────────────────────────────────────────────────┘");

            int choice = getIntInput(msg.getMessage("menu.choice"));

            switch (choice) {
                case 1: addBook(); break;
                case 2: updateBook(); break;
                case 3: deleteBook(); break;
                case 4: searchBooks(); break;
                case 5: viewAllBooks(); break;
                case 6: viewBookByIsbn(); break;
                case 0: return;
                default: System.out.println(msg.getMessage("common.invalidInput"));
            }
        }
    }

    private void memberMenu() {
        while (true) {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────────────┐");
            System.out.println("│            " + msg.getMessage("member.menu") + "          │");
            System.out.println("├─────────────────────────────────────────────────────┤");
            System.out.println("│  " + msg.getMessage("member.add") + "                     │");
            System.out.println("│  " + msg.getMessage("member.update") + "                  │");
            System.out.println("│  " + msg.getMessage("member.delete") + "                  │");
            System.out.println("│  " + msg.getMessage("member.search") + "                   │");
            System.out.println("│  " + msg.getMessage("member.viewAll") + "                  │");
            System.out.println("│  " + msg.getMessage("member.back") + "                         │");
            System.out.println("└─────────────────────────────────────────────────────┘");

            int choice = getIntInput(msg.getMessage("menu.choice"));

            switch (choice) {
                case 1: addMember(); break;
                case 2: updateMember(); break;
                case 3: deleteMember(); break;
                case 4: searchMembers(); break;
                case 5: viewAllMembers(); break;
                case 0: return;
                default: System.out.println(msg.getMessage("common.invalidInput"));
            }
        }
    }

    private void borrowMenu() {
        while (true) {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────────────┐");
            System.out.println("│            " + msg.getMessage("borrow.menu") + "          │");
            System.out.println("├─────────────────────────────────────────────────────┤");
            System.out.println("│  " + msg.getMessage("borrow.borrow") + "                     │");
            System.out.println("│  " + msg.getMessage("borrow.return") + "                     │");
            System.out.println("│  " + msg.getMessage("borrow.history") + "                    │");
            System.out.println("│  " + msg.getMessage("borrow.active") + "                     │");
            System.out.println("│  " + msg.getMessage("borrow.overdue") + "                    │");
            System.out.println("│  " + msg.getMessage("borrow.back") + "                         │");
            System.out.println("└─────────────────────────────────────────────────────┘");

            int choice = getIntInput(msg.getMessage("menu.choice"));

            switch (choice) {
                case 1: borrowBook(); break;
                case 2: returnBook(); break;
                case 3: viewBorrowHistory(); break;
                case 4: viewActiveBorrows(); break;
                case 5: viewOverdueBooks(); break;
                case 0: return;
                default: System.out.println(msg.getMessage("common.invalidInput"));
            }
        }
    }

    private void settingsMenu() {
        while (true) {
            System.out.println();
            System.out.println("┌─────────────────────────────────────────────────────┐");
            System.out.println("│              " + msg.getMessage("settings.menu") + "              │");
            System.out.println("├─────────────────────────────────────────────────────┤");
            System.out.println("│  " + msg.getMessage("settings.language") + "                          │");
            System.out.println("│  " + msg.getMessage("settings.back") + "                         │");
            System.out.println("└─────────────────────────────────────────────────────┘");
            System.out.println();
            System.out.println("    " + msg.getMessage("settings.language.current") + " " + msg.getCurrentLanguage().getDisplayName());

            int choice = getIntInput(msg.getMessage("menu.choice"));

            switch (choice) {
                case 1:
                    changeLanguage();
                    break;
                case 0:
                    return;
                default:
                    System.out.println(msg.getMessage("common.invalidInput"));
            }
        }
    }

    private void changeLanguage() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│           " + msg.getMessage("settings.language.select") + "           │");
        System.out.println("├─────────────────────────────────────────────────────┤");
        System.out.println("│  " + msg.getMessage("settings.language.fa") + "                     │");
        System.out.println("│  " + msg.getMessage("settings.language.en") + "                         │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        int choice = getIntInput(msg.getMessage("menu.choice"));

        switch (choice) {
            case 1:
                msg.setLanguage(Language.FA);
                System.out.println();
                System.out.println("╔══════════════════════════════════════════════════════╗");
                System.out.println("║      " + msg.getMessage("settings.language.changed") + "        ║");
                System.out.println("╚══════════════════════════════════════════════════════╝");
                break;
            case 2:
                msg.setLanguage(Language.EN);
                System.out.println();
                System.out.println("╔══════════════════════════════════════════════════════╗");
                System.out.println("║      " + msg.getMessage("settings.language.changed") + "        ║");
                System.out.println("╚══════════════════════════════════════════════════════╝");
                break;
            default:
                System.out.println(msg.getMessage("common.invalidInput"));
        }
    }

    private void addBook() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("book.add.title") + "            │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        String title = getStringInput("    " + msg.getMessage("book.title"));
        String author = getStringInput("    " + msg.getMessage("book.author"));
        String isbn = getStringInput("    " + msg.getMessage("book.isbn"));
        Integer publishedYear = getIntInputOrNull("    " + msg.getMessage("book.year"));
        int quantity = getIntInput("    " + msg.getMessage("book.quantity"));

        try {
            Book book = new Book(title, author, isbn, publishedYear, quantity, quantity);
            bookService.addBook(book);
            System.out.println();
            System.out.println("    ✓ " + msg.getMessage("book.add.success") + book.getId());
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("common.error") + e.getMessage());
        }
    }

    private void updateBook() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("book.update.title") + "           │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        Long id = getLongInput("    " + msg.getMessage("book.update.id"));

        try {
            Book existingBook = bookService.getBookById(id);
            System.out.println("    " + msg.getMessage("book.update.current"));
            System.out.println("    " + existingBook);

            String title = getStringInput("    " + msg.getMessage("book.update.newTitle"));
            String author = getStringInput("    " + msg.getMessage("book.update.newAuthor"));
            String isbn = getStringInput("    " + msg.getMessage("book.update.newIsbn"));
            Integer publishedYear = getIntInputOrNull("    " + msg.getMessage("book.update.newYear"));
            Integer quantity = getIntInputOrNull("    " + msg.getMessage("book.update.newQuantity"));

            if (!title.isEmpty()) existingBook.setTitle(title);
            if (!author.isEmpty()) existingBook.setAuthor(author);
            if (!isbn.isEmpty()) existingBook.setIsbn(isbn);
            if (publishedYear != null) existingBook.setPublishedYear(publishedYear);
            if (quantity != null) existingBook.setQuantity(quantity);

            bookService.updateBook(existingBook);
            System.out.println();
            System.out.println("    ✓ " + msg.getMessage("book.update.success"));
        } catch (BookNotFoundException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("book.notFound") + id);
        }
    }

    private void deleteBook() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("book.delete.title") + "           │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        Long id = getLongInput("    " + msg.getMessage("book.delete.id"));

        try {
            bookService.deleteBook(id);
            System.out.println();
            System.out.println("    ✓ " + msg.getMessage("book.delete.success"));
        } catch (BookNotFoundException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("book.notFound") + id);
        }
    }

    private void searchBooks() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("book.search.title") + "            │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        String keyword = getStringInput("    " + msg.getMessage("book.search.keyword"));

        List<Book> books = bookService.searchBooks(keyword);
        if (books.isEmpty()) {
            System.out.println();
            System.out.println("    " + msg.getMessage("book.search.notFound"));
        } else {
            System.out.println();
            System.out.println("    " + msg.getMessage("book.search.results") + " " + books.size());
            System.out.println("    ─────────────────────────────────────────────────");
            books.forEach(b -> System.out.println("    " + b));
        }
    }

    private void viewAllBooks() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│           " + msg.getMessage("book.viewAll.title") + "              │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            System.out.println();
            System.out.println("    " + msg.getMessage("book.viewAll.empty"));
        } else {
            System.out.println();
            System.out.println("    " + msg.getMessage("book.viewAll.total") + " " + books.size());
            System.out.println("    ─────────────────────────────────────────────────");
            books.forEach(b -> System.out.println("    " + b));
        }
    }

    private void viewBookByIsbn() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│         " + msg.getMessage("book.viewByIsbn.title") + "            │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        String isbn = getStringInput("    " + msg.getMessage("book.viewByIsbn.isbn"));

        try {
            Book book = bookService.getBookByIsbn(isbn);
            System.out.println();
            System.out.println("    " + book);
        } catch (BookNotFoundException e) {
            System.out.println();
            System.out.println("    ✗ " + e.getMessage());
        }
    }

    private void addMember() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│         " + msg.getMessage("member.add.title") + "              │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        String name = getStringInput("    " + msg.getMessage("member.name"));
        String nationalCode = getStringInput("    " + msg.getMessage("member.nationalCode"));
        String phoneNumber = getStringInput("    " + msg.getMessage("member.phone"));
        LocalDate joinDate = getDateInput("    " + msg.getMessage("member.joinDate"));

        try {
            Member member = new Member(name, nationalCode, phoneNumber, joinDate);
            memberService.addMember(member);
            System.out.println();
            System.out.println("    ✓ " + msg.getMessage("member.add.success") + member.getId());
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("common.error") + e.getMessage());
        }
    }

    private void updateMember() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│         " + msg.getMessage("member.update.title") + "              │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        Long id = getLongInput("    " + msg.getMessage("member.update.id"));

        try {
            Member existingMember = memberService.getMemberById(id);
            System.out.println("    " + msg.getMessage("member.update.current"));
            System.out.println("    " + existingMember);

            String name = getStringInput("    " + msg.getMessage("member.update.newName"));
            String nationalCode = getStringInput("    " + msg.getMessage("member.update.newNationalCode"));
            String phoneNumber = getStringInput("    " + msg.getMessage("member.update.newPhone"));
            LocalDate joinDate = getDateInputOrNull("    " + msg.getMessage("member.update.newJoinDate"));

            if (!name.isEmpty()) existingMember.setName(name);
            if (!nationalCode.isEmpty()) existingMember.setNationalCode(nationalCode);
            if (!phoneNumber.isEmpty()) existingMember.setPhoneNumber(phoneNumber);
            if (joinDate != null) existingMember.setJoinDate(joinDate);

            memberService.updateMember(existingMember);
            System.out.println();
            System.out.println("    ✓ " + msg.getMessage("member.update.success"));
        } catch (MemberNotFoundException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("member.notFound") + id);
        }
    }

    private void deleteMember() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│         " + msg.getMessage("member.delete.title") + "              │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        Long id = getLongInput("    " + msg.getMessage("member.delete.id"));

        try {
            memberService.deleteMember(id);
            System.out.println();
            System.out.println("    ✓ " + msg.getMessage("member.delete.success"));
        } catch (MemberNotFoundException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("member.notFound") + id);
        }
    }

    private void searchMembers() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│         " + msg.getMessage("member.search.title") + "              │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        String keyword = getStringInput("    " + msg.getMessage("member.search.keyword"));

        List<Member> members = memberService.searchMembers(keyword);
        if (members.isEmpty()) {
            System.out.println();
            System.out.println("    " + msg.getMessage("member.search.notFound"));
        } else {
            System.out.println();
            System.out.println("    " + msg.getMessage("member.search.results") + " " + members.size());
            System.out.println("    ─────────────────────────────────────────────────");
            members.forEach(m -> System.out.println("    " + m));
        }
    }

    private void viewAllMembers() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("member.viewAll.title") + "              │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        List<Member> members = memberService.getAllMembers();
        if (members.isEmpty()) {
            System.out.println();
            System.out.println("    " + msg.getMessage("member.viewAll.empty"));
        } else {
            System.out.println();
            System.out.println("    " + msg.getMessage("member.viewAll.total") + " " + members.size());
            System.out.println("    ─────────────────────────────────────────────────");
            members.forEach(m -> System.out.println("    " + m));
        }
    }

    private void borrowBook() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("borrow.borrow.title") + "             │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        Long bookId = getLongInput("    " + msg.getMessage("borrow.borrow.bookId"));
        Long memberId = getLongInput("    " + msg.getMessage("borrow.borrow.memberId"));

        try {
            BorrowRecord record = borrowService.borrowBook(bookId, memberId);
            System.out.println();
            System.out.println("    ✓ " + msg.getMessage("borrow.borrow.success") + record.getId());
        } catch (BookNotFoundException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("borrow.borrow.bookNotFound"));
        } catch (MemberNotFoundException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("borrow.borrow.memberNotFound"));
        } catch (BookNotAvailableException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("borrow.borrow.notAvailable"));
        } catch (BorrowLimitExceededException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("borrow.borrow.limitExceeded"));
        }
    }

    private void returnBook() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("borrow.return.title") + "             │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        Long borrowRecordId = getLongInput("    " + msg.getMessage("borrow.return.id"));

        try {
            BorrowRecord record = borrowService.returnBook(borrowRecordId);
            System.out.println();
            System.out.println("    ✓ " + msg.getMessage("borrow.return.success"));
            if (record.getStatus().name().equals("OVERDUE")) {
                System.out.println("    ⚠ " + msg.getMessage("borrow.return.delay"));
            }
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("common.error") + e.getMessage());
        }
    }

    private void viewBorrowHistory() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("borrow.history.title") + "             │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        Long memberId = getLongInput("    " + msg.getMessage("borrow.history.memberId"));

        try {
            List<BorrowRecord> records = borrowService.getBorrowHistoryByMemberId(memberId);
            if (records.isEmpty()) {
                System.out.println();
                System.out.println("    " + msg.getMessage("borrow.history.notFound"));
            } else {
                System.out.println();
                System.out.println("    " + msg.getMessage("borrow.history.results") + " " + records.size());
                System.out.println("    ─────────────────────────────────────────────────");
                records.forEach(r -> System.out.println("    " + r));
            }
        } catch (MemberNotFoundException e) {
            System.out.println();
            System.out.println("    ✗ " + msg.getMessage("member.notFound") + memberId);
        }
    }

    private void viewActiveBorrows() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("borrow.active.title") + "              │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        List<BorrowRecord> records = borrowService.getActiveBorrows();
        if (records.isEmpty()) {
            System.out.println();
            System.out.println("    " + msg.getMessage("borrow.active.empty"));
        } else {
            System.out.println();
            System.out.println("    " + msg.getMessage("borrow.active.total") + " " + records.size());
            System.out.println("    ─────────────────────────────────────────────────");
            records.forEach(r -> System.out.println("    " + r));
        }
    }

    private void viewOverdueBooks() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│          " + msg.getMessage("borrow.overdue.title") + "              │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        List<BorrowRecord> records = borrowService.getOverdueBooks();
        if (records.isEmpty()) {
            System.out.println();
            System.out.println("    " + msg.getMessage("borrow.overdue.empty"));
        } else {
            System.out.println();
            System.out.println("    " + msg.getMessage("borrow.overdue.total") + " " + records.size());
            System.out.println("    ─────────────────────────────────────────────────");
            records.forEach(r -> System.out.println("    " + r));
        }
    }

    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("    " + msg.getMessage("common.invalidInput"));
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
                System.out.println("    " + msg.getMessage("common.invalidInput"));
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
            System.out.println("    " + msg.getMessage("common.invalidInput"));
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
                System.out.println("    " + msg.getMessage("common.invalidInput"));
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
            System.out.println("    " + msg.getMessage("common.invalidInput"));
            return null;
        }
    }
}