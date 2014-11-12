/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncUtils;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * Taskomatic job for refreshing data about channels, products and subscriptions.
 */
public class MgrSyncRefresh extends RhnJavaJob {

    /**
     * {@inheritDoc}
     * @throws JobExecutionException in case of errors during mgr-inter-sync execution
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("Refreshing mgr-sync data");
        }

        // Do nothing if this server has not been migrated yet
        if (!MgrSyncUtils.isMigratedToSCC()) {
            log.warn("No need to refresh, this server has not been migrated to SCC yet.");
            return;
        }

        // Use mgr-inter-sync if this server is an ISS slave
        if (IssFactory.getCurrentMaster() != null) {
            log.debug("This server is an ISS slave, refresh using mgr-inter-sync");
            String cmd[] = new String[1];
            cmd[0] = "/usr/bin/mgr-inter-sync";
            executeExtCmd(cmd);
            return;
        }

        // Measure time to calculate the total duration
        Date start = new Date();

        try {
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateChannels(null);
            csm.updateChannelFamilies(csm.readChannelFamilies());
            csm.updateSUSEProducts(csm.getProducts());
            csm.updateSUSEProductChannels(csm.getAvailableChannels(csm.readChannels()));
            csm.updateSubscriptions(csm.getSubscriptions());
            csm.updateUpgradePaths();
        }
        catch (ContentSyncException e) {
            log.error("Error during mgr-sync refresh", e);
        }

        // Schedule sync of all vendor channels
        new TaskomaticApi().scheduleSingleRepoSync(ChannelFactory.listVendorChannels());

        if (log.isDebugEnabled()) {
            long duration = new Date().getTime() - start.getTime();
            log.debug("Total duration was: " + duration + " ms");
        }
    }
}
