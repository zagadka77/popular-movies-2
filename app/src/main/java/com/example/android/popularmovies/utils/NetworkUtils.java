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

import android.net.Uri;

import com.example.android.popularmovies.BuildConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Utilities to communicate with the API and retrieve data from network.
 */
public class NetworkUtils {
    // Selectors for query type
    public static final int QUERY_LIST = 0;
    public static final int QUERY_SEARCH = 1;
    // Selectors for image type
    public static final int IMAGE_POSTER = 0;
    public static final int IMAGE_BACKDROP = 1;
    // API Key for themoviedb.org
    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String API_KEY_QUERY = "api_key";
    // URL parts for images
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p";
    private static final String IMAGE_SIZE_W342 = "w342";
    private static final String IMAGE_SIZE_W780 = "w780";
    // URL parts for movies list
    private static final String API_BASE_URL = "http://api.themoviedb.org/3/";
    private static final String API_MOVIE = "movie";
    private static final String API_PAGE = "page";
    private static final String API_SEARCH = "search";
    private static final String API_QUERY = "query";
    private static final String API_VIDEOS = "videos";
    private static final String API_REVIEWS = "reviews";
    private static final String API_LANGUAGE = "language";
    private static final String API_LANGUAGE_EN = "en-US";
    // OkHttpClient
    private static final OkHttpClient client = new OkHttpClient();

    // Private constructor, which we use to avoid instantiating this class
    private NetworkUtils() {
    }

    /**
     * Builds the complete url to an image.
     *
     * @param imageRelativePath the relative path of the image
     * @param imageType         the image type (poster or backdrop)
     * @return the complete url of the image
     */
    public static URL getImageUrl(String imageRelativePath, int imageType) {
        String imageSize;

        // Check the image type and sets the image size
        switch (imageType) {
            case IMAGE_BACKDROP:
                imageSize = IMAGE_SIZE_W780;
                break;
            case IMAGE_POSTER:
            default:
                imageSize = IMAGE_SIZE_W342;
                break;
        }

        // Cut the forward slash at the beginning of the image relative path
        if ((imageRelativePath != null) && !imageRelativePath.equals("")) {
            imageRelativePath = imageRelativePath.substring(1);
        }

        // Build the Uri
        Uri uri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                .appendPath(imageSize)
                .appendPath(imageRelativePath)
                .build();

        URL url = null;
        try {
            // Transform the Uri into a URL
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Gets the url which will be used to retrieve the movies list
     * API endpoint for movies list (popular movies): /movie/popular
     * API endpoint for movies list (top rated movies): /movie/top_rated
     * API endpoint for movies search: /search/movie
     *
     * @param sortBy sort order preference
     * @param page   the actual page
     * @param type   the type of query (show a list of movies or search for movies)
     * @param query  a search query
     * @return the url for the movies list
     */
    public static URL getMoviesListUrl(String sortBy, String page, int type, String query) {
        Uri uri;
        switch (type) {
            case NetworkUtils.QUERY_LIST:
                // Build the Uri
                uri = Uri.parse(API_BASE_URL).buildUpon()
                        .appendPath(API_MOVIE)
                        .appendPath(sortBy)
                        .appendQueryParameter(API_PAGE, String.valueOf(page))
                        .appendQueryParameter(API_LANGUAGE, API_LANGUAGE_EN)
                        .appendQueryParameter(API_KEY_QUERY, API_KEY)
                        .build();
                break;
            case NetworkUtils.QUERY_SEARCH:
                // Build the Uri
                uri = Uri.parse(API_BASE_URL).buildUpon()
                        .appendPath(API_SEARCH)
                        .appendPath(API_MOVIE)
                        .appendQueryParameter(API_PAGE, String.valueOf(page))
                        .appendQueryParameter(API_LANGUAGE, API_LANGUAGE_EN)
                        .appendQueryParameter(API_KEY_QUERY, API_KEY)
                        .appendQueryParameter(API_QUERY, query)
                        .build();
                break;
            default:
                uri = null;
                break;
        }

        URL url = null;
        try {
            if (uri != null) {
                // Transform the Uri into a URL
                url = new URL(uri.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Gets the url which will be used to retrieve the videos
     * API endpoint: /movie/{id}/videos/
     *
     * @param movieId the movie id
     * @return the URL to retrieve the reviews
     */
    public static URL getVideosUrl(int movieId) {
        Uri uri;
        // Build the uri
        uri = Uri.parse(API_BASE_URL).buildUpon()
                .appendPath(API_MOVIE)
                .appendPath(String.valueOf(movieId))
                .appendPath(API_VIDEOS)
                .appendQueryParameter(API_LANGUAGE, API_LANGUAGE_EN)
                .appendQueryParameter(API_KEY_QUERY, API_KEY)
                .build();

        URL url = null;
        try {
            if (uri != null) {
                // Transform the Uri into a URL
                url = new URL(uri.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Gets the url which will be used to retrieve the reviews
     * API endpoint: /movie/{id}/reviews/
     *
     * @param movieId the movie id
     * @return the URL to retrieve the reviews
     */
    public static URL getReviewsUrl(int movieId) {
        Uri uri;
        // Build the uri
        uri = Uri.parse(API_BASE_URL).buildUpon()
                .appendPath(API_MOVIE)
                .appendPath(String.valueOf(movieId))
                .appendPath(API_REVIEWS)
                .appendQueryParameter(API_LANGUAGE, API_LANGUAGE_EN)
                .appendQueryParameter(API_KEY_QUERY, API_KEY)
                .build();

        URL url = null;
        try {
            if (uri != null) {
                // Transform the Uri into a URL
                url = new URL(uri.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Returns the entire result from the HTTP response using OkHttp
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        Request request = new Request.Builder()
                .url(url.toString())
                .build();

        Response response = client.newCall(request).execute();

        ResponseBody body = response.body();
        if ((body != null) && (response.isSuccessful())) {
            return body.string();
        } else {
            return null;
        }
    }
}
