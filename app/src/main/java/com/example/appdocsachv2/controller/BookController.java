package com.example.appdocsachv2.controller;

import android.content.Context;
import android.content.SharedPreferences;


import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookController {
//    private static final String TAG = "BookController";
    private BookDAO bookDAO;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "favorite_books";
    private static final String KEY_FAVORITES = "favorites";

    public BookController(BookDAO bookDAO) {
        this.bookDAO = bookDAO;

        // Lấy Context từ DAO
        Context context = bookDAO.getContext();
        if (context == null) {
            throw new IllegalStateException("Context cannot be null in BookDAO");
        }
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Thêm sách
    public long addBook(Book book) {
        if (book == null || book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return -1;
        }
        long result = bookDAO.insertBook(book);
        return result;
    }

    // Cập nhật sách
    public boolean updateBook(Book book) {
        if (book == null || book.getBookId() < 0 || book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return false;
        }
        boolean result = bookDAO.updateBook(book);
        return result;
    }

    // Xoá sách
    public boolean deleteBook(int bookId) {
        if (bookId < 0) {
            return false;
        }
        bookDAO.deleteBook(bookId);
        removeFavorite(bookId);
        return true;
    }

    // Lấy tất cả sách
    public List<Book> getAllBooks() {
        List<Book> books = bookDAO.getAllBooks();
        return books;
    }

    // Lấy sách theo ID
    public Book getBookById(int bookId) {
        if (bookId < 0) {
            return null;
        }
        Book book = bookDAO.getBookById(bookId);
        return book;
    }
//xóa khỏi ds yêu thích
    public void removeFavorite(int bookId) {
        Set<String> favorites = getFavoriteSet();
        favorites.remove(String.valueOf(bookId));
        saveFavoriteSet(favorites);
    }

//    public boolean isFavorite(int bookId) {
//        boolean isFavorite = getFavoriteSet().contains(String.valueOf(bookId));
//        return isFavorite;
//    }

//Lấy tập hợp String từ SharedPreferences với key favorites, mặc định là tập rỗng nếu không có.
    private Set<String> getFavoriteSet() {
        Set<String> favorites = sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>());
        if (favorites == null) {
            return new HashSet<>();
        }
        return new HashSet<>(favorites);
    }

    private void saveFavoriteSet(Set<String> favorites) {
        SharedPreferences.Editor editor = sharedPreferences.edit();//Tạo một Editor để chỉnh sửa SharedPreferences.
        editor.putStringSet(KEY_FAVORITES, favorites);
        editor.apply();
    }
}