package com.redhat.rhn.manager.content.test;

import com.redhat.rhn.manager.content.ListedProduct;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;

import junit.framework.TestCase;

/**
 * Tests ListedProduct.
 */
public class ListedProductTest extends TestCase {

    /** The listed product under test. */
    private ListedProduct listedProduct;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        listedProduct =
                new ListedProduct("friendlyName", 0, "version", new MgrSyncChannel());
    }

    /**
     * Tests getStatus().
     *
     * @throws Exception if anything goes bad
     */
    public void testGetStatus() throws Exception {
        assertEquals(MgrSyncStatus.INSTALLED, listedProduct.getStatus());

        MgrSyncChannel installedChannel = new MgrSyncChannel();
        installedChannel.setLabel("installed");
        installedChannel.setStatus(MgrSyncStatus.INSTALLED);
        installedChannel.setOptional(false);

        for (int i = 0; i < 3; i++) {
            listedProduct.addChannel(installedChannel);
            assertEquals(MgrSyncStatus.INSTALLED, listedProduct.getStatus());
        }

        MgrSyncChannel availableChannel = new MgrSyncChannel();
        availableChannel.setLabel("available");
        availableChannel.setStatus(MgrSyncStatus.AVAILABLE);
        availableChannel.setOptional(false);
        listedProduct.addChannel(availableChannel);
        assertEquals(MgrSyncStatus.AVAILABLE, listedProduct.getStatus());

        MgrSyncChannel unavailableChannel = new MgrSyncChannel();
        unavailableChannel.setLabel("unavailable");
        unavailableChannel.setStatus(MgrSyncStatus.UNAVAILABLE);
        unavailableChannel.setOptional(false);
        listedProduct.addChannel(unavailableChannel);
        assertEquals(MgrSyncStatus.UNAVAILABLE, listedProduct.getStatus());
    }
}
