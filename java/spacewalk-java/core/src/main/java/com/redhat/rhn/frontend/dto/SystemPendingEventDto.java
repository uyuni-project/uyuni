/*
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
package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.domain.action.ActionFactory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * DTO for a com.redhat.rhn.frontend.action..systems.sdc.SystemPendingEventsAction
 */
public class SystemPendingEventDto extends SystemEventDto implements Serializable {

    private static final long serialVersionUID = 1277838056019707817L;

    private Date scheduledFor;
    private Long prereqAid;
    private Long prereqStatusId;
    private String actionName;

    /**
     * @return Returns date of creation
     */
    public Date getScheduledFor() {
        return scheduledFor;
    }

    /**
     * @param scheduledForIn Date of creation to set
     */
    public void setScheduledFor(Date scheduledForIn) {
        this.scheduledFor = scheduledForIn;
    }

    /**
     * @return Id of prerequisite action
     */
    public Long getPrereqAid() {
        return prereqAid;
    }

    /**
     * @param prereqAidIn prerequisite Id to set
     */
    public void setPrereqAid(Long prereqAidIn) {
        this.prereqAid = prereqAidIn;
    }

    /**
     * @return status id of the prerequisite
     */
    public Long getPrereqStatusId() {
        return prereqStatusId;
    }

    /**
     * @param prereqStatusIdIn status id to set for the prerequisite
     */
    public void setPrereqStatusId(Long prereqStatusIdIn) {
        this.prereqStatusId = prereqStatusIdIn;
    }

    /**
     * @return Action name
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * @param actionNameIn action name to set
     */
    public void setActionName(String actionNameIn) {
        this.actionName = actionNameIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectionKey() {
        return String.valueOf(getId());
    }

    /**
     * @return True if this entry should be selectable in the UI.
     */
    @Override
    public boolean isSelectable() {
        return prereqAid == null || Objects.equals(ActionFactory.STATUS_FAILED.getId(), prereqStatusId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SystemPendingEventDto that = (SystemPendingEventDto) o;

        return new EqualsBuilder().appendSuper(super.equals(o))
                                  .append(scheduledFor, that.scheduledFor)
                                  .append(prereqAid, that.prereqAid)
                                  .append(prereqStatusId, that.prereqStatusId)
                                  .append(actionName, that.actionName)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode())
                                          .append(scheduledFor)
                                          .append(prereqAid)
                                          .append(prereqStatusId)
                                          .append(actionName)
                                          .toHashCode();
    }
}
