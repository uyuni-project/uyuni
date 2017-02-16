package com.redhat.rhn.domain.audit;

/**
 * Created by matei on 2/15/17.
 */
public class XccdfIdentSystem {

    private Long id;

    private String system;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }
}
