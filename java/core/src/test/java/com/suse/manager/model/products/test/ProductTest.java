/**
 * Copyright (c) 2014 SUSE LLC
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

import com.redhat.rhn.frontend.dto.SetupWizardProductDto;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests Product.
 */
public class ProductTest extends TestCase {

    /**
     * Tests the method isProvided().
     */
    public void testIsProvided() {
        SetupWizardProductDto nonSynchronizingProduct = new SetupWizardProductDto(
                "x86_46",
                "test",
                "test product",
                "",
                new MandatoryChannels(new LinkedList<Channel>() { {
                    add(new Channel("test channel 1", Channel.STATUS_PROVIDED, false));
                    add(new Channel("test channel 2", Channel.STATUS_PROVIDED, false));
                    add(new Channel("test channel 3", Channel.STATUS_NOT_PROVIDED, false));
                } }),
                new OptionalChannels(new LinkedList<Channel>() { {
                    add(new Channel("test channel 1", Channel.STATUS_NOT_PROVIDED, false));
                    add(new Channel("test channel 2", Channel.STATUS_NOT_PROVIDED, false));
                    add(new Channel("test channel 3", Channel.STATUS_NOT_PROVIDED, false));
                } })
       );

       assertEquals(false, nonSynchronizingProduct.isProvided());

       SetupWizardProductDto synchronizingProduct = new SetupWizardProductDto(
               "x86_46",
               "test",
               "test product",
               "",
               new MandatoryChannels(new LinkedList<Channel>() { {
                   add(new Channel("test channel 1", Channel.STATUS_PROVIDED, false));
                   add(new Channel("test channel 2", Channel.STATUS_PROVIDED, false));
                   add(new Channel("test channel 3", Channel.STATUS_PROVIDED, false));
               } }),
               new OptionalChannels(new LinkedList<Channel>() { {
                   add(new Channel("test channel 1", Channel.STATUS_NOT_PROVIDED, false));
                   add(new Channel("test channel 2", Channel.STATUS_NOT_PROVIDED, false));
                   add(new Channel("test channel 3", Channel.STATUS_NOT_PROVIDED, false));
               } })
      );

      assertEquals(true, synchronizingProduct.isProvided());
    }

    /**
     * Test compareTo().
     */
    public void testCompareTo() {
        List<SetupWizardProductDto> products = new LinkedList<SetupWizardProductDto>();

        SetupWizardProductDto prodAs390 = new SetupWizardProductDto(
            "s390",
            "product_a_s390",
            "Product A",
            "",
            new MandatoryChannels(new LinkedList<Channel>()),
            new OptionalChannels(new LinkedList<Channel>())
        );
        SetupWizardProductDto prodAx64 = new SetupWizardProductDto(
            "x86_46",
            "product_a_x86_64",
            "Product A",
            "",
            new MandatoryChannels(new LinkedList<Channel>()),
            new OptionalChannels(new LinkedList<Channel>())
        );
        SetupWizardProductDto prodA1s390 = new SetupWizardProductDto(
            "s390",
            "product_a1",
            "Product A1",
            "product_a1_s390",
            new MandatoryChannels(new LinkedList<Channel>()),
            new OptionalChannels(new LinkedList<Channel>())
        );
        SetupWizardProductDto prodA2s390 = new SetupWizardProductDto(
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
        products.add(prodAx64);

        Collections.sort(products);

        assertEquals(prodAs390, products.get(0));
        assertEquals(prodAx64, products.get(1));
        assertEquals(prodA1s390, products.get(2));
        assertEquals(prodA2s390, products.get(3));
    }
}
