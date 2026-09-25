/*
 * Copyright (c) 2023 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.scc.model;

import java.util.List;

/**
 * This is an object that matches the expected response from an SCC organization system update.
 */
public class SCCOrganizationSystemsUpdateResponse {

    private List<SCCSystemCredentialsJson> systems;

    /**
     * Constructor
     * @param systemsIn the systems
     */
    public SCCOrganizationSystemsUpdateResponse(List<SCCSystemCredentialsJson> systemsIn) {
        this.systems = systemsIn;
    }

    /**
     * @return Returns the systems
     */
    public List<SCCSystemCredentialsJson> getSystems() {
        return systems;
    }
}
