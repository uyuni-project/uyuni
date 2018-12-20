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
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.*;

/**
 * Tests for {@link SUSEProductFactory}.
 */
public class SUSEProductFactoryTest extends BaseTestCaseWithUser {

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
