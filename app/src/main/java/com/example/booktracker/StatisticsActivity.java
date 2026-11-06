package com.example.booktracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.booktracker.databinding.ActivityStatisticsBinding;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
