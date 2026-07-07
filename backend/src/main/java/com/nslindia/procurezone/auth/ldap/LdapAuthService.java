package com.nslindia.procurezone.auth.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import java.util.Hashtable;
import java.util.Optional;

/**
 * LDAP authentication against per-domain Zimbra OpenLDAP servers (inetOrgPerson), using plain JNDI.
 *
 * <p>Flow (confirmed against the live servers):
 * <ol>
 *   <li>Extract the email domain (part after '@').</li>
 *   <li>Look up {@link LdapConfig} for that domain (exact, then malformed-tolerant fallback).</li>
 *   <li>Service-bind as {@code uid=<config_user>,<config_princ>} with {@code config_pwd}.</li>
 *   <li>Search {@code config_princ} subtree for {@code (mail=<email>)} to get the user's real DN.</li>
 *   <li>Bind a second context as that user DN with the password the user typed.</li>
 * </ol>
 * A successful second bind = authenticated. Every failure mode (no config, N/A password, user not
 * found, wrong password, unreachable server) returns {@code false} — the caller cannot distinguish
 * them, and no stack trace or credential is ever surfaced.
 *
 * <p>This service ONLY verifies the password. All authorization data (roles, department, company)
 * comes from the employee record, resolved by the caller.
 */
@Service
public class LdapAuthService {

    private static final Logger log = LoggerFactory.getLogger(LdapAuthService.class);

    private static final String LDAP_CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    // Timeouts so a bad/unreachable server can never hang the login thread.
    private static final String CONNECT_TIMEOUT_MS = "5000";
    private static final String READ_TIMEOUT_MS = "5000";

    private final LdapConfigRepository ldapConfigRepository;

    public LdapAuthService(LdapConfigRepository ldapConfigRepository) {
        this.ldapConfigRepository = ldapConfigRepository;
    }

    /**
     * @return true only if the email/password authenticate against the domain's directory.
     *         All failures return false (generic — never leaks the reason to the caller).
     */
    public boolean authenticate(String email, String password) {
        if (email == null || password == null || password.isEmpty() || !email.contains("@")) {
            return false;
        }
        String domain = email.substring(email.lastIndexOf('@') + 1).trim().toLowerCase();
        if (domain.isEmpty()) {
            return false;
        }

        // [DIAG] Stage logging at WARN so it surfaces without changing log config. No passwords logged.
        log.warn("LDAP-DIAG step1: email='{}' → domain='{}'", email, domain);

        Optional<LdapConfig> configOpt = lookupConfig(domain);
        if (configOpt.isEmpty()) {
            log.warn("LDAP-DIAG step2: NO config row found for domain '{}' — failing", domain);
            return false;
        }
        LdapConfig config = configOpt.get();
        log.warn("LDAP-DIAG step2: config found id={} for domain '{}' (url='{}')",
                config.getId(), domain, config.getUrl());

        // Rows with an "N/A" / blank service password cannot service-bind — fail cleanly, never
        // attempt a bind with the literal "N/A".
        String servicePwd = config.getPwd();
        if (servicePwd == null || servicePwd.isBlank() || "N/A".equalsIgnoreCase(servicePwd.trim())) {
            log.warn("LDAP-DIAG step2: config id={} has blank/N/A service password — failing", config.getId());
            return false;
        }
        if (config.getUrl() == null || config.getUrl().isBlank()
                || config.getPrinc() == null || config.getPrinc().isBlank()
                || config.getUser() == null || config.getUser().isBlank()) {
            log.warn("LDAP-DIAG step2: config id={} incomplete (url/user/princ missing) — failing", config.getId());
            return false;
        }

        // Normalize DN whitespace: config_princ values carry spaces after commas
        // (e.g. "ou=people, dc=ashaagrisciences, dc=com"). ldapsearch tolerates them, but JNDI's
        // DN parser can reject/misparse them, so collapse ", " → "," before building any DN or
        // using it as a search base.
        String rawPrinc = config.getPrinc();
        String princ = normalizeDn(rawPrinc);
        String serviceBindDn = "uid=" + config.getUser().trim() + "," + princ;
        if (!rawPrinc.equals(princ)) {
            log.warn("LDAP-DIAG step3: normalized config_princ whitespace: '{}' → '{}'", rawPrinc, princ);
        }
        log.warn("LDAP-DIAG step3: service-bind DN='{}' url='{}' auth=simple", serviceBindDn, config.getUrl());

        DirContext serviceCtx = null;
        DirContext userCtx = null;
        try {
            serviceCtx = new InitialDirContext(buildEnv(config.getUrl(), serviceBindDn, servicePwd));
            log.warn("LDAP-DIAG step4: service-bind SUCCEEDED for config id={}", config.getId());

            String userDn = findUserDn(serviceCtx, princ, email);
            if (userDn == null) {
                log.warn("LDAP-DIAG step5: (mail={}) search under base '{}' returned NO entry — failing",
                        email, princ);
                return false; // user not found — generic failure
            }
            log.warn("LDAP-DIAG step5: search found user DN='{}'", userDn);

            // Second bind AS THE USER with the typed password — this is the actual credential check.
            userCtx = new InitialDirContext(buildEnv(config.getUrl(), userDn, password));
            log.warn("LDAP-DIAG step6: user-bind SUCCEEDED — authenticated {}", email);
            return true;
        } catch (AuthenticationException e) {
            // Wrong password OR service-bind rejected — generic failure to the client, detail here.
            log.warn("LDAP-DIAG FAIL (auth) for {} at bind: {}: {}",
                    email, e.getClass().getName(), e.getMessage());
            return false;
        } catch (NamingException e) {
            // Unreachable/misconfigured server, timeout, DN-parse error, etc. Isolated to this domain.
            log.warn("LDAP-DIAG FAIL (naming) for domain '{}' url='{}': {}: {}",
                    domain, config.getUrl(), e.getClass().getName(), e.getMessage());
            return false;
        } finally {
            closeQuietly(userCtx);
            closeQuietly(serviceCtx);
        }
    }

    /** Collapse whitespace after DN component separators: ", " (and ",  ") → ",". */
    private String normalizeDn(String dn) {
        return dn == null ? null : dn.replaceAll(",\\s+", ",").trim();
    }

    /**
     * Exact domain match first; then a malformed-tolerant fallback for rows whose config_domine
     * contains an '@' (e.g. "barracudanslgroup@nslgroup.in") by comparing the part after '@'.
     */
    private Optional<LdapConfig> lookupConfig(String domain) {
        Optional<LdapConfig> exact = ldapConfigRepository.findByDomine(domain);
        if (exact.isPresent()) {
            return exact;
        }
        return ldapConfigRepository.findAll().stream()
                .filter(c -> c.getDomine() != null)
                .filter(c -> {
                    String d = c.getDomine().trim();
                    String tail = d.contains("@") ? d.substring(d.lastIndexOf('@') + 1) : d;
                    return tail.equalsIgnoreCase(domain);
                })
                .findFirst();
    }

    private String findUserDn(DirContext ctx, String baseDn, String email) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(new String[0]); // we only need the DN, not attributes
        controls.setCountLimit(1);
        controls.setTimeLimit(5000);

        NamingEnumeration<SearchResult> results = null;
        try {
            results = ctx.search(baseDn, "(mail={0})", new Object[] { email }, controls);
            if (results.hasMore()) {
                return results.next().getNameInNamespace(); // absolute DN to bind as
            }
            return null;
        } finally {
            if (results != null) {
                try { results.close(); } catch (NamingException ignored) { /* no-op */ }
            }
        }
    }

    private Hashtable<String, String> buildEnv(String url, String principal, String credentials) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CTX_FACTORY);
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, credentials);
        env.put("com.sun.jndi.ldap.connect.timeout", CONNECT_TIMEOUT_MS);
        env.put("com.sun.jndi.ldap.read.timeout", READ_TIMEOUT_MS);
        return env;
    }

    private void closeQuietly(DirContext ctx) {
        if (ctx != null) {
            try { ctx.close(); } catch (NamingException ignored) { /* no-op */ }
        }
    }
}
