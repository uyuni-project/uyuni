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
 * The ObjectRefType defines an object reference to be used by OVAL Tests that are defined in the component schemas.
 * The required object_ref attribute specifies the id of the OVAL Object being referenced.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectRefType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class ObjectRefType {

    @XmlAttribute(name = "object_ref", required = true)
    protected String objectRef;

    /**
     * Gets the value of the objectRef property.
     */
    public String getObjectRef() {
        return objectRef;
    }

    /**
     * Sets the value of the objectRef property.
     */
    public void setObjectRef(String value) {
        this.objectRef = value;
    }

}
