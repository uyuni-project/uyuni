package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class AdvisoryCveType {
    @XmlValue
    private String cve;

    public String getCve() {
        return cve;
    }

    public void setCve(String cve) {
        this.cve = cve;
    }
}
