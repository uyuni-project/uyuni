/*
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
package com.redhat.rhn.domain.cloudpayg;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseCloudRmtHost")
@NamedQuery(name = "CloudRmtHost.listHostToUpdate",
        query = "from CloudRmtHost s where not exists " +
                "(select 1 from CloudRmtHost s1 where s1.id != s.id and s1.host = s.host and s1.modified > s.modified)"
)
public class CloudRmtHost extends BaseDomainHelper {
    private Long id;
    private String host;
    private String ip;
    private String sslCert;
    private PaygSshData paygSshData;


    /**
     * Gets the id.
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cloudRmtHost_seq")
    @SequenceGenerator(name = "cloudRmtHost_seq", sequenceName = "susecloudrmthost_id_seq",
            allocationSize = 1)
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

    @Column(name = "hostname")
    public String getHost() {
        return host;
    }

    public void setHost(String h) {
        this.host = h;
    }

    @Column(name = "ip_address")
    public String getIp() {
        return ip;
    }

    public void setIp(String ipIn) {
        this.ip = ipIn;
    }

    @Column(name = "ssl_cert")
    public String getSslCert() {
        return sslCert;
    }

    public void setSslCert(String sslC) {
        this.sslCert = sslC;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payg_ssh_data_id", referencedColumnName = "id")
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
