package com.teamfoundry.backend.account.repository.company;

import com.teamfoundry.backend.superadmin.dto.credential.company.CompanyCredentialResponse;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

/**
 * Repositório dedicado à entidade CompanyAccount.
 * Mantém consultas específicas para o fluxo de aprovação de credenciais.
 */
public interface CompanyAccountRepository extends JpaRepository<CompanyAccount, Integer> {

    /**
     * Busca somente contas de empresa com status pendente (status=false),
     * incluindo o responsável associado. A projeção direta no DTO evita
     * carregamentos adicionais na camada de serviço.
     */
    @Query("""
            SELECT new com.teamfoundry.backend.superadmin.dto.credential.company.CompanyCredentialResponse(
                c.id,
                c.name,
                c.email,
                c.website,
                c.address,
                c.nif,
                c.country,
                manager.name,
                manager.email,
                manager.phone,
                manager.position
            )
            FROM CompanyAccount c
            LEFT JOIN CompanyAccountManager manager ON manager.companyAccount = c
            WHERE c.status = false
            ORDER BY c.id DESC
            """)
    List<CompanyCredentialResponse> findPendingCompanyCredentials();

    Optional<CompanyAccount> findByEmail(String email);
}
