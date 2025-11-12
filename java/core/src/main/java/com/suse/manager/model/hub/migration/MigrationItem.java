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

package com.suse.manager.model.hub.migration;

import com.redhat.rhn.domain.iss.IssSlave;

import com.suse.manager.model.hub.IssPeripheral;

import java.util.function.UnaryOperator;

/**
 * Immutable class to represent an item to be processed by the migration process from ISSv1 to ISSv3.
 * @param slave the ISSv1 slave
 * @param migrationData the data needed for the migration
 * @param peripheral the ISSv3 peripheral, if correctly created
 * @param success true if the process is successful
 */
public record MigrationItem(
    IssSlave slave,
    SlaveMigrationData migrationData,
    IssPeripheral peripheral,
    boolean success
) {

    /**
     * Create an item for the migration process.
     * @param slaveIn the ISSv1 slave
     * @param migrationDataIn the data needed for the migration
     */
    public MigrationItem(IssSlave slaveIn, SlaveMigrationData migrationDataIn) {
        this(slaveIn, migrationDataIn, null, true);
    }

    /**
     * Create an item for the migration process.
     * @param migrationDataIn the data needed for the migration
     */
    public MigrationItem(SlaveMigrationData migrationDataIn) {
        this(null, migrationDataIn, null, true);
    }

    /**
     * Gets the fully qualified domain of this server. See {@link SlaveMigrationData#fqdn()}.
     * @return the FQDN of this server
     */
    public String fqdn() {
        return migrationData.fqdn();
    }

    /**
     * Gets the token needed for accessing ISSv3 API. See {@link SlaveMigrationData#token()}.
     * @return the access token
     */
    public String token() {
        return migrationData.token();
    }

    /**
     * Gets the root certificate authority, needed to establish a secure connection.
     * See {@link SlaveMigrationData#rootCA()}.
     * @return the root certificate authority
     */
    public String rootCA() {
        return migrationData.rootCA();
    }

    /**
     * Creates a new migration item with the current data and the specified peripheral.
     * @param peripheralIn the migrated ISSv3 peripheral server
     * @return a new migration item.
     */
    public MigrationItem withPeripheral(IssPeripheral peripheralIn) {
        return new MigrationItem(slave, migrationData, peripheralIn, true);
    }

    /**
     * Creates a new migration item with the same data, but marked as failed in the migration process.
     * @return a new failed migration item.
     */
    public MigrationItem fail() {
        return new MigrationItem(slave, migrationData, peripheral, false);
    }

    /**
     * Execute the specified action if this item is failed
     * @param action a unary operation that receives this item, perform the needed operations in case of failure and
     * returns a possibly updated version of this item.
     */
    public MigrationItem ifFailed(UnaryOperator<MigrationItem> action) {
        if (success) {
            return this;
        }

        return action.apply(this);
    }
}
