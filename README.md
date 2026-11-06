# 📚 Personal Book Tracker

**Personal Book Tracker** is an offline native Android application designed to help users track their personal reading goals, manage book collections, and view reading statistics. The application is built using Java, Android XML ViewBinding, and a local SQLite database for complete data privacy without requiring an internet connection.

**Project Type:** College Mini Project (Mobile Application Development)

---

## ✨ Features

- 📊 **Dashboard:** Displays overall library counts (Total Books, Want to Read, Currently Reading, Completed) and a quick view of recently added books.
- ➕ **Add Book:** Simple form to add new books with input validation for title, author, category, total pages, pages read, reading status, and optional description.
- 📖 **Library List:** View all books in your library in a clean scrollable list with progress indicators.
- ✏️ **Edit Book:** Seamless inline form editing with prefilled fields and automatic validation rules.
- 🗑️ **Delete Book:** Safely delete books from the library with an explicit confirmation dialog.
- 🔍 **Search & Filter:** Search books by title or author, filter by reading status, and sort by title, author, or addition date.
- 📈 **Reading Statistics:** View library summary metrics including completion rate, average pages read per book, and status and category breakdowns.
- 🕒 **IST Date and Time:** Timestamps for creation and updates automatically formatted in Indian Standard Time (Asia/Kolkata) in 12-hour AM/PM format.

---

## 🛠️ Technologies Used

| Category | Technology |
| :--- | :--- |
| **Language** | Java (JDK 11) |
| **UI Framework** | Android XML, ViewBinding, Material Components (Material 3) |
| **Database** | SQLite via SQLiteOpenHelper |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 35 (Android 15) |
| **Testing** | JUnit 4, Espresso |

---

## 📱 App Screens and Flow

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

## 🗄️ Database

- **Database Name:** `BookTracker.db`
- **Table Name:** `books`

The database contains a case-insensitive UNIQUE constraint on the combination of `title` and `author`, ensuring that the exact same title and author cannot be saved twice.

| Column Name | Data Type | Constraints & Rules |
| :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT |
| `title` | TEXT | NOT NULL, 2 to 100 characters |
| `author` | TEXT | NOT NULL, 2 to 60 characters |
| `category` | TEXT | NOT NULL (Fiction, Non-Fiction, Sci-Fi, Fantasy, Mystery, Biography, Romance, Other) |
| `total_pages` | INTEGER | NOT NULL, 1 to 10,000 |
| `pages_read` | INTEGER | NOT NULL, DEFAULT 0, 0 to `total_pages` |
| `status` | TEXT | NOT NULL (WANT_TO_READ, READING, COMPLETED) |
| `description` | TEXT | Optional, maximum 500 characters |
| `created_at` | INTEGER | NOT NULL, UTC epoch milliseconds |
| `updated_at` | INTEGER | NOT NULL, UTC epoch milliseconds |

---

## ✅ Validation

Validation runs across three distinct layers to ensure complete data integrity:
1. **UI Layer:** Real-time text watchers, input filters, and TextInputLayout errors on screen forms.
2. **Java Code Layer:** Domain validator class (`BookValidator`) checking input boundaries and cross-field status rules before database operations.
3. **Database Layer:** SQLite table `CHECK` constraints and `UNIQUE` indexes.

| Field Name | Validation Rules |
| :--- | :--- |
| **Title** | Required, length between 2 and 100 characters. Unique combination with author. |
| **Author** | Required, length between 2 and 60 characters. Allowed characters: letters, spaces, dots, hyphens, and apostrophes. |
| **Category** | Required, must be selected from the permitted category list. |
| **Total Pages** | Required, integer between 1 and 10,000. |
| **Pages Read** | Required, integer between 0 and total pages. |
| **Reading Status** | Required. Want to Read requires 0 pages read; Completed requires pages read to equal total pages. |
| **Description** | Optional, maximum length of 500 characters. |

---

## 📂 Project Structure

```
PersonalBookTracker/
├── app/
│   ├── build.gradle.kts          # App-level build configuration and dependencies
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # App manifest and activity declarations
│       │   ├── java/com/example/booktracker/
│       │   │   ├── MainActivity.java        # Main dashboard screen
│       │   │   ├── AddBookActivity.java     # Add book screen
│       │   │   ├── BookListActivity.java    # Library list screen with search/filters
│       │   │   ├── BookDetailActivity.java  # Book details and inline edit panel
│       │   │   ├── StatisticsActivity.java  # Reading analytics screen
│       │   │   ├── adapter/BookAdapter.java # RecyclerView adapter for book cards
│       │   │   ├── database/DatabaseHelper.java # SQLite database manager
│       │   │   ├── model/Book.java          # Book data model
│       │   │   └── utils/                   # Validation, IST date utils, constants
│       │   └── res/                         # XML layouts, drawables, strings, colors
│       ├── test/                        # Local JUnit 4 unit tests
│       └── androidTest/                 # Instrumented Espresso and database tests
├── build.gradle.kts                     # Root-level Gradle build file
└── README.md                            # Project documentation
```

---

## ▶️ How to Run

1. Open **Android Studio** (Ladybug 2024.2 or newer).
2. Select **Open** and choose the `PersonalBookTracker` project directory.
3. Wait for Android Studio to sync the Gradle dependencies.
4. Connect an Android phone via USB with USB Debugging enabled, or launch an Android Virtual Device (Emulator) running API 24 or higher.
5. Click the green **Run** button (or press `Shift + F10`) in the top toolbar to build and launch the application.

---

## 📲 Install on a Phone Without USB

1. In Android Studio, open the top menu and select **Build > Build APK(s) / Bundle(s) > Build APK(s)**.
2. Once the build finishes, click the **locate** link in the bottom-right notification popup, or navigate to `app/build/outputs/apk/debug/app-debug.apk`.
3. Copy the `app-debug.apk` file to your Android phone via Bluetooth, Google Drive, or email.
4. On your phone, open the File Manager app, locate `app-debug.apk`, tap it, and allow installation from unknown sources if prompted.
5. Tap **Install** and launch **Personal Book Tracker** from your phone's app drawer.

---

## 🧪 Running Tests

You can run the automated unit tests and instrumented UI tests using the Gradle wrapper terminal commands:

Execute local JUnit unit tests:
```bash
./gradlew test
```

Execute instrumented database and Espresso UI tests on an attached device or emulator:
```bash
./gradlew connectedAndroidTest
```
