/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.repomd.test;


import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.frontend.action.channel.manage.PublishErrataHelper;
import com.redhat.rhn.taskomatic.task.repomd.UpdateInfoWriter;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.io.StringWriter;

import static com.redhat.rhn.domain.errata.test.ErrataFactoryTest.createTestPublishedErrata;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProduct;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProductChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestVendorBaseChannel;

/**
 * Tests for the {@link com.redhat.rhn.taskomatic.task.repomd.UpdateInfoWriter} generator.
 */
public class UpdateInfoWriterTest extends BaseTestCaseWithUser {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testSUSEPatchNames() throws Exception {

        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProduct(channelFamily);
        ChannelProduct channelProduct1 = createTestChannelProduct();
        // Create channels
        Channel baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct1);
        baseChannel.setUpdateTag("SLE-SERVER");
        createTestSUSEProductChannel(baseChannel, product, true);

        Errata errata = createTestPublishedErrata(user.getId());
        errata.setAdvisoryName("SUSE-2016-1234");
        baseChannel.addErrata(errata);

        Channel clonedChannel = ChannelFactoryTest.createTestClonedChannel(baseChannel, user);
        Errata clonedErrata = createTestPublishedErrata(user.getId());
        PublishErrataHelper.setUniqueAdvisoryCloneName(errata, clonedErrata);
        clonedChannel.addErrata(clonedErrata);

        StringWriter buffer = new StringWriter();
        UpdateInfoWriter metadataWriter = new UpdateInfoWriter(buffer);
        metadataWriter.getUpdateInfo(baseChannel);
        assertContains(buffer.toString(), "<id>SUSE-SLE-SERVER-2016-1234</id>");

        buffer = new StringWriter();
        metadataWriter = new UpdateInfoWriter(buffer);
        metadataWriter.getUpdateInfo(clonedChannel);
        assertContains(buffer.toString(), "<id>CL-SUSE-SLE-SERVER-2016-1234</id>");
    }
}

