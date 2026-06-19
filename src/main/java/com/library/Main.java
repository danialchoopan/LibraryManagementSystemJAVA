package com.library;

import com.library.repository.BookRepositoryImpl;
import com.library.repository.BorrowRecordRepositoryImpl;
import com.library.repository.MemberRepositoryImpl;
import com.library.service.BookService;
import com.library.service.BookServiceImpl;
import com.library.service.BorrowService;
import com.library.service.BorrowServiceImpl;
import com.library.service.MemberService;
import com.library.service.MemberServiceImpl;
import com.library.ui.ConsoleUI;
import com.library.ui.MainGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Library Management System starting...");

        BookService bookService = new BookServiceImpl(new BookRepositoryImpl());
        MemberService memberService = new MemberServiceImpl(new MemberRepositoryImpl());
        BorrowService borrowService = new BorrowServiceImpl(new BorrowRecordRepositoryImpl(),
                new BookRepositoryImpl(), new MemberRepositoryImpl());

        if (args.length > 0 && "--console".equals(args[0])) {
            ConsoleUI ui = new ConsoleUI(bookService, memberService, borrowService);
            ui.start();
        } else {
            MainGUI.launch(bookService, memberService, borrowService);
        }
    }
}
