/*
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MgrServerInfo;
import com.redhat.rhn.taskomatic.task.threaded.QueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * Hub Reporting DB Task Driver
 */
public class HubReportDbUpdateDriver implements QueueDriver<MgrServerInfo> {

    private static Set<MgrServerInfo> currentMgrServerInfos = Collections.synchronizedSet(new HashSet<>());
    private Logger log;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogger(Logger loggerIn) {
        this.log = loggerIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger() {
        return log;
    }

    public static synchronized Set<MgrServerInfo> getCurrentMgrServerInfos() {
        return currentMgrServerInfos;
    }

    /**
     * Query the existing Mgr Servers and return their infos
     * @return Set of server info
     */
    public Set<MgrServerInfo> getMgrServers() {
        CriteriaBuilder builder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<MgrServerInfo> criteria = builder.createQuery(MgrServerInfo.class);
        criteria.from(MgrServerInfo.class);
        Query<MgrServerInfo> query = HibernateFactory.getSession().createQuery(criteria);
        List<MgrServerInfo> mgrServerInfos = query.list();
        return new HashSet<>(mgrServerInfos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MgrServerInfo> getCandidates() {
        synchronized (currentMgrServerInfos) {
            Set<MgrServerInfo> candidates = getMgrServers();
            // Do not return candidates we are talking to already
            for (MgrServerInfo s : currentMgrServerInfos) {
                if (candidates.contains(s)) {
                    log.debug("Skipping system: " + s.getServer().getName());
                    candidates.remove(s);
                }
            }
            candidates.forEach(e ->  Hibernate.initialize(e.getReportDbCredentials()));
            return candidates.stream().collect(Collectors.toList());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWorkers() {
        return Config.get()
                .getInt(ConfigDefaults.REPORT_DB_HUB_WORKERS, 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueWorker makeWorker(MgrServerInfo workItem) {
        return new HubReportDbUpdateWorker(log, workItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canContinue() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        //empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlockingTaskQueue() {
        return true;
    }
}
