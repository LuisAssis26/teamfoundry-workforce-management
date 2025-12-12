package com.teamfoundry.backend.superadmin.dto.home.sections.noLogin;

import com.teamfoundry.backend.superadmin.dto.home.showcase.IndustryShowcaseResponse;
import com.teamfoundry.backend.superadmin.dto.home.showcase.PartnerShowcaseResponse;

import java.util.List;

public record HomeNoLoginConfigResponse(
        List<HomeNoLoginSectionResponse> sections,
        List<IndustryShowcaseResponse> industries,
        List<PartnerShowcaseResponse> partners
) {}
