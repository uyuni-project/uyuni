package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * This represents the epoch, version, and release fields as a single version string. It has the form "EPOCH:VERSION-RELEASE".
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class EVRType {
    @XmlValue
    private String value;
    @XmlAttribute(name = "datatype")
    private EVRDataTypeEnum datatype;
    @XmlAttribute(name = "operation", required = true)
    private OperationEnumeration operation;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public EVRDataTypeEnum getDatatype() {
        return datatype;
    }

    public void setDatatype(EVRDataTypeEnum datatype) {
        this.datatype = datatype;
    }

    public OperationEnumeration getOperation() {
        return operation;
    }

    public void setOperation(OperationEnumeration operation) {
        this.operation = operation;
    }
}
