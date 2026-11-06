package com.example.booktracker.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {
    // Intent Extra Keys
    public static final String EXTRA_BOOK_ID = "book_id";
    public static final String EXTRA_EDIT_MODE = "edit_mode";

    // Book Statuses
    public static final String STATUS_WANT_TO_READ = "WANT_TO_READ";
    public static final String STATUS_READING = "READING";
    public static final String STATUS_COMPLETED = "COMPLETED";

    // Allowed Categories
    public static final List<String> CATEGORIES = Arrays.asList(
            "Fiction",
            "Non-Fiction",
            "Sci-Fi",
            "Fantasy",
            "Mystery",
            "Biography",
            "Romance",
            "Other"
    );

    // Validation Limits
    public static final int TITLE_MIN_LENGTH = 2;
    public static final int TITLE_MAX_LENGTH = 100;

    public static final int AUTHOR_MIN_LENGTH = 2;
    public static final int AUTHOR_MAX_LENGTH = 60;

    public static final int MIN_PAGES = 1;
    public static final int MAX_PAGES = 10000;

    public static final int DESCRIPTION_MAX_LENGTH = 500;
}
