package com.example.appdocsachv2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BookApp.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS User (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "email TEXT UNIQUE"+
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS Book (" +
                "book_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "author TEXT," +
                "genre TEXT," +
                "file_path TEXT NOT NULL," +
                "cover_image TEXT," +
                "total_pages INTEGER"+");");

        db.execSQL("CREATE TABLE IF NOT EXISTS Chapter (" +
                "chapter_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "book_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "start_page INTEGER," +
                "end_page INTEGER," +
                "FOREIGN KEY (book_id) REFERENCES Book(book_id) ON DELETE CASCADE"+");");

        db.execSQL("CREATE TABLE IF NOT EXISTS FavoriteBook (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "book_id INTEGER NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE," +
                "FOREIGN KEY (book_id) REFERENCES Book(book_id) ON DELETE CASCADE," +
                "UNIQUE (user_id, book_id)"+");");

        db.execSQL("CREATE TABLE IF NOT EXISTS ReadingProgress (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "book_id INTEGER NOT NULL," +
                "current_page INTEGER DEFAULT 0," +
                "FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE," +
                "FOREIGN KEY (book_id) REFERENCES Book(book_id) ON DELETE CASCADE," +
                "UNIQUE (user_id, book_id)"+");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ReadingProgress");
        db.execSQL("DROP TABLE IF EXISTS FavoriteBook");
        db.execSQL("DROP TABLE IF EXISTS Chapter");
        db.execSQL("DROP TABLE IF EXISTS Book");
        db.execSQL("DROP TABLE IF EXISTS User");
        onCreate(db);
    }
}