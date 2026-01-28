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
package com.suse.manager.webui.controllers.appstreams;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class SsmAppStreamsChangesJson {
    private Long channelId;
    private Set<String> toEnable;
    private Set<String> toDisable;
    private String actionChainLabel;
    private Optional<LocalDateTime> earliest = Optional.empty();

    public Long getChannelId() {
        return channelId;
    }

    public Set<String> getToEnable() {
        return (toEnable != null) ? toEnable : Collections.emptySet();
    }

    public Set<String> getToDisable() {
        return (toDisable != null) ? toDisable : Collections.emptySet();
    }

    public Optional<String> getActionChainLabel() {
        return Optional.ofNullable(actionChainLabel);
    }

    public Optional<LocalDateTime> getEarliest() {
        return earliest;
    }
}
