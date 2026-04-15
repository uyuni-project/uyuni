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
package com.redhat.rhn.domain.rhnpackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * PackageArchTest
 */
public class PackageArchTest extends RhnBaseTestCase {
    /**
     * Simple test to make sure we can lookup PackageArchs from
     * the db. Turn on hibernate.show_sql to make sure hibernate
     * is only going to the db once.
     */
    @Test
    public void testPackageArch() {

        Long testid = 100L;
        PackageArch p1 = PackageFactory.lookupPackageArchById(testid);
        PackageArch p2 = PackageFactory.lookupPackageArchById(p1.getId());

        assertNotNull(p1.getArchType());
        assertEquals(p1.getLabel(), p2.getLabel());
    }

    @Test
    public void testToUniversalArchString() {
        PackageArch archx86 = PackageFactory.lookupPackageArchByLabel("x86_64");
        PackageArch archAmd64 = PackageFactory.lookupPackageArchByLabel("amd64-deb");

        assertEquals("x86_64", archx86.toUniversalArchString());
        assertEquals("amd64", archAmd64.toUniversalArchString());
    }
}
