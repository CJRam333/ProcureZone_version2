package com.nslindia.procurezone.auth.ldap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Per-domain LDAP connection config, mapped to the legacy {@code tbl_ldap_config} table.
 * Read-only: consumed by {@link LdapAuthService} to service-bind and search a domain's directory.
 *
 * <p>These are Zimbra OpenLDAP servers (inetOrgPerson), one config row per email domain.
 * Only the columns needed for authentication are mapped (config_lmd / config_lmu are ignored).
 */
@Entity
@Table(name = "tbl_ldap_config")
public class LdapConfig {

    @Id
    @Column(name = "config_id")
    private Integer id;

    /** Email domain this config serves, e.g. "ashaagrisciences.com". May be malformed on some rows. */
    @Column(name = "config_domine")
    private String domine;

    /** LDAP server URL, e.g. "ldap://host:389". */
    @Column(name = "config_url")
    private String url;

    /** Service-bind account local-part (used as uid=&lt;user&gt;,&lt;princ&gt;). */
    @Column(name = "config_user")
    private String user;

    /** Service-bind password. May be "N/A" on rows that cannot service-bind. */
    @Column(name = "config_pwd")
    private String pwd;

    /** Base DN, e.g. "ou=people,dc=ashaagrisciences,dc=com". */
    @Column(name = "config_princ")
    private String princ;

    public Integer getId() { return id; }
    public String getDomine() { return domine; }
    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPwd() { return pwd; }
    public String getPrinc() { return princ; }
}
