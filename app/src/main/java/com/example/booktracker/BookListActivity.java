package com.example.booktracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.booktracker.adapter.BookAdapter;
import com.example.booktracker.database.DatabaseHelper;
import com.example.booktracker.databinding.ActivityBookListBinding;
import com.example.booktracker.model.Book;
import com.example.booktracker.utils.Constants;
import com.example.booktracker.utils.InsetsHelper;
import com.example.booktracker.utils.ToastHelper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookListActivity extends AppCompatActivity {

    private static final String KEY_SEARCH_QUERY = "KEY_SEARCH_QUERY";
    private static final String KEY_STATUS_FILTER = "KEY_STATUS_FILTER";
    private static final String KEY_SORT_BY = "KEY_SORT_BY";

    private ActivityBookListBinding binding;
    private DatabaseHelper dbHelper;
    private BookAdapter bookAdapter;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String currentSearchQuery = "";
    private String currentStatusFilter = "ALL";
    private String currentSortBy = "RECENT";

    private static final long DEBOUNCE_DELAY_MS = 300;
    private final Runnable searchRunnable = this::loadBooksFromDatabase;

    private final List<String> sortOptions = Arrays.asList(
            "Recently Added",
            "Title (A-Z)",
            "Title (Z-A)",
            "Author (A-Z)",
            "Author (Z-A)"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityBookListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InsetsHelper.applySystemBarInsets(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        if (savedInstanceState != null) {
            currentSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
            currentStatusFilter = savedInstanceState.getString(KEY_STATUS_FILTER, "ALL");
            currentSortBy = savedInstanceState.getString(KEY_SORT_BY, "RECENT");
        }

        setupRecyclerView();
        setupFiltersAndSearch();
        setupSortDropdown();
        setupClickListeners();
        restoreUiStates();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_QUERY, currentSearchQuery);
        outState.putString(KEY_STATUS_FILTER, currentStatusFilter);
        outState.putString(KEY_SORT_BY, currentSortBy);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndShowExtraToast();
        loadBooksFromDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacks(searchRunnable);
        executorService.shutdown();
    }

    private void checkAndShowExtraToast() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.EXTRA_MESSAGE)) {
            String message = intent.getStringExtra(Constants.EXTRA_MESSAGE);
            if (message != null && !message.isEmpty()) {
                ToastHelper.show(this, message);
                intent.removeExtra(Constants.EXTRA_MESSAGE);
            }
        }
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(new BookAdapter.OnBookActionListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
                intent.putExtra(Constants.EXTRA_BOOK_ID, book.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Book book) {
                Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
                intent.putExtra(Constants.EXTRA_BOOK_ID, book.getId());
                intent.putExtra(Constants.EXTRA_EDIT_MODE, true);
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
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                mainHandler.removeCallbacks(searchRunnable);
                mainHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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

        binding.actvSort.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 1) {
                currentSortBy = "TITLE_ASC";
            } else if (position == 2) {
                currentSortBy = "TITLE_DESC";
            } else if (position == 3) {
                currentSortBy = "AUTHOR_ASC";
            } else if (position == 4) {
                currentSortBy = "AUTHOR_DESC";
            } else {
                currentSortBy = "RECENT";
            }
            loadBooksFromDatabase();
        });
    }

    private void restoreUiStates() {
        if (!currentSearchQuery.equals(binding.etSearch.getText().toString())) {
            binding.etSearch.setText(currentSearchQuery);
        }

        if (Constants.STATUS_WANT_TO_READ.equals(currentStatusFilter)) {
            binding.cgStatusFilter.check(R.id.chip_filter_want_to_read);
        } else if (Constants.STATUS_READING.equals(currentStatusFilter)) {
            binding.cgStatusFilter.check(R.id.chip_filter_reading);
        } else if (Constants.STATUS_COMPLETED.equals(currentStatusFilter)) {
            binding.cgStatusFilter.check(R.id.chip_filter_completed);
        } else {
            binding.cgStatusFilter.check(R.id.chip_filter_all);
        }

        String selectedSortLabel;
        if ("TITLE_ASC".equals(currentSortBy)) {
            selectedSortLabel = sortOptions.get(1);
        } else if ("TITLE_DESC".equals(currentSortBy)) {
            selectedSortLabel = sortOptions.get(2);
        } else if ("AUTHOR_ASC".equals(currentSortBy)) {
            selectedSortLabel = sortOptions.get(3);
        } else if ("AUTHOR_DESC".equals(currentSortBy)) {
            selectedSortLabel = sortOptions.get(4);
        } else {
            selectedSortLabel = sortOptions.get(0);
        }
        binding.actvSort.setText(selectedSortLabel, false);
    }

    private void setupClickListeners() {
        binding.fabAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(BookListActivity.this, AddBookActivity.class);
            startActivity(intent);
        });

        binding.btnEmptyAddBook.setOnClickListener(v -> {
            int totalInDb = dbHelper.getTotalBooksCount();
            if (totalInDb > 0) {
                binding.etSearch.setText("");
                binding.cgStatusFilter.check(R.id.chip_filter_all);
                binding.actvSort.setText(sortOptions.get(0), false);
                currentSearchQuery = "";
                currentStatusFilter = "ALL";
                currentSortBy = "RECENT";
                loadBooksFromDatabase();
            } else {
                Intent intent = new Intent(BookListActivity.this, AddBookActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadBooksFromDatabase() {
        executorService.execute(() -> {
            List<Book> books = dbHelper.getFilteredBooks(currentSearchQuery, currentStatusFilter, currentSortBy);
            int totalCountInDb = dbHelper.getTotalBooksCount();
            mainHandler.post(() -> {
                bookAdapter.submitList(books, () -> updateEmptyStateVisibility(books.isEmpty(), totalCountInDb));
            });
        });
    }

    private void updateEmptyStateVisibility(boolean isEmptyResult, int totalInDb) {
        if (isEmptyResult) {
            binding.rvBooks.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);

            if (totalInDb == 0) {
                binding.btnEmptyAddBook.setText(R.string.empty_state_button);
                binding.btnEmptyAddBook.setIconResource(R.drawable.ic_add);
            } else {
                binding.btnEmptyAddBook.setText(R.string.no_results_button);
                binding.btnEmptyAddBook.setIconResource(R.drawable.ic_search);
            }
        } else {
            binding.rvBooks.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmationDialog(Book book) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, which) -> {
                    executorService.execute(() -> {
                        dbHelper.deleteBook(book.getId());
                        mainHandler.post(() -> {
                            ToastHelper.show(BookListActivity.this, R.string.toast_book_deleted);
                            loadBooksFromDatabase();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(BookListActivity.this, R.color.delete_red));
        });

        dialog.show();
    }
}
