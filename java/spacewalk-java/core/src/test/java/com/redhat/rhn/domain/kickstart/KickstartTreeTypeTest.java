/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

/**
 * KickstartTreeTypeTest
 */
public class KickstartTreeTypeTest extends RhnBaseTestCase {

    @Test
    public void testKsTreeType() {
        Long testid = 1L;

        KickstartTreeType ktt1 = TestUtils.lookupFromCacheById(testid, KickstartTreeType.class);
        assertNotNull(ktt1);
        assertEquals(ktt1.getId(), testid);

        KickstartTreeType ktt2 = TestUtils.lookupFromCacheById(ktt1.getId(), KickstartTreeType.class);
        assertEquals(ktt1.getLabel(), ktt2.getLabel());

        KickstartTreeType ktt3 = TestUtils.lookupFromCacheById(ktt1.getId(), KickstartTreeType.class);
        assertNotNull(ktt3);
    }

}
