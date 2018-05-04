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

/**
 * POJO Class that represents the position (actual page) in a set of pages.
 */
public class Page {
    private static int page = 1;
    private static int totalPages;
    private static int searchPage = 1;
    private static int searchTotalPages;

    /**
     * Gets the actual page
     *
     * @return the actual page
     */
    public static int getPage() {
        return page;
    }

    /**
     * Sets the actual page
     *
     * @param page the actual page
     */
    public static void setPage(int page) {
        Page.page = page;
    }

    /**
     * Gets the actual search page
     *
     * @return the actual search page
     */
    public static int getSearchPage() {
        return searchPage;
    }

    /**
     * Sets the actual search page
     *
     * @param searchPage the actual searchPage
     */
    public static void setSearchPage(int searchPage) {
        Page.searchPage = searchPage;
    }

    /**
     * Get the total number of pages available
     *
     * @return the total number of pages available
     */
    public static int getTotalPages() {
        return totalPages;
    }

    /**
     * Sets the total number of pages available
     *
     * @param pages the total number of pages available
     */
    public static void setTotalPages(int pages) {
        Page.totalPages = pages;
    }

    /**
     * Get the total number of search pages available
     *
     * @return the total number of search pages available
     */
    public static int getSearchTotalPages() {
        return searchTotalPages;
    }

    /**
     * Sets the total number of search pages available
     *
     * @param searchTotalPages the total number of search pages available
     */
    public static void setSearchTotalPages(int searchTotalPages) {
        Page.searchTotalPages = searchTotalPages;
    }
}
