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

package com.redhat.rhn.domain.action.salt;

import java.io.Serializable;
import java.util.Objects;

public class ApplyStatesActionResultId implements Serializable {
    private Long serverId;
    private Long actionApplyStatesId;

    /**
     * Constructor
     */
    public ApplyStatesActionResultId() {
    }

    /**
     * Constructor
     *
     * @param serverIdIn            the server  id
     * @param actionApplyStatesIdIn the action apply states id
     */
    public ApplyStatesActionResultId(Long serverIdIn, Long actionApplyStatesIdIn) {
        serverId = serverIdIn;
        actionApplyStatesId = actionApplyStatesIdIn;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverIdIn) {
        serverId = serverIdIn;
    }

    public Long getActionApplyStatesId() {
        return actionApplyStatesId;
    }

    public void setActionApplyStatesId(Long actionApplyStatesIdIn) {
        actionApplyStatesId = actionApplyStatesIdIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ApplyStatesActionResultId that)) {
            return false;
        }
        return Objects.equals(serverId, that.serverId) &&
                Objects.equals(actionApplyStatesId, that.actionApplyStatesId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, actionApplyStatesId);
    }
}
