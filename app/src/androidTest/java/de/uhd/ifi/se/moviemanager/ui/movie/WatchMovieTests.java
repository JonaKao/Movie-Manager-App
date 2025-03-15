package de.uhd.ifi.se.moviemanager.ui.movie;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.childAtPosition;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.openStorage;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.selectMenuItemAndEnterEdit;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.uhd.ifi.se.moviemanager.MovieManagerActivity;
import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.MovieManagerModel;
import de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class WatchMovieTests {

    private static final MovieManagerModel model = MovieManagerModel.getInstance();
    @Rule
    public ActivityScenarioRule<MovieManagerActivity> activityActivityScenarioRule = new ActivityScenarioRule<>(MovieManagerActivity.class);

    @BeforeClass
    public static void initStorage() {
        ActivityScenario<MovieManagerActivity> activityScenario = launch(MovieManagerActivity.class);
        activityScenario.onActivity(activity -> {
            openStorage(activity);
            clearStorage();
            populateStorageWithMovie();
        });
    }

    private static void populateStorageWithMovie() {
        Movie blade = STORAGE.saveMovieToFile(new Movie("Star Wars"));
        model.addMovie(blade);
    }

    @AfterClass
    public static void clearStorageAfterClass() {
        clearStorage();
    }

    @Test
    public void testWatchMovieNoCancellationWithConfirmation() {
        selectMenuItemAndEnterEdit("Star Wars");

        enterWatchDatePicker();

        onView(withId(com.google.android.material.R.id.mtrl_calendar_main_pane))
                .perform(UiTestUtils.setDate(2016, 9, 13));
        onView(withId(com.google.android.material.R.id.confirm_button))
                .perform(click());
        onView(withId(R.id.commit)).perform(click());
        onView(withId(R.id.content_subtitle))
                .check(matches(withText(containsString("13.09.2016"))));
    }

    @Test
    public void testWatchMovieNoCancellationWithoutConfirmation() {
        selectMenuItemAndEnterEdit("Star Wars");

        enterWatchDatePicker();

        onView(withId(com.google.android.material.R.id.mtrl_calendar_main_pane))
                .perform(UiTestUtils.setDate(2017, 4, 22));
        onView(withId(com.google.android.material.R.id.confirm_button))
                .perform(click());
        pressBack();
        onView(withText(R.string.yes)).inRoot(isDialog()).perform(click());

        onView(withId(R.id.content_subtitle))
                .check(matches(not(hasDescendant(withText(containsString("22.04.2017"))))));
    }

    @Test
    public void testWatchMovieWithCancellation() {
        selectMenuItemAndEnterEdit("Star Wars");

        enterWatchDatePicker();

        onView(withId(com.google.android.material.R.id.mtrl_calendar_main_pane))
                .perform(UiTestUtils.setDate(2019, 2, 19));
        onView(withId(com.google.android.material.R.id.cancel_button))
                .perform(click());

        pressBack();
        onView(withId(R.id.content_subtitle))
                .check(matches(not(hasDescendant(withText(containsString("19.02.2019"))))));
    }


    private void enterWatchDatePicker() {
        onView(withId(R.id.watch_date_input)).perform(scrollTo(), click());
    }
}
