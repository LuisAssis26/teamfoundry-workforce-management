package com.teamfoundry.backend.account.model.employee.profile;

import com.teamfoundry.backend.account.model.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_account")
@PrimaryKeyJoinColumn(name = "id")
public class EmployeeAccount extends Account {

    @Column
    private String name;

    @Column
    private String surname;

    @Column
    private String phone;

    @Column
    private String nationality;

    @Column
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "profile_picture_public_id")
    private String profilePicturePublicId;

    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @jakarta.persistence.OneToMany(mappedBy = "employee", cascade = jakarta.persistence.CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<EmployeeGeoArea> geoAreas;
}
