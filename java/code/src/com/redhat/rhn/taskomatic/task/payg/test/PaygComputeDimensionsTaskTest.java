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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.taskomatic.task.payg.PaygComputeDimensionsTask;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionsConfiguration;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.domain.BillingDimension;
import com.suse.cloud.domain.PaygDimensionComputation;
import com.suse.cloud.domain.PaygDimensionFactory;
import com.suse.cloud.domain.PaygDimensionResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;

import java.util.Date;
import java.util.Optional;

public class PaygComputeDimensionsTaskTest extends JMockBaseTestCaseWithUser {

    private JobExecutionContext contextMock;

    private PaygDimensionFactory factory;

    private CloudPaygManager cloudManager;

    @BeforeEach
    public void before() {
        contextMock = mock(JobExecutionContext.class);
        // Force being in PAYG context
        cloudManager = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return true;
            }
        };

        factory = new PaygDimensionFactory();
    }

    @Test
    public void canComputeDimensions() throws Exception {
        // Creating the servers
        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.setPayg(true);
        ServerFactory.save(server1);

        Server server2 = MinionServerFactoryTest.createTestMinionServer(user);

        ServerTestUtils.createTestSystem(user);
        ServerFactoryTest.createUnentitledTestServer(user, true, ServerFactoryTest.TYPE_SERVER_PROXY, new Date());

        ChannelFamily channelFamily = ErrataTestUtils.createTestChannelFamily();
        SUSEProduct testProduct1 = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "sles_sap");
        SUSEProduct testProduct2 = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);

        SUSEProductTestUtils.installSUSEProductOnServer(testProduct1, server1);
        SUSEProductTestUtils.installSUSEProductOnServer(testProduct2, server2);

        var task = new PaygComputeDimensionsTask(DimensionsConfiguration.DEFAULT_CONFIGURATION, factory, cloudManager);

        task.execute(contextMock);

        PaygDimensionComputation result = factory.getLatestSuccessfulComputation();
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getDimensionResults().size());

        Optional<PaygDimensionResult> managedSystems = result.getResultForDimension(BillingDimension.MANAGED_SYSTEMS);
        assertTrue(managedSystems.isPresent());
        managedSystems.ifPresent(dimensionResult -> assertEquals(2L, dimensionResult.getCount()));

        Optional<PaygDimensionResult> monitoringSystems = result.getResultForDimension(BillingDimension.MONITORING);
        assertTrue(monitoringSystems.isPresent());
        monitoringSystems.ifPresent(dimensionResult -> assertEquals(0L, dimensionResult.getCount()));
    }
}
