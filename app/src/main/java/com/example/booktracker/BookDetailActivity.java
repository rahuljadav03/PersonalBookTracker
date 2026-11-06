package com.example.booktracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.booktracker.databinding.ActivityBookDetailsBinding;

public class BookDetailActivity extends AppCompatActivity {

    private ActivityBookDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
