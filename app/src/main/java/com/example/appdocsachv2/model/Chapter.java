package com.example.appdocsachv2.model;

public class Chapter {
    private int chapterId;
    private int bookId;
    private String title;
    private int startPage;
    private int endPage;

    public Chapter() {
    }
    public Chapter(int bookId, String title, int startPage, int endPage) {
        this.bookId = bookId;
        this.title = title;
        this.startPage = startPage;
        this.endPage = endPage;
    }
    public Chapter(int chapterId, int bookId, String title, int startPage, int endPage) {
        this.chapterId = chapterId;
        this.bookId = bookId;
        this.title = title;
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }
}
