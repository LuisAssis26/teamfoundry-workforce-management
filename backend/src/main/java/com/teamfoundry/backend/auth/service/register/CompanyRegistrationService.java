package com.teamfoundry.backend.auth.service.register;

import com.teamfoundry.backend.common.dto.GenericResponse;
import com.teamfoundry.backend.auth.dto.register.company.CompanyRegistrationRequest;
import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.company.CompanyAccountManager;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountOwnerRepository;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.auth.service.exception.CompanyRegistrationException;
import com.teamfoundry.backend.account.model.preferences.PrefActivitySectors;
import com.teamfoundry.backend.account.model.company.CompanyActivitySectors;
import com.teamfoundry.backend.account.repository.preferences.PrefActivitySectorsRepository;
import com.teamfoundry.backend.account.repository.company.CompanyActivitySectorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyRegistrationService {

    private final CompanyAccountRepository companyAccountRepository;
    private final CompanyAccountOwnerRepository companyAccountOwnerRepository;
    private final CompanyActivitySectorsRepository companyActivitySectorsRepository;
    private final PrefActivitySectorsRepository prefActivitySectorsRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public GenericResponse registerCompany(CompanyRegistrationRequest request) {
        if (Boolean.FALSE.equals(request.termsAccepted())) {
            throw new CompanyRegistrationException("É necessário aceitar os termos e condições.", HttpStatus.BAD_REQUEST);
        }

        String normalizedEmail = request.credentialEmail().trim().toLowerCase();
        String normalizedResponsibleEmail = request.responsibleEmail().trim().toLowerCase();
        String normalizedResponsiblePhone = request.responsiblePhone().trim();

        accountRepository.findByEmail(normalizedEmail)
                .ifPresent(existing -> {
                    throw new CompanyRegistrationException("O email informado já está associado a outra conta.", HttpStatus.CONFLICT);
                });

        if (companyAccountOwnerRepository.existsByEmailIgnoreCase(normalizedResponsibleEmail)) {
            throw new CompanyRegistrationException("O e-mail do responsável já está em utilização.", HttpStatus.CONFLICT);
        }

        CompanyAccount companyAccount = new CompanyAccount();
        companyAccount.setEmail(normalizedEmail);
        companyAccount.setPassword(passwordEncoder.encode(request.password()));
        companyAccount.setRole(UserType.COMPANY);
        companyAccount.setRegistrationStatus(RegistrationStatus.COMPLETED);
        companyAccount.setVerified(false);
        companyAccount.setName(request.companyName().trim());
        companyAccount.setNif(request.nif());
        companyAccount.setAddress(request.address().trim());
        companyAccount.setCountry(request.country().trim());
        companyAccount.setWebsite(StringUtils.hasText(request.website()) ? request.website().trim() : null);
        companyAccount.setDescription(StringUtils.hasText(request.description()) ? request.description().trim() : null);
        companyAccount.setStatus(false);

        CompanyAccount savedCompany = companyAccountRepository.save(companyAccount);

        CompanyAccountManager owner = new CompanyAccountManager();
        owner.setCompanyAccount(savedCompany);
        owner.setName(request.responsibleName().trim());
        owner.setEmail(normalizedResponsibleEmail);
        owner.setPhone(normalizedResponsiblePhone);
        owner.setPosition(request.responsiblePosition().trim());
        companyAccountOwnerRepository.save(owner);

        attachActivitySectors(savedCompany, request.activitySectors());

        log.info("Empresa {} registada com estado COMPLETED", request.companyName());
        return GenericResponse.success("Registo submetido. Entraremos em contacto após validação.");
    }

    private void attachActivitySectors(CompanyAccount company, List<String> sectors) {
        if (sectors == null || sectors.isEmpty()) {
            throw new CompanyRegistrationException("Selecione pelo menos uma área de atividade.", HttpStatus.BAD_REQUEST);
        }

        List<PrefActivitySectors> matched = prefActivitySectorsRepository.findByNameIn(sectors);
        if (matched.size() != sectors.stream().distinct().count()) {
            throw new CompanyRegistrationException("Uma ou mais áreas de atividade são inválidas.", HttpStatus.BAD_REQUEST);
        }

        List<CompanyActivitySectors> relations = matched.stream()
                .map(sector -> {
                    CompanyActivitySectors relation = new CompanyActivitySectors();
                    relation.setCompany(company);
                    relation.setSector(sector);
                    return relation;
                })
                .toList();
        companyActivitySectorsRepository.saveAll(relations);
    }
}
