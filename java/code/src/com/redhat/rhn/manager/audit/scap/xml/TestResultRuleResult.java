package com.redhat.rhn.manager.audit.scap.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by matei on 2/15/17.
 */
@Root(name = "rr", strict = false)
public class TestResultRuleResult {

    @Attribute
    private String id;

    @ElementList(entry="ident", inline = true, required = false)
    private List<TestResultRuleResultIdent> idents;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TestResultRuleResultIdent> getIdents() {
        return idents;
    }

    public void setIdents(List<TestResultRuleResultIdent> idents) {
        this.idents = idents;
    }
}
