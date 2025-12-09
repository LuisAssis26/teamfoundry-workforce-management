package com.teamfoundry.backend.account.config.admin;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;


/**
 * Popula a base com administradores padrao usando senha com hash BCrypt.
 */
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class AdminAccountInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    CommandLineRunner seedAdmins(AdminAccountRepository adminAccountRepository) {
        return args -> {
            if (adminAccountRepository.count() > 0) {
                LOGGER.debug("Admins already exist; skipping admin seeding.");
                return;
            }

            List<AdminAccount> admins = List.of(
                    admin("superadmin", "password123", UserType.SUPERADMIN),
                    admin("admin1", "password123", UserType.ADMIN),
                    admin("admin2", "password123", UserType.ADMIN),
                    admin("admin3", "password123", UserType.ADMIN),
                    admin("admin4", "password123", UserType.ADMIN),
                    admin("admin5", "password123", UserType.ADMIN)
            );

            adminAccountRepository.saveAll(admins);
            LOGGER.info("Seeded {} admin accounts (including superadmin).", admins.size());
        };
    }

    private AdminAccount admin(String username, String rawPassword, UserType role) {
        AdminAccount acc = new AdminAccount();
        acc.setUsername(username);
        acc.setPassword(passwordEncoder.encode(rawPassword));
        acc.setRole(role);
        return acc;
    }
}
