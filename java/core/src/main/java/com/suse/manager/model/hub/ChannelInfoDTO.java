/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.model.hub;

/**
 * Simple DTO for {@link com.redhat.rhn.domain.channel.Channel}.
 *
 * @param id the channel ID
 * @param name the channel name
 * @param label the channel label
 * @param parentId the parent channel ID, or {@code null} if this is a base channel
 * @param architecture the architecture name
 * @param orgId the organization ID if this is a custom channel, or {@code null} otherwise
 * @param orgName the organization name if this is a custom channel, or {@code null} otherwise
 * @param originalId the original channel ID if this is a clone, or {@code null} otherwise
 */
public record ChannelInfoDTO(
    long id,
    String name,
    String label,
    Long parentId,
    String architecture,
    Long orgId,
    String orgName,
    Long originalId) {
}
