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

package com.example.android.popularmovies.utils;

import com.example.android.popularmovies.data.objects.Movie;
import com.example.android.popularmovies.data.objects.MovieResponse;
import com.example.android.popularmovies.data.objects.Page;
import com.example.android.popularmovies.data.objects.ReviewResponse;
import com.example.android.popularmovies.data.objects.VideoResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.util.List;

/**
 * Utilities to parse Json strings and mapping them to Java objects
 */
public class JsonUtils {

    // Private constructor, which we use to avoid instantiating this class
    private JsonUtils() {
    }

    /**
     * Parses a JSON String containing the movies list, and returns a List of Movie objects
     *
     * @param jsonString the JSON String containing the movies list
     * @return a List of Movie objects
     */
    public static List<Movie> parseMoviesList(String jsonString, int queryType) {
        List<Movie> movies;

        // Parse the Json String with Gson
        Gson gson = new GsonBuilder().serializeNulls().create();
        MovieResponse movieResponse;

        try {
            movieResponse = gson.fromJson(jsonString, MovieResponse.class);
            movies = movieResponse.getMovies();
        } catch (JsonParseException e) {
            return null;
        }

        // Set the total number of pages
        if (queryType == NetworkUtils.QUERY_LIST) {
            Page.setTotalPages(movieResponse.getTotalPages());
        } else if (queryType == NetworkUtils.QUERY_SEARCH) {
            Page.setSearchTotalPages(movieResponse.getTotalPages());
        }

        if (movies.size() > 0) {
            // Return the List of Movie objects
            return movies;
        } else {
            return null;
        }
    }

    /**
     * Parse a json string and return a VideoResponse object (containing a list of videos)
     *
     * @param jsonString the json string
     * @return a VideoResponse object
     */
    public static VideoResponse parseVideos(String jsonString) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            return gson.fromJson(jsonString, VideoResponse.class);
        } catch (JsonParseException e) {
            return null;
        }
    }

    /**
     * Parse a json string and return a ReviewResponse object (containing a list of reviews)
     *
     * @param jsonString the json string
     * @return a ReviewResponse object
     */
    public static ReviewResponse parseReviews(String jsonString) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            return gson.fromJson(jsonString, ReviewResponse.class);
        } catch (JsonParseException e) {
            return null;
        }
    }
}
