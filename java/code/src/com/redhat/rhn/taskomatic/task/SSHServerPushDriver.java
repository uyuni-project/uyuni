/**
 * Copyright (c) 2012 Novell
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

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.taskomatic.task.threaded.QueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

/**
 * Call rhn_check on relevant systems via SSH using remote port forwarding.
 */
public class SSHServerPushDriver implements QueueDriver {

    private Logger log;

    @Override
    public void initialize() {
    }

    /**
     * Call query to find relevant systems.
     * @return list of system IDs
     */
    @Override
    public List<?> getCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SSH_SERVER_PUSH_FIND_CANDIDATES);
        List<?> candidates = select.execute();
        log.info("Found " + candidates.size() + " candidates");
        return candidates;
    }

    @Override
    public QueueWorker makeWorker(Object item) {
        Long sid = (Long) (((HashMap<?, ?>) item).get("server_id"));
        return new SSHServerPushWorker(getLogger(), sid);
    }

    @Override
    public int getMaxWorkers() {
        return Config.get().getInt("taskomatic.ssh_server_push_workers", 2);
    }

    @Override
    public boolean canContinue() {
        return true;
    }

    @Override
    public void setLogger(Logger loggerIn) {
        this.log = loggerIn;
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
