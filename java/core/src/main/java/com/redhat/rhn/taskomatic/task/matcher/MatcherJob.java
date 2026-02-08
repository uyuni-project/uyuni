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

package com.redhat.rhn.taskomatic.task.matcher;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;
import com.redhat.rhn.taskomatic.task.gatherer.GathererJob;

import com.suse.manager.matcher.MatcherRunner;

import org.quartz.JobExecutionContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Taskomatic job for running subscription matcher and processing its results.
 */
public class MatcherJob extends RhnJavaJob {

    public static final String CSV_SEPARATOR = "server.susemanager.matchercsvseparator";

    @Override
    public String getConfigNamespace() {
        return "matcher";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        if (jobExecutionContext.getJobDetail().getJobDataMap()
                .containsKey(GathererJob.VHM_LABEL)) {
            log.warn("Gatherer-matcher bunch was run for a specific Virtual Host " +
                    "Manager. NOT running matcher.");
            return;
        }

        // Wait on running actions scheduled by the gatherer jobs
        Instant i = Instant.now().plus(60, ChronoUnit.SECONDS);
        do {
            List<ServerAction> pending = ActionFactory.listPendingServerActionsByTypes(
                    List.of(ActionFactory.TYPE_VIRT_PROFILE_REFRESH));
            if (pending.isEmpty()) {
                break;
            }
            try {
                log.debug("waiting for pending actions to finish");
                TimeUnit.SECONDS.sleep(5);
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted", ie);
                break;
            }
        } while (Instant.now().isBefore(i));

        try {
            String sep = Config.get().getString(CSV_SEPARATOR, ",");
            new MatcherRunner().run(sep);
        }
        catch (SecurityException e) {
            log.error("Not permitted to execute subscription-matcher", e);
            HibernateFactory.rollbackTransaction();
        }
        catch (IllegalStateException e) {
            log.error("Output problem with matcher: {}", e.getMessage(), e);
            HibernateFactory.rollbackTransaction();
        }
        catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            HibernateFactory.closeSession();
        }
    }
}
