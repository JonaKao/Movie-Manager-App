package de.uhd.ifi.se.moviemanager.storage;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.app.Activity;
import android.util.Log;

import java.io.File;

import de.uhd.ifi.se.moviemanager.MovieManagerActivity;
import de.uhd.ifi.se.moviemanager.model.ImagePyramid;
import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.Performer;

/**
 * Proxy which manages a single instance of the {@link StorageManagerImpl} and
 * handles the Android permissions. Both classes implement the {@link
 * StorageManager} interface.
 * <p>
 * This class uses the Singleton design pattern. The Singleton pattern restricts
 * the instantiation of a class to one "single" instance.
 */
public final class StorageManagerAccess implements StorageManager {
    private static final StorageManagerAccess INSTANCE =
            new StorageManagerAccess();

    private StorageManager storageManager;

    public static StorageManagerAccess getInstance() {
        return INSTANCE;
    }

    private StorageManagerAccess() {

    }

    public void setStorageManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public void openMovieManagerStorage(Activity activity) {
        File base = activity.getExternalFilesDir(DIRECTORY_DOCUMENTS);
        File directory = new File(base, MovieManagerActivity.STORAGE_NAME);

        if (!isStorageOpened()) {
            openStorageIn(directory);
        }
    }

    private void openStorageIn(File directory) {
        if (isStorageOpened()) {
            Log.e("openStorageIn", "Storage was already opened!");
        }

        if (!directory.getParentFile().exists()) {
            directory.getParentFile().mkdirs();
        }
        storageManager = StorageManagerImpl.getInstance(directory);
    }

    private boolean isStorageOpened() {
        return storageManager != null;
    }

    private StorageManager getStorageManager() {
        if (!isStorageOpened()) {
            Log.e("getStorageManager", "Storage is not open!");
        }

        return storageManager;
    }

    @Override
    public Movie saveMovieToFile(Movie movie) {
        return getStorageManager().saveMovieToFile(movie);
    }

    @Override
    public Performer savePerformerToFile(Performer performer) {
        return getStorageManager().savePerformerToFile(performer);
    }

    @Override
    public boolean deleteMovieFile(Movie movie) {
        return getStorageManager().deleteMovieFile(movie);
    }

    @Override
    public boolean deletePerformerFile(Performer performer) {
        return getStorageManager().deletePerformerFile(performer);
    }

    @Override
    public void saveImage(ImagePyramid imagePyramid) {
        getStorageManager().saveImage(imagePyramid);
    }

    @Override
    public String getImagePath() {
        return getStorageManager().getImagePath();
    }

    @Override
    public void selfDestruct() {
        getStorageManager().selfDestruct();
    }

    @Override
    public void clear() {
        getStorageManager().clear();
    }
}
