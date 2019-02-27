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

package com.redhat.rhn.domain.rhnpackage.test;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

public class PackageEvrTest extends BaseTestCaseWithUser {

    public void testToUniversalEvrString() {
        PackageEvr evr1 = PackageEvrFactoryTest.createTestPackageEvr("1", "2.3.4", "5");
        PackageEvr evr2 = PackageEvrFactoryTest.createTestPackageEvr(null, "1.2", "X");

        assertEquals("1:2.3.4-5", evr1.toUniversalEvrString());
        assertEquals("1.2", evr2.toUniversalEvrString());
    }
}
