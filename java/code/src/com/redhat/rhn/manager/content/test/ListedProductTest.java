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
     * Tests getstatus().
     *
     * @throws Exception if anything goes bad
     */
    public void testGetStatus() throws Exception {
        assertEquals(MgrSyncStatus.INSTALLED, listedProduct.getStatus());

        MgrSyncChannel installedChannel = new MgrSyncChannel();
        installedChannel.setStatus(MgrSyncStatus.INSTALLED);

        for (int i = 0; i < 3; i++) {
            listedProduct.addChannel(installedChannel);
            assertEquals(MgrSyncStatus.INSTALLED, listedProduct.getStatus());
        }

        MgrSyncChannel availableChannel = new MgrSyncChannel();
        installedChannel.setStatus(MgrSyncStatus.AVAILABLE);
        listedProduct.addChannel(availableChannel);
        assertEquals(MgrSyncStatus.AVAILABLE, listedProduct.getStatus());

        MgrSyncChannel unavailableChannel = new MgrSyncChannel();
        unavailableChannel.setStatus(MgrSyncStatus.UNAVAILABLE);
        listedProduct.addChannel(availableChannel);
        assertEquals(MgrSyncStatus.AVAILABLE, listedProduct.getStatus());
    }
}
