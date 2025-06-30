package com.music.application.be.common;

import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

/**
 * Utility class for building paginated responses.
 */
public class PaginationUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private PaginationUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    /**
     * Converts a {@link Page} and its content into a standardized {@link PagedResponse}.
     *
     * @param content the list of items on the current page (typically DTOs)
     * @param page    the original Page object from Spring Data
     * @param <T>     the type of content in the page
     * @return a PagedResponse containing pagination metadata and content
     * @throws IllegalArgumentException if the page is null
     */
    public static <T> PagedResponse<T> buildPagedResponse(List<T> content, Page<?> page) {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null.");
        }

        // Use empty list if content is null to prevent NullPointerException
        List<T> safeContent = (content == null) ? Collections.emptyList() : content;

        return new PagedResponse<>(
                safeContent,
                page.getNumber(),        // current page number (0-based)
                page.getSize(),          // size of the page
                page.getTotalElements(), // total number of elements
                page.getTotalPages(),    // total number of pages
                page.isLast()            // whether this is the last page
        );
    }
}

