/*
 * Copyright (c) 2026 SUSE LLC
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

package com.suse.manager.webui.services.pillar;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.taskomatic.task.ReportDBHelper;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Optional;

public class MinionReportDBPillarGenerator extends MinionPillarGeneratorBase {

    private static final Logger LOG = LogManager.getLogger(MinionReportDBPillarGenerator.class);
    public static final MinionReportDBPillarGenerator INSTANCE = new MinionReportDBPillarGenerator();
    public static final String CATEGORY = "reportDB";

    @Override
    public Optional<Pillar> generatePillarData(MinionServer minion) {
        Pillar pillar = minion.getPillarByCategory(CATEGORY).orElseGet(() -> {
            Pillar newPillar = new Pillar(CATEGORY, new HashMap<>(), minion);
            minion.getPillars().add(newPillar);
            return newPillar;
        });
        pillar.getPillar().clear();
        String reportDBName = Config.get().getString(ConfigDefaults.REPORT_DB_NAME, "reportdb");
        String reportDBUser = "grafana_" + minion.getId();
        String reportDBPass = RandomStringUtils.secure().nextAlphanumeric(20);
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
        try {
            ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;

            pillar.add("reportdb_name", reportDBName);
            pillar.add("reportdb_user", reportDBUser);
            pillar.add("reportdb_pass", reportDBPass);
            if (dbHelper.hasDBUser(localRh.getSession(), reportDBUser)) {
                dbHelper.changeDBPassword(localRh.getSession(), reportDBUser, reportDBPass);
            }
            else {
                dbHelper.createDBUser(localRh.getSession(), reportDBName, reportDBUser, reportDBPass);
            }
            localRh.commitTransaction();
        }
        catch (Exception e) {
            LOG.error("Setting user/password failed", e);
            pillar.getPillar().clear();
            minion.getPillars().remove(pillar);
            localRh.rollbackTransaction();
        }
        finally {
            localRh.closeSession();
            localRh.closeSessionFactory();
        }

        return Optional.of(pillar);
    }

    @Override
    public void removePillar(MinionServer minion) {
        minion.getPillarByCategory(CATEGORY).ifPresent(pillar -> {
            ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
            ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
            try {
                ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;
                try {
                    String username = (String)pillar.getPillarValue("reportdb_user");
                    if (dbHelper.hasDBUser(localRh.getSession(), username)) {
                        dbHelper.dropDBUser(localRh.getSession(), username);
                        localRh.commitTransaction();
                    }
                    else {
                        LOG.warn("DB User '{}' does not exist", username);
                    }
                }
                catch (LookupException le) {
                    LOG.warn("Pillar does not contain reportdb_user key.");
                }
                minion.getPillars().remove(pillar);
                HibernateFactory.getSession().remove(pillar);
            }
            catch (Exception e) {
                LOG.error("Removing user failed", e);
                localRh.rollbackTransaction();
            }
            finally {
                localRh.closeSession();
                localRh.closeSessionFactory();
            }
        });
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }
}
