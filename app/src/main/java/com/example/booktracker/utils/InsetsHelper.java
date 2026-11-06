package com.example.booktracker.utils;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetsHelper {

    public static void applySystemBarInsets(View root) {
        if (root == null) return;

        final int initialLeft = root.getPaddingLeft();
        final int initialTop = root.getPaddingTop();
        final int initialRight = root.getPaddingRight();
        final int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemInsets = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime()
            );

            v.setPadding(
                    initialLeft + systemInsets.left,
                    initialTop + systemInsets.top,
                    initialRight + systemInsets.right,
                    initialBottom + systemInsets.bottom
            );

            return insets;
        });
    }
}
