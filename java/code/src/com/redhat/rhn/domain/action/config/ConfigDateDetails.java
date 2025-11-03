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
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/**
 * ConfigDateDetails - Class representation of the table rhnActionConfigDate.
 *
 */
@Entity
@Table(name = "rhnActionConfigDate")
public class ConfigDateDetails extends BaseDomainHelper {

    @Id
    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "import_contents")
    private String importContents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false, insertable = false)
    @MapsId
    private Action parentAction;

    /**
     * @return Returns the actionId.
     */
    public Long getActionId() {
        return actionId;
    }
    /**
     * @param actionIdIn The actionId to set.
     */
    protected void setActionId(Long actionIdIn) {
        this.actionId = actionIdIn;
    }
    /**
     * Getter for startDate
     * @return Date to get
    */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * Setter for startDate
     * @param startDateIn to set
    */
    public void setStartDate(Date startDateIn) {
        this.startDate = startDateIn;
    }

    /**
     * Getter for endDate
     * @return Date to get
    */
    public Date getEndDate() {
        return this.endDate;
    }

    /**
     * Setter for endDate
     * @param endDateIn to set
    */
    public void setEndDate(Date endDateIn) {
        this.endDate = endDateIn;
    }

    /**
     * Getter for importContents
     * @return String to get
    */
    public String getImportContents() {
        return this.importContents;
    }

    /**
     * Setter for importContents
     * @param importContentsIn to set
    */
    public void setImportContents(String importContentsIn) {
        this.importContents = importContentsIn;
    }

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
}
