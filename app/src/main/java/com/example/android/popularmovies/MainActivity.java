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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmovies.adapters.MovieAdapter;
import com.example.android.popularmovies.data.db.FavoriteMoviesContract.FavoriteMovies;
import com.example.android.popularmovies.data.objects.Movie;
import com.example.android.popularmovies.data.objects.Page;
import com.example.android.popularmovies.databinding.ActivityMainBinding;
import com.example.android.popularmovies.loaders.MoviesListLoader;
import com.example.android.popularmovies.utils.NetworkUtils;

import java.util.List;

/**
 * The main activity of the app. It shows a grid of movie posters. When the user taps on a movie
 * poster it transitions to a details screen with additional information about the movie.
 * A setting option is available to change the sort order, either by top rated movies, or by most
 * popular ones, or by favorite movies (saved and retrieved from a database accessible through a
 * ContentProvider.
 * The user can also search for movies inserting a query string.
 */
public class MainActivity extends AppCompatActivity implements
        MovieAdapter.MovieAdapterOnClickHandler,
        LoaderCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener,
        SwipeRefreshLayout.OnRefreshListener,
        GoToPageDialogFragment.GoToPageDialogListener {

    private final static int COLUMNS_IN_GRID_LAYOUT_PORTRAIT = 2;
    private final static int COLUMNS_IN_GRID_LAYOUT_LANDSCAPE = 4;
    private final static int MOVIES_LIST_LOADER_ID = 0;
    private final static int MOVIES_CURSOR_LOADER_ID = 1;
    private SharedPreferences sharedPreferences;
    private static String moviesSelectionPreference;
    private GridLayoutManager gridLayoutManager;
    private MovieAdapter adapter;
    private static final String LIFECYCLE_RV_POSITION = "rv_position_key";
    private static int rv_position = -1;
    private ActivityMainBinding binding;

    /**
     * Main definitions on activity creation
     *
     * @param savedInstanceState the bundle on which data can be saved on activity destroying
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

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

        // Initialize the default settings the first time the app is run
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Get the preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the sort order preference
        moviesSelectionPreference =
                sharedPreferences.getString(this.getString(R.string.pref_movies_selection_key),
                        this.getString(R.string.pref_movies_selection_default));

        setActionBarTitle();

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

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
     * Main initializations on activity start
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Set an OnRefreshListener on the SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this);

        // Shows the actual page and the total number of pages available
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

                // If we are on Popular movies or Top rated movies, we show the navigation buttons
                // We don't need them on Favorite movies, as these are on one single page
                if (!isFavoriteMovies()) {
                    // If we are at the very end of the page, and we are not on the last page, we
                    // only show the right button
                    if ((gridLayoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1)
                            && (Page.getPage() < Page.getTotalPages())) {
                        binding.floatingActionButtonRight.show();
                    } else {
                        binding.floatingActionButtonRight.hide();
                    }

                    // If we are at the very beginning of the page, and we are not on the first page,
                    // we only show the left button
                    if ((gridLayoutManager.findFirstVisibleItemPosition() == 0)
                            && (Page.getPage() > 1)) {
                        binding.floatingActionButtonLeft.show();
                    } else {
                        binding.floatingActionButtonLeft.hide();
                    }
                }
            }
        });

        // Register a listener which notifies when the preferences have been changed
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Set the click listeners on the left FAB
        binding.floatingActionButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Page.getPage() > 1) {
                    // Navigate one page backwards
                    Page.setPage(Page.getPage() - 1);
                    adapter.clear();
                    getSupportLoaderManager().restartLoader(MOVIES_LIST_LOADER_ID, null,
                            MainActivity.this);
                }
            }
        });

        // Set the click listeners on the right FAB
        binding.floatingActionButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Page.getPage() < Page.getTotalPages()) {
                    // Navigate one page forwards
                    Page.setPage(Page.getPage() + 1);
                    adapter.clear();
                    getSupportLoaderManager().restartLoader(MOVIES_LIST_LOADER_ID, null,
                            MainActivity.this);
                }
            }
        });

        // Set the behavior of the Navigation drawer
        binding.navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        SharedPreferences.Editor editor;

                        switch (menuItem.getItemId()) {
                            case R.id.nav_top_rated:
                                // Change the preference setting. We are now in top rated movies
                                editor = sharedPreferences.edit();
                                editor.putString(getString(R.string.pref_movies_selection_key),
                                        getString(R.string.pref_movies_selection_top_rated));
                                editor.apply();

                                // Clear the adapter
                                adapter.setNotFavorites();
                                adapter.swapCursor(null);
                                adapter.clear();
                                rv_position = -1;

                                // Restart the loader and destroy the other loader if it is the case
                                getSupportLoaderManager().destroyLoader(MOVIES_CURSOR_LOADER_ID);
                                getSupportLoaderManager().restartLoader(MOVIES_LIST_LOADER_ID, null,
                                        MainActivity.this);
                                break;

                            case R.id.nav_popular:
                                // Change the preference setting. We are now in popular movies
                                editor = sharedPreferences.edit();
                                editor.putString(getString(R.string.pref_movies_selection_key),
                                        getString(R.string.pref_movies_selection_popular));
                                editor.apply();

                                // Clear the adapter
                                adapter.setNotFavorites();
                                adapter.swapCursor(null);
                                adapter.clear();
                                rv_position = -1;

                                // Restart the loader and destroy the other loader if it is the case
                                getSupportLoaderManager().destroyLoader(MOVIES_CURSOR_LOADER_ID);
                                getSupportLoaderManager().restartLoader(MOVIES_LIST_LOADER_ID, null,
                                        MainActivity.this);
                                break;

                            case R.id.nav_favorite:
                                // Change the preference setting. We are now in favorite movies
                                editor = sharedPreferences.edit();
                                editor.putString(getString(R.string.pref_movies_selection_key),
                                        getString(R.string.pref_movies_selection_favorite));
                                editor.apply();

                                // Clear the adapter
                                adapter.clear();
                                rv_position = -1;

                                // Restart the loader and destroy the other loader if it is the case
                                getSupportLoaderManager().destroyLoader(MOVIES_LIST_LOADER_ID);
                                getSupportLoaderManager().restartLoader(MOVIES_CURSOR_LOADER_ID, null,
                                        MainActivity.this);
                                binding.actualPagesTv.setVisibility(View.GONE);
                                break;

                            case R.id.nav_credits:
                                // Navigate to CreditsActivity, in which we show the credits for the app
                                Context context = MainActivity.this;
                                Class destinationClass;
                                Intent intent;
                                destinationClass = CreditsActivity.class;
                                intent = new Intent(context, destinationClass);
                                startActivity(intent);
                                break;

                            default:
                                break;
                        }

                        // Close the drawer on item click
                        binding.drawerLayout.closeDrawers();
                        return true;
                    }
                });

        // Hide the TextView with the page numbers. We don't need it in favorite movies, if we are
        // in popular or top rated it will be set visible again in onLoadFinished
        binding.actualPagesTv.setVisibility(View.GONE);
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

        // Restart the loader. We use initLoader, so that the loader doesn't have to be restarted if
        // data is already present.
        // Also, in this way, the scroll position in the RecyclerView gets maintained.
        if (isFavoriteMovies()) {
            getSupportLoaderManager().initLoader(MOVIES_CURSOR_LOADER_ID, null, MainActivity.this);
        } else {
            getSupportLoaderManager().initLoader(MOVIES_LIST_LOADER_ID, null, MainActivity.this);
        }
    }

    /**
     * We implement onStop to unregister the shared preferences change listener
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Unregister the listener for preference changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIES_LIST_LOADER_ID:
                // Hide the RecyclerView and show the ProgressBar
                binding.moviesRv.setVisibility(View.INVISIBLE);
                binding.loadingIndicatorPb.setVisibility(View.VISIBLE);

                // Create a new instance of MoviesListLoader
                return new MoviesListLoader(this, NetworkUtils.QUERY_LIST, null,
                        moviesSelectionPreference);

            case MOVIES_CURSOR_LOADER_ID:
                // Hide the RecyclerView and show the ProgressBar
                binding.moviesRv.setVisibility(View.INVISIBLE);
                binding.loadingIndicatorPb.setVisibility(View.VISIBLE);

                // Create a new CursorLoader
                return new CursorLoader(this,
                        FavoriteMovies.CONTENT_URI,
                        null,
                        null,
                        null,
                        FavoriteMovies._ID);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    /**
     * Actions to be taken if we refresh the activity page
     */
    @Override
    public void onRefresh() {
        // Restart the loader
        if (isFavoriteMovies()) {
            getSupportLoaderManager().initLoader(MOVIES_CURSOR_LOADER_ID, null, MainActivity.this);
        } else {
            adapter.clear();
            getSupportLoaderManager().restartLoader(MOVIES_LIST_LOADER_ID, null, MainActivity.this);
        }

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
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        switch (loader.getId()) {
            case MOVIES_LIST_LOADER_ID:
                // Hide the ProgressBar and the right FAB
                binding.loadingIndicatorPb.setVisibility(View.INVISIBLE);
                binding.floatingActionButtonRight.hide();
                // Pass the movies to the adapter. If there is no data, show an error message
                // Hide or show the FABs according to the situation
                // noinspection unchecked
                adapter.setMovies((List<Movie>) data);
                if (data == null) {
                    binding.floatingActionButtonLeft.hide();
                    binding.actualPagesTv.setVisibility(View.GONE);
                    binding.moviesRv.setVisibility(View.INVISIBLE);
                    binding.errorMessageTv.setText(R.string.error_message_no_movies);
                    binding.errorMessageTv.setVisibility(View.VISIBLE);
                } else {
                    // If we are at page 1 there is no need for a left FAB
                    if (Page.getPage() == 1) {
                        binding.floatingActionButtonLeft.hide();
                        binding.actualPagesTv.setVisibility(View.GONE);
                    }
                    // Show the RecyclerView and hide the error message
                    binding.errorMessageTv.setVisibility(View.INVISIBLE);
                    binding.moviesRv.setVisibility(View.VISIBLE);
                    binding.actualPagesTv.setText(getString(R.string.actual_page, Page.getPage(),
                            Page.getTotalPages()));
                    binding.actualPagesTv.setVisibility(View.VISIBLE);

                    // If a position has been saved and retrieved, set the actual position in the
                    // RecyclerView to that position
                    if (rv_position != -1) {
                        binding.moviesRv.smoothScrollToPosition(rv_position);
                        rv_position = -1;
                    }
                }
                break;
            case MOVIES_CURSOR_LOADER_ID:
                adapter.swapCursor((Cursor) data);
                adapter.setFavorites();
                binding.loadingIndicatorPb.setVisibility(View.INVISIBLE);
                binding.floatingActionButtonLeft.hide();
                binding.floatingActionButtonRight.hide();
                binding.actualPagesTv.setVisibility(View.GONE);
                if (((Cursor) data).getCount() == 0) {
                    // If there is no movie in database hide the RecyclerView and show a message
                    binding.moviesRv.setVisibility(View.INVISIBLE);
                    binding.errorMessageTv.setText(R.string.error_message_no_favorite_movies);
                    binding.errorMessageTv.setVisibility(View.VISIBLE);
                } else {
                    // Show the RecyclerView
                    binding.errorMessageTv.setVisibility(View.INVISIBLE);
                    binding.moviesRv.setVisibility(View.VISIBLE);

                    // If a position has been saved and retrieved, set the actual position in the
                    // RecyclerView to that position
                    if (rv_position != -1) {
                        binding.moviesRv.smoothScrollToPosition(rv_position);
                        rv_position = -1;
                    }
                }
                break;
        }
    }

    /**
     * We don't need this function
     *
     * @param loader the loader to be reset
     */
    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        // Not implemented
    }

    /**
     * Check if shared preferences have changed and behave accordingly
     *
     * @param sharedPreferences the actual shared preferences
     * @param s not used
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Get the sort order preference
        moviesSelectionPreference =
                sharedPreferences.getString(this.getString(R.string.pref_movies_selection_key),
                        this.getString(R.string.pref_movies_selection_default));

        // If we are not in favorite movies, set page to 1
        if (!isFavoriteMovies()) {
            Page.setPage(1);
            binding.floatingActionButtonRight.hide();
        }

        // Set the title in the ActionBar
        setActionBarTitle();
    }

    /**
     * Inflate the settings menu
     *
     * @param menu the menu on which we inflate the xml defined menu
     * @return true or false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Set a SearchView with which the user can insert a query string and search for movies
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                Context context = MainActivity.this;
                Class destinationClass = SearchActivity.class;
                Intent intent = new Intent(context, destinationClass);
                intent.putExtra(SearchActivity.EXTRA_QUERY_STRING, queryString);
                Page.setSearchPage(1);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    /**
     * Manage the selection of menu options
     *
     * @param item the selected item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if we are in favorite movies or else (top rated or popular)
     *
     * @return true or false
     */
    private boolean isFavoriteMovies() {
        return moviesSelectionPreference.equals(getString(R.string.pref_movies_selection_favorite));
    }

    /**
     * Navigate to a page and restart the loader
     *
     * @param pageNumber the page number to which we navigate to
     */
    @Override
    public void onSelectingPageNumber(int pageNumber) {
        if ((pageNumber >= 1) && (pageNumber <= Page.getTotalPages())) {
            // Set the actual page as from the user input and restart the loader
            // to show the page the user chose
            Page.setPage(pageNumber);
            adapter.clear();
            getSupportLoaderManager().restartLoader(MOVIES_LIST_LOADER_ID, null, MainActivity.this);
        } else {
            // If the number was not in the available range, show an error message
            Toast.makeText(this, R.string.no_page_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Change the title in the action bar depending on which section (top rated, popular or favorites
     * we are in.
     */
    private void setActionBarTitle() {
        if (moviesSelectionPreference.equals(getString(R.string.pref_movies_selection_top_rated))) {
            setTitle(getString(R.string.pref_movies_selection_top_rated_label));
        } else if (moviesSelectionPreference.equals(getString(R.string.pref_movies_selection_popular))) {
            setTitle(getString(R.string.pref_movies_selection_popular_label));
        } else if (moviesSelectionPreference.equals(getString(R.string.pref_movies_selection_favorite))) {
            setTitle(getString(R.string.pref_movies_selection_favorite_label));
        }
    }
}
