package com.teamfoundry.backend.account.model.company;

import com.teamfoundry.backend.account.model.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_account")
@PrimaryKeyJoinColumn(name = "id")
public class CompanyAccount extends Account {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String country;

    private String phone;

    private String website;

    private String description;

    @Column(nullable = false)
    private boolean status; // estado
}
