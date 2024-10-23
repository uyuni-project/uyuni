/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.model.hub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HubFactoryTest extends BaseTestCaseWithUser {

    private HubFactory hubFactory;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        hubFactory = new HubFactory();
    }

    @Test
    public void testCreateIssHub() {
        IssHub hub = new IssHub("hub.example.com");
        hubFactory.save(hub);

        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn("hub2.example.com");
        assertFalse(issHub.isPresent(), "Hub object unexpectedly found");

        issHub = hubFactory.lookupIssHubByFqdn("hub.example.com");
        assertTrue(issHub.isPresent(), "Hub object not found");
        assertNotNull(issHub.get().getId(), "ID should not be NULL");
        assertNotNull(issHub.get().getCreated(), "created should not be NULL");
        assertNull(issHub.get().getRootCa(), "Root CA should be NULL");

        SCCCredentials sccCredentials = CredentialsFactory.createSCCCredentials("U123", "not so secret");
        CredentialsFactory.storeCredentials(sccCredentials);

        hub.setRootCa("----- BEGIN CA -----");
        hub.setMirrorCredentials(sccCredentials);
        hubFactory.save(hub);

        issHub = hubFactory.lookupIssHubByFqdn("hub.example.com");
        assertTrue(issHub.isPresent(), "Hub object not found");
        assertEquals("----- BEGIN CA -----", issHub.get().getRootCa());
        assertEquals("U123", issHub.get().getMirrorCredentials().getUsername());
    }

    @Test
    public void testCreateIssPeripheral() {
        IssPeripheral peripheral = new IssPeripheral("peripheral1.example.com");
        hubFactory.save(peripheral);

        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn("peripheral2.example.com");
        assertFalse(issPeripheral.isPresent(), "Peripheral object unexpectedly found");

        issPeripheral = hubFactory.lookupIssPeripheralByFqdn("peripheral1.example.com");
        assertTrue(issPeripheral.isPresent(), "Peripheral object not found");
        assertNotNull(issPeripheral.get().getId(), "ID should not be NULL");
        assertNotNull(issPeripheral.get().getCreated(), "created should not be NULL");
        assertNull(issPeripheral.get().getRootCa(), "Root CA should be NULL");

        SCCCredentials sccCredentials = CredentialsFactory.createSCCCredentials("U123", "not so secret");
        CredentialsFactory.storeCredentials(sccCredentials);

        peripheral.setRootCa("----- BEGIN CA -----");
        peripheral.setMirrorCredentials(sccCredentials);
        hubFactory.save(peripheral);

        issPeripheral = hubFactory.lookupIssPeripheralByFqdn("peripheral1.example.com");
        assertTrue(issPeripheral.isPresent(), "Peripheral object not found");
        assertEquals("----- BEGIN CA -----", issPeripheral.get().getRootCa());
        assertEquals("U123", issPeripheral.get().getMirrorCredentials().getUsername());

    }

    @Test
    public void testCreateIssPeripheralChannels() throws Exception {
        SCCCredentials sccCredentials = CredentialsFactory.createSCCCredentials("U123", "not so secret");
        CredentialsFactory.storeCredentials(sccCredentials);

        Channel baseChannel = ChannelFactoryTest.createBaseChannel(user);
        Channel childChannel = ChannelFactoryTest.createTestChannel(user);
        childChannel.setParentChannel(baseChannel);
        ChannelFactory.save(baseChannel);
        ChannelFactory.save(childChannel);

        IssPeripheral peripheral = new IssPeripheral("peripheral1.example.com");
        peripheral.setRootCa("----- BEGIN CA -----");
        peripheral.setMirrorCredentials(sccCredentials);
        hubFactory.save(peripheral);

        IssPeripheralChannels pcBase = new IssPeripheralChannels(peripheral, baseChannel);
        IssPeripheralChannels pcChild = new IssPeripheralChannels(peripheral, childChannel);
        hubFactory.save(pcBase);
        hubFactory.save(pcChild);

        TestUtils.flushAndEvict(peripheral);

        IssPeripheral peripheral2 = new IssPeripheral("peripheral2.example.com");
        peripheral2.setRootCa("----- BEGIN CA -----");
        peripheral2.setMirrorCredentials(sccCredentials);
        hubFactory.save(peripheral2);

        IssPeripheralChannels pcBase2 = new IssPeripheralChannels(peripheral2, baseChannel);
        hubFactory.save(pcBase2);

        TestUtils.flushAndEvict(peripheral2);

        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn("peripheral1.example.com");
        assertTrue(issPeripheral.isPresent(), "Peripheral object not found");
        Set<IssPeripheralChannels> peripheralChannels = issPeripheral.get().getPeripheralChannels();
        assertEquals(2, peripheralChannels.size());

        Optional<IssPeripheral> issPeripheral2 = hubFactory.lookupIssPeripheralByFqdn("peripheral2.example.com");
        assertTrue(issPeripheral2.isPresent(), "Peripheral object not found");
        Set<IssPeripheralChannels> peripheralChannels2 = issPeripheral2.get().getPeripheralChannels();
        assertEquals(1, peripheralChannels2.size());

        List<IssPeripheralChannels> pcWithBase = hubFactory.listIssPeripheralChannelsByChannels(baseChannel);
        assertEquals(2, pcWithBase.size());

        List<IssPeripheralChannels> pcWithChild = hubFactory.listIssPeripheralChannelsByChannels(childChannel);
        assertEquals(1, pcWithChild.size());
    }
}
