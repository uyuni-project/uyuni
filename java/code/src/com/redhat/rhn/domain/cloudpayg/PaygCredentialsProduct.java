/*
 * Copyright (c) 2023 SUSE LLC
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
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "susePaygProduct")
@NamedQuery(
    name = "PaygCredentialsProduct.listByCredentialsId",
    query = "SELECT p FROM com.redhat.rhn.domain.cloudpayg.PaygCredentialsProduct AS p WHERE p.credentialsId = :credsId"
)
@NamedQuery(
    name = "PaygCredentialsProduct.deleteByCredentialsId",
    query = "DELETE FROM com.redhat.rhn.domain.cloudpayg.PaygCredentialsProduct AS p WHERE p.credentialsId = :credsId"
)
public class PaygCredentialsProduct extends BaseDomainHelper {

    private Long id;

    private Long credentialsId;

    private String name;

    private String version;

    private String arch;

    /**
     * Default constructor for hibernate
     */
    protected PaygCredentialsProduct() {
    }

    /**
     * Create an instance with the given credentials id and product
     * @param credentialsIdIn the id of the credential
     * @param productInfoIn the product
     */
    PaygCredentialsProduct(Long credentialsIdIn, PaygProductInfo productInfoIn) {
        this.credentialsId = credentialsIdIn;
        this.name = productInfoIn.getName();
        this.version = productInfoIn.getVersion();
        this.arch = productInfoIn.getArch();
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "susePaygProduct_seq")
    @SequenceGenerator(name = "susePaygProduct_seq", sequenceName = "susePaygProduct_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    @Column(name = "credentials_id")
    public Long getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(Long credentialsIdIn) {
        this.credentialsId = credentialsIdIn;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    @Column(name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    @Column(name = "arch")
    public String getArch() {
        return arch;
    }

    public void setArch(String archIn) {
        this.arch = archIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PaygCredentialsProduct)) {
            return false;
        }

        PaygCredentialsProduct that = (PaygCredentialsProduct) o;

        return new EqualsBuilder()
            .append(credentialsId, that.credentialsId)
            .append(name, that.getName())
            .append(version, that.getVersion())
            .append(arch, that.getArch())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(credentialsId)
            .append(name)
            .append(version)
            .append(arch)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("credentialsId", credentialsId)
            .append("name", name)
            .append("version", version)
            .append("arch", arch)
            .append("created", getCreated())
            .append("modified", getModified())
            .toString();
    }
}
