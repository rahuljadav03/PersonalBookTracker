# Personal Book Tracker 📚

**Personal Book Tracker** is a native Android application built in Java with Material Design 3, ViewBinding, and an offline-first SQLite architecture. It allows avid readers to track their personal library, record reading progress across pages, filter and sort book collections, view comprehensive reading statistics, and manage book details seamlessly.

---

## 🌟 Key Features

- **Library Dashboard:** Summary card counts for Total Books, Want to Read, Currently Reading, and Completed books, along with a quick-access list of recently added books.
- **Full Library Search & Filtering:** Case-insensitive search by title or author, status filter chips (`Want to Read`, `Reading`, `Completed`), and multiple sorting modes (`Recently Added`, `Title A-Z / Z-A`, `Author A-Z / Z-A`).
- **Inline Editing & Prefilled Forms:** Seamless transition between View Mode and Edit Mode on the Book Details screen with automatic field pre-filling (including Category and Status dropdowns).
- **Exact Progress Management:** Progress tracking with automatic cross-field status alignment (`0` pages read for `Want to Read`, `total_pages` for `Completed`).
- **Indian Standard Time (IST) Support:** All timestamps (`Added on` and `Last updated`) formatted in IST (`Asia/Kolkata`) with 12-hour AM/PM timestamps.
- **Reading Statistics:** Aggregated library insights including completion rate, average pages per book, total pages read, reading status breakdown, and category distribution with zero-division safety.
- **Consistent Visual Styling:** Unified soft coral red (`#FF6B6B`) delete actions, zero-ripple press effects on Delete buttons, clean Material 3 outlined buttons, and dotless progress bars (`trackStopIndicatorSize = 0dp`).

---

## 🛠️ Tech Stack & Architecture

- **Language:** Java (JDK 11)
- **UI Framework:** Android XML, ViewBinding, Material Components (Material 3 v1.14.0)
- **Architecture:** Model-View-Presenter / Separated Domain Logic (Off-main-thread background DB operations using `ExecutorService` & `Handler`)
- **Database:** Local SQLite via `SQLiteOpenHelper`
- **Validation:** 3-Layer Validation System (Frontend UI Real-Time Watchers $\rightarrow$ Domain `BookValidator` $\rightarrow$ SQLite SQL `CHECK` & `UNIQUE` Constraints)
- **System Theme:** AndroidX SplashScreen API, Light & Dark Theme Support, Edge-to-Edge window insets
- **Min SDK:** 24 (Android 7.0 Nougat) | **Target SDK:** 35 (Android 15)

---

## 📸 Screenshots

| Splash & Launch | Dashboard | Add Book |
| :---: | :---: | :---: |
| ![Splash Screen](docs/screenshots/splash.png) | ![Dashboard](docs/screenshots/dashboard.png) | ![Add Book Form](docs/screenshots/add_book.png) |

| Library List | Book Details | Edit Mode | Statistics |
| :---: | :---: | :---: | :---: |
| ![Book List](docs/screenshots/book_list.png) | ![Book Details](docs/screenshots/book_details.png) | ![Edit Mode](docs/screenshots/edit_book.png) | ![Reading Statistics](docs/screenshots/statistics.png) |

---

## 📱 Screen Navigation Flow

```
                     +---------------------------+
                     |    System Splash Screen   |
                     +-------------+-------------+
                                   |
                                   v
                     +---------------------------+
                     |  MainActivity (Dashboard) |
                     +----+-------+---------+----+
                          |       |         |
      +-------------------+       |         +--------------------+
      |                           |                              |
      v                           v                              v
+---------------+       +------------------+           +------------------+
| AddBookActivity|      | BookListActivity |           |StatisticsActivity|
|   (New Book)  |       |   (My Library)   |           |    (Analytics)   |
+---------------+       +--------+---------+           +------------------+
                                 |
                                 v
                     +---------------------------+
                     |     BookDetailActivity    |
                     | (View Mode & Edit Mode)   |
                     +-------------+-------------+
                                   |
                                   | (Click Back)
                                   v
                     +---------------------------+
                     |  MainActivity (Dashboard) |
                     +---------------------------+
```

---

## 🗄️ SQLite Database Schema

Table Name: `books`

```sql
CREATE TABLE books (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL CHECK(length(trim(title)) >= 2 AND length(title) <= 100),
    author TEXT NOT NULL CHECK(length(trim(author)) >= 2 AND length(author) <= 60),
    category TEXT NOT NULL,
    total_pages INTEGER NOT NULL CHECK(total_pages >= 1 AND total_pages <= 10000),
    pages_read INTEGER NOT NULL DEFAULT 0 CHECK(pages_read >= 0 AND pages_read <= total_pages),
    status TEXT NOT NULL CHECK(status IN ('WANT_TO_READ', 'READING', 'COMPLETED')),
    description TEXT CHECK(description IS NULL OR length(description) <= 500),
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    CHECK (
        (status = 'WANT_TO_READ' AND pages_read = 0) OR
        (status = 'COMPLETED' AND pages_read = total_pages) OR
        (status = 'READING')
    ),
    UNIQUE(title COLLATE NOCASE, author COLLATE NOCASE)
);
```

---

## 🛡️ 3-Layer Validation Strategy

| Field | Layer 1: Frontend UI (`TextWatcher` / Form) | Layer 2: Domain Logic (`BookValidator`) | Layer 3: Database (`SQLite CHECK`) |
| :--- | :--- | :--- | :--- |
| **Title** | Non-empty, 2–100 chars, Real-time `TextInputLayout` error | Trimmed, non-null, length check (2–100) | `CHECK(length(trim(title)) >= 2 AND length(title) <= 100)` |
| **Author** | Non-empty, 2–60 chars, Regex `^[a-zA-Z\s\.'\-]+$` | Trimmed, non-null, valid characters regex | `CHECK(length(trim(author)) >= 2 AND length(author) <= 60)` |
| **Category** | Selected item must exist in `Constants.CATEGORIES` | Must match predefined allowed category list | Column `category TEXT NOT NULL` |
| **Total Pages** | Integer between 1 and 10,000 | Non-zero positive integer (1–10,000) | `CHECK(total_pages >= 1 AND total_pages <= 10000)` |
| **Pages Read** | Integer between 0 and `total_pages` | `0 <= pagesRead <= totalPages` | `CHECK(pages_read >= 0 AND pages_read <= total_pages)` |
| **Status Rules** | Auto-sets `pagesRead=0` (`WANT_TO_READ`), `pagesRead=total` (`COMPLETED`) | Cross-field validation enforcing status/page alignment | Table `CHECK` constraint enforcing page alignment per status |
| **Duplicate Check**| Checks title + author NOCASE, ignoring current book ID | Off-main-thread query against SQLite | `UNIQUE(title COLLATE NOCASE, author COLLATE NOCASE)` |

---

## ⏰ IST Date & Time Handling

All timestamps are stored as UTC epoch milliseconds (`System.currentTimeMillis()`) in SQLite. `DateTimeUtils.formatIst(long epochMillis)` formats timestamps strictly in Indian Standard Time (`Asia/Kolkata` / UTC+5:30):

- **Pattern:** `dd MMM yyyy, hh:mm a` (e.g., `06 Nov 2026, 03:45 PM`).
- **Rules:** `created_at` is set during `insertBook()` and never altered. `updated_at` refreshes automatically on every `updateBook()`.

---

## 📂 Project Directory Structure

```
PersonalBookTracker/
├── app/
│   ├── proguard-rules.pro
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/booktracker/
│       │   │   ├── MainActivity.java
│       │   │   ├── AddBookActivity.java
│       │   │   ├── BookListActivity.java
│       │   │   ├── BookDetailActivity.java
│       │   │   ├── StatisticsActivity.java
│       │   │   ├── adapter/BookAdapter.java
│       │   │   ├── database/DatabaseHelper.java
│       │   │   ├── model/Book.java
│       │   │   └── utils/
│       │   │       ├── BookValidator.java
│       │   │       ├── Constants.java
│       │   │       ├── DateTimeUtils.java
│       │   │       ├── InsetsHelper.java
│       │   │       └── ValidationResult.java
│       │   └── res/
│       │       ├── drawable/
│       │       ├── layout/
│       │       ├── values/
│       │       └── values-night/
│       ├── test/java/com/example/booktracker/
│       │   └── utils/
│       │       ├── BookValidatorTest.java
│       │       └── DateTimeUtilsTest.java
│       └── androidTest/java/com/example/booktracker/
│           ├── database/DatabaseHelperTest.java
│           └── ui/AppUiTest.java
├── docs/
│   └── screenshots/
├── build.gradle.kts
├── gradle.properties
├── keystore.properties (ignored)
└── README.md
```

---

## 🚀 Setup & Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2) or newer
- JDK 11
- Android SDK Platform 35

### Building Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Building & Signing Release APK
1. Create `keystore.properties` in the project root:
   ```properties
   storeFile=release.keystore
   storePassword=your_store_password
   keyAlias=your_key_alias
   keyPassword=your_key_password
   ```
2. Build signed release APK:
   ```bash
   ./gradlew assembleRelease
   ```
Output: `app/build/outputs/apk/release/app-release.apk`

---

## 🧪 Testing Suite Execution

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run Instrumented Espresso & Database Tests
```bash
./gradlew connectedAndroidTest
```

---

## 🔄 Git Branching Strategy

- `main`: Production release branch (`v1.0.0`).
- `develop`: Integration branch for active development.
- `feature/*`: Feature development branches merged into `develop`.

---

## 🗺️ Future Roadmap

- Cloud synchronization with Firebase Firestore / Room.
- ISBN barcode scanner integration via ML Kit Vision.
- Export library reading records to CSV and PDF formats.

---

## 📄 License & Author

**Author:** Personal Book Tracker Engineering Team  
**License:** MIT License — See `LICENSE` for details.
