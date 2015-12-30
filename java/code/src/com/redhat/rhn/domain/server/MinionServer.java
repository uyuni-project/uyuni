package com.redhat.rhn.domain.server;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * MinionServer
 */
public class MinionServer extends Server {

    private String minionId;
    private String machineId;

    /**
     * Constructs a MinionServer instance.
     */
    public MinionServer() {
        super();
    }

    public String getMinionId() {
        return minionId;
    }

    public void setMinionId(String minionId) {
        this.minionId = minionId;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MinionServer)) {
            return false;
        }
        MinionServer otherMinion = (MinionServer) other;
        return new EqualsBuilder()
                .appendSuper(super.equals(otherMinion))
                .append(getMachineId(), otherMinion.getMachineId())
                .append(getMinionId(), otherMinion.getMinionId())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(getMachineId())
                .append(getMinionId())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("machineId", getMachineId())
                .append("minionId", getMinionId())
                .toString();
    }
}
