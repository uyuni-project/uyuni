/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.gatherer.test;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.gatherer.GathererJsonIO;
import com.suse.manager.model.gatherer.GathererModule;

import java.util.Map;

import junit.framework.TestCase;


public class GathererJsonIOTest extends TestCase {
    private static final String MODULELIST = "modulelist.json";

    public void testReadGathererModules() throws Exception {
        String json = FileUtils.readStringFromFile(TestUtils.findTestData(MODULELIST).getPath());
        Map<String, GathererModule> mods = new GathererJsonIO().readGathererModules(json);

        assertEquals(2, mods.keySet().size());
        assertTrue(mods.keySet().contains("VMware"));
        assertTrue(mods.keySet().contains("SUSECloud"));

        for(GathererModule g : mods.values()) {
            if(g.getName().equals("VMware")) {
                assertTrue(g.getParameter().containsKey("host"));
                assertTrue(g.getParameter().containsKey("port"));
                assertTrue(g.getParameter().containsKey("user"));
                assertTrue(g.getParameter().containsKey("pass"));
                assertFalse(g.getParameter().containsKey("proto"));
                assertFalse(g.getParameter().containsKey("tenant"));
            }
            else if(g.getName().equals("SUSECloud")) {
                assertTrue(g.getParameter().containsKey("host"));
                assertTrue(g.getParameter().containsKey("port"));
                assertTrue(g.getParameter().containsKey("user"));
                assertTrue(g.getParameter().containsKey("pass"));
                assertTrue(g.getParameter().containsKey("proto"));
                assertTrue(g.getParameter().containsKey("tenant"));
            }
            else {
                assertTrue("Unknown Module", false);
            }
        }
    }
}
