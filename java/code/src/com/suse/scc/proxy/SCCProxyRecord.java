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

import com.redhat.rhn.domain.BaseDomainHelper;

import java.util.Arrays;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseSccProxy")
public class SCCProxyRecord extends BaseDomainHelper {

    private Long proxyId;
    private String sccLogin;
    private String sccPasswd;
    private String sccCreationJson;
    private Long sccId;
    private Status status;

    public enum Status {
        SCC_CREATION_PENDING("SCC_CREATION_PENDING"),
        SCC_CREATED("SCC_CREATED"),
        SCC_REMOVAL_PENDING("SCC_REMOVAL_PENDING");

        private final String label;

        Status(String labelIn) {
            this.label = labelIn;
        }

        /**
         * Gets the label representing the status
         *
         * @return label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Looks up Status by label
         *
         * @param label the label representing the status
         * @return the matching status
         * @throws java.lang.IllegalArgumentException if no matching status is found
         */
        public static SCCProxyRecord.Status byLabel(String label) {
            return Arrays.stream(SCCProxyRecord.Status.values())
                    .filter(type -> type.getLabel().equals(label))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid label " + label));
        }
    }

    /**
     * Default constructor
     */
    public SCCProxyRecord() {
        this(null, null, null);
    }

    /**
     * Constructor
     *
     * @param sccLoginIn login of the system to register in SCC
     * @param sccPasswdIn password of the system to register in SCC
     * @param sccCreationJsonIn original creation json of the system to register in SCC
     */
    public SCCProxyRecord(String sccLoginIn, String sccPasswdIn, String sccCreationJsonIn) {
        sccLogin = sccLoginIn;
        sccPasswd = sccPasswdIn;
        sccCreationJson = sccCreationJsonIn;
        status = Status.SCC_CREATION_PENDING;
    }

    @Id
    @Column(name = "proxy_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccproxy_seq")
    @SequenceGenerator(name = "sccproxy_seq", sequenceName = "suse_sccproxy_id_seq",
            allocationSize = 1)
    public Long getProxyId() {
        return proxyId;
    }

    public void setProxyId(Long proxyIdIn) {
        proxyId = proxyIdIn;
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

    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status statusIn) {
        status = statusIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof SCCProxyRecord that)) {
            return false;
        }
        return Objects.equals(getProxyId(), that.getProxyId()) &&
                Objects.equals(getSccLogin(), that.getSccLogin()) &&
                Objects.equals(getSccPasswd(), that.getSccPasswd()) &&
                Objects.equals(getSccCreationJson(), that.getSccCreationJson()) &&
                Objects.equals(getSccId(), that.getSccId()) &&
                Objects.equals(getStatus(), that.getStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProxyId(),
                getSccLogin(),
                getSccPasswd(),
                getSccCreationJson(),
                getSccId(),
                getStatus());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SCCProxyRecord{");
        sb.append("proxyId=").append(proxyId);
        sb.append(", sccLogin='").append(sccLogin).append('\'');
        sb.append(", sccPasswd='").append(sccPasswd).append('\'');
        sb.append(", sccCreationJson='").append(sccCreationJson).append('\'');
        sb.append(", sccId=").append(sccId);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
