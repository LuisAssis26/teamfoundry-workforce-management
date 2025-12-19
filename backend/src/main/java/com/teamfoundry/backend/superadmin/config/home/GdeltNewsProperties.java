package com.teamfoundry.backend.superadmin.config.home;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.gdelt")
public class GdeltNewsProperties {

    /**
     * Toggle to enable/disable GDELT integration.
     */
    private boolean enabled = true;

    /**
     * Base URL for the GDELT Doc API.
     */
    private String baseUrl = "https://api.gdeltproject.org/api/v2/doc/doc";

    /**
     * Default request mode. ArtList returns a compact article list.
     */
    private String mode = "ArtList";

    /**
     * Output format requested to GDELT.
     */
    private String format = "json";

    /**
     * Maximum number of records fetched per request (GDELT max is 250).
     */
    private int maxRecords = 50;

    /**
     * Preferred country filter, PT by default.
     */
    private String country = "";

    /**
     * Preferred language filter for sources (GDELT uses English names, e.g., "portuguese").
     */
    private String language = "POR";

    /**
     * Whether we allow a fallback request without the country filter when few/no results are returned.
     */
    private boolean fallbackEnabled = true;

    /**
     * Optional request timeout (milliseconds) applied on each call, 0 to use client default.
     */
    private int timeoutMs = 0;
}
