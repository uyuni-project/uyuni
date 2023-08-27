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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
     * Gets the list of OVAL definitions.
     */
    public List<DefinitionType> getDefinitions() {
        if (definitions == null) {
            return new ArrayList<>();
        } else {
            return definitions.getDefinitions();
        }
    }

    /**
     * Sets the list of OVAL definitions.
     */
    public void setDefinitions(List<DefinitionType> value) {
        this.definitions = new DefinitionsType();
        this.definitions.definitions = new ArrayList<>(value);
    }

    /**
     * Gets the list of OVAL tests.
     */
    public List<TestType> getTests() {
        return Optional.ofNullable(tests).map(TestsType::getTests).orElse(new ArrayList<>());
    }

    /**
     * Sets the list of OVAL tests.
     */
    public void setTests(List<TestType> value) {
        this.tests = new TestsType();
        this.tests.tests = new ArrayList<>(value);
    }

    /**
     * Gets the list of OVAL objects.
     */
    public List<ObjectType> getObjects() {
        return Optional.ofNullable(objects).map(ObjectsType::getObjects).orElse(new ArrayList<>());
    }

    /**
     * Sets the list of OVAL objects.
     */
    public void setObjects(List<ObjectType> value) {
        this.objects = new ObjectsType();
        this.objects.objects = new ArrayList<>(value);
    }

    /**
     * Gets the list of OVAL states.
     */
    public List<StateType> getStates() {
        return Optional.ofNullable(states).map(StatesType::getStates).orElse(new ArrayList<>());
    }

    /**
     * Sets the list of OVAL states.
     */
    public void setStates(List<StateType> value) {
        this.states = new StatesType();
        this.states.states = new ArrayList<>(value);
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
