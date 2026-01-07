/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action;


import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.TransactionalUpdate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * RebootAction - Class representing TYPE_REBOOT
 */
@Entity
@DiscriminatorValue("9")
public class RebootAction extends Action {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        int rebootDelay = ConfigDefaults.get().getRebootDelay();
        return minionSummaries.stream().collect(
                Collectors.groupingBy(
                        m -> m.isTransactionalUpdate() ? TransactionalUpdate.reboot() :
                                com.suse.salt.netapi.calls.modules.System.reboot(Optional.of(rebootDelay))
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldCleanupAction(Date bootTime, ServerAction sa) {
        boolean result = false;
        if (sa.isStatusPickedUp() && sa.getPickupTime() != null) {
            result = bootTime.after(sa.getPickupTime());
        }
        else if (sa.isStatusPickedUp() && sa.getPickupTime() == null) {
            result = bootTime.after(getEarliestAction());
        }
        else if (sa.isStatusQueued()) {
            if (getPrerequisite() != null) {
                // queued reboot actions that do not complete in 12 hours will
                // be cleaned up by MinionActionUtils.cleanupMinionActions()
                result = false;
            }
            else {
                result = bootTime.after(getEarliestAction());
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("shouldCleanupAction Server:{} Action: {} BootTime: {} PickupTime: {} EarliestAction {}" +
                            " Result: {}", sa.getServer().getId(), getId(), bootTime,
                    sa.getPickupTime(), getEarliestAction(), result);
        }
        return result;
    }

}
