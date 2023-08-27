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

import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Additional generator information is also allowed although it is not part of the official OVAL Schema. Individual
 * organizations can place generator information that they feel are important and these will be skipped during the validation.
 * <p>
 * All OVAL really cares about is that the stated generator information is there.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeneratorType", namespace = "http://oval.mitre.org/XMLSchema/oval-common-5")
public class GeneratorType {

    @XmlElement(name = "product_name", namespace = "http://oval.mitre.org/XMLSchema/oval-common-5")
    protected String productName;
    @XmlElement(name = "product_version", namespace = "http://oval.mitre.org/XMLSchema/oval-common-5")
    protected String productVersion;
    @XmlElement(name = "schema_version", namespace = "http://oval.mitre.org/XMLSchema/oval-common-5", required = true)
    protected BigDecimal schemaVersion;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-common-5", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestamp;
    @XmlAnyElement
    protected List<Element> any;

    /**
     * Gets the value of the productName property.
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the value of the productName property.
     */
    public void setProductName(String value) {
        this.productName = value;
    }

    /**
     * Gets the value of the productVersion property.
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Sets the value of the productVersion property.
     */
    public void setProductVersion(String value) {
        this.productVersion = value;
    }

    /**
     * Gets the value of the schemaVersion property.
     */
    public BigDecimal getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Sets the value of the schemaVersion property.
     */
    public void setSchemaVersion(BigDecimal value) {
        this.schemaVersion = value;
    }

    /**
     * Gets the value of the timestamp property.
     */
    public XMLGregorianCalendar getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     */
    public void setTimestamp(XMLGregorianCalendar value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the any property.
     */
    public List<Element> getAny() {
        if (any == null) {
            any = new ArrayList<>();
        }
        return this.any;
    }

}
