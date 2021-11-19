/*
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


import static com.redhat.rhn.domain.errata.test.ErrataFactoryTest.createTestErrata;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProduct;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProductChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestVendorBaseChannel;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.frontend.action.channel.manage.ErrataHelper;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.taskomatic.task.repomd.UpdateInfoWriter;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

        Errata errata = createTestErrata(user.getId());
        errata.setAdvisoryName("SUSE-2016-1234");
        baseChannel.addErrata(errata);

        Channel clonedChannel = ChannelFactoryTest.createTestClonedChannel(baseChannel, user);
        Errata clonedErrata = createTestErrata(user.getId());
        ErrataHelper.setUniqueAdvisoryCloneName(errata, clonedErrata);
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

    public void testErrataFieldsGeneratedCorrectly() throws Exception {

        final ChannelFamily channelFamily = createTestChannelFamily();
        final SUSEProduct product = createTestSUSEProduct(channelFamily);

        // Create channels
        final Channel baseChannel = createTestVendorBaseChannel(channelFamily, createTestChannelProduct());
        baseChannel.setUpdateTag("SLE-SERVER");
        createTestSUSEProductChannel(baseChannel, product, true);

        Errata errata = createTestErrata(user.getId());
        errata = TestUtils.reload(errata);

        errata.setAdvisoryName("SUSE-2016-1234");
        baseChannel.addErrata(errata);

        StringWriter buffer = new StringWriter();
        UpdateInfoWriter metadataWriter = new UpdateInfoWriter(buffer);
        metadataWriter.getUpdateInfo(baseChannel);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final String xml = buffer.toString();

        assertContains(xml, "<id>SUSE-SLE-SERVER-2016-1234</id>");
        assertContains(xml, "<title>" + errata.getSynopsis() + "</title>");
        assertContains(xml, "<severity>" + errata.getSeverity().getLocalizedLabel().toLowerCase() + "</severity>");
        assertContains(xml, "<issued date=\"" + df.format(errata.getIssueDate()) + "\"/>");
        assertContains(xml, "<updated date=\"" + df.format(errata.getUpdateDate()) + "\"/>");
        assertContains(xml, "<rights>" + errata.getRights() + "</rights>");
        assertContains(xml, "<description>" + errata.getDescription() + "</description>");

        ErrataOverview o = new ErrataOverview();
        o.setIssueDate("2021-11-22");
    }
}

