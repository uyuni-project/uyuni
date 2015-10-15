/**
 * Copyright (c) 2014--2015 SUSE LLC
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
package com.redhat.rhn.manager;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Handles SUSE EULAs in the database.
 */
public class EulaManager {

    /**
     * Get a list of EULAs for a package
     * @param pkgId id of the package
     * @return list of EULAs
     */
    @SuppressWarnings("unchecked")
    public List<String> getEulasForPackage(long pkgId) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
            TaskConstants.TASK_QUERY_REPOMD_GENERATOR_EULAS);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("package_id", pkgId);
        DataResult<Map<String, Object>> dataResult = m.execute(params);

        List<String> result = new LinkedList<String>();
        for (Map<String, Object> row: dataResult) {
            result.add(HibernateFactory.getBlobContents(row.get("text")));
        }

        return result;
    }
}

