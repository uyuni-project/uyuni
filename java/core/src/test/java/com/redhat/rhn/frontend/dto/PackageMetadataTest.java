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
package com.redhat.rhn.frontend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.testing.BaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * PackageMetadataTest
 */
public class PackageMetadataTest extends BaseTestCase {

    @Test
    public void testParameterizedCtor() {
        PackageMetadata pm = new PackageMetadataNoDiff(null, null, null);

        assertNull(pm.getSystem());
        assertNull(pm.getOther());
        assertEquals(PackageMetadata.KEY_NO_DIFF, pm.getComparisonAsInt());
        assertEquals(PackageMetadata.ACTION_NONE, pm.getActionStatusAsInt());

        assertEquals("", pm.getActionStatus());
        assertEquals("", pm.getComparison());
        assertNotNull(pm.toString());
        assertEquals("", pm.getName());
        assertNull(pm.getNameId());
        assertNull(pm.getEvrId());

        pm.updateActionStatus();
        assertEquals(PackageMetadata.ACTION_NONE, pm.getActionStatusAsInt());
    }

    @Test
    public void testDefaultCtor() {
        PackageMetadata pm = new PackageMetadataNoDiff(new PackageListItem(), new PackageListItem(), null);

        assertNotNull(pm.getSystem());
        assertNotNull(pm.getOther());
        assertEquals(PackageMetadata.KEY_NO_DIFF, pm.getComparisonAsInt());
        assertEquals(PackageMetadata.ACTION_NONE, pm.getActionStatusAsInt());

        assertEquals("", pm.getActionStatus());
        assertEquals("", pm.getComparison());
        assertNotNull(pm.toString());
        assertNull(pm.getName());
        assertNull(pm.getNameId());
        assertNull(pm.getEvrId());

        pm.updateActionStatus();
        assertEquals(PackageMetadata.ACTION_NONE, pm.getActionStatusAsInt());
    }

    @Test
    public void testGetActionStatusAsInt() {
        PackageMetadata pm = new PackageMetadataOtherNewer(new PackageListItem(), new PackageListItem(), null);
        assertEquals(PackageMetadata.ACTION_UPGRADE, pm.getActionStatusAsInt());

        pm = new PackageMetadataThisOnly(new PackageListItem(), new PackageListItem(), null);
        assertEquals(PackageMetadata.ACTION_REMOVE, pm.getActionStatusAsInt());

        pm = new PackageMetadataThisNewer(new PackageListItem(), new PackageListItem(), null);
        assertEquals(PackageMetadata.ACTION_DOWNGRADE, pm.getActionStatusAsInt());

        pm = new PackageMetadataOtherOnly(new PackageListItem(), new PackageListItem(), null);
        assertEquals(PackageMetadata.ACTION_INSTALL, pm.getActionStatusAsInt());
    }

    @Test
    public void testGetComparison() {
        PackageMetadata pm = new PackageMetadataOtherNewer(new PackageListItem(), new PackageListItem(), null);
        assertEquals("Profile newer", pm.getComparison());
        pm = new PackageMetadataOtherNewer(new PackageListItem(), new PackageListItem(), "foo");
        assertEquals("foo newer", pm.getComparison());

        pm = new PackageMetadataThisOnly(new PackageListItem(), new PackageListItem(), null);
        assertEquals("This system only", pm.getComparison());
        pm = new PackageMetadataThisOnly(new PackageListItem(), new PackageListItem(), "foo");
        assertEquals("This system only", pm.getComparison());

        pm = new PackageMetadataThisNewer(new PackageListItem(), new PackageListItem(), null);
        assertEquals("This system newer", pm.getComparison());
        pm = new PackageMetadataThisNewer(new PackageListItem(), new PackageListItem(), "foo");
        assertEquals("This system newer", pm.getComparison());

        pm = new PackageMetadataOtherOnly(new PackageListItem(), new PackageListItem(), null);
        assertEquals("Profile only", pm.getComparison());
        pm = new PackageMetadataOtherOnly(new PackageListItem(), new PackageListItem(), "foo");
        assertEquals("foo only", pm.getComparison());
    }
}
