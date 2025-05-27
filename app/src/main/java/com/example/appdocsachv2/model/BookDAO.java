package com.example.appdocsachv2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.appdocsachv2.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
//•	Đây là lớp chịu trách nhiệm thực hiện các thao tác CRUD (Create, Read, Update, Delete)
// trên bảng Book trong cơ sở dữ liệu.
public class BookDAO {
    private DatabaseHelper dbHelper;
    private Context context;
    public BookDAO(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);

    }
    public Context getContext(){
        return context;
}
    public long insertBook(Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();//mở csdl cho phép thêm sửa xóa
        ContentValues values = new ContentValues();//chèn dl vào bảng
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("genre", book.getGenre());
        values.put("file_path", book.getFilePath());
        values.put("cover_image", book.getCoverImage());
        values.put("total_pages", book.getTotal_pages());
        values.put("summary", book.getSummary());
        long id = db.insert("Book", null, values);//thêm một hàng vào bảng
        db.close();
        return id;
    }

    public boolean updateBook(Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("genre", book.getGenre());
        values.put("file_path", book.getFilePath());
        values.put("cover_image", book.getCoverImage());
        values.put("total_pages", book.getTotal_pages());
        values.put("summary", book.getSummary());
        int rows = db.update("Book", values, "book_id = ?", new String[]{String.valueOf(book.getBookId())});
        db.close();
        return rows > 0;
    }
    // Lấy tất cả sách
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();//mở csdl ở chế độ đọc
        Cursor cursor = db.rawQuery("SELECT * FROM Book", null);
        if (cursor.moveToFirst()) {
            do {
                Book book = new Book();
                book.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow("book_id")));
                book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                book.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow("author")));
                book.setGenre(cursor.getString(cursor.getColumnIndexOrThrow("genre")));
                book.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
                book.setCoverImage(cursor.getString(cursor.getColumnIndexOrThrow("cover_image")));
                book.setTotal_pages(cursor.getInt(cursor.getColumnIndexOrThrow("total_pages")));
                book.setSummary(cursor.getString(cursor.getColumnIndexOrThrow("summary")));
                books.add(book);
            } while (cursor.moveToNext());//lăpj qua tất cả các hnagf
        }
        cursor.close();
        db.close();
        return books;
    }
    public void deleteBook(int bookId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Xóa các chương liên quan trước
        db.delete("Chapter", "book_id = ?", new String[]{String.valueOf(bookId)});
        // Xóa sách
        db.delete("Book", "book_id = ?", new String[]{String.valueOf(bookId)});
        db.close();
    }
    public Book getBookById(int bookId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Book WHERE book_id = ?", new String[]{String.valueOf(bookId)});
        Book book = null;
        if (cursor.moveToFirst()) {
            book = new Book();
            book.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow("book_id")));
            book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            book.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow("author")));
            book.setGenre(cursor.getString(cursor.getColumnIndexOrThrow("genre")));
            book.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
            book.setCoverImage(cursor.getString(cursor.getColumnIndexOrThrow("cover_image")));
            book.setTotal_pages(cursor.getInt(cursor.getColumnIndexOrThrow("total_pages")));
            book.setSummary(cursor.getString(cursor.getColumnIndexOrThrow("summary")));
        }
        cursor.close();
        db.close();
        return book;
    }
}