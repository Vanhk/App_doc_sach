package com.example.appdocsachv2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.appdocsachv2.database.DatabaseHelper;

import java.util.ArrayList;

public class ChapterDAO {
    private DatabaseHelper dbHelper;

    public ChapterDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insertChapter(Chapter chapter) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("book_id", chapter.getBookId());
        values.put("title", chapter.getTitle());
        values.put("start_page", chapter.getStartPage());
        values.put("end_page", chapter.getEndPage());
        long id = db.insert("Chapter", null, values);
        db.close();
        return id;
    }

//    public boolean updateChapter(Chapter chapter) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("book_id", chapter.getBookId());
//        values.put("title", chapter.getTitle());
//        values.put("start_page", chapter.getStartPage());
//        values.put("end_page", chapter.getEndPage());
//        int rows = db.update("Chapter", values, "chapter_id = ?", new String[]{String.valueOf(chapter.getChapterId())});
//        db.close();
//        return rows > 0;
//    }

//    public Chapter getChapterById(int chapterId) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM Chapter WHERE chapter_id = ?", new String[]{String.valueOf(chapterId)});
//        Chapter chapter = null;
//        if (cursor.moveToFirst()) {
//            chapter = new Chapter();
//            chapter.setChapterId(cursor.getInt(cursor.getColumnIndexOrThrow("chapter_id")));
//            chapter.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow("book_id")));
//            chapter.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
//            chapter.setStartPage(cursor.getInt(cursor.getColumnIndexOrThrow("start_page")));
//            chapter.setEndPage(cursor.getInt(cursor.getColumnIndexOrThrow("end_page")));
//        }
//        cursor.close();
//        db.close();
//        return chapter;
//    }
    public ArrayList<Chapter> getChaptersByBookId(int bookId) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Chapter WHERE book_id = ?", new String[]{String.valueOf(bookId)});
        if (cursor.moveToFirst()) {
            do {
                Chapter chapter = new Chapter();
                chapter.setChapterId(cursor.getInt(cursor.getColumnIndexOrThrow("chapter_id")));
                chapter.setBookId(bookId);
                chapter.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                chapter.setStartPage(cursor.getInt(cursor.getColumnIndexOrThrow("start_page")));
                chapter.setEndPage(cursor.getInt(cursor.getColumnIndexOrThrow("end_page")));
                chapters.add(chapter);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return chapters;
    }

    public void deleteChaptersByBookId(int bookId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Chapter", "book_id = ?", new String[]{String.valueOf(bookId)});
        db.close();
    }
}