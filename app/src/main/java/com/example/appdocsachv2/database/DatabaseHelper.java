package com.example.appdocsachv2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BookApp.db";
    private static final int DATABASE_VERSION = 2; // Tăng version để áp dụng nâng cấp

    // Tạo bảng User
    private static final String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS User (" +
            "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "username TEXT NOT NULL UNIQUE," +
            "password TEXT NOT NULL," +
            "email TEXT UNIQUE" +
            ")";

    // Tạo bảng Book (thêm cột summary)
    private static final String CREATE_TABLE_BOOK = "CREATE TABLE IF NOT EXISTS Book (" +
            "book_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT NOT NULL," +
            "author TEXT," +
            "genre TEXT," +
            "file_path TEXT NOT NULL," +
            "cover_image TEXT," +
            "total_pages INTEGER," +
            "summary TEXT" + // Thêm cột summary
            ")";

    // Tạo bảng Chapter
    private static final String CREATE_TABLE_CHAPTER = "CREATE TABLE IF NOT EXISTS Chapter (" +
            "chapter_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "book_id INTEGER NOT NULL," +
            "title TEXT NOT NULL," +
            "start_page INTEGER," +
            "end_page INTEGER," +
            "FOREIGN KEY (book_id) REFERENCES Book(book_id) ON DELETE CASCADE" +
            ")";

    // Tạo bảng FavoriteBook
    private static final String CREATE_TABLE_FAVORITE_BOOK = "CREATE TABLE IF NOT EXISTS FavoriteBook (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "book_id INTEGER NOT NULL," +
            "FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE," +
            "FOREIGN KEY (book_id) REFERENCES Book(book_id) ON DELETE CASCADE," +
            "UNIQUE (user_id, book_id)" +
            ")";

    // Tạo bảng ReadingProgress
    private static final String CREATE_TABLE_READING_PROGRESS = "CREATE TABLE IF NOT EXISTS ReadingProgress (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "book_id INTEGER NOT NULL," +
            "current_page INTEGER DEFAULT 0," +
            "FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE," +
            "FOREIGN KEY (book_id) REFERENCES Book(book_id) ON DELETE CASCADE," +
            "UNIQUE (user_id, book_id)" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_BOOK);
        db.execSQL(CREATE_TABLE_CHAPTER);
        db.execSQL(CREATE_TABLE_FAVORITE_BOOK);
        db.execSQL(CREATE_TABLE_READING_PROGRESS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Thêm cột summary vào bảng Book nếu chưa tồn tại
            db.execSQL("ALTER TABLE Book ADD COLUMN summary TEXT");
        }
        // Thêm các nâng cấp khác nếu có trong tương lai
        // Ví dụ: nếu cần thêm cột mới hoặc thay đổi cấu trúc khác
    }
}