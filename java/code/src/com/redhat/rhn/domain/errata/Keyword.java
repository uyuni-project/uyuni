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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.errata;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Keyword
 */
public class Keyword extends BaseDomainHelper implements Serializable {

    private String keyword;
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
        if (!(other instanceof Keyword)) {
            return false;
        }
        Keyword castOther = (Keyword) other;
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
