/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.task.repomd;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.task.TaskConstants;
import com.redhat.rhn.taskomatic.task.threaded.AbstractQueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class ChannelRepodataDriver extends AbstractQueueDriver<Map<String, Object>> {

    private Logger logger = null;

    @Override
    public void initialize() {
        WriteMode resetChannelRepodata = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMOD_CLEAR_IN_PROGRESS);
        try {
            int eqReset = resetChannelRepodata.executeUpdate(Map.of());
            if (eqReset > 0) {
                logger.info("Resetting {} unfinished channel repodata tasks", eqReset);
            }
            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            logger.error("Error resetting rhnRepoRegenQueue.next_action", e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    @Override
    protected List<Map<String, Object>> getCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMD_DRIVER_QUERY);

        Map<String, Object> params = new HashMap<>();
        List<Map<String, Object>> results = select.execute(params);
        if (results != null) {
            return results;
        }
        return Collections.emptyList();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Logger loggerIn) {
        logger = loggerIn;
    }

    @Override
    public int getMaxWorkers() {
        return ConfigDefaults.get().getTaskoChannelRepodataWorkers();
    }

    @Override
    protected QueueWorker makeWorker(Map<String, Object> workItem) {
        return new ChannelRepodataWorker(workItem, getLogger());
    }
}
