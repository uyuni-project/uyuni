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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.ReportDbConnectionManager;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.Map;


public class ReportDbUpdateTask extends RhnJavaJob {

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        ReportDbConnectionManager rcm = new ReportDbConnectionManager();
        ReportDbHibernateFactory rh = new ReportDbHibernateFactory(rcm);
        SelectMode m = ModeFactory.getMode("SystemReport_queries", "system", Map.class);
        DataResult<Map<String, Object>> dataResult = m.execute();

        WriteMode rd = ModeFactory.getWriteMode(rh.getSession(), "ReportDb_queries", "delete_system");
        Map<String, Object> params = new HashMap<>();
        params.put("mgm_id", 1); // 1 == "localhost"
        rd.executeUpdate(params);

        WriteMode rm = ModeFactory.getWriteMode(rh.getSession(), "ReportDb_queries", "insert_system");
        dataResult.forEach(set -> {
            set.putAll(params);
            rm.executeUpdate(set);
        });
        rh.commitTransaction();
        rh.closeSession();
        rh.closeSessionFactory();
    }

    @Override
    public String getConfigNamespace() {
        return "report_db_update";
    }
}
