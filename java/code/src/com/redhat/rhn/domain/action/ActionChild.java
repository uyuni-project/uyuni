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
package com.redhat.rhn.domain.action;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * ActionChild - Class that can be used for records that require an Action as their
 * parent.  IOTW: Tables that have a foreign key action_id.
 *
 */
@MappedSuperclass
public abstract class ActionChild implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Action parentAction;

    @Transient
    private Date created = new Date();
    @Transient
    private Date modified = new Date();

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }

    /**
     * Gets the current value of created
     * @return Date the current value
     */
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

}
