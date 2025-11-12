/*
 * Copyright (c) 2010--2011 Red Hat, Inc.
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
package com.redhat.satellite.search.db.tests;

import com.redhat.satellite.search.db.DatabaseManager;
import com.redhat.satellite.search.db.Query;
import com.redhat.satellite.search.db.models.RhnPackage;
import com.redhat.satellite.search.tests.BaseTestCase;
import com.redhat.satellite.search.tests.TestUtil;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManagerTest extends BaseTestCase {

    private DatabaseManager dm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dm = (DatabaseManager)
            container.getComponentInstance(DatabaseManager.class);
    }

    public void testObjectQuery() throws SQLException {
        Query<Long> maxidquery = dm.getQuery("maxPackageId");
        Long maxid = maxidquery.load();
        Query<RhnPackage> query = dm.getQuery("getPackageById");
        RhnPackage p = query.load(maxid);
        assertNotNull(p);
        assertEquals(p.getId(), maxid.longValue());
    }

    public void testListQuery() throws SQLException {
        Query<RhnPackage> query = dm.getQuery("listPackagesFromId");
        List<RhnPackage> results = query.loadList((long) 0);
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class[] getComponentClasses() {
        return TestUtil.buildComponentsList(DatabaseManager.class);
    }
}
