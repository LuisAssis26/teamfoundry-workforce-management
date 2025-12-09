package com.teamfoundry.backend.account.model.company;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_account_manager")
public class CompanyAccountManager {

    @Id
    @Column(name = "id_empresa")
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_empresa", referencedColumnName = "id", nullable = false)
    private CompanyAccount companyAccount;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String position;
}
