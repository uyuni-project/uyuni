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

    /**
     * Direct truth table of the OS-class predicates the factory routes on.
     */
    @Test
    public void testPredicates() {
        assertTrue(type("sles15generic").isSUSE());
        assertTrue(type("suse").isSUSE());
        assertTrue(type("opensuse16.1").isSUSE());
        assertFalse(type("rhel_7").isSUSE());
        assertFalse(type("fedora18").isSUSE());
        assertFalse(type("generic_rpm").isSUSE());

        assertTrue(type("rhel_6").isRhel());
        assertTrue(type("rhel_7").isRhel());
        assertTrue(type("rhel_10").isRhel());
        assertFalse(type("sles15generic").isRhel());
        assertFalse(type("opensuse16.1").isRhel());
        assertFalse(type("fedora18").isRhel());
        assertFalse(type("generic_rpm").isRhel());

        assertTrue(type("fedora18").isFedora());
        assertFalse(type("rhel_7").isFedora());
        assertFalse(type("opensuse16.1").isFedora());

        assertTrue(type("generic_rpm").isGeneric());
        assertFalse(type("rhel_7").isGeneric());
        assertFalse(type("sles15generic").isGeneric());
    }

    /**
     * The Cobbler identity of the install types: SLE16 stays on the suse
     * breed (locked decision, 00-goals.md §5.1) with its label as os_version.
     */
    @Test
    public void testCobblerBreedAndOsVersion() {
        assertEquals("suse", type("opensuse16.1").getCobblerBreed());
        assertEquals("opensuse16.1", type("opensuse16.1").getCobblerOsVersion());

        assertEquals("suse", type("sles15generic").getCobblerBreed());
        assertEquals("sles15generic", type("sles15generic").getCobblerOsVersion());

        assertEquals("redhat", type("rhel_7").getCobblerBreed());
        assertEquals("rhel7", type("rhel_7").getCobblerOsVersion());

        assertEquals("generic", type("generic_rpm").getCobblerBreed());
        assertEquals("virtio26", type("generic_rpm").getCobblerOsVersion());

        assertEquals("redhat", type("fedora18").getCobblerBreed());
        assertEquals("fedora18", type("fedora18").getCobblerOsVersion());
    }

    private KickstartInstallType type(String label) {
        KickstartInstallType installType = new KickstartInstallType();
        installType.setLabel(label);
        return installType;
    }

}
