/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.Labeled;

import java.util.Arrays;
import java.util.Optional;

public enum ChannelSyncStatus implements Labeled {
    CREATED("C"),
    SYNCING("S"),
    READY("R");

    private final String label;

    ChannelSyncStatus(String labelIn) {
        label = labelIn;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return LocalizationService.getInstance().getMessage("channel.jsp.sync.status." + name().toLowerCase());
    }

    /**
     * @param labelIn the label
     * @return returns the enum type for the given value
     */
    public static Optional<ChannelSyncStatus> fromValue(String labelIn) {
        return Arrays.stream(ChannelSyncStatus.values())
                .filter(e -> e.getLabel().equals(labelIn))
                .findFirst();
    }
}
