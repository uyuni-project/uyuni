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

package com.suse.manager.webui.controllers.admin.beans;

import com.redhat.rhn.domain.org.Org;

/**
 * The organization data of a channel
 * @param orgId the organization id
 * @param orgName the organizationName
 */
public record ChannelOrg(Long orgId, String orgName) {

    /**
     * Builds an instance from an organization
     * @param org the organization
     */
    public ChannelOrg(Org org) {
        this(org.getId(), org.getName());
    }
}
