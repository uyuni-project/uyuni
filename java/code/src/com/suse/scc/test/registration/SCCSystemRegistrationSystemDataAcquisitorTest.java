/*
 * Copyright (c) 2023--2024 SUSE LLC
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

package com.suse.scc.test.registration;

import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceType;

import com.suse.scc.SCCSystemId;
import com.suse.scc.model.SCCHwInfoJson;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.registration.SCCSystemRegistrationContext;
import com.suse.scc.registration.SCCSystemRegistrationSystemDataAcquisitor;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(JUnit5Mockery.class)
public class SCCSystemRegistrationSystemDataAcquisitorTest extends AbstractSCCSystemRegistrationTest {

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery();

    /**
     * Tests when no systems are provided.
     * In this case no systems should be added to context.getPendingRegistrationSystems().
     */
    @Test
    public void testSuccessSCCSystemRegistrationSystemDataAcquisitorWhenNoSystemsProvided() throws Exception {
        // setup
        this.setupSystems(0, 0);
        final SCCSystemRegistrationContext sccSystemRegistrationContext =
                new SCCSystemRegistrationContext(null, getTestSystems(), null);

        // pre-conditions
        assertEquals(0, sccSystemRegistrationContext.getItems().size());
        assertEquals(0, sccSystemRegistrationContext.getPendingRegistrationSystems().size());
        assertEquals(0, sccSystemRegistrationContext.getItemsBySccSystemId().size());

        // execution
        new SCCSystemRegistrationSystemDataAcquisitor().handle(sccSystemRegistrationContext);

        // assertions
        assertEquals(0, sccSystemRegistrationContext.getPendingRegistrationSystems().size());
        assertEquals(0, sccSystemRegistrationContext.getItemsBySccSystemId().size());
    }

    /**
     * Test success when 20 systems are provided and 5 of them are PayG.
     * In this case 15 systems should be added to context.getPendingRegistrationSystems() and 5 to
     * context.getPaygSystems().
     * At this point all systems should be marked as requiring registration.
     */
    @Test
    public void testSuccessSCCSystemRegistrationSystemDataAcquisitor() throws Exception {
        // setup
        this.setupSystems(15, 5);
        final SCCSystemRegistrationContext sccSystemRegistrationContext =
                new SCCSystemRegistrationContext(null, getTestSystems(), null);

        // pre-conditions
        assertEquals(20, sccSystemRegistrationContext.getItems().size());
        assertEquals(20,
                sccSystemRegistrationContext.getItems().stream()
                        .filter(SCCRegCacheItem::isSccRegistrationRequired)
                        .count()
        );
        assertEquals(0, sccSystemRegistrationContext.getPendingRegistrationSystems().size());
        assertEquals(0, sccSystemRegistrationContext.getItemsBySccSystemId().size());
        assertEquals(0, sccSystemRegistrationContext.getPaygSystems().size());

        // execution
        new SCCSystemRegistrationSystemDataAcquisitor().handle(sccSystemRegistrationContext);

        // assertions
        assertEquals(20,
                sccSystemRegistrationContext.getItems().stream()
                        .filter(SCCRegCacheItem::isSccRegistrationRequired)
                        .count()
        );
        assertEquals(15, sccSystemRegistrationContext.getPendingRegistrationSystems().size());
        assertEquals(
                sccSystemRegistrationContext.getPendingRegistrationSystems().keySet(),
                sccSystemRegistrationContext.getItemsBySccSystemId().keySet()
        );
        assertEquals(5, sccSystemRegistrationContext.getPaygSystems().size());
    }


    /**
     * Tests the scenario where getPayload processes physical server instance with minimal data provided with no
     * installed products but login and password information available.
     * @throws Exception if the test setup fails
     */
    @Test
    public void testGetPayloadWhenPhysicalServerAndProducts() throws Exception {
        final Date testBeginTimestamp = new Date();

        // SCC setup
        this.setupSystems(1, 0);

        //
        final String expectedHostname = "physicalServeHostname";
        final long expectedCpus = 4L;
        final long expectedSockets = 2L;
        final long expectedRam = 8765;

        SUSEProduct baseProduct =
                SUSEProductTestUtils.createTestSUSEProduct(ChannelFamilyFactoryTest.createTestChannelFamily());
        SUSEProduct addonProduct =
                SUSEProductTestUtils.createTestSUSEProduct(ChannelFamilyFactoryTest.createTestChannelFamily());
        Long[] targetAddonProducts = new Long[] {addonProduct.getId()};
        final SUSEProductSet suseProductSetMock = new SUSEProductSet(baseProduct.getId(), List.of(targetAddonProducts));

        SCCRegCacheItemMock sccRegCacheItemMock = new SCCRegCacheItemMockBuilder(false, true)
                .suseProductSet(suseProductSetMock)
                .hostname(expectedHostname)
                .serverArchLabel("server0-arch-mock")
                .cpus(expectedCpus)
                .sockets(expectedSockets)
                .ram(expectedRam)
                .build();

        // Execute
        new SCCSystemRegistrationSystemDataAcquisitor().handle(sccRegCacheItemMock.getContextMock());

        // Assertions
        Map<SCCSystemId, SCCRegisterSystemJson> pendingRegistrationSystems =
                sccRegCacheItemMock.getPendingRegistrationSystems();
        assertEquals(1, pendingRegistrationSystems.size());
        SCCRegisterSystemJson sccRegisterSystemJson = pendingRegistrationSystems.values().iterator().next();
        assertEquals(SCCRegCacheItemMock.SCC_LOGIN, sccRegisterSystemJson.getLogin());
        assertEquals(SCCRegCacheItemMock.SCC_PWD, sccRegisterSystemJson.getPassword());
        assertEquals(expectedHostname, sccRegisterSystemJson.getHostname());
        assertEquals(2, sccRegisterSystemJson.getProducts().size());
        assertTrue(!sccRegisterSystemJson.getLastSeenAt().before(testBeginTimestamp));

        assertNotNull(sccRegisterSystemJson.getHwinfo());
        SCCHwInfoJson hwInfo = sccRegisterSystemJson.getHwinfo();
        assertEquals(expectedCpus, hwInfo.getCpus());
        assertEquals(expectedSockets, hwInfo.getSockets());
        assertEquals("server0", hwInfo.getArch());
        assertNull(hwInfo.getUuid());
        assertEquals(expectedRam, hwInfo.getMemTotal());
        assertNull(hwInfo.getHypervisor());
        assertNull(hwInfo.getCloudProvider());
    }

    /**
     * Tests the scenario where getPayload processes virtual server instance with minimal data provided with no
     * installed products but login and password information available.
     * @throws Exception if the test setup fails
     */
    @Test
    public void testGetPayloadWhenVirtualServerHasMinimalData() throws Exception {
        final Date testBeginTimestamp = new Date();

        //
        final String expectedHostname = "virtualServerHostname";
        final long expectedCpus = 0;
        final long expectedSockets = 0;

        // SCC setup
        this.setupSystems(1, 0);

        // Mock setup
        SCCRegCacheItemMock sccRegCacheItemMock = new SCCRegCacheItemMockBuilder(true, false)
                .hostname(expectedHostname)
                .serverArchLabel("server1-arch-mock")
                .cpus(expectedCpus)
                .sockets(expectedSockets)
                .build();

        // Execute
        new SCCSystemRegistrationSystemDataAcquisitor().handle(sccRegCacheItemMock.getContextMock());

        // Assertions
        Map<SCCSystemId, SCCRegisterSystemJson> pendingRegistrationSystems =
                sccRegCacheItemMock.getPendingRegistrationSystems();
        assertEquals(1, pendingRegistrationSystems.size());
        SCCRegisterSystemJson sccRegisterSystemJson = pendingRegistrationSystems.values().iterator().next();
        assertEquals(SCCRegCacheItemMock.SCC_LOGIN, sccRegisterSystemJson.getLogin());
        assertEquals(SCCRegCacheItemMock.SCC_PWD, sccRegisterSystemJson.getPassword());
        assertEquals(expectedHostname, sccRegisterSystemJson.getHostname());
        assertEquals(0, sccRegisterSystemJson.getProducts().size());
        assertTrue(!sccRegisterSystemJson.getLastSeenAt().before(testBeginTimestamp));

        assertNotNull(sccRegisterSystemJson.getHwinfo());
        SCCHwInfoJson hwInfo = sccRegisterSystemJson.getHwinfo();
        assertEquals(expectedCpus, hwInfo.getCpus());
        assertEquals(expectedSockets, hwInfo.getSockets());
        assertEquals("server1", hwInfo.getArch());
        assertNull(hwInfo.getUuid());
        assertEquals(0, hwInfo.getMemTotal());
        assertTrue(hwInfo.getHypervisor().isEmpty());
        assertTrue(hwInfo.getCloudProvider().isEmpty());
    }

    /**
     * Tests the scenario where getPayload processes a virtual server instance with full data provided;
     * both with no installed products but login and password information available.
     * @throws Exception if the test setup fails
     */
    @Test
    public void testGetPayloadWhenVirtualServerHasFullData() throws Exception {
        final Date testBeginTimestamp = new Date();

        // SCC setup
        this.setupSystems(1, 0);

        final String expectedCloudProvider = "Amazon";
        final String expectedHypervisor = "KVM";
        final String expectedHostname = "virtualServerHostname";
        final String expectedUuid = "018d9d33-f73c-79f6-a21c-05722fd4ff22";
        long expectedTotalMemory = 2048L;

        SCCRegCacheItemMock sccRegCacheItemMock = new SCCRegCacheItemMockBuilder(true, false)
                .cloudProvider(expectedCloudProvider)
                .hypervisor(expectedHypervisor)
                .hostname(expectedHostname)
                .uuid(expectedUuid)
                .serverArchLabel("server2-arch-mock")
                .totalMemory(expectedTotalMemory)
                .build();

        // Execute
        new SCCSystemRegistrationSystemDataAcquisitor().handle(sccRegCacheItemMock.getContextMock());

        // Assertions
        Map<SCCSystemId, SCCRegisterSystemJson> pendingRegistrationSystems =
                sccRegCacheItemMock.getPendingRegistrationSystems();
        assertEquals(1, pendingRegistrationSystems.size());
        SCCRegisterSystemJson sccRegisterSystemJson = pendingRegistrationSystems.values().iterator().next();
        assertEquals(SCCRegCacheItemMock.SCC_LOGIN, sccRegisterSystemJson.getLogin());
        assertEquals(SCCRegCacheItemMock.SCC_PWD, sccRegisterSystemJson.getPassword());
        assertEquals(expectedHostname, sccRegisterSystemJson.getHostname());
        assertEquals(0, sccRegisterSystemJson.getProducts().size());
        assertTrue(!sccRegisterSystemJson.getLastSeenAt().before(testBeginTimestamp));

        assertNotNull(sccRegisterSystemJson.getHwinfo());
        SCCHwInfoJson hwInfo = sccRegisterSystemJson.getHwinfo();
        assertEquals(0L, hwInfo.getCpus());
        assertEquals(0L, hwInfo.getSockets());
        assertEquals("server2", hwInfo.getArch());
        assertEquals(expectedUuid, hwInfo.getUuid());
        assertEquals(expectedTotalMemory, hwInfo.getMemTotal());
        assertEquals(expectedHypervisor, hwInfo.getHypervisor());
        assertEquals(expectedCloudProvider, hwInfo.getCloudProvider());
    }

    class SCCRegCacheItemMockBuilder {
        private final boolean isVirtualGuest;
        private final boolean hasCpuInfo;

        private SUSEProductSet suseProductSet;
        private Long cpus;
        private Long sockets;
        private String serverArchLabel;
        private String hypervisor;
        private String cloudProvider;
        private String hostname;
        private String uuid;
        private Long totalMemory;
        private Long ram;

        SCCRegCacheItemMockBuilder(boolean isVirtualGuestIn, boolean hasCpuInfoIn) {
            isVirtualGuest = isVirtualGuestIn;
            hasCpuInfo = hasCpuInfoIn;
        }

        public SCCRegCacheItemMockBuilder suseProductSet(SUSEProductSet suseProductSetIn) {
            suseProductSet = suseProductSetIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder cpus(Long cpusIn) {
            cpus = cpusIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder sockets(Long socketsIn) {
            sockets = socketsIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder serverArchLabel(String serverArchLabelIn) {
            serverArchLabel = serverArchLabelIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder hypervisor(String hypervisorIn) {
            hypervisor = hypervisorIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder cloudProvider(String cloudProviderIn) {
            cloudProvider = cloudProviderIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder hostname(String hostnameIn) {
            hostname = hostnameIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder uuid(String uuidIn) {
            uuid = uuidIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder totalMemory(Long totalMemoryIn) {
            totalMemory = totalMemoryIn;
            return this;
        }

        public SCCRegCacheItemMockBuilder ram(Long ramIn) {
            ram = ramIn;
            return this;
        }

        public SCCRegCacheItemMock build() {
            return new SCCRegCacheItemMock(this);
        }
    }

    class SCCRegCacheItemMock {
        public static final String SCC_LOGIN = "sccLogin";
        public static final String SCC_PWD = "sccPasswd";

        // Maps we need to spy on
        private final Map<SCCSystemId, SCCRegisterSystemJson> pendingRegistrationSystems = new HashMap<>();

        private final SCCSystemRegistrationContext contextMock;

        SCCRegCacheItemMock(SCCRegCacheItemMockBuilder builder) {
            // So you can mock more than just interfaces
            context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

            this.contextMock = context.mock(SCCSystemRegistrationContext.class);

            final CPU cpuMock = context.mock(CPU.class);
            final SCCRegCacheItem cacheItemMock = context.mock(SCCRegCacheItem.class);
            final Server serverMock = context.mock(Server.class);
            final ServerArch serverArchMock = context.mock(ServerArch.class);
            final ServerInfo serverInfoMock = context.mock(ServerInfo.class);
            final VirtualInstance virtualInstanceMock = context.mock(VirtualInstance.class);
            final VirtualInstanceType virtualInstanceTypeMock = context.mock(VirtualInstanceType.class);

            final Map<SCCSystemId, SCCRegCacheItem> itemsBySccSystemId = new HashMap<>();

            // Mock expectations
            context.checking(new Expectations() {{
                // Base expectations to get into getPayload method
                allowing(contextMock).getItems(); will(returnValue(Arrays.asList(cacheItemMock)));
                allowing(cacheItemMock).getOptServer(); will(returnValue(Optional.of(serverMock)));
                allowing(serverMock).isForeign(); will(returnValue(false));
                allowing(serverMock).isPayg(); will(returnValue(false));
                allowing(contextMock).getItemsBySccSystemId(); will(returnValue(itemsBySccSystemId));
                allowing(contextMock).getPendingRegistrationSystems(); will(returnValue(pendingRegistrationSystems));

                //  Installed products
                allowing(serverMock).getInstalledProductSet(); will(returnValue(ofNullable(builder.suseProductSet)));

                // CPU information
                if(builder.hasCpuInfo) {
                    allowing(serverMock).getCpu(); will(returnValue(cpuMock));
                }
                allowing(cpuMock).getNrCPU(); will(returnValue(builder.cpus));
                allowing(cpuMock).getNrsocket(); will(returnValue(builder.sockets));

                // Arch label setup
                allowing(serverArchMock).getLabel();
                will(returnValue(builder.serverArchLabel));
                allowing(serverMock).getServerArch(); will(returnValue(serverArchMock));

                // Set as virtual guest
                allowing(serverMock).isVirtualGuest(); will(returnValue(builder.isVirtualGuest));
                allowing(serverMock).getVirtualInstance(); will(returnValue(virtualInstanceMock));
                allowing(virtualInstanceMock).getType(); will(returnValue(virtualInstanceTypeMock));
                allowing(virtualInstanceTypeMock).getHypervisor(); will(returnValue(ofNullable(builder.hypervisor)));
                allowing(virtualInstanceTypeMock).getCloudProvider();
                will(returnValue(ofNullable(builder.cloudProvider)));
                allowing(virtualInstanceMock).getUuid(); will(returnValue(builder.uuid));
                allowing(virtualInstanceMock).getTotalMemory(); will(returnValue(builder.totalMemory));
                allowing(serverMock).getRam(); will(returnValue(builder.ram));

                // SCC login and password setup
                allowing(cacheItemMock).getOptSccLogin(); will(returnValue(Optional.of(SCC_LOGIN)));
                allowing(cacheItemMock).getOptSccPasswd(); will(returnValue(Optional.of(SCC_PWD)));

                // Additional data required for SCCRegisterSystemJson
                allowing(serverMock).getHostname(); will(returnValue(builder.hostname));
                allowing(serverMock).getServerInfo(); will(returnValue(serverInfoMock));
                allowing(serverInfoMock).getCheckin(); will(returnValue(new Date()));

            }});

        }

        public Map<SCCSystemId, SCCRegisterSystemJson> getPendingRegistrationSystems() {
            return pendingRegistrationSystems;
        }

        public SCCSystemRegistrationContext getContextMock() {
            return contextMock;
        }
    }
}
