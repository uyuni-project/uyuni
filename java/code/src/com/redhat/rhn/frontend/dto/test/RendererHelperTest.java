/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

package com.redhat.rhn.frontend.dto.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.renderers.RendererHelper;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.errata.cache.UpdateErrataCacheCommand;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * RendererHelperTest
 * @version $Rev$
 */
public class RendererHelperTest extends RhnBaseTestCase {

    private static final int PAGE_SIZE = 5;

    private static final int TOTAL_SERVERS_COUNT = PAGE_SIZE + 5;
    private static final int EQUAL_SERVERS_COUNT = TOTAL_SERVERS_COUNT - (PAGE_SIZE / 2);

    public void testSortOverviews() throws Exception {
        DataResult<SystemOverview> dataResult = this.createMockSystemsList();

        RendererHelper.sortOverviews(dataResult);

        this.assertSystemsList(dataResult);
    }

    private DataResult<SystemOverview> createMockSystemsList() throws Exception {
        User user = ErrataTestUtils.createTestUser();
        Channel channel = ErrataTestUtils.createTestChannel(user);

        int errataCount;

        for (int i = 0; i < TOTAL_SERVERS_COUNT; i++) {
            errataCount = EQUAL_SERVERS_COUNT;

            //condition just have more than one server with same data
            if (i <  TOTAL_SERVERS_COUNT - EQUAL_SERVERS_COUNT) {
                errataCount = TOTAL_SERVERS_COUNT - i;
            }
            this.createCriticalServerWithErrataCount(user, channel, errataCount, i);
        }

        PageControl pageControl = new PageControl();
        pageControl.setStart(1);
        pageControl.setPageSize(PAGE_SIZE);

        DataResult<SystemOverview> criticalSystems =
                SystemManager.mostCriticalSystems(user, pageControl);

        return criticalSystems;
    }

    private void createCriticalServerWithErrataCount(User user, Channel channel,
            int errataCount, int serverIndex)
        throws Exception {
        Set<Channel> channels = new HashSet<Channel>();
        channels.add(channel);
        Server server = ErrataTestUtils.createTestServerWithName(user, channels,
                "server_" + serverIndex);

        // ERRATA
        String cveName = TestUtils.randomString().substring(0, 13);
        Cve cve = ErrataTestUtils.createTestCve(cveName);
        Set<Cve> cves = new HashSet<Cve>();
        cves.add(cve);

        for (int i = 0; i < errataCount; i++) {
            Errata errata = ErrataTestUtils.createTestErrata(user, cves);
            channel.addErrata(errata);

            TestUtils.saveAndFlush(channel);

            Package unpatched = ErrataTestUtils.createTestPackage(user, channel, "noarch");
            ErrataTestUtils.createLaterTestPackage(user, errata, channel, unpatched);

            ErrataTestUtils.createTestInstalledPackage(unpatched, server);
        }
        UpdateErrataCacheCommand uECC = new UpdateErrataCacheCommand();
        uECC.updateErrataCacheForServer(server.getId(), false);
    }

    private void assertSystemsList(List<SystemOverview> in) {
        // check that the first (TOTAL_SERVERS_COUNT - EQUAL_SERVERS_COUNT) systems
        // contains the biggest errata amount
        IntStream.range(0, TOTAL_SERVERS_COUNT - EQUAL_SERVERS_COUNT).forEach(i -> {
            int errataCount = TOTAL_SERVERS_COUNT - i;
            assertEquals(Long.valueOf(errataCount), in.get(i).getTotalErrataCount());
        });

        // check that the rest of the systems contains the EQUAL_SERVERS_COUNT errata amount
        // ordered by name
        IntStream.range(TOTAL_SERVERS_COUNT - EQUAL_SERVERS_COUNT, in.size()).forEach(i -> {
            assertEquals(Long.valueOf(EQUAL_SERVERS_COUNT), in.get(i).getTotalErrataCount());
            assertEquals("server_" + i, in.get(i).getServerName());
        });
    }

}
