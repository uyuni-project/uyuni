package com.suse.oval.ovaltypes;

import com.suse.utils.Opt;

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

    @XmlElement(name = "affected", namespace = "http://oval.mitre.org/XMLSchema/oval-definitions-5")
    private AdvisoryAffectedType affected;

    public void setAffectedCpeList(AffectedCpeList affectedCpeList) {
        this.affectedCpeList = affectedCpeList;
    }

    public List<String> getAffectedCpeList() {
        return Optional.ofNullable(affectedCpeList)
                .map(AffectedCpeList::getCpeList)
                .orElse(Collections.emptyList());
    }

    public List<String> getAffectedComponents() {
        return Optional.ofNullable(affected).map(AdvisoryAffectedType::getResolution)
                .map(AdvisoryResolutionType::getAffectedComponents).orElse(Collections.emptyList());
    }

    public void setAffected(AdvisoryAffectedType affected) {
        this.affected = affected;
    }
}
