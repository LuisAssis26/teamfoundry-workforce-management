package com.teamfoundry.backend.site.dto;

import com.teamfoundry.backend.site.enums.HomeLoginSectionType;

import java.util.List;

public record HomeLoginSectionResponse(
        Long id,
        HomeLoginSectionType type,
        boolean active,
        int displayOrder,
        String title,
        String subtitle,
        String content,
        String primaryCtaLabel,
        String primaryCtaUrl,
        boolean apiEnabled,
        String apiUrl,
        Integer apiMaxItems,
        String apiToken,
        List<HomeNewsArticleResponse> newsArticles,
        String greetingPrefix,
        boolean profileBarVisible,
        String labelCurrentCompany,
        String labelOffers
) {
}
