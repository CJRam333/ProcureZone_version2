package com.nslindia.procurezone.auth.ldap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Read access to {@code tbl_ldap_config}. One row per email domain.
 */
public interface LdapConfigRepository extends JpaRepository<LdapConfig, Integer> {

    /** Exact match on config_domine (covers the well-formed domain rows). */
    Optional<LdapConfig> findByDomine(String domine);
}
