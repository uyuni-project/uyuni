/**
 * Copyright (c) 2020 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.manager.system.entitling;

import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.Server;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.impl.SaltService;

/**
 * Manager class for adding/removing entitlements to/from servers
 */
public class SystemEntitlementManager {

    public static final SystemEntitlementManager INSTANCE = new SystemEntitlementManager(
            SystemUnentitler.INSTANCE,
            new SystemEntitler(SaltService.INSTANCE, new VirtManagerSalt(SaltService.INSTANCE_SALT_API))
    );

    private SystemUnentitler systemUnentitler;
    private SystemEntitler systemEntitler;

    /**
     * Constructor for SystemEntitlementManager
     * @param systemUnentitlerIn a system unentitler
     * @param systemEntitlerIn a system entitler
     */
    public SystemEntitlementManager(SystemUnentitler systemUnentitlerIn, SystemEntitler systemEntitlerIn) {
        this.systemUnentitler = systemUnentitlerIn;
        this.systemEntitler = systemEntitlerIn;
    }

    /**
     * Checks whether or not a given server can be entitled with a specific entitlement
     * @param server The server in question
     * @param ent The entitlement to test
     * @return Returns true or false depending on whether or not the server can be
     * entitled to the passed in entitlement.
     */
    public boolean canEntitleServer(Server server, Entitlement ent) {
        return this.systemEntitler.canEntitleServer(server, ent);
    }

    /**
     * Entitles the given server to the given Entitlement
     * @param server Server to be entitled
     * @param ent Level of Entitlement
     * @return ValidatorResult of errors and warnings
     */
    public ValidatorResult addEntitlementToServer(Server server, Entitlement ent) {
        return this.systemEntitler.addEntitlementToServer(server, ent);
    }

    /**
     * Removes all the entitlements related to a server
     * @param server server to be unentitled
     */
    public void removeAllServerEntitlements(Server server) {
        this.systemUnentitler.removeAllServerEntitlements(server);
    }

    /**
     * Removes an entitlement from the given Server. If the given entitlement is the base entitlement,
     * removes all entitlements from the Server.
     * @param server the server
     * @param ent the entitlement
     */
    public void removeServerEntitlement(Server server, Entitlement ent) {
        this.systemUnentitler.removeServerEntitlement(server, ent);
    }

    /**
     * Sets the base entitlement for the passed server
     * @param server the server
     * @param baseIn the base entitlement
     */
    public void setBaseEntitlement(Server server, Entitlement baseIn) {
        if (!baseIn.isBase()) {
            throw new IllegalArgumentException("baseIn is not a base entitlement");
        }

        Entitlement baseEntitlement = server.getBaseEntitlement();
        if (baseEntitlement != null && baseIn.equals(baseEntitlement)) {
            // noop if there is no change
            return;
        }
        if (baseEntitlement != null) {
            this.systemUnentitler.removeServerEntitlement(server, baseEntitlement);
        }
        this.systemEntitler.addEntitlementToServer(server, baseIn);
    }
}
