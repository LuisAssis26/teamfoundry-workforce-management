package com.teamfoundry.backend.account.service.employee;

import com.teamfoundry.backend.account.dto.employee.documents.EmployeeCertificationRequest;
import com.teamfoundry.backend.account.dto.employee.documents.EmployeeCertificationResponse;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.model.employee.documents.EmployeeCertification;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.account.repository.employee.documents.EmployeeCertificationRepository;
import com.teamfoundry.backend.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeCertificationService {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final EmployeeCertificationRepository employeeCertificationRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<EmployeeCertificationResponse> list(String email) {
        EmployeeAccount account = resolveAccount(email);
        return employeeCertificationRepository.findByEmployeeOrderByCompletionDateDescIdDesc(account).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EmployeeCertificationResponse create(String email, EmployeeCertificationRequest request) {
        EmployeeAccount account = resolveAccount(email);
        ensureNotDuplicate(account, request, null);

        EmployeeCertification certification = new EmployeeCertification();
        certification.setEmployee(account);
        applyRequest(certification, request, true);

        EmployeeCertification saved = employeeCertificationRepository.save(certification);
        return toResponse(saved);
    }

    @Transactional
    public EmployeeCertificationResponse update(int certificationId,
                                                String email,
                                                EmployeeCertificationRequest request) {
        EmployeeAccount account = resolveAccount(email);
        EmployeeCertification certification = employeeCertificationRepository
                .findByIdAndEmployee(certificationId, account)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formação não encontrada."));

        ensureNotDuplicate(account, request, certificationId);
        applyRequest(certification, request, false);

        EmployeeCertification saved = employeeCertificationRepository.save(certification);
        return toResponse(saved);
    }

    @Transactional
    public void delete(int certificationId, String email) {
        EmployeeAccount account = resolveAccount(email);
        EmployeeCertification certification = employeeCertificationRepository
                .findByIdAndEmployee(certificationId, account)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formação não encontrada."));

        cloudinaryService.delete(certification.getCertificatePublicId());
        employeeCertificationRepository.delete(certification);
    }

    private void applyRequest(EmployeeCertification certification,
                              EmployeeCertificationRequest request,
                              boolean isCreate) {
        certification.setName(request.getName().trim());
        certification.setInstitution(request.getInstitution().trim());
        certification.setLocation(trimToNull(request.getLocation()));
        certification.setDescription(trimToNull(request.getDescription()));
        certification.setCompletionDate(request.getCompletionDate());

        if (StringUtils.hasText(request.getCertificateFile())) {
            String storedPublicId = storeCertificateFile(certification.getEmployee().getId(),
                    request.getCertificateFile(),
                    request.getCertificateFileName());
            cloudinaryService.delete(certification.getCertificatePublicId());
            certification.setCertificatePublicId(storedPublicId);
        } else if (isCreate && !StringUtils.hasText(certification.getCertificatePublicId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O certificado é obrigatório.");
        }
    }

    private EmployeeCertificationResponse toResponse(EmployeeCertification certification) {
        return EmployeeCertificationResponse.builder()
                .id(certification.getId())
                .name(certification.getName())
                .institution(certification.getInstitution())
                .location(certification.getLocation())
                .completionDate(certification.getCompletionDate())
                .description(certification.getDescription())
                .certificateUrl(buildCertificateUrl(certification.getCertificatePublicId()))
                .build();
    }

    private EmployeeAccount resolveAccount(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador não autenticado.");
        }
        return employeeAccountRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta não encontrada."));
    }

    private void ensureNotDuplicate(EmployeeAccount account,
                                    EmployeeCertificationRequest request,
                                    Integer currentId) {
        LocalDate completionDate = request.getCompletionDate();
        employeeCertificationRepository
                .findByEmployeeAndNameIgnoreCaseAndInstitutionIgnoreCaseAndCompletionDate(
                        account,
                        request.getName().trim(),
                        request.getInstitution().trim(),
                        completionDate
                )
                .filter(existing -> currentId == null || existing.getId() != currentId)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta formação já foi registada.");
                });
    }

    private String storeCertificateFile(Integer employeeId, String payload, String originalName) {
        try {
            String base64 = extractBase64Content(payload);
            byte[] data = Base64.getDecoder().decode(base64);
            CloudinaryService.UploadResult upload = cloudinaryService.uploadBytes(data, "certification", originalName);
            log.info("Stored certificate {} for employee {}", upload.getPublicId(), employeeId);
            return upload.getPublicId();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ficheiro do certificado é inválido.");
        } catch (Exception ex) {
            log.error("Erro a guardar certificado do funcionário {}", employeeId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível guardar o certificado.");
        }
    }

    private String extractBase64Content(String raw) {
        int commaIndex = raw.indexOf(',');
        return commaIndex >= 0 ? raw.substring(commaIndex + 1) : raw;
    }

    private String buildCertificateUrl(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            return null;
        }
        return cloudinaryService.buildUrl(publicId);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
