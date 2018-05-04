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
import com.example.android.popularmovies.data.objects.ReviewResponse;
import com.example.android.popularmovies.data.objects.VideoResponse;
import com.example.android.popularmovies.utils.JsonUtils;
import com.example.android.popularmovies.utils.NetworkUtils;

import java.net.URL;

/**
 * A subclass of AsyncTaskLoader which is responsible for communicating with the API on a
 * background thread and retrieve information about a single movie
 */
public class MovieDetailLoader extends AsyncTaskLoader<Movie> {
    private final Movie tempMovie;
    private Movie movie = null;

    public MovieDetailLoader(Context context, Movie tempMovie) {
        super(context);
        this.tempMovie = tempMovie;
    }

    @Override
    protected void onStartLoading() {
        // Check if there is cached data,
        if (movie != null) {
            // If there is cached data, call deliverResult and pass the cached data as argument
            deliverResult(movie);
        } else {
            // If there is no cached data, force load
            forceLoad();
        }
    }

    @Nullable
    @Override
    public Movie loadInBackground() {
        try {
            movie = tempMovie;
            int movieId = movie.getMovieId();

            // Get the videos
            URL videosUrl = NetworkUtils.getVideosUrl(movieId);
            String videosJsonResponse = NetworkUtils.getResponseFromHttpUrl(videosUrl);
            VideoResponse videos = JsonUtils.parseVideos(videosJsonResponse);
            movie.setVideos(videos);


            // Get the reviews
            URL reviewsUrl = NetworkUtils.getReviewsUrl(movieId);
            String reviewsJsonResponse = NetworkUtils.getResponseFromHttpUrl(reviewsUrl);
            ReviewResponse reviews = JsonUtils.parseReviews(reviewsJsonResponse);
            movie.setReviews(reviews);

            // Return the movie
            return movie;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cache data
     *
     * @param movie the Movie object
     */
    public void deliverResult(Movie movie) {
        this.movie = movie;
        super.deliverResult(movie);
    }
}
