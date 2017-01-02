/**
 * Copyright (c) 2010 Red Hat, Inc.
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

import com.redhat.rhn.domain.channel.AccessTokenFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * TokenCleanup
 * @version $Rev$
 */
public class TokenCleanup extends RhnJavaJob {

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext arg0In)
        throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("start token cleanup");
        }
        try {
            AccessTokenFactory.cleanupUnusedExpired();
        }
        catch (Exception e) {
            log.error("error while token cleanup", e);
        }
    }
}
