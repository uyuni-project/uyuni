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
import java.util.Objects;
import java.util.Optional;


/**
 * The required id attribute is the OVAL-ID of the Definition. The form of an OVAL-ID must follow the specific format
 * described by the oval:DefinitionIDPattern. The required version attribute holds the current version of the definition.
 * <p>
 * Versions are integers, starting at 1 and incrementing every time a definition is modified. The required class attribute
 * indicates the specific class to which the definition belongs. The class gives a hint to a user, so they can know what the definition
 * writer is trying to say. See the definition of oval-def:ClassEnumeration for more information about the different valid classes.
 * <p>
 * The optional deprecated attribute signifies that an id is no longer to be used or referenced but the information has been kept around
 * for historic purposes.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "definition", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class DefinitionType {

    @XmlElement(name = "metadata", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", required = true)
    protected MetadataType metadata;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected CriteriaType criteria;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected NotesType notes;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "version", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger version;
    @XmlAttribute(name = "class", required = true)
    protected DefinitionClassEnum definitionClass;
    @XmlAttribute(name = "deprecated")
    protected Boolean deprecated;

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
     * Gets the value of the notes property.
     */
    public NotesType getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     */
    public void setNotes(NotesType value) {
        this.notes = value;
    }

    /**
     * Gets the value of the id property.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     */
    public void setId(String value) {
        this.id = value;
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
    public void setVersion(BigInteger value) {
        this.version = value;
    }

    /**
     * Gets the value of the clazz property.
     */
    public DefinitionClassEnum getDefinitionClass() {
        return definitionClass;
    }

    /**
     * Sets the value of the clazz property.
     */
    public void setDefinitionClass(DefinitionClassEnum value) {
        this.definitionClass = value;
    }

    /**
     * Gets the value of the deprecated property.
     */
    public boolean isDeprecated() {
        return Objects.requireNonNullElse(deprecated, false);
    }

    /**
     * Sets the value of the deprecated property.
     */
    public void setDeprecated(Boolean value) {
        this.deprecated = value;
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

    public void setCves(List<String> cves) {
        this.cves = cves;
    }

    public OsFamily getOsFamily() {
        return osFamily;
    }

    public void setOsFamily(OsFamily osFamily) {
        this.osFamily = osFamily;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
}
