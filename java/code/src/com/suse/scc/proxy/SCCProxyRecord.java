/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.scc.proxy;

import static java.util.Optional.ofNullable;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.hibernate.annotations.Type;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "suseSccProxy")
public class SCCProxyRecord extends BaseDomainHelper {

    private Long proxyId;
    private String peripheralFqdn;
    private String sccLogin;
    private String sccPasswd;
    private String sccCreationJson;
    private Long sccId;
    private Date sccRegistrationErrorTime;
    private Date lastSeenAt;
    private SccProxyStatus status;

    /**
     * Default constructor
     */
    public SCCProxyRecord() {
        this(null, null, null, null);
    }

    /**
     * Constructor with status SCC_CREATION_PENDING
     *
     * @param peripheralFqdnIn peripheral from which the request comes from
     * @param sccLoginIn login of the system to register in SCC
     * @param sccPasswdIn password of the system to register in SCC
     */
    public SCCProxyRecord(String peripheralFqdnIn, String sccLoginIn, String sccPasswdIn) {
        this(peripheralFqdnIn, sccLoginIn, sccPasswdIn, null, SccProxyStatus.SCC_CREATION_PENDING);
    }

    /**
     * Constructor with status SCC_CREATION_PENDING
     *
     * @param peripheralFqdnIn peripheral from which the request comes from
     * @param sccLoginIn login of the system to register in SCC
     * @param sccPasswdIn password of the system to register in SCC
     * @param sccCreationJsonIn original creation json of the system to register in SCC
     */
    public SCCProxyRecord(String peripheralFqdnIn, String sccLoginIn, String sccPasswdIn, String sccCreationJsonIn) {
        this(peripheralFqdnIn, sccLoginIn, sccPasswdIn, sccCreationJsonIn, SccProxyStatus.SCC_CREATION_PENDING);
    }

    /**
     * Constructor
     *
     * @param peripheralFqdnIn peripheral from which the request comes from
     * @param sccLoginIn login of the system to register in SCC
     * @param sccPasswdIn password of the system to register in SCC
     * @param sccCreationJsonIn original creation json of the system to register in SCC
     * @param statusIn the status of this entry
     */
    public SCCProxyRecord(String peripheralFqdnIn, String sccLoginIn, String sccPasswdIn, String sccCreationJsonIn,
                          SccProxyStatus statusIn) {
        peripheralFqdn = peripheralFqdnIn;
        sccLogin = sccLoginIn;
        sccPasswd = sccPasswdIn;
        sccCreationJson = sccCreationJsonIn;
        status = statusIn;
    }

    @Id
    @Column(name = "proxy_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getProxyId() {
        return proxyId;
    }

    public void setProxyId(Long proxyIdIn) {
        proxyId = proxyIdIn;
    }

    @Column(name = "peripheral_fqdn")
    public String getPeripheralFqdn() {
        return peripheralFqdn;
    }

    public void setPeripheralFqdn(String peripheralFqdnIn) {
        peripheralFqdn = peripheralFqdnIn;
    }

    @Column(name = "scc_login")
    public String getSccLogin() {
        return sccLogin;
    }

    public void setSccLogin(String sccLoginIn) {
        sccLogin = sccLoginIn;
    }

    @Column(name = "scc_passwd")
    public String getSccPasswd() {
        return sccPasswd;
    }

    public void setSccPasswd(String sccPasswdIn) {
        sccPasswd = sccPasswdIn;
    }

    @Column(name = "scc_creation_json")
    public String getSccCreationJson() {
        return sccCreationJson;
    }

    public void setSccCreationJson(String sccCreationJsonIn) {
        sccCreationJson = sccCreationJsonIn;
    }

    @Column(name = "scc_id")
    public Long getSccId() {
        return sccId;
    }

    public void setSccId(Long sccIdIn) {
        sccId = sccIdIn;
    }

    /**
     * @return Returns the SCC ID when this system was registered already
     */
    @Transient
    public Optional<Long> getOptSccId() {
        return ofNullable(sccId);
    }

    /**
     * @return the time when the last registration failed
     */
    @Column(name = "scc_regerror_timestamp")
    public Date getSccRegistrationErrorTime() {
        return sccRegistrationErrorTime;
    }

    public void setSccRegistrationErrorTime(Date sccRegistrationErrorTimeIn) {
        sccRegistrationErrorTime = sccRegistrationErrorTimeIn;
    }

    @Transient
    public Optional<Date> getOptSccRegistrationErrorTime() {
        return ofNullable(sccRegistrationErrorTime);
    }

    /**
     * @return the time when the system has been seen
     */
    @Column(name = "last_seen_at")
    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAtIn) {
        lastSeenAt = lastSeenAtIn;
    }

    @Transient
    public Optional<Date> getOptLastSeenAt() {
        return ofNullable(lastSeenAt);
    }


    @Type(type = "com.suse.scc.proxy.SccProxyStatusEnumType")
    public SccProxyStatus getStatus() {
        return status;
    }

    public void setStatus(SccProxyStatus statusIn) {
        status = statusIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof SCCProxyRecord that)) {
            return false;
        }
        return Objects.equals(getProxyId(), that.getProxyId()) &&
                Objects.equals(getPeripheralFqdn(), that.getPeripheralFqdn()) &&
                Objects.equals(getSccLogin(), that.getSccLogin()) &&
                Objects.equals(getSccPasswd(), that.getSccPasswd()) &&
                Objects.equals(getSccId(), that.getSccId()) &&
                Objects.equals(getStatus(), that.getStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProxyId(),
                getPeripheralFqdn(),
                getSccLogin(),
                getSccPasswd(),
                getSccId(),
                getStatus());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SCCProxyRecord{");
        sb.append("proxyId=").append(proxyId);
        sb.append(", peripheralFqdn='").append(peripheralFqdn).append('\'');
        sb.append(", sccLogin='").append(sccLogin).append('\'');
        sb.append(", sccPasswd='").append(sccPasswd).append('\'');
        sb.append(", sccCreationJson='").append(sccCreationJson).append('\'');
        sb.append(", sccId=").append(sccId);
        sb.append(", sccRegistrationErrorTime=").append(sccRegistrationErrorTime);
        sb.append(", lastSeenAt=").append(lastSeenAt);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
