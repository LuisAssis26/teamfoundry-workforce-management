package com.teamfoundry.backend.account.repository.company;

import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.company.CompanyActivitySectors;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyActivitySectorsRepository extends JpaRepository<CompanyActivitySectors, Integer> {
    void deleteByCompany(CompanyAccount company);
}
