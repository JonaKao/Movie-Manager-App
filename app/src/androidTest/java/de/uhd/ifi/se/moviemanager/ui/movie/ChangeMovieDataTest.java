package de.uhd.ifi.se.moviemanager.ui.movie;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.openStorage;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.selectMenuItemAndEnterEdit;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.google.common.base.CharMatcher;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.matcher.StringMatcher;
import org.hamcrest.Matchers;
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
public class ChangeMovieDataTest {

    private static final MovieManagerModel model = MovieManagerModel.getInstance();
    @Rule
    public ActivityScenarioRule<MovieManagerActivity> activityScenarioRule = new ActivityScenarioRule<>(MovieManagerActivity.class);

    @BeforeClass
    public static void initStorage() {
        ActivityScenario<MovieManagerActivity> activityScenario = launch(MovieManagerActivity.class);
        activityScenario.onActivity(activity -> {
            openStorage(activity);
            clearStorage();
            populateStorage();
        });
    }

    @AfterClass
    public static void clearStorageAfterClass() {
        UiTestUtils.clearStorage();
    }

    private static void populateStorage() {
        Movie blade = STORAGE.saveMovieToFile(new Movie("Blade Runner"));
        model.addMovie(blade);

        Movie pulp = STORAGE.saveMovieToFile(new Movie("Pulp Fiction"));
        model.addMovie(pulp);
    }

    @Test
    public void testChangeMovieDetailDataSaveChanges() {
        selectMenuItemAndEnterEdit("Blade Runner");

        onView(withId(R.id.title_input))
                .perform(clearText(),
                        typeText("Star Wars"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.runtime_input))
                .perform(clearText(),
                        typeText("180"),
                        closeSoftKeyboard()
                );

        onView(withId(R.id.description_input))
                .perform(scrollTo(),
                        typeText("Hello There"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.languages_input))
                .perform(scrollTo(),
                        typeText("English"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.production_location_input))
                .perform(scrollTo(),
                        typeText("USA"),
                        closeSoftKeyboard()
                );


        onView(withId(R.id.releases_input)).perform(scrollTo(), click());

        onView(withId(R.id.release_title_input))
                .inRoot(isDialog())
                .perform(typeText("Great Britain"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.release_date_input)).perform(click());

        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());

        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());

        onView(withId(R.id.commit)).perform(click());

        // test
        onView(withText("Star Wars")).check(matches(isDisplayed()));
        onView(withId(R.id.content_subtitle)).check(matches(
                withText(containsString("180"))));
        onView(withId(R.id.description)).check(matches(withText("Hello There")));
        onView(withId(R.id.languages)).check(matches(withText("English")));
        onView(withId(R.id.production_locations)).check(matches(withText("USA")));
        onView(withId(R.id.releases)).check(matches(
                withText(containsString("Great Britain"))));
    }

    @Test
    public void testChangeMovieDetailDataDiscardChanges() {
        selectMenuItemAndEnterEdit("Pulp Fiction");

        onView(withId(R.id.title_input))
                .perform(clearText(),
                        typeText("Star Wars 2"),
                        closeSoftKeyboard()
                );

        pressBack();

        onView(withText("Do you really want to discard changes?"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText("Yes"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());

        // test
        onView(withText("Pulp Fiction")).check(matches(isDisplayed()));
    }
}
