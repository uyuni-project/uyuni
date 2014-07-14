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

package com.suse.mgrsync;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Channel family class.
 */
@Root(name = "family")
public class MgrSyncChannelFamily {
    @Attribute(name = "default_nodecount")
    private Integer defaultNodeCount;

    @Attribute
    private String label;

    @Attribute
    private String name;

    public Integer getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }
}
