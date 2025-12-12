package com.teamfoundry.backend.auth.service.login;

import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.auth.dto.login.AdminLoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Serviço dedicado ao login dos administradores utilizando hashes BCrypt.
 */
@Service
@Transactional(readOnly = true)
public class AdminAuthService {

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(AdminAccountRepository adminAccountRepository,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Optional<AdminLoginResponse> authenticate(String username, String rawPassword) {
        return adminAccountRepository.findByUsernameIgnoreCaseAndDeactivatedFalse(username)
                .filter(account -> passwordEncoder.matches(rawPassword, account.getPassword()))
                .map(account -> {
                    String access = jwtService.generateToken("admin:" + account.getUsername(),
                            account.getRole().name(), account.getId());
                    return new AdminLoginResponse(account.getRole(), access,
                            jwtService.getExpirationSeconds(), null);
                });
    }
}
