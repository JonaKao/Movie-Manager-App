package de.uhd.ifi.se.moviemanager.ui.detail;

import static de.uhd.ifi.se.moviemanager.util.RecyclerViewUtils.setLinearLayoutTo;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Optional;

import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.ImagePyramid;
import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.Performer;
import de.uhd.ifi.se.moviemanager.ui.adapter.PosterAdapter;
import de.uhd.ifi.se.moviemanager.util.DateUtils;

/**
 * Responsible for the Performer DetailView.
 */
public class PerformerDetailActivity extends DetailActivity<Performer> {
    private TextView showTitle;
    private TextView showSubtitle;
    private TextView showDescriptionHeader;
    private TextView showDescription;
    private TextView linkedMoviesHeader;
    private RecyclerView linkedMoviesList;
    private PosterAdapter<Movie> linkedMoviesAdapter;
    private TextView showBirthNameHeader;
    private TextView showBirthName;
    private TextView showOccupationsHeader;
    private TextView showOccupations;


    public PerformerDetailActivity() {
        super(R.layout.activity_performer_detail,
                PerformerDetailEditActivity.class);
    }

    @Override
    protected void bindViews() {
        showTitle = findViewById(R.id.content_title);
        showSubtitle = findViewById(R.id.content_subtitle);
        showDescriptionHeader = findViewById(R.id.description_header);
        showDescription = findViewById(R.id.description);
        linkedMoviesHeader = findViewById(R.id.linked_movies_header);
        linkedMoviesList = findViewById(R.id.linked_movies);
        showBirthNameHeader = findViewById(R.id.birth_name_header);
        showBirthName = findViewById(R.id.birth_name);
        showOccupationsHeader = findViewById(R.id.occupations_header);
        showOccupations = findViewById(R.id.occupations);
    }

    @Override
    protected void setupLists() {
        setupLinkedMoviesList();
    }

    @Override
    protected void registerSpecificListeners() {
        //not needed
    }

    private void setupLinkedMoviesList() {
        setLinearLayoutTo(this, linkedMoviesList, LinearLayout.HORIZONTAL);

        linkedMoviesAdapter = new PosterAdapter<>(this,
                currentObject.getMovies(),
                R.layout.medium_poster);
        linkedMoviesAdapter.setOnItemClickListener(movie -> {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(CURRENT_OBJECT, movie);
            updateAfterLinkedDetailsLauncher.launch(intent);
        });
        linkedMoviesList.addItemDecoration(new PosterAdapter
                .PosterItemDecoration((int) getResources().getDimension(R.dimen.default_margin)));
        linkedMoviesList.setAdapter(linkedMoviesAdapter);
    }

    @Override
    protected void updateAfterLinkedDetails(Intent result) {
        Optional<Performer> currentModelOpt = model
                .getPerformerById(currentObject.getId());
        if (currentModelOpt.isPresent()) {
            currentObject = currentModelOpt.get();
            updateUIWithModelData();
        } else {
            finishAfterDeletion(currentObject);
        }
    }

    @Override
    protected void updateUIWithModelData() {
        initImageView(ImagePyramid.ImageSize.LARGE);

        if (currentObject.getBiography().isEmpty()) {
            showDescriptionHeader.setVisibility(View.GONE);
            showDescription.setVisibility(View.GONE);
        } else {
            showDescriptionHeader.setVisibility(View.VISIBLE);
            showDescription.setVisibility(View.VISIBLE);
            showDescription.setText(currentObject.getBiography());
        }
        if (currentObject.getMovies().isEmpty()) {
            linkedMoviesHeader.setVisibility(View.GONE);
            linkedMoviesList.setVisibility(View.GONE);
        } else {
            linkedMoviesHeader.setVisibility(View.VISIBLE);
            linkedMoviesList.setVisibility(View.VISIBLE);
        }
        if (currentObject.getBirthName().isEmpty()) {
            showBirthNameHeader.setVisibility(View.GONE);
            showBirthName.setVisibility(View.GONE);
        } else {
            showBirthNameHeader.setVisibility(View.VISIBLE);
            showBirthName.setVisibility(View.VISIBLE);
            showBirthName.setText(currentObject.getBirthName());
        }
        if (currentObject.getOccupations().isEmpty()) {
            showOccupationsHeader.setVisibility(View.GONE);
            showOccupations.setVisibility(View.GONE);
        } else {
            showOccupationsHeader.setVisibility(View.VISIBLE);
            showOccupations.setVisibility(View.VISIBLE);
        }

        showTitle.setText(currentObject.getName());

        if (currentObject.getDateOfBirth() != null && currentObject.getRating() >= 0) {
            showSubtitle.setText(String.format("* %s | %s ★",
                    DateUtils.dateToText(currentObject.getDateOfBirth()),
                    currentObject.getRating()));
        } else if (currentObject.getDateOfBirth() != null) {
            showSubtitle.setText(String.format("* %s",
                    DateUtils.dateToText(currentObject.getDateOfBirth())));
        } else if (currentObject.getRating() >= 0) {
            showSubtitle.setText(String.format("%s ★",
                    currentObject.getRating()));
        } else {
            showSubtitle.setText("");
        }

        linkedMoviesAdapter.update(currentObject.getMovies());

        showBirthName.setText(currentObject.getBirthName());

        showOccupations
                .setText(String.join("\n", currentObject.getOccupations()));
    }

    @Override
    protected boolean updateAfterEdit(Intent intent) {
        Performer performer = intent.getParcelableExtra(CURRENT_OBJECT);
        if (performer == null) {
            return false;
        }
        currentObject = performer;
        setUpdated(true);
        updateUIWithModelData();
        return true;
    }

    void finishAfterDeletion(Performer removedPerformer) {
        String msg = String.format(getString(R.string.info_performer_deletion),
                removedPerformer.getName());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }
}
