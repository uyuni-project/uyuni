/**
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
package com.suse.manager.utils.test;

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.suse.manager.utils.PackageUtils;

public class PackageUtilsTest extends BaseTestCaseWithUser {

    public void testIsType() throws Exception {
        Package pkgRpm = PackageTest.createTestPackage(user.getOrg());
        pkgRpm.setPackageArch(PackageFactory.lookupPackageArchByLabel("x86_64"));

        Package pkgDeb = PackageTest.createTestPackage(user.getOrg());
        pkgDeb.setPackageArch(PackageFactory.lookupPackageArchByLabel("amd64-deb"));

        assertTrue(PackageUtils.isTypeRpm(pkgRpm));
        assertFalse(PackageUtils.isTypeDeb(pkgRpm));

        assertFalse(PackageUtils.isTypeRpm(pkgDeb));
        assertTrue(PackageUtils.isTypeDeb(pkgDeb));
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

        evr = PackageUtils.parseDebianEvr("1:2.3~.4a+b-5");
        assertEquals("1", evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("5", evr.getRelease());

        evr = PackageUtils.parseDebianEvr("2.3~.4a+b-5");
        assertNull(evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("5", evr.getRelease());

        evr = PackageUtils.parseDebianEvr("2.3~.4a+b");
        assertNull(evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("X", evr.getRelease());

        evr = PackageUtils.parseDebianEvr("1:2.3~.4a+b");
        assertEquals("1", evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("X", evr.getRelease());

        evr = PackageUtils.parseDebianEvr("1:2.3~.4a+b-5+abc.6~");
        assertEquals("1", evr.getEpoch());
        assertEquals("2.3~.4a+b", evr.getVersion());
        assertEquals("5+abc.6~", evr.getRelease());

        evr = PackageUtils.parseDebianEvr("2-3-4-5");
        assertNull(evr.getEpoch());
        assertEquals("2-3-4", evr.getVersion());
        assertEquals("5", evr.getRelease());
    }
}
