package com.teamfoundry.backend.superadmin.service.home;

import com.teamfoundry.backend.superadmin.config.home.NewsApiProperties;
import com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn.HomeNewsArticleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NewsApiService {

    private static final int MIN_ITEMS = 1;
    private static final int MAX_ITEMS = 6;

    private final RestClient client;
    private final NewsApiProperties properties;

    public NewsApiService(RestClient.Builder builder, NewsApiProperties properties) {
        this.client = builder.baseUrl(properties.getBaseUrl()).build();
        this.properties = properties;
    }

    public List<HomeNewsArticleResponse> getPortugueseHeadlines() {
        return getPortugueseHeadlines(MAX_ITEMS);
    }

    public List<HomeNewsArticleResponse> getPortugueseHeadlines(int limit) {
        return performRequest(
                properties.getTopHeadlinesPath(),
                limit,
                builder -> {
                    builder.queryParam("country", properties.getCountry());
                    if (StringUtils.hasText(properties.getCategory())) {
                        builder.queryParam("category", properties.getCategory());
                    }
                    if (StringUtils.hasText(properties.getQuery())) {
                        builder.queryParam("q", properties.getQuery());
                    }
                    return builder;
                },
                "[NewsAPI] Portuguese headlines"
        );
    }

    public List<HomeNewsArticleResponse> getLatestNews(String query) {
        return getLatestNews(query, MAX_ITEMS);
    }

    public List<HomeNewsArticleResponse> getLatestNews(String query, int limit) {
        return performRequest(
                properties.getEverythingPath(),
                limit,
                builder -> {
                    builder.queryParam("sortBy", "publishedAt")
                            .queryParam("q", sanitizeQuery(query));
                    if (StringUtils.hasText(properties.getLanguage())) {
                        builder.queryParam("language", properties.getLanguage());
                    }
                    return builder;
                },
                "[NewsAPI] Latest news"
        );
    }

    public List<HomeNewsArticleResponse> getLatestPortugueseNews() {
        return getLatestPortugueseNews(MAX_ITEMS);
    }

    public List<HomeNewsArticleResponse> getLatestPortugueseNews(int limit) {
        // Overfetch to compensate for articles descartados sem URL/título, mas limitar no retorno ao "limit" solicitado.
        int fetchSize = Math.min(Math.max(limit * 3, limit), 30);
        return performRequest(
                properties.getEverythingPath(),
                limit,
                fetchSize,
                builder -> builder
                        .queryParam("sortBy", "publishedAt")
                        .queryParam("language", properties.getLanguage())
                        .queryParam("q", sanitizeQuery(null)),
                "[NewsAPI] Latest Portuguese news"
        );
    }

    public List<HomeNewsArticleResponse> getNewsByQuery(String query) {
        return getNewsByQuery(query, MAX_ITEMS);
    }

    public List<HomeNewsArticleResponse> getNewsByQuery(String query, int limit) {
        return performRequest(
                properties.getEverythingPath(),
                limit,
                builder -> builder.queryParam("q", sanitizeQuery(query)),
                "[NewsAPI] News by query"
        );
    }

    public List<HomeNewsArticleResponse> getEmpregabilidadeNews() {
        return getEmpregabilidadeNews(MAX_ITEMS);
    }

    public List<HomeNewsArticleResponse> getEmpregabilidadeNews(int limit) {
        return queryNewsByKeywords(
                "[NewsAPI] Empregabilidade news",
                limit,
                "empregabilidade",
                "emprego",
                "mercado de trabalho",
                "recrutamento",
                "vagas",
                "competencias profissionais",
                "formacao profissional"
        );
    }

    public List<HomeNewsArticleResponse> getMetalurgiaNews() {
        return getMetalurgiaNews(MAX_ITEMS);
    }

    public List<HomeNewsArticleResponse> getMetalurgiaNews(int limit) {
        return queryNewsByKeywords(
                "[NewsAPI] Metalurgia news",
                limit,
                "metalurgia",
                "siderurgia",
                "fundicao",
                "metalworking",
                "steel",
                "aluminio",
                "industria metalurgica",
                "producao de metais"
        );
    }

    public List<HomeNewsArticleResponse> getImpregnabilidadeNews() {
        return getImpregnabilidadeNews(MAX_ITEMS);
    }

    public List<HomeNewsArticleResponse> getImpregnabilidadeNews(int limit) {
        return queryNewsByKeywords(
                "[NewsAPI] Impregnabilidade news",
                limit,
                "impregnabilidade",
                "impregnacao",
                "processos de impregnacao",
                "tratamento de materiais",
                "resinas industriais"
        );
    }

    private List<HomeNewsArticleResponse> queryNewsByKeywords(
            String contextLabel,
            int limit,
            String... keywords
    ) {
        return performRequest(
                properties.getEverythingPath(),
                limit,
                builder -> builder
                        .queryParam("sortBy", "publishedAt")
                        .queryParam("language", properties.getLanguage())
                        .queryParam("q", buildKeywordQuery(keywords)),
                contextLabel
        );
    }

    private List<HomeNewsArticleResponse> performRequest(
            String path,
            int limit,
            Function<UriBuilder, UriBuilder> uriCustomizer,
            String contextLabel
    ) {
        return performRequest(path, limit, limit, uriCustomizer, contextLabel);
    }

    private List<HomeNewsArticleResponse> performRequest(
            String path,
            int limit,
            int fetchSize,
            Function<UriBuilder, UriBuilder> uriCustomizer,
            String contextLabel
    ) {
        if (!properties.isEnabled()) {
            log.debug("{} ignorado porque app.news.enabled=false.", contextLabel);
            return Collections.emptyList();
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            log.warn("NEWSAPI_KEY nao configurada. {} cancelado.", contextLabel);
            return Collections.emptyList();
        }

        int pageSize = clampPageSize(fetchSize);

        try {
            log.info("{} iniciando requisicao (limit={}).", contextLabel, pageSize);
            NewsApiResponse response = client.get()
                    .uri(uriBuilder -> {
                        UriBuilder builder = uriBuilder.path(path)
                                .queryParam("pageSize", pageSize);
                        if (uriCustomizer != null) {
                            builder = uriCustomizer.apply(builder);
                        }
                        return builder.build();
                    })
                    .header("X-Api-Key", properties.getApiKey())
                    .retrieve()
                    .body(NewsApiResponse.class);
            return handleResponse(response, limit, contextLabel);
        } catch (Exception ex) {
            log.warn("{} falhou: {}", contextLabel, ex.getMessage());
            log.debug("Detalhes completos do erro", ex);
            return Collections.emptyList();
        }
    }

    private List<HomeNewsArticleResponse> handleResponse(
            NewsApiResponse response,
            int limit,
            String contextLabel
    ) {
        if (response == null) {
            log.warn("{} respondeu vazio (null).", contextLabel);
            return Collections.emptyList();
        }
        log.info("{} status='{}' totalResults={}", contextLabel, response.status(), response.totalResults());
        if (!"ok".equalsIgnoreCase(response.status())) {
            log.warn("{} devolveu estado '{}'.", contextLabel, response.status());
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(response.articles())) {
            log.info("{} nao retornou artigos.", contextLabel);
            return Collections.emptyList();
        }

        List<HomeNewsArticleResponse> mapped = response.articles().stream()
                .filter(article -> StringUtils.hasText(article.url()))
                .map(this::mapArticle)
                .limit(limit)
                .toList();
        log.info("{} devolveu {} artigos apos filtragem.", contextLabel, mapped.size());
        return mapped;
    }

    private HomeNewsArticleResponse mapArticle(NewsApiArticle article) {
        return new HomeNewsArticleResponse(
                StringUtils.hasText(article.title()) ? article.title() : Optional.ofNullable(article.description()).orElse(""),
                article.description(),
                article.url(),
                article.urlToImage(),
                Optional.ofNullable(article.source()).map(NewsApiSource::name).orElse(null),
                parsePublishedAt(article.publishedAt())
        );
    }

    private Instant parsePublishedAt(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(raw).toInstant();
        } catch (DateTimeParseException ex) {
            log.debug("Nao foi possivel converter a data [{}] para Instant.", raw, ex);
            return null;
        }
    }

    private int normalizeLimit(int desired) {
        if (desired < MIN_ITEMS) {
            return MIN_ITEMS;
        }
        return Math.min(desired, MAX_ITEMS);
    }

    private int clampPageSize(int desired) {
        int minimum = Math.max(desired, MIN_ITEMS);
        return Math.min(minimum, 100);
    }

    private String sanitizeQuery(String query) {
        if (StringUtils.hasText(query)) {
            return query.trim();
        }
        return properties.getQuery();
    }

    private String buildKeywordQuery(String... keywords) {
        return Arrays.stream(keywords)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(keyword -> keyword.contains(" ") ? "\"" + keyword + "\"" : keyword)
                .collect(Collectors.joining(" OR "));
    }

    private record NewsApiResponse(String status, Integer totalResults, List<NewsApiArticle> articles) {
    }

    private record NewsApiArticle(
            NewsApiSource source,
            String author,
            String title,
            String description,
            String url,
            String urlToImage,
            String publishedAt,
            String content
    ) {
    }

    private record NewsApiSource(String id, String name) {
    }
}
