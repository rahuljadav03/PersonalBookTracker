package com.example.booktracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.booktracker.model.Book;
import com.example.booktracker.utils.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {

    private DatabaseHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("BookTracker.db");
        dbHelper = new DatabaseHelper(context);
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        context.deleteDatabase("BookTracker.db");
    }

    private Book createSampleBook(String title, String author, String status, int totalPages, int pagesRead) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory("Fiction");
        book.setTotalPages(totalPages);
        book.setPagesRead(pagesRead);
        book.setStatus(status);
        book.setDescription("Test description for " + title);
        return book;
    }

    // 1. Basic CRUD Operations
    @Test
    public void testInsertAndGetBookById() {
        Book book = createSampleBook("1984", "George Orwell", Constants.STATUS_READING, 328, 100);
        long id = dbHelper.insertBook(book);
        assertTrue("Inserted book ID should be greater than 0", id > 0);

        Book fetched = dbHelper.getBookById(id);
        assertNotNull("Fetched book should not be null", fetched);
        assertEquals("1984", fetched.getTitle());
        assertEquals("George Orwell", fetched.getAuthor());
        assertEquals("Fiction", fetched.getCategory());
        assertEquals(328, fetched.getTotalPages());
        assertEquals(100, fetched.getPagesRead());
        assertEquals(Constants.STATUS_READING, fetched.getStatus());
        assertTrue("Created timestamp should be > 0", fetched.getCreatedAt() > 0);
        assertTrue("Updated timestamp should be > 0", fetched.getUpdatedAt() > 0);
    }

    @Test
    public void testGetAllBooks() {
        Book b1 = createSampleBook("Book One", "Author One", Constants.STATUS_WANT_TO_READ, 200, 0);
        Book b2 = createSampleBook("Book Two", "Author Two", Constants.STATUS_COMPLETED, 150, 150);
        dbHelper.insertBook(b1);
        dbHelper.insertBook(b2);

        List<Book> allBooks = dbHelper.getAllBooks();
        assertEquals(2, allBooks.size());
        assertEquals(2, dbHelper.getTotalBooksCount());
    }

    @Test
    public void testUpdateBook() throws InterruptedException {
        Book book = createSampleBook("Original Title", "Original Author", Constants.STATUS_READING, 300, 50);
        long id = dbHelper.insertBook(book);

        Book inserted = dbHelper.getBookById(id);
        long initialCreatedAt = inserted.getCreatedAt();
        long initialUpdatedAt = inserted.getUpdatedAt();

        // Small delay to ensure timestamp difference
        Thread.sleep(10);

        inserted.setTitle("Updated Title");
        inserted.setPagesRead(150);
        int rowsAffected = dbHelper.updateBook(inserted);

        assertEquals(1, rowsAffected);

        Book updated = dbHelper.getBookById(id);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals(150, updated.getPagesRead());
        assertEquals("Created_at must remain unchanged after edit", initialCreatedAt, updated.getCreatedAt());
        assertTrue("Updated timestamp should be greater than initial", updated.getUpdatedAt() > initialUpdatedAt);
    }

    @Test
    public void testGetRecentBooks_ReturnsLatest5InRightOrder() throws InterruptedException {
        for (int i = 1; i <= 7; i++) {
            Book b = createSampleBook("Book " + i, "Author " + i, Constants.STATUS_WANT_TO_READ, 100, 0);
            dbHelper.insertBook(b);
            Thread.sleep(5);
        }

        List<Book> recent = dbHelper.getRecentBooks(5);
        assertEquals(5, recent.size());
        assertEquals("Book 7", recent.get(0).getTitle());
        assertEquals("Book 6", recent.get(1).getTitle());
        assertEquals("Book 5", recent.get(2).getTitle());
    }

    @Test
    public void testUpdateBook_DuplicateTitleAndAuthorCheck() {
        Book b1 = createSampleBook("Dune", "Frank Herbert", Constants.STATUS_READING, 412, 100);
        Book b2 = createSampleBook("Foundation", "Isaac Asimov", Constants.STATUS_COMPLETED, 255, 255);
        long id1 = dbHelper.insertBook(b1);
        long id2 = dbHelper.insertBook(b2);

        // Checking duplicate for b2 using b1's title and author should return true
        assertTrue(dbHelper.isDuplicateTitleAndAuthor("Dune", "Frank Herbert", id2));

        // Checking duplicate for b1 using its own title and author should return false (ignores self ID)
        assertFalse(dbHelper.isDuplicateTitleAndAuthor("Dune", "Frank Herbert", id1));
    }

    @Test
    public void testDeleteBook() {
        Book book = createSampleBook("To Be Deleted", "Some Author", Constants.STATUS_WANT_TO_READ, 100, 0);
        long id = dbHelper.insertBook(book);

        assertNotNull(dbHelper.getBookById(id));
        dbHelper.deleteBook(id);
        assertNull("Deleted book should return null", dbHelper.getBookById(id));
        assertEquals(0, dbHelper.getTotalBooksCount());
    }

    // 2. Case-Insensitive Unique Constraint & Duplicate Checks
    @Test(expected = SQLiteConstraintException.class)
    public void testUniqueConstraint_DuplicateInsertThrowsException() {
        Book b1 = createSampleBook("Animal Farm", "George Orwell", Constants.STATUS_WANT_TO_READ, 140, 0);
        dbHelper.insertBook(b1);

        // Case-insensitive duplicate
        Book b2 = createSampleBook("ANIMAL FARM", "george orwell", Constants.STATUS_WANT_TO_READ, 140, 0);
        dbHelper.insertBook(b2);
    }

    @Test
    public void testIsDuplicateTitleAndAuthor() {
        Book b1 = createSampleBook("Brave New World", "Aldous Huxley", Constants.STATUS_WANT_TO_READ, 288, 0);
        long id1 = dbHelper.insertBook(b1);

        // Checking duplicate ignoring self ID should return false
        assertFalse("Should return false when checking duplicate against self ID",
                dbHelper.isDuplicateTitleAndAuthor("Brave New World", "Aldous Huxley", id1));

        // Checking duplicate for new book ID (0) should return true
        assertTrue("Should return true when duplicate title and author exist",
                dbHelper.isDuplicateTitleAndAuthor("brave new world", "aldous huxley", 0));
    }

    // 3. Raw SQL CHECK Constraint Failures
    @Test(expected = SQLiteConstraintException.class)
    public void testCheckConstraint_InvalidStatus_RawSql() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();
        db.execSQL("INSERT INTO books (title, author, category, total_pages, pages_read, status, description, created_at, updated_at) " +
                "VALUES ('Title', 'Author', 'Fiction', 100, 0, 'INVALID_STATUS', 'Desc', " + now + ", " + now + ");");
    }

    @Test(expected = SQLiteConstraintException.class)
    public void testCheckConstraint_PagesReadExceedsTotalPages_RawSql() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();
        db.execSQL("INSERT INTO books (title, author, category, total_pages, pages_read, status, description, created_at, updated_at) " +
                "VALUES ('Title', 'Author', 'Fiction', 100, 150, 'READING', 'Desc', " + now + ", " + now + ");");
    }

    @Test(expected = SQLiteConstraintException.class)
    public void testCheckConstraint_TotalPagesZero_RawSql() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();
        db.execSQL("INSERT INTO books (title, author, category, total_pages, pages_read, status, description, created_at, updated_at) " +
                "VALUES ('Title', 'Author', 'Fiction', 0, 0, 'WANT_TO_READ', 'Desc', " + now + ", " + now + ");");
    }

    @Test(expected = SQLiteConstraintException.class)
    public void testCheckConstraint_TotalPagesExceedsMax_RawSql() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();
        db.execSQL("INSERT INTO books (title, author, category, total_pages, pages_read, status, description, created_at, updated_at) " +
                "VALUES ('Title', 'Author', 'Fiction', 10001, 0, 'WANT_TO_READ', 'Desc', " + now + ", " + now + ");");
    }

    // 4. SQL Injection Security
    @Test
    public void testSearchWithSqlInjectionAttempts() {
        Book book = createSampleBook("Normal Title", "Normal Author", Constants.STATUS_WANT_TO_READ, 200, 0);
        dbHelper.insertBook(book);

        // Injection payloads
        String[] injectionPayloads = new String[]{
                "'; DROP TABLE books;--",
                "1' OR '1'='1",
                "\" OR \"1\"=\"1",
                "%",
                "_",
                "'",
                "\"",
                "\\",
                "Very long string " + new String(new char[1000]).replace('\0', 'A')
        };

        for (String payload : injectionPayloads) {
            List<Book> results = dbHelper.getFilteredBooks(payload, null, null);
            assertNotNull("Query result should not be null", results);
            assertEquals("Database table must stay intact", 1, dbHelper.getTotalBooksCount());
        }
    }

    // 5. Filtering, Sorting, and SQL Aggregates
    @Test
    public void testFilterByStatusAndSort() {
        Book b1 = createSampleBook("Alpha Book", "Author Z", Constants.STATUS_WANT_TO_READ, 100, 0);
        Book b2 = createSampleBook("Beta Book", "Author A", Constants.STATUS_READING, 200, 50);
        Book b3 = createSampleBook("Gamma Book", "Author M", Constants.STATUS_COMPLETED, 300, 300);

        dbHelper.insertBook(b1);
        dbHelper.insertBook(b2);
        dbHelper.insertBook(b3);

        // Status filter
        List<Book> readingBooks = dbHelper.getFilteredBooks(null, Constants.STATUS_READING, null);
        assertEquals(1, readingBooks.size());
        assertEquals("Beta Book", readingBooks.get(0).getTitle());

        // Sort TITLE_ASC
        List<Book> sortedTitleAsc = dbHelper.getFilteredBooks(null, "ALL", "TITLE_ASC");
        assertEquals("Alpha Book", sortedTitleAsc.get(0).getTitle());
        assertEquals("Beta Book", sortedTitleAsc.get(1).getTitle());
        assertEquals("Gamma Book", sortedTitleAsc.get(2).getTitle());

        // Sort AUTHOR_ASC
        List<Book> sortedAuthorAsc = dbHelper.getFilteredBooks(null, "ALL", "AUTHOR_ASC");
        assertEquals("Author A", sortedAuthorAsc.get(0).getAuthor());
    }

    @Test
    public void testStatisticsQueries() {
        Book b1 = createSampleBook("Book A", "Author A", Constants.STATUS_COMPLETED, 200, 200);
        b1.setCategory("Fiction");
        Book b2 = createSampleBook("Book B", "Author B", Constants.STATUS_READING, 300, 150);
        b2.setCategory("Sci-Fi");
        Book b3 = createSampleBook("Book C", "Author C", Constants.STATUS_READING, 100, 50);
        b3.setCategory("Fiction");

        dbHelper.insertBook(b1);
        dbHelper.insertBook(b2);
        dbHelper.insertBook(b3);

        assertEquals(3, dbHelper.getTotalBooksCount());
        assertEquals(1, dbHelper.getBooksCountByStatus(Constants.STATUS_COMPLETED));
        assertEquals(2, dbHelper.getBooksCountByStatus(Constants.STATUS_READING));
        assertEquals(400, dbHelper.getTotalPagesRead()); // 200 + 150 + 50
        assertEquals(600, dbHelper.getTotalPagesCount()); // 200 + 300 + 100

        Map<String, Integer> categoryCounts = dbHelper.getCategoryCounts();
        assertEquals(Integer.valueOf(2), categoryCounts.get("Fiction"));
        assertEquals(Integer.valueOf(1), categoryCounts.get("Sci-Fi"));
    }
}
