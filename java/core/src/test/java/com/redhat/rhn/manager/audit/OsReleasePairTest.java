/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.manager.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.oval.OsFamily;

import org.junit.jupiter.api.Test;

import java.util.Optional;

public class OsReleasePairTest {

    @Test
    public void testToOVALOsProductRhel() {
        // Test RHEL 9 with minor version
        OsReleasePair rhel94 = new OsReleasePair("Red Hat Enterprise Linux", "9.4");
        Optional<CVEAuditManagerOVAL.OVALOsProduct> productOpt = rhel94.toOVALOsProduct();
        assertTrue(productOpt.isPresent());
        assertEquals(OsFamily.REDHAT_ENTERPRISE_LINUX, productOpt.get().getOsFamily());
        assertEquals("9", productOpt.get().getOsVersion());

        // Test RHEL 9 without minor version
        OsReleasePair rhel9 = new OsReleasePair("Red Hat Enterprise Linux", "9");
        productOpt = rhel9.toOVALOsProduct();
        assertTrue(productOpt.isPresent());
        assertEquals(OsFamily.REDHAT_ENTERPRISE_LINUX, productOpt.get().getOsFamily());
        assertEquals("9", productOpt.get().getOsVersion());

        // Test RHEL 8
        OsReleasePair rhel85 = new OsReleasePair("Red Hat Enterprise Linux", "8.5");
        productOpt = rhel85.toOVALOsProduct();
        assertTrue(productOpt.isPresent());
        assertEquals(OsFamily.REDHAT_ENTERPRISE_LINUX, productOpt.get().getOsFamily());
        assertEquals("8", productOpt.get().getOsVersion());

        // Test unsupported RHEL version
        OsReleasePair rhel6 = new OsReleasePair("Red Hat Enterprise Linux", "6.10");
        assertFalse(rhel6.toOVALOsProduct().isPresent());
    }

    @Test
    public void testToOVALOsProductDerivatives() {
        // Test AlmaLinux
        OsReleasePair alma93 = new OsReleasePair("AlmaLinux", "9.3");
        Optional<CVEAuditManagerOVAL.OVALOsProduct> productOpt = alma93.toOVALOsProduct();
        assertTrue(productOpt.isPresent());
        assertEquals(OsFamily.ALMA_LINUX, productOpt.get().getOsFamily());
        assertEquals("9", productOpt.get().getOsVersion());

        // Test Oracle Linux
        OsReleasePair oracle89 = new OsReleasePair("OEL", "8.9");
        productOpt = oracle89.toOVALOsProduct();
        assertTrue(productOpt.isPresent());
        assertEquals(OsFamily.ORACLE_LINUX, productOpt.get().getOsFamily());
        assertEquals("8", productOpt.get().getOsVersion());

        // Test SUSE Liberty Linux
        OsReleasePair sll92 = new OsReleasePair("SLL", "9.2");
        productOpt = sll92.toOVALOsProduct();
        assertTrue(productOpt.isPresent());
        assertEquals(OsFamily.SUSE_LIBERTY_LINUX, productOpt.get().getOsFamily());
        assertEquals("9", productOpt.get().getOsVersion());
    }

    @Test
    public void testToOVALOsProductUnsupported() {
        OsReleasePair invalidOs = new OsReleasePair("Unknown OS", "1.0");
        assertFalse(invalidOs.toOVALOsProduct().isPresent());
    }
}
