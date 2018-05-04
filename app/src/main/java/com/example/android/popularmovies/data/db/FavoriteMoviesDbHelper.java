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

package com.example.android.popularmovies.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.data.db.FavoriteMoviesContract.FavoriteMovies;

/**
 * The DbHelper that we use to create and upgrade the database of favorite movies
 */
class FavoriteMoviesDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorite_movies.db";
    private static final int DATABASE_VERSION = 1;

    FavoriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // String query that will create the database
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " +
                FavoriteMovies.TABLE_NAME + " (" +
                FavoriteMovies._ID + " INTEGER PRIMARY KEY, " +
                FavoriteMovies.COLUMN_ORIGINAL_TITLE + " TEXT, " +
                FavoriteMovies.COLUMN_TITLE + " TEXT, " +
                FavoriteMovies.COLUMN_POSTER_PATH + " TEXT, " +
                FavoriteMovies.COLUMN_BACKDROP_PATH + " TEXT, " +
                FavoriteMovies.COLUMN_OVERVIEW + " TEXT, " +
                FavoriteMovies.COLUMN_VOTE_AVERAGE + " FLOAT, " +
                FavoriteMovies.COLUMN_RELEASE_DATE + " TEXT" +
                ");";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Destroys and recreates the table
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteMovies.TABLE_NAME);
        onCreate(db);
    }
}
