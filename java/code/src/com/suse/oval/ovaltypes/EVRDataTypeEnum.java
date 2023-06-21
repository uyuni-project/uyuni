package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-common-5")
@XmlEnum
public enum EVRDataTypeEnum {
    @XmlEnumValue("debian_evr_string")
    DEBIAN_EVR,
    @XmlEnumValue("evr_string")
    RPM_EVR
}
