/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.manager.webui.utils.gson;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class SystemScheduledRequestJson extends ScheduledRequestJson {
    private final List<Long> serverIds;

    /**
     * Default constructor
     * @param serverIdsIn the list of servers
     */
    public SystemScheduledRequestJson(List<Long> serverIdsIn) {
        this.serverIds = serverIdsIn;
    }

    public List<Long> getServerIds() {
        return serverIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemScheduledRequestJson)) {
            return false;
        }
        SystemScheduledRequestJson that = (SystemScheduledRequestJson) o;

        return Objects.equals(serverIds, that.serverIds) &&
            Objects.equals(getEarliest(), that.getEarliest()) &&
            Objects.equals(getActionChain(), that.getActionChain());
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverIds, getEarliest(), getActionChain());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SystemScheduledRequestJson.class.getSimpleName() + "[", "]")
            .add("serverIds=" + getServerIds())
            .add("earliest=" + getEarliest())
            .add("actionChain=" + getActionChain())
            .toString();
    }
}

