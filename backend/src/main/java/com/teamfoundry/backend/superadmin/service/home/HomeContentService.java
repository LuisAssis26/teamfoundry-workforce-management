package com.teamfoundry.backend.superadmin.service.home;

import com.teamfoundry.backend.superadmin.dto.home.sections.LoggedIn.*;
import com.teamfoundry.backend.superadmin.dto.home.sections.noLogin.HomeNoLoginConfigResponse;
import com.teamfoundry.backend.superadmin.dto.home.sections.noLogin.HomeNoLoginSectionResponse;
import com.teamfoundry.backend.superadmin.dto.home.sections.noLogin.HomeNoLoginSectionUpdateRequest;
import com.teamfoundry.backend.superadmin.dto.home.showcase.IndustryShowcaseRequest;
import com.teamfoundry.backend.superadmin.dto.home.showcase.IndustryShowcaseResponse;
import com.teamfoundry.backend.superadmin.dto.home.showcase.PartnerShowcaseRequest;
import com.teamfoundry.backend.superadmin.dto.home.showcase.PartnerShowcaseResponse;
import com.teamfoundry.backend.superadmin.dto.other.WeeklyTipRequest;
import com.teamfoundry.backend.superadmin.dto.other.WeeklyTipResponse;
import com.teamfoundry.backend.superadmin.dto.other.WeeklyTipsPageResponse;
import com.teamfoundry.backend.superadmin.enums.HomeLoginSectionType;
import com.teamfoundry.backend.superadmin.model.home.*;
import com.teamfoundry.backend.superadmin.model.other.WeeklyTip;
import com.teamfoundry.backend.superadmin.repository.home.HomeLoginMetricRepository;
import com.teamfoundry.backend.superadmin.repository.home.HomeLoginSectionRepository;
import com.teamfoundry.backend.superadmin.repository.home.HomeNoLoginSectionRepository;
import com.teamfoundry.backend.superadmin.repository.home.IndustryShowcaseRepository;
import com.teamfoundry.backend.superadmin.repository.home.PartnerShowcaseRepository;
import com.teamfoundry.backend.superadmin.repository.other.WeeklyTipRepository;
import com.teamfoundry.backend.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HomeContentService {

    private final HomeNoLoginSectionRepository sections;
    private final IndustryShowcaseRepository industries;
    private final PartnerShowcaseRepository partners;
    private final HomeLoginSectionRepository appHomeSections;
    private final WeeklyTipRepository weeklyTips;
    private final NewsApiService newsApiService;
    private final CloudinaryService cloudinaryService;

    /*
     * PUBLIC QUERIES
     */
    @Transactional(readOnly = true)
    public HomeNoLoginConfigResponse getPublicHomepage() {
        return new HomeNoLoginConfigResponse(
                sections.findAllByOrderByDisplayOrderAsc().stream()
                        .filter(HomeNoLoginSection::isActive)
                        .map(this::mapSection)
                        .toList(),
                industries.findAllByOrderByDisplayOrderAsc().stream()
                        .filter(IndustryShowcase::isActive)
                        .map(this::mapIndustry)
                        .toList(),
                partners.findAllByOrderByDisplayOrderAsc().stream()
                        .filter(PartnerShowcase::isActive)
                        .map(this::mapPartner)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public HomeNoLoginConfigResponse getAdminHomepage() {
        return new HomeNoLoginConfigResponse(
                sections.findAllByOrderByDisplayOrderAsc().stream()
                        .map(this::mapSection)
                        .toList(),
                industries.findAllByOrderByDisplayOrderAsc().stream()
                        .map(this::mapIndustry)
                        .toList(),
                partners.findAllByOrderByDisplayOrderAsc().stream()
                        .map(this::mapPartner)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public HomeLoginConfigResponse getPublicHomeLogin() {
        return buildHomeLoginConfig(false, false);
    }

    @Transactional(readOnly = true)
    public HomeLoginConfigResponse getAdminHomeLogin() {
        return buildHomeLoginConfig(true, true);
    }

    @Transactional(readOnly = true)
    public WeeklyTipsPageResponse getPublicWeeklyTips() {
        List<WeeklyTip> activeTips = weeklyTips.findAllByOrderByDisplayOrderAsc().stream()
                .filter(WeeklyTip::isActive)
                .toList();

        // Limit the pool to at most 11 tips (oldest first by display order)
        List<WeeklyTip> rotationPool = activeTips.stream()
                .sorted(Comparator.comparingInt(WeeklyTip::getDisplayOrder))
                .limit(11)
                .toList();

        WeeklyTipResponse highlighted = null;
        // 1) If admin marked any tip as featured, use that as tip of the week
        Optional<WeeklyTip> featured = rotationPool.stream()
                .filter(WeeklyTip::isFeatured)
                .findFirst();

        if (featured.isPresent()) {
            highlighted = mapWeeklyTip(featured.get());
        } else if (!rotationPool.isEmpty()) {
            // 2) Otherwise, rotate automatically by week of year
            LocalDate today = LocalDate.now();
            int weekOfYear = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int index = (weekOfYear - 1) % rotationPool.size();
            WeeklyTip tipOfWeek = rotationPool.get(index);
            highlighted = mapWeeklyTip(tipOfWeek);
        }

        List<WeeklyTipResponse> all = rotationPool.stream()
                .map(this::mapWeeklyTip)
                .toList();

        return new WeeklyTipsPageResponse(highlighted, all);
    }

    /*
     * SECTIONS
     */
    public HomeNoLoginSectionResponse updateSection(Long id, HomeNoLoginSectionUpdateRequest request) {
        HomeNoLoginSection section = sections.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        section.setTitle(request.title());
        section.setSubtitle(request.subtitle());
        section.setPrimaryCtaLabel(request.primaryCtaLabel());
        section.setPrimaryCtaUrl(request.primaryCtaUrl());
        section.setSecondaryCtaLabel(request.secondaryCtaLabel());
        section.setSecondaryCtaUrl(request.secondaryCtaUrl());
        section.setActive(Boolean.TRUE.equals(request.active()));

        sections.save(section);
        return mapSection(section);
    }

    public List<HomeNoLoginSectionResponse> reorderSections(List<Long> ids) {
        List<HomeNoLoginSection> current = sections.findAllByOrderByDisplayOrderAsc();
        ensureSameElements(ids, current, HomeNoLoginSection::getId, "sections");

        applyNewOrder(ids, current, HomeNoLoginSection::getId, (item, order) -> item.setDisplayOrder(order));
        sections.saveAll(current);

        return current.stream()
                .sorted(Comparator.comparingInt(HomeNoLoginSection::getDisplayOrder))
                .map(this::mapSection)
                .toList();
    }

    /*
     * INDUSTRIES
     */
    @Transactional(readOnly = true)
    public List<IndustryShowcaseResponse> listIndustries() {
        return industries.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapIndustry)
                .toList();
    }

    public IndustryShowcaseResponse createIndustry(IndustryShowcaseRequest request) {
        IndustryShowcase entity = new IndustryShowcase();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());
        entity.setLinkUrl(request.linkUrl());
        entity.setActive(Boolean.TRUE.equals(request.active()));
        entity.setDisplayOrder(nextIndustryOrder());

        industries.save(entity);
        return mapIndustry(entity);
    }

    public IndustryShowcaseResponse updateIndustry(Long id, IndustryShowcaseRequest request) {
        IndustryShowcase entity = industries.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Industry not found"));

        String previousImageUrl = entity.getImageUrl();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());
        entity.setLinkUrl(request.linkUrl());
        entity.setActive(Boolean.TRUE.equals(request.active()));
        if (previousImageUrl != null && !previousImageUrl.equals(request.imageUrl())) {
            cloudinaryService.deleteByUrl(previousImageUrl);
        }

        return mapIndustry(industries.save(entity));
    }

    public IndustryShowcaseResponse toggleIndustry(Long id, boolean active) {
        IndustryShowcase entity = industries.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Industry not found"));
        entity.setActive(active);
        return mapIndustry(industries.save(entity));
    }

    public List<IndustryShowcaseResponse> reorderIndustries(List<Long> ids) {
        List<IndustryShowcase> current = industries.findAllByOrderByDisplayOrderAsc();
        ensureSameElements(ids, current, IndustryShowcase::getId, "industries");

        applyNewOrder(ids, current, IndustryShowcase::getId, (item, order) -> item.setDisplayOrder(order));
        industries.saveAll(current);

        return current.stream()
                .sorted(Comparator.comparingInt(IndustryShowcase::getDisplayOrder))
                .map(this::mapIndustry)
                .toList();
    }

    public void deleteIndustry(Long id) {
        IndustryShowcase entity = industries.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Industry not found"));
        cloudinaryService.deleteByUrl(entity.getImageUrl());
        industries.delete(entity);
    }

    /*
     * PARTNERS
     */
    @Transactional(readOnly = true)
    public List<PartnerShowcaseResponse> listPartners() {
        return partners.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapPartner)
                .toList();
    }

    public PartnerShowcaseResponse createPartner(PartnerShowcaseRequest request) {
        PartnerShowcase entity = new PartnerShowcase();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());
        entity.setWebsiteUrl(request.websiteUrl());
        entity.setActive(Boolean.TRUE.equals(request.active()));
        entity.setDisplayOrder(nextPartnerOrder());

        partners.save(entity);
        return mapPartner(entity);
    }

    public PartnerShowcaseResponse updatePartner(Long id, PartnerShowcaseRequest request) {
        PartnerShowcase entity = partners.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found"));

        String previousImageUrl = entity.getImageUrl();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());
        entity.setWebsiteUrl(request.websiteUrl());
        entity.setActive(Boolean.TRUE.equals(request.active()));
        if (previousImageUrl != null && !previousImageUrl.equals(request.imageUrl())) {
            cloudinaryService.deleteByUrl(previousImageUrl);
        }

        return mapPartner(partners.save(entity));
    }

    public PartnerShowcaseResponse togglePartner(Long id, boolean active) {
        PartnerShowcase entity = partners.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found"));
        entity.setActive(active);
        return mapPartner(partners.save(entity));
    }

    public List<PartnerShowcaseResponse> reorderPartners(List<Long> ids) {
        List<PartnerShowcase> current = partners.findAllByOrderByDisplayOrderAsc();
        ensureSameElements(ids, current, PartnerShowcase::getId, "partners");

        applyNewOrder(ids, current, PartnerShowcase::getId, (item, order) -> item.setDisplayOrder(order));
        partners.saveAll(current);

        return current.stream()
                .sorted(Comparator.comparingInt(PartnerShowcase::getDisplayOrder))
                .map(this::mapPartner)
                .toList();
    }

    public void deletePartner(Long id) {
        PartnerShowcase entity = partners.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found"));
        cloudinaryService.deleteByUrl(entity.getImageUrl());
        partners.delete(entity);
    }

    /*
     * AUTHENTICATED HOME - SECTIONS
     */
    public HomeLoginSectionResponse updateHomeLoginSection(Long id, HomeLoginSectionUpdateRequest request) {
        HomeLoginSection section = appHomeSections.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App home section not found"));

        section.setTitle(request.title());
        section.setSubtitle(request.subtitle());
        section.setContent(request.content());
        section.setPrimaryCtaLabel(request.primaryCtaLabel());
        section.setPrimaryCtaUrl(request.primaryCtaUrl());
        if (request.active() != null) {
            section.setActive(request.active());
        }
        if (request.apiEnabled() != null) {
            section.setApiEnabled(request.apiEnabled());
        }
        section.setApiUrl(request.apiUrl());
        section.setApiToken(request.apiToken());
        section.setApiMaxItems(request.apiMaxItems());
        section.setGreetingPrefix(request.greetingPrefix());
        if (request.profileBarVisible() != null) {
            section.setProfileBarVisible(request.profileBarVisible());
        }
        section.setLabelCurrentCompany(request.labelCurrentCompany());
        section.setLabelOffers(request.labelOffers());

        return mapHomeLoginSection(appHomeSections.save(section), true);
    }

    public List<HomeLoginSectionResponse> reorderHomeLoginSections(List<Long> ids) {
        List<HomeLoginSection> current = appHomeSections.findAllByOrderByDisplayOrderAsc();
        ensureSameElements(ids, current, HomeLoginSection::getId, "app-home sections");

        applyNewOrder(ids, current, HomeLoginSection::getId, (item, order) -> item.setDisplayOrder(order));
        appHomeSections.saveAll(current);

        return current.stream()
                .sorted(Comparator.comparingInt(HomeLoginSection::getDisplayOrder))
                .map(section -> mapHomeLoginSection(section, true))
                .toList();
    }

    /*
     * WEEKLY TIPS
     */
    @Transactional(readOnly = true)
    public List<WeeklyTipResponse> listWeeklyTips() {
        return weeklyTips.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapWeeklyTip)
                .toList();
    }

    public WeeklyTipResponse createWeeklyTip(WeeklyTipRequest request) {
        // Limit total number of tips to 11
        long totalTips = weeklyTips.count();
        if (totalTips >= 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ja existem 11 dicas configuradas. Edite ou remova uma dica antes de criar outra.");
        }

        WeeklyTip tip = new WeeklyTip();
        tip.setCategory(request.category());
        tip.setTitle(request.title());
        tip.setDescription(request.description());
        tip.setPublishedAt(request.publishedAt());
        tip.setActive(Boolean.TRUE.equals(request.active()));
        tip.setFeatured(Boolean.TRUE.equals(request.featured()));
        tip.setDisplayOrder(nextWeeklyTipOrder());

        if (tip.isFeatured()) {
            clearOtherFeaturedTips(null);
        }

        return mapWeeklyTip(weeklyTips.save(tip));
    }

    public WeeklyTipResponse updateWeeklyTip(Long id, WeeklyTipRequest request) {
        WeeklyTip tip = weeklyTips.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weekly tip not found"));

        tip.setCategory(request.category());
        tip.setTitle(request.title());
        tip.setDescription(request.description());
        tip.setPublishedAt(request.publishedAt());
        if (request.active() != null) {
            tip.setActive(request.active());
        }
        if (request.featured() != null) {
            tip.setFeatured(request.featured());
        }

        if (tip.isFeatured()) {
            clearOtherFeaturedTips(tip.getId());
        }

        return mapWeeklyTip(weeklyTips.save(tip));
    }

    public WeeklyTipResponse toggleWeeklyTipVisibility(Long id, boolean active) {
        WeeklyTip tip = weeklyTips.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weekly tip not found"));
        tip.setActive(active);
        return mapWeeklyTip(weeklyTips.save(tip));
    }

    public WeeklyTipResponse markWeeklyTipFeatured(Long id) {
        WeeklyTip tip = weeklyTips.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weekly tip not found"));

        clearOtherFeaturedTips(id);
        tip.setFeatured(true);

        return mapWeeklyTip(weeklyTips.save(tip));
    }

    public List<WeeklyTipResponse> reorderWeeklyTips(List<Long> ids) {
        List<WeeklyTip> current = weeklyTips.findAllByOrderByDisplayOrderAsc();
        ensureSameElements(ids, current, WeeklyTip::getId, "weekly tips");

        applyNewOrder(ids, current, WeeklyTip::getId, (item, order) -> item.setDisplayOrder(order));
        weeklyTips.saveAll(current);

        return current.stream()
                .sorted(Comparator.comparingInt(WeeklyTip::getDisplayOrder))
                .map(this::mapWeeklyTip)
                .toList();
    }

    public void deleteWeeklyTip(Long id) {
        WeeklyTip tip = weeklyTips.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weekly tip not found"));
        weeklyTips.delete(tip);
    }

    /*
     * HELPERS
     */
    private HomeNoLoginSectionResponse mapSection(HomeNoLoginSection section) {
        return new HomeNoLoginSectionResponse(
                section.getId(),
                section.getType(),
                section.isActive(),
                section.getDisplayOrder(),
                section.getTitle(),
                section.getSubtitle(),
                section.getPrimaryCtaLabel(),
                section.getPrimaryCtaUrl(),
                section.getSecondaryCtaLabel(),
                section.getSecondaryCtaUrl()
        );
    }

    private IndustryShowcaseResponse mapIndustry(IndustryShowcase entity) {
        return new IndustryShowcaseResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getLinkUrl(),
                entity.isActive(),
                entity.getDisplayOrder()
        );
    }

    private PartnerShowcaseResponse mapPartner(PartnerShowcase entity) {
        return new PartnerShowcaseResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getWebsiteUrl(),
                entity.isActive(),
                entity.getDisplayOrder()
        );
    }

    private HomeLoginConfigResponse buildHomeLoginConfig(boolean includeInactive, boolean includeSecrets) {
        var sectionStream = appHomeSections.findAllByOrderByDisplayOrderAsc().stream();
        if (!includeInactive) {
            sectionStream = sectionStream.filter(HomeLoginSection::isActive);
        }
        List<HomeLoginSectionResponse> homeSections = sectionStream
                .map(section -> mapHomeLoginSection(section, includeSecrets))
                .toList();
        return new HomeLoginConfigResponse(homeSections);
    }

    private HomeLoginSectionResponse mapHomeLoginSection(HomeLoginSection section, boolean includeSecret) {
        List<HomeNewsArticleResponse> articles = Collections.emptyList();
        if (section.getType() == HomeLoginSectionType.NEWS) {
            int limit = Optional.ofNullable(section.getApiMaxItems()).orElse(6);
            articles = newsApiService.getEmpregabilidadeNews(limit);
        }
        return new HomeLoginSectionResponse(
                section.getId(),
                section.getType(),
                section.isActive(),
                section.getDisplayOrder(),
                section.getTitle(),
                section.getSubtitle(),
                section.getContent(),
                section.getPrimaryCtaLabel(),
                section.getPrimaryCtaUrl(),
                section.isApiEnabled(),
                section.getApiUrl(),
                section.getApiMaxItems(),
                includeSecret ? section.getApiToken() : null,
                articles,
                section.getGreetingPrefix(),
                section.isProfileBarVisible(),
                section.getLabelCurrentCompany(),
                section.getLabelOffers()
        );
    }

    private <T> void ensureSameElements(List<Long> ids,
                                    List<T> current,
                                    Function<T, Long> idExtractor,
                                    String target) {
        Set<Long> payload = new LinkedHashSet<>(ids);
        Set<Long> existing = current.stream()
                .map(item -> idExtractor.apply(item))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!payload.equals(existing) || payload.size() != current.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid identifiers supplied for " + target);
        }
    }

    private <T> void applyNewOrder(List<Long> ids,
                                   List<T> items,
                                   Function<T, Long> idExtractor,
                                   OrderUpdater<T> orderUpdater) {
        Map<Long, T> map = items.stream()
                .collect(Collectors.toMap(idExtractor, Function.identity()));
        for (int i = 0; i < ids.size(); i++) {
            T item = map.get(ids.get(i));
            if (item == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown identifier: " + ids.get(i));
            }
            orderUpdater.apply(item, i);
        }
    }

    private int nextIndustryOrder() {
        return industries.findAll().stream()
                .mapToInt(IndustryShowcase::getDisplayOrder)
                .max()
                .orElse(-1) + 1;
    }

    private int nextPartnerOrder() {
        return partners.findAll().stream()
                .mapToInt(PartnerShowcase::getDisplayOrder)
                .max()
                .orElse(-1) + 1;
    }

    private int nextWeeklyTipOrder() {
        return weeklyTips.findAll().stream()
                .mapToInt(WeeklyTip::getDisplayOrder)
                .max()
                .orElse(-1) + 1;
    }

    private WeeklyTipResponse mapWeeklyTip(WeeklyTip tip) {
        return new WeeklyTipResponse(
                tip.getId(),
                tip.getCategory(),
                tip.getTitle(),
                tip.getDescription(),
                tip.getPublishedAt(),
                tip.isFeatured(),
                tip.isActive(),
                tip.getDisplayOrder()
        );
    }

    private void clearOtherFeaturedTips(Long keepId) {
        List<WeeklyTip> featured = weeklyTips.findAll().stream()
                .filter(WeeklyTip::isFeatured)
                .filter(tip -> keepId == null || !tip.getId().equals(keepId))
                .toList();
        if (featured.isEmpty()) {
            return;
        }
        featured.forEach(tip -> tip.setFeatured(false));
        weeklyTips.saveAll(featured);
    }

    @FunctionalInterface
    private interface OrderUpdater<T> {
        void apply(T item, int order);
    }
}
