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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.android.popularmovies.data.objects.Review;
import com.example.android.popularmovies.databinding.ReviewItemBinding;

import java.util.List;

/**
 * Adapter responsible for showing the movie reviews in DetailActivity through a RecyclerView.
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {
    private List<Review> reviews;

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
    public ReviewsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Initialize DataBinding
        ReviewItemBinding itemBinding =
                ReviewItemBinding.inflate(inflater, parent, false);

        return new ReviewsAdapterViewHolder(itemBinding);
    }

    /**
     * This is executed to display the data in the specified position.
     *
     * @param holder   the ViewHolder which has to be updated
     * @param position the position of the item within the data set
     */
    @Override
    public void onBindViewHolder(@NonNull ReviewsAdapterViewHolder holder, int position) {
        // Get the review's author and text and set them to the TextViews
        String author = reviews.get(position).getAuthor() + ":";
        String review = reviews.get(position).getContent();
        holder.binding.reviewAuthorTv.setText(author);
        holder.binding.reviewContentTv.setText(review);
    }

    /**
     * Returns the total number of items in the adapter
     *
     * @return the number of items in the adapter
     */
    @Override
    public int getItemCount() {
        if (reviews == null) {
            return 0;
        } else {
            return reviews.size();
        }
    }

    /**
     * Sets the List of Review objects and notify that the data set has changed.
     *
     * @param reviews the List of Review objects
     */
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;

        notifyDataSetChanged();
    }

    /**
     * Clear the data set
     */
    void clear() {
        reviews = null;
        notifyDataSetChanged();
    }

    /**
     * The ViewHolder which will manage the single items in the RecyclerView
     */
    class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
        private final ReviewItemBinding binding;

        /**
         * The constructor
         *
         * @param binding the data binding to movie_grid_item.xml
         */
        ReviewsAdapterViewHolder(ReviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
