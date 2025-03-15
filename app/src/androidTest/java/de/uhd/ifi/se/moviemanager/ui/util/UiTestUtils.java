package de.uhd.ifi.se.moviemanager.ui.util;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Checks.checkNotNull;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.app.Activity;
import android.view.InputDevice;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;

import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.MovieManagerModel;
import de.uhd.ifi.se.moviemanager.storage.StorageManagerAccess;

public class UiTestUtils {

    public static final StorageManagerAccess STORAGE = StorageManagerAccess
            .getInstance();

    /**
     * @param criterion    name of the sorting criterion that should be used.
     * @param isDescending true if sorting should be done in descending
     *                     direction.
     */
    public static void selectCriterionAndDirection(String criterion,
                                                   boolean isDescending) {
        int numberOfClicks = isDescending ? 2 : 1;
        while (numberOfClicks > 0) {
            clickCriterionButton();
            onView(withText(criterion)).inRoot(isPlatformPopup())
                    .perform(click());
            --numberOfClicks;
        }
    }

    public static void clickCriterionButton() {
        onView(withId(R.id.sort)).perform(click());
    }

    public static Matcher<View> childAtPosition(Matcher<View> parentMatcher,
                                                int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(
                        "Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher
                        .matches(parent) && view
                        .equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static Matcher<View> atPosition(int position,
                                           @NonNull Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description
                        .appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view
                        .findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    public static ViewAction clickXY(int x, int y) {
        return new GeneralClickAction(Tap.SINGLE, view -> {

            int[] screenPos = new int[2];
            view.getLocationOnScreen(screenPos);

            float screenX = screenPos[0] + x;
            float screenY = screenPos[1] + y;
            float[] coordinates = {screenX, screenY};

            return coordinates;
        }, Press.FINGER, InputDevice.SOURCE_TOUCHSCREEN, BUTTON_PRIMARY);
    }

    public static void selectMenuItemAndEnterEdit(String itemName) {
        onView(withId(R.id.model_objects_with_image)).perform(
                RecyclerViewActions
                        .actionOnItem(hasDescendant(withText(itemName)),
                                click()));
        onView(withId(R.id.edit)).perform(click());
    }

    public static void openStorage(Activity activity) {
        STORAGE.openMovieManagerStorage(activity);
    }

    public static void clearStorage() {
        STORAGE.clear();
        MovieManagerModel.getInstance().clear();
    }

    public static ViewAction clickChildViewWithId(int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }

    public static ViewAction setDate(final int year, final int monthOfYear, final int dayOfMonth) {

        return new ViewAction() {

            @Override
            public void perform(UiController uiController, View view) {
                //select month + year
                int rv_position = monthOfYear + (year - 1900) * 12 - 1;
                RecyclerView months = view.findViewById(com.google.android.material.R.id.mtrl_calendar_months);
                months.scrollToPosition(rv_position);

                uiController.loopMainThreadUntilIdle();

                //select day
                GridView dayGridView = months.getChildAt(1).findViewById(com.google.android.material.R.id.month_grid);
                ArrayList<View> day = new ArrayList<>();
                dayGridView.findViewsWithText(day, String.valueOf(dayOfMonth), View.FIND_VIEWS_WITH_TEXT);

                int dayPosition = dayGridView.getPositionForView(day.get(0));
                dayGridView.performItemClick(day.get(0), dayPosition, dayGridView.getItemIdAtPosition(dayPosition));

                uiController.loopMainThreadUntilIdle();
            }

            @Override
            public String getDescription() {
                return "set date";
            }

            @Override
            public Matcher<View> getConstraints() {
                return allOf(withId(com.google.android.material.R.id.mtrl_calendar_main_pane), isDisplayed());
            }
        };
    }
}
