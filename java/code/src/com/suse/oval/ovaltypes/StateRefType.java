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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * The StateRefType defines a state reference to be used by OVAL Tests that are defined in the component schemas.
 * The required state_ref attribute specifies the id of the OVAL State being referenced.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StateRefType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class StateRefType {

    @XmlAttribute(name = "state_ref", required = true)
    protected String stateRef;

    /**
     * Gets the value of the stateRef property.
     */
    public String getStateRef() {
        return stateRef;
    }

    /**
     * Sets the value of the stateRef property.
     * @param value the state id
     */
    public void setStateRef(String value) {
        this.stateRef = value;
    }

}
