package com.redhat.rhn.domain.audit;

/**
 * Created by matei on 2/15/17.
 */
public class XccdfIdent {

    private Long id;

    private XccdfIdentSystem identSystem;

    private String identifier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public XccdfIdentSystem getIdentSystem() {
        return identSystem;
    }

    public void setIdentSystem(XccdfIdentSystem identSystem) {
        this.identSystem = identSystem;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
