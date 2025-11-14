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

import com.suse.manager.webui.utils.gson.ChannelsJson;

import java.util.List;

/**
 * Represent the possible channels for a migration target
 * @param baseChannelTrees the possible base channels
 * @param mandatoryMap the information about which channels are mandatory for each possible base
 * @param reversedMandatoryMap the information about which channels have a specific channel as mandatory.
 * @param systemsData the description of the systems involved in the product migration
 */
public record MigrationChannelsSelection(
    List<ChannelsJson> baseChannelTrees,
    List<Object[]> mandatoryMap,
    List<Object[]> reversedMandatoryMap,
    List<MigrationSystemData> systemsData
) { }
