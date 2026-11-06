package com.example.booktracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booktracker.database.DatabaseHelper;
import com.example.booktracker.databinding.ActivityStatisticsBinding;
import com.example.booktracker.utils.Constants;
import com.example.booktracker.utils.InsetsHelper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private DatabaseHelper dbHelper;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InsetsHelper.applySystemBarInsets(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatisticsData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void loadStatisticsData() {
        executorService.execute(() -> {
            // Execute SQL aggregate queries on background thread (COUNT / SUM)
            int totalBooks = dbHelper.getTotalBooksCount();
            int completedCount = dbHelper.getBooksCountByStatus(Constants.STATUS_COMPLETED);
            int readingCount = dbHelper.getBooksCountByStatus(Constants.STATUS_READING);
            int wantToReadCount = dbHelper.getBooksCountByStatus(Constants.STATUS_WANT_TO_READ);

            int totalPagesRead = dbHelper.getTotalPagesRead();
            Map<String, Integer> categoryCounts = dbHelper.getCategoryCounts();

            // Calculate percentages & averages safely handling zero-books case
            int completionRate = totalBooks > 0 ? (int) Math.round(((double) completedCount / totalBooks) * 100) : 0;
            int readingRate = totalBooks > 0 ? (int) Math.round(((double) readingCount / totalBooks) * 100) : 0;
            int wantToReadRate = totalBooks > 0 ? (int) Math.round(((double) wantToReadCount / totalBooks) * 100) : 0;
            int avgPages = totalBooks > 0 ? (int) Math.round((double) totalPagesRead / totalBooks) : 0;

            mainHandler.post(() -> updateUi(
                    totalBooks,
                    completedCount,
                    readingCount,
                    wantToReadCount,
                    totalPagesRead,
                    completionRate,
                    readingRate,
                    wantToReadRate,
                    avgPages,
                    categoryCounts
            ));
        });
    }

    private void updateUi(
            int totalBooks,
            int completedCount,
            int readingCount,
            int wantToReadCount,
            int totalPagesRead,
            int completionRate,
            int readingRate,
            int wantToReadRate,
            int avgPages,
            Map<String, Integer> categoryCounts
    ) {
        // Metric Cards
        binding.tvStatTotalBooks.setText(String.valueOf(totalBooks));
        binding.tvStatTotalPages.setText(String.format(java.util.Locale.getDefault(), "%,d", totalPagesRead));
        binding.tvStatCompletionRate.setText(completionRate + "%");
        binding.tvStatAvgPages.setText(String.valueOf(avgPages));

        // Reading Status Breakdown & Progress Bar Visualizations
        binding.tvStatCompletedText.setText("Completed: " + completedCount + " books (" + completionRate + "%)");
        binding.progressStatCompleted.setProgress(completionRate);

        binding.tvStatReadingText.setText("Currently Reading: " + readingCount + " books (" + readingRate + "%)");
        binding.progressStatReading.setProgress(readingRate);

        binding.tvStatWantToReadText.setText("Want to Read: " + wantToReadCount + " books (" + wantToReadRate + "%)");
        binding.progressStatWantToRead.setProgress(wantToReadRate);

        // Category Distribution Breakdown
        binding.layoutCategoryItems.removeAllViews();
        if (categoryCounts.isEmpty()) {
            TextView emptyCategoryTv = new TextView(this);
            emptyCategoryTv.setText("No books in library");
            emptyCategoryTv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            binding.layoutCategoryItems.addView(emptyCategoryTv);
        } else {
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                TextView itemTv = new TextView(this);
                itemTv.setText(entry.getKey() + ": " + entry.getValue() + " book" + (entry.getValue() > 1 ? "s" : ""));
                itemTv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
                itemTv.setPadding(0, 4, 0, 4);
                binding.layoutCategoryItems.addView(itemTv);
            }
        }
    }
}
