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
    private String fqdn;
    private boolean defaultHub;
    private int knownOrgs;
    private int unmappedOrgs;

    public IssV3HubResponse(String fqdnIn, boolean defaultHubIn, int knownOrgsIn, int unmappedOrgsIn) {
        fqdn = fqdnIn;
        defaultHub = defaultHubIn;
        knownOrgs = knownOrgsIn;
        unmappedOrgs = unmappedOrgsIn;
    }

    public IssV3HubResponse() {}

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdnIn) {
        fqdn = fqdnIn;
    }

    public boolean isDefaultHub() {
        return defaultHub;
    }

    public void setDefaultHub(boolean defaultHubIn) {
        defaultHub = defaultHubIn;
    }

    public int getKnownOrgs() {
        return knownOrgs;
    }

    public void setKnownOrgs(int knownOrgsIn) {
        knownOrgs = knownOrgsIn;
    }

    public int getUnmappedOrgs() {
        return unmappedOrgs;
    }

    public void setUnmappedOrgs(int unmappedOrgsIn) {
        unmappedOrgs = unmappedOrgsIn;
    }
}
