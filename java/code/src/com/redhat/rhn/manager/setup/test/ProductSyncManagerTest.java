/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.manager.content.ListedProduct;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;
import com.suse.manager.model.products.Product;
import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * Tests for ProductSyncManager.
 */
public class ProductSyncManagerTest extends BaseTestCaseWithUser {

    /**
     * Test for the convertProduct() method of {@link ProductSyncManager}:
     * Convert a product with an extension into displayable objects.
     *
     * @throws Exception if something goes wrong
     */
    public void testConvertProduct() throws Exception {
        // Setup some test data
        MgrSyncChannel baseChannel = new MgrSyncChannel();
        baseChannel.setArch("x86_64");
        baseChannel.setLabel("baseChannel");
        baseChannel.setStatus(MgrSyncStatus.INSTALLED);
        ListedProduct baseProduct =
                new ListedProduct("baseProduct", 100, "11", baseChannel);
        baseProduct.addChannel(baseChannel);

        // Setup an addon
        MgrSyncChannel childChannel = new MgrSyncChannel();
        childChannel.setArch("x86_64");
        childChannel.setLabel("childChannel");
        childChannel.setOptional(true);
        childChannel.setStatus(MgrSyncStatus.AVAILABLE);
        ListedProduct extension =
                new ListedProduct("extensionProduct", 200, "12", baseChannel);
        extension.addChannel(childChannel);
        baseProduct.addExtension(extension);

        // Call the private method
        Method method = ProductSyncManager.class.getDeclaredMethod(
                "convertProduct", ListedProduct.class);
        method.setAccessible(true);
        Product productOut = (Product) method.invoke(
                new ProductSyncManager(), baseProduct);

        // Verify the base product attributes
        assertEquals("x86_64", productOut.getArch());
        assertEquals("baseProduct", productOut.getName());
        assertEquals("100-baseChannel", productOut.getIdent());
        assertEquals("", productOut.getBaseProductIdent());
        assertNotEmpty(productOut.getMandatoryChannels());
        for (Channel c: productOut.getMandatoryChannels()) {
            assertEquals("P", c.getStatus());
        }
        assertTrue(productOut.getOptionalChannels().isEmpty());
        assertNotEmpty(productOut.getAddonProducts());

        // Verify the addon product
        Product addonOut = productOut.getAddonProducts().get(0);
        assertEquals("x86_64", addonOut.getArch());
        assertEquals("extensionProduct", addonOut.getName());
        assertEquals("200-baseChannel", addonOut.getIdent());
        assertEquals("100-baseChannel", addonOut.getBaseProductIdent());
        assertNotEmpty(addonOut.getOptionalChannels());
        for (Channel c: addonOut.getOptionalChannels()) {
            assertEquals(".", c.getStatus());
        }
    }

    /**
     * Verify product sync status for a given product: NOT_MIRRORED
     * Product is NOT_MIRRORED if any of the channels is not mirrored (= exists in the DB).
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusNotMirrored() throws Exception {
        Product product = createFakeProduct("...");
        Product.SyncStatus status = new ProductSyncManager().getProductSyncStatus(product);
        assertEquals(Product.SyncStatus.SyncStage.NOT_MIRRORED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FAILED
     * All channels are mirrored, but no metadata is there yet and no schedule either.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusFailed() throws Exception {
        Product product = createFakeProduct("PPP");
        Product.SyncStatus status = new ProductSyncManager().getProductSyncStatus(product);
        assertEquals(Product.SyncStatus.SyncStage.FAILED, status.getStage());
    }

    /**
     * Create fake product with channels as described in channelDesc, e.g. "P..P".
     * For every "P" (= provided) a real channel will be created in the database.
     *
     * @param channelDesc description of a set of channels and their status
     * @return {@link Product} fake product
     * @throws Exception if something goes wrong
     */
    private Product createFakeProduct(String channelDesc) throws Exception {
        String ident = "product-" + TestUtils.randomString();
        Product p = new Product("x86_64", ident, "Product " + ident, "",
                new MandatoryChannels(), new OptionalChannels());

        for (int k = 0; k < channelDesc.length(); k++) {
            char descChar = channelDesc.charAt(k);
            Channel channel = new Channel();
            if (!(descChar == '.' || descChar == 'P')) {
                throw new IllegalArgumentException(
                        "Ilegal channel description char " + descChar);
            }
            channel.setStatus(String.valueOf(descChar));
            p.getMandatoryChannels().add(channel);

            // If the channel is "Provided" create a real channel in the database
            if (channel.isProvided()) {
                com.redhat.rhn.domain.channel.Channel dbChannel =
                        ChannelFactoryTest.createTestChannel(user);
                channel.setLabel(dbChannel.getLabel());
                dbChannel.setLastSynced(new Date());
            }
            else {
                channel.setLabel(p.getIdent() + "-channel-" + k);
            }
        }

        return p;
    }
}
