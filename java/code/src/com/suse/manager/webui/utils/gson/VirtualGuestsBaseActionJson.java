/**
 * Copyright (c) 2018 SUSE LLC
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
 * VirtualGuestsBaseAction represents the common properties for all the virtualization guests
 * action requests JSON data.
 */
public class VirtualGuestsBaseActionJson {

    private List<String> uuids;

    /**
     * @return the list of UUIDs of virtual guests to act on
     */
    public List<String> getUuids() {
        return uuids;
    }

    /**
     * @param uuidsIn the list of UUIDs of virtual guests to act on
     */
    public void setUuids(List<String> uuidsIn) {
        this.uuids = uuidsIn;
    }
}
