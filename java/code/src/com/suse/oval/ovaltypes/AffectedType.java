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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * Please note that the AffectedType will change in future versions of OVAL in order to support the
 * Common Platform Enumeration (CPE).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AffectedType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class AffectedType {
    @XmlElement(name = "platform", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected List<String> platforms;
    @XmlElement(name = "product", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected List<String> products;
    @XmlAttribute(name = "family", required = true)
    protected FamilyEnum family;

    /**
     * Gets the value of the list of affected platforms
     */
    public List<String> getPlatforms() {
        if (platforms == null) {
            platforms = new ArrayList<>();
        }
        return this.platforms;
    }

    /**
     * Gets the value of the list of affected products
     */
    public List<String> getProducts() {
        if (products == null) {
            products = new ArrayList<>();
        }
        return this.products;
    }

    /**
     * Gets the value of the family property.
     */
    public FamilyEnum getFamily() {
        return family;
    }

    /**
     * Sets the value of the family property.
     */
    public void setFamily(FamilyEnum value) {
        this.family = value;
    }

}
