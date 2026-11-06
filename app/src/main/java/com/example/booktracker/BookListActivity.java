package com.example.booktracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.booktracker.databinding.ActivityBookListBinding;

public class BookListActivity extends AppCompatActivity {

    private ActivityBookListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
