package com.teamfoundry.backend.teamRequests.dto.search;

import java.util.List;

public record AdminEmployeeSearchResponse(
        Integer id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String role,
        List<String> skills,
        List<String> areas,
        List<String> experiences,
        String profilePicturePublicId
) {}
