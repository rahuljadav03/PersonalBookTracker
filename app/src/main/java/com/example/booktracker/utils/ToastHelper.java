package com.example.booktracker.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.booktracker.R;

import java.lang.ref.WeakReference;

public class ToastHelper {

    private static WeakReference<Activity> activeActivityRef = new WeakReference<>(null);
    private static WeakReference<View> activeToastViewRef = new WeakReference<>(null);
    private static Runnable activeDismissRunnable = null;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void show(Activity activity, @StringRes int resId) {
        if (activity == null) return;
        show(activity, activity.getString(resId));
    }

    public static void show(Activity activity, String message) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed() || message == null || message.trim().isEmpty()) {
            return;
        }

        mainHandler.post(() -> performShow(activity, message));
    }

    private static void performShow(@NonNull Activity activity, String message) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        // Cancel any active toast view immediately before showing new message
        cancelActiveToast();

        ViewGroup rootContainer = activity.findViewById(android.R.id.content);
        if (rootContainer == null) {
            return;
        }

        View toastView = LayoutInflater.from(activity).inflate(R.layout.view_toast, rootContainer, false);
        TextView tvMessage = toastView.findViewById(R.id.tv_toast_message);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        int defaultBottomMargin = activity.getResources().getDimensionPixelSize(R.dimen.toast_margin_bottom);
        params.bottomMargin = defaultBottomMargin;
        toastView.setLayoutParams(params);

        ViewCompat.setOnApplyWindowInsetsListener(toastView, (v, insets) -> {
            int navBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int insetBottom = Math.max(navBottom, imeBottom);

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
            if (lp != null) {
                lp.bottomMargin = defaultBottomMargin + insetBottom;
                v.setLayoutParams(lp);
            }
            return insets;
        });

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(150);

        rootContainer.addView(toastView);
        toastView.startAnimation(fadeIn);

        activeActivityRef = new WeakReference<>(activity);
        activeToastViewRef = new WeakReference<>(toastView);

        toastView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {}

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                if (activeDismissRunnable != null) {
                    mainHandler.removeCallbacks(activeDismissRunnable);
                }
            }
        });

        long displayDuration = (message.length() > 40) ? 3500L : 2000L;
        activeDismissRunnable = () -> dismissToast(toastView, rootContainer);
        mainHandler.postDelayed(activeDismissRunnable, displayDuration);
    }

    private static void dismissToast(View toastView, ViewGroup container) {
        if (toastView == null || container == null) return;

        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(200);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                container.removeView(toastView);
                if (activeToastViewRef.get() == toastView) {
                    activeToastViewRef.clear();
                    activeActivityRef.clear();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        toastView.startAnimation(fadeOut);
    }

    public static void cancelActiveToast() {
        if (activeDismissRunnable != null) {
            mainHandler.removeCallbacks(activeDismissRunnable);
            activeDismissRunnable = null;
        }

        View toastView = activeToastViewRef.get();
        if (toastView != null) {
            ViewGroup parent = (ViewGroup) toastView.getParent();
            if (parent != null) {
                parent.removeView(toastView);
            }
            activeToastViewRef.clear();
            activeActivityRef.clear();
        }
    }
}
