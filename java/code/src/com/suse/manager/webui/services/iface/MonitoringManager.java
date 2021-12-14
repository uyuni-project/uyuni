/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.services.iface;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.server.MinionServer;

import java.io.IOException;

/**
 * Manage enablement and disablement of monitoring exporters.
 */
public interface MonitoringManager {

    /**
     * Configure system for monitoring when the entitlement is added.
     *
     * @param minion the MinionServer to be configured for monitoring
     * @throws IOException if an IO error occurs while saving the data
     * @throws ValidatorException if a formula is not present (unchecked)
     */
    void enableMonitoring(MinionServer minion) throws IOException, ValidatorException;

    /**
     * Configure the monitoring formula for cleanup (disable exporters) if needed.
     *
     * @param minion the MinionServer to be configured for monitoring cleanup
     * @throws IOException if an IO error occurs while saving the data
     */
    void disableMonitoring(MinionServer minion) throws IOException;
}
