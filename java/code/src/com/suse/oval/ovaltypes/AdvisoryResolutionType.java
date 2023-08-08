package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class AdvisoryResolutionType {
    @XmlElement(name = "component", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    protected List<String> affectedComponents;
    @XmlAttribute(name = "state", required = true)
    protected String state;

    public List<String> getAffectedComponents() {
        return affectedComponents;
    }

    public void setAffectedComponents(List<String> affectedComponents) {
        this.affectedComponents = affectedComponents;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
