package com.suse.oval.db;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class OVALReferenceKey implements Serializable {
    private String refId;
    private OVALDefinition definition;

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public OVALDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(OVALDefinition definition) {
        this.definition = definition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder()
                .append(refId)
                .append(definition.getId());
        return builder.toHashCode();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof OVALReferenceKey) {
            OVALReferenceKey otherKey = (OVALReferenceKey) other;
            return new EqualsBuilder()
                    .append(this.getRefId(), otherKey.getRefId())
                    .append(this.getDefinition().getId(), otherKey.getDefinition().getId())
                    .isEquals();
        } else {
            return false;
        }
    }
}
