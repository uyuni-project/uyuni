package com.redhat.rhn.manager.content.test;
import org.junit.jupiter.api.BeforeEach;

import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.manager.content.MgrSyncProductDto;

import com.suse.mgrsync.MgrSyncStatus;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

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
                "This is the base Channel", true, false, Optional.ofNullable(PackageFactory.lookupPackageArchByLabel("x86_64")),
                "", "SLES", "SLES", "15", MgrSyncStatus.INSTALLED, true, "http://path/to/basechannel", "");
        childChannel = new MgrSyncChannelDto("ChildChannel", "childchannel", "This is the child Channel",
                "This is the child Channel", true, false, Optional.ofNullable(PackageFactory.lookupPackageArchByLabel("x86_64")),
                "", "SLES", "SLES", "15", MgrSyncStatus.AVAILABLE, true, "http://path/to/childchannel", "");
        product = new MgrSyncProductDto("friendlyName", 0L, "version", false, null, new HashSet<>(), new HashSet<>());
    }


    /**
     * Tests getStatus().
     *
     * @throws Exception if anything goes bad
     */
    @Test
    public void testGetStatus() throws Exception {
        assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());

        product = new MgrSyncProductDto("friendlyName", 0L, "version", false, baseChannel, new HashSet<>(), new HashSet<>());
        product.addChannel(baseChannel);
        assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());

        product.addChannel(childChannel);
        assertEquals(MgrSyncStatus.AVAILABLE, product.getStatus());

        childChannel = new MgrSyncChannelDto("ChildChannel", "childchannel", "This is the child Channel",
                "This is the child Channel", false, false, Optional.ofNullable(PackageFactory.lookupPackageArchByLabel("x86_64")),
                "", "SLES", "SLES", "15", MgrSyncStatus.AVAILABLE, true, "http://path/to/childchannel", "");
        Set<MgrSyncChannelDto> childs = new HashSet<>();
        childs.add(baseChannel);
        childs.add(childChannel);

        product = new MgrSyncProductDto("friendlyName", 0L, "version", false, baseChannel, childs, new HashSet<>());
        assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());
    }
}
