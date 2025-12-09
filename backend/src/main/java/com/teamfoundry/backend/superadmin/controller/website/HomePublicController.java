package com.teamfoundry.backend.superadmin.controller.website;

import com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn.HomeLoginConfigResponse;
import com.teamfoundry.backend.superadmin.dto.home.sections.noLogin.HomeNoLoginConfigResponse;
import com.teamfoundry.backend.superadmin.dto.other.WeeklyTipsPageResponse;
import com.teamfoundry.backend.superadmin.service.home.HomeContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
public class HomePublicController {

    private final HomeContentService service;

    @GetMapping("/homepage")
    public HomeNoLoginConfigResponse homepage() {
        return service.getPublicHomepage();
    }

    @GetMapping("/app-home")
    public HomeLoginConfigResponse appHome() {
        return service.getPublicHomeLogin();
    }

    @GetMapping("/weekly-tips")
    public WeeklyTipsPageResponse weeklyTips() {
        return service.getPublicWeeklyTips();
    }
}
