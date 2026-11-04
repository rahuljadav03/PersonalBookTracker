package com.example.booktracker.utils;

import com.example.booktracker.model.Book;

public class BookValidator {
    public static void validate(Book book) throws IllegalArgumentException {
        // 1. Title Validation
        if (book.getTitle() == null || book.getTitle().trim().length() < Constants.TITLE_MIN_LENGTH || book.getTitle().trim().length() > Constants.TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Title must be between " + Constants.TITLE_MIN_LENGTH + " and " + Constants.TITLE_MAX_LENGTH + " characters.");
        }

        // 2. Author Validation
        if (book.getAuthor() == null || book.getAuthor().trim().length() < Constants.AUTHOR_MIN_LENGTH || book.getAuthor().trim().length() > Constants.AUTHOR_MAX_LENGTH) {
            throw new IllegalArgumentException("Author must be between " + Constants.AUTHOR_MIN_LENGTH + " and " + Constants.AUTHOR_MAX_LENGTH + " characters.");
        }
        if (!book.getAuthor().matches("^[a-zA-Z\\s\\.'\\-]+$")) {
            throw new IllegalArgumentException("Author contains invalid characters.");
        }

        // 3. Category Validation
        if (!Constants.CATEGORIES.contains(book.getCategory())) {
            throw new IllegalArgumentException("Invalid category selected.");
        }

        // 4. Total Pages Validation
        if (book.getTotalPages() < Constants.MIN_PAGES || book.getTotalPages() > Constants.MAX_PAGES) {
            throw new IllegalArgumentException("Total pages must be between " + Constants.MIN_PAGES + " and " + Constants.MAX_PAGES + ".");
        }

        // 5. Pages Read Validation
        if (book.getPagesRead() < 0 || book.getPagesRead() > book.getTotalPages()) {
            throw new IllegalArgumentException("Pages read cannot be less than 0 or greater than total pages.");
        }

        // 6. Status and Pages Read Logical Tie
        if (Constants.STATUS_WANT_TO_READ.equals(book.getStatus()) && book.getPagesRead() != 0) {
            throw new IllegalArgumentException("'Want to Read' books must have 0 pages read.");
        }
        if (Constants.STATUS_COMPLETED.equals(book.getStatus()) && book.getPagesRead() != book.getTotalPages()) {
            throw new IllegalArgumentException("'Completed' books must have pages read equal to total pages.");
        }

        // 7. Description Validation
        if (book.getDescription() != null && book.getDescription().length() > Constants.DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + Constants.DESCRIPTION_MAX_LENGTH + " characters.");
        }
    }
}