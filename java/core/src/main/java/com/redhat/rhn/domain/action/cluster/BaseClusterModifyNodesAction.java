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

package com.redhat.rhn.domain.action.cluster;

import java.util.HashSet;
import java.util.Set;

public class BaseClusterModifyNodesAction extends BaseClusterAction {

    private String jsonParams;
    private Set<Long> serverIds = new HashSet<>();

    /**
     * @return jsonParams to get
     */
    public String getJsonParams() {
        return jsonParams;
    }

    /**
     * @param jsonParamsIn to set
     */
    public void setJsonParams(String jsonParamsIn) {
        this.jsonParams = jsonParamsIn;
    }

    /**
     * @return serverId to get
     */
    public Set<Long> getServerIds() {
        return serverIds;
    }

    /**
     * @param serverIdIn to set
     */
    public void setServerIds(Set<Long> serverIdIn) {
        this.serverIds = serverIdIn;
    }
}
