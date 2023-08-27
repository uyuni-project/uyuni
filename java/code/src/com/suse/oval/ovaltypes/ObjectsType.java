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

import com.suse.oval.ovaltypes.linux.DpkginfoObject;
import com.suse.oval.ovaltypes.linux.RpminfoObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * The ObjectsType is a container for one or more object child elements.
 * <p>
 * Each object element provides details that define a unique set of matching items to be used by an OVAL Test.
 * Please refer to the description of the object element for more information about an individual object.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectsType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class ObjectsType {

    @XmlElements({
        @XmlElement(name = "rpminfo_object",
                namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux", type = RpminfoObject.class),
        @XmlElement(name = "dpkginfo_object",
                namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux", type = DpkginfoObject.class),
        @XmlElement(name = "object",
                namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", type = ObjectType.class)
    })
    protected List<ObjectType> objects;

    /**
     * Gets the list of contained objects.
     * @return the objects
     */
    public List<ObjectType> getObjects() {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        return this.objects;
    }

}
