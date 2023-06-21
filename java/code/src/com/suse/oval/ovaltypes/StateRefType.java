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
     *
     * @return the contained state id
     */
    public String getStateRef() {
        return stateRef;
    }

    /**
     * Sets the value of the stateRef property.
     */
    public void setStateRef(String value) {
        this.stateRef = value;
    }

}
