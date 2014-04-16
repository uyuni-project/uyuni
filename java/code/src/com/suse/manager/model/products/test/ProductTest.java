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
package com.suse.manager.model.products.test;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;
import com.suse.manager.model.products.Product;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests Product.
 */
public class ProductTest extends TestCase {

    /**
     * Tests the method isSynchronizing().
     */
    public void testIsSynchronizing() {
        Product nonSynchronizingProduct = new Product(
                "x86_46",
                "test",
                "test product",
                "",
                new MandatoryChannels(new LinkedList<Channel>() { {
                    add(new Channel("test channel 1", Channel.STATUS_NOT_INSTALLED));
                    add(new Channel("test channel 2", Channel.STATUS_NOT_INSTALLED));
                    add(new Channel("test channel 3", Channel.STATUS_NOT_INSTALLED));
                } }),
                new OptionalChannels(new LinkedList<Channel>() { {
                    add(new Channel("test channel 1", Channel.STATUS_NOT_INSTALLED));
                    add(new Channel("test channel 2", Channel.STATUS_NOT_INSTALLED));
                    add(new Channel("test channel 3", Channel.STATUS_NOT_INSTALLED));
                } })
       );

       assertEquals(false, nonSynchronizingProduct.channelsInstalled());

       Product synchronizingProduct = new Product(
               "x86_46",
               "test",
               "test product",
               "",
               new MandatoryChannels(new LinkedList<Channel>() { {
                   add(new Channel("test channel 1", Channel.STATUS_NOT_INSTALLED));
                   add(new Channel("test channel 2", Channel.STATUS_NOT_INSTALLED));
                   add(new Channel("test channel 3", Channel.STATUS_NOT_INSTALLED));
               } }),
               new OptionalChannels(new LinkedList<Channel>() { {
                   add(new Channel("test channel 1", Channel.STATUS_NOT_INSTALLED));
                   add(new Channel("test channel 2", Channel.STATUS_NOT_INSTALLED));
                   add(new Channel("test channel 3", Channel.STATUS_INSTALLED));
               } })
      );

      assertEquals(true, synchronizingProduct.channelsInstalled());
    }

    public void testSorting() {
        List<Product> products = new LinkedList<Product>();

        Product prodAs390 = new Product(
            "s390",
            "product_a_s390",
            "Product A",
            "",
            new MandatoryChannels(new LinkedList<Channel>()),
            new OptionalChannels(new LinkedList<Channel>())
        );
        Product prodAx86_64 = new Product(
            "x86_46",
            "product_a_x86_64",
            "Product A",
            "",
            new MandatoryChannels(new LinkedList<Channel>()),
            new OptionalChannels(new LinkedList<Channel>())
        );
        Product prodA1s390 = new Product(
            "s390",
            "product_a1",
            "Product A1",
            "product_a1_s390",
            new MandatoryChannels(new LinkedList<Channel>()),
            new OptionalChannels(new LinkedList<Channel>())
        );
        Product prodA2s390 = new Product(
            "s390",
            "product_a2",
            "Product A2",
            "product_a2_s390",
            new MandatoryChannels(new LinkedList<Channel>()),
            new OptionalChannels(new LinkedList<Channel>())
        );

        products.add(prodA2s390);
        products.add(prodA1s390);
        products.add(prodAs390);
        products.add(prodAx86_64);

        Collections.sort(products);

        assertEquals(prodAs390, products.get(0));
        assertEquals(prodAx86_64, products.get(1));
        assertEquals(prodA1s390, products.get(2));
        assertEquals(prodA2s390, products.get(3));
    }
}
