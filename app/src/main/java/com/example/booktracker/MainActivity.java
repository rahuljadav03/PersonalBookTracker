package com.example.booktracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.booktracker.adapter.BookAdapter;
import com.example.booktracker.database.DatabaseHelper;
import com.example.booktracker.databinding.ActivityMainBinding;
import com.example.booktracker.model.Book;
import com.example.booktracker.utils.Constants;
import com.example.booktracker.utils.InsetsHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private BookAdapter recentBooksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        refreshDashboardData();
    }

    private void setupRecyclerView() {
        recentBooksAdapter = new BookAdapter(book -> {
            Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            startActivity(intent);
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
        int totalCount = dbHelper.getTotalBooksCount();
        int wantToReadCount = dbHelper.getBooksCountByStatus(Constants.STATUS_WANT_TO_READ);
        int readingCount = dbHelper.getBooksCountByStatus(Constants.STATUS_READING);
        int completedCount = dbHelper.getBooksCountByStatus(Constants.STATUS_COMPLETED);

        binding.tvTotalBooksCount.setText(String.valueOf(totalCount));
        binding.tvWantToReadCount.setText(String.valueOf(wantToReadCount));
        binding.tvReadingCount.setText(String.valueOf(readingCount));
        binding.tvCompletedCount.setText(String.valueOf(completedCount));

        List<Book> recentBooks = dbHelper.getRecentBooks(5);
        recentBooksAdapter.setBooks(recentBooks);
    }
}
