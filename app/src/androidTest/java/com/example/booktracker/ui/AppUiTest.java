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
import static org.hamcrest.Matchers.containsString;
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

    // 1. Dashboard Launcher Opens MainActivity Directly
    @Test
    public void testLauncherOpensMainActivityDirectly() {
        onView(withId(R.id.tv_dashboard_title)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_total_books_count)).check(matches(withText("0")));
    }

    // 2. Add Book Validation Error
    @Test
    public void testAddBook_EmptyFormValidationErrors() {
        onView(withId(R.id.fab_add_book)).perform(click());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());
        onView(withId(R.id.tv_form_title)).check(matches(isDisplayed()));
    }

    // 3. Add Valid Book & Dashboard Counts Update
    @Test
    public void testAddBook_ValidBookUpdatesDashboard() {
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

        onView(withId(R.id.tv_total_books_count)).check(matches(withText("1")));
        onView(withId(R.id.tv_reading_count)).check(matches(withText("1")));
    }

    // 4. Edit Cancel With No Changes Navigates To Dashboard Directly
    @Test
    public void testEditBook_CancelWithNoChangesNavigatesToDashboard() {
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

        // Open details and enter Edit Mode
        onView(withId(R.id.rv_recent_books)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.btn_edit_book)).perform(scrollTo(), click());

        // Tap Cancel with no changes
        onView(withId(R.id.btn_cancel_edit)).perform(scrollTo(), click());

        // Assert Dashboard is displayed
        onView(withId(R.id.tv_dashboard_title)).check(matches(isDisplayed()));
    }

    // 5. Edit Cancel With Changes Shows Discard Dialog
    @Test
    public void testEditBook_CancelWithChangesShowsDiscardDialog() {
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

        // Open details and enter Edit Mode
        onView(withId(R.id.rv_recent_books)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.btn_edit_book)).perform(scrollTo(), click());

        // Modify title
        onView(withId(R.id.et_title)).perform(clearText(), typeText("Dune Messiah"), closeSoftKeyboard());

        // Tap Cancel
        onView(withId(R.id.btn_cancel_edit)).perform(scrollTo(), click());

        // Assert Discard dialog appears and click Discard
        onView(withText("Unsaved Changes")).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());

        // Assert Dashboard is displayed
        onView(withId(R.id.tv_dashboard_title)).check(matches(isDisplayed()));
    }

    // 6. Dashboard Recent List Delete Dialog Cancel & Delete Flow
    @Test
    public void testDashboardRecentList_DeleteCancelAndDeleteFlow() {
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

        // Confirm total count = 1
        onView(withId(R.id.tv_total_books_count)).check(matches(withText("1")));

        // Click delete icon on item
        onView(withId(R.id.btn_item_delete)).perform(click());

        // Click Cancel in AlertDialog
        onView(withId(android.R.id.button2)).perform(click());

        // Count stays 1
        onView(withId(R.id.tv_total_books_count)).check(matches(withText("1")));

        // Click delete icon again and confirm Delete
        onView(withId(R.id.btn_item_delete)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        // Dashboard count returns to 0 and empty state / recent list hides
        onView(withId(R.id.tv_total_books_count)).check(matches(withText("0")));
    }

    // 7. Book Details Panel Action Buttons, No Update Progress, and Back to Dashboard
    @Test
    public void testBookDetail_ActionButtonsOrderAndNoUpdateProgressButton() {
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

        // Assert Edit Book, Delete Book, Back buttons are visible
        onView(withId(R.id.btn_edit_book)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_delete_book)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_to_dashboard)).check(matches(isDisplayed()));

        // Click Back button
        onView(withId(R.id.btn_back_to_dashboard)).perform(scrollTo(), click());

        // Assert Dashboard is displayed
        onView(withId(R.id.tv_dashboard_title)).check(matches(isDisplayed()));
    }

    // 8. Last Updated Changes On Edit While Added On Stays Unchanged
    @Test
    public void testEditBook_LastUpdatedChangesAndAddedOnStaysUnchanged() {
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

        // Verify Added on text pattern
        onView(withId(R.id.tv_detail_created_at)).check(matches(withText(containsString("Added on:"))));
        onView(withId(R.id.tv_detail_updated_at)).check(matches(withText(containsString("Last updated:"))));

        // Tap Edit button
        onView(withId(R.id.btn_edit_book)).perform(scrollTo(), click());

        // Update Title
        onView(withId(R.id.et_title)).perform(clearText(), typeText("Hyperion Cantos"), closeSoftKeyboard());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Verify updated title and timestamps
        onView(withId(R.id.tv_detail_title)).check(matches(withText("Hyperion Cantos")));
        onView(withId(R.id.tv_detail_created_at)).check(matches(withText(containsString("Added on:"))));
        onView(withId(R.id.tv_detail_updated_at)).check(matches(withText(containsString("Last updated:"))));
    }

    // 9. Statistics Screen Without Crash On 0 Books and With Books
    @Test
    public void testStatisticsScreen_ZeroBooksAndWithBooksNoCrash() {
        // Open statistics with 0 books
        onView(withId(R.id.btn_view_statistics)).perform(click());
        onView(withId(R.id.tv_stats_title)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_stat_total_books)).check(matches(withText("0")));
        onView(withId(R.id.tv_stat_completion_rate)).check(matches(withText("0%")));

        // Press back
        androidx.test.espresso.Espresso.pressBack();

        // Add a book
        onView(withId(R.id.fab_add_book)).perform(click());
        onView(withId(R.id.et_title)).perform(typeText("Solaris"), closeSoftKeyboard());
        onView(withId(R.id.et_author)).perform(typeText("Stanislaw Lem"), closeSoftKeyboard());
        onView(withId(R.id.actv_category)).perform(click());
        onData(is("Sci-Fi")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.et_total_pages)).perform(typeText("204"), closeSoftKeyboard());
        onView(withId(R.id.et_pages_read)).perform(typeText("204"), closeSoftKeyboard());
        onView(withId(R.id.actv_status)).perform(click());
        onData(is("COMPLETED")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.btn_save_book)).perform(scrollTo(), click());

        // Open statistics again
        onView(withId(R.id.btn_view_statistics)).perform(click());
        onView(withId(R.id.tv_stat_total_books)).check(matches(withText("1")));
        onView(withId(R.id.tv_stat_completion_rate)).check(matches(withText("100%")));
    }
}
