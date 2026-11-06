package com.example.booktracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.booktracker.adapter.BookAdapter;
import com.example.booktracker.database.DatabaseHelper;
import com.example.booktracker.databinding.ActivityBookListBinding;
import com.example.booktracker.model.Book;
import com.example.booktracker.utils.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookListActivity extends AppCompatActivity {

    private ActivityBookListBinding binding;
    private DatabaseHelper dbHelper;
    private BookAdapter bookAdapter;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String currentSearchQuery = "";
    private String currentStatusFilter = "ALL";
    private String currentSortBy = "RECENT";

    private final List<String> sortOptions = Arrays.asList(
            "Recently Added",
            "Title (A-Z)",
            "Author (A-Z)"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        setupRecyclerView();
        setupFiltersAndSearch();
        setupSortDropdown();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooksFromDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(new BookAdapter.OnBookActionListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
                intent.putExtra("book_id", book.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Book book) {
                Intent intent = new Intent(BookListActivity.this, AddBookActivity.class);
                intent.putExtra("book_id", book.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Book book) {
                showDeleteConfirmationDialog(book);
            }
        });

        binding.rvBooks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBooks.setAdapter(bookAdapter);
    }

    private void setupFiltersAndSearch() {
        // Search watcher
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                loadBooksFromDatabase();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter chips
        binding.cgStatusFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatusFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_filter_want_to_read) {
                    currentStatusFilter = Constants.STATUS_WANT_TO_READ;
                } else if (checkedId == R.id.chip_filter_reading) {
                    currentStatusFilter = Constants.STATUS_READING;
                } else if (checkedId == R.id.chip_filter_completed) {
                    currentStatusFilter = Constants.STATUS_COMPLETED;
                } else {
                    currentStatusFilter = "ALL";
                }
            }
            loadBooksFromDatabase();
        });
    }

    private void setupSortDropdown() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sortOptions
        );
        binding.actvSort.setAdapter(sortAdapter);
        binding.actvSort.setText("Recently Added", false);

        binding.actvSort.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 1) {
                currentSortBy = "TITLE_ASC";
            } else if (position == 2) {
                currentSortBy = "AUTHOR_ASC";
            } else {
                currentSortBy = "RECENT";
            }
            loadBooksFromDatabase();
        });
    }

    private void setupClickListeners() {
        binding.fabAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(BookListActivity.this, AddBookActivity.class);
            startActivity(intent);
        });

        binding.btnEmptyAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(BookListActivity.this, AddBookActivity.class);
            startActivity(intent);
        });
    }

    private void loadBooksFromDatabase() {
        executorService.execute(() -> {
            List<Book> books = dbHelper.getFilteredBooks(currentSearchQuery, currentStatusFilter, currentSortBy);
            mainHandler.post(() -> {
                bookAdapter.submitList(books, () -> updateEmptyStateVisibility(books.isEmpty()));
            });
        });
    }

    private void updateEmptyStateVisibility(boolean isEmpty) {
        if (isEmpty) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvBooks.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvBooks.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmationDialog(Book book) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executorService.execute(() -> {
                        dbHelper.deleteBook(book.getId());
                        mainHandler.post(() -> {
                            Toast.makeText(BookListActivity.this, "Book deleted", Toast.LENGTH_SHORT).show();
                            loadBooksFromDatabase();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
