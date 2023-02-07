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
package com.redhat.rhn.domain.channel.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * ChannelArchTest
 */
public class ChannelArchTest extends RhnBaseTestCase {

    @Test
    public void testChannelArch() {
        Long testid = 500L;
        String query = "ChannelArch.findById";
        ChannelArch ca = (ChannelArch) TestUtils.lookupFromCacheById(testid, query);
        ChannelArch ca2 = (ChannelArch) TestUtils.lookupFromCacheById(ca.getId(), query);
        assertNotNull(ca.getArchType());
        assertEquals(ca.getLabel(), ca2.getLabel());
    }

    @Test
    public void testChannelArchByLabel() {
        ChannelArch x86Arch = ChannelFactory.lookupArchByName("IA-32");
        assertNotNull(x86Arch);
    }

    @Test
    public void testCompatibleServerArches() {
        ChannelArch ca = ChannelFactory.lookupArchByName("IA-32");
        Set arches = ca.getCompatibleServerArches();
        assertNotNull(arches);
        for (Object o : arches) {
            assertNotNull(o);
            assertEquals(ServerArch.class, o.getClass());
        }
    }

    @Test
    public void testIsCompatible() {
        ChannelArch ca = ChannelFactory.lookupArchByName("x86_64");
        ServerArch amd64 = ServerFactory.lookupServerArchByLabel("amd64-redhat-linux");
        assertTrue(ca.isCompatible(amd64));

        ServerArch s390 = ServerFactory.lookupServerArchByLabel("s390-redhat-linux");
        assertFalse(ca.isCompatible(s390));
    }

    @Test
    public void testIsCompatibleForPackages() {
        ChannelArch ca = ChannelFactory.lookupArchByName("IA-32");
        PackageArch i386 = PackageFactory.lookupPackageArchByLabel("i386");
        assertTrue(ca.isCompatible(i386));

        PackageArch s390 = PackageFactory.lookupPackageArchByLabel("s390");
        assertFalse(ca.isCompatible(s390));
    }
}
