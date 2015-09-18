package com.redhat.rhn.domain.server.virtualhostmanager;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.org.Org;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Virtual Host Manager
 */
public class VirtualHostManager extends BaseDomainHelper {

    private Long id;
    private Org org;
    private String label;
    private String gathererModule;
    private Credentials credentials;
    private Set<VirtualHostManagerConfig> configs;

    public VirtualHostManager() {
        configs = new HashSet<>();
    }

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getGathererModule() {
        return gathererModule;
    }

    public void setGathererModule(String gathererModule) {
        this.gathererModule = gathererModule;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public Set<VirtualHostManagerConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(Set<VirtualHostManagerConfig> configs) {
        this.configs = configs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        VirtualHostManager that = (VirtualHostManager) o;

        return new EqualsBuilder()
                .append(label, that.label)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(label)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "VirtualHostManager{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", gathererModule='" + gathererModule + '\'' +
                '}';
    }
}
