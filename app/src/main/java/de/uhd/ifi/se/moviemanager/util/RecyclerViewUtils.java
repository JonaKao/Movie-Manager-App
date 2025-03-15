package de.uhd.ifi.se.moviemanager.util;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public final class RecyclerViewUtils {

    // private constructor to prevent instantiation
    private RecyclerViewUtils() {
        throw new UnsupportedOperationException();
    }

    public static void setLinearLayoutTo(Context context,
                                         RecyclerView recyclerView) {
        setLinearLayoutTo(context, recyclerView, LinearLayoutManager.VERTICAL);
    }

    public static void setLinearLayoutTo(Context context,
                                         RecyclerView recyclerView,
                                         int orientation) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                context);
        linearLayoutManager.setOrientation(orientation);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
}
