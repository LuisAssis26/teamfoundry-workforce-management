package com.teamfoundry.backend.account.repository;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração com H2 para o {@link AdminAccountRepository}.
 */
@DataJpaTest
class AdminAccountRepositoryTest {

    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @Test
    @DisplayName("findByUsernameIgnoreCase encontra registos independentemente do casing")
    void findByUsernameIgnoreCase() {
        AdminAccount admin = new AdminAccount();
        admin.setUsername("RootUser");
        admin.setPassword("hash");
        admin.setRole(UserType.ADMIN);
        adminAccountRepository.save(admin);

        assertThat(adminAccountRepository.findByUsernameIgnoreCase("rootuser")).isPresent();
    }
}
