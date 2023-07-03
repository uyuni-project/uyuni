package com.suse.oval.ovaltypes;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class AffectedCpeList {
    @XmlElement(name = "cpe", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private List<String> cpeList;

    public List<String> getCpeList() {
        if (cpeList == null) {
            return new ArrayList<>();
        }
        return cpeList;
    }

    public void setCpeList(List<String> cpeList) {
        this.cpeList = cpeList;
    }
}
