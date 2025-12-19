package com.teamfoundry.backend.teamRequests.service;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeSkill;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeRole;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeGeoArea;
import com.teamfoundry.backend.account.model.employee.documents.EmployeeDocument;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeRoleRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.account.repository.employee.documents.EmployeeDocumentRepository;
import com.teamfoundry.backend.account.repository.employee.documents.EmployeeCertificationRepository;
import com.teamfoundry.backend.teamRequests.dto.search.AdminEmployeeProfileResponse;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.model.EmployeeRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.account.enums.DocumentType;
import com.teamfoundry.backend.account.dto.employee.documents.EmployeeCertificationResponse;
import com.teamfoundry.backend.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Perfil do colaborador (consulta por admin), incluindo experiências concluídas (máx. 2).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEmployeeProfileService {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeGeoAreaRepository employeeGeoAreaRepository;
    private final EmployeeRequestRepository employeeRequestRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final EmployeeCertificationRepository employeeCertificationRepository;
    private final CloudinaryService cloudinaryService;

    public AdminEmployeeProfileResponse getProfile(Integer employeeId) {
        EmployeeAccount employee = employeeAccountRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado."));

        String preferredRole = employeeRoleRepository.findFirstByEmployee(employee)
                .map(EmployeeRole::getFunction)
                .filter(f -> f != null && StringUtils.hasText(f.getName()))
                .map(f -> f.getName())
                .orElse(null);

        List<String> skills = employeeSkillRepository.findByEmployee(employee).stream()
                .map(EmployeeSkill::getPrefSkill)
                .filter(c -> c != null && StringUtils.hasText(c.getName()))
                .map(c -> c.getName())
                .toList();

        List<String> areas = employeeGeoAreaRepository.findByEmployee(employee).stream()
                .map(EmployeeGeoArea::getGeoArea)
                .filter(a -> a != null && StringUtils.hasText(a.getName()))
                .map(a -> a.getName())
                .toList();

        List<String> experiences =
                employeeRequestRepository.findByEmployee_IdOrderByAcceptedDateDesc(employeeId).stream()
                        .filter(req -> req.getAcceptedDate() != null && isConcluded(req))
                        .limit(2)
                        .map(this::toExperienceLabel)
                        .toList();

        EmployeeDocument cv = getDocument(employee, DocumentType.CURRICULUM);
        EmployeeDocument idFront = getDocument(employee, DocumentType.IDENTIFICATION_FRONT);
        EmployeeDocument idBack = getDocument(employee, DocumentType.IDENTIFICATION_BACK);

        List<EmployeeCertificationResponse> certs = employeeCertificationRepository.findByEmployeeOrderByCompletionDateDescIdDesc(employee).stream()
                .map(cert -> EmployeeCertificationResponse.builder()
                        .id(cert.getId())
                        .name(cert.getName())
                        .institution(cert.getInstitution())
                        .location(cert.getLocation())
                        .completionDate(cert.getCompletionDate())
                        .description(cert.getDescription())
                        .certificateUrl(buildUrl(cert.getCertificatePublicId()))
                        .build())
                .toList();

        return new AdminEmployeeProfileResponse(
                employee.getId(),
                employee.getName(),
                employee.getSurname(),
                employee.getGender(),
                employee.getBirthDate(),
                employee.getNationality(),
                employee.getPhone(),
                employee.getEmail(),
                preferredRole,
                skills,
                areas,
                experiences,
                buildUrl(employee.getProfilePicturePublicId()),
                buildUrl(docPublicId(cv)),
                docFileName(cv),
                buildUrl(docPublicId(idFront)),
                docFileName(idFront),
                buildUrl(docPublicId(idBack)),
                docFileName(idBack),
                certs
        );
    }

    private String toExperienceLabel(EmployeeRequest req) {
        String company = (req.getTeamRequest() != null && req.getTeamRequest().getCompany() != null)
                ? req.getTeamRequest().getCompany().getName()
                : "Empresa";
        String job = req.getRequestedRole() != null ? req.getRequestedRole() : "Função";
        String date = req.getAcceptedDate() != null ? req.getAcceptedDate().toLocalDate().toString() : "";
        return company + " - " + job + (date.isEmpty() ? "" : " (" + date + ")");
    }

    private boolean isConcluded(EmployeeRequest req) {
        if (req.getTeamRequest() == null) return false;
        var tr = req.getTeamRequest();
        if (tr.getState() == State.COMPLETE) return true;
        LocalDateTime end = tr.getEndDate();
        return end != null && end.isBefore(LocalDateTime.now());
    }

    private EmployeeDocument getDocument(EmployeeAccount employee, DocumentType type) {
        return employeeDocumentRepository.findByEmployeeAndType(employee, type).orElse(null);
    }

    private String docPublicId(EmployeeDocument doc) {
        return doc != null ? doc.getPublicId() : null;
    }

    private String docFileName(EmployeeDocument doc) {
        return doc != null ? doc.getFileName() : null;
    }

    private String buildUrl(String publicId) {
        if (!StringUtils.hasText(publicId)) return null;
        return cloudinaryService.buildUrl(publicId);
    }
}
