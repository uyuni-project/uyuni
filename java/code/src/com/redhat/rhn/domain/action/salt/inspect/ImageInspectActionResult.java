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
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * ImageInspectActionResult
 */
@Entity
@Table(name = "rhnActionImageInspectResult")
public class ImageInspectActionResult implements Serializable {

    @Embeddable
    public static class ImageInspectActionResultId implements Serializable {

        @Column(name = "server_id")
        private Long serverId;

        @Column(name = "action_image_inspect_id")
        private Long actionImageInspectId;

        /**
         * @return the actionImageInspectId
         */
        public Long getActionImageInspectId() {
            return actionImageInspectId;
        }

        /**
         * @param actionImageInspectIdIn the actionImageInspectId to set.
         */
        public void setActionImageInspectId(Long actionImageInspectIdIn) {
            actionImageInspectId = actionImageInspectIdIn;
        }

        /**
         * @return the serverId
         */
        public Long getServerId() {
            return serverId;
        }

        /**
         * @param serverIdIn serverId to set
         */
        public void setServerId(Long serverIdIn) {
            serverId = serverIdIn;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ImageInspectActionResultId that)) {
                return false;
            }
            return Objects.equals(getServerId(), that.getServerId()) &&
                    Objects.equals(getActionImageInspectId(), that.getActionImageInspectId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getServerId(), getActionImageInspectId());
        }
    }

    @EmbeddedId
    private ImageInspectActionResultId id = new ImageInspectActionResultId();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_image_inspect_id", insertable = false, updatable = false)
    private ImageInspectActionDetails parentScriptActionDetails;


    /**
     * @return the Id
     */
    public ImageInspectActionResultId getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(ImageInspectActionResultId idIn) {
        id = idIn;
    }

    /**
     * @return the serverId
     */
    public Long getServerId() {
        return this.getId().getServerId();
    }

    /**
     * @param sid serverId to set
     */
    public void setServerId(Long sid) {
        this.getId().setServerId(sid);
    }

    /**
     * @return the actionImageInspectId
     */
    public Long getActionImageInspectId() {
        return this.getId().getActionImageInspectId();
    }

    /**
     * @param actionId the actionImageInspectId to set.
     */
    public void setActionImageInspectId(Long actionId) {
        this.getId().setActionImageInspectId(actionId);
    }

    /**
     * @return the parentActionDetails
     */
    public ImageInspectActionDetails getParentScriptActionDetails() {
        return parentScriptActionDetails;
    }

    /**
     * @param parentActionDetailsIn the parentActionDetails to set
     */
    public void setParentScriptActionDetails(
            ImageInspectActionDetails parentActionDetailsIn) {
        this.parentScriptActionDetails = parentActionDetailsIn;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageInspectActionResult result)) {
            return false;
        }
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
