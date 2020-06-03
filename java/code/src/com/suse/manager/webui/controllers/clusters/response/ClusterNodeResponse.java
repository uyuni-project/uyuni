/**
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.webui.controllers.clusters.response;

import java.util.HashMap;
import java.util.Map;

public class ClusterNodeResponse {

    private String hostname;
    private ServerResponse server;
    private Map<String, Object> details = new HashMap<>();

    /**
     * @return hostname to get
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostnameIn to set
     */
    public void setHostname(String hostnameIn) {
        this.hostname = hostnameIn;
    }

    /**
     * @return server to get
     */
    public ServerResponse getServer() {
        return server;
    }

    /**
     * @param serverIn to set
     */
    public void setServer(ServerResponse serverIn) {
        this.server = serverIn;
    }

    /**
     * @return details to get
     */
    public Map<String, Object> getDetails() {
        return details;
    }
}
