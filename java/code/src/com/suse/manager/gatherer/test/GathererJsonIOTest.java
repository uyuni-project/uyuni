/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.VHMCredentials;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerConfig;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.gatherer.GathererJsonIO;
import com.suse.manager.gatherer.HostJson;
import com.suse.manager.model.gatherer.GathererModule;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for {@link GathererJsonIO}
 */
public class GathererJsonIOTest  {

    private static final String MODULELIST = "modulelist.json";
    private static final String GATHEREROUT = "exampleGathererOutput.json";

    @Test
    public void testReadGathererModules() throws Exception {
        String json =
                FileUtils.readStringFromFile(TestUtils.findTestData(MODULELIST).getPath());
        Map<String, GathererModule> mods = new GathererJsonIO().readGathererModules(json);

        assertEquals(3, mods.keySet().size());
        assertTrue(mods.keySet().contains("VMware"));
        assertTrue(mods.keySet().contains("SUSECloud"));
        assertTrue(mods.keySet().contains("Libvirt"));

        for (GathererModule g : mods.values()) {
            if (g.getName().equals("VMware")) {
                assertTrue(g.getParameters().containsKey("hostname"));
                assertTrue(g.getParameters().containsKey("port"));
                assertTrue(g.getParameters().containsKey("username"));
                assertTrue(g.getParameters().containsKey("password"));
                assertFalse(g.getParameters().containsKey("protocol"));
                assertFalse(g.getParameters().containsKey("tenant"));
            }
            else if (g.getName().equals("SUSECloud")) {
                assertTrue(g.getParameters().containsKey("hostname"));
                assertTrue(g.getParameters().containsKey("port"));
                assertTrue(g.getParameters().containsKey("username"));
                assertTrue(g.getParameters().containsKey("password"));
                assertTrue(g.getParameters().containsKey("protocol"));
                assertTrue(g.getParameters().containsKey("tenant"));
            }
            else if (g.getName().equals("Libvirt")) {
                assertTrue(g.getParameters().containsKey("uri"));
                assertTrue(g.getParameters().containsKey("sasl_username"));
                assertTrue(g.getParameters().containsKey("sasl_password"));
            }
            else {
                fail("Unknown Module");
            }
        }
    }

    @Test
    public void testVHMtoJson() throws Exception {
        VHMCredentials creds = CredentialsFactory.createVHMCredentials("tux", "penguin");

        Set<VirtualHostManagerConfig> config = new HashSet<>();
        VirtualHostManagerConfig vhmc = new VirtualHostManagerConfig();
        vhmc.setParameter("hostname");
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
        assertTrue(s.contains("\"hostname\": \"vCenter.example.com\""));
        assertTrue(s.contains("\"port\": \"443\""));
        assertTrue(s.contains("\"username\": \"tux\""));
        assertTrue(s.contains("\"password\": \"penguin\""));
        assertTrue(s.contains("\"id\": \"vCenter\""));
        assertTrue(s.contains("\"module\": \"VMware\""));
    }

    @Test
    public void testReadGathererOutput() throws Exception {
        String json = FileUtils.readStringFromFile(TestUtils.findTestData(GATHEREROUT).getPath());
        Map<String, Map<String, HostJson>> hosts = new GathererJsonIO().readHosts(json);

        assertEquals(3, hosts.keySet().size());

        assertTrue(hosts.containsKey("1"));
        HostJson h = hosts.get("1").get("10.162.186.111");
        assertEquals(16, h.getTotalCpuCores().intValue());
        assertEquals("x86_64", h.getCpuArch());
        assertEquals("AMD Opteron(tm) Processor 4386", h.getCpuDescription());
        assertEquals("amd", h.getCpuVendor());
        assertEquals(Double.valueOf(3092.212727), h.getCpuMhz());
        assertEquals("10.162.186.111", h.getName());
        assertEquals("de8-9a-8f-bd-a1-48.d3.cloud.mydomain.de", h.getHostIdentifier());
        assertEquals("VMware ESXi", h.getOs());
        assertEquals("5.5.0", h.getOsVersion());
        assertEquals(65512, h.getRamMb().intValue());
        assertEquals(2, h.getTotalCpuSockets().intValue());
        assertEquals(16, h.getTotalCpuThreads().intValue());
        assertEquals("564d6d90-459c-2256-8f39-3cb2bd24b7b0", h.getVms().get("vCenter"));
        assertEquals(Collections.emptyMap(), h.getOptionalVmData());
    }

    @Test
    public void testReadGathererOutputWithVmAddiotnalData() throws Exception {
        String json = FileUtils.readStringFromFile(TestUtils.findTestData(GATHEREROUT).getPath());
        Map<String, Map<String, HostJson>> hosts = new GathererJsonIO().readHosts(json);

        assertEquals(3, hosts.keySet().size());

        assertTrue(hosts.containsKey("9c84c119-cb23-439b-b479-327e81d53988"));
        HostJson h = hosts.get("9c84c119-cb23-439b-b479-327e81d53988").get("abcdefg.suse.de");
        assertNotNull(h.getOptionalVmData());
        assertEquals("running", h.getOptionalVmData().get("SUSE-Manager-Reference").get("vmState"));
    }
}
