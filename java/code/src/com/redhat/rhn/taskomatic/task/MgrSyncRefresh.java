/*
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.FileLocks;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.scc.client.SCCConfig;

import org.apache.commons.io.FileUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Taskomatic job for refreshing data about channels, products and subscriptions.
 */
public class MgrSyncRefresh extends RhnJavaJob {

    private static final String NO_REPO_SYNC_KEY = "noRepoSync";

    @Override
    public String getConfigNamespace() {
        return "mgrsyncrefresh";
    }

    /**
     * {@inheritDoc}
     * @throws JobExecutionException in case of errors during mgr-inter-sync execution
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("Refreshing mgr-sync data");
        }

        // Measure time to calculate the total duration
        Date start = new Date();

        // Get parameter
        boolean noRepoSync = false;
        if (context.getJobDetail().getJobDataMap().containsKey(NO_REPO_SYNC_KEY)) {
            try {
                noRepoSync = context.getJobDetail().getJobDataMap().getBooleanValue(NO_REPO_SYNC_KEY);
            }
            catch (ClassCastException e) {
                // if the provided value is not a bool we treat the presence of
                // the key as a true
                noRepoSync = true;
            }
        }

        // Use mgr-inter-sync if this server is an ISS slave
        if (IssFactory.getCurrentMaster() != null) {
            log.info("This server is an ISS slave, refresh using mgr-inter-sync");
            List<String> cmd = new ArrayList<>();
            cmd.add("/usr/bin/mgr-inter-sync");
            cmd.add("--include-custom-channels");
            if (noRepoSync) {
                cmd.add("--no-kickstarts");
                cmd.add("--no-errata");
                cmd.add("--no-packages");
            }
            executeExtCmd(cmd.toArray(new String[cmd.size()]));
        }
        else {
            // Get rid of old logging SCC data
            try {
                FileUtils.forceDelete(new File(SCCConfig.DEFAULT_LOGGING_DIR));
            }
            catch (IOException e1) {
                // never happens
            }

            // Perform the refresh
            FileLocks.SCC_REFRESH_LOCK.withFileLock(() -> {
                try {
                    ContentSyncManager csm = new ContentSyncManager();
                    csm.updateChannelFamilies(csm.readChannelFamilies());
                    HibernateFactory.commitTransaction();
                    HibernateFactory.closeSession();
                    csm.updateSUSEProducts(csm.getProducts());
                    HibernateFactory.commitTransaction();
                    HibernateFactory.closeSession();
                    csm.updateRepositories(null);
                    HibernateFactory.commitTransaction();
                    HibernateFactory.closeSession();
                    csm.updateSubscriptions();
                    HibernateFactory.commitTransaction();
                    HibernateFactory.closeSession();
                }
                catch (ContentSyncException e) {
                    log.error("Error during mgr-sync refresh", e);
                }
            });

            try {
                // Schedule sync of all vendor channels
                if (!noRepoSync) {
                    final TaskomaticApi taskoApi = new TaskomaticApi();

                    log.debug("Scheduling synchronization of all vendor channels");
                    taskoApi.scheduleSingleRepoSync(ChannelFactory.listVendorChannels());

                    if (ConfigDefaults.get().isCustomChannelManagementUnificationEnabled()) {
                        log.debug("Scheduling synchronization of all custom channels");
                        taskoApi.scheduleSingleRepoSync(ChannelFactory.listCustomChannelsWithRepositories());
                    }
                }
            }
            catch (TaskomaticApiException e) {
                throw new JobExecutionException(e);
            }
        }

        if (log.isDebugEnabled()) {
            long duration = new Date().getTime() - start.getTime();
            log.debug("Total duration was: {} ms", duration);
        }
    }
}
