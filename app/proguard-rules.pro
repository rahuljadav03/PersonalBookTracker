# Personal Book Tracker - ProGuard & R8 Release Keep Rules

# Keep ViewBinding generated classes
-keep class com.example.booktracker.databinding.** { *; }

# Keep Data Models
-keep class com.example.booktracker.model.** { *; }

# Keep Material Components and AndroidX Core
-keep class com.google.android.material.** { *; }
-keep class androidx.core.splashscreen.** { *; }
