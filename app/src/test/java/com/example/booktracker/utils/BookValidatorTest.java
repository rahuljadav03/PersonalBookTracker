package com.example.booktracker.utils;

import com.example.booktracker.model.Book;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BookValidatorTest {

    private Book createValidBook() {
        Book book = new Book();
        book.setTitle("The Great Gatsby");
        book.setAuthor("F. Scott Fitzgerald");
        book.setCategory("Fiction");
        book.setTotalPages(180);
        book.setPagesRead(50);
        book.setStatus(Constants.STATUS_READING);
        book.setDescription("A classic modern novel.");
        return book;
    }

    private String createStringOfLength(int length) {
        return String.join("", Collections.nCopies(length, "A"));
    }

    // 1. Valid Book Test
    @Test
    public void testValidBook() {
        Book book = createValidBook();
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Valid book should pass validation", result.isValid());
        assertTrue("Errors map should be empty for valid book", result.getErrors().isEmpty());
    }

    // 2. Title Boundary and Edge Cases
    @Test
    public void testTitle_MinLength_Boundary2() {
        Book book = createValidBook();
        book.setTitle("AB"); // min length = 2
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Title of 2 characters should be valid", result.isValid());
    }

    @Test
    public void testTitle_MaxLength_Boundary100() {
        Book book = createValidBook();
        book.setTitle(createStringOfLength(100)); // max length = 100
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Title of 100 characters should be valid", result.isValid());
    }

    @Test
    public void testTitle_MinMinus1_Boundary1() {
        Book book = createValidBook();
        book.setTitle("A"); // min - 1 = 1
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Title of 1 character should be invalid", result.isValid());
        assertTrue("Should contain error for title", result.hasError("title"));
    }

    @Test
    public void testTitle_MaxPlus1_Boundary101() {
        Book book = createValidBook();
        book.setTitle(createStringOfLength(101)); // max + 1 = 101
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Title of 101 characters should be invalid", result.isValid());
        assertTrue("Should contain error for title", result.hasError("title"));
    }

    @Test
    public void testTitle_Empty() {
        Book book = createValidBook();
        book.setTitle("");
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Empty title should be invalid", result.isValid());
        assertTrue("Should contain error for title", result.hasError("title"));
    }

    @Test
    public void testTitle_WhitespaceOnly() {
        Book book = createValidBook();
        book.setTitle("     ");
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Whitespace-only title should be invalid", result.isValid());
        assertTrue("Should contain error for title", result.hasError("title"));
    }

    @Test
    public void testTitle_Null() {
        Book book = createValidBook();
        book.setTitle(null);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Null title should be invalid", result.isValid());
        assertTrue("Should contain error for title", result.hasError("title"));
    }

    // 3. Author Boundary, Edge and Character Cases
    @Test
    public void testAuthor_MinLength_Boundary2() {
        Book book = createValidBook();
        book.setAuthor("Li"); // min length = 2
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Author of 2 characters should be valid", result.isValid());
    }

    @Test
    public void testAuthor_MaxLength_Boundary60() {
        Book book = createValidBook();
        book.setAuthor(createStringOfLength(60)); // max length = 60
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Author of 60 characters should be valid", result.isValid());
    }

    @Test
    public void testAuthor_MinMinus1_Boundary1() {
        Book book = createValidBook();
        book.setAuthor("A"); // min - 1 = 1
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Author of 1 character should be invalid", result.isValid());
        assertTrue("Should contain error for author", result.hasError("author"));
    }

    @Test
    public void testAuthor_MaxPlus1_Boundary61() {
        Book book = createValidBook();
        book.setAuthor(createStringOfLength(61)); // max + 1 = 61
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Author of 61 characters should be invalid", result.isValid());
        assertTrue("Should contain error for author", result.hasError("author"));
    }

    @Test
    public void testAuthor_Empty() {
        Book book = createValidBook();
        book.setAuthor("");
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Empty author should be invalid", result.isValid());
        assertTrue("Should contain error for author", result.hasError("author"));
    }

    @Test
    public void testAuthor_WhitespaceOnly() {
        Book book = createValidBook();
        book.setAuthor("    ");
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Whitespace-only author should be invalid", result.isValid());
        assertTrue("Should contain error for author", result.hasError("author"));
    }

    @Test
    public void testAuthor_Null() {
        Book book = createValidBook();
        book.setAuthor(null);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Null author should be invalid", result.isValid());
        assertTrue("Should contain error for author", result.hasError("author"));
    }

    @Test
    public void testAuthor_ValidCharacters() {
        Book book = createValidBook();
        book.setAuthor("Mary O'Connor-Smith Jr.");
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Author with valid special characters (dots, hyphens, apostrophes) should be valid", result.isValid());
    }

    @Test
    public void testAuthor_InvalidCharacters_Digits() {
        Book book = createValidBook();
        book.setAuthor("Author123");
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Author with digits should be invalid", result.isValid());
        assertEquals("Author contains invalid characters.", result.getError("author"));
    }

    @Test
    public void testAuthor_InvalidCharacters_Symbols() {
        Book book = createValidBook();
        book.setAuthor("Author@Work");
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Author with @ symbol should be invalid", result.isValid());
        assertEquals("Author contains invalid characters.", result.getError("author"));
    }

    // 4. Category Rules
    @Test
    public void testCategory_ValidCategories() {
        Book book = createValidBook();
        for (String category : Constants.CATEGORIES) {
            book.setCategory(category);
            ValidationResult result = BookValidator.validate(book);
            assertTrue("Category " + category + " should be valid", result.isValid());
        }
    }

    @Test
    public void testCategory_InvalidCategory() {
        Book book = createValidBook();
        book.setCategory("Cookbook");
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Unlisted category should be invalid", result.isValid());
        assertTrue("Should contain error for category", result.hasError("category"));
    }

    @Test
    public void testCategory_Null() {
        Book book = createValidBook();
        book.setCategory(null);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Null category should be invalid", result.isValid());
        assertTrue("Should contain error for category", result.hasError("category"));
    }

    // 5. Total Pages Boundary Values
    @Test
    public void testTotalPages_MinBoundary1() {
        Book book = createValidBook();
        book.setTotalPages(1);
        book.setPagesRead(0);
        book.setStatus(Constants.STATUS_WANT_TO_READ);
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Total pages = 1 should be valid", result.isValid());
    }

    @Test
    public void testTotalPages_MaxBoundary10000() {
        Book book = createValidBook();
        book.setTotalPages(10000);
        book.setPagesRead(5000);
        book.setStatus(Constants.STATUS_READING);
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Total pages = 10000 should be valid", result.isValid());
    }

    @Test
    public void testTotalPages_MinMinus1_Boundary0() {
        Book book = createValidBook();
        book.setTotalPages(0);
        book.setPagesRead(0);
        book.setStatus(Constants.STATUS_WANT_TO_READ);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Total pages = 0 should be invalid", result.isValid());
        assertTrue("Should contain error for totalPages", result.hasError("totalPages"));
    }

    @Test
    public void testTotalPages_MaxPlus1_Boundary10001() {
        Book book = createValidBook();
        book.setTotalPages(10001);
        book.setPagesRead(500);
        book.setStatus(Constants.STATUS_READING);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Total pages = 10001 should be invalid", result.isValid());
        assertTrue("Should contain error for totalPages", result.hasError("totalPages"));
    }

    @Test
    public void testTotalPages_Negative() {
        Book book = createValidBook();
        book.setTotalPages(-50);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Negative total pages should be invalid", result.isValid());
        assertTrue("Should contain error for totalPages", result.hasError("totalPages"));
    }

    // 6. Pages Read Boundary Values
    @Test
    public void testPagesRead_MinBoundary0() {
        Book book = createValidBook();
        book.setTotalPages(100);
        book.setPagesRead(0);
        book.setStatus(Constants.STATUS_WANT_TO_READ);
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Pages read = 0 should be valid for WANT_TO_READ", result.isValid());
    }

    @Test
    public void testPagesRead_MaxBoundaryTotalPages() {
        Book book = createValidBook();
        book.setTotalPages(100);
        book.setPagesRead(100);
        book.setStatus(Constants.STATUS_COMPLETED);
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Pages read = totalPages should be valid for COMPLETED", result.isValid());
    }

    @Test
    public void testPagesRead_MinMinus1_Negative() {
        Book book = createValidBook();
        book.setPagesRead(-1);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Negative pages read should be invalid", result.isValid());
        assertTrue("Should contain error for pagesRead", result.hasError("pagesRead"));
    }

    @Test
    public void testPagesRead_MaxPlus1_ExceedsTotalPages() {
        Book book = createValidBook();
        book.setTotalPages(100);
        book.setPagesRead(101);
        book.setStatus(Constants.STATUS_READING);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Pages read > totalPages should be invalid", result.isValid());
        assertTrue("Should contain error for pagesRead", result.hasError("pagesRead"));
    }

    // 7. Status Rules and Cross-Field Logic
    @Test
    public void testStatus_WantToRead_ValidPagesRead0() {
        Book book = createValidBook();
        book.setStatus(Constants.STATUS_WANT_TO_READ);
        book.setPagesRead(0);
        ValidationResult result = BookValidator.validate(book);
        assertTrue("WANT_TO_READ status with 0 pages read should be valid", result.isValid());
    }

    @Test
    public void testStatus_WantToRead_InvalidPagesReadGreaterThan0() {
        Book book = createValidBook();
        book.setStatus(Constants.STATUS_WANT_TO_READ);
        book.setPagesRead(10);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("WANT_TO_READ status with >0 pages read should be invalid", result.isValid());
        assertEquals("'Want to Read' books must have 0 pages read.", result.getError("pagesRead"));
    }

    @Test
    public void testStatus_Completed_ValidPagesReadEqualsTotalPages() {
        Book book = createValidBook();
        book.setStatus(Constants.STATUS_COMPLETED);
        book.setTotalPages(250);
        book.setPagesRead(250);
        ValidationResult result = BookValidator.validate(book);
        assertTrue("COMPLETED status with pages read == total pages should be valid", result.isValid());
    }

    @Test
    public void testStatus_Completed_InvalidPagesReadLessThanTotalPages() {
        Book book = createValidBook();
        book.setStatus(Constants.STATUS_COMPLETED);
        book.setTotalPages(250);
        book.setPagesRead(249);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("COMPLETED status with pages read < total pages should be invalid", result.isValid());
        assertEquals("'Completed' books must have pages read equal to total pages.", result.getError("pagesRead"));
    }

    @Test
    public void testStatus_Reading_ValidPagesRead() {
        Book book = createValidBook();
        book.setStatus(Constants.STATUS_READING);
        book.setTotalPages(200);
        book.setPagesRead(100);
        ValidationResult result = BookValidator.validate(book);
        assertTrue("READING status with 0 < pagesRead < totalPages should be valid", result.isValid());
    }

    @Test
    public void testStatus_InvalidStatus() {
        Book book = createValidBook();
        book.setStatus("IN_PROGRESS");
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Invalid status string should fail validation", result.isValid());
        assertTrue("Should contain error for status", result.hasError("status"));
    }

    @Test
    public void testStatus_NullStatus() {
        Book book = createValidBook();
        book.setStatus(null);
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Null status should fail validation", result.isValid());
        assertTrue("Should contain error for status", result.hasError("status"));
    }

    // 8. Description Boundary Values
    @Test
    public void testDescription_NullDescription() {
        Book book = createValidBook();
        book.setDescription(null);
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Null description should be valid", result.isValid());
    }

    @Test
    public void testDescription_EmptyDescription() {
        Book book = createValidBook();
        book.setDescription("");
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Empty description should be valid", result.isValid());
    }

    @Test
    public void testDescription_MaxLength500() {
        Book book = createValidBook();
        book.setDescription(createStringOfLength(500));
        ValidationResult result = BookValidator.validate(book);
        assertTrue("Description of 500 characters should be valid", result.isValid());
    }

    @Test
    public void testDescription_MaxPlus1_501() {
        Book book = createValidBook();
        book.setDescription(createStringOfLength(501));
        ValidationResult result = BookValidator.validate(book);
        assertFalse("Description of 501 characters should be invalid", result.isValid());
        assertTrue("Should contain error for description", result.hasError("description"));
    }

    // 9. Input Trimming and Normalization
    @Test
    public void testTrimAndNormalizeInput() {
        Book book = createValidBook();
        book.setTitle("   Clean    Code   ");
        book.setAuthor("  Robert   C.   Martin  ");
        book.setDescription("   Great   book   on   clean    code.   ");

        ValidationResult result = BookValidator.validate(book);
        assertTrue("Book with whitespace to normalize should be valid", result.isValid());

        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthor());
        assertEquals("Great book on clean code.", book.getDescription());
    }

    // 10. Null Book and Multiple Errors
    @Test
    public void testNullBook() {
        ValidationResult result = BookValidator.validate(null);
        assertFalse("Null book object should fail validation", result.isValid());
        assertTrue("Should contain error for book", result.hasError("book"));
    }

    @Test
    public void testMultipleErrors() {
        Book book = new Book();
        book.setTitle("A"); // Invalid: length 1
        book.setAuthor("InvalidAuthor123"); // Invalid: digits
        book.setCategory("UnknownCategory"); // Invalid category
        book.setTotalPages(0); // Invalid total pages
        book.setStatus("UNKNOWN_STATUS"); // Invalid status

        ValidationResult result = BookValidator.validate(book);
        assertFalse("Book with multiple errors should fail validation", result.isValid());
        assertNotNull(result.getErrors());
        assertTrue("Should contain error for title", result.hasError("title"));
        assertTrue("Should contain error for author", result.hasError("author"));
        assertTrue("Should contain error for category", result.hasError("category"));
        assertTrue("Should contain error for totalPages", result.hasError("totalPages"));
        assertTrue("Should contain error for status", result.hasError("status"));
    }
}
