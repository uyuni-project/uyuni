/**
 * Copyright (c) 2013 SUSE
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
package com.redhat.rhn.manager.distupgrade.test;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

/**
 * Tests for {@link DistUpgradeManager} methods.
 */
public class DistUpgradeManagerTest extends RhnBaseTestCase {

    /**
     * Verify that the correct product base channels are returned for a given
     * {@link ChannelArch}. The arch parameter has been added to fix this bug:
     *
     * https://bugzilla.novell.com/show_bug.cgi?id=841054.
     *
     * @throws Exception
     */
    public void testGetProductBaseChannelDto() throws Exception {
        // Create SUSE product and channel product
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProductNoArch(channelFamily);
        ChannelProduct channelProduct = createTestChannelProduct();

        // Create product base channels with different channel archs
        ChannelArch channelArch;
        channelArch = ChannelFactory.findArchByLabel("channel-ia32");
        Channel baseChannelIA32 = createTestBaseChannel(
                channelFamily, channelProduct, channelArch);
        SUSEProductTestUtils.createTestSUSEProductChannel(baseChannelIA32, product);
        channelArch = ChannelFactory.findArchByLabel("channel-x86_64");
        Channel baseChannelX8664 = createTestBaseChannel(
                channelFamily, channelProduct, channelArch);
        SUSEProductTestUtils.createTestSUSEProductChannel(baseChannelX8664, product);

        // Check the product base channels for given architectures
        EssentialChannelDto productBaseChannel;
        productBaseChannel = DistUpgradeManager.getProductBaseChannelDto(product.getId(),
                        ChannelFactory.findArchByLabel("channel-ia32"));
        assertEquals(baseChannelIA32.getId(), productBaseChannel.getId());
        productBaseChannel = DistUpgradeManager.getProductBaseChannelDto(product.getId(),
                ChannelFactory.findArchByLabel("channel-x86_64"));
        assertEquals(baseChannelX8664.getId(), productBaseChannel.getId());
        productBaseChannel = DistUpgradeManager.getProductBaseChannelDto(product.getId(),
                ChannelFactory.findArchByLabel("channel-s390"));
        assertNull(productBaseChannel);
    }

    /**
     * Create a SUSE product with arch == null. This is actually the case with
     * e.g. SLES for VMWARE.
     *
     * @param family the channel family
     * @return the channel product
     * @throws Exception if anything goes wrong
     */
    public static SUSEProduct createTestSUSEProductNoArch(ChannelFamily family)
            throws Exception {
        SUSEProduct product = new SUSEProduct();
        String name = TestUtils.randomString().toLowerCase();
        product.setName(name);
        product.setVersion("1");
        product.setFriendlyName("SUSE Test product " + name);
        product.setArch(null);
        product.setRelease("test");
        product.setChannelFamilyId(family.getId().toString());
        product.setProductList('Y');
        product.setProductId(0);
        TestUtils.saveAndFlush(product);
        return product;
    }

    /**
     * Create a vendor base channel for a given {@link ChannelFamily},
     * {@link ChannelProduct} and {@link ChannelArch}.
     *
     * @param channelFamily channelFamily
     * @param channelProduct channelProduct
     * @return the new vendor base channel
     * @throws Exception if anything goes wrong
     */
    public static Channel createTestBaseChannel(ChannelFamily channelFamily,
            ChannelProduct channelProduct, ChannelArch channelArch) throws Exception {
        Channel channel = ChannelFactoryTest.
                createTestChannel(null, channelFamily);
        channel.setProduct(channelProduct);
        channel.setChannelArch(channelArch);
        TestUtils.saveAndFlush(channel);
        return channel;
    }
}
