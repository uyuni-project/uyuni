package com.suse.oval.ovaltypes;

import com.suse.oval.OsFamily;
import com.suse.oval.manager.OVALLookupHelper;
import com.suse.oval.vulnerablepkgextractor.DebianVulnerablePackagesExtractor;
import com.suse.oval.vulnerablepkgextractor.ProductVulnerablePackages;
import com.suse.oval.vulnerablepkgextractor.SUSEVulnerablePackageExtractor;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackagesExtractor;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackagesExtractors;
import com.suse.oval.vulnerablepkgextractor.redhat.RedHatVulnerablePackageExtractorFromPatchDefinition;
import com.suse.oval.vulnerablepkgextractor.redhat.RedHatVulnerablePackageExtractorFromVulnerabilityDefinition;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "oval_definitions", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class OvalRootType {

    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5", required = true)
    protected GeneratorType generator;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected DefinitionsType definitions;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected TestsType tests;
    @XmlElement(name = "objects", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected ObjectsType objects;
    @XmlElement(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected StatesType states;
    @Transient
    protected OsFamily osFamily;
    @Transient
    protected String osVersion;

    /**
     * Gets the value of the generator property.
     */
    public GeneratorType getGenerator() {
        return generator;
    }

    /**
     * Sets the value of the generator property.
     */
    public void setGenerator(GeneratorType value) {
        this.generator = value;
    }

    /**
     * Gets the value of the definitions property.
     */
    public List<DefinitionType> getDefinitions() {
        if (definitions == null) {
            return new ArrayList<>();
        } else {
            return definitions.getDefinitions();
        }
    }

    /**
     * Sets the value of the definitions' property.
     */
    public void setDefinitions(List<DefinitionType> value) {
        this.definitions = new DefinitionsType();
        this.definitions.definitions.addAll(value);
    }

    /**
     * Gets the value of the tests property.
     */
    public TestsType getTests() {
        return tests;
    }

    /**
     * Sets the value of the tests property.
     */
    public void setTests(TestsType value) {
        this.tests = value;
    }

    /**
     * Gets the value of the objects property.
     */
    public ObjectsType getObjects() {
        return objects;
    }

    /**
     * Sets the value of the objects property.
     */
    public void setObjects(ObjectsType value) {
        this.objects = value;
    }

    /**
     * Gets the value of the states property.
     */
    public StatesType getStates() {
        return states;
    }

    /**
     * Sets the value of the states property.
     */
    public void setStates(StatesType value) {
        this.states = value;
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
