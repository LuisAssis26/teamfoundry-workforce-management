package com.teamfoundry.backend.superadmin.config.home;

import com.teamfoundry.backend.superadmin.enums.SiteSectionType;
import com.teamfoundry.backend.superadmin.model.home.HomeNoLoginSection;
import com.teamfoundry.backend.superadmin.repository.home.HomeNoLoginSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HomeContentInitializer implements CommandLineRunner {

    private final HomeNoLoginSectionRepository sections;

    @Override
    public void run(String... args) {
        if (sections.count() > 0) {
            return;
        }

        List<HomeNoLoginSection> defaults = List.of(
                createSection(SiteSectionType.HERO, 0,
                        "TeamFoundry",
                        "Forjamos equipas, movemos a indústria.",
                        "Quero Trabalhar", "/login",
                        "Sou Empresa", "/company-register"),
                createSection(SiteSectionType.INDUSTRIES, 1,
                        "Indústrias em que atuamos",
                        "Mostre os segmentos estratégicos onde a TeamFoundry atua.",
                        null, null,
                        null, null),
                createSection(SiteSectionType.PARTNERS, 2,
                        "Parceiros principais",
                        "Destaque empresas que confiam na TeamFoundry.",
                        null, null,
                        null, null)
        );

        sections.saveAll(defaults);
    }

    private HomeNoLoginSection createSection(SiteSectionType type,
                                             int order,
                                             String title,
                                             String subtitle,
                                             String primaryLabel,
                                             String primaryUrl,
                                             String secondaryLabel,
                                             String secondaryUrl) {
        HomeNoLoginSection section = new HomeNoLoginSection();
        section.setType(type);
        section.setDisplayOrder(order);
        section.setTitle(title);
        section.setSubtitle(subtitle);
        section.setPrimaryCtaLabel(primaryLabel);
        section.setPrimaryCtaUrl(primaryUrl);
        section.setSecondaryCtaLabel(secondaryLabel);
        section.setSecondaryCtaUrl(secondaryUrl);
        section.setActive(true);
        return section;
    }
}
