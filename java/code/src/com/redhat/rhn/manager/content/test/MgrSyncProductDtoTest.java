package com.redhat.rhn.manager.content.test;

import com.redhat.rhn.manager.content.MgrSyncProductDto;

import com.suse.mgrsync.XMLChannel;
import com.suse.mgrsync.MgrSyncStatus;

import junit.framework.TestCase;

/**
 * Tests {@link MgrSyncProductDto}.
 */
public class MgrSyncProductDtoTest extends TestCase {

    /** The product under test. */
    private MgrSyncProductDto product;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        product =
                new MgrSyncProductDto("friendlyName", 0L, "version", new XMLChannel());
    }

    /**
     * Tests getStatus().
     *
     * @throws Exception if anything goes bad
     */
    public void testGetStatus() throws Exception {
        assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());

        XMLChannel installedChannel = new XMLChannel();
        installedChannel.setLabel("installed");
        installedChannel.setStatus(MgrSyncStatus.INSTALLED);
        installedChannel.setOptional(false);

        for (int i = 0; i < 3; i++) {
            product.addChannel(installedChannel);
            assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());
        }

        XMLChannel availableChannel = new XMLChannel();
        availableChannel.setLabel("available");
        availableChannel.setStatus(MgrSyncStatus.AVAILABLE);
        availableChannel.setOptional(false);
        product.addChannel(availableChannel);
        assertEquals(MgrSyncStatus.AVAILABLE, product.getStatus());

        XMLChannel unavailableChannel = new XMLChannel();
        unavailableChannel.setLabel("unavailable");
        unavailableChannel.setStatus(MgrSyncStatus.UNAVAILABLE);
        unavailableChannel.setOptional(false);
        product.addChannel(unavailableChannel);
        assertEquals(MgrSyncStatus.UNAVAILABLE, product.getStatus());
    }
}
