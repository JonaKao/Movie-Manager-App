package de.uhd.ifi.se.moviemanager.ui.adapter;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import de.uhd.ifi.se.moviemanager.R;
import de.uhd.ifi.se.moviemanager.model.Identifiable;
import de.uhd.ifi.se.moviemanager.model.ImageBased;
import de.uhd.ifi.se.moviemanager.model.ImagePyramid;
import de.uhd.ifi.se.moviemanager.model.Movie;
import de.uhd.ifi.se.moviemanager.model.Nameable;
import de.uhd.ifi.se.moviemanager.model.Performer;

/**
 * Enables to show a list of e.g. {@link Movie}s or {@link Performer}s in a
 * {@link RecyclerView}. Used in the
 * {@link de.uhd.ifi.se.moviemanager.ui.detail.DetailActivity}s.
 *
 * @param <T> {@link Movie} or {@link Performer}
 *            class.
 */
public class PosterAdapter<T extends Identifiable & ImageBased & Nameable & Parcelable>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;

    // List of Movies or Performers
    private final List<T> data;
    private final int itemLayout;
    private Consumer<T> onItemClick;

    public PosterAdapter(Context context, List<T> data,
                         @LayoutRes int itemLayout) {
        this.context = context;
        this.itemLayout = itemLayout;
        this.data = Objects.requireNonNull(data);
        onItemClick = t -> Log.d("LinkedDataAdapter", "onItemClick: " + t);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context)
                .inflate(itemLayout, parent, false);
        view.setOnClickListener(this::onItemClick);

        ViewHolder holder = new ViewHolder(view) {
        };
        view.setTag(holder);
        return holder;
    }

    private void onItemClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        int pos = holder.getBindingAdapterPosition();
        T element = data.get(pos);
        onItemClick.accept(element);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (position >= getItemCount()) {
            return;
        }

        T element = data.get(position);
        if (element == null) {
            return;
        }
        ImageView showImage = viewHolder.itemView.findViewById(R.id.show_image);
        showImage.setImageDrawable(
                element.getImage(context, ImagePyramid.ImageSize.LARGE));
        if (showImage.getDrawable() instanceof LayerDrawable) {
            TextView showTitle = viewHolder.itemView.findViewById(R.id.show_title);
            showTitle.setText(element.getName());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(Consumer<T> onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void update(List<T> updatedData) {
        data.clear();
        data.addAll(updatedData);
        notifyDataSetChanged();
    }

    public static class PosterItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public PosterItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.top = space;
            outRect.bottom = space;

            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = space * 2;
            }
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.right = space * 2;
            }
        }
    }
}
