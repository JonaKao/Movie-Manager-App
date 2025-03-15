package de.uhd.ifi.se.moviemanager.ui.dialog;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.Performer;

public class PerformerSafeRemovalDialog {

    private PerformerSafeRemovalDialog() {
    }

    public static void showIfNecessary(FragmentActivity context,
                                       List<Performer> performers,
                                       Consumer<List<Performer>> positiveListener,
                                       Runnable negativeListener,
                                       Runnable defaultAction) {
        List<Performer> performersToRemove = getInvalidPerformers(performers);
        if (!performersToRemove.isEmpty()) {
            show(context, performersToRemove, positiveListener,
                    negativeListener);
        } else {
            defaultAction.run();
        }
    }

    public static void show(FragmentActivity context,
                            List<Performer> performers,
                            Consumer<List<Performer>> positiveListener,
                            Runnable negativeListener) {
        List<Performer> performersToRemove = getInvalidPerformers(performers);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.warning)
                .setMessage(context.getString(R.string.warning_linked_performers_removal,
                        performersToRemove.size()) + "\n \n" + performersToRemove.stream().map(Performer::getName)
                        .collect(Collectors.joining(", ")))
                .setPositiveButton(R.string.yes, (dialog, which) -> positiveListener
                        .accept(performersToRemove))
                .setNegativeButton(R.string.no, (dialog, which) -> negativeListener
                        .run()).show();
    }

    /**
     * Returns a list of performers with no more than one linked movie. Unlinking a movie from any of
     * these performers would result in an invalid state.
     *
     * @param linkedPerformer
     * @return list of performers with no more than one linked movie
     */
    public static List<Performer> getInvalidPerformers(
            List<Performer> linkedPerformer) {
        List<Performer> performersToRemove = new ArrayList<>();
        for (Performer performer : linkedPerformer) {
            if (performer.getMovies().size() <= 1) {
                performersToRemove.add(performer);
            }
        }
        return performersToRemove;
    }
}
