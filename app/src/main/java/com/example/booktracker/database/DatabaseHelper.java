package com.example.booktracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.booktracker.model.Book;
import com.example.booktracker.utils.BookValidator;
import com.example.booktracker.utils.Constants;
import com.example.booktracker.utils.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BookTracker.db";
    private static final int DATABASE_VERSION = 1;

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

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_BOOKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL CHECK(length(trim(title)) >= " + Constants.TITLE_MIN_LENGTH + " AND length(title) <= " + Constants.TITLE_MAX_LENGTH + "), " +
                    COLUMN_AUTHOR + " TEXT NOT NULL CHECK(length(trim(author)) >= " + Constants.AUTHOR_MIN_LENGTH + " AND length(author) <= " + Constants.AUTHOR_MAX_LENGTH + "), " +
                    COLUMN_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_TOTAL_PAGES + " INTEGER NOT NULL CHECK(total_pages >= " + Constants.MIN_PAGES + " AND total_pages <= " + Constants.MAX_PAGES + "), " +
                    COLUMN_PAGES_READ + " INTEGER NOT NULL DEFAULT 0 CHECK(pages_read >= 0 AND pages_read <= total_pages), " +
                    COLUMN_STATUS + " TEXT NOT NULL CHECK(status IN ('" + Constants.STATUS_WANT_TO_READ + "', '" + Constants.STATUS_READING + "', '" + Constants.STATUS_COMPLETED + "')), " +
                    COLUMN_DESCRIPTION + " TEXT CHECK(description IS NULL OR length(description) <= " + Constants.DESCRIPTION_MAX_LENGTH + "), " +
                    COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                    COLUMN_UPDATED_AT + " INTEGER NOT NULL, " +
                    "CHECK ((" + COLUMN_STATUS + " = '" + Constants.STATUS_WANT_TO_READ + "' AND " + COLUMN_PAGES_READ + " = 0) OR " +
                    "(" + COLUMN_STATUS + " = '" + Constants.STATUS_COMPLETED + "' AND " + COLUMN_PAGES_READ + " = " + COLUMN_TOTAL_PAGES + ") OR " +
                    "(" + COLUMN_STATUS + " = '" + Constants.STATUS_READING + "')), " +
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        onCreate(db);
    }

    public long insertBook(Book book) {
        ValidationResult validationResult = BookValidator.validate(book);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Book validation failed: " + validationResult.getErrors());
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        long currentTime = System.currentTimeMillis();

        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_AUTHOR, book.getAuthor());
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

    public int updateBook(Book book) {
        ValidationResult validationResult = BookValidator.validate(book);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Book validation failed: " + validationResult.getErrors());
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_CATEGORY, book.getCategory());
        values.put(COLUMN_TOTAL_PAGES, book.getTotalPages());
        values.put(COLUMN_PAGES_READ, book.getPagesRead());
        values.put(COLUMN_STATUS, book.getStatus());
        values.put(COLUMN_DESCRIPTION, book.getDescription());
        values.put(COLUMN_UPDATED_AT, System.currentTimeMillis());

        int rows = db.update(TABLE_BOOKS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(book.getId())});
        db.close();
        return rows;
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
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

    public int getTotalBooksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKS, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        db.close();
        return count;
    }

    public int getBooksCountByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKS + " WHERE " + COLUMN_STATUS + " = ?", new String[]{status})) {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        db.close();
        return count;
    }

    public int getTotalPagesRead() {
        SQLiteDatabase db = this.getReadableDatabase();
        int sum = 0;
        try (Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_PAGES_READ + ") FROM " + TABLE_BOOKS, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                sum = cursor.getInt(0);
            }
        }
        db.close();
        return sum;
    }

    public int getTotalPagesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int sum = 0;
        try (Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_TOTAL_PAGES + ") FROM " + TABLE_BOOKS, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                sum = cursor.getInt(0);
            }
        }
        db.close();
        return sum;
    }

    public java.util.Map<String, Integer> getCategoryCounts() {
        java.util.Map<String, Integer> categoryMap = new java.util.LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + COLUMN_CATEGORY + ", COUNT(*) FROM " + TABLE_BOOKS +
                " GROUP BY " + COLUMN_CATEGORY +
                " ORDER BY COUNT(*) DESC";
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    int count = cursor.getInt(1);
                    categoryMap.put(category, count);
                } while (cursor.moveToNext());
            }
        }
        db.close();
        return categoryMap;
    }

    public List<Book> getRecentBooks(int limit) {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, COLUMN_CREATED_AT + " DESC", String.valueOf(limit))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    bookList.add(cursorToBook(cursor));
                } while (cursor.moveToNext());
            }
        }
        db.close();
        return bookList;
    }

    public Book getBookById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Book book = null;
        try (Cursor cursor = db.query(TABLE_BOOKS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                book = cursorToBook(cursor);
            }
        }
        db.close();
        return book;
    }

    public boolean isDuplicateTitleAndAuthor(String title, String author, long excludeBookId) {
        if (title == null || author == null) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isDuplicate = false;
        String sql = "SELECT COUNT(*) FROM " + TABLE_BOOKS +
                " WHERE " + COLUMN_TITLE + " = ? COLLATE NOCASE" +
                " AND " + COLUMN_AUTHOR + " = ? COLLATE NOCASE" +
                " AND " + COLUMN_ID + " != ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{title.trim(), author.trim(), String.valueOf(excludeBookId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                isDuplicate = cursor.getInt(0) > 0;
            }
        }
        db.close();
        return isDuplicate;
    }

    public List<Book> getFilteredBooks(String searchQuery, String statusFilter, String sortBy) {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_BOOKS + " WHERE 1=1");
        List<String> selectionArgs = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            // Safely escape SQLite LIKE wildcards (%, _, !)
            String escapedQuery = searchQuery.trim()
                    .replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_");

            sql.append(" AND (").append(COLUMN_TITLE).append(" LIKE ? ESCAPE '!' OR ")
               .append(COLUMN_AUTHOR).append(" LIKE ? ESCAPE '!')");
            String likeParam = "%" + escapedQuery + "%";
            selectionArgs.add(likeParam);
            selectionArgs.add(likeParam);
        }

        if (statusFilter != null && !statusFilter.isEmpty() && !"ALL".equals(statusFilter)) {
            sql.append(" AND ").append(COLUMN_STATUS).append(" = ?");
            selectionArgs.add(statusFilter);
        }

        if ("TITLE_ASC".equals(sortBy)) {
            sql.append(" ORDER BY ").append(COLUMN_TITLE).append(" COLLATE NOCASE ASC");
        } else if ("TITLE_DESC".equals(sortBy)) {
            sql.append(" ORDER BY ").append(COLUMN_TITLE).append(" COLLATE NOCASE DESC");
        } else if ("AUTHOR_ASC".equals(sortBy)) {
            sql.append(" ORDER BY ").append(COLUMN_AUTHOR).append(" COLLATE NOCASE ASC");
        } else if ("AUTHOR_DESC".equals(sortBy)) {
            sql.append(" ORDER BY ").append(COLUMN_AUTHOR).append(" COLLATE NOCASE DESC");
        } else {
            sql.append(" ORDER BY ").append(COLUMN_CREATED_AT).append(" DESC");
        }

        try (Cursor cursor = db.rawQuery(sql.toString(), selectionArgs.toArray(new String[0]))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    bookList.add(cursorToBook(cursor));
                } while (cursor.moveToNext());
            }
        }
        db.close();
        return bookList;
    }

    public void deleteBook(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

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
