package com.teamfoundry.backend.auth.repository;

import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.auth.model.tokens.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    @SuppressWarnings("unused")
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserAndToken(Account user, String token);

    void deleteByUser(Account user);
}

