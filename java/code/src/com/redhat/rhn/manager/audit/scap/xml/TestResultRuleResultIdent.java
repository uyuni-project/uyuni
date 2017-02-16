package com.redhat.rhn.manager.audit.scap.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

/**
 * Created by matei on 2/16/17.
 */
public class TestResultRuleResultIdent {

    @Attribute
    private String system;

    @Text
    private String text;

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
