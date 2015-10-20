package com.redhat.rhn.taskomatic.task.gatherer.test;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.taskomatic.task.gatherer.VirtualHostManagerProcessor;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.gatherer.GathererJsonIO;
import com.suse.manager.gatherer.JSONHost;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test for parsing gatherer output and mapping virtual instances to
 * servers.
 */
public class VirtualizationLinkTest extends BaseTestCaseWithUser {

    /**
     * Parses test data, verifies that corresponding foreign entitled Servers and
     * corresponding VirtualInstances are created.
     * @throws Exception - if anything goes wrong
     */
    public void testLinking() throws Exception {
        FileReader fr =
                new FileReader(TestUtils.findTestData("gatherer.out.json").getPath());
        Map<String, Map<String, JSONHost>> hosts = new GathererJsonIO().readHosts(fr);

        VirtualHostManager manager = new VirtualHostManager();
        String vhmLabel = "1";
        manager.setLabel(vhmLabel);
        manager.setOrg(user.getOrg());

        new VirtualHostManagerProcessor(manager, hosts.get(vhmLabel)).processMapping();

        // server 1
        Server server1 = ServerFactory.lookupForeignSystemByName("10.162.186.111");
        assertNotNull(server1);
        assertEquals(1, server1.getGuests().size());
        VirtualInstance guest1 = server1.getGuests().iterator().next();
        assertEquals("vCenter", guest1.getName());
        assertEquals("564d6d90-459c-2256-8f39-3cb2bd24b7b0", guest1.getUuid());
        VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(guest1.getUuid());

        // server 2
        Server server2 = ServerFactory.lookupForeignSystemByName("10.162.186.112");
        assertNotNull(server2);
        assertEquals(5, server2.getGuests().size());

        Map<String, String> vms = new HashMap<>();
        vms.put("49737e0a-c9e6-4ceb-aef8-6a9452f67cb5",
                "4230c60f-3f98-2a65-f7c3-600b26b79c22");
        vms.put("5a2e4e63-a957-426b-bfa8-4169302e4fdb",
                "42307b15-1618-0595-01f2-427ffcddd88e");
        vms.put("NSX-gateway", "4230d43e-aafe-38ba-5a9e-3cb67c03a16a");
        vms.put("NSX-l3gateway", "4230b00f-0b21-0e9d-dfde-6c7b06909d5f");
        vms.put("NSX-service", "4230e924-b714-198b-348b-25de01482fd9");
        server2.getGuests().stream().forEach(
                guest -> assertEquals(
                        vms.get(guest.getName()),
                        guest.getUuid()
                )
        );

        // server 3 (with no vms)
        Server server3 = ServerFactory.lookupForeignSystemByName("host-with-no-vms");
        assertNotNull(server3);
        assertTrue(server3.getGuests().isEmpty());
    }
}
