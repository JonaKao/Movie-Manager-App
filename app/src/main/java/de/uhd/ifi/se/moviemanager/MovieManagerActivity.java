package de.uhd.ifi.se.moviemanager;

import static de.uhd.ifi.se.moviemanager.ui.master.DataMasterFragment.newMovieFragmentInstance;
import static de.uhd.ifi.se.moviemanager.ui.master.DataMasterFragment.newPerformerFragmentInstance;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.elevation.SurfaceColors;

import java.util.Set;

import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.MovieManagerModel;
import de.uhd.ifi.se.moviemanager.model.Performer;
import de.uhd.ifi.se.moviemanager.storage.StorageManagerAccess;
import de.uhd.ifi.se.moviemanager.ui.master.SearchMasterFragment;
import de.uhd.ifi.se.moviemanager.util.ActivityUtils;

/**
 * Main activity of the movie manager app. Run this activity to start the app.
 * The data objects and their associations are managed in the {@link
 * MovieManagerModel} class.
 */
public class MovieManagerActivity extends AppCompatActivity {
    private static final StorageManagerAccess STORAGE = StorageManagerAccess.getInstance();
    private final MovieManagerModel model = MovieManagerModel.getInstance();
    public static final String STORAGE_NAME = "movie_manager";
    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        setSupportActionBar(findViewById(R.id.topAppBar));
        STORAGE.openMovieManagerStorage(this);
        initViewItems();
        navigationView.setSelectedItemId(R.id.bottom_navigation_movies);
        openInitialFragment();
        navigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
        ActivityUtils.setStatusBarColor(this);
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setThreadPolicy();
    }


    private void initViewItems() {
        navigationView = findViewById(R.id.bottom_navigation);
    }

    private void setThreadPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void openInitialFragment() {
        Fragment fragment = createMoviesFragment();
        openFragment(fragment, false);
    }

    private Fragment createMoviesFragment() {
        Set<Movie> movies = model.getMovies();
        final int menuId = R.string.bottom_navigation_menu_movies;
        return newMovieFragmentInstance(menuId, movies);
    }

    private boolean onNavigationItemSelected(MenuItem menuItem) {
        Fragment fragment = null;
        int itemId = menuItem.getItemId();

        if (itemId == R.id.bottom_navigation_movies) {
            fragment = createMoviesFragment();
        } else if (itemId == R.id.bottom_navigation_performers) {
            fragment = createPerformersFragment();
        } else if (itemId == R.id.bottom_navigation_search) {
            fragment = createSearchFragment();
        }

        if (fragment != null) {
            openFragment(fragment);
        }

        return fragment != null;
    }

    private Fragment createPerformersFragment() {
        Set<Performer> performers = model.getPerformers();
        final int menuId = R.string.bottom_navigation_menu_performers;
        return newPerformerFragmentInstance(menuId, performers);
    }

    private Fragment createSearchFragment() {
        final int menuId = R.string.bottom_navigation_menu_search;
        return SearchMasterFragment.newInstance(menuId);
    }

    private void openFragment(Fragment fragment) {
        openFragment(fragment, true);
    }

    private void openFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        } else {
            transaction.disallowAddToBackStack();
        }
        transaction.commit();
    }

    public void setBottomNavigationTo(@StringRes int nameId) {
        navigationView.setOnItemSelectedListener(null);
        switch (nameId) {
            case R.string.bottom_navigation_menu_movies ->
                    navigationView.setSelectedItemId(R.id.bottom_navigation_movies);
            case R.string.bottom_navigation_menu_performers ->
                    navigationView.setSelectedItemId(R.id.bottom_navigation_performers);
            case R.string.bottom_navigation_menu_search ->
                    navigationView.setSelectedItemId(R.id.bottom_navigation_search);
            default -> throw new IllegalStateException("Unexpected value: " + nameId);
        }
        navigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }
}
