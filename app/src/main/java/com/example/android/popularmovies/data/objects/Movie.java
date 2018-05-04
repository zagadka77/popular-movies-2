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

package com.example.android.popularmovies.data.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO Class that represents a single movie.
 * <p>
 * It implements Parcelable in order to be able to pass a Movie object through an intent.
 * The app does that when it transitions from MainActivity to DetailActivity, to show the details
 * of a particular movie.
 */
public class Movie implements Parcelable {

    /**
     * Implements the Parcelable.Creator interface
     */
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @SerializedName("id")
    private final int movieId;
    @SerializedName("original_title")
    private String originalTitle;
    @SerializedName("title")
    private String title;
    @SerializedName("poster_path")
    private final String posterImageUrl;
    @SerializedName("backdrop_path")
    private final String backdropImageUrl;
    @SerializedName("overview")
    private final String overview;
    @SerializedName("vote_average")
    private final double userRating;
    @SerializedName("release_date")
    private final String releaseDate;
    @SerializedName("videos")
    private VideoResponse videos;
    @SerializedName("reviews")
    private ReviewResponse reviews;

    /**
     * Constructor which sets all the movie's member variables
     *
     * @param movieId          the movie's id
     * @param originalTitle    the movie's original title
     * @param title            the movie's title
     * @param posterImageUrl   the movie's poster image url
     * @param backdropImageUrl the movie's backdrop image url
     * @param overview         a synopsis of the movie
     * @param userRating       the movie's user rating
     * @param releaseDate      the movie's release date
     */
    public Movie(int movieId,
                 String originalTitle,
                 String title,
                 String posterImageUrl,
                 String backdropImageUrl,
                 String overview,
                 double userRating,
                 String releaseDate) {

        this.movieId = movieId;
        this.originalTitle = originalTitle;
        this.title = title;
        this.posterImageUrl = (posterImageUrl == null ? "" : posterImageUrl);
        this.backdropImageUrl = (backdropImageUrl == null ? "" : backdropImageUrl);
        this.overview = overview;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.videos = null;
        this.reviews = null;

        // If there is no original title, substitute it with the title
        if (originalTitle.equals("") && (!title.equals(""))) {
            this.originalTitle = title;
        }

        // If there is no title, substitute it with the original title
        if (!originalTitle.equals("") && (title.equals(""))) {
            this.title = originalTitle;
        }
    }

    /**
     * Constructor to read from a Parcelable
     *
     * @param in a parcel to read
     */
    private Movie(Parcel in) {
        this.movieId = in.readInt();
        this.originalTitle = in.readString();
        this.title = in.readString();
        this.posterImageUrl = in.readString();
        this.backdropImageUrl = in.readString();
        this.overview = in.readString();
        this.userRating = in.readDouble();
        this.releaseDate = in.readString();
    }

    public int getMovieId() {
        return movieId;
    }

    /**
     * Getter method for originalTitle
     *
     * @return the movie's original title
     */
    public String getOriginalTitle() {
        // If there is no original title, substitute it with the title
        if (originalTitle.equals("") && (!title.equals(""))) {
            originalTitle = title;
        }

        return originalTitle;
    }

    /**
     * Getter method for title
     *
     * @return the movie's title
     */
    public String getTitle() {
        // If there is no title, substitute it with the original title
        if (!originalTitle.equals("") && (title.equals(""))) {
            title = originalTitle;
        }

        return title;
    }

    /**
     * Setter method for Title
     *
     * @param title the movie's original title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter method for posterImageUrl
     *
     * @return the movie's poster image url
     */
    public String getPosterImageUrl() {
        return posterImageUrl;
    }

    /**
     * Getter method for backdropImageUrl
     *
     * @return the movie's backdrop image url
     */
    public String getBackdropImageUrl() {
        return backdropImageUrl;
    }

    /**
     * Getter method for overview
     *
     * @return a synopsis of the movie
     */
    public String getOverview() {
        return overview;
    }

    /**
     * Getter method for userRating
     *
     * @return the movie's user rating
     */
    public float getUserRating() {
        return (float) userRating;
    }

    /**
     * Getter method for releaseDate
     *
     * @return the movie's release date
     */
    public String getReleaseDate() {
        if (releaseDate != null && !releaseDate.equals("")) {
            return releaseDate.substring(0, 4);
        } else {
            return null;
        }
    }

    /**
     * Method required from the Parcelable interface
     *
     * @return 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Method required from the Parcelable interface
     *
     * @param parcel the parcel to write on
     * @param i      optional flags
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(movieId);
        parcel.writeString(originalTitle);
        parcel.writeString(title);
        parcel.writeString(posterImageUrl);
        parcel.writeString(backdropImageUrl);
        parcel.writeString(overview);
        parcel.writeDouble(userRating);
        parcel.writeString(releaseDate);
    }

    /**
     * Getter method for the videos
     *
     * @return the List of Video objects
     */
    public List<Video> getVideos() {
        if (videos != null) {
            return videos.getVideos();
        } else {
            return null;
        }
    }

    /**
     * Setter method for the videos
     *
     * @param videos the videos
     */
    public void setVideos(VideoResponse videos) {
        this.videos = videos;
    }

    /**
     * Getter method for the reviews
     *
     * @return the List of Review objects
     */
    public List<Review> getReviews() {
        if (reviews != null) {
            return reviews.getReviews();
        } else {
            return null;
        }
    }

    /**
     * Setter method for the reviews
     *
     * @param reviews the reviews
     */
    public void setReviews(ReviewResponse reviews) {
        this.reviews = reviews;
    }
}
