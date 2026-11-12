package com.example.booktracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.booktracker.adapter.BookAdapter;
import com.example.booktracker.database.DatabaseHelper;
import com.example.booktracker.databinding.ActivityMainBinding;
import com.example.booktracker.model.Book;
import com.example.booktracker.utils.Constants;
import com.example.booktracker.utils.InsetsHelper;
import com.example.booktracker.utils.ToastHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private BookAdapter recentBooksAdapter;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InsetsHelper.applySystemBarInsets(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        setupRecyclerView();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndShowExtraToast();
        refreshDashboardData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        recentBooksAdapter = new BookAdapter(new BookAdapter.OnBookActionListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra(Constants.EXTRA_BOOK_ID, book.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Book book) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra(Constants.EXTRA_BOOK_ID, book.getId());
                intent.putExtra(Constants.EXTRA_EDIT_MODE, true);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Book book) {
                showDeleteConfirmationDialog(book);
            }
        });

        binding.rvRecentBooks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentBooks.setAdapter(recentBooksAdapter);
    }

    private void setupClickListeners() {
        binding.btnViewAllBooks.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BookListActivity.class);
            startActivity(intent);
        });

        binding.btnViewStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        binding.fabAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddBookActivity.class);
            startActivity(intent);
        });
    }

    private void refreshDashboardData() {
        executorService.execute(() -> {
            int totalCount = dbHelper.getTotalBooksCount();
            int wantToReadCount = dbHelper.getBooksCountByStatus(Constants.STATUS_WANT_TO_READ);
            int readingCount = dbHelper.getBooksCountByStatus(Constants.STATUS_READING);
            int completedCount = dbHelper.getBooksCountByStatus(Constants.STATUS_COMPLETED);
            List<Book> recentBooks = dbHelper.getRecentBooks(5);

            mainHandler.post(() -> {
                binding.tvTotalBooksCount.setText(String.valueOf(totalCount));
                binding.tvWantToReadCount.setText(String.valueOf(wantToReadCount));
                binding.tvReadingCount.setText(String.valueOf(readingCount));
                binding.tvCompletedCount.setText(String.valueOf(completedCount));

                recentBooksAdapter.setBooks(recentBooks);

                if (recentBooks.isEmpty()) {
                    binding.rvRecentBooks.setVisibility(View.GONE);
                } else {
                    binding.rvRecentBooks.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void showDeleteConfirmationDialog(Book book) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, which) -> {
                    executorService.execute(() -> {
                        dbHelper.deleteBook(book.getId());
                        mainHandler.post(() -> {
                            ToastHelper.show(MainActivity.this, R.string.toast_book_deleted);
                            refreshDashboardData();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(MainActivity.this, R.color.delete_red));
        });

        dialog.show();
    }
}
