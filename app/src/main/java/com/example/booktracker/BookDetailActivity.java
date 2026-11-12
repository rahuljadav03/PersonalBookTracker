package com.example.booktracker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.booktracker.database.DatabaseHelper;
import com.example.booktracker.databinding.ActivityBookDetailsBinding;
import com.example.booktracker.model.Book;
import com.example.booktracker.utils.BookValidator;
import com.example.booktracker.utils.Constants;
import com.example.booktracker.utils.DateTimeUtils;
import com.example.booktracker.utils.InsetsHelper;
import com.example.booktracker.utils.ToastHelper;
import com.example.booktracker.utils.ValidationResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookDetailActivity extends AppCompatActivity {

    private static final String KEY_IS_EDIT_MODE = "KEY_IS_EDIT_MODE";
    private static final String KEY_EDIT_TITLE = "KEY_EDIT_TITLE";
    private static final String KEY_EDIT_AUTHOR = "KEY_EDIT_AUTHOR";
    private static final String KEY_EDIT_CATEGORY = "KEY_EDIT_CATEGORY";
    private static final String KEY_EDIT_TOTAL_PAGES = "KEY_EDIT_TOTAL_PAGES";
    private static final String KEY_EDIT_PAGES_READ = "KEY_EDIT_PAGES_READ";
    private static final String KEY_EDIT_STATUS = "KEY_EDIT_STATUS";
    private static final String KEY_EDIT_DESCRIPTION = "KEY_EDIT_DESCRIPTION";

    private ActivityBookDetailsBinding binding;
    private DatabaseHelper dbHelper;
    private Book currentBook;
    private long bookId = -1;
    private boolean isEditMode = false;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String STATUS_LABEL_WANT_TO_READ = "Want to Read";
    private static final String STATUS_LABEL_READING = "Reading";
    private static final String STATUS_LABEL_COMPLETED = "Completed";

    private final List<String> statusLabels = Arrays.asList(
            STATUS_LABEL_WANT_TO_READ,
            STATUS_LABEL_READING,
            STATUS_LABEL_COMPLETED
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InsetsHelper.applySystemBarInsets(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        bookId = getIntent().getLongExtra(Constants.EXTRA_BOOK_ID, -1);
        if (bookId == -1) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.EXTRA_MESSAGE, getString(R.string.toast_book_not_found));
            startActivity(intent);
            finish();
            return;
        }

        if (savedInstanceState != null) {
            isEditMode = savedInstanceState.getBoolean(KEY_IS_EDIT_MODE, false);
        } else {
            isEditMode = getIntent().getBooleanExtra(Constants.EXTRA_EDIT_MODE, false);
        }

        setupDropdowns();
        setupTextWatchers();
        setupClickListeners();
        setupBackPressedHandler();

        loadBookDetailsAndInitialize(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_EDIT_MODE, isEditMode);
        if (isEditMode) {
            outState.putString(KEY_EDIT_TITLE, binding.etTitle.getText().toString());
            outState.putString(KEY_EDIT_AUTHOR, binding.etAuthor.getText().toString());
            outState.putString(KEY_EDIT_CATEGORY, binding.actvCategory.getText().toString());
            outState.putString(KEY_EDIT_TOTAL_PAGES, binding.etTotalPages.getText().toString());
            outState.putString(KEY_EDIT_PAGES_READ, binding.etPagesRead.getText().toString());
            outState.putString(KEY_EDIT_STATUS, binding.actvStatus.getText().toString());
            outState.putString(KEY_EDIT_DESCRIPTION, binding.etDescription.getText().toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndShowExtraToast();
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

    private void loadBookDetailsAndInitialize(Bundle savedInstanceState) {
        executorService.execute(() -> {
            currentBook = dbHelper.getBookById(bookId);
            mainHandler.post(() -> {
                if (currentBook == null) {
                    Intent intent = new Intent(BookDetailActivity.this, MainActivity.class);
                    intent.putExtra(Constants.EXTRA_MESSAGE, getString(R.string.toast_book_not_found_or_deleted));
                    startActivity(intent);
                    finish();
                    return;
                }

                if (isEditMode) {
                    enterEditMode();
                    if (savedInstanceState != null) {
                        restoreEditedFieldsFromSavedState(savedInstanceState);
                    }
                } else {
                    enterViewMode();
                }
            });
        });
    }

    private void restoreEditedFieldsFromSavedState(Bundle savedState) {
        if (savedState.containsKey(KEY_EDIT_TITLE)) {
            binding.etTitle.setText(savedState.getString(KEY_EDIT_TITLE, ""));
        }
        if (savedState.containsKey(KEY_EDIT_AUTHOR)) {
            binding.etAuthor.setText(savedState.getString(KEY_EDIT_AUTHOR, ""));
        }
        if (savedState.containsKey(KEY_EDIT_CATEGORY)) {
            binding.actvCategory.setText(savedState.getString(KEY_EDIT_CATEGORY, ""), false);
        }
        if (savedState.containsKey(KEY_EDIT_TOTAL_PAGES)) {
            binding.etTotalPages.setText(savedState.getString(KEY_EDIT_TOTAL_PAGES, ""));
        }
        if (savedState.containsKey(KEY_EDIT_PAGES_READ)) {
            binding.etPagesRead.setText(savedState.getString(KEY_EDIT_PAGES_READ, ""));
        }
        if (savedState.containsKey(KEY_EDIT_STATUS)) {
            binding.actvStatus.setText(savedState.getString(KEY_EDIT_STATUS, ""), false);
        }
        if (savedState.containsKey(KEY_EDIT_DESCRIPTION)) {
            binding.etDescription.setText(savedState.getString(KEY_EDIT_DESCRIPTION, ""));
        }
        validateForm(true);
    }

    private void populateViewModeData() {
        if (currentBook == null) return;
        binding.tvDetailsHeader.setText(R.string.title_book_details);
        binding.tvDetailTitle.setText(currentBook.getTitle());
        binding.tvDetailAuthor.setText("by " + currentBook.getAuthor());

        if (currentBook.getCategory() != null && !currentBook.getCategory().isEmpty()) {
            binding.chipDetailCategory.setVisibility(View.VISIBLE);
            binding.chipDetailCategory.setText(currentBook.getCategory());
        } else {
            binding.chipDetailCategory.setVisibility(View.GONE);
        }

        if (currentBook.getStatus() != null && !currentBook.getStatus().isEmpty()) {
            binding.chipDetailStatus.setVisibility(View.VISIBLE);
            binding.chipDetailStatus.setText(mapStatusCodeToLabel(currentBook.getStatus()));
            applyStatusBadgeColor(currentBook.getStatus());
        } else {
            binding.chipDetailStatus.setVisibility(View.GONE);
        }

        int totalPages = currentBook.getTotalPages();
        int pagesRead = currentBook.getPagesRead();
        int progressPercent = totalPages > 0 ? (int) (((double) pagesRead / totalPages) * 100) : 0;

        binding.progressDetailPages.setTrackStopIndicatorSize(0);
        binding.progressDetailPages.setProgress(progressPercent);
        binding.tvDetailProgressText.setText(pagesRead + " of " + totalPages + " pages (" + progressPercent + "%)");

        if (currentBook.getDescription() != null && !currentBook.getDescription().isEmpty()) {
            binding.cardDescription.setVisibility(View.VISIBLE);
            binding.tvDetailDescription.setText(currentBook.getDescription());
        } else {
            binding.cardDescription.setVisibility(View.GONE);
        }

        binding.tvDetailCreatedAt.setText(getString(R.string.added_on_format, DateTimeUtils.formatIst(currentBook.getCreatedAt())));
        binding.tvDetailUpdatedAt.setText(getString(R.string.last_updated_format, DateTimeUtils.formatIst(currentBook.getUpdatedAt())));
    }

    private void setupDropdowns() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Constants.CATEGORIES
        );
        binding.actvCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusLabels
        );
        binding.actvStatus.setAdapter(statusAdapter);
    }

    private void setupTextWatchers() {
        TextWatcher commonWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditMode) {
                    validateForm(true);
                }
            }
        };

        binding.etTitle.addTextChangedListener(commonWatcher);
        binding.etAuthor.addTextChangedListener(commonWatcher);
        binding.etDescription.addTextChangedListener(commonWatcher);
        binding.etPagesRead.addTextChangedListener(commonWatcher);

        binding.actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            if (isEditMode) validateForm(true);
        });

        binding.etTotalPages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditMode) {
                    String statusLabel = binding.actvStatus.getText().toString();
                    if (STATUS_LABEL_COMPLETED.equals(statusLabel)) {
                        binding.etPagesRead.setText(s.toString());
                    }
                    validateForm(true);
                }
            }
        });

        binding.actvStatus.setOnItemClickListener((parent, view, position, id) -> {
            String selectedStatusLabel = statusLabels.get(position);
            handleStatusChange(selectedStatusLabel);
            if (isEditMode) validateForm(true);
        });
    }

    private void handleStatusChange(String statusLabel) {
        if (STATUS_LABEL_WANT_TO_READ.equals(statusLabel)) {
            binding.etPagesRead.setText("0");
        } else if (STATUS_LABEL_COMPLETED.equals(statusLabel)) {
            String totalPagesStr = binding.etTotalPages.getText().toString().trim();
            if (!totalPagesStr.isEmpty()) {
                binding.etPagesRead.setText(totalPagesStr);
            }
        }
    }

    private void setupClickListeners() {
        binding.btnEditBook.setOnClickListener(v -> enterEditMode());
        binding.btnCancelEdit.setOnClickListener(v -> {
            if (hasUnsavedChanges()) {
                showUnsavedChangesDialog(this::enterViewMode);
            } else {
                enterViewMode();
            }
        });

        binding.btnSaveBook.setOnClickListener(v -> saveBookUpdate());
        binding.btnDeleteBook.setOnClickListener(v -> showDeleteConfirmationDialog());
        binding.btnBackToDashboard.setOnClickListener(v -> navigateToDashboard());
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToDashboard();
            }
        });
    }

    private void navigateToDashboard() {
        if (isEditMode && hasUnsavedChanges()) {
            showUnsavedChangesDialog(this::performDashboardNavigation);
        } else {
            performDashboardNavigation();
        }
    }

    private void performDashboardNavigation() {
        Intent intent = new Intent(BookDetailActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void enterViewMode() {
        isEditMode = false;
        binding.layoutViewMode.setVisibility(View.VISIBLE);
        binding.layoutEditMode.setVisibility(View.GONE);
        populateViewModeData();
    }

    private void enterEditMode() {
        if (currentBook == null) return;

        isEditMode = true;
        binding.layoutViewMode.setVisibility(View.GONE);
        binding.layoutEditMode.setVisibility(View.VISIBLE);

        binding.tvDetailsHeader.setText(R.string.title_edit_book);
        binding.btnSaveBook.setText("Update Book");

        binding.etTitle.setText(currentBook.getTitle());
        binding.etAuthor.setText(currentBook.getAuthor());
        binding.actvCategory.setText(currentBook.getCategory(), false);
        binding.etTotalPages.setText(String.valueOf(currentBook.getTotalPages()));
        binding.etPagesRead.setText(String.valueOf(currentBook.getPagesRead()));
        binding.actvStatus.setText(mapStatusCodeToLabel(currentBook.getStatus()), false);
        binding.etDescription.setText(currentBook.getDescription() != null ? currentBook.getDescription() : "");

        validateForm(false);
    }

    private boolean hasUnsavedChanges() {
        if (currentBook == null) return false;

        String title = binding.etTitle.getText().toString().trim();
        String author = binding.etAuthor.getText().toString().trim();
        String category = binding.actvCategory.getText().toString().trim();
        String totalPagesStr = binding.etTotalPages.getText().toString().trim();
        String pagesReadStr = binding.etPagesRead.getText().toString().trim();
        String statusLabel = binding.actvStatus.getText().toString().trim();
        String statusCode = mapStatusLabelToCode(statusLabel);
        String description = binding.etDescription.getText().toString().trim();

        int totalPages = parseInteger(totalPagesStr, currentBook.getTotalPages());
        int pagesRead = parseInteger(pagesReadStr, currentBook.getPagesRead());
        String origDescription = currentBook.getDescription() != null ? currentBook.getDescription().trim() : "";

        return !title.equals(currentBook.getTitle()) ||
                !author.equals(currentBook.getAuthor()) ||
                !category.equals(currentBook.getCategory()) ||
                totalPages != currentBook.getTotalPages() ||
                pagesRead != currentBook.getPagesRead() ||
                !statusCode.equals(currentBook.getStatus()) ||
                !description.equals(origDescription);
    }

    private int parseInteger(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean validateForm(boolean showErrors) {
        boolean isValid = true;

        // Title
        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            if (showErrors) binding.tilTitle.setError("Title is required");
            isValid = false;
        } else if (title.length() < Constants.TITLE_MIN_LENGTH || title.length() > Constants.TITLE_MAX_LENGTH) {
            if (showErrors) binding.tilTitle.setError("Title must be between 2 and 100 characters");
            isValid = false;
        } else {
            if (showErrors) binding.tilTitle.setError(null);
        }

        // Author
        String author = binding.etAuthor.getText().toString().trim();
        if (author.isEmpty()) {
            if (showErrors) binding.tilAuthor.setError("Author is required");
            isValid = false;
        } else if (author.length() < Constants.AUTHOR_MIN_LENGTH || author.length() > Constants.AUTHOR_MAX_LENGTH) {
            if (showErrors) binding.tilAuthor.setError("Author must be between 2 and 60 characters");
            isValid = false;
        } else if (!author.matches("^[a-zA-Z\\s\\.'\\-]+$")) {
            if (showErrors) binding.tilAuthor.setError("Author contains invalid characters");
            isValid = false;
        } else {
            if (showErrors) binding.tilAuthor.setError(null);
        }

        // Category
        String category = binding.actvCategory.getText().toString().trim();
        if (category.isEmpty() || !Constants.CATEGORIES.contains(category)) {
            if (showErrors) binding.tilCategory.setError("Please select a valid category");
            isValid = false;
        } else {
            if (showErrors) binding.tilCategory.setError(null);
        }

        // Total Pages
        String totalPagesStr = binding.etTotalPages.getText().toString().trim();
        int totalPages = 0;
        if (totalPagesStr.isEmpty()) {
            if (showErrors) binding.tilTotalPages.setError("Total pages is required");
            isValid = false;
        } else {
            try {
                totalPages = Integer.parseInt(totalPagesStr);
                if (totalPages < Constants.MIN_PAGES || totalPages > Constants.MAX_PAGES) {
                    if (showErrors) binding.tilTotalPages.setError("Total pages must be between 1 and 10000");
                    isValid = false;
                } else {
                    if (showErrors) binding.tilTotalPages.setError(null);
                }
            } catch (NumberFormatException e) {
                if (showErrors) binding.tilTotalPages.setError("Invalid number");
                isValid = false;
            }
        }

        // Pages Read & Cross-Field Status Rules
        String statusLabel = binding.actvStatus.getText().toString().trim();
        String pagesReadStr = binding.etPagesRead.getText().toString().trim();
        int pagesRead = -1;

        if (statusLabel.isEmpty()) {
            if (showErrors) binding.tilStatus.setError("Please select a status");
            isValid = false;
        } else {
            if (showErrors) binding.tilStatus.setError(null);
        }

        if (pagesReadStr.isEmpty()) {
            if (showErrors) binding.tilPagesRead.setError("Pages read is required");
            isValid = false;
        } else {
            try {
                pagesRead = Integer.parseInt(pagesReadStr);
                if (pagesRead < 0 || (totalPages > 0 && pagesRead > totalPages)) {
                    if (showErrors) binding.tilPagesRead.setError("Pages read must be between 0 and total pages");
                    isValid = false;
                } else if (STATUS_LABEL_WANT_TO_READ.equals(statusLabel) && pagesRead != 0) {
                    if (showErrors) binding.tilPagesRead.setError("'Want to Read' books must have 0 pages read");
                    isValid = false;
                } else if (STATUS_LABEL_COMPLETED.equals(statusLabel) && pagesRead != totalPages) {
                    if (showErrors) binding.tilPagesRead.setError("'Completed' books must have pages read equal to total pages");
                    isValid = false;
                } else {
                    if (showErrors) binding.tilPagesRead.setError(null);
                }
            } catch (NumberFormatException e) {
                if (showErrors) binding.tilPagesRead.setError("Invalid number");
                isValid = false;
            }
        }

        // Description
        String description = binding.etDescription.getText().toString().trim();
        if (!description.isEmpty() && description.length() > Constants.DESCRIPTION_MAX_LENGTH) {
            if (showErrors) binding.tilDescription.setError("Description cannot exceed 500 characters");
            isValid = false;
        } else {
            if (showErrors) binding.tilDescription.setError(null);
        }

        binding.btnSaveBook.setEnabled(isValid);
        return isValid;
    }

    private void saveBookUpdate() {
        if (!hasUnsavedChanges()) {
            ToastHelper.show(this, R.string.toast_no_changes_to_save);
            enterViewMode();
            return;
        }

        if (!validateForm(true)) {
            ToastHelper.show(this, R.string.toast_please_correct_errors);
            return;
        }

        String title = binding.etTitle.getText().toString().trim();
        String author = binding.etAuthor.getText().toString().trim();
        String category = binding.actvCategory.getText().toString().trim();
        int totalPages = Integer.parseInt(binding.etTotalPages.getText().toString().trim());
        int pagesRead = Integer.parseInt(binding.etPagesRead.getText().toString().trim());
        String statusLabel = binding.actvStatus.getText().toString().trim();
        String statusCode = mapStatusLabelToCode(statusLabel);
        String description = binding.etDescription.getText().toString().trim();

        executorService.execute(() -> {
            if (dbHelper.isDuplicateTitleAndAuthor(title, author, currentBook.getId())) {
                mainHandler.post(() -> {
                    binding.tilTitle.setError("A book with this title and author already exists.");
                    binding.tilAuthor.setError("A book with this title and author already exists.");
                    ToastHelper.show(BookDetailActivity.this, R.string.toast_error_duplicate_book);
                });
                return;
            }

            Book updatedBook = new Book(title, author, category, totalPages, pagesRead, statusCode, description);
            updatedBook.setId(currentBook.getId());
            updatedBook.setCreatedAt(currentBook.getCreatedAt());

            ValidationResult validationResult = BookValidator.validate(updatedBook);
            if (!validationResult.isValid()) {
                mainHandler.post(() -> {
                    displayValidationErrors(validationResult);
                    ToastHelper.show(BookDetailActivity.this, R.string.toast_validation_failed);
                });
                return;
            }

            try {
                int rowsUpdated = dbHelper.updateBook(updatedBook);
                Book reloadedBook = dbHelper.getBookById(currentBook.getId());
                mainHandler.post(() -> {
                    if (rowsUpdated > 0 && reloadedBook != null) {
                        ToastHelper.show(BookDetailActivity.this, R.string.toast_book_updated_success);
                        currentBook = reloadedBook;
                        enterViewMode();
                    } else {
                        ToastHelper.show(BookDetailActivity.this, R.string.toast_failed_to_update_book);
                    }
                });
            } catch (SQLiteConstraintException e) {
                mainHandler.post(() -> {
                    binding.tilTitle.setError("A book with this title and author already exists.");
                    binding.tilAuthor.setError("A book with this title and author already exists.");
                    ToastHelper.show(BookDetailActivity.this, R.string.toast_error_duplicate_book);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    ToastHelper.show(BookDetailActivity.this, getString(R.string.toast_failed_to_update_book) + ": " + e.getMessage());
                });
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        if (currentBook == null) return;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + currentBook.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, which) -> {
                    executorService.execute(() -> {
                        dbHelper.deleteBook(currentBook.getId());
                        mainHandler.post(() -> {
                            Intent intent = new Intent(BookDetailActivity.this, MainActivity.class);
                            intent.putExtra(Constants.EXTRA_MESSAGE, getString(R.string.toast_book_deleted_success));
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(BookDetailActivity.this, R.color.delete_red));
        });

        dialog.show();
    }

    private void showUnsavedChangesDialog(Runnable onDiscard) {
        new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Discard changes and leave?")
                .setPositiveButton("Discard", (dialog, which) -> {
                    if (onDiscard != null) {
                        onDiscard.run();
                    }
                })
                .setNegativeButton("Keep Editing", null)
                .show();
    }

    private void displayValidationErrors(ValidationResult result) {
        if (result.hasError("title")) binding.tilTitle.setError(result.getError("title"));
        if (result.hasError("author")) binding.tilAuthor.setError(result.getError("author"));
        if (result.hasError("category")) binding.tilCategory.setError(result.getError("category"));
        if (result.hasError("totalPages")) binding.tilTotalPages.setError(result.getError("totalPages"));
        if (result.hasError("pagesRead")) binding.tilPagesRead.setError(result.getError("pagesRead"));
        if (result.hasError("status")) binding.tilStatus.setError(result.getError("status"));
        if (result.hasError("description")) binding.tilDescription.setError(result.getError("description"));
    }

    private void applyStatusBadgeColor(String status) {
        int backgroundColor;
        int textColor;

        if (Constants.STATUS_WANT_TO_READ.equals(status)) {
            backgroundColor = Color.parseColor("#E3F2FD"); // Soft Blue
            textColor = Color.parseColor("#0D47A1");
        } else if (Constants.STATUS_READING.equals(status)) {
            backgroundColor = Color.parseColor("#E8F5E9"); // Soft Green
            textColor = Color.parseColor("#1B5E20");
        } else if (Constants.STATUS_COMPLETED.equals(status)) {
            backgroundColor = Color.parseColor("#F3E5F5"); // Soft Purple
            textColor = Color.parseColor("#4A148C");
        } else {
            backgroundColor = Color.parseColor("#EEEEEE");
            textColor = Color.parseColor("#212121");
        }

        binding.chipDetailStatus.setChipBackgroundColor(ColorStateList.valueOf(backgroundColor));
        binding.chipDetailStatus.setTextColor(textColor);
    }

    private String mapStatusCodeToLabel(String code) {
        if (Constants.STATUS_WANT_TO_READ.equals(code)) {
            return STATUS_LABEL_WANT_TO_READ;
        } else if (Constants.STATUS_READING.equals(code)) {
            return STATUS_LABEL_READING;
        } else if (Constants.STATUS_COMPLETED.equals(code)) {
            return STATUS_LABEL_COMPLETED;
        }
        return STATUS_LABEL_WANT_TO_READ;
    }

    private String mapStatusLabelToCode(String label) {
        if (STATUS_LABEL_WANT_TO_READ.equals(label)) {
            return Constants.STATUS_WANT_TO_READ;
        } else if (STATUS_LABEL_READING.equals(label)) {
            return Constants.STATUS_READING;
        } else if (STATUS_LABEL_COMPLETED.equals(label)) {
            return Constants.STATUS_COMPLETED;
        }
        return Constants.STATUS_WANT_TO_READ;
    }
}
