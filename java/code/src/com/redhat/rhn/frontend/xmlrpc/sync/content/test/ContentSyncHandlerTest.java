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
package com.redhat.rhn.frontend.xmlrpc.sync.content.test;

import com.redhat.rhn.domain.session.InvalidSessionIdException;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test case for the ContentSyncHandler XML-RPC API.
 */
public class ContentSyncHandlerTest extends BaseHandlerTestCase {
    private ContentSyncHandler csh;
    private String invalidAuthKey;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.csh = new ContentSyncHandler();
        this.invalidAuthKey = this.adminKey + "deadchicken";
    }

    /**
     * Test adding a new product.
     */
    public void testAddProduct() {
        BaseHandlerTestCase.assertEquals(this.csh.addProduct(this.adminKey, "foo").intValue(),
                                         BaseHandler.VALID);
    }

    /**
     * Test adding new channel.
     */
    public void testAddChannel() {
        BaseHandlerTestCase.assertEquals(this.csh.addChannel(this.adminKey, "foo").intValue(),
                                         BaseHandler.VALID);
    }

    /**
     * Test listing products.
     */
    public void testListProducts() {
        Object[] products = this.csh.listProducts(this.adminKey);
        BaseHandlerTestCase.assertNotNull(products);
        BaseHandlerTestCase.assertNotEmpty(Arrays.asList(products));
    }

    /**
     * Test listing channels.
     */
    public void testListChannels() {
        List<Map<String, Object>> channels = this.csh.listChannels(this.adminKey);
        BaseHandlerTestCase.assertNotNull(channels);
        BaseHandlerTestCase.assertNotEmpty(channels);
    }

    /**
     * Test to capture wrong authentication token on channel adding.
     */
    public void testAddChannelAuth() {
        try {
            this.csh.addChannel(this.invalidAuthKey, "bogus");
            ContentSyncHandlerTest.fail("Expected an exception of type " +
                                        this.getClass().getCanonicalName());
        } catch (InvalidSessionIdException ex) {
            // Expected
        }
    }

    /**
     * Test to capture wrong authentication token on product adding.
     */
    public void testAddProductAuth() {
        try {
            this.csh.addProduct(this.invalidAuthKey, "bogus");
            ContentSyncHandlerTest.fail("Expected an exception of type " +
                                        this.getClass().getCanonicalName());
        } catch (InvalidSessionIdException ex) {
            // Expected
        }
    }

    /**
     * Test to capture wrong authentication token on channel listing.
     */
    public void testListChannelsAuth() {
        try {
            this.csh.listChannels(this.invalidAuthKey);
            ContentSyncHandlerTest.fail("Expected an exception of type " +
                                        this.getClass().getCanonicalName());
        } catch (InvalidSessionIdException ex) {
            // Expected
        }
    }

    /**
     * Test to capture wrong authentication token on product listing.
     */
    public void testListProductsAuth() {
        try {
            this.csh.listProducts(this.invalidAuthKey);
            ContentSyncHandlerTest.fail("Expected an exception of type " +
                                        this.getClass().getCanonicalName());
        } catch (InvalidSessionIdException ex) {
            // Expected
        }
    }
}
