package com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn;

import java.time.Instant;

/**
 * Represents a single article fetched from an external news provider (GDELT) and exposed to the
 * authenticated home in the frontend.
 */
public record HomeNewsArticleResponse(
        String title,
        String description,
        String url,
        String imageUrl,
        String sourceName,
        Instant publishedAt
) {
}
