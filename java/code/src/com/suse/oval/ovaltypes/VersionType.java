package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class VersionType {
    @XmlValue
    private String value;
    @XmlAttribute(name = "operation", required = true)
    private OperationEnumeration operation;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OperationEnumeration getOperation() {
        return operation;
    }

    public void setOperation(OperationEnumeration operation) {
        this.operation = operation;
    }
}