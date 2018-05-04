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

package com.example.android.popularmovies;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmovies.adapters.ReviewsAdapter;
import com.example.android.popularmovies.adapters.VideosAdapter;
import com.example.android.popularmovies.data.db.FavoriteMoviesContract;
import com.example.android.popularmovies.data.db.FavoriteMoviesContract.FavoriteMovies;
import com.example.android.popularmovies.data.objects.Movie;
import com.example.android.popularmovies.data.objects.Review;
import com.example.android.popularmovies.data.objects.Video;
import com.example.android.popularmovies.databinding.ActivityDetailBinding;
import com.example.android.popularmovies.loaders.MovieDetailLoader;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Shows the details of a particular movie.
 */
public class DetailActivity extends AppCompatActivity implements
        LoaderCallbacks<Movie>,
        VideosAdapter.VideosAdapterOnClickHandler,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_MOVIE = "extra_movie";
    private static final String YOUTUBE_URL = "http://www.youtube.com/watch?v=";
    private final static int MOVIE_DETAILS_LOADER_ID = 0;
    private final LoaderCallbacks<Movie> callback = DetailActivity.this;
    private Movie movie;
    private String backdropUrl;
    private final static int CHECK_IF_FAVORITE_TOKEN = 1;
    private final static int DELETE_MOVIE_TOKEN = 2;
    private final static int INSERT_MOVIE_TOKEN = 3;
    private boolean movieIsFavorite;
    private ActivityDetailBinding binding;

    // An OnClickListener to expand or collapse the reviews
    private final View.OnClickListener reviewsExpandOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (binding.reviewsRv.getVisibility() == View.VISIBLE) {
                // If reviews are visible, collapse them
                binding.reviewsRv.setVisibility(View.GONE);
                binding.reviewsExpandIconIv.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_expand_more_black_24dp));
            } else {
                // If reviews are invisible, expand them
                binding.reviewsRv.setVisibility(View.VISIBLE);
                binding.reviewsExpandIconIv.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_expand_less_black_24dp));
            }
        }
    };

    /**
     * Open a video in the YouTube app or in a browser (if the first is not installed on the device)
     *
     * @param context the context
     * @param key     the YouTube id of the video
     */
    private static void watchVideo(Context context, String key) {
        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(YOUTUBE_URL + key));
        try {
            // Watch the video with the YouTube app
            context.startActivity(youtubeIntent);
        } catch (ActivityNotFoundException ex) {
            // If the YouTube app is not available on the phone, play the video with a browser
            context.startActivity(webIntent);
        }
    }

    /**
     * Main definitions on activity creation
     *
     * @param savedInstanceState the bundle on which data can be saved on activity destroying
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Get movie data from the intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            movie = intent.getExtras().getParcelable(EXTRA_MOVIE);
        } else {
            movie = null;
        }

        // If there were any errors retrieving the movie details, close the activity
        if (movie == null) {
            closeOnError();
            return;
        }

        // Check if the movie is in the favorites database and update the FAB accordingly
        checkIfFavorite(movie);

        // Bind the data
        binding.setMovie(movie);
        // Change the activity title with the movie's original title
        setTitle(movie.getOriginalTitle());

        // Get the backdrop image complete url
        String backdropRelativeUrl = movie.getBackdropImageUrl();
        backdropUrl =
                NetworkUtils.getImageUrl(backdropRelativeUrl, NetworkUtils.IMAGE_BACKDROP)
                        .toString();

        // Load the image with Picasso
        loadImage(backdropUrl);

        // If there is no overview, we hide the TextView and we show a message
        String overview = movie.getOverview();
        if ((overview == null) || (overview.equals(""))) {
            binding.overviewTv.setVisibility(View.GONE);
            binding.detailErrorNoOverviewTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Main initializations on activity start
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Set an OnRefreshListener on the SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this);

        // Set an OnClickListener on the FAB to favorite or unfavorite the movie
        binding.addToFavoritesFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (movieIsFavorite) {
                    removeFromFavorites(movie);
                } else {
                    addToFavorites(movie);
                }
            }
        });
    }

    /**
     * Load the image with Picasso
     * We we use a ProgressBar as a placeholder, and a TextView as error message
     *
     * @param backdropUrl the url of the backdrop image
     */
    private void loadImage(String backdropUrl) {
        Picasso.with(this)
                .load(TextUtils.isEmpty(backdropUrl) ? null : backdropUrl)
                .into(binding.backdropImageIv, new Callback() {
                    @Override
                    public void onSuccess() {
                        // On loading success, hide the progress bar and error message
                        binding.detailPb.setVisibility(View.GONE);
                        binding.detailErrorNoImageTv.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        // On loading error, hide the progress bar and show the error message
                        binding.detailPb.setVisibility(View.GONE);
                        binding.detailErrorNoImageTv.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * We start or restart the loader in onResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Start or restart the loader
        getSupportLoaderManager().initLoader(MOVIE_DETAILS_LOADER_ID, null, callback);
    }

    /**
     * Handles errors closing the activity (and returning to MainActivity) and opening a Toast
     * message to warn the user.
     */
    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_LONG).show();
    }

    /**
     * When the user clicks on the back button, the activity gets closed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Actions to be performed when the loader is created
     *
     * @param id the loader id
     * @param args the arguments
     * @return a new loader
     */
    @NonNull
    @Override
    public Loader<Movie> onCreateLoader(int id, @Nullable Bundle args) {
        // Create a new instance of MoviesListLoader
        return new MovieDetailLoader(this, movie);
    }

    /**
     * Actions to be performed when the loader finishes loading
     *
     * @param loader the loader
     * @param data the data returned from the loader
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Movie> loader, Movie data) {
        if (data == null) {
            // If there is no data, hide and show elements accordingly
            binding.loadingIndicatorPb.setVisibility(View.INVISIBLE);
            binding.videosLabelTv.setVisibility(View.GONE);
            binding.reviewsLabelTv.setVisibility(View.GONE);
            binding.videosRv.setVisibility(View.GONE);
            binding.reviewsRv.setVisibility(View.GONE);
            binding.reviewsExpandIconIv.setVisibility(View.GONE);
            binding.detailErrorNoReviewsTv.setVisibility(View.GONE);
            binding.detailErrorNoVideosTv.setVisibility(View.GONE);
            binding.movieDetailsCv.setVisibility(View.VISIBLE);
            binding.detailErrorNoDataTv.setVisibility(View.VISIBLE);
        } else {
            // Initialize the RecyclerView and the LayoutManager for the videos
            List<Video> videos = data.getVideos();
            if ((videos != null) && (videos.size() > 0)) {
                binding.videosRv.setHasFixedSize(false);
                // Set the LayoutManager for the RecyclerView
                LinearLayoutManager videosLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                binding.videosRv.setLayoutManager(videosLayoutManager);
                // Set the adapter
                VideosAdapter videosAdapter = new VideosAdapter(this);
                binding.videosRv.setAdapter(videosAdapter);
                binding.videosRv.setNestedScrollingEnabled(false);
                videosAdapter.setVideos(data.getVideos());
                binding.videosRv.setVisibility(View.VISIBLE);
                binding.videosLabelTv.setVisibility(View.VISIBLE);
                binding.detailErrorNoVideosTv.setVisibility(View.GONE);
                binding.reviewsLabelTv.setVisibility(View.VISIBLE);
                binding.detailErrorNoReviewsTv.setVisibility(View.GONE);
                binding.reviewsExpandIconIv.setVisibility(View.VISIBLE);
                binding.detailErrorNoDataTv.setVisibility(View.GONE);
            } else {
                // If there are no videos, hide the RecyclerView and label and show an error message
                binding.videosRv.setVisibility(View.GONE);
                binding.videosLabelTv.setVisibility(View.VISIBLE);
                binding.detailErrorNoVideosTv.setVisibility(View.VISIBLE);
            }

            // Initialize the RecyclerView and the LayoutManager for the reviews
            List<Review> reviews = data.getReviews();
            if ((reviews != null) && (reviews.size() > 0)) {
                binding.reviewsRv.setHasFixedSize(false);
                // Set the LayoutManager for the RecyclerView
                LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);
                binding.reviewsRv.setLayoutManager(reviewsLayoutManager);
                // Set the adapter
                ReviewsAdapter reviewsAdapter = new ReviewsAdapter();
                binding.reviewsRv.setAdapter(reviewsAdapter);
                binding.reviewsRv.setNestedScrollingEnabled(false);
                reviewsAdapter.setReviews(data.getReviews());
                binding.reviewsLabelTv.setOnClickListener(reviewsExpandOnClickListener);
                binding.reviewsExpandIconIv.setOnClickListener(reviewsExpandOnClickListener);
                binding.detailErrorNoReviewsTv.setVisibility(View.GONE);
            } else {
                // If there are no reviews, hide the RecyclerView and label and show an error message
                binding.reviewsRv.setVisibility(View.GONE);
                binding.reviewsLabelTv.setVisibility(View.VISIBLE);
                binding.reviewsExpandIconIv.setVisibility(View.GONE);
                binding.detailErrorNoReviewsTv.setVisibility(View.VISIBLE);
            }

            // Hide the ProgressBar
            binding.loadingIndicatorPb.setVisibility(View.INVISIBLE);
            // Show the card with the movie details
            binding.movieDetailsCv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Movie> loader) {
        // Not implemented
    }

    /**
     * Manages clicks on the videos RecyclerView items
     * When the user clicks on a video thumbnail, an Intent is launched
     *
     * @param video the video object which is passed through the intent
     */
    @Override
    public void onClick(Video video) {
        watchVideo(this, video.getKey());
    }

    /**
     * Update the FAB icon if the movie is or is not in the favorite database
     *
     * @param isFavorite a boolean representing if a movie is favorite or not
     */
    private void updateFavoriteFabStatus(boolean isFavorite) {
        if (isFavorite) {
            // Set the fab with a full hearth symbol
            binding.addToFavoritesFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp));
            movieIsFavorite = true;
        } else {
            // Set the fab with an empty hearth symbol
            binding.addToFavoritesFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_white_24dp));
            movieIsFavorite = false;
        }
    }

    /**
     * Show a snackbar with a success message if the movie was successfully inserted into the database
     */
    private void movieSuccessfullyInserted() {
        // Create a Snackbar to inform the user the movie has been inserted into db
        Snackbar mySnackbar = Snackbar.make(binding.detailsSv,
                R.string.movie_added_to_favorites, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_string, new addUndoListener());
        mySnackbar.show();
        updateFavoriteFabStatus(true);
    }

    /**
     * Show a snackbar with a success message if the movie was successfully removed from the database
     */
    private void movieSuccessfullyRemoved() {
        // Create a Snackbar to inform the user the movie has been removed from db
        Snackbar mySnackbar = Snackbar.make(binding.detailsSv,
                R.string.movie_removed_from_favorites, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_string, new removeUndoListener());
        mySnackbar.show();
        updateFavoriteFabStatus(false);
    }

    /**
     * Show a snackbar with an error message if there was a problem adding or removing the movie from the favorites
     */
    private void errorAddingOrRemoving() {
        // Create Snackbar to inform the user that there was an error
        Snackbar mySnackbar = Snackbar.make(binding.detailsSv,
                R.string.error_movie_favorites, Snackbar.LENGTH_LONG);
        mySnackbar.show();
        updateFavoriteFabStatus(false);
    }

    /**
     * Actions to be performed when the user refreshes the page
     */
    @Override
    public void onRefresh() {
        // Reload the image with Picasso
        loadImage(backdropUrl);

        // Restart the loader
        getSupportLoaderManager().restartLoader(MOVIE_DETAILS_LOADER_ID, null, callback);

        // Signal that refresh has finished
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Remove a movie from the favorites database
     *
     * @param movie the movie to be removed
     */
    private void removeFromFavorites(Movie movie) {
        // Create an object of the HelperQueryHandler
        HelperQueryHandler queryHandler = new HelperQueryHandler(getContentResolver());

        String stringId = Integer.toString(movie.getMovieId());
        Uri uri = FavoriteMoviesContract.FavoriteMovies.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        // Start the query in the AsyncQueryHandler
        queryHandler.startDelete(DELETE_MOVIE_TOKEN, null, uri, null, null);
    }

    /**
     * Add a movie in the favorites database
     *
     * @param movie the movie to be inserted
     */
    private void addToFavorites(Movie movie) {
        // Create an object of the HelperQueryHandler
        HelperQueryHandler queryHandler = new HelperQueryHandler(getContentResolver());

        // Set the ContentValues to be put into the database
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteMovies._ID, movie.getMovieId());
        contentValues.put(FavoriteMovies.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        contentValues.put(FavoriteMovies.COLUMN_TITLE, movie.getTitle());
        contentValues.put(FavoriteMovies.COLUMN_POSTER_PATH, movie.getPosterImageUrl());
        contentValues.put(FavoriteMovies.COLUMN_BACKDROP_PATH, movie.getBackdropImageUrl());
        contentValues.put(FavoriteMovies.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(FavoriteMovies.COLUMN_VOTE_AVERAGE, movie.getUserRating());
        contentValues.put(FavoriteMovies.COLUMN_RELEASE_DATE, movie.getReleaseDate());

        // Start the query in the AsyncQueryHandler
        queryHandler.startInsert(INSERT_MOVIE_TOKEN, null,
                FavoriteMovies.CONTENT_URI, contentValues);
    }

    /**
     * Check if the movie is in the favorites or not
     *
     * @param movie the movie to be checked
     */
    private void checkIfFavorite(Movie movie) {
        // Create an object of the HelperQueryHandler
        HelperQueryHandler queryHandler = new HelperQueryHandler(getContentResolver());

        // Set the id of the movie as a string
        String[] movieIdToStringArray = {String.valueOf(movie.getMovieId())};

        // Start the query in the AsyncQueryHandler
        queryHandler.startQuery(CHECK_IF_FAVORITE_TOKEN, null,
                FavoriteMovies.CONTENT_URI,
                null,
                FavoriteMovies._ID + "=?",
                movieIdToStringArray,
                null);
    }

    /**
     * An implementation of AsyncQueryHandler, that we use to make simple queries, like for inserting
     * a new movie, query a single movie to check if already in favorites, or delete a single movie.
     */
    @SuppressLint("HandlerLeak")
    private class HelperQueryHandler extends AsyncQueryHandler {

        // The constructor
        HelperQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        // We use this to check if a movie is favorite or n ot
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            boolean isFavorite = false;

            if (cursor != null) {
                isFavorite = cursor.getCount() > 0;
                cursor.close();
            }

            updateFavoriteFabStatus(isFavorite);
        }

        // We use this to insert a new movie into the favorites db
        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            super.onInsertComplete(token, cookie, uri);

            if (uri != null) {
                movieSuccessfullyInserted();
            } else {
                errorAddingOrRemoving();
            }
        }

        // We use this to remove a movie from the favorites db
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);

            if (result != 0) {
                movieSuccessfullyRemoved();
            } else {
                errorAddingOrRemoving();
            }
        }
    }

    /**
     * We set this on the "undo" button in the Snackbars to undo the removing of a movie
     */
    class removeUndoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            addToFavorites(movie);
        }
    }

    /**
     * We set this on the "undo" button in the Snackbars to undo the adding of a movie
     */
    class addUndoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            removeFromFavorites(movie);
        }
    }
}
