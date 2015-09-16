package com.redhat.rhn.domain.server.virtualhostmanager;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * VirtualHostManagerConfig - represtentation of a single configuration entry of
 * VirtualHostManager.
 */
public class VirtualHostManagerConfig {

    private Long id;
    private VirtualHostManager virtualHostManager;
    private String parameter;
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VirtualHostManager getVirtualHostManager() {
        return virtualHostManager;
    }

    public void setVirtualHostManager(VirtualHostManager virtualHostManager) {
        this.virtualHostManager = virtualHostManager;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        VirtualHostManagerConfig that = (VirtualHostManagerConfig) o;

        return new EqualsBuilder()
                .append(virtualHostManager, that.virtualHostManager)
                .append(parameter, that.parameter)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(virtualHostManager)
                .append(parameter)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "VirtualHostManagerConfig{" +
                "id=" + id +
                ", virtualHostManager=" + virtualHostManager +
                ", parameter='" + parameter + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
