package com.redhat.rhn.manager.audit.scap.xml;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;


public class Rule {

    @Attribute(name="id", required = false)
    private String id;

    @Element(name = "remediation", required = false)
    private String remediation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemediation() {
        return remediation;
    }

    public void setRemediation(String remediation) {
        this.remediation = remediation;
    }
}
