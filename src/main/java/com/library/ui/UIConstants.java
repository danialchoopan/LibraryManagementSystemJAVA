package com.library.ui;

import java.time.format.DateTimeFormatter;

public class UIConstants {
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final int MAX_BORROW_DAYS = 14;
    public static final int MAX_BORROW_LIMIT = 3;
}
