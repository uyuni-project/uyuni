/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.impl.channel.software;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

import com.suse.spec.channel.software.dto.ErrataCriteria;
import com.suse.spec.channel.software.dto.SyncOperation;
import com.suse.spec.channel.software.dto.SyncRequest;

import java.util.Arrays;

/**
 * Test utilities for Channel Software sync tests.
 */
public class ChannelSoftwareTestUtils {

    /**
     * Creates a SyncRequest for testing.
     *
     * @param operation the sync operation
     * @param advisoryNames the advisory names filter (optional)
     * @return the sync request
     */
    public static SyncRequest createSyncRequest(SyncOperation operation, String... advisoryNames) {
        ErrataCriteria criteria = new ErrataCriteria(
                advisoryNames != null && advisoryNames.length > 0 ? Arrays.asList(advisoryNames) : null,
                null,
                null
        );
        return new SyncRequest(criteria, operation, false, false, false);
    }

    /**
     * Creates an SyncRequest for testing asynchronously
     *
     * @param operation the sync operation
     * @param advisoryNames the advisory names filter (optional)
     * @return the sync request
     */
    public static SyncRequest createSyncRequestAsync(SyncOperation operation, String... advisoryNames) {
        ErrataCriteria criteria = new ErrataCriteria(
                advisoryNames != null && advisoryNames.length > 0 ? Arrays.asList(advisoryNames) : null,
                null,
                null
        );
        return new SyncRequest(criteria, operation, true, false, false);
    }

    private ChannelSoftwareTestUtils() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
