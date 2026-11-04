package com.example.booktracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BookTracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table and Column Names
    public static final String TABLE_BOOKS = "books";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_TOTAL_PAGES = "total_pages";
    public static final String COLUMN_PAGES_READ = "pages_read";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // SQL Statement to create the table with strict constraints
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_BOOKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL CHECK(length(trim(title)) >= 2 AND length(title) <= 100), " +
                    COLUMN_AUTHOR + " TEXT NOT NULL CHECK(length(trim(author)) >= 2 AND length(author) <= 60), " +
                    COLUMN_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_TOTAL_PAGES + " INTEGER NOT NULL CHECK(total_pages >= 1 AND total_pages <= 10000), " +
                    COLUMN_PAGES_READ + " INTEGER NOT NULL DEFAULT 0 CHECK(pages_read >= 0 AND pages_read <= total_pages), " +
                    COLUMN_STATUS + " TEXT NOT NULL CHECK(status IN ('" + Constants.STATUS_WANT_TO_READ + "', '" + Constants.STATUS_READING + "', '" + Constants.STATUS_COMPLETED + "')), " +
                    COLUMN_DESCRIPTION + " TEXT CHECK(description IS NULL OR length(description) <= 500), " +
                    COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                    COLUMN_UPDATED_AT + " INTEGER NOT NULL, " +
                    // Enforce logical status limits at the database level
                    "CHECK ((" + COLUMN_STATUS + " = '" + Constants.STATUS_WANT_TO_READ + "' AND " + COLUMN_PAGES_READ + " = 0) OR " +
                    "(" + COLUMN_STATUS + " = '" + Constants.STATUS_COMPLETED + "' AND " + COLUMN_PAGES_READ + " = " + COLUMN_TOTAL_PAGES + ") OR " +
                    "(" + COLUMN_STATUS + " = '" + Constants.STATUS_READING + "')), " +
                    // Case-insensitive unique constraint for Title + Author
                    "UNIQUE(" + COLUMN_TITLE + " COLLATE NOCASE, " + COLUMN_AUTHOR + " COLLATE NOCASE)" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For a mini-project V1, simple drop and recreate is fine.
        // In production, you would run ALTER TABLE migrations here.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        onCreate(db);
    }

    /**
     * Inserts a new book into the database.
     * @param book The book model to insert
     * @return The row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insertBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        long currentTime = System.currentTimeMillis();

        // ContentValues automatically parameterizes inputs, preventing SQL Injection
        values.put(COLUMN_TITLE, book.getTitle().trim());
        values.put(COLUMN_AUTHOR, book.getAuthor().trim());
        values.put(COLUMN_CATEGORY, book.getCategory());
        values.put(COLUMN_TOTAL_PAGES, book.getTotalPages());
        values.put(COLUMN_PAGES_READ, book.getPagesRead());
        values.put(COLUMN_STATUS, book.getStatus());
        values.put(COLUMN_DESCRIPTION, book.getDescription());
        values.put(COLUMN_CREATED_AT, currentTime);
        values.put(COLUMN_UPDATED_AT, currentTime);

        long id = db.insert(TABLE_BOOKS, null, values);
        db.close();
        return id;
    }

    /**
     * Updates an existing book.
     * @param book The book model with updated data
     * @return the number of rows affected
     */
    public int updateBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, book.getTitle().trim());
        values.put(COLUMN_AUTHOR, book.getAuthor().trim());
        values.put(COLUMN_CATEGORY, book.getCategory());
        values.put(COLUMN_TOTAL_PAGES, book.getTotalPages());
        values.put(COLUMN_PAGES_READ, book.getPagesRead());
        values.put(COLUMN_STATUS, book.getStatus());
        values.put(COLUMN_DESCRIPTION, book.getDescription());
        values.put(COLUMN_UPDATED_AT, System.currentTimeMillis());

        // Parameterized query using String array
        int rows = db.update(TABLE_BOOKS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(book.getId())});
        db.close();
        return rows;
    }

    /**
     * Retrieves all books from the database.
     * @return List of Book objects
     */
    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Parameterized query, though no WHERE clause here.
        Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, COLUMN_UPDATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookList.add(cursorToBook(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return bookList;
    }

    /**
     * Deletes a book by ID.
     * @param id The ID of the book to delete
     */
    public void deleteBook(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Helper method to map a Database Cursor row to a Book object.
     */
    private Book cursorToBook(Cursor cursor) {
        Book book = new Book();
        book.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
        book.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR)));
        book.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
        book.setTotalPages(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PAGES)));
        book.setPagesRead(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGES_READ)));
        book.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
        book.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
        book.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        book.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)));
        return book;
    }
}