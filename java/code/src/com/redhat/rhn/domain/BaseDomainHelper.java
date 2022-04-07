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

package com.redhat.rhn.domain;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Class UserImpl that reflects the DB representation of web_contact
 * and ancillary tables.
 * DB table: web_contact
 */
@MappedSuperclass
public abstract class BaseDomainHelper {
    private Date created = new Date();
    private Date modified;

    /**
     * Create a new empty object
     */
    protected BaseDomainHelper() {
    }


    /**
     * Gets the current value of created
     * @return Date the current value
     */
    @Column(name = "created", nullable = false, updatable = false)
    @CreationTimestamp
    public Date getCreated() {
        return this.created;
    }

    /**
     * Sets the value of created to new value
     * @param createdIn New value for created
     */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /**
     * Gets the current value of modified
     * @return Date the current value
     */
    @Column(name = "modified")
    @UpdateTimestamp
    public Date getModified() {
        return this.modified;
    }

    /**
     * Sets the value of modified to new value
     * @param modifiedIn New value for modified
     */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
