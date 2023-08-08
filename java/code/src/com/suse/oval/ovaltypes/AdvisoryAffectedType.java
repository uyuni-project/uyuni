package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class AdvisoryAffectedType {
    @XmlElement(name = "resolution", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private AdvisoryResolutionType resolution;

    public AdvisoryResolutionType getResolution() {
        return resolution;
    }

    public void setResolution(AdvisoryResolutionType resolution) {
        this.resolution = resolution;
    }
}
