package com.example.appdocsachv2.model;

public class Book {
    private int bookId;
    private String title;
    private String author;
    private String genre;
    private String filePath;
    private String coverImage;
    private int total_pages;
    private String summary;

    public Book() {
    }

    public Book(int bookId, String title, String author, String genre, String filePath, String coverImage, int total_pages,String summary) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.filePath = filePath;
        this.coverImage = coverImage;
        this.total_pages = total_pages;
        this.summary = summary;

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(int total_pages) {
        this.total_pages = total_pages;
    }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
