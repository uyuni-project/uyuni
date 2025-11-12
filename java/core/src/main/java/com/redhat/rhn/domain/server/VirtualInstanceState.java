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
package com.redhat.rhn.domain.server;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * VirtualInstanceState
 */
@Entity
@Table(name = "rhnVirtualInstanceState")
@Immutable
@Cache(usage = READ_ONLY)
public class VirtualInstanceState extends BaseDomainHelper {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String label;

    VirtualInstanceState() {
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long idIn) {
        id = idIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String labelIn) {
        label = labelIn;
    }


    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof VirtualInstanceState that)) {
            return false;
        }
        return new EqualsBuilder().append(this.getName(), that.getName())
                .append(this.getLabel(), that.getLabel()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(getLabel()).toHashCode();
    }
}
