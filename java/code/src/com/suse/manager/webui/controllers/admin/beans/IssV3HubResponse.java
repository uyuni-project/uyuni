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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers.admin.beans;

public class IssV3HubResponse {
    private final String fqdn;
    private final boolean defaultHub;
    private final int knownOrgs;
    private final int unmappedOrgs;

    /**
     * Response for the hub details
     * @param fqdnIn
     * @param defaultHubIn
     * @param knownOrgsIn
     * @param unmappedOrgsIn
     */
    public IssV3HubResponse(String fqdnIn, boolean defaultHubIn, int knownOrgsIn, int unmappedOrgsIn) {
        fqdn = fqdnIn;
        defaultHub = defaultHubIn;
        knownOrgs = knownOrgsIn;
        unmappedOrgs = unmappedOrgsIn;
    }

    public String getFqdn() {
        return fqdn;
    }

    public boolean isDefaultHub() {
        return defaultHub;
    }

    public int getKnownOrgs() {
        return knownOrgs;
    }

    public int getUnmappedOrgs() {
        return unmappedOrgs;
    }
}
