/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.payg;

import static com.redhat.rhn.domain.product.SUSEProductTestUtils.createTestSUSEProduct;
import static com.redhat.rhn.domain.product.SUSEProductTestUtils.installSUSEProductsOnServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionsConfiguration;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.TestCloudPaygManagerBuilder;
import com.suse.cloud.domain.BillingDimension;
import com.suse.cloud.domain.PaygDimensionComputation;
import com.suse.cloud.domain.PaygDimensionFactory;
import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.iface.SaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;

import java.util.Date;
import java.util.Set;

class PaygComputeDimensionsTaskTest extends JMockBaseTestCaseWithUser {

    private static final String ARCH_X86_64 = "x86_64";
    private static final String ARCH_IBM_Z = "s390x";

    private JobExecutionContext contextMock;
    private PaygDimensionFactory factory;
    private CloudPaygManager cloudManager;
    private SystemEntitlementManager systemEntitlementManager;

    @BeforeEach
    void before() {
        contextMock = mock(JobExecutionContext.class);
        // Force being in PAYG context
        cloudManager = new TestCloudPaygManagerBuilder().withPaygInstance().build();

        factory = new PaygDimensionFactory();
        SaltApi saltApi = new TestSaltApi();

        systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi),
            new SystemEntitler(saltApi)
        );
    }

    @Test
    void canComputeManagedSystemAndMonitoring() throws Exception {
        assertTrue(ServerFactory.listAllServerIds().isEmpty(), "Unable to execute test: " +
            "there are entries the rhnserver table not cleaned up by previous unit tests.");

        // Creating the servers            //   MANAGED_SYSTEMS   MONITORING
        createPaygSLESSapServer();         //
        createSLES12Server();              //         X
        createOpenSUSEServer();            //         X              X
        createSUSEManagerProxy();          //
        createTraditionalClient();         //         X

        assertEquals(5, ServerFactory.listAllServerIds().size(), "The expected servers were not created");

        var task = new PaygComputeDimensionsTask(DimensionsConfiguration.DEFAULT_CONFIGURATION, factory, cloudManager);

        task.execute(contextMock);

        PaygDimensionComputation computationResult = factory.getLatestSuccessfulComputation();

        assertNotNull(computationResult, "No successful computation found");
        assertTrue(computationResult.isSuccess(), "Computation was not successful");
        assertResultsCount(computationResult, DimensionsConfiguration.DEFAULT_CONFIGURATION.getDimensions().size(), 2);

        assertDimensionResult(computationResult, BillingDimension.MANAGED_SYSTEMS, 3L);
        assertDimensionResult(computationResult, BillingDimension.MONITORING, 1L);
    }

    @Test
    void canComputeMultiLinuxSupportDimensions() throws Exception {
        assertTrue(ServerFactory.listAllServerIds().isEmpty(), "Unable to execute test: " +
            "there are entries the rhnserver table not cleaned up by previous unit tests.");

        SUSEProduct sll9 = createTestSUSEProduct(user, "sll", "9", ARCH_X86_64, "RES", true);
        SUSEProduct sll9Ems = createTestSUSEProduct(user, "sll-ems", "9.6", ARCH_X86_64, "RES-EMS", false);
        SUSEProduct sll9Ha = createTestSUSEProduct(user, "sll-ha", "9", ARCH_X86_64, "RES-HA", false);

        SUSEProduct res7 = createTestSUSEProduct(user, "res", "7", ARCH_X86_64, "RES", true);
        SUSEProduct res7Ltss = createTestSUSEProduct(user, "res-ltss", "7", ARCH_X86_64, "RES-7-LTSS-X86", true);

        SUSEProduct sll8 = createTestSUSEProduct(user, "res", "8", ARCH_X86_64, "RES", true);
        SUSEProduct sll8Ha = createTestSUSEProduct(user, "res-ha", "8", ARCH_X86_64, "RES-HA", false);

        SUSEProduct sles = createTestSUSEProduct(user, "sles", "15.4", ARCH_X86_64, "SLES", true);

        // Creating the servers                         //   MLS   LTSS   EMS   HA
        createMinionServerWithProducts(sll9);           //    X
        createMinionServerWithProducts(sll8, sll8Ha);   //    X                 X
        createMinionServerWithProducts(res7);           //    X
        createMinionServerWithProducts(res7Ltss);       //    X     X
        createMinionServerWithProducts(sll9, sll9Ems);  //    X            X
        createMinionServerWithProducts(sll9, sll9Ha);   //    X                 X
        createMinionServerWithProducts(sles);           //

        assertEquals(7, ServerFactory.listAllServerIds().size(), "The expected servers were not created");

        var task = new PaygComputeDimensionsTask(DimensionsConfiguration.DEFAULT_CONFIGURATION, factory, cloudManager);

        task.execute(contextMock);

        PaygDimensionComputation computationResult = factory.getLatestSuccessfulComputation();

        assertNotNull(computationResult, "No successful computation found");
        assertTrue(computationResult.isSuccess(), "Computation was not successful");
        assertResultsCount(computationResult, DimensionsConfiguration.DEFAULT_CONFIGURATION.getDimensions().size(), 4);

        assertDimensionResult(computationResult, BillingDimension.MULTI_LINUX_SUPPORT, 6L);
        assertDimensionResult(computationResult, BillingDimension.MULTI_LINUX_SUPPORT_LTSS, 1L);
        assertDimensionResult(computationResult, BillingDimension.MULTI_LINUX_SUPPORT_EMS, 1L);
        assertDimensionResult(computationResult, BillingDimension.MULTI_LINUX_SUPPORT_HA, 2L);
    }

    private void assertResultsCount(PaygDimensionComputation computationResult, int total, int effective) {
        // Check the total number of results, it should match the configuration
        assertEquals(
            total,
            computationResult.getDimensionResults().size(),
            "Number of results does not match the current configuration"
        );

        // Check how many dimensions actually have a count greater than zero
        assertEquals(
            effective,
            computationResult.getDimensionResults().stream().filter(result -> result.getCount() != 0).count(),
            "Exactly " + effective + " dimensions should have a non-zero result"
        );
    }

    private static void assertDimensionResult(PaygDimensionComputation computationResult, BillingDimension dimension,
                                             long expectedCount) {
        computationResult.getResultForDimension(dimension)
            .ifPresentOrElse(
                result -> assertEquals(expectedCount, result.getCount(), "Wrong count for dimension " + dimension),
                () -> fail("No result for dimension " + dimension)
            );
    }

    private void createPaygSLESSapServer() {
        Server server = createMinion(ARCH_X86_64, true);

        installSUSEProductsOnServer(server, Set.of(
            createTestSUSEProduct(user, "sles_sap", "15.4", ARCH_X86_64, "AiO", true),
            createTestSUSEProduct(user, "sle-module-basesystem", "15.4", ARCH_X86_64, "MODULE", false),
            createTestSUSEProduct(user, "sle-manager-tools", "15", ARCH_X86_64, "SLE-M-T", false)
        ));
    }

    private void createSLES12Server() {
        Server server = createMinion(ARCH_IBM_Z, false);

        installSUSEProductsOnServer(server, Set.of(
            createTestSUSEProduct(user, "sles", "12.3", ARCH_IBM_Z, "SLES-Z", true)
        ));
    }

    private void createOpenSUSEServer() {
        Server server = createMinion(ARCH_X86_64, false);

        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.MONITORING);

        installSUSEProductsOnServer(server, Set.of(
            createTestSUSEProduct(user, "opensuse", "15.2", ARCH_X86_64, "OPENSUSE", true),
            createTestSUSEProduct(user, "sle-manager-tools", "15", ARCH_X86_64, "SLE-M-T", false)
        ));
    }

    private void createSUSEManagerProxy() {
        Server server = createMinion(ARCH_X86_64, false);

        installSUSEProductsOnServer(server, Set.of(
            createTestSUSEProduct(user, "suse-manager-proxy", "4.3", ARCH_X86_64, "SMP", true),
            createTestSUSEProduct(user, "sle-module-basesystem", "15.4", ARCH_X86_64, "MODULE", false),
            createTestSUSEProduct(user, "sle-module-server-applications", "15.4", ARCH_X86_64, "MODULE", false),
            createTestSUSEProduct(user, "sle-module-suse-manager-proxy", "15.4", ARCH_X86_64, "MODULE", false)
        ));
    }

    private void createTraditionalClient() {
        Server server = ServerFactoryTest.createUnentitledTestServer(
            user, true, ServerFactoryTest.TYPE_SERVER_NORMAL, new Date()
        );

        server.setServerArch(ServerFactory.lookupServerArchByName(ARCH_X86_64));
        ServerFactory.save(server);

        systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.MANAGEMENT);

        installSUSEProductsOnServer(server, Set.of(
            createTestSUSEProduct(user, "sles_sap", "12", ARCH_X86_64, "AiO", true),
            createTestSUSEProduct(user, "sles-ltss", "12", ARCH_X86_64, "SLES12-GA-LTSS-X86", false),
            createTestSUSEProduct(user, "sle-module-legacy", "12", ARCH_X86_64, "MODULE", false)
        ));
    }

    private void createMinionServerWithProducts(SUSEProduct... products) {
        Server server = createMinion(ARCH_X86_64, false);
        installSUSEProductsOnServer(server, Set.of(products));
    }

    private Server createMinion(String arch, boolean payg) {
        Server server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setServerArch(ServerFactory.lookupServerArchByName(arch));
        server.setPayg(payg);
        ServerFactory.save(server);
        return server;
    }

}
