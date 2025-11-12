/*
 * Copyright (c) 2010--2015 Red Hat, Inc.
 * Copyright (c) 2022 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.satellite.search.scheduler.tests;

import com.redhat.satellite.search.index.IndexManager;
import com.redhat.satellite.search.index.IndexingException;
import com.redhat.satellite.search.index.QueryParseException;
import com.redhat.satellite.search.index.Result;
import com.redhat.satellite.search.index.builder.BuilderFactory;
import com.redhat.satellite.search.scheduler.ScheduleManager;
import com.redhat.satellite.search.tests.BaseTestCase;
import com.redhat.satellite.search.tests.TestUtil;

import java.util.List;

public class ScheduleManagerTest extends BaseTestCase {
    @SuppressWarnings("unchecked")
    @Override
    protected Class[] getComponentClasses() {
        return TestUtil.buildComponentsList(ScheduleManager.class);
    }

    public void testIndexing() throws IndexingException, QueryParseException {
        try {
            Thread.sleep(30000);
            IndexManager mgr = (IndexManager)
                container.getComponentInstance(IndexManager.class);
            List<Result>hits = mgr.search("package", "description:package",
                    "en");
            assertTrue(hits.size() > 0);
        }
        catch (InterruptedException e) {
            return;
        }
    }

    public void testTriggerIndexTask() {
        ScheduleManager sm = new ScheduleManager(null, null);
        assertTrue(sm.triggerIndexTask(BuilderFactory.ERRATA_TYPE));
        assertTrue(sm.triggerIndexTask(BuilderFactory.HARDWARE_DEVICE_TYPE));
        assertTrue(sm.triggerIndexTask(BuilderFactory.PACKAGES_TYPE));
        assertTrue(sm.triggerIndexTask(BuilderFactory.SERVER_CUSTOM_INFO_TYPE));
        assertTrue(sm.triggerIndexTask(BuilderFactory.SERVER_TYPE));
        assertTrue(sm.triggerIndexTask(BuilderFactory.SNAPSHOT_TAG_TYPE));
        assertTrue(sm.triggerIndexTask(BuilderFactory.XCCDF_IDENT_TYPE));
        assertFalse(sm.triggerIndexTask(null));
        assertFalse(sm.triggerIndexTask("biteme"));
        assertFalse(sm.triggerIndexTask(""));
    }
}
