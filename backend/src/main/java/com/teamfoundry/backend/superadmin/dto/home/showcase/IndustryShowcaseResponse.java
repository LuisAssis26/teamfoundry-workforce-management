package com.teamfoundry.backend.superadmin.dto.home.showcase;

public record IndustryShowcaseResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        String linkUrl,
        boolean active,
        int displayOrder
) {}
