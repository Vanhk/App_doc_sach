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

    public ReadingProgressDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insertOrUpdateReadingProgress(int userId, int bookId, int currentPage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("book_id", bookId);
        values.put("current_page", currentPage);

        long id = db.insertWithOnConflict("ReadingProgress", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return id;
    }

    public List<ReadingProgress> getReadingProgressByUserId(int userId) {
        List<ReadingProgress> progressList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ReadingProgress WHERE user_id = ?", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                ReadingProgress progress = new ReadingProgress();
                progress.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                progress.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                progress.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow("book_id")));
                progress.setCurrentPage(cursor.getInt(cursor.getColumnIndexOrThrow("current_page")));
                progressList.add(progress);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return progressList;
    }
}