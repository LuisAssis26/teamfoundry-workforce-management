package com.teamfoundry.backend.common.util;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.account.model.employee.documents.EmployeeDocument;
import com.teamfoundry.backend.account.repository.employee.documents.EmployeeDocumentRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeSkillRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeRoleRepository;
import com.teamfoundry.backend.account.repository.employee.profile.EmployeeGeoAreaRepository;
import com.teamfoundry.backend.common.service.CloudinaryService;
import com.teamfoundry.backend.auth.repository.AuthTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Utilitário que remove um candidato e todas as relações dependentes (funções, competências, etc.).
 * Útil em ambientes de desenvolvimento para “limpar” contas sem ter de recriar a base.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountCleanupService {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeGeoAreaRepository employeeGeoAreaRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final AuthTokenRepository authTokenRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Remove um EmployeeAccount e todas as dependências pelo email informado.
     *
     * @param email email do candidato a remover
     * @throws EntityNotFoundException se a conta não existir
     */
    @Transactional
    public void deleteEmployeeAccountByEmail(String email) {
        EmployeeAccount account = employeeAccountRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada para o email informado."));

        log.info("Eliminando dados dependentes do candidato {}", email);
        employeeRoleRepository.deleteByEmployee(account);
        employeeSkillRepository.deleteByEmployee(account);
        employeeGeoAreaRepository.deleteByEmployee(account);
        List<EmployeeDocument> documents = employeeDocumentRepository.findAllByEmployee(account);
        documents.forEach(doc -> cloudinaryService.delete(doc.getPublicId()));
        employeeDocumentRepository.deleteAll(documents);
        cloudinaryService.delete(account.getProfilePicturePublicId());
        authTokenRepository.deleteAllByUser(account);

        employeeAccountRepository.delete(account);
        log.info("Conta {} removida com sucesso.", email);
    }
}
