/*
 * Copyright (c) 2014--2021 SUSE LLC
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
package com.redhat.rhn.manager.content.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.manager.content.MgrSyncProductDto;

import com.suse.mgrsync.MgrSyncStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Tests {@link MgrSyncProductDto}.
 */
public class MgrSyncProductDtoTest  {

    /** The product under test. */
    private MgrSyncProductDto product;
    private MgrSyncChannelDto baseChannel;
    private MgrSyncChannelDto childChannel;

    /**
     * {@inheritDoc}
     */
    @BeforeEach
    public void setUp() throws Exception {


        baseChannel = new MgrSyncChannelDto("BaseChannel", "basechannel", "This is the base Channel",
                "This is the base Channel", true, false,
                Optional.ofNullable(PackageFactory.lookupPackageArchByLabel("x86_64")),
                "", "SLES", "SLES", "15", MgrSyncStatus.INSTALLED, true, "http://path/to/basechannel", "");
        childChannel = new MgrSyncChannelDto("ChildChannel", "childchannel", "This is the child Channel",
                "This is the child Channel", true, false,
                Optional.ofNullable(PackageFactory.lookupPackageArchByLabel("x86_64")),
                "", "SLES", "SLES", "15", MgrSyncStatus.AVAILABLE, true, "http://path/to/childchannel", "");
        product = new MgrSyncProductDto("friendlyName", 0L, 0L, "version", false, null, new HashSet<>(),
            new HashSet<>());
    }


    /**
     * Tests getStatus().
     *
     * @throws Exception if anything goes bad
     */
    @Test
    public void testGetStatus() throws Exception {
        assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());

        product = new MgrSyncProductDto("friendlyName", 0L, 0L, "version", false,
                baseChannel, new HashSet<>(), new HashSet<>());
        product.addChannel(baseChannel);
        assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());

        product.addChannel(childChannel);
        assertEquals(MgrSyncStatus.AVAILABLE, product.getStatus());

        childChannel = new MgrSyncChannelDto("ChildChannel", "childchannel", "This is the child Channel",
                "This is the child Channel", false, false,
                Optional.ofNullable(PackageFactory.lookupPackageArchByLabel("x86_64")),
                "", "SLES", "SLES", "15", MgrSyncStatus.AVAILABLE, true, "http://path/to/childchannel", "");
        Set<MgrSyncChannelDto> childs = new HashSet<>();
        childs.add(baseChannel);
        childs.add(childChannel);

        product = new MgrSyncProductDto("friendlyName", 0L, 0L, "version", false, baseChannel, childs, new HashSet<>());
        assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());
    }
}
