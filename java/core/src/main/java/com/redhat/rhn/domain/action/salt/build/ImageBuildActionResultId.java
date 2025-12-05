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

package com.redhat.rhn.domain.action.salt.build;

import java.io.Serializable;
import java.util.Objects;

public class ImageBuildActionResultId implements Serializable {
    private Long serverId;
    private Long actionImageBuildId;

    /**
     * Constructor
     */
    public ImageBuildActionResultId() {
    }

    /**
     * Constructor
     *
     * @param serverIdIn           the server id
     * @param actionImageBuildIdIn the action image build id
     */
    public ImageBuildActionResultId(Long serverIdIn, Long actionImageBuildIdIn) {
        serverId = serverIdIn;
        actionImageBuildId = actionImageBuildIdIn;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverIdIn) {
        serverId = serverIdIn;
    }

    public Long getActionImageBuildId() {
        return actionImageBuildId;
    }

    public void setActionImageBuildId(Long actionImageBuildIdIn) {
        actionImageBuildId = actionImageBuildIdIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ImageBuildActionResultId that)) {
            return false;
        }
        return Objects.equals(serverId, that.serverId) &&
                Objects.equals(actionImageBuildId, that.actionImageBuildId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, actionImageBuildId);
    }
}
