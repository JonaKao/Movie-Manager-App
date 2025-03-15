package de.uhd.ifi.se.moviemanager.ui.movie;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.clickXY;
import static de.uhd.ifi.se.moviemanager.ui.util.UiTestUtils.openStorage;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
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
import de.uhd.ifi.se.moviemanager.model.Performer;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class RemoveMovieWithPerformersTest {

    private static final MovieManagerModel model = MovieManagerModel
            .getInstance();
    @Rule
    public ActivityScenarioRule<MovieManagerActivity> activityScenarioRule =
            new ActivityScenarioRule<>(
            MovieManagerActivity.class);

    @BeforeClass
    public static void initStorage() {
        ActivityScenario<MovieManagerActivity> activityScenario = launch(
                MovieManagerActivity.class);
        activityScenario.onActivity(activity -> {
            openStorage(activity);
            clearStorage();
            populateStorageWithMoviesAndPerformers();
        });
    }

    private static void populateStorageWithMoviesAndPerformers() {
        Movie starWars = STORAGE.saveMovieToFile(new Movie("Star Wars"));
        model.addMovie(starWars);

        Movie red = STORAGE.saveMovieToFile(new Movie("R.E.D."));
        model.addMovie(red);

        Movie pulpFiction = STORAGE.saveMovieToFile(new Movie("Pulp Fiction"));
        model.addMovie(pulpFiction);

        Performer performer1 = new Performer("Harrison Ford");
        model.addPerformer(performer1);

        Performer performer2 = new Performer("Samuel Leroy Jackson");
        model.addPerformer(performer2);

        Performer performer3 = new Performer("Karl Urban");
        model.addPerformer(performer3);

        starWars.link(performer1);
        pulpFiction.link(performer2);
        red.link(performer3);
        starWars.link(performer2);
        STORAGE.savePerformerToFile(performer1);
        STORAGE.savePerformerToFile(performer2);
        STORAGE.savePerformerToFile(performer3);
    }

    @AfterClass
    public static void clearStorageAfterClass() {
        clearStorage();
    }

    @Test
    public void testRemoveMovieNoConfirmation() {
        onView(withId(R.id.model_objects_with_image)).perform(
                RecyclerViewActions
                        .actionOnItem(hasDescendant(withText("Star Wars")),
                                swipeRight()));
        onView(withId(R.id.model_objects_with_image)).perform(
                RecyclerViewActions
                        .actionOnItem(hasDescendant(withText("Star Wars")),
                                click()));

        onView(withId(R.id.model_objects_with_image))
                .check(matches(hasDescendant(withText("Star Wars"))));
    }

    @Test
    public void testRemoveMovieWithPerformerOnlyLinkedToTheMovieWithCancellation() {
        selectAndConfirmDeletion("Star Wars");
        onView(withText(R.string.no))
                .inRoot(isDialog()).perform(click());
        onView(withId(R.id.model_objects_with_image))
                .check(matches(hasDescendant(withText("Star Wars"))));
    }

    @Test
    public void testRemoveMovieWithPerformerOnlyLinkedToTheMovieNoCancellation() {
        selectAndConfirmDeletion("R.E.D.");
        onView(withText(R.string.yes))
                .inRoot(isDialog()).perform(click());
        onView(withId(R.id.model_objects_with_image))
                .check(matches(not(hasDescendant(withText("R.E.D.")))));
    }

    @Test
    public void testRemoveMovieWithoutPerformerOnlyLinkedToTheMovie() {
        selectAndConfirmDeletion("Pulp Fiction");
        onView(withText("Yes")).perform(click());
        onView(withId(R.id.model_objects_with_image))
                .check(matches(not(hasDescendant(withText("Pulp Fiction")))));
    }

    private void selectAndConfirmDeletion(String itemName) {
        onView(withId(R.id.model_objects_with_image)).perform(
                RecyclerViewActions
                        .actionOnItem(hasDescendant(withText(itemName)),
                                swipeRight()));
        onView(withId(R.id.model_objects_with_image)).perform(
                RecyclerViewActions
                        .actionOnItem(hasDescendant(withText(itemName)),
                                clickXY(-26, 0)));
    }
}
