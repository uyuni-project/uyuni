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

package com.redhat.rhn.taskomatic.task.payg.test;

import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProduct;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.installSUSEProductsOnServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.task.payg.PaygComputeDimensionsTask;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionsConfiguration;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.domain.BillingDimension;
import com.suse.cloud.domain.PaygDimensionComputation;
import com.suse.cloud.domain.PaygDimensionFactory;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;

import java.util.Date;
import java.util.Set;

public class PaygComputeDimensionsTaskTest extends JMockBaseTestCaseWithUser {

    private static final String ARCH_X86_64 = "x86_64";
    private static final String ARCH_IBM_Z = "s390x";

    private JobExecutionContext contextMock;
    private PaygDimensionFactory factory;
    private CloudPaygManager cloudManager;
    private SystemEntitlementManager systemEntitlementManager;

    @BeforeEach
    public void before() {
        contextMock = mock(JobExecutionContext.class);
        // Force being in PAYG context
        cloudManager = new TestCloudPaygManagerBuilder().withPaygInstance().build();

        factory = new PaygDimensionFactory();
        SaltApi saltApi = new TestSaltApi();

        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        VirtManager virtManager = new VirtManagerSalt(saltApi);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);

        systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
            new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
        );
    }

    @Test
    public void canComputeDimensions() throws Exception {
        assertTrue(ServerFactory.listAllServerIds().isEmpty(), "Unable to execute test: " +
            "there are entries the rhnserver table not cleaned up by previous unit tests.");

        // Creating the servers            //   MANAGED_SYSTEMS   MONITORING
        createPaygSLESSapServer();         //
        createSLES12Server();              //         X
        createOpenSUSEServer();            //         X              X
        createSUSEManagerProxy();          //
        createTraditionalClient();         //         X

        assertEquals(ServerFactory.listAllServerIds().size(), 5, "The expected servers were not created");

        var task = new PaygComputeDimensionsTask(DimensionsConfiguration.DEFAULT_CONFIGURATION, factory, cloudManager);

        task.execute(contextMock);

        PaygDimensionComputation computationResult = factory.getLatestSuccessfulComputation();

        assertNotNull(computationResult, "No successful computation found");
        assertTrue(computationResult.isSuccess(), "Computation was not successful");
        assertEquals(2, computationResult.getDimensionResults().size(), "Wrong number of result returned");

        // Check the result for the MANAGED_SYSTEMS dimension
        computationResult.getResultForDimension(BillingDimension.MANAGED_SYSTEMS)
                         .ifPresentOrElse(
                             result -> assertEquals(3L, result.getCount(), "Wrong count for dimension MANAGED_SYSTEMS"),
                             () -> fail("No result for dimension MANAGED_SYSTEMS")
                         );

        // Check the result for the MONITORING dimension
        computationResult.getResultForDimension(BillingDimension.MONITORING)
                         .ifPresentOrElse(
                             result -> assertEquals(1L, result.getCount(), "Wrong count for dimension MONITORING"),
                             () -> fail("No result for dimension MONITORING")
                         );
    }

    private void createPaygSLESSapServer() {
        Server server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setServerArch(ServerFactory.lookupServerArchByName(ARCH_X86_64));
        server.setPayg(true);
        ServerFactory.save(server);

        installSUSEProductsOnServer(server, Set.of(
            createTestSUSEProduct(user, "sles_sap", "15.4", ARCH_X86_64, "AiO", true),
            createTestSUSEProduct(user, "sle-module-basesystem", "15.4", ARCH_X86_64, "MODULE", false),
            createTestSUSEProduct(user, "sle-manager-tools", "15", ARCH_X86_64, "SLE-M-T", false)
        ));
    }

    private void createSLES12Server() {
        Server server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setServerArch(ServerFactory.lookupServerArchByName(ARCH_IBM_Z));
        ServerFactory.save(server);

        installSUSEProductsOnServer(server, Set.of(
            createTestSUSEProduct(user, "sles", "12.3", ARCH_IBM_Z, "SLES-Z", true)
        ));
    }

    private void createOpenSUSEServer() {
        Server server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setServerArch(ServerFactory.lookupServerArchByName(ARCH_X86_64));
        ServerFactory.save(server);

        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.MONITORING);

        installSUSEProductsOnServer(server, Set.of(
            createTestSUSEProduct(user, "opensuse", "15.2", ARCH_X86_64, "OPENSUSE", true),
            createTestSUSEProduct(user, "sle-manager-tools", "15", ARCH_X86_64, "SLE-M-T", false)
        ));
    }

    private void createSUSEManagerProxy() {
        Server server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setServerArch(ServerFactory.lookupServerArchByName(ARCH_X86_64));
        ServerFactory.save(server);

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
}
