/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.task.ReportDBHelper;
import com.redhat.rhn.taskomatic.task.ReportDbUpdateTask;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.PackageTestUtils;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.Tuple;

public class ReportDbUpdateTaskTest extends JMockBaseTestCaseWithUser {

    private static ConnectionManager reportDbConnectionManager = null;

    private JobExecutionContext contextMock;

    @BeforeEach
    public void before() {
        contextMock = mock(JobExecutionContext.class);
    }

    @AfterEach
    public void after() {
        if (reportDbConnectionManager != null) {
            reportDbConnectionManager.closeSession();
        }
    }

    @Test
    public void smokeTest() {
        ReportDbUpdateTask task = new ReportDbUpdateTask();

        // Just run the task and verify that all the queries run smoothly.
        // This will at least guarantee that the sql queries are syntactically correct
        assertDoesNotThrow(() -> task.execute(contextMock));
    }

    @Test
    public void canSyncTablesUsingAQueryById() throws Exception {
        // Set up the data for testing the query SystemPackageUpdate_byId

        // Create a channel
        Channel channel = ChannelTestUtils.createBaseChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));

        // Create some servers
        List<Server> servers = IntStream.range(1, 5)
            .mapToObj(index -> ServerFactoryTest.createTestServer(user))
            .collect(Collectors.toList());

        // Store the packages we create to be able to verify the data at the end
        Map<Long, List<Package>> updatablePackagesMap = new HashMap<>();

        servers.forEach(server -> {
            // Subscribe the server to the channel
            SystemManager.subscribeServerToChannel(user, server, channel);

            // Create some packages and add them to the channel
            List<Package> packages = IntStream.range(1, 10)
                .mapToObj(index -> PackageTest.createTestPackage(user.getOrg()))
                .collect(Collectors.toList());
            channel.getPackages().addAll(packages);


            // Create a newer version for each of the packages and add  them to the channel as well
            List<Package> updatedPackages = packages.stream()
                .map(originalPackage -> PackageTestUtils.newVersionOfPackage(originalPackage, null, "2.0.0", null,
                    user.getOrg()))
                .collect(Collectors.toList());
            channel.getPackages().addAll(updatedPackages);
            updatablePackagesMap.put(server.getId(), updatedPackages);

            // Install the original package on the server
            PackageTestUtils.installPackagesOnServer(packages, server);
            // Update the server needed cache
            ServerFactory.updateServerNeededCache(server.getId());
        });

        // Keep a low batch size to iterate multiple times
        ReportDbUpdateTask task = new ReportDbUpdateTask(ReportDBHelper.INSTANCE, 2);

        assertDoesNotThrow(() -> task.execute(contextMock));

        // Verify the results for each server
        String query = "SELECT package_id, name, epoch, version, release, arch, type " +
            "FROM SystemPackageUpdate " +
            "WHERE mgm_id = 1 AND system_id = :id " +
            "ORDER BY package_id";

        servers.forEach(testServer -> {
            List<Tuple> resultList = getSession()
                .createNativeQuery(query, Tuple.class)
                .setParameter("id", testServer.getId())
                .getResultList();
            assertFalse(CollectionUtils.isEmpty(resultList),
                "The updatable packages result list should not be empty");

            List<Package> updatablePackagesForServer = updatablePackagesMap.get(testServer.getId()).stream()
                .sorted(Comparator.comparing(pck -> pck.getId()))
                .collect(Collectors.toList());

            assertFalse(CollectionUtils.isEmpty(updatablePackagesForServer),
                "The updatable packages reference list should not be empty");

            // Check the results are consistent
            assertEquals(updatablePackagesForServer.size(), resultList.size());
            IntStream.range(0, updatablePackagesForServer.size()).forEach(index ->
                assertAll(
                    "Package fields do not match",
                    () -> assertEquals(updatablePackagesForServer.get(index).getId(),
                        resultList.get(index).get("package_id", BigDecimal.class).longValue()),
                    () -> assertEquals(updatablePackagesForServer.get(index).getPackageName().getName(),
                        resultList.get(index).get("name", String.class)),
                    () -> assertEquals(updatablePackagesForServer.get(index).getPackageEvr().getEpoch(),
                        resultList.get(index).get("epoch", String.class)),
                    () -> assertEquals(updatablePackagesForServer.get(index).getPackageEvr().getVersion(),
                        resultList.get(index).get("version", String.class)),
                    () -> assertEquals(updatablePackagesForServer.get(index).getPackageEvr().getRelease(),
                        resultList.get(index).get("release", String.class)),
                    () -> assertEquals(updatablePackagesForServer.get(index).getPackageArch().getLabel(),
                        resultList.get(index).get("arch", String.class)),
                    () -> assertEquals(updatablePackagesForServer.get(index).getPackageEvr().getType(),
                        resultList.get(index).get("type", String.class))
                )
            );
        });
    }

    private static synchronized Session getSession() {
        if (reportDbConnectionManager == null) {
            reportDbConnectionManager = ConnectionManagerFactory.localReportingConnectionManager();
        }

        return reportDbConnectionManager.getSession();
    }
}
