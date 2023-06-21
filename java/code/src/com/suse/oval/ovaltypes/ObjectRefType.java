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
