/*
 * Copyright (c) 2024--2025 SUSE LLC
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
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.model.hub.AccessTokenDTO;
import com.suse.manager.model.hub.ChannelInfoDetailsJson;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.model.hub.TokenType;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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

        HubSCCCredentials sccCredentials = CredentialsFactory.createHubSCCCredentials("U123", "not so secret", "fqdn");
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
        HubSCCCredentials sccCredentials = CredentialsFactory.createHubSCCCredentials("U123", "not so secret", "fqdn");
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

    @Test
    public void testCreateAndLookupTokens() {
        Instant expiration = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(60, ChronoUnit.DAYS);

        long currentTokens = countCurrentTokens();

        hubFactory.saveToken("uyuni-hub.dev.local", "dummy-hub-token", TokenType.ISSUED, expiration);
        hubFactory.saveToken("uyuni-peripheral.dev.local", "dummy-peripheral-token", TokenType.CONSUMED, expiration);

        assertEquals(currentTokens + 2, countCurrentTokens());

        IssAccessToken hubAccessToken = hubFactory.lookupIssuedToken("dummy-hub-token");
        assertNotNull(hubAccessToken);
        assertEquals("uyuni-hub.dev.local", hubAccessToken.getServerFqdn());
        assertEquals(TokenType.ISSUED, hubAccessToken.getType());
        assertEquals(Date.from(expiration), hubAccessToken.getExpirationDate());

        IssAccessToken peripheralAccessToken = hubFactory.lookupAccessTokenFor("uyuni-peripheral.dev.local");
        assertNotNull(peripheralAccessToken);
        assertEquals("dummy-peripheral-token", peripheralAccessToken.getToken());
        assertEquals(TokenType.CONSUMED, peripheralAccessToken.getType());
        assertEquals(Date.from(expiration), peripheralAccessToken.getExpirationDate());
    }

    @Test
    public void canLookupTokensByFqdnAndType() {
        Instant expiration = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(60, ChronoUnit.DAYS);

        hubFactory.saveToken("dummy.fqdn", "dummy-issued-token", TokenType.ISSUED, expiration);
        hubFactory.saveToken("dummy.fqdn", "dummy-consumed-token", TokenType.CONSUMED, expiration);

        IssAccessToken issued = hubFactory.lookupAccessTokenByFqdnAndType("dummy.fqdn", TokenType.ISSUED);
        assertNotNull(issued);
        assertEquals("dummy.fqdn", issued.getServerFqdn());
        assertEquals(TokenType.ISSUED, issued.getType());
        assertEquals(Date.from(expiration), issued.getExpirationDate());

        IssAccessToken consumed = hubFactory.lookupAccessTokenByFqdnAndType("dummy.fqdn", TokenType.CONSUMED);
        assertNotNull(consumed);
        assertEquals("dummy.fqdn", consumed.getServerFqdn());
        assertEquals(TokenType.CONSUMED, consumed.getType());
        assertEquals(Date.from(expiration), consumed.getExpirationDate());
    }

    @Test
    public void ensureOnlyOneTokenIsStoredForFqdnAndType() {
        Instant shortExpiration = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(7, ChronoUnit.DAYS);
        Instant longExpiration = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(60, ChronoUnit.DAYS);

        String fqdn = getRandomFqdn();
        // Ensure no token exists with this fqdn
        assertEquals(0, countCurrentTokens(fqdn));

        // Should be possible to store both issued and consumed token
        hubFactory.saveToken(fqdn, "dummy-issued-token", TokenType.ISSUED, longExpiration);
        hubFactory.saveToken(fqdn, "dummy-consumed-token", TokenType.CONSUMED, longExpiration);

        assertEquals(2, countCurrentTokens(fqdn));

        // Check if the token is correct
        IssAccessToken issued = hubFactory.lookupAccessTokenByFqdnAndType(fqdn, TokenType.ISSUED);
        assertNotNull(issued);
        assertEquals(fqdn, issued.getServerFqdn());
        assertEquals("dummy-issued-token", issued.getToken());
        assertEquals(TokenType.ISSUED, issued.getType());
        assertEquals(Date.from(longExpiration), issued.getExpirationDate());

        // Storing a new issued token should replace the existing one
        hubFactory.saveToken(fqdn, "updated-issued-token", TokenType.ISSUED, shortExpiration);

        // We should still have 2 tokens
        assertEquals(2, HibernateFactory.getSession()
            .createQuery("SELECT COUNT(*) FROM IssAccessToken at WHERE at.serverFqdn = :fqdn", Long.class)
            .setParameter("fqdn", fqdn)
            .uniqueResult());

        // Check if the token is updated correctly
        issued = hubFactory.lookupAccessTokenByFqdnAndType(fqdn, TokenType.ISSUED);
        assertNotNull(issued);
        assertEquals(fqdn, issued.getServerFqdn());
        assertEquals("updated-issued-token", issued.getToken());
        assertEquals(TokenType.ISSUED, issued.getType());
        assertEquals(Date.from(shortExpiration), issued.getExpirationDate());
    }

    @Test
    public void canCountAndListTokens() throws Exception {
        Instant expiration = Instant.now().truncatedTo(ChronoUnit.MINUTES).plus(7, ChronoUnit.DAYS);

        Long initialTokenCount = countCurrentTokens();

        // Store the first token
        IssAccessToken tokenZero = hubFactory.saveToken(getRandomFqdn(), "zero", TokenType.ISSUED, expiration);
        assertEquals(initialTokenCount + 1, hubFactory.countAccessToken());
        Thread.sleep(1_000);

        // Store multiple tokens
        List<Long> generatedTokenIds = Stream.of("one", "two", "three", "four")
            .map(value -> hubFactory.saveToken(getRandomFqdn(), value, TokenType.ISSUED, expiration))
            .map(IssAccessToken::getId)
            .toList();

        // Check if the count is correct
        assertEquals(initialTokenCount + generatedTokenIds.size() + 1, hubFactory.countAccessToken());

        // Ensure we can extract all items
        List<AccessTokenDTO> tokens = hubFactory.listAccessToken(0, 1_000);
        assertEquals(initialTokenCount + generatedTokenIds.size() + 1, tokens.size());

        // Ensure the list is sorted by creation date
        tokens = hubFactory.listAccessToken(0, generatedTokenIds.size());
        assertEquals(generatedTokenIds.size(), tokens.size());

        assertTrue(tokens.stream()
            .allMatch(token -> generatedTokenIds.contains(token.getId())));

        // Ensure we can skip items
        tokens = hubFactory.listAccessToken(4, 1);
        assertEquals(1, tokens.size());
        assertEquals(tokenZero.getId(), tokens.get(0).getId());
    }

    private static Stream<Arguments> allCloneSyncVariants() {
        return Stream.of(
                Arguments.of(true, true, true, true),
                Arguments.of(true, true, true, false),
                Arguments.of(true, true, false, true),
                Arguments.of(true, true, false, false),
                Arguments.of(true, false, true, true),
                Arguments.of(true, false, true, false),
                Arguments.of(true, false, false, true),
                Arguments.of(true, false, false, false),
                Arguments.of(false, true, true, true),
                Arguments.of(false, true, true, false),
                Arguments.of(false, true, false, true),
                Arguments.of(false, true, false, false),
                Arguments.of(false, false, true, true),
                Arguments.of(false, false, true, false),
                Arguments.of(false, false, false, true),
                Arguments.of(false, false, false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("allCloneSyncVariants")
    public void canCreateChannelInfoFromPeripheralChannels(boolean peripheralSyncProd, boolean peripheralSyncTest,
                                                           boolean peripheralSyncDev, boolean peripheralSyncVendor)
            throws Exception {
        HubSCCCredentials sccCredentials = CredentialsFactory.createHubSCCCredentials("U123", "not so secret", "fqdn");
        CredentialsFactory.storeCredentials(sccCredentials);

        IssPeripheral peripheral = new IssPeripheral("peripheral1.example.com");
        peripheral.setRootCa("----- BEGIN CA -----");
        peripheral.setMirrorCredentials(sccCredentials);
        hubFactory.save(peripheral);

        Channel vendorBaseChannel = ChannelFactoryTest.createBaseChannel(user);
        vendorBaseChannel.setOrg(null);
        Channel vendorChildChannel = ChannelFactoryTest.createTestChannel(user);
        vendorChildChannel.setOrg(null);
        vendorChildChannel.setParentChannel(vendorBaseChannel);
        ChannelFactory.save(vendorBaseChannel);
        ChannelFactory.save(vendorChildChannel);

        Channel devBaseChannel = ChannelFactoryTest.createTestClonedChannel(vendorBaseChannel, user,
                "dev-", "-base", "DEV ", " Base", null);
        Channel devChildChannel = ChannelFactoryTest.createTestClonedChannel(vendorChildChannel, user,
                "dev-", "-child", "DEV ", " Child", devBaseChannel);
        ChannelFactory.save(devBaseChannel);
        ChannelFactory.save(devChildChannel);

        Channel testBaseChannel = ChannelFactoryTest.createTestClonedChannel(devBaseChannel, user,
                "test-", "-base", "TEST ", " Base", null);
        Channel testChildChannel = ChannelFactoryTest.createTestClonedChannel(devChildChannel, user,
                "test-", "-child", "TEST ", " Child", testBaseChannel);
        ChannelFactory.save(testBaseChannel);
        ChannelFactory.save(testChildChannel);

        Channel prodBaseChannel = ChannelFactoryTest.createTestClonedChannel(testBaseChannel, user,
                "prod-", "-base", "PROD ", " Base", null);
        Channel prodChildChannel = ChannelFactoryTest.createTestClonedChannel(testChildChannel, user,
                "prod-", "-child", "PROD ", " Child", prodBaseChannel);
        ChannelFactory.save(prodBaseChannel);
        ChannelFactory.save(prodChildChannel);

        int expectedChannelsSynced = 0;
        if (peripheralSyncProd) {
            hubFactory.save(new IssPeripheralChannels(peripheral, prodBaseChannel, 1L));
            hubFactory.save(new IssPeripheralChannels(peripheral, prodChildChannel, 1L));
            expectedChannelsSynced += 2;
        }

        if (peripheralSyncTest) {
            hubFactory.save(new IssPeripheralChannels(peripheral, testBaseChannel, 1L));
            hubFactory.save(new IssPeripheralChannels(peripheral, testChildChannel, 1L));
            expectedChannelsSynced += 2;
        }

        if (peripheralSyncDev) {
            hubFactory.save(new IssPeripheralChannels(peripheral, devBaseChannel, 1L));
            hubFactory.save(new IssPeripheralChannels(peripheral, devChildChannel, 1L));
            expectedChannelsSynced += 2;
        }

        if (peripheralSyncVendor) {
            hubFactory.save(new IssPeripheralChannels(peripheral, vendorBaseChannel));
            hubFactory.save(new IssPeripheralChannels(peripheral, vendorChildChannel));
            expectedChannelsSynced += 2;
        }

        List<ChannelInfoDetailsJson> infos = hubFactory.listChannelInfoForPeripheral(peripheral);
        assertEquals(expectedChannelsSynced, infos.size());

        for (ChannelInfoDetailsJson info : infos) {
            String label = info.getLabel();
            if (label.equals(vendorBaseChannel.getLabel())) {
                assertNull(info.getOriginalChannelLabel());
                assertNull(info.getParentChannelLabel());
            }
            else if (label.equals(vendorChildChannel.getLabel())) {
                assertNull(info.getOriginalChannelLabel());
                assertEquals(vendorBaseChannel.getLabel(), info.getParentChannelLabel());
            }
            else if (label.equals(devBaseChannel.getLabel())) {
                if (peripheralSyncVendor) {
                    assertEquals(vendorBaseChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else {
                    assertNull(info.getOriginalChannelLabel());
                }
                assertNull(info.getParentChannelLabel());
            }
            else if (label.equals(devChildChannel.getLabel())) {
                if (peripheralSyncVendor) {
                    assertEquals(vendorChildChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else {
                    assertNull(info.getOriginalChannelLabel());
                }
                assertEquals(devBaseChannel.getLabel(), info.getParentChannelLabel());
            }
            else if (label.equals(testBaseChannel.getLabel())) {
                if (peripheralSyncDev) {
                    assertEquals(devBaseChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else if (peripheralSyncVendor) {
                    assertEquals(vendorBaseChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else {
                    assertNull(info.getOriginalChannelLabel());
                }
                assertNull(info.getParentChannelLabel());
            }
            else if (label.equals(testChildChannel.getLabel())) {
                if (peripheralSyncDev) {
                    assertEquals(devChildChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else if (peripheralSyncVendor) {
                    assertEquals(vendorChildChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else {
                    assertNull(info.getOriginalChannelLabel());
                }
                assertEquals(testBaseChannel.getLabel(), info.getParentChannelLabel());
            }
            else if (label.equals(prodBaseChannel.getLabel())) {
                if (peripheralSyncTest) {
                    assertEquals(testBaseChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else if (peripheralSyncDev) {
                    assertEquals(devBaseChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else if (peripheralSyncVendor) {
                    assertEquals(vendorBaseChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else {
                    assertNull(info.getOriginalChannelLabel());
                }
                assertNull(info.getParentChannelLabel());
            }
            else if (label.equals(prodChildChannel.getLabel())) {
                if (peripheralSyncTest) {
                    assertEquals(testChildChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else if (peripheralSyncDev) {
                    assertEquals(devChildChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else if (peripheralSyncVendor) {
                    assertEquals(vendorChildChannel.getLabel(), info.getOriginalChannelLabel());
                }
                else {
                    assertNull(info.getOriginalChannelLabel());
                }
                assertEquals(prodBaseChannel.getLabel(), info.getParentChannelLabel());
            }
            else {
                fail("Unexpected channel");
            }
        }
    }

    @Test
    public void canCountPeripheralWithPaginationControl() {
        // Create a bunch of peripherals
        createPeripherals();

        PageControl pc = new PageControl();
        pc.setFilter(true);
        pc.setFilterColumn("fqdn");

        assertEquals(5, hubFactory.countPeripherals(null));

        pc.setFilterData("local");
        assertEquals(3, hubFactory.countPeripherals(pc));

        pc.setFilterData("dev");
        assertEquals(2, hubFactory.countPeripherals(pc));

        pc.setFilterData("aws");
        assertEquals(2, hubFactory.countPeripherals(pc));

        pc.setFilterData("test.local");
        assertEquals(1, hubFactory.countPeripherals(pc));

        pc.setFilterData("gamma");
        assertEquals(1, hubFactory.countPeripherals(pc));

        pc.setFilterData("03");
        assertEquals(3, hubFactory.countPeripherals(pc));

        pc.setFilterData("omega");
        assertEquals(0, hubFactory.countPeripherals(pc));
    }

    @Test
    public void canListPeripheralWithPaginationControl() {
        // Create a bunch of peripherals
        createPeripherals();

        // Ensure sorting is correct
        List<IssPeripheral> resultList;

        // First just sort all the items in ascending order
        resultList = hubFactory.listPaginatedPeripherals(new PageControl(1, 10, "fqdn"));
        assertNotEmpty(resultList);
        assertEquals(
            List.of("alpha", "beta", "delta", "epsilon", "gamma"),
            resultList.stream().map(ph -> ph.getFqdn().split("-")[0]).toList()
        );

        // Sort descending and limit
        resultList = hubFactory.listPaginatedPeripherals(new PageControl(1, 2, "fqdn", true));
        assertNotEmpty(resultList);
        assertEquals(
            List.of("gamma", "epsilon"),
            resultList.stream().map(ph -> ph.getFqdn().split("-")[0]).toList()
        );

        // Filter, sort ascending and limit
        resultList = hubFactory.listPaginatedPeripherals(new PageControl(1, 2, "fqdn", false, "fqdn", "local"));
        assertNotEmpty(resultList);
        assertEquals(
            List.of("alpha", "beta"),
            resultList.stream().map(ph -> ph.getFqdn().split("-")[0]).toList()
        );

        // Filter, sort descending and limit, getting second page
        resultList = hubFactory.listPaginatedPeripherals(new PageControl(2, 1, "fqdn", true, "fqdn", "03"));
        assertNotEmpty(resultList);
        assertEquals(
            List.of("epsilon"),
            resultList.stream().map(ph -> ph.getFqdn().split("-")[0]).toList()
        );
    }

    private void createPeripherals() {
        Stream.of(
            new IssPeripheral("alpha-01.dev.local"),
            new IssPeripheral("beta-03.test.local"),
            new IssPeripheral("gamma-03.prod.aws"),
            new IssPeripheral("delta-01.test.aws"),
            new IssPeripheral("epsilon-03.dev.local")
        ).forEach(ph -> hubFactory.save(ph));
    }

    private static String getRandomFqdn() {
        return "dummy.random.%s.fqdn".formatted(RandomStringUtils.randomAlphabetic(8));
    }

    private static Long countCurrentTokens() {
        return countCurrentTokens(null);
    }

    private static Long countCurrentTokens(String fqdn) {
        String hql = "SELECT COUNT(*) FROM IssAccessToken at";
        if (fqdn != null) {
            hql += " WHERE at.serverFqdn = :fqdn";
        }

        Query<Long> query = HibernateFactory.getSession().createQuery(hql, Long.class);
        if (fqdn != null) {
            query.setParameter("fqdn", fqdn);
        }

        return query.uniqueResult();
    }

    @Test
    public void generatedCoverageTestLookupAccessTokenById() {
        // this test has been generated programmatically to test HubFactory.lookupAccessTokenById
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        HubFactory testObject = new HubFactory();
        testObject.lookupAccessTokenById(0L);
    }


    @Test
    public void generatedCoverageTestRemoveAccessTokenById() {
        // this test has been generated programmatically to test HubFactory.removeAccessTokenById
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        HubFactory testObject = new HubFactory();
        testObject.removeAccessTokenById(0L);
    }
}
