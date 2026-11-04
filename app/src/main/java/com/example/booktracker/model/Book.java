package com.example.booktracker.model;

public class Book {
    private long id;
    private String title;
    private String author;
    private String category;
    private int totalPages;
    private int pagesRead;
    private String status;
    private String description;
    private long createdAt;
    private long updatedAt;

    // Empty constructor
    public Book() {}

    public Book(String title, String author, String category, int totalPages, int pagesRead, String status, String description) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalPages = totalPages;
        this.pagesRead = pagesRead;
        this.status = status;
        this.description = description;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public int getPagesRead() { return pagesRead; }
    public void setPagesRead(int pagesRead) { this.pagesRead = pagesRead; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}