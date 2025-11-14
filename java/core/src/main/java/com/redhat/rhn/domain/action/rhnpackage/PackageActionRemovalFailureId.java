/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */


package com.redhat.rhn.domain.action.rhnpackage;


import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;


public class PackageActionRemovalFailureId implements Serializable {

    @Serial
    private static final long serialVersionUID = 7555043505348828776L;

    private Server server;

    private Action action;

    private PackageName packageName;

    /**
     * Constructor
     */
    public PackageActionRemovalFailureId() {
    }

    /**
     * Constructor
     *
     * @param serverIn      the input server
     * @param actionIn      the input action
     * @param packageNameIn the input packageName
     */
    public PackageActionRemovalFailureId(Server serverIn, Action actionIn, PackageName packageNameIn) {
        server = serverIn;
        action = actionIn;
        packageName = packageNameIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action actionIn) {
        action = actionIn;
    }

    public PackageName getPackageName() {
        return packageName;
    }

    public void setPackageName(PackageName packageNameIn) {
        packageName = packageNameIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof PackageActionRemovalFailureId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(server, that.server)
                .append(action, that.action)
                .append(packageName, that.packageName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(server)
                .append(action)
                .append(packageName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "PackageActionRemovalFailureId{" +
                "server=" + server +
                ", action=" + action +
                ", packageName=" + packageName +
                '}';
    }
}


