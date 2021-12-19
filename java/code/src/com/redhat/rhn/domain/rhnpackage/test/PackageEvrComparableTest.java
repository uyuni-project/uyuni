/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.common.db.WrappedSQLException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.testing.RhnBaseTestCase;

/**
 * Test the compare() method in PackageEvr
 */
public class PackageEvrComparableTest extends RhnBaseTestCase {

    public void testEquality() {
        compare(0, "0:0-0", "0:0-0");
        compare(0, "0-0", "0:0-0");
        compare(0, "0-0", "0-0");
    }

    public void testFailure() {
        failure(new PackageEvr("0", null, "0", PackageType.RPM), IllegalStateException.class);
        failure(new PackageEvr("0", "0", null, PackageType.RPM), NullPointerException.class);
        failure(new PackageEvr("X", "0", null, PackageType.RPM), NumberFormatException.class);
    }

    public void testDifference() {
        compare(-1, "1:1-1", "2:5-7");
        compare(-1, "1:5-7", "2:5-7");
        compare(-1, "1:1-7", "1:5-7");
        compare(1, "1:1-7", "1:1-6");
        compare(1, "1:10-7", "1:9-6");
        compare(-1, "1:9-7", "1:11-6");
        compare(-1, "1:1-7", "1:1-7.PTF");
        compare(1, "1:1-7.PTF", "1:1-7");
        compare(1, "1:2-1", "1:2~beta1-1");
        compare(-1, "1:2~beta2-1", "1:2-1");
        compare(1, "1:2~beta2-1", "1:2~beta1-1");
        compare(-1, "1:2~beta2-1", "1:2~beta3-1");

        compare(1, "1.27+1.3.9-1", "1.27.1+1.3.9-1", PackageType.RPM);
        compare(-1, "1.27+1.3.9-1", "1.27.1+1.3.9-1", PackageType.DEB);

        compare(1, "2-1.27+1.3.9", "2-1.27.1+1.3.9", PackageType.RPM);
        compare(-1, "2-1.27+1.3.9", "2-1.27.1+1.3.9", PackageType.DEB);

        compare(1, "8.0.9-1", "a.8.0.9-1", PackageType.RPM);
        compare(-1, "8.0.9-1", "a.8.0.9-1", PackageType.DEB);
}

    // On Postgres, we don't get as far as the specific errors - the DB kicks us out
    // with WrappedSQLException before we can even get to the specific-failure-code
    private void failure(PackageEvr evr, Class excClass) {
        try {
            PackageEvr evrdb = PackageEvrFactory.lookupOrCreatePackageEvr(evr);
            compare(0, evrdb, evrdb);
            fail("Comparison of " + evr + " must fail");
        }
        catch (WrappedSQLException wse) {
            assertTrue(true);
            HibernateFactory.rollbackTransaction();
        }
        catch (Exception e) {
            assertEquals(excClass, e.getClass());
        }
    }

    /*
     * compare with default type RPM
     */
    private void compare(int exp, String evrString1, String evrString2) {
        compare(exp, evrString1, evrString2, PackageType.RPM);
    }

    private void compare(int exp, String evrString1, String evrString2, PackageType type) {
        PackageEvr evr1 = create(evrString1, type);
        PackageEvr evr2 = create(evrString2, type);
        compare(exp, evr1, evr2);
    }

    private void compare(int exp, PackageEvr evr1, PackageEvr evr2) {
        assertEquals(exp, evr1.compareTo(evr2));
        assertEquals(-exp, evr2.compareTo(evr1));
        assertEquals(0, evr1.compareTo(evr1));
        assertEquals(0, evr2.compareTo(evr2));
    }

    private PackageEvr create(String evr, PackageType type) {
        PackageEvr packageEvr = PackageEvr.parsePackageEvr(type, evr);
        return PackageEvrFactory.lookupOrCreatePackageEvr(packageEvr);
    }
}
