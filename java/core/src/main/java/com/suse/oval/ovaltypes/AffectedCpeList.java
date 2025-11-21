/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.ovaltypes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class AffectedCpeList {
    @XmlElement(name = "cpe", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private List<String> cpeList;

    /**
     * Gets the list of CPEs
     *
     * @return the CPEs or an empty list if none is set
     * */
    public List<String> getCpeList() {
        if (cpeList == null) {
            return new ArrayList<>();
        }
        return cpeList;
    }

    /**
     * Sets the CPEs
     *
     * @param cpeListIn the CPEs to set
     * */
    public void setCpeList(List<String> cpeListIn) {
        this.cpeList = cpeListIn;
    }
}
