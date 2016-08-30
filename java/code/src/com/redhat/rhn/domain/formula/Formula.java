/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.formula;

import java.util.Map;

public class Formula {

    public String name;
    public String description;
    public String group;

    public Formula(String name) {
        this.name = name;
    }

    public void setMetadata(Map<String, Object> metadata) {
        description = (String) metadata.getOrDefault("description", "");
        group = (String) metadata.getOrDefault("group", "");
    }
}
