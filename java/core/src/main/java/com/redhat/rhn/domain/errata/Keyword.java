/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.errata;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Keyword
 */
@Entity
@Table(name = "rhnErrataKeyword")
@IdClass(KeywordId.class)
public class Keyword extends BaseDomainHelper implements Serializable {
    /**
     * A keyword signaling that a system reboot is advisable following the application of the errata.
     * Typical example is if the errata requires kernel update.
     * */
    public static final String REBOOT_SUGGESTED = "reboot_suggested";

    /**
     * A keyword signaling that a reboot of the package manager is advisable following the application of
     * the errata. This is commonly used to address update stack issues before proceeding with other updates.
     * */
    public static final String RESTART_SUGGESTED = "restart_suggested";

    @Id
    private String keyword;

    @Id
    @ManyToOne(targetEntity = Errata.class)
    @JoinColumn(name = "errata_id")
    private Errata errata;

    /**
     * @return Returns the keyword.
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * @param k The keyword to set.
     */
    public void setKeyword(String k) {
        this.keyword = k;
    }

    /**
     * @return Returns the errata.
     */
    public Errata getErrata() {
        return errata;
    }

    /**
     * @param errataIn The errata to set.
     */
    public void setErrata(Errata errataIn) {
        this.errata = errataIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return keyword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Keyword castOther)) {
            return false;
        }
        return new EqualsBuilder().append(keyword, castOther.keyword)
                                  .append(errata, castOther.errata)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(keyword)
                                    .append(errata)
                                    .toHashCode();
    }
}
