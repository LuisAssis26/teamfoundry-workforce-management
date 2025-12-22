package com.teamfoundry.backend.teamRequests.service;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.teamRequests.dto.search.AdminEmployeeSearchResponse;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeSkill;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeGeoArea;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeRoleRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Busca candidatos filtrando por função, áreas e competências.
 * Experiências listam apenas trabalhos concluídos (máx. 2).
 */
@Service
@Transactional(readOnly = true)
public class AdminEmployeeSearchService {

    private static final String ADMIN_TOKEN_PREFIX = "admin:";

    private final AdminAccountRepository adminAccountRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeGeoAreaRepository employeeGeoAreaRepository;
    private final EmployeeRequestRepository employeeRequestRepository;

    public AdminEmployeeSearchService(AdminAccountRepository adminAccountRepository,
                                      EmployeeAccountRepository employeeAccountRepository,
                                      EmployeeRoleRepository employeeRoleRepository,
                                      EmployeeSkillRepository employeeSkillRepository,
                                      EmployeeGeoAreaRepository employeeGeoAreaRepository,
                                      EmployeeRequestRepository employeeRequestRepository) {
        this.adminAccountRepository = adminAccountRepository;
        this.employeeAccountRepository = employeeAccountRepository;
        this.employeeRoleRepository = employeeRoleRepository;
        this.employeeSkillRepository = employeeSkillRepository;
        this.employeeGeoAreaRepository = employeeGeoAreaRepository;
        this.employeeRequestRepository = employeeRequestRepository;
    }

    public List<AdminEmployeeSearchResponse> search(String role,
                                                    List<String> areas,
                                                    List<String> skills,
                                                    List<String> preferredRoles,
                                                    List<String> statuses,
                                                    Integer teamId) {

        
        
        
        resolveAuthenticatedAdmin(); // garante que é admin
        List<String> normAreas = normalizeList(areas);
        List<String> normSkills = normalizeList(skills);
        List<String> normRoles = normalizeList(preferredRoles);
        List<String> normStatuses = normalizeList(statuses);
        
        String normVacancyRole = null;
        if (StringUtils.hasText(role)) {
            String normRole = role.trim().toLowerCase(Locale.ROOT);
            normVacancyRole = normRole; 
        }


        List<String> validStatuses = statuses != null ? statuses.stream().filter(StringUtils::hasText).toList() : List.of();

        List<EmployeeAccount> results = employeeAccountRepository.searchCandidates(
                normAreas,
                normAreas.isEmpty(),
                normSkills,
                normSkills.isEmpty(),
                normRoles,
                normRoles.isEmpty(),
                validStatuses,
                validStatuses.isEmpty(),
                teamId,
                normVacancyRole
        );
        
        

        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    private AdminEmployeeSearchResponse toResponse(EmployeeAccount employee) {
        String role = employeeRoleRepository.findFirstByEmployee(employee)
                .map(rel -> rel.getFunction().getName())
                .orElse(null);

        List<String> skills = employeeSkillRepository.findByEmployee(employee).stream()
                .map(EmployeeSkill::getPrefSkill)
                .filter(comp -> comp != null && StringUtils.hasText(comp.getName()))
                .map(comp -> comp.getName())
                .toList();

        List<String> areas = employeeGeoAreaRepository.findByEmployee(employee).stream()
                .map(EmployeeGeoArea::getGeoArea)
                .filter(area -> area != null && StringUtils.hasText(area.getName()))
                .map(area -> area.getName())
                .toList();

        List<String> experiences = employeeRequestRepository
                .findByEmployee_EmailOrderByAcceptedDateDesc(employee.getEmail().toLowerCase())
                .stream()
                .filter(req -> req.getAcceptedDate() != null && isConcluded(req.getTeamRequest()))
                .limit(2)
                .map(req -> {
                    String company = (req.getTeamRequest() != null && req.getTeamRequest().getCompany() != null)
                            ? req.getTeamRequest().getCompany().getName()
                            : "Empresa";
                    String job = req.getRequestedRole() != null ? req.getRequestedRole() : "Função";
                    String date = req.getAcceptedDate().toLocalDate().toString();
                    return company + " - " + job + " (" + date + ")";
                })
                .toList();

        return new AdminEmployeeSearchResponse(
                employee.getId(),
                employee.getName(),
                employee.getSurname(),
                employee.getEmail(),
                employee.getPhone(),
                role,
                skills,
                areas,
                experiences,
                employee.getProfilePicturePublicId()
        );
    }

    private boolean isConcluded(TeamRequest tr) {
        if (tr == null) return false;
        if (tr.getState() == State.COMPLETED) return true;
        LocalDateTime end = tr.getEndDate();
        return end != null && end.isBefore(LocalDateTime.now());
    }

    private AdminAccount resolveAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação requerida.");
        }
        String principal = authentication.getName();
        if (principal == null || !principal.startsWith(ADMIN_TOKEN_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente administradores autenticados.");
        }
        String username = principal.substring(ADMIN_TOKEN_PREFIX.length());
        return adminAccountRepository.findByUsernameIgnoreCase(username)
                .filter(admin -> admin.getRole() == UserType.ADMIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não encontrado."));
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null) return List.of();
        return values.stream()
                .filter(StringUtils::hasText)
                .map(v -> v.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}
