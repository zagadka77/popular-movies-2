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
import android.view.View;
import android.view.ViewGroup;

import com.example.android.popularmovies.data.objects.Video;
import com.example.android.popularmovies.databinding.VideoItemBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter responsible for showing the video thumbnails in DetailActivity through a RecyclerView.
 */
public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosAdapterViewHolder> {
    private final static String YOUTUBE_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/";
    private final static String YOUTUBE_THUMBNAIL_FINAL_URL = "/0.jpg";

    private final VideosAdapterOnClickHandler clickHandler;
    private List<Video> videos;

    /**
     * The constructor which initializes the click handler
     *
     * @param clickHandler the click handler
     */
    public VideosAdapter(VideosAdapterOnClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

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
    public VideosAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Initialize DataBinding
        VideoItemBinding itemBinding =
                VideoItemBinding.inflate(inflater, parent, false);

        return new VideosAdapterViewHolder(itemBinding);
    }

    /**
     * This is executed to display the data in the specified position.
     *
     * @param holder   the ViewHolder which has to be updated
     * @param position the position of the item within the data set
     */
    @Override
    public void onBindViewHolder(@NonNull VideosAdapterViewHolder holder, int position) {
        String thumbnailUrl = YOUTUBE_THUMBNAIL_BASE_URL
                + videos.get(position).getKey()
                + YOUTUBE_THUMBNAIL_FINAL_URL;

        // We don't set any placeholder or error images, we simply leave it blank
        Picasso.with(holder.binding.videoThumbnailIv.getContext())
                .load(thumbnailUrl)
                .into(holder.binding.videoThumbnailIv);

        // Get title and type of video
        holder.binding.videoTitleTv.setText(videos.get(position).getName());
        String typeParenthesized = "(" + videos.get(position).getType() + ")";
        holder.binding.videoTypeTv.setText(typeParenthesized);
    }

    /**
     * Returns the total number of items in the adapter
     *
     * @return the number of items in the adapter
     */
    @Override
    public int getItemCount() {
        if (videos == null) {
            return 0;
        } else {
            return videos.size();
        }
    }

    /**
     * Sets the List of Video objects and notify that the data set has changed.
     *
     * @param videos the List of Video objects
     */
    public void setVideos(List<Video> videos) {
        this.videos = videos;

        notifyDataSetChanged();
    }

    /**
     * Clear the data set
     */
    void clear() {
        videos = null;
        notifyDataSetChanged();
    }

    /**
     * The interface which will be implemented to manage clicks on single movie thumbnail
     */
    public interface VideosAdapterOnClickHandler {
        void onClick(Video video);
    }

    /**
     * The ViewHolder which will manage the single items in the RecyclerView
     */
    class VideosAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final VideoItemBinding binding;

        /**
         * The constructor
         *
         * @param binding the data binding to video_item.xml
         */
        VideosAdapterViewHolder(VideoItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set an OnClickListener on the item
            binding.videoThumbnailIv.setOnClickListener(this);
        }

        /**
         * Manages the click events on the single item, calling the onClick function in DetailActivity
         *
         * @param view the view which has been clicked on it
         */
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Video video = videos.get(adapterPosition);
            clickHandler.onClick(video);
        }
    }
}
