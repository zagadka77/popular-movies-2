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

package com.example.android.popularmovies.loaders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.popularmovies.data.objects.Movie;
import com.example.android.popularmovies.data.objects.Page;
import com.example.android.popularmovies.utils.JsonUtils;
import com.example.android.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.List;

/**
 * A subclass of AsyncTaskLoader which is responsible for communicating with the API on a
 * background thread and retrieve a list of movies
 */
public class MoviesListLoader extends AsyncTaskLoader<List<Movie>> {
    private List<Movie> movies = null;
    private final int queryType;
    private final String queryString;
    private final String moviesSelectionPreference;

    /**
     * The constructor
     *
     * @param context                   the context
     * @param queryType                 the type of query
     * @param moviesSelectionPreference the selection preference
     * @param queryString               the query string (for search)
     */
    public MoviesListLoader(Context context, int queryType, String queryString, String moviesSelectionPreference) {
        super(context);
        this.queryType = queryType;
        this.moviesSelectionPreference = moviesSelectionPreference;
        this.queryString = queryString;
    }

    @Override
    protected void onStartLoading() {
        // Check if there is cached data,
        if (movies != null) {
            // If there is cached data, call deliverResult and pass the cached data as argument
            deliverResult(movies);
        } else {
            // If there is no cached data, force load
            forceLoad();
        }
    }

    @Nullable
    @Override
    public List<Movie> loadInBackground() {
        // Get the url with which to make the http request
        URL movieRequestUrl;
        switch (queryType) {
            case NetworkUtils.QUERY_LIST:
                // Get url for popular or top rated movies
                movieRequestUrl = NetworkUtils.getMoviesListUrl(
                        moviesSelectionPreference,
                        String.valueOf(Page.getPage()),
                        queryType,
                        null);
                break;
            case NetworkUtils.QUERY_SEARCH:
                // Get url for the search query
                movieRequestUrl = NetworkUtils.getMoviesListUrl(
                        null,
                        String.valueOf(Page.getSearchPage()),
                        queryType,
                        queryString);
                break;
            default:
                movieRequestUrl = null;
                break;
        }

        try {
            // Make the http request to the API
            String jsonResponse = null;
            if (movieRequestUrl != null) {
                jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
            }

            // Parse the JSON response into a List of Movie objects
            if (jsonResponse != null) {
                movies = JsonUtils.parseMoviesList(jsonResponse, queryType);
            } else {
                return null;
            }

            // Return the list
            return movies;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cache data
     *
     * @param moviesList the List of Movie objects
     */
    public void deliverResult(List<Movie> moviesList) {
        movies = moviesList;
        super.deliverResult(moviesList);
    }
}
