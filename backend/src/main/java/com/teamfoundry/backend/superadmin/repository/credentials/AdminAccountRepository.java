package com.teamfoundry.backend.superadmin.repository.credentials;

import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para consultar administradores por username.
 */
public interface AdminAccountRepository extends JpaRepository<AdminAccount, Integer> {

    Optional<AdminAccount> findByUsername(String username);
    Optional<AdminAccount> findByUsernameIgnoreCase(String username);

    Optional<AdminAccount> findByUsernameIgnoreCaseAndDeactivatedFalse(String username);

    List<AdminAccount> findByDeactivatedFalse(Sort sort);
}
