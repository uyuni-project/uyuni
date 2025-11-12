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

package com.redhat.rhn.manager.ssm.channelchange;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.manager.ssm.ChannelChangeDto;

import java.util.Optional;
import java.util.Set;

/**
 * Factory for creating {@link ChannelChange}
 */
public final class ChannelChangeFactory {

    private ChannelChangeFactory() {
        // Prevent instantiation
    }

    /**
     * Creates the {@link ChannelChange} corresponding to the given set of {@link ChannelChangeDto}.
     * @param changes the set of changes
     * @return the {@link ChannelChange} represented by the set of {@link ChannelChangeDto}, empty otherwise.
     */
    public static Optional<ChannelChange> parseChanges(Set<ChannelChangeDto> changes) {
        return parseChanges(changes, null);
    }

    /**
     * Creates the {@link ChannelChange} corresponding to the given set of {@link ChannelChangeDto}.
     * @param changes the set of changes
     * @param baseChannel  the current base channel, or null if no base is currently defined.
     * @return the {@link ChannelChange} represented by the set of {@link ChannelChangeDto}, empty otherwise.
     */
    public static Optional<ChannelChange> parseChanges(Set<ChannelChangeDto> changes, Channel baseChannel) {
        // First check if it's a change from a default base
        return defaultBaseChange(changes)
            // then try an explicit base change from the given base channel
            .or(() -> explicitBaseChange(changes, baseChannel))
            // child channels changes are only possible in case the system has a base channel
            .or(() -> baseChannel != null ? onlyChildChannelsChange(changes) : Optional.empty());
    }

    private static Optional<ChannelChange> defaultBaseChange(Set<ChannelChangeDto> changes) {
        DefaultBaseChange defaultBaseChange = new DefaultBaseChange();

        boolean allMatch = changes.stream()
            .allMatch(ch -> {
                if (ch.getNewBaseId().isPresent() && ch.isNewBaseDefault()) {
                    defaultBaseChange.addChange(ch.getNewBaseId().get(), ch.getChildChannelActions());
                    return true;
                }

                return false;
            });

        return allMatch ? Optional.of(defaultBaseChange) : Optional.empty();
    }

    private static Optional<ChannelChange> explicitBaseChange(Set<ChannelChangeDto> changes, Channel base) {
        if (changes.size() != 1) {
            return Optional.empty();
        }

        return changes.stream()
            .filter(ch -> ch.getNewBaseId().isPresent() && !ch.isNewBaseDefault())
            .map(ch -> new ExplicitBaseChange(base, ch.getNewBaseId().orElseThrow(), ch.getChildChannelActions()))
            .map(ChannelChange.class::cast)
            .findFirst();
    }

    private static Optional<ChannelChange> onlyChildChannelsChange(Set<ChannelChangeDto> changes) {
        if (changes.size() != 1) {
            return Optional.empty();
        }

        return changes.stream()
            .filter(ch -> ch.getNewBaseId().isPresent() && ch.getOldBaseId().isPresent() &&
                          ch.getOldBaseId().get().equals(ch.getNewBaseId().get())
            )
            .map(ch -> new OnlyChildChannelsChange(ch.getChildChannelActions()))
            .map(ChannelChange.class::cast)
            .findFirst();
    }
}
