package com.redhat.rhn.domain.state;

import com.redhat.rhn.domain.org.Org;

/**
 * Created by matei on 2/16/16.
 */
public class SaltState {

    private Long id;
    private Org org;
    private String stateName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}
