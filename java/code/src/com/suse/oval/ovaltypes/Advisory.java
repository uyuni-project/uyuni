package com.suse.oval.ovaltypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
public class Advisory {
    @XmlElement(name = "affected_cpe_list", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private AffectedCpeList affectedCpeList;

    public void setAffectedCpeList(AffectedCpeList affectedCpeList) {
        this.affectedCpeList = affectedCpeList;
    }

    public List<String> getAffectedCpeList() {
        return Optional.ofNullable(affectedCpeList)
                .map(AffectedCpeList::getCpeList)
                .orElse(Collections.emptyList());
    }
}
