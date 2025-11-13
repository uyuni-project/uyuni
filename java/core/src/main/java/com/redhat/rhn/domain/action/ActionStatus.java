/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.action;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ActionStatus
 */
@Entity
@Table(name = "rhnActionStatus")
@Immutable
@Cache(usage = READ_ONLY)
public class ActionStatus implements Serializable {

    @Id
    private Long id;
    @Column
    private String name;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    protected void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
    }


    /**
     * @return if the status represents an action that is in its final state and considered done.
     * (either completed or failed)
     */
    public boolean isDone() {
        return isCompleted() || isFailed();
    }

    public boolean isQueued() {
        return this.equals(ActionFactory.STATUS_QUEUED);
    }

    public boolean isPickedUp() {
        return this.equals(ActionFactory.STATUS_PICKED_UP);
    }

    public boolean isCompleted() {
        return this.equals(ActionFactory.STATUS_COMPLETED);
    }

    public boolean isFailed() {
        return this.equals(ActionFactory.STATUS_FAILED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ActionStatus other)) {
            return false;
        }
        return new EqualsBuilder().append(this.getName(), other.getName())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName())
                                    .toHashCode();
    }
}
