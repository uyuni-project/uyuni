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

package com.redhat.rhn.domain.org;

import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


public class SystemMigrationId implements Serializable {

    @Serial
    private static final long serialVersionUID = 6494008438235722454L;

    private Org toOrg;

    private Org fromOrg;

    private Server server;

    private Date migrated;

    /**
     * Constructor
     */
    public SystemMigrationId() {
    }

    /**
     * Constructor
     *
     * @param toOrgIn    the input toOrg
     * @param fromOrgIn  the input fromOrg
     * @param serverIn   the input server
     * @param migratedIn the input migrated
     */
    public SystemMigrationId(Org toOrgIn, Org fromOrgIn, Server serverIn, Date migratedIn) {
        toOrg = toOrgIn;
        fromOrg = fromOrgIn;
        server = serverIn;
        migrated = migratedIn;
    }

    public Org getToOrg() {
        return toOrg;
    }

    public void setToOrg(Org toOrgIn) {
        toOrg = toOrgIn;
    }

    public Org getFromOrg() {
        return fromOrg;
    }

    public void setFromOrg(Org fromOrgIn) {
        fromOrg = fromOrgIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
    }

    public Date getMigrated() {
        return migrated;
    }

    public void setMigrated(Date migratedIn) {
        migrated = migratedIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof SystemMigrationId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(toOrg, that.toOrg)
                .append(fromOrg, that.fromOrg)
                .append(server, that.server)
                .append(migrated, that.migrated)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(toOrg)
                .append(fromOrg)
                .append(server)
                .append(migrated)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "SystemMigrationId{" +
                "toOrg=" + toOrg +
                ", fromOrg=" + fromOrg +
                ", server=" + server +
                ", migrated=" + migrated +
                '}';
    }
}
