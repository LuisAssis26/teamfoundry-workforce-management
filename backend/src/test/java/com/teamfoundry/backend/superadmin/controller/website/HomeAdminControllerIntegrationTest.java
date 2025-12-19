package com.teamfoundry.backend.superadmin.controller.website;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.model.home.HomeNoLoginSection;
import com.teamfoundry.backend.superadmin.model.home.IndustryShowcase;
import com.teamfoundry.backend.superadmin.model.other.WeeklyTip;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.superadmin.repository.home.HomeLoginSectionRepository;
import com.teamfoundry.backend.superadmin.repository.home.HomeNoLoginSectionRepository;
import com.teamfoundry.backend.superadmin.repository.home.IndustryShowcaseRepository;
import com.teamfoundry.backend.superadmin.repository.home.PartnerShowcaseRepository;
import com.teamfoundry.backend.superadmin.repository.other.WeeklyTipRepository;
import com.teamfoundry.backend.superadmin.enums.SiteSectionType;
import com.teamfoundry.backend.superadmin.dto.home.showcase.IndustryShowcaseRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("HomeAdminController integration")
@Transactional
class HomeAdminControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AdminAccountRepository adminAccountRepository;
    @Autowired HomeNoLoginSectionRepository homeNoLoginSectionRepository;
    @Autowired IndustryShowcaseRepository industryShowcaseRepository;
    @Autowired PartnerShowcaseRepository partnerShowcaseRepository;
    @Autowired HomeLoginSectionRepository homeLoginSectionRepository;
    @Autowired WeeklyTipRepository weeklyTipRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean com.teamfoundry.backend.common.service.CloudinaryService cloudinaryService;
    @MockBean com.teamfoundry.backend.superadmin.service.home.NewsApiService newsApiService;

    private final String superUsername = "superadmin";
    private final String superPassword = "Super#Site123";

    @BeforeEach
    void setup() {
        weeklyTipRepository.deleteAll();
        homeLoginSectionRepository.deleteAll();
        partnerShowcaseRepository.deleteAll();
        industryShowcaseRepository.deleteAll();
        homeNoLoginSectionRepository.deleteAll();
        adminAccountRepository.deleteAll();

        adminAccountRepository.save(new AdminAccount(0, superUsername,
                passwordEncoder.encode(superPassword), UserType.SUPERADMIN, false));
    }

    @Test
    @DisplayName("GET /site/homepage devolve todas as secoes para superadmin")
    void homepageReturnsAllSections() throws Exception {
        var existing = homeNoLoginSectionRepository.findAll();
        if (existing.isEmpty()) {
            HomeNoLoginSection hero = new HomeNoLoginSection();
            hero.setType(SiteSectionType.HERO);
            hero.setTitle("Hero title");
            hero.setDisplayOrder(0);
            hero.setActive(true);

            HomeNoLoginSection partners = new HomeNoLoginSection();
            partners.setType(SiteSectionType.PARTNERS);
            partners.setTitle("Partners block");
            partners.setDisplayOrder(1);
            partners.setActive(false);

            homeNoLoginSectionRepository.save(hero);
            homeNoLoginSectionRepository.save(partners);
        } else {
            existing.get(0).setTitle("Hero title");
            existing.get(0).setActive(true);
            if (existing.size() > 1) {
                existing.get(1).setTitle("Partners block");
                existing.get(1).setActive(false);
            }
            homeNoLoginSectionRepository.saveAll(existing);
        }

        String token = login(superUsername, superPassword);

        mockMvc.perform(get("/api/super-admin/site/homepage")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections", hasSize(2)))
                .andExpect(jsonPath("$.sections[0].title").value("Hero title"))
                .andExpect(jsonPath("$.sections[1].title").value("Partners block"))
                .andExpect(jsonPath("$.sections[1].active").value(false));
    }

    @Test
    @DisplayName("PUT /site/industries/{id} apaga imagem anterior e atualiza")
    void updateIndustryReplacesImageAndDeletesOld() throws Exception {
        IndustryShowcase industry = new IndustryShowcase();
        industry.setName("Health");
        industry.setDescription("Desc");
        industry.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1/old.png");
        industry.setLinkUrl("https://old.example.com");
        industry.setDisplayOrder(0);
        industry.setActive(true);
        industry = industryShowcaseRepository.save(industry);

        String token = login(superUsername, superPassword);

        IndustryShowcaseRequest payload = new IndustryShowcaseRequest(
                "Healthcare",
                "Updated description",
                "https://res.cloudinary.com/demo/image/upload/v1/new.png",
                "https://new.example.com",
                true
        );

        mockMvc.perform(put("/api/super-admin/site/industries/{id}", industry.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Healthcare"))
                .andExpect(jsonPath("$.imageUrl").value(payload.imageUrl()));

        Mockito.verify(cloudinaryService).deleteByUrl("https://res.cloudinary.com/demo/image/upload/v1/old.png");
    }

    @Test
    @DisplayName("POST /site/weekly-tips limita a 11 registos")
    void createWeeklyTipWhenLimitReachedReturnsBadRequest() throws Exception {
        for (int i = 0; i < 11; i++) {
            WeeklyTip tip = new WeeklyTip();
            tip.setCategory("Cat" + i);
            tip.setTitle("Tip " + i);
            tip.setDescription("Desc " + i);
            tip.setPublishedAt(LocalDate.now());
            tip.setDisplayOrder(i);
            weeklyTipRepository.save(tip);
        }

        String token = login(superUsername, superPassword);

        var body = objectMapper.writeValueAsString(Map.of(
                "category", "Cat 12",
                "title", "Too many",
                "description", "Limit reached",
                "publishedAt", LocalDate.now().toString(),
                "featured", false,
                "active", true
        ));

        mockMvc.perform(post("/api/super-admin/site/weekly-tips")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /site/weekly-tips/reorder devolve 400 se ids nao corresponderem")
    void reorderWeeklyTipsWithMissingIdsReturnsBadRequest() throws Exception {
        WeeklyTip tip1 = new WeeklyTip();
        tip1.setCategory("A");
        tip1.setTitle("One");
        tip1.setDescription("Desc");
        tip1.setPublishedAt(LocalDate.now());
        tip1.setDisplayOrder(0);
        weeklyTipRepository.save(tip1);

        WeeklyTip tip2 = new WeeklyTip();
        tip2.setCategory("B");
        tip2.setTitle("Two");
        tip2.setDescription("Desc");
        tip2.setPublishedAt(LocalDate.now());
        tip2.setDisplayOrder(1);
        weeklyTipRepository.save(tip2);

        String token = login(superUsername, superPassword);

        var body = objectMapper.writeValueAsString(Map.of(
                "ids", java.util.List.of(tip1.getId())
        ));

        mockMvc.perform(put("/api/super-admin/site/weekly-tips/reorder")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private String login(String username, String password) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        var response = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }
}

