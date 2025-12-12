package com.teamfoundry.backend.auth.model.tokens;

import com.teamfoundry.backend.account.model.Account;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private Account user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "expire_at", nullable = false)
    private Timestamp expireAt;
}
