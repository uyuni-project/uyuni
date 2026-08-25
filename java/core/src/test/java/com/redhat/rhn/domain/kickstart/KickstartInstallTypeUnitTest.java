/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.kickstart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pure database-free unit tests for KickstartInstallType predicates, breed, and os_version mapping.
 */
public class KickstartInstallTypeUnitTest {

    @Test
    public void testSles16() {
        KickstartInstallType sles16 = new KickstartInstallType();
        sles16.setLabel("sles16generic");

        assertTrue(sles16.isSUSE());
        assertTrue(sles16.isSLES());
        assertTrue(sles16.isSLES16());
        assertTrue(sles16.isSLES16OrGreater());
        assertTrue(sles16.isSLES15OrGreater());
        assertTrue(sles16.isSLES12OrGreater());
        assertTrue(sles16.isSLES11OrGreater());
        assertTrue(sles16.isSLES10OrGreater());
        assertFalse(sles16.isSLES15());
        assertFalse(sles16.isSLES12());
        assertFalse(sles16.isSLES11());
        assertFalse(sles16.isSLES10());

        assertEquals("generic", sles16.getCobblerBreed());
        assertEquals("sles16generic", sles16.getCobblerOsVersion());
    }

    @Test
    public void testSles15() {
        KickstartInstallType sles15 = new KickstartInstallType();
        sles15.setLabel("sles15generic");

        assertTrue(sles15.isSUSE());
        assertTrue(sles15.isSLES());
        assertFalse(sles15.isSLES16());
        assertFalse(sles15.isSLES16OrGreater());
        assertTrue(sles15.isSLES15());
        assertTrue(sles15.isSLES15OrGreater());

        assertEquals("suse", sles15.getCobblerBreed());
        assertEquals("sles15generic", sles15.getCobblerOsVersion());
    }
}
