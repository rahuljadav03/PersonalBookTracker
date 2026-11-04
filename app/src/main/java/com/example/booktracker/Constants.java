package com.example.booktracker;

import java.util.Arrays;
import java.util.List;

public class Constants {
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
}