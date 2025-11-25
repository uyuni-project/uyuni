/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.token;

import com.redhat.rhn.domain.org.Org;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Token
 */
@Entity
@Table(
        name = "rhnRegTokenOrgDefault",
        uniqueConstraints = @UniqueConstraint(columnNames = "reg_token_id")
)
public class RegTokenOrgDefault implements Serializable {

    @Id
    @Column(name = "org_id")
    private Long orgId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "org_id", foreignKey = @ForeignKey(name = "rhn_reg_token_def_oid_fk"))
    private Org org;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "reg_token_id", foreignKey = @ForeignKey(name = "rhn_reg_token_def_tokid_fk"))
    private Token token;

    /**
     * Default Constructor.
     */
    public RegTokenOrgDefault() {
        org = new Org();
        token = new Token();
    }

    /**
     * Constructor with parameters.
     * @param orgIn The org
     * @param tokenIn The token
     */
    public RegTokenOrgDefault(Org orgIn, Token tokenIn) {
        org = orgIn;
        token = tokenIn;
    }

    /**
     * @return Returns the orgId.
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * @param orgIdIn The orgIn to set.
     */
    public void setOrg(Long orgIdIn) {
        orgId = orgIdIn;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn The org to set.
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /**
     * @return Returns the token.
     */
    public Token getToken() {
        return token;
    }

    /**
     * @param tokenIn The token to set.
     */
    public void setToken(Token tokenIn) {
        token = tokenIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof RegTokenOrgDefault that)) {
            return false;

        }
        return Objects.equals(orgId, that.orgId) &&
                Objects.equals(org, that.org) && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId, org, token);
    }

    @Override
    public String toString() {
        return "RegTokenOrgDefault{" +
                "orgId=" + orgId +
                ", org=" + org +
                ", token=" + token +
                '}';
    }
}
