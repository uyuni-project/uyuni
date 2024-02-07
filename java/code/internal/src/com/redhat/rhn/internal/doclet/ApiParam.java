/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.internal.doclet;

/**
 * Represents an API call parameter
 */
public class ApiParam {
    private String name;
    private String desc;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getDesc() {
        return desc.replace("\n", "");
    }

    public void setDesc(String descIn) {
        desc = descIn;
    }

    public String getType() {
        if (type.equals("boolean")) {
            return "bool";
        }
        return type;
    }

    public void setType(String typeIn) {
        type = typeIn;
    }

    public String getFlagName() {
        return new StringHelper().toCamelCase(name);
    }
}
