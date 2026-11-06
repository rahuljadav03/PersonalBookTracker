package com.example.booktracker;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.booktracker.database.DatabaseHelper;
import com.example.booktracker.databinding.ActivityAddBookBinding;
import com.example.booktracker.model.Book;
import com.example.booktracker.utils.BookValidator;
import com.example.booktracker.utils.Constants;
import com.example.booktracker.utils.ValidationResult;

import java.util.Arrays;
import java.util.List;

public class AddBookActivity extends AppCompatActivity {

    private ActivityAddBookBinding binding;
    private DatabaseHelper dbHelper;

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
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        setupDropdowns();
        setupTextWatchers();
        setupClickListeners();

        // Initial validation check to set Save button state
        validateForm(false);
    }

    private void setupDropdowns() {
        // Category Dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Constants.CATEGORIES
        );
        binding.actvCategory.setAdapter(categoryAdapter);

        // Status Dropdown
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusLabels
        );
        binding.actvStatus.setAdapter(statusAdapter);

        // Default status selection if empty
        if (binding.actvStatus.getText().toString().isEmpty()) {
            binding.actvStatus.setText(STATUS_LABEL_WANT_TO_READ, false);
            binding.etPagesRead.setText("0");
        }
    }

    private void setupTextWatchers() {
        TextWatcher commonWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateForm(true);
            }
        };

        binding.etTitle.addTextChangedListener(commonWatcher);
        binding.etAuthor.addTextChangedListener(commonWatcher);
        binding.etDescription.addTextChangedListener(commonWatcher);

        // Category selection listener
        binding.actvCategory.setOnItemClickListener((parent, view, position, id) -> validateForm(true));

        // Total pages listener: if status is Completed, auto-update pages read to total pages
        binding.etTotalPages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String statusLabel = binding.actvStatus.getText().toString();
                if (STATUS_LABEL_COMPLETED.equals(statusLabel)) {
                    binding.etPagesRead.setText(s.toString());
                }
                validateForm(true);
            }
        });

        // Pages read listener
        binding.etPagesRead.addTextChangedListener(commonWatcher);

        // Status listener: auto-update pages read when status changes
        binding.actvStatus.setOnItemClickListener((parent, view, position, id) -> {
            String selectedStatusLabel = statusLabels.get(position);
            handleStatusChange(selectedStatusLabel);
            validateForm(true);
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
        binding.btnSaveBook.setOnClickListener(v -> saveBook());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private boolean validateForm(boolean showErrors) {
        boolean isValid = true;

        // 1. Title
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

        // 2. Author
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

        // 3. Category
        String category = binding.actvCategory.getText().toString().trim();
        if (category.isEmpty() || !Constants.CATEGORIES.contains(category)) {
            if (showErrors) binding.tilCategory.setError("Please select a valid category");
            isValid = false;
        } else {
            if (showErrors) binding.tilCategory.setError(null);
        }

        // 4. Total Pages
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

        // 5. Pages Read & Status Cross-Field
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

        // 6. Description
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

    private void saveBook() {
        if (!validateForm(true)) {
            Toast.makeText(this, "Please correct the errors in the form.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = binding.etTitle.getText().toString();
        String author = binding.etAuthor.getText().toString();
        String category = binding.actvCategory.getText().toString();
        int totalPages = Integer.parseInt(binding.etTotalPages.getText().toString().trim());
        int pagesRead = Integer.parseInt(binding.etPagesRead.getText().toString().trim());
        String statusLabel = binding.actvStatus.getText().toString();
        String statusCode = mapStatusLabelToCode(statusLabel);
        String description = binding.etDescription.getText().toString();

        Book book = new Book(title, author, category, totalPages, pagesRead, statusCode, description);

        // Run BookValidator
        ValidationResult validationResult = BookValidator.validate(book);
        if (!validationResult.isValid()) {
            displayValidationErrors(validationResult);
            Toast.makeText(this, "Validation failed. Please check field errors.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long id = dbHelper.insertBook(book);
            if (id == -1) {
                // Duplicate constraint failed
                binding.tilTitle.setError("A book with this title and author already exists.");
                binding.tilAuthor.setError("A book with this title and author already exists.");
                Toast.makeText(this, "Error: A book with this title and author already exists.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Book added successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddBookActivity.this, BookListActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (SQLiteConstraintException e) {
            binding.tilTitle.setError("A book with this title and author already exists.");
            binding.tilAuthor.setError("A book with this title and author already exists.");
            Toast.makeText(this, "Error: A book with this title and author already exists.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving book: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
