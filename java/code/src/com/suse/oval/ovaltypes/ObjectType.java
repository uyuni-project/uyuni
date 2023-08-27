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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;


/**
 * The required id attribute uniquely identifies each object, and must conform to the format specified by the
 * ObjectIdPattern simple type. The required version attribute holds the current version of the object element.
 * <p>
 * Versions are integers, starting at 1 and incrementing every time an object is modified. The optional comment
 * attribute provides a short description of the object.
 * <p>
 * The optional deprecated attribute signifies that an id is no longer to be used or referenced but the information
 * has been kept around for historic purposes.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectType", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class ObjectType {

    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "version", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger version;
    @XmlAttribute(name = "comment")
    protected String comment;
    @XmlAttribute(name = "deprecated")
    protected Boolean deprecated;


    // These attributes are not specified for the base object type as per the schema; nevertheless,
    // they have been included since both dpkg and rpm objects have them.
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5#linux")
    protected String name;

    /**
     * Gets the value of the id property.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * @param valueIn the object id to set
     */
    public void setId(String valueIn) {
        this.id = valueIn;
    }

    /**
     * Gets the value of the version property.
     */
    public BigInteger getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     */
    public void setVersion(BigInteger valueIn) {
        this.version = valueIn;
    }

    /**
     * Gets the value of the comment property.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     */
    public void setComment(String valueIn) {
        this.comment = valueIn;
    }

    /**
     * Gets the value of the deprecated property.
     */
    public boolean isDeprecated() {
        if (deprecated == null) {
            return false;
        }
        else {
            return deprecated;
        }
    }

    /**
     * Sets the value of the deprecated property.
     */
    public void setDeprecated(Boolean valueIn) {
        this.deprecated = valueIn;
    }

    /**
     * Returns the package name.
     */
    public String getPackageName() {
        return name;
    }

    public void setPackageName(String nameIn) {
        this.name = nameIn;
    }

    public boolean isDpkg() {
        return this instanceof DpkginfoObject;
    }

    public boolean isRpm() {
        return this instanceof RpminfoObject;
    }
}
