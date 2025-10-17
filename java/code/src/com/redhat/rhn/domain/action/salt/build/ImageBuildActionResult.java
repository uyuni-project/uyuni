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
package com.redhat.rhn.domain.action.salt.build;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * ImageBuildActionResult
 */
@Entity
@Table(name = "rhnActionImageBuildResult")
@IdClass(ImageBuildActionResultId.class)
public class ImageBuildActionResult implements Serializable {

    @Id
    @Column(name = "server_id")
    private Long serverId;

    @Id
    @Column(name = "action_image_build_id")
    private Long actionImageBuildId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_image_build_id", nullable = true, insertable = false, updatable = false)
    private ImageBuildActionDetails parentActionDetails;

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
     * @return the actionApplyStatesId
     */
    public Long getActionImageBuildId() {
        return actionImageBuildId;
    }

    /**
     * @param actionId the actionApplyStatesId to set.
     */
    public void setActionImageBuildId(Long actionId) {
        this.actionImageBuildId = actionId;
    }

    /**
     * @return the parentActionDetails
     */
    public ImageBuildActionDetails getParentScriptActionDetails() {
        return parentActionDetails;
    }

    /**
     * @param parentActionDetailsIn the parentActionDetails to set
     */
    public void setParentScriptActionDetails(
            ImageBuildActionDetails parentActionDetailsIn) {
        this.parentActionDetails = parentActionDetailsIn;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageBuildActionResult result)) {
            return false;
        }
        return new EqualsBuilder()
                .append(this.getActionImageBuildId(), result.getActionImageBuildId())
                .append(this.getServerId(), result.getServerId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getActionImageBuildId())
                .append(getServerId())
                .toHashCode();
    }
}
