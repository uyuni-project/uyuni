package com.suse.manager.matcher.test;

import com.redhat.rhn.domain.common.LoggingFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.matcher.MatcherJsonIO;

import junit.framework.TestCase;

public class MatcherJsonIOTest extends TestCase {

    public void testSystemsToJson() throws Exception {
        LoggingFactory.clearLogId();

        Server h1 = ServerTestUtils.createTestSystem();
        h1.setName("host1.example.com");
        h1.setCpu(createCPU(h1, 8L));

        /* FIXME: add products to systems */

        Server g1 = ServerTestUtils.createTestSystem();
        g1.setCpu(createCPU(g1, 2L));
        g1.setName("guest1.example.com");
        String uuid1 = TestUtils.randomString();

        VirtualInstance refGuest1 = createVirtualInstance(h1, g1, uuid1);
        h1.addGuest(refGuest1);

        Server g2 = ServerTestUtils.createTestSystem();
        g2.setName("guest2.example.com");
        g2.setCpu(createCPU(g2, 4L));
        String uuid2 = TestUtils.randomString();

        VirtualInstance refGuest2 = createVirtualInstance(h1, g2, uuid2);
        h1.addGuest(refGuest2);

        String jsonString = new MatcherJsonIO().getJsonSystems();
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("\"name\": \"host1.example.com\""));
        assertTrue(jsonString.contains("\"name\": \"guest1.example.com\""));
        assertTrue(jsonString.contains("\"name\": \"guest2.example.com\""));
        assertTrue(jsonString.contains("      " + g1.getId()));
        assertTrue(jsonString.contains("      " + g2.getId()));
    }

    private VirtualInstance createVirtualInstance(Server host, Server guest, String uuid) {
        VirtualInstance virtualInstance = new VirtualInstance();
        virtualInstance.setHostSystem(host);
        virtualInstance.setGuestSystem(guest);
        virtualInstance.setUuid(uuid);
        virtualInstance.setConfirmed(1L);
        return virtualInstance;
    }

    private CPU createCPU(Server s, Long sockets) {
        CPU cpu = new CPU();
        cpu.setNrsocket(sockets);
        cpu.setServer(s);
        cpu.setArch(ServerFactory.lookupCPUArchByName("x86_64"));
        return cpu;
    }
}
