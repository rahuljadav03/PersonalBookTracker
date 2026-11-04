package com.example.booktracker;

public class BookValidator {

    /**
     * Validates a book object against all business rules.
     * @param book The book to validate.
     * @throws IllegalArgumentException if any rule is violated.
     */
    public static void validate(Book book) throws IllegalArgumentException {
        // 1. Title Validation
        if (book.getTitle() == null || book.getTitle().trim().length() < 2 || book.getTitle().trim().length() > 100) {
            throw new IllegalArgumentException("Title must be between 2 and 100 characters.");
        }

        // 2. Author Validation
        if (book.getAuthor() == null || book.getAuthor().trim().length() < 2 || book.getAuthor().trim().length() > 60) {
            throw new IllegalArgumentException("Author must be between 2 and 60 characters.");
        }
        // Regex allows letters, spaces, dots, apostrophes, and hyphens
        if (!book.getAuthor().matches("^[a-zA-Z\\s\\.'\\-]+$")) {
            throw new IllegalArgumentException("Author contains invalid characters.");
        }

        // 3. Category Validation
        if (!Constants.CATEGORIES.contains(book.getCategory())) {
            throw new IllegalArgumentException("Invalid category selected.");
        }

        // 4. Total Pages Validation
        if (book.getTotalPages() < 1 || book.getTotalPages() > 10000) {
            throw new IllegalArgumentException("Total pages must be between 1 and 10,000.");
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
        if (book.getDescription() != null && book.getDescription().length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters.");
        }
    }
}