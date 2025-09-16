/*
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.manager.task.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageExtraTagsKeys;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.task.TaskManager;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

public class TaskManagerTest extends JMockBaseTestCaseWithUser {

    @Test
    public void testGetChannelPackageExtraTags() throws Exception {
        Channel channel = ChannelFactoryTest.createBaseChannel(user);

        PackageExtraTagsKeys tag1 = PackageManagerTest.createExtraTagKey("Tag1");
        PackageExtraTagsKeys tag2 = PackageManagerTest.createExtraTagKey("Tag2");
        PackageExtraTagsKeys tag3 = PackageManagerTest.createExtraTagKey("Tag3");

        Package pkg1 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
        pkg1.getExtraTags().put(tag1, "value1");
        pkg1.getExtraTags().put(tag2, "value2");
        PackageFactory.save(pkg1);

        Package pkg2 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
        pkg2.getExtraTags().put(tag2, "value2");
        pkg2.getExtraTags().put(tag3, "value3");
        PackageFactory.save(pkg2);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        pkg1 = PackageFactory.lookupByIdAndOrg(pkg1.getId(), user.getOrg());
        pkg2 = PackageFactory.lookupByIdAndOrg(pkg2.getId(), user.getOrg());

        assertEquals(2, pkg1.getExtraTags().size());
        assertEquals("value1", pkg1.getExtraTags().get(tag1));
        assertEquals("value2", pkg1.getExtraTags().get(tag2));

        assertEquals(2, pkg2.getExtraTags().size());
        assertEquals("value2", pkg2.getExtraTags().get(tag2));
        assertEquals("value3", pkg2.getExtraTags().get(tag3));

        Map<Long, Map<String, String>> tagsByPkg =
                TaskManager.getChannelPackageExtraTags(Arrays.asList(pkg1.getId(), pkg2.getId()));

        assertEquals(tagsByPkg.get(pkg1.getId()).get("Tag1"), "value1");
        assertEquals(tagsByPkg.get(pkg1.getId()).get("Tag2"), "value2");

        assertEquals(tagsByPkg.get(pkg2.getId()).get("Tag2"), "value2");
        assertEquals(tagsByPkg.get(pkg2.getId()).get("Tag3"), "value3");
    }

}
