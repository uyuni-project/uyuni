/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.action.server;

import com.redhat.rhn.domain.action.Action;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class ServerActionId implements Serializable {

    @Serial
    private static final long serialVersionUID = 4061490449078941309L;

    private Long serverId;

    private Action parentAction;

    /**
     * Constructor
     */
    public ServerActionId() {
    }

    /**
     * Constructor
     *
     * @param serverIdIn     the input serverId
     * @param parentActionIn the input parentAction
     */
    public ServerActionId(Long serverIdIn, Action parentActionIn) {
        serverId = serverIdIn;
        parentAction = parentActionIn;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverIdIn) {
        serverId = serverIdIn;
    }

    public Action getParentAction() {
        return parentAction;
    }

    public void setParentAction(Action parentActionIn) {
        parentAction = parentActionIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ServerActionId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(serverId, that.serverId)
                .append(parentAction, that.parentAction)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(serverId)
                .append(parentAction)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ServerAction{" +
                "serverId=" + serverId +
                ", parentAction=" + parentAction +
                '}';
    }
}
