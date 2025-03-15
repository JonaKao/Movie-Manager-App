package de.uhd.ifi.se.moviemanager.ui.detail;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.MovieRelease;
import de.uhd.ifi.se.moviemanager.model.Performer;
import de.uhd.ifi.se.moviemanager.ui.dialog.DateSelectionDialog;
import de.uhd.ifi.se.moviemanager.ui.dialog.PerformerSafeRemovalDialog;
import de.uhd.ifi.se.moviemanager.util.DateUtils;
import de.uhd.ifi.se.moviemanager.util.Listeners;

/**
 * Responsible for the Movie DetailEditView.
 */
public class MovieDetailEditActivity extends DetailEditActivity<Movie> {
    private EditText descriptionEditText;
    private EditText runtimeEditText;
    private EditText ratingEditText;
    private EditText linkedPerformersEditText;
    private TextInputLayout watchDateInput;
    private EditText watchDateEditText;
    private EditText languagesEditText;
    private TextInputLayout releaseInput;
    private EditText releaseEditText;
    private EditText productionLocationsEditText;

    private final ArrayList<MovieRelease> movieReleases = new ArrayList<>();
    private ArrayList<Performer> linkedPerformers = new ArrayList<>();

    public MovieDetailEditActivity() {
        super(R.layout.activity_movie_detail_edit);
    }

    @Override
    protected void initViewItems() {
        descriptionEditText = findViewById(R.id.description_input);
        runtimeEditText = findViewById(R.id.runtime_input);
        ratingEditText = findViewById(R.id.rating_input);
        linkedPerformersEditText = findViewById(R.id.linked_performers_input);
        linkedPerformersEditText.setInputType(InputType.TYPE_NULL); // hides the keyboard on click
        watchDateInput = findViewById(R.id.edit_watch_date);
        watchDateEditText = findViewById(R.id.watch_date_input);
        watchDateEditText.setInputType(InputType.TYPE_NULL); //hides the keyboard on click
        languagesEditText = findViewById(R.id.languages_input);
        releaseInput = findViewById(R.id.edit_releases);
        releaseEditText = findViewById(R.id.releases_input);
        releaseEditText.setInputType(InputType.TYPE_NULL); //hides the keyboard on click
        productionLocationsEditText = findViewById(R.id.production_location_input);
        nameInput = findViewById(R.id.edit_title);
        nameEditText = findViewById(R.id.title_input);
        initEditableImageView();
    }

    @Override
    protected Movie getObject(int id) {
        Movie movie = null;
        if (id >= 0) {
            movie = model.getMovieById(id).orElse(null);
        }
        return movie;
    }

    @Override
    protected void initForCreation() {
        currentObject = new Movie();
        setResetImageButtonEnabled(false);
    }

    @Override
    protected void initForUpdate() {
        setInitialStateFrom(currentObject);
    }

    private void setInitialStateFrom(Movie movie) {
        nameEditText.setText(Optional.ofNullable(movie.getTitle())
                .orElse(""));
        descriptionEditText.setText(movie.getDescription());
        runtimeEditText.setText(movie.getRuntime() > 0 ?
                String.valueOf(movie.getRuntime()) : "");
        ratingEditText.setText(movie.getRating() >= 0 ?
                String.valueOf(movie.getRating()) : "");
        linkedPerformers.addAll(movie.getPerformers());

        linkedPerformersEditText.setText(linkedPerformers.stream()
                .map(Performer::getName)
                .collect(Collectors.joining(", ")));
        watchDateEditText.setText(DateUtils.dateToText(movie.getWatchDate()));
        productionLocationsEditText.setText(Optional.ofNullable(movie.getProductionLocations())
                .map(locations -> String.join(", ", locations))
                .orElse(""));
        languagesEditText.setText(Optional.ofNullable(movie.getLanguages())
                .map(langs -> String.join(", ", langs))
                .orElse(""));
        movieReleases.addAll(movie.getReleases());
        releaseEditText.setText(movieReleases.stream().map(movieRelease ->
                DateUtils.dateToText(movieRelease.getDate()) + " in " +
                        movieRelease.getLocation()).collect(Collectors.joining(", ")));
        if (!watchDateEditText.getText().toString().isEmpty())
            watchDateInput.setEndIconDrawable(R.drawable.baseline_clear_24);
        if (!releaseEditText.getText().toString().isEmpty())
            releaseInput.setEndIconDrawable(R.drawable.baseline_clear_24);
    }

    @Override
    protected void registerSpecificListeners() {
        linkedPerformersEditText.setOnFocusChangeListener((v, hasFocus) ->
                ifHasFocus(v, hasFocus, this::showPerformerSelectionDialog));
        linkedPerformersEditText.setOnClickListener(this::showPerformerSelectionDialog);

        watchDateEditText.setOnFocusChangeListener((v, hasFocus) ->
                ifHasFocus(v, hasFocus, view -> showDatePickerDialog(watchDateInput)));
        watchDateEditText.setOnClickListener(v -> showDatePickerDialog(watchDateInput));
        watchDateInput.setEndIconOnClickListener(this::onWatchDateClear);

        releaseEditText.setOnFocusChangeListener((v, hasFocus) ->
                ifHasFocus(v, hasFocus, view -> showReleaseDialog()));
        releaseEditText.setOnClickListener(v -> showReleaseDialog());
        releaseInput.setEndIconOnClickListener(this::onReleaseClear);

        descriptionEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        runtimeEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        ratingEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        linkedPerformersEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        watchDateEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        languagesEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        releaseEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        productionLocationsEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
    }

    private void ifHasFocus(View v, boolean hasFocus, Consumer<View> action) {
        if (hasFocus) {
            action.accept(v);
        }
    }

    private void onWatchDateClear(View view) {
        // Prüfen ob Film ausgeliehen ist
        if (currentObject.isLent()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cannot Change Watch Status")
                    .setMessage("The movie \"" + currentObject.getName() +
                            "\" cannot have its watch status changed while it is lent out.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }
        if (!watchDateEditText.getText().toString().isEmpty()) {
            watchDateEditText.getText().clear();
            watchDateInput.setEndIconDrawable(R.drawable.baseline_arrow_drop_down_24);
        } else {
            showDatePickerDialog(watchDateInput);
        }
    }

    private void onReleaseClear(View view) {
        if (!releaseEditText.getText().toString().isEmpty()) {
            releaseEditText.getText().clear();
            movieReleases.clear();
            releaseInput.setEndIconDrawable(R.drawable.baseline_arrow_drop_down_24);
        } else {
            showReleaseDialog();
        }
    }

    private void showReleaseDialog() {
        var createReleaseDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Add a release")
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    EditText editTitle = ((AlertDialog) dialog).findViewById(R.id.release_title_input);
                    EditText editDate = ((AlertDialog) dialog).findViewById(R.id.release_date_input);

                    if (editTitle == null || editDate == null ||
                            editTitle.getText().toString().isEmpty() ||
                            editDate.getText().toString().isEmpty())
                        return;

                    var title = editTitle.getText().toString();
                    var date = DateUtils.textToDate(editDate.getText().toString());

                    if (title.isEmpty() || date == null)
                        return;

                    movieReleases.add(new MovieRelease(title, date));
                    updateReleaseText();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create();

        View content = createReleaseDialog.getLayoutInflater()
                .inflate(R.layout.dialog_release_creation, createReleaseDialog.getListView());

        TextInputLayout dateInput = content.findViewById(R.id.edit_releases_date);
        EditText dateEditText = content.findViewById(R.id.release_date_input);
        dateEditText.setInputType(InputType.TYPE_NULL);

        dateEditText.setOnFocusChangeListener((v, hasFocus) ->
                ifHasFocus(v, hasFocus, view -> showDatePickerDialog(dateInput)));
        dateEditText.setOnClickListener(v -> showDatePickerDialog(dateInput));

        createReleaseDialog.setView(content);
        createReleaseDialog.show();
    }

    private void updateReleaseText() {
        releaseEditText.setText(movieReleases.stream().map(movieRelease ->
                DateUtils.dateToText(movieRelease.getDate()) + " in " +
                        movieRelease.getLocation()).collect(Collectors.joining(", ")));
        releaseInput.setEndIconDrawable(R.drawable.baseline_clear_24);
    }

    private void showDatePickerDialog(TextInputLayout inputLayout) {
        // Prüfen ob Film ausgeliehen ist bevor WatchDate gesetzt werden kann
        if (currentObject.isLent()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cannot Mark as Watched")
                    .setMessage("The movie \"" + currentObject.getName() +
                            "\" cannot be marked as watched while it is lent out.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }
        DateSelectionDialog dialog = DateSelectionDialog
                .create(DateUtils.nowAtMidnight(), null, DateUtils.nowAtMidnight());

        dialog.setPositiveButtonListener(date -> {
            Optional.ofNullable(inputLayout.getEditText())
                    .ifPresent(editText -> editText.setText(DateUtils.dateToText(date)));
            inputLayout.setEndIconDrawable(R.drawable.baseline_clear_24);
        });
        dialog.show(getSupportFragmentManager(), "select date");
    }

    private void showPerformerSelectionDialog(View view) {
        if (model.getPerformers().isEmpty()) {
            Toast.makeText(this, R.string.no_performers_available, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Performer> performers = new ArrayList<>(model.getPerformers());

        //check the performers that are already linked
        boolean[] alreadyLinked = new boolean[model.getPerformers().size()];
        for (int i = 0; i < model.getPerformers().size(); i++) {
            alreadyLinked[i] = linkedPerformers.contains(performers.get(i));
        }

        ArrayList<Performer> modifiedLinkedPerformers = new ArrayList<>(linkedPerformers);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_performers)
                .setMultiChoiceItems(
                        performers.stream().map(Performer::getName).toArray(String[]::new),
                        alreadyLinked,
                        (dialog, which, isChecked) -> {
                            if (isChecked) {
                                modifiedLinkedPerformers.add(performers.get(which));
                            } else {
                                modifiedLinkedPerformers.remove(performers.get(which));
                            }
                        })
                .setPositiveButton(R.string.ok, (dialog, which) -> showPerformerUnlinkedWarningIfNecessary(linkedPerformers, modifiedLinkedPerformers))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showPerformerUnlinkedWarningIfNecessary(List<Performer> previouslyLinked,
                                                     ArrayList<Performer> currentlyLinked) {
        String unlinkedPerformers = previouslyLinked.stream()
                .filter(p -> !currentlyLinked.contains(p))
                .map(Performer::getName)
                .collect(Collectors.joining (", "));

        if(unlinkedPerformers.isEmpty()) {
            linkedPerformers = currentlyLinked;
            updateLinkedPerformerEditText();
            return;
        }

        String removedPerformers = previouslyLinked.stream()
                .filter(p -> !currentlyLinked.contains(p) && p.getMovies().size() <= 1)
                .map(Performer::getName)
                .collect(Collectors.joining (", "));

        String removedPerformersMessage = removedPerformers.isEmpty() ? "": String.format("This will delete%n%n%s%n%n.", removedPerformers);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Warning")
                .setMessage(String.format("You are about to unlink%n%n%s%n%n%sAre you sure?", unlinkedPerformers, removedPerformersMessage))
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                            linkedPerformers = currentlyLinked;
                            updateLinkedPerformerEditText();
                            dialogInterface.dismiss();
                        })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }


    private void updateLinkedPerformerEditText() {
        linkedPerformersEditText.setText(
                linkedPerformers.stream()
                        .map(Performer::getName)
                        .collect(Collectors.joining(", ")));
    }

    @Override
    protected void showWarnings() {
        if (getCurrentName().isEmpty()) {
            nameInput.setError(getString(R.string.warning_movie_title));
        } else {
            nameInput.setError(null);
        }
    }

    @Override
    protected boolean areConstraintsFulfilled() {
        return !getCurrentName().isEmpty() && getInvalidPerformers().isEmpty();
    }

    private List<Performer> getInvalidPerformers() {
        if (currentObject.getPerformers() == null) {
            return Collections.emptyList();
        }

        List<Performer> unlinkedPerformer = model.getPerformers().stream()
                .filter(performer -> currentObject.getPerformers().contains(performer))
                .filter(performer -> !linkedPerformers.contains(performer))
                .collect(Collectors.toList());
        return PerformerSafeRemovalDialog
                .getInvalidPerformers(unlinkedPerformer);
    }

    @Override
    protected void enableOrDisableSaveButton() {
        // the button is also enabled if a performer has no movies linked
        // anymore but then a dialog
        // is shown that warns that the performer will be deleted
        boolean isButtonEnabled = isChanged && !getCurrentName().isEmpty();
        if (commitItem != null) {
            commitItem.setEnabled(isButtonEnabled);
        }
    }

    @Override
    protected void showCommitWarnings() {
        PerformerSafeRemovalDialog.show(this, getInvalidPerformers(), li -> {
            getInvalidPerformers().forEach(storage::deletePerformerFile);
            saveAndFinish();
        }, () -> {
        });
    }

    @Override
    protected void onSave() {
        currentObject.setImage(imageView.getDrawable());
        currentObject.setTitle(getCurrentName());
        currentObject.setDescription(descriptionEditText.getText().toString());
        currentObject.setRuntime(getRuntimeFromView());
        currentObject.setRating(getRatingFromView());
        currentObject.setLanguages(getLanguagesFromView());
        currentObject.setProductionLocations(getProductionLocationsFromView());
        currentObject.setWatchDate(DateUtils.textToDate(watchDateEditText.getText().toString()));
        currentObject.setReleases(movieReleases);

        updateLinkedElements();
        model.addMovie(currentObject);
        currentObject.calculateOverallRating();
        storage.saveMovieToFile(currentObject);
    }

    /**
     * Grabs the runtime from the view and returns it as an int.
     * Returns 0 if the runtime is not a number.
     *
     * @return the runtime as an int
     */
    private int getRuntimeFromView() {
        int runtime = 0;
        try {
            runtime = Integer.parseInt(runtimeEditText.getText().toString());
        } catch (NumberFormatException e) {
            // do nothing
        }
        return runtime;
    }

    /**
     * Grabs the rating from the view and returns it as an double.
     * Returns -1 if the rating is not a number. Or not in the range of 0 to 5.
     *
     * @return the rating as an double
     */
    private double getRatingFromView() {
        double rating = -1;
        try {
            rating = Double.parseDouble(ratingEditText.getText().toString());
            rating = Math.round(rating * 10d) / 10d;
        } catch (NumberFormatException e) {
            // do nothing
        }
        if (rating > 5 || rating < 0) {
            rating = -1;
        }
        return rating;
    }

    /**
     * Grabs the languages from the view and returns it as a list of strings.
     * Returns null if the languages are empty.
     *
     * @return the languages as a list of strings
     */
    @Nullable
    private List<String> getLanguagesFromView() {
        return languagesEditText.getText().toString().isEmpty() ?
                null : Arrays.stream(languagesEditText.getText().toString().split(", "))
                .collect(Collectors.toList());
    }

    /**
     * Grabs the production locations from the view and returns it as a list of strings.
     * Returns null if the production locations are empty.
     *
     * @return the production locations as a list of strings
     */
    @Nullable
    private List<String> getProductionLocationsFromView() {
        return productionLocationsEditText.getText().toString().isEmpty() ?
                null : Arrays.stream(productionLocationsEditText.getText().toString().split(", "))
                .collect(Collectors.toList());
    }

    private void updateLinkedElements() {
        var addedPerformers = linkedPerformers.stream()
                .filter(performer -> !currentObject.getPerformers().contains(performer));
        var removedPerformers = currentObject.getPerformers().stream()
                .filter(performer -> !linkedPerformers.contains(performer));


        addedPerformers.forEach(currentObject::link);
        removedPerformers.forEach(currentObject::unlink);
    }
}
