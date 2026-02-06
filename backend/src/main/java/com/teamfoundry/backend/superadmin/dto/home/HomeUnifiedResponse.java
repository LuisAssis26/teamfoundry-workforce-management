package com.teamfoundry.backend.superadmin.dto.home;

import com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn.HomeLoginSectionResponse;
import com.teamfoundry.backend.superadmin.dto.home.sections.noLogin.HomeNoLoginSectionResponse;
import com.teamfoundry.backend.superadmin.dto.home.showcase.IndustryShowcaseResponse;
import com.teamfoundry.backend.superadmin.dto.home.showcase.PartnerShowcaseResponse;
import com.teamfoundry.backend.superadmin.dto.other.WeeklyTipsPageResponse;
import java.util.List;

public record HomeUnifiedResponse(
        List<HomeNoLoginSectionResponse> publicSections,
        List<HomeLoginSectionResponse> authenticatedSections,
        List<IndustryShowcaseResponse> industries,
        List<PartnerShowcaseResponse> partners,
        WeeklyTipsPageResponse weeklyTips
) {
}
