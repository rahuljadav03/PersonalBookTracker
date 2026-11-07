package com.example.booktracker.data;

import java.util.Objects;

public class Book {
    private long id;
    private String title;
    private String author;
    private String status;
    private long createdAt;
    private long updatedAt;

    public Book() {
    }

    public Book(long id, String title, String author, String status, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return id == book.id &&
                createdAt == book.createdAt &&
                updatedAt == book.updatedAt &&
                Objects.equals(title, book.title) &&
                Objects.equals(author, book.author) &&
                Objects.equals(status, book.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, author, status, createdAt, updatedAt);
    }
}
