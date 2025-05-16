package com.example.appdocsachv2.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookController {
    private static final String TAG = "BookController";
    private BookDAO bookDAO;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "favorite_books";
    private static final String KEY_FAVORITES = "favorites";

    public BookController(BookDAO bookDAO) {
        this.bookDAO = bookDAO;

        // Lấy Context từ DAO
        Context context = bookDAO.getContext();
        if (context == null) {
            Log.e(TAG, "Context from BookDAO is null");
            throw new IllegalStateException("Context cannot be null in BookDAO");
        }
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "BookController initialized with SharedPreferences: " + PREF_NAME);
    }

    // Thêm sách
    public long addBook(Book book) {
        if (book == null || book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            Log.w(TAG, "Cannot add book: invalid book or title");
            return -1;
        }
        long result = bookDAO.insertBook(book);
        Log.d(TAG, "Added book with ID: " + result);
        return result;
    }

    // Cập nhật sách
    public boolean updateBook(Book book) {
        if (book == null || book.getBookId() < 0 || book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            Log.w(TAG, "Cannot update book: invalid book or ID");
            return false;
        }
        boolean result = bookDAO.updateBook(book);
        Log.d(TAG, "Updated book with ID: " + book.getBookId() + ", Success: " + result);
        return result;
    }

    // Xoá sách
    public boolean deleteBook(int bookId) {
        if (bookId < 0) {
            Log.w(TAG, "Cannot delete book: invalid bookId: " + bookId);
            return false;
        }
        bookDAO.deleteBook(bookId);
        removeFavorite(bookId); // Xoá khỏi danh sách yêu thích nếu có
        Log.d(TAG, "Deleted book with ID: " + bookId);
        return true;
    }

    // Lấy tất cả sách
    public List<Book> getAllBooks() {
        List<Book> books = bookDAO.getAllBooks();
        Log.d(TAG, "Retrieved all books, count: " + (books != null ? books.size() : 0));
        return books;
    }

    // Lấy sách theo ID
    public Book getBookById(int bookId) {
        if (bookId < 0) {
            Log.w(TAG, "Cannot get book: invalid bookId: " + bookId);
            return null;
        }
        Book book = bookDAO.getBookById(bookId);
        Log.d(TAG, "Retrieved book with ID: " + bookId + ", Found: " + (book != null));
        return book;
    }

    // ========================
    // QUẢN LÝ YÊU THÍCH
    // ========================

    public void addFavorite(int bookId) {
        Set<String> favorites = getFavoriteSet();
        favorites.add(String.valueOf(bookId));
        saveFavoriteSet(favorites);
        Log.d(TAG, "Added book to favorites: " + bookId);
    }

    public void removeFavorite(int bookId) {
        Set<String> favorites = getFavoriteSet();
        favorites.remove(String.valueOf(bookId));
        saveFavoriteSet(favorites);
        Log.d(TAG, "Removed book from favorites: " + bookId);
    }

    public boolean isFavorite(int bookId) {
        boolean isFavorite = getFavoriteSet().contains(String.valueOf(bookId));
        Log.d(TAG, "Checked if book is favorite: " + bookId + ", Result: " + isFavorite);
        return isFavorite;
    }

    public List<Book> getFavoriteBooks() {
        List<Book> result = new ArrayList<>();
        Set<String> favoriteIds = getFavoriteSet();
        if (favoriteIds.isEmpty()) {
            Log.d(TAG, "No favorite books found");
            return result;
        }

        // Tối ưu: Lấy tất cả sách một lần rồi lọc
        List<Book> allBooks = getAllBooks();
        if (allBooks != null) {
            for (String idStr : favoriteIds) {
                try {
                    int id = Integer.parseInt(idStr);
                    for (Book book : allBooks) {
                        if (book.getBookId() == id) {
                            result.add(book);
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid favorite book ID: " + idStr, e);
                }
            }
        }
        Log.d(TAG, "Retrieved favorite books, count: " + result.size());
        return result;
    }

    private Set<String> getFavoriteSet() {
        Set<String> favorites = sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>());
        if (favorites == null) {
            Log.w(TAG, "Favorites set is null, returning empty set");
            return new HashSet<>();
        }
        return new HashSet<>(favorites);
    }

    private void saveFavoriteSet(Set<String> favorites) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_FAVORITES, favorites);
        editor.apply();
        Log.d(TAG, "Saved favorite set, size: " + favorites.size());
    }
}