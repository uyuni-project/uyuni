/*
 * Copyright (c) 2018 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server;

import com.suse.manager.webui.controllers.utils.ContactMethodUtil;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This class represents a summary of a minion.
 */
public class MinionSummary {

    private final Long serverId;
    private final String minionId;
    private final String digitalServerId;
    private final String machineId;
    private final String os;
    private final String contactMethodLabel;
    private final boolean transactionalUpdate;

    /**
     * Convenience constructor from a MinionServer instance.
     *
     * @param minion the minion
     */
    public MinionSummary(MinionServer minion) {
        this(minion.getId(), minion.getMinionId(), minion.getDigitalServerId(),
                minion.getMachineId(), minion.getContactMethodLabel().orElse(null), minion.getOs(),
                minion.doesOsSupportsTransactionalUpdate());
    }

    /**
     * Standard constructor.
     *
     * @param serverIdIn the server id
     * @param minionIdIn the minion id
     * @param digitalServerIdIn the digital server id
     * @param machineIdIn the machine id
     * @param contactMethodLabelIn the contact method label
     * @param osIn the minion os
     */
    public MinionSummary(Long serverIdIn, String minionIdIn, String digitalServerIdIn, String machineIdIn,
            String contactMethodLabelIn, String osIn) {
        this(serverIdIn, minionIdIn, digitalServerIdIn, machineIdIn, contactMethodLabelIn, osIn,
            ServerConstants.SLEMICRO.equals(osIn));
    }

    /**
     * Standard constructor.
     *
     * @param serverIdIn the server id
     * @param minionIdIn the minion id
     * @param digitalServerIdIn the digital server id
     * @param machineIdIn the machine id
     * @param contactMethodLabelIn the contact method label
     * @param osIn the minion os
     * @param transactionalUpdateIn if minion supports transactional update

     */
    public MinionSummary(Long serverIdIn, String minionIdIn, String digitalServerIdIn, String machineIdIn,
            String contactMethodLabelIn, String osIn, boolean transactionalUpdateIn) {
        this.serverId = serverIdIn;
        this.minionId = minionIdIn;
        this.digitalServerId = digitalServerIdIn;
        this.machineId = machineIdIn;
        this.contactMethodLabel = contactMethodLabelIn;
        this.os = osIn;
        this.transactionalUpdate = transactionalUpdateIn;
    }

    /**
     * @return true is minion is transactional update
     */
    public boolean isTransactionalUpdate() {
        return transactionalUpdate;
    }


    /**
     * @return the minion os
     */
    public String getOs() {
        return os;
    }

    /**
     * @return the server id
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * @return the digital server id
     */
    public String getDigitalServerId() {
        return digitalServerId;
    }

    /**
     * @return the machine id
     */
    public String getMachineId() {
        return machineId;
    }

    /**
     * @return true if the minion contact method is ssh-push
     */
    public boolean isSshPush() {
        if (contactMethodLabel == null) {
            return false;
        }

        return ContactMethodUtil.isSSHPushContactMethod(contactMethodLabel);
    }

    @Override
    public boolean equals(Object in) {
        if (this == in) {
            return true;
        }
        if (in == null || getClass() != in.getClass()) {
            return false;
        }
        MinionSummary minionIds = (MinionSummary) in;

        return new EqualsBuilder()
                .append(serverId, minionIds.serverId)
                .append(minionId, minionIds.minionId)
                .append(digitalServerId, minionIds.digitalServerId)
                .append(machineId, minionIds.machineId)
                .append(contactMethodLabel, minionIds.contactMethodLabel)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(serverId)
                .append(minionId)
                .append(digitalServerId)
                .append(machineId)
                .append(contactMethodLabel)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serverId", serverId)
                .append("minionId", minionId)
                .toString();
    }
}
