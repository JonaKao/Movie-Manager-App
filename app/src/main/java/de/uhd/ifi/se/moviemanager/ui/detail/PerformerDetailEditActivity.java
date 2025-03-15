package de.uhd.ifi.se.moviemanager.ui.detail;

import static java.util.stream.Collectors.toList;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.uhd.ifi.se.moviemanager.MovieManagerActivity;
import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.Performer;
import de.uhd.ifi.se.moviemanager.ui.dialog.DateSelectionDialog;
import de.uhd.ifi.se.moviemanager.util.DateUtils;
import de.uhd.ifi.se.moviemanager.util.Listeners;

/**
 * Responsible for the Performer DetailView.
 */
public class PerformerDetailEditActivity extends DetailEditActivity<Performer> {
    private EditText birthNameEditText;
    private EditText biographyEditText;
    private EditText ratingEditText;
    private TextInputLayout birthDateInput;
    private EditText birthDateEditText;
    private EditText occupationsEditText;
    private EditText linkedMoviesEditText;

    private ArrayList<Movie> linkedMovies = new ArrayList<>();

    public PerformerDetailEditActivity() {
        super(R.layout.activity_performer_detail_edit);
    }

    @Override
    protected void initViewItems() {
        imageView = findViewById(R.id.edit_image);
        nameInput = findViewById(R.id.edit_name);
        nameEditText = findViewById(R.id.name_input);
        birthNameEditText = findViewById(R.id.birthname_input);
        biographyEditText = findViewById(R.id.biography_input);
        ratingEditText = findViewById(R.id.rating_input);
        birthDateInput = findViewById(R.id.edit_date_of_birth);
        birthDateEditText = findViewById(R.id.date_of_birth_input);
        birthDateEditText.setInputType(InputType.TYPE_NULL);          // hides the keyboard on click
        occupationsEditText = findViewById(R.id.occupations_input);
        linkedMoviesEditText = findViewById(R.id.linked_movies_input);
        linkedMoviesEditText.setInputType(InputType.TYPE_NULL);       // hides the keyboard on click

        initEditableImageView();
    }

    @Override
    protected void initForCreation() {
        currentObject = new Performer();
        showMovieSelectionDialog();
        setResetImageButtonEnabled(false);
    }

    @Override
    protected Performer getObject(int id) {
        Performer result = null;
        if (id >= 0) {
            result = model.getPerformerById(id).orElse(null);
        }
        return result;
    }

    @Override
    protected void initForUpdate() {
        birthNameEditText.setText(currentObject.getBirthName());
        biographyEditText.setText(currentObject.getBiography());
        ratingEditText.setText(currentObject.getRating() >= 0 ? String.valueOf(currentObject.getRating())
                : "");
        birthDateEditText.setText(DateUtils.dateToText(currentObject.getDateOfBirth()));
        if (currentObject.getDateOfBirth() != null)
            birthDateInput.setEndIconDrawable(R.drawable.baseline_clear_24);

        occupationsEditText.setText(String.join(", ", currentObject.getOccupations()));

        linkedMoviesEditText.setText(Optional.ofNullable(currentObject.getMovies())
                .map(movies -> movies.stream().map(Movie::getName).collect(Collectors.joining(", ")))
                .orElse(""));
        linkedMovies.addAll(currentObject.getMovies());
        nameEditText.setText(currentObject.getName());
    }

    @Override
    protected void registerSpecificListeners() {
        birthDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePickerDialog(birthDateInput, birthDateEditText);
            }
        });
        birthDateEditText.setOnClickListener(v ->
                showDatePickerDialog(birthDateInput, birthDateEditText));
        birthDateInput.setEndIconOnClickListener(v -> {
            if (birthDateEditText.getText().toString().isEmpty()) {
                showDatePickerDialog(birthDateInput, birthDateEditText);
            } else {
                birthDateEditText.setText("");
                birthDateInput.setEndIconDrawable(R.drawable.baseline_arrow_drop_down_24);
            }
        });

        linkedMoviesEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showMovieSelectionDialog();
            }
        });
        linkedMoviesEditText.setOnClickListener(v -> showMovieSelectionDialog());

        birthNameEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        biographyEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        ratingEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        birthDateEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        occupationsEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
        linkedMoviesEditText
                .addTextChangedListener(Listeners.createOnTextChangedListener(this::setChanged));
    }

    private void showMovieSelectionDialog() {
        List<Movie> movies = new ArrayList<>(model.getMovies());

        //check the performers that are already linked
        boolean[] alreadyLinked = new boolean[movies.size()];
        for (int i = 0; i < movies.size(); i++) {
            alreadyLinked[i] = linkedMovies.contains(movies.get(i));
        }

        ArrayList<Movie> modifiedLinkedMovies = new ArrayList<>(linkedMovies);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_movies)
                .setMultiChoiceItems(
                        movies.stream().map(Movie::getName).toArray(String[]::new),
                        alreadyLinked,
                        (dialog, which, isChecked) -> {
                            if (isChecked) {
                                modifiedLinkedMovies.add(movies.get(which));
                            } else {
                                modifiedLinkedMovies.remove(movies.get(which));
                            }
                        })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    linkedMovies = modifiedLinkedMovies;
                    linkedMoviesEditText.setText(linkedMovies.stream()
                            .map(Movie::getName)
                            .collect(Collectors.joining(", ")));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Shows a date picker dialog and sets the selected date to the given input layout.
     */
    private void showDatePickerDialog(TextInputLayout inputLayout, EditText editText) {
        var datePicker = DateSelectionDialog
                .create(DateUtils.nowAtMidnight(), null, DateUtils.nowAtMidnight());

        datePicker.setPositiveButtonListener(date -> {
            editText.setText(DateUtils.dateToText(date));
            inputLayout.setEndIconDrawable(R.drawable.baseline_clear_24);
        });

        datePicker.show(getSupportFragmentManager(), "select watch date");
    }


    @Override
    protected boolean areConstraintsFulfilled() {
        return !getCurrentName().isEmpty() && !linkedMovies.isEmpty();
    }

    @Override
    protected void showWarnings() {
        if (getCurrentName().isEmpty()) {
            nameInput.setError(getString(R.string.warning_performer_name));
        } else {
            nameInput.setError(null);
        }

        if (linkedMovies.isEmpty()) {
            linkedMoviesEditText.setError(getString(R.string.warning_performer_minimum_movies));
        } else {
            linkedMoviesEditText.setError(null);
        }
    }

    @Override
    protected void onSave() {
        currentObject.setName(getCurrentName());
        currentObject.setImage(imageView.getDrawable());
        currentObject.setBirthName(birthNameEditText.getText().toString());
        currentObject.setBiography(biographyEditText.getText().toString());

        double rating = -1;
        try {
            rating = Double.parseDouble(ratingEditText.getText().toString());
        } catch (NumberFormatException e) {
            //do nothing
        }
        currentObject.setRating(rating);
        currentObject.setDateOfBirth(DateUtils.textToDate(birthDateEditText.getText().toString()));
        currentObject.setOccupations(
                Arrays.stream(occupationsEditText.getText().toString().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(toList()));


        model.addPerformer(currentObject);

        var addedMovies = linkedMovies.stream()
                .filter(performer -> !currentObject.getMovies().contains(performer));
        var removedMovies = currentObject.getMovies().stream()
                .filter(performer -> !linkedMovies.contains(performer));


        addedMovies.forEach(currentObject::link);
        removedMovies.forEach(currentObject::unlink);
        storage.savePerformerToFile(currentObject);
    }
}
