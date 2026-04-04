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

package com.suse.manager.webui.controllers.admin.beans;

import com.suse.manager.model.hub.OrgInfoJson;

import java.util.List;

public record ChannelSyncModel(
    List<OrgInfoJson> peripheralOrgs,
    List<ChannelSyncDetail> channels,
    List<Object[]> mandatoryMap,
    List<Object[]> reversedMandatoryMap
) { }
