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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * DamonConfigAction - Class representation of the table rhnActionDaemonConfig.
 *
 */
@Entity
@Table(name = "rhnActionDaemonConfig")
public class DaemonConfigDetails extends BaseDomainHelper {

    @Id
    @Column(name = "action_id")
    private Long actionId;

    @Column
    private Long interval;

    @Column
    private String restart;

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
     * Getter for interval
     * @return Long to get
    */
    public Long getInterval() {
        return this.interval;
    }

    /**
     * Setter for interval
     * @param intervalIn to set
    */
    public void setInterval(Long intervalIn) {
        this.interval = intervalIn;
    }

    /**
     * Getter for restart
     * @return String to get
    */
    public String getRestart() {
        return this.restart;
    }

    /**
     * Setter for restart
     * @param restartIn to set
    */
    public void setRestart(String restartIn) {
        this.restart = restartIn;
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
