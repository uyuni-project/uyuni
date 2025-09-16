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

package com.suse.manager.model.products.migration;

import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.gson.ChannelsJson;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public record MigrationScheduleRequest(
    List<Long> serverIds,
    MigrationProduct targetProduct,
    ChannelsJson targetChannelTree,
    boolean dryRun,
    boolean allowVendorChange,
    Optional<LocalDateTime> earliest,
    Optional<String> actionChain
) {

    /**
     * Retrieves the earliest date of execution associated with this schedule request
     * @return the earliest date of execution if one is associated with this request. "now" is returned otherwise.
     */
    public Date getEarliestDate() {
        return MinionActionUtils.getScheduleDate(earliest);
    }

    /**
     * Retrieves the action chain associated with this schedule request
     * @param user the user owning the action chain
     * @return the {@link ActionChain}, if one is associated with this request. <code>null</code> otherwise.
     */
    public ActionChain getActionChain(User user) {
        return MinionActionUtils.getActionChain(actionChain, user);
    }
}
