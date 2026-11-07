package com.example.booktracker.utils;

import com.example.booktracker.model.Book;

import java.util.HashMap;
import java.util.Map;

public class BookValidator {

    public static ValidationResult validate(Book book) {
        Map<String, String> errors = new HashMap<>();

        if (book == null) {
            errors.put("book", "Book object cannot be null.");
            return ValidationResult.failure(errors);
        }

        // 1. Trim & Normalize input strings
        if (book.getTitle() != null) {
            book.setTitle(normalize(book.getTitle()));
        }
        if (book.getAuthor() != null) {
            book.setAuthor(normalize(book.getAuthor()));
        }
        if (book.getCategory() != null) {
            book.setCategory(normalize(book.getCategory()));
        }
        if (book.getStatus() != null) {
            book.setStatus(book.getStatus().trim());
        }
        if (book.getDescription() != null) {
            book.setDescription(normalize(book.getDescription()));
        }

        // 2. Title Validation
        String title = book.getTitle();
        if (title == null || title.isEmpty()) {
            errors.put("title", "Title is required.");
        } else if (title.length() < Constants.TITLE_MIN_LENGTH || title.length() > Constants.TITLE_MAX_LENGTH) {
            errors.put("title", "Title must be between " + Constants.TITLE_MIN_LENGTH + " and " + Constants.TITLE_MAX_LENGTH + " characters.");
        }

        // 3. Author Validation
        String author = book.getAuthor();
        if (author == null || author.isEmpty()) {
            errors.put("author", "Author is required.");
        } else if (author.length() < Constants.AUTHOR_MIN_LENGTH || author.length() > Constants.AUTHOR_MAX_LENGTH) {
            errors.put("author", "Author must be between " + Constants.AUTHOR_MIN_LENGTH + " and " + Constants.AUTHOR_MAX_LENGTH + " characters.");
        } else if (!author.matches("^[a-zA-Z\\s\\.'\\-]+$")) {
            errors.put("author", "Author contains invalid characters.");
        }

        // 4. Category Validation
        String category = book.getCategory();
        if (category == null || category.isEmpty()) {
            errors.put("category", "Category is required.");
        } else if (!Constants.CATEGORIES.contains(category)) {
            errors.put("category", "Invalid category selected.");
        }

        // 5. Total Pages Validation
        int totalPages = book.getTotalPages();
        if (totalPages < Constants.MIN_PAGES || totalPages > Constants.MAX_PAGES) {
            errors.put("totalPages", "Total pages must be between " + Constants.MIN_PAGES + " and " + Constants.MAX_PAGES + ".");
        }

        // 6. Pages Read Validation
        int pagesRead = book.getPagesRead();
        if (pagesRead < 0 || pagesRead > totalPages) {
            errors.put("pagesRead", "Pages read cannot be less than 0 or greater than total pages.");
        }

        // 7. Status & Cross-Field Validation
        String status = book.getStatus();
        if (status == null || status.isEmpty()) {
            errors.put("status", "Status is required.");
        } else if (!Constants.STATUS_WANT_TO_READ.equals(status)
                && !Constants.STATUS_READING.equals(status)
                && !Constants.STATUS_COMPLETED.equals(status)) {
            errors.put("status", "Invalid status selected.");
        } else {
            // Cross-field rules for pages_read vs status vs total_pages
            if (Constants.STATUS_WANT_TO_READ.equals(status)) {
                if (pagesRead != 0) {
                    errors.put("pagesRead", "'Want to Read' books must have 0 pages read.");
                }
            } else if (Constants.STATUS_COMPLETED.equals(status)) {
                if (pagesRead != totalPages) {
                    errors.put("pagesRead", "'Completed' books must have pages read equal to total pages.");
                }
            }
        }

        // 8. Description Validation
        String description = book.getDescription();
        if (description != null && description.length() > Constants.DESCRIPTION_MAX_LENGTH) {
            errors.put("description", "Description cannot exceed " + Constants.DESCRIPTION_MAX_LENGTH + " characters.");
        }

        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }

    private static String normalize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }
}
