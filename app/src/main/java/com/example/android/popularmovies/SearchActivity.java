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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmovies.adapters.MovieAdapter;
import com.example.android.popularmovies.data.objects.Movie;
import com.example.android.popularmovies.data.objects.Page;
import com.example.android.popularmovies.databinding.ActivitySearchBinding;
import com.example.android.popularmovies.loaders.MoviesListLoader;
import com.example.android.popularmovies.utils.NetworkUtils;

import java.util.List;

/**
 * An activity in which search results are shown. It is similar to MainActivity, but the list of
 * movies is the result of a search.
 */
public class SearchActivity extends AppCompatActivity implements
        MovieAdapter.MovieAdapterOnClickHandler,
        LoaderCallbacks<List<Movie>>,
        SwipeRefreshLayout.OnRefreshListener,
        GoToPageDialogFragment.GoToPageDialogListener {

    public final static String EXTRA_QUERY_STRING = "extra_query_string";
    private final static int COLUMNS_IN_GRID_LAYOUT_PORTRAIT = 2;
    private final static int COLUMNS_IN_GRID_LAYOUT_LANDSCAPE = 4;
    private final static int MOVIES_SEARCH_LOADER_ID = 0;
    private static String queryString;
    private final LoaderCallbacks<List<Movie>> callback = SearchActivity.this;
    private GridLayoutManager gridLayoutManager;
    private MovieAdapter adapter;
    private static final String LIFECYCLE_RV_POSITION = "rv_position_key";
    private static int rv_position = -1;
    private ActivitySearchBinding binding;

    /**
     * Main definitions on activity creation
     *
     * @param savedInstanceState the bundle on which data can be saved on activity destroying
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        // Get the query string from the intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            queryString = intent.getExtras().getString(EXTRA_QUERY_STRING);
            setTitle(getString(R.string.search_for) + queryString);
        } else {
            queryString = null;
        }

        // If there were any errors retrieving the query string, close the activity
        if (queryString == null || queryString.equals("")) {
            closeOnError();
            return;
        }

        // RecyclerView's size will not be affected by adapter content. This brings some optimizations
        binding.moviesRv.setHasFixedSize(true);

        // Set the GridLayoutManager with a different number of columns depending on whether
        // the orientation is portrait or landscape (we limit this to 2 for portrait and 4 for
        // landscape. This should be ok both for smartphones and for tablets
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(this, COLUMNS_IN_GRID_LAYOUT_PORTRAIT);
        } else {
            gridLayoutManager = new GridLayoutManager(this, COLUMNS_IN_GRID_LAYOUT_LANDSCAPE);
        }

        // Set the LayoutManager for the RecyclerView
        binding.moviesRv.setLayoutManager(gridLayoutManager);

        // Set the adapter
        adapter = new MovieAdapter(this);
        binding.moviesRv.setAdapter(adapter);

        // Retrieve the position in the RecyclerView if it was saved. This is not useful in the
        // normal situations when for example the user rotates the device while in MainActivity,
        // because the Loader and the RecyclerView are responsible for resetting the content and
        // position in the RecyclerView. We only have a problem when for example the user navigates to
        // DetailActivity and rotates the device while there, then coming back to MainActivity. In
        // this case the Loader gets restarted and the RecyclerView reloaded from the starting
        // position. That's why we save the RecyclerView position in onSaveInstanceState and let the
        // RecyclerView smooth scroll to the desired position in onLoadFinished. So the user after
        // coming back from DetailActivity will always find the RecyclerView on the position from
        // where it started.
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LIFECYCLE_RV_POSITION)) {
                rv_position = savedInstanceState.getInt(LIFECYCLE_RV_POSITION);
            }
        }
    }

    /**
     * Main initializations
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Set an OnRefreshListener on the SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this);

        // Show the actual page and the total number of pages available
        // The user can click on the text view and an alert dialog is shown in which the user
        // can insert a page number to navigate to
        binding.actualPagesTv.setOnClickListener(new View.OnClickListener() {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final GoToPageDialogFragment fragment = new GoToPageDialogFragment();

            @Override
            public void onClick(View view) {
                fragment.show(fragmentManager, "fragment_go_to_page");
            }
        });

        // Show or hide the FABs when the user is at page start or end
        binding.moviesRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // If we are at the very end of the page, and we are not on the last page, we
                // only show the right button
                if ((gridLayoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1)
                        && (Page.getSearchPage() < Page.getSearchTotalPages())) {
                    binding.floatingActionButtonRight.show();
                } else {
                    binding.floatingActionButtonRight.hide();
                }

                // If we are at the very beginning of the page, and we are not on the first page,
                // we only show the left button
                if ((gridLayoutManager.findFirstVisibleItemPosition() == 0)
                        && (Page.getSearchPage() > 1)) {
                    binding.floatingActionButtonLeft.show();
                } else {
                    binding.floatingActionButtonLeft.hide();
                }
            }
        });

        // Set the click listeners on the left FAB
        binding.floatingActionButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Page.getSearchPage() > 1) {
                    // Navigate one page backwards
                    Page.setSearchPage(Page.getSearchPage() - 1);
                    adapter.clear();
                    getSupportLoaderManager().restartLoader(MOVIES_SEARCH_LOADER_ID, null, callback);
                }
            }
        });

        // Set the click listeners on the right FAB
        binding.floatingActionButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate one page forwards
                if (Page.getSearchPage() < Page.getSearchTotalPages()) {
                    Page.setSearchPage(Page.getSearchPage() + 1);
                    adapter.clear();
                    getSupportLoaderManager().restartLoader(MOVIES_SEARCH_LOADER_ID, null, callback);
                }
            }
        });
    }

    /**
     * Save the actual position in the RecyclerView for future use
     *
     * @param outState the state on which we save our data
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the position in the RecyclerView to be restored later
        outState.putInt(LIFECYCLE_RV_POSITION, rv_position);
    }

    /**
     * In onResume we start (or restart) the loader
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Initialize the adapter
        adapter.clear();

        // Start or restart the loader
        getSupportLoaderManager().initLoader(MOVIES_SEARCH_LOADER_ID, null, callback);
    }

    /**
     * Manages clicks on RecyclerViews items.
     * When the user clicks on a poster image, an Intent is launched for DetailActivity, and through
     * the intent a Movie object is passed.
     *
     * @param movie the movie object which is passed through the intent
     */
    @Override
    public void onClick(Movie movie, int position) {
        rv_position = position;
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intent = new Intent(context, destinationClass);
        intent.putExtra(DetailActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    /**
     * Actions to be taken when we start loading (either through the MovieListLoader, or through a new CursorLoader
     *
     * @param id   the loader id
     * @param args the args, not used
     * @return a new loader
     */
    @NonNull
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        // Hide the RecyclerView and show the ProgressBar
        binding.moviesRv.setVisibility(View.INVISIBLE);
        binding.loadingIndicatorPb.setVisibility(View.VISIBLE);

        // Create a new instance of MoviesListLoader
        return new MoviesListLoader(this, NetworkUtils.QUERY_SEARCH, queryString, null);
    }

    /**
     * Actions to be taken if we refresh the activity page
     */
    @Override
    public void onRefresh() {
        // Restart the loader
        getSupportLoaderManager().restartLoader(MOVIES_SEARCH_LOADER_ID, null, callback);

        // Signal that refresh has finished
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Actions to be done when the loader has done loading data
     *
     * @param loader the loader
     * @param data   the data returned from the loader
     */
    @Override
    public void onLoadFinished(@NonNull Loader<List<Movie>> loader, List<Movie> data) {
        // Hide the ProgressBar and the right FAB
        binding.loadingIndicatorPb.setVisibility(View.INVISIBLE);
        binding.floatingActionButtonRight.hide();
        // Pass the movies to the adapter. If there is no data, show an error message
        // Hide or show the FABs according to the situation
        adapter.setMovies(data);
        if (data == null) {
            binding.floatingActionButtonLeft.hide();
            binding.actualPagesTv.setVisibility(View.INVISIBLE);
            binding.moviesRv.setVisibility(View.INVISIBLE);
            binding.errorMessageTv.setText(R.string.no_movies_found);
            binding.errorMessageTv.setVisibility(View.VISIBLE);
        } else {
            // If we are at page 1 there is no need for a left FAB
            if (Page.getSearchPage() == 1) {
                binding.floatingActionButtonLeft.hide();
            }
            binding.moviesRv.setVisibility(View.VISIBLE);
            binding.errorMessageTv.setVisibility(View.INVISIBLE);
            // Set the values of actual page and total pages and show the TextView
            binding.actualPagesTv.setText(getString(R.string.actual_page, Page.getSearchPage(), Page.getSearchTotalPages()));
            binding.actualPagesTv.setVisibility(View.VISIBLE);

            // If a position has been saved and retrieved, set the actual position in the
            // RecyclerView to that position
            if (rv_position != -1) {
                binding.moviesRv.smoothScrollToPosition(rv_position);
                rv_position = -1;
            }
        }
    }

    /**
     * We don't need this function
     *
     * @param loader the loader to be reset
     */
    @Override
    public void onLoaderReset(@NonNull Loader<List<Movie>> loader) {
        // Not implemented
    }

    /**
     * Handle errors closing the activity (and returning to MainActivity) and opening a Toast
     * message to warn the user.
     */
    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.search_error_message, Toast.LENGTH_LONG).show();
    }

    /**
     * Navigate to a page and restart the loader
     *
     * @param pageNumber the page number to which we navigate to
     */
    @Override
    public void onSelectingPageNumber(int pageNumber) {
        if ((pageNumber >= 1) && (pageNumber <= Page.getSearchTotalPages())) {
            // Set the actual page as from the user input and restart the loader
            // to show the page the user chose
            Page.setSearchPage(pageNumber);
            adapter.clear();
            getSupportLoaderManager().restartLoader(MOVIES_SEARCH_LOADER_ID, null, callback);
        } else {
            // If the number was not in the available range, show an error message
            Toast.makeText(this, R.string.no_page_error, Toast.LENGTH_LONG).show();
        }
    }
}

