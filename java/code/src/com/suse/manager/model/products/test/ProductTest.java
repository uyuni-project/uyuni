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

import java.util.LinkedList;

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
                null,
                new MandatoryChannels(new LinkedList<Channel>() { {
                    add(new Channel("test channel 1", Channel.STATUS_NOT_SYNCHRONIZING));
                    add(new Channel("test channel 2", Channel.STATUS_NOT_SYNCHRONIZING));
                    add(new Channel("test channel 3", Channel.STATUS_NOT_SYNCHRONIZING));
                } }),
                new OptionalChannels(new LinkedList<Channel>() { {
                    add(new Channel("test channel 1", Channel.STATUS_NOT_SYNCHRONIZING));
                    add(new Channel("test channel 2", Channel.STATUS_NOT_SYNCHRONIZING));
                    add(new Channel("test channel 3", Channel.STATUS_NOT_SYNCHRONIZING));
                } })
       );

       assertEquals(false, nonSynchronizingProduct.isSynchronizing());

       Product synchronizingProduct = new Product(
               "x86_46",
               "test",
               "test product",
               null,
               new MandatoryChannels(new LinkedList<Channel>() { {
                   add(new Channel("test channel 1", Channel.STATUS_NOT_SYNCHRONIZING));
                   add(new Channel("test channel 2", Channel.STATUS_NOT_SYNCHRONIZING));
                   add(new Channel("test channel 3", Channel.STATUS_NOT_SYNCHRONIZING));
               } }),
               new OptionalChannels(new LinkedList<Channel>() { {
                   add(new Channel("test channel 1", Channel.STATUS_NOT_SYNCHRONIZING));
                   add(new Channel("test channel 2", Channel.STATUS_NOT_SYNCHRONIZING));
                   add(new Channel("test channel 3", Channel.STATUS_SYNCHRONIZING));
               } })
      );

      assertEquals(true, synchronizingProduct.isSynchronizing());
    }
}
