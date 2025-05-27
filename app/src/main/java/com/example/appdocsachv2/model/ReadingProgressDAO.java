package com.example.appdocsachv2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.appdocsachv2.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ReadingProgressDAO {
    private DatabaseHelper dbHelper;
    private static final String TABLE_NAME = "ReadingProgress";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_BOOK_ID = "book_id";
    private static final String COLUMN_CURRENT_PAGE = "current_page";

    public ReadingProgressDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

//    public void open() {
//        dbHelper.getWritableDatabase();
//    }
//
//    public void close() {
//        dbHelper.close();
//    }

    public long insertOrUpdateReadingProgress(int userId, int bookId, int currentPage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_BOOK_ID, bookId);
        values.put(COLUMN_CURRENT_PAGE, currentPage);
//nếu có hàng đã tồn taij với cùng userid và book id thay thế bằng CONFLICT_REPLACE
        long id = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return id;
    }
//Lấy danh sách tất cả tiến trình đọc của một người dùng dựa trên userId.
    public List<ReadingProgress> getReadingProgress(int userId) {
        List<ReadingProgress> progressList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                ReadingProgress progress = new ReadingProgress();
                progress.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                progress.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
                progress.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)));
                progress.setCurrentPage(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PAGE)));
                progressList.add(progress);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return progressList;
    }
//Lấy trang cuối cùng mà người dùng đã đọc cho một cuốn sách cụ thể.
    public int getLastReadPage(int userId, int bookId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int lastPage = -1;
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_CURRENT_PAGE + " FROM " + TABLE_NAME +
                        " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_BOOK_ID + " = ? LIMIT 1",
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        if (cursor.moveToFirst()) {
            lastPage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PAGE));
        }
        cursor.close();
        db.close();
        return lastPage;
    }
}