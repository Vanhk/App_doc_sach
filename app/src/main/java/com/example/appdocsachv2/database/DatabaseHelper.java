package com.example.appdocsachv2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "BookApp.db";
    private static final int DATABASE_VERSION = 2; // Tăng version để áp dụng nâng cấp
    // Bảng User

    public static final String TABLE_USER = "User";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";

    // Bảng Book
    public static final String TABLE_BOOK = "Book";
    public static final String COLUMN_BOOK_ID = "book_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_GENRE = "genre";
    public static final String COLUMN_FILE_PATH = "file_path";
    public static final String COLUMN_COVER_IMAGE = "cover_image";
    public static final String COLUMN_TOTAL_PAGES = "total_pages";
    public static final String COLUMN_SUMMARY = "summary";

    // Bảng Chapter
    public static final String TABLE_CHAPTER = "Chapter";
    public static final String COLUMN_CHAPTER_ID = "chapter_id";
    public static final String COLUMN_BOOK_ID_FK = "book_id";
    public static final String COLUMN_CHAPTER_TITLE = "title";
    public static final String COLUMN_START_PAGE = "start_page";
    public static final String COLUMN_END_PAGE = "end_page";

    // Bảng FavoriteBook
    public static final String TABLE_FAVORITE_BOOK = "FavoriteBook";
    public static final String COLUMN_FAVORITE_ID = "id";
    public static final String COLUMN_FAVORITE_USER_ID = "user_id";
    public static final String COLUMN_FAVORITE_BOOK_ID = "book_id";

    // Bảng ReadingProgress
    public static final String TABLE_READING_PROGRESS = "ReadingProgress";
    public static final String COLUMN_PROGRESS_ID = "id";
    public static final String COLUMN_USER_ID_FK = "user_id";
    public static final String COLUMN_BOOK_ID_FK_PROGRESS = "book_id";
    public static final String COLUMN_CURRENT_PAGE = "current_page";

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
        try {
            db.execSQL(CREATE_TABLE_USER);
            db.execSQL(CREATE_TABLE_BOOK);
            db.execSQL(CREATE_TABLE_CHAPTER);
            db.execSQL(CREATE_TABLE_FAVORITE_BOOK);
            db.execSQL(CREATE_TABLE_READING_PROGRESS);
            //cursor cho phép lấy dữ liệu từ bảng,kiểm tra xem có tk admin trong bảng user ko
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USER + " WHERE " + COLUMN_USERNAME + " = ?", new String[]{"admin"});
            if (cursor != null) {
                if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_USERNAME, "admin");
                    values.put(COLUMN_PASSWORD, "admin");
                    values.put(COLUMN_EMAIL, "admin@example.com");
                    db.insert(TABLE_USER, null, values);
                }
                cursor.close();//đóng cursor khi không dùng
            }
        } catch (SQLiteException e) {
        }
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