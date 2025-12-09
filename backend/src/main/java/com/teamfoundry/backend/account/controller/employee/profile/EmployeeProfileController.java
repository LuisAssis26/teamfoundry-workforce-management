package com.teamfoundry.backend.account.controller.employee.profile;

import com.teamfoundry.backend.account.dto.employee.profile.EmployeeProfileResponse;
import com.teamfoundry.backend.account.dto.employee.documents.EmployeeProfileSummaryResponse;
import com.teamfoundry.backend.account.dto.employee.profile.EmployeeProfileUpdateRequest;
import com.teamfoundry.backend.account.dto.employee.documents.CurriculumUploadRequest;
import com.teamfoundry.backend.account.dto.employee.documents.IdentificationDocumentUploadRequest;
import com.teamfoundry.backend.account.dto.employee.documents.ProfilePictureUploadRequest;
import com.teamfoundry.backend.account.dto.employee.profile.EmployeeDeactivateAccountRequest;
import com.teamfoundry.backend.account.enums.DocumentType;
import com.teamfoundry.backend.account.service.employee.EmployeeProfileAndDocumentsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/employee/profile", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EmployeeProfileController {

    private final EmployeeProfileAndDocumentsService employeeProfileAndDocumentsService;

    /**
     * Devolve os dados básicos do perfil do colaborador autenticado.
     */
    @GetMapping
    public EmployeeProfileResponse getProfile(Authentication authentication) {
        return employeeProfileAndDocumentsService.getProfile(resolveEmail(authentication));
    }

    /**
     * Devolve resumo do perfil (percentual de preenchimento, ofertas e empresa atual).
     */
    @GetMapping("/summary")
    public EmployeeProfileSummaryResponse getProfileSummary(Authentication authentication) {
        return employeeProfileAndDocumentsService.getProfileSummary(resolveEmail(authentication));
    }

    /**
     * Atualiza os campos do perfil (nome, género, contactos) do colaborador autenticado.
     */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeProfileResponse updateProfile(
            @Valid @RequestBody EmployeeProfileUpdateRequest request,
            Authentication authentication) {
        return employeeProfileAndDocumentsService.updateProfile(resolveEmail(authentication), request);
    }

    /**
     * Devolve URL do currículo (se existir).
     */
    @GetMapping("/cv")
    public EmployeeProfileResponse getProfileWithCv(Authentication authentication) {
        return employeeProfileAndDocumentsService.getProfileWithCv(resolveEmail(authentication));
    }

    /**
     * Upload/substituição do currículo. Guarda apenas o public_id e devolve o perfil com URL atualizado.
     */
    @PostMapping(value = "/cv", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeProfileResponse uploadCv(
            @Valid @RequestBody CurriculumUploadRequest request,
            Authentication authentication) {
        String email = resolveEmail(authentication);
        employeeProfileAndDocumentsService.uploadCurriculum(email, request);
        return employeeProfileAndDocumentsService.getProfileWithCv(email);
    }

    /**
     * Elimina o currículo do colaborador autenticado.
     */
    @DeleteMapping("/cv")
    public void deleteCv(Authentication authentication) {
        employeeProfileAndDocumentsService.deleteCurriculum(resolveEmail(authentication));
    }

    /**
     * Upload/substituição de documento de identificação (frente/verso).
     */
    @PostMapping(value = "/id-document", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeProfileResponse uploadIdentificationDocument(
            @Valid @RequestBody IdentificationDocumentUploadRequest request,
            Authentication authentication) {
        String email = resolveEmail(authentication);
        employeeProfileAndDocumentsService.uploadIdentificationDocument(email, request);
        return employeeProfileAndDocumentsService.getProfileWithCv(email);
    }

    /**
     * Elimina um documento de identificação específico.
     */
    @DeleteMapping("/id-document")
    public void deleteIdentificationDocument(
            @RequestParam DocumentType type,
            Authentication authentication) {
        employeeProfileAndDocumentsService.deleteIdentificationDocument(resolveEmail(authentication), type);
    }

    @PostMapping(value = "/photo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeProfileResponse uploadProfilePicture(
            @Valid @RequestBody ProfilePictureUploadRequest request,
            Authentication authentication) {
        return employeeProfileAndDocumentsService.uploadProfilePicture(resolveEmail(authentication), request);
    }

    @DeleteMapping("/photo")
    public void deleteProfilePicture(Authentication authentication) {
        employeeProfileAndDocumentsService.deleteProfilePicture(resolveEmail(authentication));
    }

    @PostMapping(value = "/deactivate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deactivateAccount(
            @Valid @RequestBody EmployeeDeactivateAccountRequest request,
            Authentication authentication) {
        employeeProfileAndDocumentsService.deactivateAccount(resolveEmail(authentication), request);
    }

    private String resolveEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return authentication.getName();
    }
}
