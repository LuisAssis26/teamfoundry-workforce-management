package com.teamfoundry.backend.account.model.company;

import com.teamfoundry.backend.account.model.preferences.PrefActivitySectors;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_activity_sectors")
public class CompanyActivitySectors {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_empresa", nullable = false)
    private CompanyAccount company;

    @ManyToOne
    @JoinColumn(name = "id_setor", nullable = false)
    private PrefActivitySectors sector;
}
