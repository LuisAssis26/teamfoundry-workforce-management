package com.teamfoundry.backend.auth.model.tokens;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "token_auth")
@PrimaryKeyJoinColumn(name = "id")
public class AuthToken extends Token {
    // Usa o campo 'token' herdado para armazenar o refresh token
}
