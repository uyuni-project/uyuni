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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class AdvisoryAffectedType {
    @XmlElement(name = "resolution", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private AdvisoryResolutionType resolution;

    /**
     * Gets the resolution
     *
     * @return the resolution
     * */
    public AdvisoryResolutionType getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution type
     *
     * @param resolutionIn the resolution to set
     * */
    public void setResolution(AdvisoryResolutionType resolutionIn) {
        this.resolution = resolutionIn;
    }
}
