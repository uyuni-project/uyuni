/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.channel.ChannelFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Run ANALYZE on tables heavily modified by CLM alignment.
 *
 * This is triggered by CLM alignment completion events, and may be triggered multiple times in a short period if
 * multiple CLM builds are being run. To avoid excessive load, only one run executes at a time, and if multiple
 * triggers arrive during a run, only one additional trailing run will be queued to execute immediately after the
 * active run completes.
 */
public class AnalyzeAlignTablesAction implements MessageAction {

    private static final Logger LOG = LogManager.getLogger(AnalyzeAlignTablesAction.class);
    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int TRAILING_RUN_QUEUED = 2;
    private static final AtomicInteger STATE = new AtomicInteger(IDLE);

    @Override
    public void execute(EventMessage msg) {
        // If a run is active, queue one trailing rerun and return.
        int previousState = STATE.getAndUpdate(state -> state == IDLE ? RUNNING : TRAILING_RUN_QUEUED);
        if (previousState != IDLE) {
            LOG.debug("Post-align ANALYZE run already in progress, trailing rerun requested");
            return;
        }

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Instant start = Instant.now();
                LOG.info("Starting post-align ANALYZE run");

                try {
                    runAnalyzeTables();
                    LOG.info("Finished post-align ANALYZE run in {}", Duration.between(start, Instant.now()));
                }
                catch (Exception e) {
                    LOG.error("Post-align ANALYZE run failed after {}", Duration.between(start, Instant.now()), e);
                }

                // Decide rerun/stop
                if (STATE.compareAndSet(RUNNING, IDLE)) {
                    return;
                }

                // It was TRAILING_RUN_QUEUED. Reset to RUNNING and loop
                STATE.set(RUNNING);
                LOG.debug("Executing trailing post-align ANALYZE run");
            }
        }
        finally {
            // Ensure we do not remain stuck in a non-idle state after any unexpected exit path.
            STATE.set(IDLE);
        }
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }

    private static void runAnalyzeTables() {
        ChannelFactory.analyzeChannelPackages();
        ChannelFactory.analyzeErrataPackages();
        ChannelFactory.analyzeChannelErrata();
        ChannelFactory.analyzeErrataCloned();
        ChannelFactory.analyzeErrata();
        ChannelFactory.analyzeServerNeededCache();
    }
}
