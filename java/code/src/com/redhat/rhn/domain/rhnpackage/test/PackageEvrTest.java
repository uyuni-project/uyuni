/*
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.domain.rhnpackage.test;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

public class PackageEvrTest extends BaseTestCaseWithUser {

    public void testToUniversalEvrString() {
        PackageEvr evr1 = PackageEvrFactoryTest.createTestPackageEvr("1", "2.3.4", "5", PackageType.RPM);
        PackageEvr evr2 = PackageEvrFactoryTest.createTestPackageEvr(null, "1.2", "X", PackageType.RPM);

        assertEquals("1:2.3.4-5", evr1.toUniversalEvrString());
        assertEquals("1.2", evr2.toUniversalEvrString());
    }

    /**
     * Debian package versioning policy format: [epoch:]upstream_version[-debian_revision]
     * Additional ':' and '-' characters are allowed in 'upstream_version'
     * https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
     *
     * Tests:
     *   - 1:2.3~.4a+b-5
     *   - 2.3~.4a+b-5
     *   - 2.3~.4a+b
     *   - 1:2.3~.4a+b
     *   - 1:2.3~.4a+b-5+abc.6~
     *   - 1:2-3-4-5
     *
     */
    public void testParseDebianEvr() {

        PackageEvr evr;

        evr = PackageEvr.parseDebian("1:2.3~.4a+b-5");
        assertEquals("1", evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("5", evr.getRelease());

        evr = PackageEvr.parseDebian("2.3~.4a+b-5");
        assertNull(evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("5", evr.getRelease());

        evr = PackageEvr.parseDebian("2.3~.4a+b");
        assertNull(evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("X", evr.getRelease());

        evr = PackageEvr.parseDebian("1:2.3~.4a+b");
        assertEquals("1", evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("X", evr.getRelease());

        evr = PackageEvr.parseDebian("1:2.3~.4a+b-5+abc.6~");
        assertEquals("1", evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("5+abc.6~", evr.getRelease());

        evr = PackageEvr.parseDebian("2-3-4-5");
        assertNull(evr.getEpoch());
        assertEquals("2-3-4", evr.getVersion());
        assertEquals("5", evr.getRelease());
    }

    public void testParseRpmEvr() {
        PackageEvr evr;

        evr = PackageEvr.parseRpm("1:1.2.3-4.5");
        assertEquals("1", evr.getEpoch());
        assertEquals("1.2.3", evr.getVersion());
        assertEquals("4.5", evr.getRelease());

        evr = PackageEvr.parseRpm("1.2.3-4.5");
        assertNull(evr.getEpoch());
        assertEquals("1.2.3", evr.getVersion());
        assertEquals("4.5", evr.getRelease());

        evr = PackageEvr.parseRpm("1:1.2.3-4.5");
        assertEquals("1", evr.getEpoch());
        assertEquals("1.2.3", evr.getVersion());
        assertEquals("4.5", evr.getRelease());

        evr = PackageEvr.parseRpm("1.2.3");
        assertNull(evr.getEpoch());
        assertEquals("1.2.3", evr.getVersion());
        assertEquals("", evr.getRelease());
    }
}
