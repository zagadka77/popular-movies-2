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

import java.util.ArrayList;
import java.util.List;

public class VideoResponse {
    private final List<Video> results;

    public VideoResponse(List<Video> results) {
        this.results = results;
    }

    public List<Video> getVideos() {
        if ((results != null) && (results.size() > 0)) {
            // Remove all non-YouTube videos from results
            List<Video> videosToRemove = new ArrayList<>();
            for (Video video : results) {
                if (!video.getSite().equals("YouTube")) {
                    videosToRemove.add(video);
                }
            }

            for (Video video : videosToRemove) {
                results.remove(video);
            }

            return results;
        } else {
            return null;
        }
    }
}
