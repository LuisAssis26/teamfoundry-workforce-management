package com.teamfoundry.backend.account.config;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.AdminAccount;
import com.teamfoundry.backend.account.repository.AdminAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Popula a base com administradores padrão usando senha com hash BCrypt.
 */
@Configuration
@Profile("!test")
public class AdminAccountInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAccountInitializer.class);

    @Bean
    CommandLineRunner seedDefaultAdmins(AdminAccountRepository repository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            // já existentes
            createIfMissing(repository, passwordEncoder, "admin", "admin123", UserType.ADMIN);
            createIfMissing(repository, passwordEncoder, "superadmin", "super123", UserType.SUPERADMIN);

            // extras para testes de atribuição
            createIfMissing(repository, passwordEncoder, "admin01", "admin01pass", UserType.ADMIN);
            createIfMissing(repository, passwordEncoder, "admin02", "admin02pass", UserType.ADMIN);
            createIfMissing(repository, passwordEncoder, "admin03", "admin03pass", UserType.ADMIN);
            createIfMissing(repository, passwordEncoder, "admin04", "admin04pass", UserType.ADMIN);
            createIfMissing(repository, passwordEncoder, "admin05", "admin05pass", UserType.ADMIN);
        };
    }

    private void createIfMissing(AdminAccountRepository repository,
                                 PasswordEncoder passwordEncoder,
                                 String username,
                                 String rawPassword,
                                 UserType role) {
        repository.findByUsername(username).ifPresentOrElse(
                account -> LOGGER.debug("Admin {} already exists; skipping seed.", username),
                () -> {
                    String hashedPassword = passwordEncoder.encode(rawPassword);
                    AdminAccount account = new AdminAccount(0, username, hashedPassword, role);
                    repository.save(account);
                    LOGGER.info("Seeded admin {} with role {}.", username, role);
                }
        );
    }
}
