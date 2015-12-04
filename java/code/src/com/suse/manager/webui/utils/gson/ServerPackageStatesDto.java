/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.utils.gson;

import java.util.List;

/**
 * Container object holding a reference to a server and a list of package states.
 */
public class ServerPackageStatesDto {

    private long sid;
    private List<PackageStateDto> packageStates;

    /**
     * @return the server id
     */
    public long getServerId() {
        return sid;
    }

    /**
     * @return the package states
     */
    public List<PackageStateDto> getPackageStates() {
        return packageStates;
    }
}
