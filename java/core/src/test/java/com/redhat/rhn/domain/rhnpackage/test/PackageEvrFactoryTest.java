/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * PackageEvrTest
 */
public class PackageEvrFactoryTest extends RhnBaseTestCase {

    /**
     * Simple test to make sure we can create
     * PackageEvrs and write them to the db.
     */
    @Test
    public void testCreate() {

       PackageEvr evr = createTestPackageEvr();
       assertNotNull(evr.getId());

       //Make sure it got into the db
       PackageEvr evr2 = PackageEvrFactory.lookupPackageEvrById(evr.getId());
       assertNotNull(evr2);
       assertEquals(evr.getEpoch(), evr2.getEpoch());
    }

    /**
     * Test method to create a test PackageEvr
     * @param epoch the epoch
     * @param version the version
     * @param release the release
     * @param type the package type
     * @return Returns a test PackageEvr
     */
    public static PackageEvr createTestPackageEvr(String epoch,
                                                  String version,
                                                  String release, PackageType type) {
        return PackageEvrFactory.lookupOrCreatePackageEvr(epoch, version, release, type);
    }

    public static PackageEvr createTestPackageEvr() {
        return createTestPackageEvr(PackageType.RPM);
    }

    /**
     * Test method to create a test PackageEvr
     * @param packageType the package type
     * @return Returns a test PackageEvr
     */
    public static PackageEvr createTestPackageEvr(PackageType packageType) {
        String epoch = "1";
        String version = "1.0.0";
        String release = "1";

        return createTestPackageEvr(epoch, version, release, packageType);
    }
}
