package com.teamfoundry.backend.auth.service;

import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountDetailsService implements UserDetailsService {

    private static final String ADMIN_PREFIX = "admin:";

    private final AccountRepository repository;
    private final AdminAccountRepository adminAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username != null && username.startsWith(ADMIN_PREFIX)) {
            String adminUsername = username.substring(ADMIN_PREFIX.length());
            AdminAccount admin = adminAccountRepository.findByUsernameIgnoreCase(adminUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()));
            return new User(username, admin.getPassword(), true, true, true, true, authorities);
        }

        Account acc = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + acc.getRole().name()));
        boolean enabled = acc.isVerified() && !acc.isDeactivated();
        return new User(acc.getEmail(), acc.getPassword(), enabled, true, true, true, authorities);
    }
}
