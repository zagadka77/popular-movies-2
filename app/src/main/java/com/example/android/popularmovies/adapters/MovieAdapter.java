/*
 * Copyright (c) 2018 Pierluca Masala <pierluca.masala@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.popularmovies.data.db.FavoriteMoviesContract.FavoriteMovies;
import com.example.android.popularmovies.data.objects.Movie;
import com.example.android.popularmovies.databinding.MovieGridItemBinding;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter responsible for showing the movie poster images in MainActivity through a RecyclerView.
 * The adapter is either used by a AsyncTaskLoader or by a CursorLoader.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    private final MovieAdapterOnClickHandler clickHandler;
    private List<Movie> movies;
    private Cursor moviesCursor;
    private boolean isFavorites = false;

    /**
     * The constructor, which initializes the click handler
     *
     * @param clickHandler the click handler
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

    /**
     * This takes care of the layout inflation for the RecyclerView's single item
     * and returns a ViewHolder.
     *
     * @param parent   the ViewGroup into which the new View will be added
     * @param viewType the type of the new View which will be created
     * @return the new ViewHolder
     */
    @NonNull
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Initialize DataBinding
        MovieGridItemBinding itemBinding =
                MovieGridItemBinding.inflate(inflater, parent, false);

        return new MovieAdapterViewHolder(itemBinding);
    }

    /**
     * This is executed to display the data in the specified position.
     *
     * @param holder   the ViewHolder which has to be updated
     * @param position the position of the item within the data set
     */
    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        String posterRelativeUrl;
        String title;
        String year;
        float rating;

        if (isFavorites) {
            moviesCursor.moveToPosition(position);
            posterRelativeUrl = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_POSTER_PATH));
            title = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_ORIGINAL_TITLE));
            year = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_RELEASE_DATE));
            rating = moviesCursor.getFloat(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_VOTE_AVERAGE));

        } else {
            // Get the movie data to be shown (title, release year, and rating)
            posterRelativeUrl = movies.get(position).getPosterImageUrl();
            title = movies.get(position).getTitle();
            year = movies.get(position).getReleaseDate();
            rating = (movies.get(position).getUserRating() / 10) * 5;
        }

        // Set the data on the views
        holder.binding.movieTitleTv.setText(title);
        holder.binding.movieYearTv.setText(year);
        holder.binding.ratingBar.setRating(rating);

        // Build the absolute url
        String posterUrl =
                NetworkUtils.getImageUrl(posterRelativeUrl, NetworkUtils.IMAGE_POSTER).toString();

        // Load the image with Picasso
        // We don't set any placeholder or error images, we simply leave it blank
        Picasso.with(holder.binding.posterImageIv.getContext())
                .load(posterUrl)
                .into(holder.binding.posterImageIv);
    }

    /**
     * Returns the total number of items in the adapter
     *
     * @return the number of items in the adapter
     */
    @Override
    public int getItemCount() {
        if (isFavorites) {
            return moviesCursor.getCount();
        } else {
            if (movies == null) {
                return 0;
            } else {
                return movies.size();
            }
        }
    }

    /**
     * Sets the List of Movie objects and notify that the data set has changed
     *
     * @param movies the List of Movie objects
     */
    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public void setFavorites() {
        isFavorites = true;
    }

    public void setNotFavorites() {
        isFavorites = false;
    }

    public void swapCursor(Cursor cursor) {
        moviesCursor = cursor;
        notifyDataSetChanged();
    }

    /**
     * Clear the data set
     */
    public void clear() {
        movies = null;
        notifyDataSetChanged();
    }

    /**
     * The interface which will be implemented to manage clicks on single movie items
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie, int position);
    }

    /**
     * The ViewHolder which will manage the single items in the RecyclerView
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MovieGridItemBinding binding;

        /**
         * The constructor
         *
         * @param binding the data binding to movie_grid_item.xml
         */
        MovieAdapterViewHolder(MovieGridItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set an OnClickListener on the item
            binding.movieItemCv.setOnClickListener(this);
        }

        /**
         * Manages the click events on the single item, calling the onClick function in MainActivity
         *
         * @param view the view which has been clicked on it
         */
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Movie movie;
            if (isFavorites) {
                moviesCursor.moveToPosition(adapterPosition);
                int movieId = moviesCursor.getInt(moviesCursor.getColumnIndex(FavoriteMovies._ID));
                String originalTitle = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_ORIGINAL_TITLE));
                String title = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_TITLE));
                String posterImageUrl = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_POSTER_PATH));
                String backdropImageUrl = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_BACKDROP_PATH));
                String overview = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_OVERVIEW));
                float userRating = moviesCursor.getFloat(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_VOTE_AVERAGE));
                String releaseDate = moviesCursor.getString(moviesCursor.getColumnIndex(FavoriteMovies.COLUMN_RELEASE_DATE));

                movie = new Movie(movieId, originalTitle, title, posterImageUrl, backdropImageUrl,
                        overview, (double) userRating, releaseDate);
                clickHandler.onClick(movie, adapterPosition);
            } else {
                movie = movies.get(adapterPosition);
                clickHandler.onClick(movie, adapterPosition);
            }
        }
    }
}
