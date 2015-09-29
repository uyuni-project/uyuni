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
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerConfig;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.gatherer.GathererJsonIO;
import com.suse.manager.gatherer.JsonHost;
import com.suse.manager.model.gatherer.GathererModule;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;


public class GathererJsonIOTest extends TestCase {
    private static final String MODULELIST = "modulelist.json";
    private static final String GATHEREROUT = "exampleGathererOutput.json";

    public void testReadGathererModules() throws Exception {
        String json = FileUtils.readStringFromFile(TestUtils.findTestData(MODULELIST).getPath());
        Map<String, GathererModule> mods = new GathererJsonIO().readGathererModules(json);

        assertEquals(2, mods.keySet().size());
        assertTrue(mods.keySet().contains("VMware"));
        assertTrue(mods.keySet().contains("SUSECloud"));

        for(GathererModule g : mods.values()) {
            if(g.getName().equals("VMware")) {
                assertTrue(g.getParameters().containsKey("host"));
                assertTrue(g.getParameters().containsKey("port"));
                assertTrue(g.getParameters().containsKey("user"));
                assertTrue(g.getParameters().containsKey("pass"));
                assertFalse(g.getParameters().containsKey("proto"));
                assertFalse(g.getParameters().containsKey("tenant"));
            }
            else if(g.getName().equals("SUSECloud")) {
                assertTrue(g.getParameters().containsKey("host"));
                assertTrue(g.getParameters().containsKey("port"));
                assertTrue(g.getParameters().containsKey("user"));
                assertTrue(g.getParameters().containsKey("pass"));
                assertTrue(g.getParameters().containsKey("proto"));
                assertTrue(g.getParameters().containsKey("tenant"));
            }
            else {
                assertTrue("Unknown Module", false);
            }
        }
    }

    public void testVHMtoJson() throws Exception {
        Credentials creds = CredentialsFactory.createVHMCredentials();
        creds.setUsername("tux");
        creds.setPassword("penguin");

        Set<VirtualHostManagerConfig> config = new HashSet<>();
        VirtualHostManagerConfig vhmc = new VirtualHostManagerConfig();
        vhmc.setParameter("host");
        vhmc.setValue("vCenter.example.com");
        config.add(vhmc);

        vhmc = new VirtualHostManagerConfig();
        vhmc.setParameter("port");
        vhmc.setValue("443");
        config.add(vhmc);

        VirtualHostManager vhm = new VirtualHostManager();
        vhm.setGathererModule("VMware");
        vhm.setLabel("vCenter");
        vhm.setId(1L);
        vhm.setCredentials(creds);
        vhm.setConfigs(config);

        List<VirtualHostManager> list = new ArrayList<>();
        list.add(vhm);

        String s = new GathererJsonIO().toJson(list);
        assertNotNull(s);
        assertTrue(s.contains("\"host\": \"vCenter.example.com\""));
        assertTrue(s.contains("\"port\": \"443\""));
        assertTrue(s.contains("\"user\": \"tux\""));
        assertTrue(s.contains("\"pass\": \"penguin\""));
        assertTrue(s.contains("\"id\": \"vCenter\""));
        assertTrue(s.contains("\"module\": \"VMware\""));
    }

    public void testReadGathererOutput() throws Exception {
        FileReader fr = new FileReader(TestUtils.findTestData(GATHEREROUT).getPath());
        Map<String, Map<String, JsonHost>> hosts = new GathererJsonIO().readHosts(fr);

        assertEquals(3, hosts.keySet().size());

        assertTrue(hosts.containsKey("1"));
        JsonHost h = hosts.get("1").get("10.162.186.111");
        assertEquals(16, h.getTotalCpuCores().intValue());
        assertEquals("x86_64", h.getCpuArch());
        assertEquals("AMD Opteron(tm) Processor 4386", h.getCpuDescription());
        assertEquals("amd", h.getCpuVendor());
        assertEquals(3092.212727, h.getCpuMhz().doubleValue());
        assertEquals("10.162.186.111", h.getName());
        assertEquals("VMware ESXi", h.getOs());
        assertEquals("5.5.0", h.getOsVersion());
        assertEquals(65512, h.getRamMb().intValue());
        assertEquals(2, h.getTotalCpuSockets().intValue());
        assertEquals(16, h.getTotalCpuThreads().intValue());
        assertEquals("564d6d90-459c-2256-8f39-3cb2bd24b7b0", h.getVms().get("vCenter"));
    }
}
