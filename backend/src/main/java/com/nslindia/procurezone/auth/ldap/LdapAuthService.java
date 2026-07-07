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

        Optional<LdapConfig> configOpt = lookupConfig(domain);
        if (configOpt.isEmpty()) {
            log.warn("LDAP login attempted for domain '{}' but no LDAP config exists", domain);
            return false;
        }
        LdapConfig config = configOpt.get();

        // Rows with an "N/A" / blank service password cannot service-bind — fail cleanly, never
        // attempt a bind with the literal "N/A".
        String servicePwd = config.getPwd();
        if (servicePwd == null || servicePwd.isBlank() || "N/A".equalsIgnoreCase(servicePwd.trim())) {
            log.warn("LDAP not configured for domain '{}' (service password is blank/N/A)", domain);
            return false;
        }
        if (config.getUrl() == null || config.getUrl().isBlank()
                || config.getPrinc() == null || config.getPrinc().isBlank()
                || config.getUser() == null || config.getUser().isBlank()) {
            log.warn("LDAP config for domain '{}' is incomplete (url/user/princ missing)", domain);
            return false;
        }

        String serviceBindDn = "uid=" + config.getUser() + "," + config.getPrinc();

        DirContext serviceCtx = null;
        DirContext userCtx = null;
        try {
            serviceCtx = new InitialDirContext(buildEnv(config.getUrl(), serviceBindDn, servicePwd));

            String userDn = findUserDn(serviceCtx, config.getPrinc(), email);
            if (userDn == null) {
                log.debug("LDAP: no directory entry with mail='{}' under '{}'", email, config.getPrinc());
                return false; // user not found — generic failure
            }

            // Second bind AS THE USER with the typed password — this is the actual credential check.
            userCtx = new InitialDirContext(buildEnv(config.getUrl(), userDn, password));
            log.debug("LDAP authentication succeeded for {}", email);
            return true;
        } catch (AuthenticationException e) {
            // Wrong password (or service-bind rejected) — generic failure, no detail leaked.
            log.debug("LDAP authentication failed for {}: {}", email, e.getMessage());
            return false;
        } catch (NamingException e) {
            // Unreachable/misconfigured server, timeout, etc. Isolated to this domain — other
            // domains and local login are unaffected.
            log.warn("LDAP error for domain '{}' ({}): {}", domain, config.getUrl(), e.getMessage());
            return false;
        } finally {
            closeQuietly(userCtx);
            closeQuietly(serviceCtx);
        }
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
