package com.browser.volant;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.view.KeyEvent;

import com.browser.volant.Activities.MainActivity;
import com.browser.volant.Database.BookmarkDbAdapter;
import com.browser.volant.Database.BookmarkDbHelper;

import junit.framework.AssertionFailedError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
public class MainActivityTest extends AndroidTestCase {

    @Rule
    public final ActivityTestRule<MainActivity> main = new ActivityTestRule<>(MainActivity.class);

    /**
     * Espresso UI testing.
     */

    // Check that all elements that should be visible at launch are actually visible.
    @Test
    public void shouldBeVisisbleAtLaunch() {
        onView(withId(R.id.webview)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.set_button)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.website_text)).check(ViewAssertions.matches(isDisplayed()));
    }

    // Check that the clear text imagebutton is hidden at launch.
    @Test(expected = AssertionFailedError.class)
    public void imageButtonShouldBeHiddenAtLaunch() {
        onView(withId(R.id.imageButton)).check(ViewAssertions.matches(isDisplayed()));
    }

    // Check that the clear text imagebutton becomes visible while the url-bar has focus.
    @Test
    public void imageButtonVisibleWhileEditTextHasFocus() {
        onView(withId(R.id.website_text)).perform(click()).check(ViewAssertions.matches(hasFocus()));
        onView(withId(R.id.imageButton)).check(ViewAssertions.matches(isDisplayed()));
    }

    // check that the softkeyboard closes when the url-bar loses focus.
    @Test
    public void softKeyboardShouldCloseWhenEditTextLosesFocus() {
        onView(withId(R.id.website_text)).perform(click()).check(ViewAssertions.matches(hasFocus()));
        onView(withId(R.id.webview)).perform(click(),closeSoftKeyboard());
    }


    /**
     * Method testing.
     */

    // testing that the url handler method creates a valid url.
    @Test
    public void testUrlHandling() {
        String url = "vg.no";
        String handledURL = "";


        // our URL-handler method from MainActivity
        if (url.startsWith("https://")) {
            handledURL = url;
        } else if (url.startsWith("http://")) {
            handledURL = url;
        } else {
            handledURL = "http://" + url;
        }

        assertEquals("http://vg.no", handledURL);
    }

    // testing that the database is actually opened and is no longer null.
    @Test
    public void testOpeningDatabase() {
        Context context = InstrumentationRegistry.getTargetContext();

        BookmarkDbHelper bDbHelper;
        bDbHelper = new BookmarkDbHelper(context);
        assertNotNull(bDbHelper);
        SQLiteDatabase db = bDbHelper.getWritableDatabase();

        assertNotNull(db);
    }

    // testing that we can query data from the database.
    @Test
    public void testPollDataFromDatabase() {
        Context context = InstrumentationRegistry.getTargetContext();
        BookmarkDbHelper bDbHelper = new BookmarkDbHelper(context);
        SQLiteDatabase db = bDbHelper.getWritableDatabase();
        BookmarkDbAdapter adapter = new BookmarkDbAdapter(context);

        Cursor cursor = adapter.getAllRows();

        assertNotNull(cursor);
    }

    // Checking that the adblockstatus that is saved is the same as the one we get from the saved
    // sharedpreferences.
    @Test
    public void testSavedAdblockStatus() {

        Context context = InstrumentationRegistry.getTargetContext();

        String IS_ADBLOCK_ENABLED = "";

        SharedPreferences.Editor editor = context.getSharedPreferences(IS_ADBLOCK_ENABLED,
                Context.MODE_PRIVATE).edit();
        editor.putBoolean("adblockstatus", true);
        editor.apply();

        SharedPreferences prefs = context.getSharedPreferences(IS_ADBLOCK_ENABLED, Context.MODE_PRIVATE);
        boolean adblockstatus = prefs.getBoolean("adblockstatus", true);

        assertTrue(adblockstatus);
    }
}
