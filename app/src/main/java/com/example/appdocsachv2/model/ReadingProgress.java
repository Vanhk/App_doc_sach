package com.example.appdocsachv2.model;

public class ReadingProgress {
    private int id;
    private int userId;
    private int bookId;
    private int currentPage;

    public ReadingProgress() {
    }

    public ReadingProgress(int id, int userId, int bookId, int currentPage) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.currentPage = currentPage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
