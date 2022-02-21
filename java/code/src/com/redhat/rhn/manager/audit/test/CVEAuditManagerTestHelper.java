/*
 * Copyright (c) 2014--2021 SUSE LLC
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
package com.redhat.rhn.manager.audit.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.manager.audit.ServerChannelIdPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of utility methods for testing.
 *
 */
public class CVEAuditManagerTestHelper {

    /**
     * Not to be instantiated.
     */
    private CVEAuditManagerTestHelper() {
    }

    /**
     * Return all CVE audit relevant channels for a given system
     * (rows from table suseCVEServerChannel).
     * @param systemID ID of system
     * @return list of relevant channel IDs
     */
    @SuppressWarnings("unchecked")
    public static List<ServerChannelIdPair> getRelevantChannels(Long systemID) {
        SelectMode selectMode = ModeFactory.getMode("test_queries",
                "find_relevant_channels");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sid", systemID);
        DataResult<ServerChannelIdPair> ret = selectMode.execute(params);
        return ret;
    }

    /**
     * Return all {@link ServerChannelIdPair} objects from suseCVEServerChannel.
     * @return list of all relevant channels for all systems
     */
    @SuppressWarnings("unchecked")
    public static List<ServerChannelIdPair> getAllRelevantChannels() {
        SelectMode selectMode = ModeFactory.getMode("test_queries",
                "find_all_relevant_channels");
        Map<String, Object> params = new HashMap<String, Object>();
        DataResult<ServerChannelIdPair> ret = selectMode.execute(params);
        return ret;
    }

}
