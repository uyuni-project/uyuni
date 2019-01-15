/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.domain.common;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Get information about us
 */
public class ManagerInfoFactory extends HibernateFactory {

    private static ManagerInfoFactory singleton = new ManagerInfoFactory();
    private static Logger log = Logger.getLogger(CommonFactory.class);

    private ManagerInfoFactory() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Return the last mgr-sync refresh date
     * @return last mgr-sync refresh date
     */
    public static Date getLastMgrSyncRefresh() {
        Map<String, Object> params = new HashMap<String, Object>();
        SelectMode m = ModeFactory.getMode("util_queries", "get_last_mgr_sync_refresh");
        DataResult<Map> dr = m.execute(params);
        if (!dr.isEmpty()) {
            return (Date) dr.get(0).get("last_mgr_sync_refresh");
        }
        return null;
    }

    /**
     * set last mgr-sync refresh to now
     */
    public static void setLastMgrSyncRefresh() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("lastrefresh", new Timestamp(System.currentTimeMillis()));

        WriteMode m = ModeFactory.getWriteMode("util_queries",
                getLastMgrSyncRefresh() == null ? "set_last_mgr_sync_refresh" : "update_last_mgr_sync_refresh");
        m.executeUpdate(params);
    }
}
