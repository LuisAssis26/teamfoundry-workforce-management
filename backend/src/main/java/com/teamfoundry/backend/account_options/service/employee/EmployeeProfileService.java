package com.teamfoundry.backend.account_options.service.employee;

import com.teamfoundry.backend.account_options.dto.employee.CurriculumUploadRequest;
import com.teamfoundry.backend.account_options.dto.employee.EmployeeProfileResponse;
import com.teamfoundry.backend.account_options.dto.employee.EmployeeProfileSummaryResponse;
import com.teamfoundry.backend.account_options.dto.employee.EmployeeProfileUpdateRequest;
import com.teamfoundry.backend.account_options.dto.employee.IdentificationDocumentUploadRequest;
import com.teamfoundry.backend.account_options.dto.employee.ProfilePictureUploadRequest;
import com.teamfoundry.backend.account_options.dto.employee.DeactivateAccountRequest;
import com.teamfoundry.backend.account.model.EmployeeAccount;
import com.teamfoundry.backend.account_options.enums.DocumentType;
import com.teamfoundry.backend.account_options.model.employee.EmployeeDocument;
import com.teamfoundry.backend.account.model.EmployeeAccount;
import com.teamfoundry.backend.account_options.repository.employee.DocumentRepository;
import com.teamfoundry.backend.account.repository.EmployeeAccountRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeCompetenceRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeFunctionRepository;
import com.teamfoundry.backend.account_options.repository.employee.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.common.service.CloudinaryService;
import com.teamfoundry.backend.security.repository.AuthTokenRepository;
import com.teamfoundry.backend.admin.service.EmployeeJobHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EmployeeProfileService {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final DocumentRepository documentRepository;
    private final EmployeeFunctionRepository employeeFunctionRepository;
    private final EmployeeCompetenceRepository employeeCompetenceRepository;
    private final EmployeeGeoAreaRepository employeeGeoAreaRepository;
    private final EmployeeJobHistoryService employeeJobHistoryService;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenRepository authTokenRepository;

    /**
     * Le o perfil do colaborador autenticado.
     */
    @Transactional(readOnly = true)
    public EmployeeProfileResponse getProfile(String email) {
        EmployeeAccount account = findByEmailOrThrow(email);
        return toResponse(account);
    }

    /**
     * Atualiza campos basicos do perfil e devolve a versao persistida.
     */
    @Transactional
    public EmployeeProfileResponse updateProfile(String email, EmployeeProfileUpdateRequest request) {
        EmployeeAccount account = findByEmailOrThrow(email);

        account.setName(request.getFirstName().trim());
        account.setSurname(request.getLastName().trim());
        account.setGender(request.getGender().trim().toUpperCase());
        account.setBirthDate(request.getBirthDate());
        account.setNationality(request.getNationality().trim());
        account.setNif(request.getNif());
        account.setPhone(request.getPhone().trim());

        EmployeeAccount saved = employeeAccountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeProfileResponse getProfileWithCv(String email) {
        return toResponse(findByEmailOrThrow(email));
    }

    @Transactional(readOnly = true)
    public EmployeeProfileSummaryResponse getProfileSummary(String email) {
        EmployeeAccount account = findByEmailOrThrow(email);

        boolean hasCv = documentRepository.findByEmployeeAndType(account, DocumentType.CURRICULUM)
                .map(EmployeeDocument::getPublicId)
                .map(StringUtils::hasText)
                .orElse(false);
        boolean hasPhoto = StringUtils.hasText(account.getProfilePicturePublicId());
        boolean hasRole = employeeFunctionRepository.findFirstByEmployee(account).isPresent();
        boolean hasCompetences = !employeeCompetenceRepository.findByEmployee(account).isEmpty();
        boolean hasGeoAreas = !employeeGeoAreaRepository.findByEmployee(account).isEmpty();

        int totalChecks = 5;
        int completed = 0;
        completed += hasCv ? 1 : 0;
        completed += hasPhoto ? 1 : 0;
        completed += hasRole ? 1 : 0;
        completed += hasCompetences ? 1 : 0;
        completed += hasGeoAreas ? 1 : 0;

        int rawPercent = (int) Math.round((completed / (double) totalChecks) * 100);
        int roundedPercent = Math.min(100, Math.max(0, (int) (Math.round(rawPercent / 10.0) * 10)));

        long openOffers = employeeJobHistoryService.countOpenInvites(email);
        String currentCompany = employeeJobHistoryService.findCurrentCompanyName(email);

        return EmployeeProfileSummaryResponse.builder()
                .profileCompletionPercentage(roundedPercent)
                .hasCurriculum(hasCv)
                .hasProfilePicture(hasPhoto)
                .hasRolePreference(hasRole)
                .hasCompetences(hasCompetences)
                .hasGeoAreas(hasGeoAreas)
                .openOffers(openOffers)
                .currentCompanyName(currentCompany)
                .build();
    }

    @Transactional
    public String uploadCurriculum(String email, CurriculumUploadRequest request) {
        EmployeeAccount account = findByEmailOrThrow(email);

        EmployeeDocument document = getOrCreateDocument(account, DocumentType.CURRICULUM);
        cloudinaryService.delete(document.getPublicId());
        CloudinaryService.UploadResult upload = cloudinaryService.uploadBase64(
                request.getFile(), "curriculum", request.getFileName());
        document.setPublicId(upload.getPublicId());
        document.setFileName(request.getFileName());
        documentRepository.save(document);
        return upload.getUrl();
    }

    @Transactional
    public void deleteCurriculum(String email) {
        EmployeeAccount account = findByEmailOrThrow(email);
        documentRepository.findByEmployeeAndType(account, DocumentType.CURRICULUM)
                .ifPresent(doc -> {
                    cloudinaryService.delete(doc.getPublicId());
                    documentRepository.delete(doc);
                });
    }

    @Transactional
    public void uploadIdentificationDocument(String email, IdentificationDocumentUploadRequest request) {
        EmployeeAccount account = findByEmailOrThrow(email);
        DocumentType type = request.getType();
        if (type == DocumentType.CURRICULUM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilize o endpoint de CV para este tipo.");
        }
        EmployeeDocument document = getOrCreateDocument(account, type);
        cloudinaryService.delete(document.getPublicId());
        CloudinaryService.UploadResult upload = cloudinaryService.uploadBase64(
                request.getFile(),
                "identification",
                request.getFileName());
        document.setPublicId(upload.getPublicId());
        document.setFileName(request.getFileName());
        documentRepository.save(document);
    }

    @Transactional
    public void deleteIdentificationDocument(String email, DocumentType type) {
        if (type == DocumentType.CURRICULUM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo inválido para este endpoint.");
        }
        EmployeeAccount account = findByEmailOrThrow(email);
        documentRepository.findByEmployeeAndType(account, type)
                .ifPresent(doc -> {
                    cloudinaryService.delete(doc.getPublicId());
                    documentRepository.delete(doc);
                });
    }

    @Transactional
    public EmployeeProfileResponse uploadProfilePicture(String email, ProfilePictureUploadRequest request) {
        EmployeeAccount account = findByEmailOrThrow(email);
        if (StringUtils.hasText(account.getProfilePicturePublicId())) {
            cloudinaryService.delete(account.getProfilePicturePublicId());
        }
        CloudinaryService.UploadResult upload = cloudinaryService.uploadBase64(
                request.getFile(), "profilepicture", request.getFileName());
        account.setProfilePicturePublicId(upload.getPublicId());
        employeeAccountRepository.save(account);
        return toResponse(account);
    }

    @Transactional
    public void deleteProfilePicture(String email) {
        EmployeeAccount account = findByEmailOrThrow(email);
        if (StringUtils.hasText(account.getProfilePicturePublicId())) {
            cloudinaryService.delete(account.getProfilePicturePublicId());
            account.setProfilePicturePublicId(null);
            employeeAccountRepository.save(account);
        }
    }

    @Transactional
    public void deactivateAccount(String email, DeactivateAccountRequest request) {
        EmployeeAccount account = findByEmailOrThrow(email);
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password incorreta.");
        }
        account.setDeactivated(true);
        account.setVerified(false);
        employeeAccountRepository.save(account);
        authTokenRepository.deleteAllByUser(account);
    }

    private EmployeeAccount findByEmailOrThrow(String email) {
        // Normaliza e valida email antes de carregar a conta.
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador nao autenticado.");
        }
        return employeeAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil nao encontrado."));
    }

    private EmployeeProfileResponse toResponse(EmployeeAccount account) {
        String cvUrl = getDocumentUrl(account, DocumentType.CURRICULUM);
        String idFrontUrl = getDocumentUrl(account, DocumentType.IDENTIFICATION_FRONT);
        String idBackUrl = getDocumentUrl(account, DocumentType.IDENTIFICATION_BACK);
        String profilePictureUrl = buildCloudinaryUrl(account.getProfilePicturePublicId());

        return EmployeeProfileResponse.builder()
                .firstName(account.getName())
                .lastName(account.getSurname())
                .gender(account.getGender())
                .birthDate(account.getBirthDate())
                .nationality(account.getNationality())
                .nif(account.getNif())
                .phone(account.getPhone())
                .email(account.getEmail())
                .deactivated(account.isDeactivated())
                .curriculumUrl(cvUrl)
                .identificationFrontUrl(idFrontUrl)
                .identificationBackUrl(idBackUrl)
                .profilePictureUrl(profilePictureUrl)
                .build();
    }

    private EmployeeDocument getOrCreateDocument(EmployeeAccount account, DocumentType type) {
        return documentRepository.findByEmployeeAndType(account, type)
                .orElseGet(() -> {
                    EmployeeDocument doc = new EmployeeDocument();
                    doc.setEmployee(account);
                    doc.setType(type);
                    doc.setPublicId(null);
                    return doc;
                });
    }

    private String getDocumentUrl(EmployeeAccount account, DocumentType type) {
        return documentRepository.findByEmployeeAndType(account, type)
                .map(EmployeeDocument::getPublicId)
                .map(this::buildCloudinaryUrl)
                .orElse(null);
    }

    private String buildCloudinaryUrl(String publicId) {
        if (publicId == null || publicId.isBlank()) return null;
        return cloudinaryService.buildUrl(publicId);
    }
}
