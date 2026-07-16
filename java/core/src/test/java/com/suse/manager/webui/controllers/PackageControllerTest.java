/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.ChannelTestUtility;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spark.Request;
import spark.Response;

public class PackageControllerTest extends BaseControllerTestCase {

    private static final Gson GSON = new Gson();

    @Test
    public void testChannelPackagesSelectAllRespectsPackageNameFilter() throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Channel otherChannel = ChannelFactoryTest.createTestChannel(user);

        Package matchingPackageOne = PackageTest.createTestPackage(user.getOrg(), "alpha-package-one");
        Package matchingPackageTwo = PackageTest.createTestPackage(user.getOrg(), "alpha-package-two");
        Package nonMatchingPackage = PackageTest.createTestPackage(user.getOrg(), "omega-package");
        Package otherChannelPackage = PackageTest.createTestPackage(user.getOrg(), "alpha-package-three");

        ChannelTestUtility.testAddPackage(channel, matchingPackageOne);
        ChannelTestUtility.testAddPackage(channel, matchingPackageTwo);
        ChannelTestUtility.testAddPackage(channel, nonMatchingPackage);
        ChannelTestUtility.testAddPackage(otherChannel, otherChannelPackage);
        TestUtils.flushSession();

        Request request = getRequestWithCsrfAndParams(
                "/manager/api/packages/list/:binary/channel/:cid",
                Map.of("f", "id", "q", "alpha"),
                "binary",
                channel.getId()
        );

        assertEquals(
                Set.of(matchingPackageOne.getId(), matchingPackageTwo.getId()),
                Set.copyOf(invokeChannelPackages(request, response, user))
        );
    }

    private List<Long> invokeChannelPackages(Request request, Response responseIn, User userIn) throws Exception {
        Method method = PackageController.class.getDeclaredMethod("channelPackages",
                Request.class, Response.class, User.class);
        method.setAccessible(true);

        String result = (String) method.invoke(null, request, responseIn, userIn);
        return GSON.fromJson(result, new TypeToken<List<Long>>() { }.getType());
    }
}
