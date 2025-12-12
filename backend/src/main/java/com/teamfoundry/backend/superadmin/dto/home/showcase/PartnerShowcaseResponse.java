package com.teamfoundry.backend.superadmin.dto.home.showcase;

public record PartnerShowcaseResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        String websiteUrl,
        boolean active,
        int displayOrder
) {}
