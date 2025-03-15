package de.uhd.ifi.se.moviemanager.ui.detail;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static java.util.Optional.ofNullable;
import static de.uhd.ifi.se.moviemanager.model.ImagePyramid.ImageSize.LARGE;
import static de.uhd.ifi.se.moviemanager.ui.detail.DetailActivity.CURRENT_OBJECT;
import static de.uhd.ifi.se.moviemanager.util.Listeners.createOnTextChangedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.ImageBased;
import de.uhd.ifi.se.moviemanager.model.ImagePyramid;
import de.uhd.ifi.se.moviemanager.model.ModelObjectWithImage;
import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.MovieManagerModel;
import de.uhd.ifi.se.moviemanager.model.Performer;
import de.uhd.ifi.se.moviemanager.storage.StorageManagerAccess;
import de.uhd.ifi.se.moviemanager.util.ActivityUtils;

/**
 * Abstract superclass of the Detail Edit Views.
 *
 * @param <T> e.g. {@link Movie} or {@link Performer} data class.
 * @see MovieDetailEditActivity
 * @see PerformerDetailEditActivity
 */
public abstract class DetailEditActivity<T extends ModelObjectWithImage>
        extends AppCompatActivity {

    protected static final MovieManagerModel model = MovieManagerModel.getInstance();
    protected static final StorageManagerAccess storage = StorageManagerAccess
            .getInstance();
    private final int layoutId;
    protected MenuItem commitItem;
    protected TextInputLayout nameInput;
    protected EditText nameEditText;
    protected ImageView imageView;
    protected MaterialButton resetImageButton;
    protected MaterialButton changeImageButton;
    // if true, the save button is enabled and a warning is shown when the
    // user cancels
    protected boolean isChanged;
    // object of {@link Movie} or {@link Performer} data class
    protected T currentObject;

    private final ActivityResultLauncher<Intent> setImageFromIntentLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        Intent data = result.getData();
                        if (data != null) {
                            setImageFromIntent(data);
                        }
                    });

    protected DetailEditActivity(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);
        storage.openMovieManagerStorage(this);

        ActivityUtils.setStatusBarColor(this);
        currentObject = getCurrentObjectFromIntent();
        initViewItems();
        initToolbar();
        initFromObject(currentObject);
        registerSpecificListeners();
        registerCommonListeners();
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_0.getColor(this));
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));

        showWarnings();
    }

    /**
     * Initializes all input fields in the edit views. If an existing object is
     * updated, they are filled with the attributes of the current object. If a
     * new object is created, the fields are empty.
     */
    protected abstract void initViewItems();

    /**
     * Initializes the heading of the view. The menu items are shown in the
     * action bar.
     */
    private void initToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
    }

    /**
     * @return null if a new object is created. Returns an object of e.g. {@link
     * Movie} or {@link Performer} data class if an existing object is updated.
     */
    private T getCurrentObjectFromIntent() {
        Intent intent = getIntent();
        // the object passed in the intent is null if this view is invoked
        // from a master view
        ModelObjectWithImage objectInIntent = intent
                .getParcelableExtra(CURRENT_OBJECT);
        return getObject(objectInIntent == null ? -1 : objectInIntent.getId());
    }

    /**
     * Decides whether a new object is created or an existing object is updated.
     * Either calls {@link #initForCreation()} or {@link #initForUpdate()}
     * method.
     */
    private void initFromObject(T currentObject) {
        if (Objects.isNull(currentObject))
            initForCreation();
        else
            initForUpdate();
    }

    /**
     * Invoked if a new object (e.g. movie) is created, i.e. if the plus button
     * in the master view is selected.
     */
    protected abstract void initForCreation();

    /**
     * Invoked if an existing object (e.g. movie) is changed/updated.
     */
    protected abstract void initForUpdate();

    /**
     * Receives an existing object from the {@link MovieManagerModel}. Returns
     * null if the object does not exist.
     *
     * @param id of the object, see {@link ModelObjectWithImage}. <<<<<<< HEAD
     * @return either an object of {@link Movie} or {@link Performer} data class
     * if existing, null otherwise. >>>>>>> c23e2fa... Add more comments and
     * simplify Wiki sync
     */
    protected abstract T getObject(int id);

    /**
     * Registers the event listeners that are the same for every detail edit
     * view.
     */
    private void registerCommonListeners() {
        nameEditText.addTextChangedListener(
                createOnTextChangedListener(this::setChanged));
    }

    /**
     * Determines what happens when the user clicks on an image or on the delete
     * icon for an image in the detail edit view.
     */
    private void registerImageViewOnClickListeners() {
        changeImageButton.setOnClickListener(v -> {
            Intent chooserIntent = createImageChooserIntent();
            setImageFromIntentLauncher.launch(chooserIntent);
        });
        resetImageButton.setOnClickListener(v -> {
            setChanged(true);
            imageView.setImageDrawable(
                    ImagePyramid.getDefaultImage(this, LARGE));
            setResetImageButtonEnabled(false);
        });
    }

    /**
     * Registers the event listeners specific to the subclass, e.g. to the movie
     * detail edit view.
     */
    protected abstract void registerSpecificListeners();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_edit_menu, menu);
        commitItem = menu.findItem(R.id.commit);
        enableOrDisableSaveButton();
        return true;
    }

    /**
     * Enables or disables the save button. The save button is disabled if no changes were made.
     */
    protected void enableOrDisableSaveButton() {
        if (commitItem != null) {
            commitItem.setEnabled(isChanged);
        }
    }

    /**
     * Shows warnings if constraints are not fulfilled, e.g., if a movie has an
     * empty title or a performer has no movie linked.
     */
    protected abstract void showWarnings();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                return cancel();
            case R.id.commit:
                return commitChanges();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method is executed when the user cancels a system function.
     *
     * @return true because the method {@link #onOptionsItemSelected(MenuItem)}
     * requires a boolean value.
     */
    private boolean cancel() {
        hideKeyboard();
        onBackPressed();
        return true;
    }


    /**
     * Hides the keyboard to insert text in Android.
     */
    private void hideKeyboard() {
        Object systemService = getSystemService(INPUT_METHOD_SERVICE);
        InputMethodManager inputMethodManager =
                (InputMethodManager) systemService;
        View view = ofNullable(getCurrentFocus()).orElse(new View(this));
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    /**
     * @return the name of the object (for movies their title).
     */
    protected String getCurrentName() {
        return nameEditText.getText().toString().trim();
    }


    /**
     * Checks whether certain constraints are fulfilled, e.g., if a movie has an
     * empty title or a performer has no movie linked.
     *
     * @return true if all constraints are fulfilled.
     */
    protected abstract boolean areConstraintsFulfilled();

    /**
     * This method is executed when the user saves the changes.
     *
     * @return false because the method {@link #onOptionsItemSelected(MenuItem)}
     * requires a boolean value.
     */
    private boolean commitChanges() {
        hideKeyboard();
        if (areConstraintsFulfilled()) {
            saveAndFinish();
            return true;
        }
        showCommitWarnings();
        return false;
    }

    /**
     * This method is invoked when the user saves (=commits) the changes. Calls
     * {@link #onSave()}, which needs to be implemented in the subclasses.
     */
    protected void saveAndFinish() {
        onSave();

        Intent intent = new Intent();
        intent.putExtra(CURRENT_OBJECT, currentObject);
        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    /**
     * Hook to be called when user tries to commit changes but constraints are not met.
     */
    protected void showCommitWarnings() {}

    /**
     * This method is invoked when the user saves (=commits) the changes. Needs
     * to be implemented in the subclasses.
     */
    protected abstract void onSave();

    @Override
    public void onBackPressed() {
        if (!isChanged) {
            discardChanges();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Warning")
                .setMessage(getString(R.string.discard_changes))
                .setPositiveButton(R.string.yes, (dialog, which) -> discardChanges())
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Called when the user selects to discard changes (on back pressed).
     */
    protected void discardChanges() {
        setResult(Activity.RESULT_CANCELED, new Intent());
        super.onBackPressed();
    }

    /**
     * Creates the interactive image. The user can change and delete the image.
     */
    protected void initEditableImageView() {
        initImageView(LARGE);
        if (imageView.getDrawable() instanceof LayerDrawable) {
            setResetImageButtonEnabled(false);
        }
        registerImageViewOnClickListeners();
    }

    /**
     * Creates the image view and fills it with
     * {@link ModelObjectWithImage#getImage(Context,
     * ImagePyramid.ImageSize)}.
     *
     * @param imageSize ImageSize#LARGE is normally used for the image in a
     *                  detail view.
     */
    protected void initImageView(ImagePyramid.ImageSize imageSize) {
        imageView = findViewById(R.id.edit_image);
        if (currentObject != null) {
            imageView.setImageDrawable(currentObject.getImage(this, imageSize));
        }
        resetImageButton = findViewById(R.id.reset_image_button);
        changeImageButton = findViewById(R.id.change_image_button);
    }

    /**
     * If isChanged is true, the save button is enabled and a warning is shown
     * when the user omits the changes/cancels.
     *
     * @param isChanged true if the object (either new or existing) was changed
     *                  since the detail edit view was opened.
     */
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
        enableOrDisableSaveButton();
        showWarnings();
    }

    /**
     * @param isEnabled true if the user should be able to delete an image
     *                  (makes only sense if an image already exists).
     */
    protected void setResetImageButtonEnabled(boolean isEnabled) {
        resetImageButton.setEnabled(isEnabled);
    }

    /**
     * @return intent that enables the user to choose an image from file system.
     */
    private Intent createImageChooserIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(EXTERNAL_CONTENT_URI, "image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                new Intent[]{pickIntent});

        return chooserIntent;
    }

    /**
     * Reads the image that the user chose in file system from an intent.
     *
     * @param data intent with image URI.
     */
    private void setImageFromIntent(Intent data) {
        Optional<Bitmap> opt = loadFromIntent(this, data);
        Bitmap b = opt.get();
        setResetImageButtonEnabled(true);
        Bitmap c = ImageBased.crop(b, 2, 3);
        imageView.setImageBitmap(c);
        setChanged(true);
    }

    /**
     * @param context Android activity.
     * @param data    intent with image that the user chose in file system.
     * @return image that the user chose in file system.
     */
    private static Optional<Bitmap> loadFromIntent(Context context,
                                                   Intent data) {
        Uri pickedImage = data.getData();

        InputStream inputStream = null;

        try {
            assert pickedImage != null;
            inputStream = context.getContentResolver()
                    .openInputStream(pickedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap b = BitmapFactory.decodeStream(inputStream, null, options);

        return Optional.of(b);
    }
}
