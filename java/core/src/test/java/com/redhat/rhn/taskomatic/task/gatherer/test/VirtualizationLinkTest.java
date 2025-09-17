/*
 * Copyright (c) 2015--2021 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.gatherer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.taskomatic.task.gatherer.VirtualHostManagerProcessor;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.gatherer.GathererJsonIO;
import com.suse.manager.gatherer.HostJson;

import org.junit.jupiter.api.Test;

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
    @Test
    public void testLinking() throws Exception {
        String json = FileUtils.readStringFromFile(TestUtils.findTestData("gatherer.out.json").getPath());
        Map<String, Map<String, HostJson>> hosts = new GathererJsonIO().readHosts(json);

        VirtualHostManager manager = new VirtualHostManager();
        String vhmLabel = "1";
        manager.setLabel(vhmLabel);
        manager.setId(101L);
        manager.setOrg(user.getOrg());

        new VirtualHostManagerProcessor(manager, hosts.get(vhmLabel)).processMapping();

        // server 1
        String digitalServerId = "de8-9a-8f-bd-a1-48.d3.cloud.mydomain.de";
        Server server1 = ServerFactory.lookupForeignSystemByDigitalServerId("101-" + digitalServerId);
        assertNotNull(server1);
        assertEquals(1, server1.getGuests().size());
        VirtualInstance guest1 = server1.getGuests().iterator().next();
        assertEquals("vCenter", guest1.getName());
        assertEquals("564d6d90459c22568f393cb2bd24b7b0", guest1.getUuid());
        VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(guest1.getUuid());

        // server 2
        digitalServerId = "de8-9a-8f-bd-a1-49.d3.cloud.mydomain.de";
        Server server2 = ServerFactory.lookupForeignSystemByDigitalServerId("101-" + digitalServerId);
        assertNotNull(server2);
        assertEquals(5, server2.getGuests().size());

        Map<String, String> vms = new HashMap<>();
        vms.put("49737e0a-c9e6-4ceb-aef8-6a9452f67cb5",
                "4230c60f3f982a65f7c3600b26b79c22");
        vms.put("5a2e4e63-a957-426b-bfa8-4169302e4fdb",
                "42307b151618059501f2427ffcddd88e");
        vms.put("NSX-gateway", "4230d43eaafe38ba5a9e3cb67c03a16a");
        vms.put("NSX-l3gateway", "4230b00f0b210e9ddfde6c7b06909d5f");
        vms.put("NSX-service", "4230e924b714198b348b25de01482fd9");
        server2.getGuests().stream().forEach(
                guest -> assertEquals(
                        vms.get(guest.getName()),
                        guest.getUuid()
                )
        );

        // server 3 (with no vms)
        digitalServerId = "de8-9a-8f-bd-a1-50.d3.cloud.mydomain.de";
        Server server3 = ServerFactory.lookupForeignSystemByDigitalServerId("101-" + digitalServerId);
        assertNotNull(server3);
        assertTrue(server3.getGuests().isEmpty());
    }
}
