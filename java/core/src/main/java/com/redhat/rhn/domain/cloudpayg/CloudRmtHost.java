/*
 * Copyright (c) 2021--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.cloudpayg;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "suseCloudRmtHost")
public class CloudRmtHost extends BaseDomainHelper {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cloudRmtHost_seq")
    @SequenceGenerator(name = "cloudRmtHost_seq", sequenceName = "susecloudrmthost_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "hostname")
    private String host;

    @Column(name = "ip_address")
    private String ip;

    @Column(name = "ssl_cert")
    private String sslCert;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payg_ssh_data_id", referencedColumnName = "id")
    private PaygSshData paygSshData;


    /**
     * Gets the id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String h) {
        this.host = h;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ipIn) {
        this.ip = ipIn;
    }

    public String getSslCert() {
        return sslCert;
    }

    public void setSslCert(String sslC) {
        this.sslCert = sslC;
    }

    public PaygSshData getPaygSshData() {
        return paygSshData;
    }

    public void setPaygSshData(PaygSshData p) {
        this.paygSshData = p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CloudRmtHost that = (CloudRmtHost) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(host, that.host)
                .append(ip, that.ip)
                .append(sslCert, that.sslCert)
                .append(paygSshData, that.paygSshData)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(host)
                .append(ip)
                .append(sslCert)
                .append(paygSshData)
                .toHashCode();
    }
}
