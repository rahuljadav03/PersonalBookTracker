package com.example.booktracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.booktracker.databinding.ActivityAddBookBinding;

public class AddBookActivity extends AppCompatActivity {

    private ActivityAddBookBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCancel.setOnClickListener(v -> finish());
    }
}
