package com.teamfoundry.backend.superadmin.service.home;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.superadmin.config.home.GdeltNewsProperties;
import com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn.HomeNewsArticleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class GdeltNewsService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> BASE_KEYWORDS = List.of(
            "\"emprego\"",
            "\"mercado de trabalho\"",
            "\"desemprego\"",
            "\"contratação\"",
            "\"despedimentos\"",
            "\"indústria\"",
            "\"economia\""
    );
    private static final int MIN_ITEMS = 1;
    private static final int MAX_ITEMS = 6;
    private static final int MAX_GDELT_RECORDS = 18;

    private final RestClient client;
    private final GdeltNewsProperties properties;

    public GdeltNewsService(RestClient.Builder builder, GdeltNewsProperties properties) {
        this.properties = properties;
        if (properties.getTimeoutMs() > 0) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(properties.getTimeoutMs());
            factory.setReadTimeout(properties.getTimeoutMs());
            this.client = builder
                    .requestFactory(factory)
                    .baseUrl(properties.getBaseUrl())
                    .build();
        } else {
            this.client = builder.baseUrl(properties.getBaseUrl()).build();
        }
    }

    public List<HomeNewsArticleResponse> getLatestNews() {
        return fetchArticles("emprego", MAX_ITEMS, "[GDELT] Latest news");
    }

    public List<HomeNewsArticleResponse> searchNews(String term) {
        return fetchArticles(term, MAX_ITEMS, "[GDELT] Search news");
    }

    public List<HomeNewsArticleResponse> getNewsByTopic(String topic) {
        return fetchArticles(topic, MAX_ITEMS, "[GDELT] Topic news: " + topic);
    }

    public List<HomeNewsArticleResponse> getLatestNews(String term, int limit) {
        return fetchArticles(term, limit, "[GDELT] Latest news (custom limit)");
    }

    public List<HomeNewsArticleResponse> getEmpregabilidadeNews(int limit) {
        return fetchArticles("emprego", limit, "[GDELT] Empregabilidade news");
    }

    private List<HomeNewsArticleResponse> fetchArticles(String topic, int limit, String contextLabel) {
        if (!properties.isEnabled()) {
            log.debug("{} skipped because app.gdelt.enabled=false.", contextLabel);
            return Collections.emptyList();
        }

        int normalizedLimit = Math.max(MIN_ITEMS, Math.min(limit, MAX_ITEMS));
        int maxRecords = Math.min(Math.max(normalizedLimit, MIN_ITEMS), MAX_GDELT_RECORDS);

        String queryWithLang = buildGdeltQuery(topic, true);
        List<HomeNewsArticleResponse> primary = executeRequest(queryWithLang, maxRecords, normalizedLimit, contextLabel);
        if (!primary.isEmpty()) {
            return primary;
        }

        log.info("{} fallback without sourceLang (need more results).", contextLabel);
        String queryWithoutLang = buildGdeltQuery(topic, false);
        return executeRequest(queryWithoutLang, maxRecords, normalizedLimit, contextLabel + " (fallback)");
    }

    private String buildGdeltQuery(String topic, boolean includeLanguage) {
        List<String> terms = new java.util.ArrayList<>(BASE_KEYWORDS);
        if (StringUtils.hasText(topic) && !"emprego".equalsIgnoreCase(topic.trim())) {
            terms.add(formatTerm(topic));
        }
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(String.join(" OR ", terms)).append(")");
        if (includeLanguage && StringUtils.hasText(properties.getLanguage())) {
            builder.append(" sourceLang:").append(properties.getLanguage().trim().toUpperCase(Locale.ROOT));
        }
        return builder.toString().trim();
    }

    private String formatTerm(String term) {
        return "\"" + term.trim() + "\"";
    }

    private List<HomeNewsArticleResponse> executeRequest(String query, int maxRecords, int limit, String contextLabel) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                    .queryParam("query", query)
                    .queryParam("mode", properties.getMode())
                    .queryParam("format", properties.getFormat())
                    .queryParam("sort", "DateDesc")
                    .queryParam("maxrecords", maxRecords)
                    .encode()
                    .build(true)
                    .toUri();

            String payload = client.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
            log.info("{} request made to {} (maxrecords={}, limit={})", contextLabel, uri, maxRecords, limit);

            GdeltResponse response = parseResponse(payload, contextLabel);
            return mapResponse(response, limit, contextLabel);
        } catch (Exception ex) {
            log.warn("{} failed: {} | query=\"{}\"", contextLabel, ex.getMessage(), query);
            log.debug("Full error details", ex);
            return Collections.emptyList();
        }
    }

    private GdeltResponse parseResponse(String payload, String contextLabel) {
        if (!StringUtils.hasText(payload)) {
            log.info("{} returned empty body.", contextLabel);
            return null;
        }
        try {
            return MAPPER.readValue(payload, GdeltResponse.class);
        } catch (JsonProcessingException ex) {
            String snippet = payload.length() > 300 ? payload.substring(0, 300) + "..." : payload;
            log.warn("{} unexpected response body (not JSON). Snippet: {}", contextLabel, snippet);
            log.debug("Failed to parse GDELT payload", ex);
            return null;
        }
    }

    private List<HomeNewsArticleResponse> mapResponse(GdeltResponse response, int limit, String contextLabel) {
        if (response == null) {
            log.warn("{} returned null response.", contextLabel);
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(response.articles())) {
            log.info("{} returned no articles (raw size {}).", contextLabel, response.articles() == null ? 0 : response.articles().size());
            return Collections.emptyList();
        }

        List<HomeNewsArticleResponse> mapped = response.articles().stream()
                .filter(article -> StringUtils.hasText(article.url()))
                .map(this::mapArticle)
                .limit(limit)
                .toList();
        log.info("{} returned {} mapped articles (raw size {}).", contextLabel, mapped.size(), response.articles().size());
        return mapped;
    }

    private HomeNewsArticleResponse mapArticle(GdeltArticle article) {
        String title = StringUtils.hasText(article.title()) ? article.title() : Optional.ofNullable(article.excerpt()).orElse("");
        String summary = firstNonEmpty(article.excerpt(), article.snippet(), article.translation());
        String image = firstNonEmpty(article.socialImage(), article.image(), article.thumbnail());
        String source = firstNonEmpty(article.sourceCommonName(), article.domain(), article.sourceCountry());
        return new HomeNewsArticleResponse(
                title,
                summary,
                article.url(),
                image,
                source,
                parsePublishedAt(article.urlPubTimeDate(), article.seenDate())
        );
    }

    private Instant parsePublishedAt(String primary, String fallback) {
        String value = firstNonEmpty(primary, fallback);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        } catch (DateTimeParseException ex) {
            // ignore and try alternate
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT);
            return OffsetDateTime.parse(value, formatter.withZone(java.time.ZoneOffset.UTC)).toInstant();
        } catch (DateTimeParseException ex) {
            log.debug("Could not parse published date [{}]", value, ex);
            return null;
        }
    }

    private String firstNonEmpty(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String sanitize(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        return input.trim();
    }

    private record GdeltResponse(@JsonProperty("articles") List<GdeltArticle> articles) {
    }

    private record GdeltArticle(
            @JsonProperty("title") String title,
            @JsonProperty("url") String url,
            @JsonProperty("seendate") String seenDate,
            @JsonProperty("urlpubtimedate") String urlPubTimeDate,
            @JsonProperty("socialimage") String socialImage,
            @JsonProperty("image") String image,
            @JsonProperty("thumbnail") String thumbnail,
            @JsonProperty("domain") String domain,
            @JsonProperty("sourceCommonName") String sourceCommonName,
            @JsonProperty("sourcecountry") String sourceCountry,
            @JsonProperty("lang") String language,
            @JsonProperty("excerpt") String excerpt,
            @JsonProperty("snippet") String snippet,
            @JsonProperty("translation") String translation
    ) {
    }
}
