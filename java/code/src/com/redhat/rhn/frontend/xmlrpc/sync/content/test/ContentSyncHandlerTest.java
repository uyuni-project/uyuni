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
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.content.ContentSyncException;

import java.util.Arrays;

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
    public void testListChannels() throws ContentSyncException {
        Object[] channels = this.csh.listChannels(this.adminKey);

        BaseHandlerTestCase.assertNotNull(channels);
        BaseHandlerTestCase.assertNotEmpty(Arrays.asList(channels));
    }

    /**
     * Test to capture wrong authentication token on channel listing.
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void testListChannelsAuth() throws ContentSyncException {
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
