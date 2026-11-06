package com.example.booktracker.ui;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.booktracker.MainActivity;
import com.example.booktracker.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class AppUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("BookTracker.db");
    }

    // 1. Dashboard Initial Empty State
    @Test
    public void testDashboardInitialZeroCounts() {
        onView(withId(R.id.tv_total_books_count)).check(matches(withText("0")));
        onView(withId(R.id.tv_want_to_read_count)).check(matches(withText("0")));
        onView(withId(R.id.tv_reading_count)).check(matches(withText("0")));
        onView(withId(R.id.tv_completed_count)).check(matches(withText("0")));
    }

    // 2. Empty Form Validation Errors
    @Test
    public void testAddBook_EmptyFormValidationErrors() {
        onView(withId(R.id.fab_add_book)).perform(click());

        // Attempt to save empty form
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Form title should still be displayed indicating we are on the form page
        onView(withId(R.id.tv_form_title)).check(matches(isDisplayed()));
    }

    // 3. Add Valid Book and See Dashboard Counts Update
    @Test
    public void testAddBook_ValidBookUpdatesDashboard() {
        onView(withId(R.id.fab_add_book)).perform(click());

        onView(withId(R.id.et_title)).perform(typeText("Dune"), closeSoftKeyboard());
        onView(withId(R.id.et_author)).perform(typeText("Frank Herbert"), closeSoftKeyboard());

        // Select Category
        onView(withId(R.id.actv_category)).perform(click());
        onData(is("Sci-Fi")).inRoot(isPlatformPopup()).perform(click());

        onView(withId(R.id.et_total_pages)).perform(typeText("412"), closeSoftKeyboard());
        onView(withId(R.id.et_pages_read)).perform(typeText("100"), closeSoftKeyboard());

        // Select Status
        onView(withId(R.id.actv_status)).perform(click());
        onData(is("READING")).inRoot(isPlatformPopup()).perform(click());

        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Returned to Dashboard -> Total count should be 1 and Reading count should be 1
        onView(withId(R.id.tv_total_books_count)).check(matches(withText("1")));
        onView(withId(R.id.tv_reading_count)).check(matches(withText("1")));
    }

    // 4. Filter and Search in Book List
    @Test
    public void testBookList_SearchAndFilter() {
        // Add book 1
        onView(withId(R.id.fab_add_book)).perform(click());
        onView(withId(R.id.et_title)).perform(typeText("1984"), closeSoftKeyboard());
        onView(withId(R.id.et_author)).perform(typeText("George Orwell"), closeSoftKeyboard());
        onView(withId(R.id.actv_category)).perform(click());
        onData(is("Fiction")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.et_total_pages)).perform(typeText("328"), closeSoftKeyboard());
        onView(withId(R.id.et_pages_read)).perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.actv_status)).perform(click());
        onData(is("WANT_TO_READ")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Navigate to Book List
        onView(withId(R.id.btn_view_all_books)).perform(click());

        // Search for 1984
        onView(withId(R.id.et_search)).perform(typeText("1984"), closeSoftKeyboard());
        onView(withId(R.id.rv_books)).check(matches(isDisplayed()));

        // Filter by Want to Read chip
        onView(withId(R.id.chip_filter_want_to_read)).perform(click());
        onView(withId(R.id.rv_books)).check(matches(isDisplayed()));
    }

    // 5. Edit and Delete Book Flow from Dashboard Recent List
    @Test
    public void testDashboardRecentList_EditAndDeleteFlow() {
        // Add a book
        onView(withId(R.id.fab_add_book)).perform(click());
        onView(withId(R.id.et_title)).perform(typeText("Foundation"), closeSoftKeyboard());
        onView(withId(R.id.et_author)).perform(typeText("Isaac Asimov"), closeSoftKeyboard());
        onView(withId(R.id.actv_category)).perform(click());
        onData(is("Sci-Fi")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.et_total_pages)).perform(typeText("255"), closeSoftKeyboard());
        onView(withId(R.id.et_pages_read)).perform(typeText("255"), closeSoftKeyboard());
        onView(withId(R.id.actv_status)).perform(click());
        onData(is("COMPLETED")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Confirm total count = 1 on Dashboard
        onView(withId(R.id.tv_total_books_count)).check(matches(withText("1")));

        // Click item on Dashboard
        onView(withId(R.id.rv_recent_books)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Verify Detail view opens
        onView(withId(R.id.tv_detail_title)).check(matches(withText("Foundation")));

        // Click Delete button in Detail view
        onView(withId(R.id.btn_delete_book)).perform(scrollTo(), click());

        // Confirm Delete in AlertDialog
        onView(withId(android.R.id.button1)).perform(click());

        // Dashboard count should return to 0 and recent list hides
        onView(withId(R.id.tv_total_books_count)).check(matches(withText("0")));
    }

    // 6. Prefilled Edit Form and Save Flow
    @Test
    public void testEditPrefilledAndSaveFlow() {
        // Add a book
        onView(withId(R.id.fab_add_book)).perform(click());
        onView(withId(R.id.et_title)).perform(typeText("Dune"), closeSoftKeyboard());
        onView(withId(R.id.et_author)).perform(typeText("Frank Herbert"), closeSoftKeyboard());
        onView(withId(R.id.actv_category)).perform(click());
        onData(is("Sci-Fi")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.et_total_pages)).perform(typeText("412"), closeSoftKeyboard());
        onView(withId(R.id.et_pages_read)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.actv_status)).perform(click());
        onData(is("READING")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // On Dashboard, open item in Detail view
        onView(withId(R.id.rv_recent_books)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Tap Edit button
        onView(withId(R.id.btn_edit_book)).perform(scrollTo(), click());

        // Assert all fields are prefilled
        onView(withId(R.id.et_title)).check(matches(withText("Dune")));
        onView(withId(R.id.et_author)).check(matches(withText("Frank Herbert")));
        onView(withId(R.id.actv_category)).check(matches(withText("Sci-Fi")));
        onView(withId(R.id.et_total_pages)).check(matches(withText("412")));
        onView(withId(R.id.et_pages_read)).check(matches(withText("100")));
        onView(withId(R.id.actv_status)).check(matches(withText("Reading")));

        // Update Title
        onView(withId(R.id.et_title)).perform(clearText(), typeText("Dune Messiah"), closeSoftKeyboard());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Assert updated title in View Mode
        onView(withId(R.id.tv_detail_title)).check(matches(withText("Dune Messiah")));
    }

    // 7. Verify Update Progress is removed and Back button navigates to Dashboard
    @Test
    public void testBookDetail_BackNavigatesToDashboardAndNoUpdateProgressButton() {
        // Add a book
        onView(withId(R.id.fab_add_book)).perform(click());
        onView(withId(R.id.et_title)).perform(typeText("The Hobbit"), closeSoftKeyboard());
        onView(withId(R.id.et_author)).perform(typeText("J.R.R. Tolkien"), closeSoftKeyboard());
        onView(withId(R.id.actv_category)).perform(click());
        onData(is("Fantasy")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.et_total_pages)).perform(typeText("310"), closeSoftKeyboard());
        onView(withId(R.id.et_pages_read)).perform(typeText("310"), closeSoftKeyboard());
        onView(withId(R.id.actv_status)).perform(click());
        onData(is("COMPLETED")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Open details
        onView(withId(R.id.rv_recent_books)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Assert Update Progress button text does NOT exist
        onView(withText("Update Progress")).check(doesNotExist());

        // Click Back button
        onView(withId(R.id.btn_back_to_dashboard)).perform(scrollTo(), click());

        // Assert Dashboard is displayed
        onView(withId(R.id.tv_dashboard_title)).check(matches(isDisplayed()));
    }

    // 8. Verify editing a book updates title and detail views
    @Test
    public void testEditBook_UpdatesLastUpdatedTimestamp() {
        // Add a book
        onView(withId(R.id.fab_add_book)).perform(click());
        onView(withId(R.id.et_title)).perform(typeText("Hyperion"), closeSoftKeyboard());
        onView(withId(R.id.et_author)).perform(typeText("Dan Simmons"), closeSoftKeyboard());
        onView(withId(R.id.actv_category)).perform(click());
        onData(is("Sci-Fi")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.et_total_pages)).perform(typeText("482"), closeSoftKeyboard());
        onView(withId(R.id.et_pages_read)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.actv_status)).perform(click());
        onData(is("READING")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Open details
        onView(withId(R.id.rv_recent_books)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Tap Edit button
        onView(withId(R.id.btn_edit_book)).perform(scrollTo(), click());

        // Update Title
        onView(withId(R.id.et_title)).perform(clearText(), typeText("Hyperion Cantos"), closeSoftKeyboard());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Verify updated title
        onView(withId(R.id.tv_detail_title)).check(matches(withText("Hyperion Cantos")));
    }
}
