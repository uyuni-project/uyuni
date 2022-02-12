/*
 * Copyright (c) 2020--2021 SUSE LLC
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
package com.redhat.rhn.manager.formula;

import static com.redhat.rhn.domain.formula.FormulaFactory.PROMETHEUS_EXPORTERS;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.datatypes.target.MinionList;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Manage enablement and disablement of monitoring exporters via Formulas.
 */
public class FormulaMonitoringManager implements MonitoringManager {

    private final SaltApi saltApi;

    /**
     * Constructor
     *
     * @param saltApiIn the Salt API
     */
    public FormulaMonitoringManager(SaltApi saltApiIn) {
        saltApi = saltApiIn;
    }
    /**
     * {@inheritDoc}
     */
    public void enableMonitoring(MinionServer minion) throws IOException, ValidatorException {
        // Assign the monitoring formula to the system unless it belongs to a group with monitoring enabled
        if (!FormulaFactory.isMemberOfGroupHavingMonitoring(minion)) {
            List<String> formulas = FormulaFactory.getFormulasByMinion(minion);
            if (!formulas.contains(FormulaFactory.PROMETHEUS_EXPORTERS)) {
                formulas.add(FormulaFactory.PROMETHEUS_EXPORTERS);
                FormulaFactory.saveServerFormulas(minion, formulas);
                saltApi.refreshPillar(new MinionList(minion.getMinionId()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disableMonitoring(MinionServer minion) throws IOException {
        if (this.isMonitoringCleanupNeeded(minion)) {
            // Get the current data and set all exporters to disabled
            String minionId = minion.getMinionId();
            Map<String, Object> data = FormulaFactory
                    .getFormulaValuesByNameAndMinion(PROMETHEUS_EXPORTERS, minion)
                    .orElse(FormulaFactory.getPillarExample(PROMETHEUS_EXPORTERS));
            FormulaFactory.saveServerFormulaData(
                    FormulaFactory.disableMonitoring(data), minion, PROMETHEUS_EXPORTERS);
            saltApi.refreshPillar(new MinionList(minion.getMinionId()));
        }
    }

    /**
     * Check for a given server if cleanup is needed on removal of the monitoring entitlement.
     *
     * @param server the given server
     * @return true if cleanup is needed, false otherwise
     */
    public boolean isMonitoringCleanupNeeded(MinionServer server) {
        return FormulaFactory.getFormulasByMinion(server).contains(PROMETHEUS_EXPORTERS) ||
                FormulaFactory.isMemberOfGroupHavingMonitoring(server);
    }
}
