package de.uhd.ifi.se.moviemanager.ui.master;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.ui.adapter.DataRVAdapter;
import de.uhd.ifi.se.moviemanager.ui.detail.MovieDetailActivity;
import de.uhd.ifi.se.moviemanager.ui.detail.MovieDetailEditActivity;
import de.uhd.ifi.se.moviemanager.ui.dialog.PerformerSafeRemovalDialog;
import de.uhd.ifi.se.moviemanager.ui.master.comparator.NameComparator;
import de.uhd.ifi.se.moviemanager.ui.master.comparator.OverallRatingComparator;
import de.uhd.ifi.se.moviemanager.ui.master.comparator.RatingComparator;
import de.uhd.ifi.se.moviemanager.ui.master.comparator.ReturnDateComparator;
import de.uhd.ifi.se.moviemanager.ui.master.comparator.WatchDateComparator;
import de.uhd.ifi.se.moviemanager.ui.view.SortingMenuItem;

/**
 * Movie MasterView
 */
public class MovieMasterFragment extends DataMasterFragment<Movie> {

    @Override
    protected void addSortingMenuItems() {
        String title = getString(R.string.movie_criterion_title);
        String rating = getString(R.string.movie_criterion_rating);
        String overallRating = getString(
                R.string.movie_criterion_overall_rating);
        String watchDate = getString(R.string.movie_criterion_watched);
        // Neue Strings für die Return-Date Sortierung
        String returnDate = getString(R.string.movie_criterion_return_date);
        String overdue = getString(R.string.movie_criterion_overdue);

        sortingMenuItems.add(new SortingMenuItem<>(title,
                new NameComparator<>(Movie::getOverallRatingInStars), true));
        sortingMenuItems
                .add(new SortingMenuItem<>(rating, new RatingComparator<>()));
        sortingMenuItems.add(new SortingMenuItem<>(overallRating,
                new OverallRatingComparator()));
        sortingMenuItems.add(new SortingMenuItem<>(watchDate,
                new WatchDateComparator()));
        // Neue Menüeinträge für Rückgabedatum und überfällige Filme
        sortingMenuItems.add(new SortingMenuItem<>(returnDate,
                new ReturnDateComparator()));
    }

    @Override
    protected DataRVAdapter<Movie> createAdapter() {
        final String constraint = "";
        adapter = new DataRVAdapter<>(this, sortingMenuItems.get(0),
                originalData, constraint);
        adapter.setDetailActivity(MovieDetailActivity.class);
        adapter.setDetailEditActivity(MovieDetailEditActivity.class);
        adapter.setRemoveFromStorageMethod(this::warnAndRemoveFromStorage);
        return adapter;
    }

    @Override
    protected void afterCreation() {
        originalData.clear();
        originalData.addAll(model.getMovies());
        adapter.sortAndFilter();
    }

    @Override
    protected void warnAndRemoveFromStorage(Movie movie) {
        // Prüfen ob der Film ausgeliehen ist
        if (movie.isLent()) {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Cannot Delete Movie")
                    .setMessage("The movie \"" + movie.getName() + "\" cannot be deleted while it is lent out.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }
        PerformerSafeRemovalDialog
                .showIfNecessary(getActivity(), movie.getPerformers(), performersToDelete -> {
                    performersToDelete.forEach(storage::deletePerformerFile);
                    storage.deleteMovieFile(movie);
                    adapter.removeModelObject(movie);
                }, () -> {
                }, () -> new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Warning")
                        .setMessage(getString(R.string.deletion_warning_message,
                                movie.getName()))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            storage.deleteMovieFile(movie);
                            adapter.removeModelObject(movie);
                            dialog.dismiss();
                        })
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show());
    }
}
