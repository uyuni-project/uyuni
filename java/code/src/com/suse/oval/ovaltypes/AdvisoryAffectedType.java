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

    /**
     * Gets the resolution
     *
     * @return the resolution
     * */
    public AdvisoryResolutionType getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution type
     *
     * @param resolutionIn the resolution to set
     * */
    public void setResolution(AdvisoryResolutionType resolutionIn) {
        this.resolution = resolutionIn;
    }
}
