/*
 * Copyright (c) 2015 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.gatherer;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.TimeUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;
import com.redhat.rhn.taskomatic.task.TaskHelper;

import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.gatherer.HostJson;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Taskomatic job for running gatherer on all Virtual Host Managers and
 * processing its results.
 */
public class GathererJob extends RhnJavaJob {

    public static final String VHM_LABEL = "vhmLabel";

    @Override
    public String getConfigNamespace() {
        return "gatherer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        String vhmLabel = null;
        if (jobExecutionContext.getJobDetail().getJobDataMap().containsKey(VHM_LABEL)) {
            vhmLabel = jobExecutionContext.getJobDetail()
                    .getJobDataMap().getString(VHM_LABEL);
        }
        List<VirtualHostManager> managers = new ArrayList<>();
        if (StringUtils.isEmpty(vhmLabel)) {
            managers.addAll(VirtualHostManagerFactory.getInstance()
                    .listVirtualHostManagers());
            gatherVmInfoFromPhysicalSystems();
        }
        else {
            managers.add(VirtualHostManagerFactory.getInstance().lookupByLabel(vhmLabel));
        }
        if (managers.isEmpty()) {
            log.debug("No Virtual Host Managers to run the gatherer job");
            return;
        }

        log.debug("Running gatherer for {} Virtual Host Managers", managers.size());

        try {
            Map<String, Map<String, HostJson>> results = new GathererRunner().run(managers);
            if (results == null) {
                return;
            }
            log.debug("Got {} Virtual Host Managers from gatherer", results.size());

            for (VirtualHostManager manager : managers) {
                String label = manager.getLabel();

                if (!results.containsKey(label)) {
                    log.warn("Virtual Host Manager with label '{}' is not " +
                            "contained in the results from gatherer - skipping it.",
                            label);
                    continue;
                }
                log.debug("Processing {}", label);
                new VirtualHostManagerProcessor(manager, results.get(label))
                        .processMapping();
            }
        }
        catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    private void gatherVmInfoFromPhysicalSystems() {
        for (Org org : OrgFactory.lookupAllOrgs()) {

            Set<Long> sids = TimeUtils.logTime(log, "Find physical systems",
                    () -> ServerFactory.listOrgSystems(org.getId()).stream()
                            .filter(s -> !s.isInactive())
                            .filter(s -> (s.hasEntitlement(EntitlementManager.SALT)))
                            .filter(s -> !s.isVirtualGuest())
                            .map(Server::getId)
                            .collect(Collectors.toSet()));
            if (sids.isEmpty()) {
                continue;
            }

            Action act = ActionFactory.createAction(ActionFactory.TYPE_VIRT_PROFILE_REFRESH);
            // set up needed fields for the action
            act.setName(act.getActionTypeName());
            act.setOrg(org);
            ActionFactory.save(act);

            ActionFactory.scheduleForExecution(act, sids);
            TaskHelper.scheduleActionExecution(act);

            log.info("  schedule Virt profile refresh for {} systems in org {}", sids.size(), org.getName());
        }
    }
}
