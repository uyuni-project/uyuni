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
package com.suse.spec.channel.software;

import com.redhat.rhn.domain.user.User;

import com.suse.spec.channel.software.dto.SyncRequest;
import com.suse.spec.channel.software.dto.SyncResponse;

/**
 * Facade for syncing erratas/packages from vendor channels.
 * Used when cloning vendor erratas into user channels.
 */
public interface SyncFromVendorService {

    /**
     * Syncs erratas and/or packages from vendor channels to target channel
     *
     * @param user User performing the operation
     * @param targetChannelLabel Label of target channel
     * @param request Sync request with operation type and filters
     * @return Response containing cloned erratas and packages
     */
    SyncResponse sync(User user, String targetChannelLabel, SyncRequest request);
}
