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

import com.suse.oval.OsFamily;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * The required id attribute is the OVAL-ID of the Definition. The form of an OVAL-ID must follow the specific format
 * described by the oval:DefinitionIDPattern.
 * <p>
 * The required version attribute holds the current version of the definition.
 * <p>
 * Versions are integers, starting at 1 and incrementing every time a definition is modified. The required class
 * attribute indicates the specific class to which the definition belongs. The class gives a hint to a user,
 * so they can know what the definition writer is trying to say. See the definition of oval-def:ClassEnumeration
 * for more information about the different valid classes.
 * <p>
 * The optional deprecated attribute signifies that an id is no longer to be used or referenced but the information
 * has been kept around for historic purposes.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "definition", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class DefinitionType {

    @XmlElement(name = "metadata", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", required = true)
    protected MetadataType metadata;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected CriteriaType criteria;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "version", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger version;
    @XmlAttribute(name = "class", required = true)
    protected DefinitionClassEnum definitionClass;

    @Transient
    protected List<String> cves = new ArrayList<>();
    @Transient
    protected OsFamily osFamily;
    @Transient
    private String osVersion;

    /**
     * Gets the value of the metadata property.
     *
     * @return possible object is
     * {@link MetadataType }
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     */
    public void setMetadata(MetadataType value) {
        this.metadata = value;
    }

    public CriteriaType getCriteria() {
        return criteria;
    }

    public void setCriteria(CriteriaType criteria) {
        this.criteria = criteria;
    }

    /**
     * Gets the value of the id property.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * @param valueIn the id to set
     */
    public void setId(String valueIn) {
        this.id = valueIn;
    }

    /**
     * Gets the value of the version property.
     * @return the version
     */
    public BigInteger getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * @param valueIn the version to set
     */
    public void setVersion(BigInteger valueIn) {
        this.version = valueIn;
    }

    /**
     * Gets the value of the clazz property.
     * @return the definition class
     */
    public DefinitionClassEnum getDefinitionClass() {
        return definitionClass;
    }

    /**
     * Sets the value of the clazz property.
     * @param value the definition class to set
     */
    public void setDefinitionClass(DefinitionClassEnum value) {
        this.definitionClass = value;
    }

    public Optional<String> getSingleCve() {
        if (cves.isEmpty()) {
            return Optional.empty();
        }
        return cves.stream().findFirst();
    }

    public void setSingleCve(String cve) {
        this.cves.clear();
        this.cves.add(cve);
    }

    public List<String> getCves() {
        return cves;
    }

    public void setCves(List<String> cvesIn) {
        this.cves = cvesIn;
    }

    public OsFamily getOsFamily() {
        return osFamily;
    }

    public void setOsFamily(OsFamily osFamilyIn) {
        this.osFamily = osFamilyIn;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersionIn) {
        this.osVersion = osVersionIn;
    }
}
