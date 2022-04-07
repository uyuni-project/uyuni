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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * Hub Reporting DB Task Driver
 */
public class HubReportDbUpdateDriver implements QueueDriver<MgrServerInfo> {

    private Logger log;
    private static AtomicBoolean hasCandidates = new AtomicBoolean(true);

    @Override
    public void setLogger(Logger loggerIn) {
        this.log = loggerIn;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public List<MgrServerInfo> getCandidates() {
        CriteriaBuilder builder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<MgrServerInfo> criteria = builder.createQuery(MgrServerInfo.class);
        criteria.from(MgrServerInfo.class);
        Query<MgrServerInfo> query = HibernateFactory.getSession().createQuery(criteria);
        List<MgrServerInfo> mgrServerInfos = query.list();
        return mgrServerInfos.stream().filter(info ->
                Optional.ofNullable(info.getReportDbLastSynced())
                        .map(time -> time.toInstant().plus(24, ChronoUnit.HOURS).isBefore(Instant.now()))
                        .orElse(true)
        ).map(e -> {
            Hibernate.initialize(e.getReportDbCredentials());
           return e;
        }).collect(Collectors.toList());
    }

    @Override
    public int getMaxWorkers() {
        return Config.get()
                .getInt(ConfigDefaults.REPORT_DB_HUB_WORKERS, 2);
    }

    @Override
    public QueueWorker makeWorker(MgrServerInfo workItem) {
        return new HubReportDbUpdateWorker(log, workItem);
    }

    @Override
    public boolean canContinue() {
        return true;
    }

    @Override
    public void initialize() {

    }
}
