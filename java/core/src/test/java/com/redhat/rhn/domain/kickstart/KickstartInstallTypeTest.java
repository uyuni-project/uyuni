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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

/**
 * KickstartInstallTypeTest
 */
public class KickstartInstallTypeTest extends BaseTestCase {

    @Test
    public void testKsInstallType() {
        Long testid = 1L;

        KickstartInstallType kit1 = TestUtils.lookupFromCacheById(testid, KickstartInstallType.class);
        assertNotNull(kit1);
        assertEquals(kit1.getId(), testid);

        KickstartInstallType kit2 = TestUtils.lookupFromCacheById(testid, KickstartInstallType.class);
        assertEquals(kit1.getLabel(), kit2.getLabel());
    }

    @Test
    public void testIsSLES16() {
        KickstartInstallType sles16 = new KickstartInstallType();
        sles16.setLabel("opensuse16.1");
        assertTrue(sles16.isSLES16());
        assertTrue(sles16.isSLES16OrGreater());
        assertFalse(sles16.isSLES15());

        KickstartInstallType sles15 = new KickstartInstallType();
        sles15.setLabel("sles15generic");
        assertFalse(sles15.isSLES16());
        assertFalse(sles15.isSLES16OrGreater());

        KickstartInstallType suse = new KickstartInstallType();
        suse.setLabel("suse");
        assertFalse(suse.isSLES16());
        assertFalse(suse.isSLES16OrGreater());
    }

}
