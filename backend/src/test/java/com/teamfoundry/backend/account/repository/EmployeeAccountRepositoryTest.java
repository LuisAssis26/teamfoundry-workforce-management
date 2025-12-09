package com.teamfoundry.backend.account.repository;

import com.teamfoundry.backend.account.enums.RegistrationStatus;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para operações básicas do {@link EmployeeAccountRepository}.
 */
@DataJpaTest
class EmployeeAccountRepositoryTest {

    @Autowired
    private EmployeeAccountRepository employeeAccountRepository;

    @Test
    @DisplayName("findByEmail devolve a conta guardada")
    void findByEmail() {
        EmployeeAccount account = new EmployeeAccount();
        account.setEmail("employee@example.com");
        account.setPassword("hash");
        account.setRole(UserType.EMPLOYEE);
        account.setRegistrationStatus(RegistrationStatus.PENDING);
        employeeAccountRepository.save(account);

        assertThat(employeeAccountRepository.findByEmail("employee@example.com")).isPresent();
    }
}
