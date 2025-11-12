/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.controllers.admin.beans;

import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.model.hub.IssRole;

import java.util.Objects;
import java.util.Optional;

/**
 * The details of a peripheral
 */
public class PeripheralDetailsData extends IssServerDetailsData {

    private final long nSyncedChannels;

    private final long nSyncedOrgs;

    /**
     * Create an instance from the peripheral entity.
     * @param peripheral the peripheral
     */
    public PeripheralDetailsData(IssPeripheral peripheral) {
        super(
            peripheral.getId(),
            IssRole.PERIPHERAL,
            peripheral.getFqdn(),
            peripheral.getRootCa(),
            Optional.ofNullable(peripheral.getMirrorCredentials()).map(creds -> creds.getUsername()).orElse(null),
            peripheral.getCreated(),
            peripheral.getModified()
        );

        this.nSyncedChannels = peripheral.getPeripheralChannels().size();
        this.nSyncedOrgs = peripheral.getPeripheralChannels().stream()
                .map(IssPeripheralChannels::getPeripheralOrgId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    public long getNSyncedChannels() {
        return nSyncedChannels;
    }

    public long getNSyncedOrgs() {
        return nSyncedOrgs;
    }
}
