package de.uhd.ifi.se.moviemanager.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.view.WindowInsetsController;

public class ActivityUtils {

    private ActivityUtils() {
    }

    public static void setStatusBarColor(Activity activity) {
        int nightModeFlags =
                activity.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            activity.getWindow().getInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        }
    }
}
