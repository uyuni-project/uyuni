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

import java.lang.reflect.Method;

import com.redhat.rhn.manager.content.ListedProduct;
import com.redhat.rhn.manager.setup.SCCProductSyncManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.Product;
import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;

/**
 * Tests for {@link SCCProductSyncManager}.
 */
public class SCCProductSyncManagerTest extends RhnBaseTestCase {

    /**
     * Test for the convertProduct() method of {@link SCCProductSyncManager}:
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
        Method method = SCCProductSyncManager.class.getDeclaredMethod(
                "convertProduct", ListedProduct.class);
        method.setAccessible(true);
        Product productOut = (Product) method.invoke(
                new SCCProductSyncManager(), baseProduct);

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
}
