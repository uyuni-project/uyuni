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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.taskomatic.task.ReportDBHelper;
import com.redhat.rhn.taskomatic.task.ReportDbUpdateTask;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void doesNotProduceConflictWhenDataChanges() throws Exception {

        // Must clean the existing rhnchannelpackage content to make sure the pagination is the one expected by the test
        HibernateFactory.getSession().createSQLQuery("DELETE FROM rhnchannelpackage").executeUpdate();

        Channel firstChannel = ChannelFactoryTest.createTestChannel(user);
        Channel secondChannel = ChannelFactoryTest.createTestChannel(user);

        List<Package> testPackages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testPackages.add(PackageTest.createTestPackage(user.getOrg()));
        }

        firstChannel.getPackages().addAll(testPackages.subList(1, 3));
        secondChannel.getPackages().addAll(testPackages.subList(3, 5));

        ReportDBHelper testReportDbHelper = new ReportDBHelper() {
            @Override
            public <T> Stream<DataResult<T>> batchStream(SelectMode query, int batchSize, int initialOffset) {
                // If we are processing the second batch for the ChannelPackage table...
                if ("ChannelPackage".equals(query.getName())) {
                    // Add a new package to the first channel. This will change the order of the result and lead to the
                    // same row from rhnchannelpackage to be extracted twice. This should not translate in an exception
                    firstChannel.getPackages().add(testPackages.get(0));
                }
                return super.batchStream(query, batchSize, initialOffset);
            }
        };

        ReportDbUpdateTask task = new ReportDbUpdateTask(testReportDbHelper, 2);


        assertDoesNotThrow(() -> task.execute(contextMock));

        String channelPackageQuery = "SELECT * FROM ChannelPackage WHERE mgm_id = 1 ORDER BY channel_id, package_id";
        List<Tuple> resultList = getSession().createNativeQuery(channelPackageQuery, Tuple.class).getResultList();

        assertEquals(4, resultList.size());

        // Extract a set of pairs from the tuples
        Set<Pair<Long, Long>> pairSet = resultList.stream()
                                                  .map(t -> Pair.of(
                                                          t.get("channel_id", BigDecimal.class).longValue(),
                                                          t.get("package_id", BigDecimal.class).longValue()
                                                      )
                                                  )
                                                  .collect(Collectors.toSet());

        // Build the set of expected pairs
        Set<Pair<Long, Long>> expectedPairSet = Set.of(
            Pair.of(firstChannel.getId(), testPackages.get(1).getId()),
            Pair.of(firstChannel.getId(), testPackages.get(2).getId()),
            Pair.of(secondChannel.getId(), testPackages.get(3).getId()),
            Pair.of(secondChannel.getId(), testPackages.get(4).getId())
        );

        assertEquals(expectedPairSet, pairSet);

    }

    private static synchronized Session getSession() {
        if (reportDbConnectionManager == null) {
            reportDbConnectionManager = ConnectionManagerFactory.localReportingConnectionManager();
        }

        return reportDbConnectionManager.getSession();
    }
}
