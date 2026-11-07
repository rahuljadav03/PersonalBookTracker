package com.example.booktracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "books.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BOOKS = "books";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_AUTHOR = "author";
    public static final String COL_STATUS = "status";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    private static final String CREATE_TABLE_BOOKS = "CREATE TABLE " + TABLE_BOOKS + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_TITLE + " TEXT NOT NULL UNIQUE COLLATE NOCASE, "
            + COL_AUTHOR + " TEXT NOT NULL, "
            + COL_STATUS + " TEXT NOT NULL CHECK(" + COL_STATUS + " IN ('TO_READ', 'READING', 'COMPLETED', 'DROPPED')), "
            + COL_CREATED_AT + " INTEGER NOT NULL, "
            + COL_UPDATED_AT + " INTEGER NOT NULL"
            + ");";

    private static final String CREATE_INDEX_STATUS = "CREATE INDEX idx_status ON " + TABLE_BOOKS + "(" + COL_STATUS + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BOOKS);
        db.execSQL(CREATE_INDEX_STATUS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // A safe approach for development when incrementing versions.
            // In a production app, you would add ALTER TABLE statements to migrate data safely.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
            onCreate(db);
        }
    }

    public DbResult<Long> insertBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, book.getTitle());
        values.put(COL_AUTHOR, book.getAuthor());
        values.put(COL_STATUS, book.getStatus());

        long currentTime = System.currentTimeMillis();
        values.put(COL_CREATED_AT, currentTime);
        values.put(COL_UPDATED_AT, currentTime);

        try {
            long id = db.insertOrThrow(TABLE_BOOKS, null, values);
            book.setId(id);
            book.setCreatedAt(currentTime);
            book.setUpdatedAt(currentTime);
            return DbResult.success(id);
        } catch (SQLiteConstraintException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                return DbResult.error(DbResult.ErrorCode.DUPLICATE, "A book with this title already exists.");
            } else if (e.getMessage() != null && e.getMessage().contains("CHECK")) {
                return DbResult.error(DbResult.ErrorCode.CONSTRAINT_FAILED, "Invalid status or constraint violation.");
            }
            return DbResult.error(DbResult.ErrorCode.CONSTRAINT_FAILED, e.getMessage());
        } catch (Exception e) {
            return DbResult.error(DbResult.ErrorCode.ERROR, e.getMessage());
        }
    }

    public DbResult<Integer> updateBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, book.getTitle());
        values.put(COL_AUTHOR, book.getAuthor());
        values.put(COL_STATUS, book.getStatus());

        long currentTime = System.currentTimeMillis();
        values.put(COL_UPDATED_AT, currentTime);

        try {
            int rows = db.update(TABLE_BOOKS, values, COL_ID + "=?", new String[]{String.valueOf(book.getId())});
            if (rows > 0) {
                book.setUpdatedAt(currentTime);
                return DbResult.success(rows);
            } else {
                return DbResult.error(DbResult.ErrorCode.ERROR, "Book not found.");
            }
        } catch (SQLiteConstraintException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                return DbResult.error(DbResult.ErrorCode.DUPLICATE, "A book with this title already exists.");
            } else if (e.getMessage() != null && e.getMessage().contains("CHECK")) {
                return DbResult.error(DbResult.ErrorCode.CONSTRAINT_FAILED, "Invalid status or constraint violation.");
            }
            return DbResult.error(DbResult.ErrorCode.CONSTRAINT_FAILED, e.getMessage());
        } catch (Exception e) {
            return DbResult.error(DbResult.ErrorCode.ERROR, e.getMessage());
        }
    }

    public DbResult<Integer> deleteBook(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rows = db.delete(TABLE_BOOKS, COL_ID + "=?", new String[]{String.valueOf(id)});
            return DbResult.success(rows);
        } catch (Exception e) {
            return DbResult.error(DbResult.ErrorCode.ERROR, e.getMessage());
        }
    }

    public Book getBookById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_BOOKS, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToBook(cursor);
            }
        }
        return null;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    books.add(cursorToBook(cursor));
                } while (cursor.moveToNext());
            }
        }
        return books;
    }

    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Escape %, _ and our escape character ! to perform safe LIKE queries
        String escapedQuery = query.replace("!", "!!")
                                   .replace("%", "!%")
                                   .replace("_", "!_");

        String sqlQuery = "SELECT * FROM " + TABLE_BOOKS + " WHERE "
                + COL_TITLE + " LIKE ? ESCAPE '!' OR "
                + COL_AUTHOR + " LIKE ? ESCAPE '!'";

        String likeParam = "%" + escapedQuery + "%";

        try (Cursor cursor = db.rawQuery(sqlQuery, new String[]{likeParam, likeParam})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    books.add(cursorToBook(cursor));
                } while (cursor.moveToNext());
            }
        }
        return books;
    }

    public List<Book> filterByStatus(String status) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_BOOKS, null, COL_STATUS + "=?", new String[]{status}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    books.add(cursorToBook(cursor));
                } while (cursor.moveToNext());
            }
        }
        return books;
    }

    public List<Book> getAllSortedByTitle() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, COL_TITLE + " COLLATE NOCASE ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    books.add(cursorToBook(cursor));
                } while (cursor.moveToNext());
            }
        }
        return books;
    }

    public int getCountByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKS + " WHERE " + COL_STATUS + "=?", new String[]{status})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public List<Book> getRecentBooks(int limit) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, COL_CREATED_AT + " DESC", String.valueOf(limit))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    books.add(cursorToBook(cursor));
                } while (cursor.moveToNext());
            }
        }
        return books;
    }

    private Book cursorToBook(Cursor cursor) {
        Book book = new Book();
        book.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
        book.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR)));
        book.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        book.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        book.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT)));
        return book;
    }
}
