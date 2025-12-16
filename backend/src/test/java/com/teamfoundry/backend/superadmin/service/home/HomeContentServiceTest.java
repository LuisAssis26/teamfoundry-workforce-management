package com.teamfoundry.backend.superadmin.service.home;

import com.teamfoundry.backend.superadmin.dto.home.other.ReorderRequest;
import com.teamfoundry.backend.superadmin.dto.home.showcase.IndustryShowcaseRequest;
import com.teamfoundry.backend.superadmin.dto.other.WeeklyTipRequest;
import com.teamfoundry.backend.superadmin.enums.HomeLoginSectionType;
import com.teamfoundry.backend.superadmin.enums.SiteSectionType;
import com.teamfoundry.backend.superadmin.model.home.HomeLoginSection;
import com.teamfoundry.backend.superadmin.model.home.HomeNoLoginSection;
import com.teamfoundry.backend.superadmin.model.home.IndustryShowcase;
import com.teamfoundry.backend.superadmin.model.home.PartnerShowcase;
import com.teamfoundry.backend.superadmin.model.other.WeeklyTip;
import com.teamfoundry.backend.superadmin.repository.home.HomeLoginSectionRepository;
import com.teamfoundry.backend.superadmin.repository.home.HomeNoLoginSectionRepository;
import com.teamfoundry.backend.superadmin.repository.home.IndustryShowcaseRepository;
import com.teamfoundry.backend.superadmin.repository.home.PartnerShowcaseRepository;
import com.teamfoundry.backend.superadmin.repository.other.WeeklyTipRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class HomeContentServiceTest {

    @Mock HomeNoLoginSectionRepository sections;
    @Mock IndustryShowcaseRepository industries;
    @Mock PartnerShowcaseRepository partners;
    @Mock HomeLoginSectionRepository appHomeSections;
    @Mock WeeklyTipRepository weeklyTips;
    @Mock NewsApiService newsApiService;
    @Mock com.teamfoundry.backend.common.service.CloudinaryService cloudinaryService;

    @InjectMocks HomeContentService service;

    @AfterEach
    void resetMocks() {
        clearInvocations(cloudinaryService, industries, partners, weeklyTips, sections, appHomeSections);
    }

    @Test
    void updateIndustry_whenImageChanges_deletesOldImage() {
        IndustryShowcase entity = new IndustryShowcase();
        entity.setId(5L);
        entity.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1/old.png");

        when(industries.findById(5L)).thenReturn(Optional.of(entity));
        when(industries.save(any(IndustryShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IndustryShowcaseRequest request = new IndustryShowcaseRequest(
                "New",
                "Desc",
                "https://res.cloudinary.com/demo/image/upload/v1/new.png",
                "https://example.com",
                true
        );

        var response = service.updateIndustry(5L, request);

        verify(cloudinaryService).deleteByUrl("https://res.cloudinary.com/demo/image/upload/v1/old.png");
        assertThat(response.imageUrl()).isEqualTo(request.imageUrl());
    }

    @Test
    void createWeeklyTip_whenLimitReached_throwsBadRequest() {
        when(weeklyTips.count()).thenReturn(11L);

        WeeklyTipRequest request = new WeeklyTipRequest(
                "Cat", "Title", "Desc", LocalDate.now(), false, true
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createWeeklyTip(request));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(weeklyTips, never()).save(any());
    }

    @Test
    void reorderSections_withMissingIds_throwsBadRequest() {
        HomeNoLoginSection hero = section(1L, SiteSectionType.HERO, 0);
        HomeNoLoginSection partnersSection = section(2L, SiteSectionType.PARTNERS, 1);
        when(sections.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of(hero, partnersSection));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.reorderSections(List.of(1L)));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(sections, never()).saveAll(any());
    }

    @Test
    void markWeeklyTipFeatured_clearsOtherFeatured() {
        WeeklyTip alreadyFeatured = new WeeklyTip();
        alreadyFeatured.setId(1L);
        alreadyFeatured.setFeatured(true);

        WeeklyTip target = new WeeklyTip();
        target.setId(2L);
        target.setFeatured(false);

        when(weeklyTips.findById(2L)).thenReturn(Optional.of(target));
        when(weeklyTips.findAll()).thenReturn(List.of(alreadyFeatured, target));
        when(weeklyTips.save(any(WeeklyTip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.markWeeklyTipFeatured(2L);

        assertThat(response.featured()).isTrue();
        assertThat(alreadyFeatured.isFeatured()).isFalse();
        verify(weeklyTips).saveAll(any());
    }

    @Test
    void updateHomeLoginSection_updatesFields() {
        HomeLoginSection section = new HomeLoginSection();
        section.setId(10L);
        section.setType(HomeLoginSectionType.NEWS);
        section.setDisplayOrder(0);
        section.setActive(false);
        section.setApiEnabled(false);
        section.setProfileBarVisible(false);

        when(appHomeSections.findById(10L)).thenReturn(Optional.of(section));
        when(appHomeSections.save(any(HomeLoginSection.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(newsApiService.getEmpregabilidadeNews(anyInt())).thenReturn(List.of());

        var request = new com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn.HomeLoginSectionUpdateRequest(
                "Title", "Sub", "Content", "CTA", "https://cta",
                true, true, "https://api", "secret", 3,
                "Hello", true, "Current", "Offers"
        );

        var response = service.updateHomeLoginSection(10L, request);

        assertThat(response.active()).isTrue();
        assertThat(response.apiEnabled()).isTrue();
        assertThat(response.profileBarVisible()).isTrue();
        verify(appHomeSections).save(section);
    }

    private HomeNoLoginSection section(Long id, SiteSectionType type, int order) {
        HomeNoLoginSection s = new HomeNoLoginSection();
        s.setId(id);
        s.setType(type);
        s.setDisplayOrder(order);
        s.setTitle(type.name());
        return s;
    }
}
