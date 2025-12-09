package com.teamfoundry.backend.account.model.employee.profile;

import com.teamfoundry.backend.account.model.preferences.PrefGeoArea;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_geo_area")
public class EmployeeGeoArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_funcionario", nullable = false)
    private EmployeeAccount employee;

    @ManyToOne
    @JoinColumn(name = "id_area_geo", nullable = false)
    private PrefGeoArea geoArea;
}
