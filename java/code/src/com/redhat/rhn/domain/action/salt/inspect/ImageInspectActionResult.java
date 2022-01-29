/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.action.salt.inspect;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * ImageInspectActionResult
 */
public class ImageInspectActionResult implements Serializable {

    private Long serverId;
    private Long actionImageInspectId;

    private ImageInspectActionDetails parentActionDetails;

    /**
     * @return the serverId
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * @param sid serverId to set
     */
    public void setServerId(Long sid) {
        this.serverId = sid;
    }

    /**
     * @return the actionImageInspectId
     */
    public Long getActionImageInspectId() {
        return actionImageInspectId;
    }

    /**
     * @param actionId the actionImageInspectId to set.
     */
    public void setActionImageInspectId(Long actionId) {
        this.actionImageInspectId = actionId;
    }

    /**
     * @return the parentActionDetails
     */
    public ImageInspectActionDetails getParentScriptActionDetails() {
        return parentActionDetails;
    }

    /**
     * @param parentActionDetailsIn the parentActionDetails to set
     */
    public void setParentScriptActionDetails(
            ImageInspectActionDetails parentActionDetailsIn) {
        this.parentActionDetails = parentActionDetailsIn;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ImageInspectActionResult)) {
            return false;
        }

        ImageInspectActionResult result = (ImageInspectActionResult) obj;

        return new EqualsBuilder()
                .append(this.getActionImageInspectId(), result.getActionImageInspectId())
                .append(this.getServerId(), result.getServerId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getActionImageInspectId())
                .append(getServerId())
                .toHashCode();
    }
}
