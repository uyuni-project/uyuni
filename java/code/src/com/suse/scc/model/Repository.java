/**
 * Copyright (c) 2014 SUSE
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
package com.suse.scc.model;

import com.google.gson.annotations.SerializedName;

/**
 * This is a SUSE repository as parsed from JSON coming in from SCC.
 */
public class Repository {

    private int id;
    private String name;
    @SerializedName("distro_target")
    private String distroTarget;
    private String url;
    private boolean autorefresh;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the distroTarget
     */
    public String getDistroTarget() {
        return distroTarget;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the autorefresh
     */
    public boolean isAutorefresh() {
        return autorefresh;
    }
}
