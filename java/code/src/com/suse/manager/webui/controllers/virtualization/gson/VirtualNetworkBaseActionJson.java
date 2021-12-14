/*
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
package com.suse.manager.webui.controllers.virtualization.gson;

import com.suse.manager.webui.utils.gson.ScheduledRequestJson;

import java.util.List;

/**
 * Represents the generic virtual network action request body structure.
 */
public class VirtualNetworkBaseActionJson extends ScheduledRequestJson {
    private List<String> names;

    /**
     * @return the names of the pools to action on
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * @param namesIn The poolNames to set.
     */
    public void setNames(List<String> namesIn) {
        names = namesIn;
    }
}
