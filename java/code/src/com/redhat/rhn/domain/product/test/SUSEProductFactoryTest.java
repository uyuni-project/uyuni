/**
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.domain.product.test;

import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProduct;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;

import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.test.ContentSyncManagerNonRegressionTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.mgrsync.XMLChannel;
import com.suse.mgrsync.XMLChannels;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for {@link SUSEProductFactory}.
 */
public class SUSEProductFactoryTest extends BaseTestCaseWithUser {

    /**
     * Test {@link SUSEProductFactory#lookupSUSEProductChannel(String, Long)}.
     * @throws Exception if anything goes wrong
     */
    public void testLookupSUSEProductChannel() throws Exception {
        // Setup a product in the database
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(family);

        // Create and save a product channel
        SUSEProductChannel expected = new SUSEProductChannel();
        String channelLabel = TestUtils.randomString();
        expected.setChannelLabel(channelLabel);
        expected.setProduct(product);
        TestUtils.saveAndFlush(expected);

        // Check if lookup returns the right SUSEProductChannel object
        SUSEProductChannel actual = SUSEProductFactory.lookupSUSEProductChannel(
                channelLabel, product.getProductId());
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link SUSEProductFactory#removeAllExcept}.
     * @throws Exception if anything goes wrong
     */
    public void testRemoveAllExcept() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();

        // ensure at least one product exists
        createTestSUSEProduct(channelFamily);

        final SUSEProduct p = createTestSUSEProduct(channelFamily);

        SUSEProductFactory.removeAllExcept(new LinkedList<SUSEProduct>() { {
            add(p);
        } });

        List<SUSEProduct> remaining = SUSEProductFactory.findAllSUSEProducts();

        assertEquals(1, remaining.size());
        assertEquals(p, remaining.get(0));
    }

}
