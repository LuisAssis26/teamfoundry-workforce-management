package com.teamfoundry.backend.superadmin.config.home;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.news")
public class NewsApiProperties {

    /**
     * Whether the NewsAPI integration is enabled. Defaults to true so that
     * providing a valid API key is enough to activate it.
     */
    private boolean enabled = true;

    /**
     * API key provided by NewsAPI.org.
     */
    private String apiKey;

    /**
     * Base URL used to reach the NewsAPI endpoints.
     */
    private String baseUrl = "https://newsapi.org/v2";

    /**
     * Relative path for the top headlines endpoint.
     */
    private String topHeadlinesPath = "/top-headlines";

    /**
     * Relative path for the everything endpoint.
     */
    private String everythingPath = "/everything";

    /**
     * Country filter to keep the feed relevant to Portuguese (pt) readers.
     */
    private String country = "pt";

    /**
     * Category filter for curated top headlines.
     */
    private String category = "business";

    /**
     * Comma separated list of sources used to fetch Portuguese news.
     */
    private String portugueseSources = "publico,observador,jn";

    /**
     * Language used on the everything endpoint when no sources are provided.
     */
    private String language = "pt";

    /**
     * Optional general query appended to NewsAPI requests.
     */
    private String query = "Portugal";
}
