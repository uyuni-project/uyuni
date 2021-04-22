/**
 * Copyright (c) 2021 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.scc;

import static java.util.Optional.ofNullable;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * This is a representation of the SCC registration cache.
 */
@Entity
@Table(name = "suseSCCRegCache")
@NamedQueries
({
    @NamedQuery(name = "SCCRegCache.serversRequireRegistration",
                query = "SELECT rci " +
                        "FROM com.redhat.rhn.domain.scc.SCCRegCacheItem as rci " +
                        "JOIN rci.server as s " +
                        "WHERE rci.sccRegistrationRequired = 'Y' " +
                        "AND (rci.registrationErrorTime IS NULL " +
                        "     OR rci.registrationErrorTime < :retryTime) " +
                        "ORDER BY s.id ASC"),
    @NamedQuery(
            name = "SCCRegCache.newServersRequireRegistration",
            query = "SELECT s " +
                    "FROM com.redhat.rhn.domain.server.Server as s " +
                    "WHERE s.id not in (" +
                    "    SELECT rci.server.id " +
                    "    FROM com.redhat.rhn.domain.scc.SCCRegCacheItem as rci ) " +
                    "ORDER BY s.id ASC"),
    @NamedQuery(name = "SCCRegCache.listDeRegisterItems",
                query = "SELECT rci " +
                        "FROM com.redhat.rhn.domain.scc.SCCRegCacheItem as rci " +
                        "WHERE rci.server is NULL " +
                        "AND (rci.registrationErrorTime IS NULL " +
                        "     OR rci.registrationErrorTime < :retryTime) " +
                        "ORDER BY rci.sccId ASC"),
    @NamedQuery(name = "SCCRegCache.listRegItemsByCredentials",
                query = "SELECT rci " +
                        "FROM com.redhat.rhn.domain.scc.SCCRegCacheItem as rci " +
                        "WHERE rci.credentials = :creds " +
                        "ORDER BY rci.sccId ASC"),
})
public class SCCRegCacheItem extends BaseDomainHelper {

    private Long id;
    private Long sccId;
    private boolean sccRegistrationRequired;
    private Server server;
    private String sccLogin;
    private String sccPasswd;
    private Credentials credentials;
    private Date registrationErrorTime;

    public SCCRegCacheItem() {
    }

    public SCCRegCacheItem(Server s) {
        sccRegistrationRequired = true;
        server = s;
        sccPasswd = RandomStringUtils.randomAlphanumeric(64);
    }

    /**
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccregcache_seq")
    @SequenceGenerator(name = "sccregcache_seq", sequenceName = "suse_sccregcache_id_seq",
                       allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @return the sccId
     */
    @Column(name = "scc_id")
    protected Long getSccId() {
        return sccId;
    }

    /**
     * @return Returns the SCC ID when this system was registered already
     */
    @Transient
    public Optional<Long> getOptSccId() {
        return ofNullable(sccId);
    }

    /**
     * Get the mirror credentials.
     * @return the credentials
     */
    @ManyToOne
    @JoinColumn(name = "creds_id")
    protected Credentials getCredentials() {
        return credentials;
    }

    /**
     * Get the mirror credentials
     * @return the mirror credentials
     */
    @Transient
    public Optional<Credentials> getOptCredentials() {
        return ofNullable(credentials);
    }

    /**
     * @return true when updating the registration at SCC is required, otherwise false
     */
    @Column(name = "scc_reg_required")
    @org.hibernate.annotations.Type(type = "yes_no")
    public boolean isSccRegistrationRequired() {
        return sccRegistrationRequired;
    }

    /**
     * @return Returns the server.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = true)
    protected Server getServer() {
        return server;
    }

    /**
     * @return returns the server if available
     */
    @Transient
    public Optional<Server> getOptServer() {
        return ofNullable(server);
    }

    /**
     * @return Returns the sccLogin.
     */
    @Column(name = "scc_login")
    protected String getSccLogin() {
        return sccLogin;
    }

    /**
     * @return return the scc login if set
     */
    @Transient
    public Optional<String> getOptSccLogin() {
        return ofNullable(sccLogin);
    }

    /**
     * @return Returns the sccPasswd.
     */
    @Column(name = "scc_passwd")
    protected String getSccPasswd() {
        return sccPasswd;
    }

    /**
     * @return return the scc password if set
     */
    @Transient
    public Optional<String> getOptSccPasswd() {
        return ofNullable(sccPasswd);
    }

    /**
     * @return the time when the last registration failed or NULL when it did not fail
     */
    @Column(name = "scc_regerror_timestamp")
    protected Date getRegistrationErrorTime() {
        return registrationErrorTime;
    }

    /**
     * @return the time when the last registration failed
     */
    @Transient
    public Optional<Date> getOptRegistrationErrorTime() {
        return ofNullable(registrationErrorTime);
    }

    // Setters

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @param sccIdIn the sccId to set
     */
    public void setSccId(Long sccIdIn) {
        sccId = sccIdIn;
    }

    /**
     * Set if an update of the registration at SCC is required
     *
     * @param sccRegistrationRequiredIn
     */
    public void setSccRegistrationRequired(boolean sccRegistrationRequiredIn) {
        sccRegistrationRequired = sccRegistrationRequiredIn;
    }

    /**
     * Set the mirror credentials this repo can be retrieved with.
     * @param credentialsIn the credentials to set
     */
    public void setCredentials(Credentials credentialsIn) {
        credentials = credentialsIn;
    }

    /**
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        server = serverIn;
    }

    /**
     * @param sccLoginIn The sccLogin to set.
     */
    public void setSccLogin(String sccLoginIn) {
        sccLogin = sccLoginIn;
    }

    /**
     * @param sccPasswdIn The sccPasswd to set.
     */
    public void setSccPasswd(String sccPasswdIn) {
        sccPasswd = sccPasswdIn;
    }

    /**
     * @param registrationErrorTimeIn time when registration failed
     */
    public void setRegistrationErrorTime(Date registrationErrorTimeIn) {
        registrationErrorTime = registrationErrorTimeIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SCCRegCacheItem)) {
            return false;
        }
        SCCRegCacheItem otherSCCRegCache = (SCCRegCacheItem) other;
        return new EqualsBuilder()
                .append(getServer(), otherSCCRegCache.getServer())
                .append(getSccId(), otherSCCRegCache.getSccId())
                .append(getSccLogin(), otherSCCRegCache.getSccLogin())
                .append(getSccPasswd(), otherSCCRegCache.getSccPasswd())
                .append(getCredentials(), otherSCCRegCache.getCredentials())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getServer())
                .append(getSccId())
                .append(getSccLogin())
                .append(getSccPasswd())
                .append(getCredentials())
                .append(getRegistrationErrorTime())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("server", getOptServer().map(s -> s.getId().toString()).orElse(""))
                .append("regRequired", isSccRegistrationRequired())
                .append("sccId", getOptSccId().map(Object::toString).orElse(""))
                .append("sccLogin", getOptSccLogin().orElse(""))
        .toString();
    }
}
