/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
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
/*
 * Copyright (c) 2010 SUSE LLC
 */
package com.redhat.rhn.domain.errata;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Bug
 */
@Entity
@Table(name = "rhnErrataBuglist")
@IdClass(BugId.class)
public class Bug extends BaseDomainHelper implements Serializable {

    @Id
    @Column(name = "bug_id")
    private Long id;

    @Id
    @ManyToOne(targetEntity = Errata.class)
    @JoinColumn(name = "errata_id")
    private Errata errata;

    @Column
    private String summary;

    @Column(name = "href")
    private String url;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param s The summary to set.
     */
    public void setSummary(String s) {
        this.summary = s;
    }

    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param urlIn The url to set.
     */
    public void setUrl(String urlIn) {
        this.url = urlIn;
    }

    /**
     * {@inheritDoc}
     */
    public Errata getErrata() {
        return errata;
    }

    /**
     * {@inheritDoc}
     */
    public void setErrata(Errata errataIn) {
        this.errata = errataIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Bug castOther)) {
            return false;
        }
        return new EqualsBuilder().append(id, castOther.id)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .toHashCode();
    }
}
