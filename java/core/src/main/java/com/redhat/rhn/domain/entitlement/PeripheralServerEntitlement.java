/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.domain.entitlement;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

/**
 * PeripheralServer entitlement
 */
public class PeripheralServerEntitlement extends Entitlement {

    /**
     * Standard constructor
     */
    public PeripheralServerEntitlement() {
        super(EntitlementManager.PERIPHERAL_SERVER_ENTITLED);
    }

    /**
     * Constructs an Entitlement labeled <code>lbl</code>.
     *
     * @param lbl Entitlement label.
     */
    PeripheralServerEntitlement(String lbl) {
        super(lbl);
    }

    @Override
    public boolean isPermanent() {
        return false;
    }

    @Override
    public boolean isBase() {
        return false;
    }

    @Override
    public boolean isAllowedOnServer(Server server) {
        if (server.getFqdns().stream()
                .flatMap(fqdn -> ServerFactory.listByFqdn(fqdn.getName()).stream())
                .filter(s -> !s.equals(server))
                .anyMatch(s -> s.hasEntitlement(EntitlementManager.PERIPHERAL_SERVER))) {
            // Peripheral Server with given fqdn already exists
            return false;
        }
        return super.isAllowedOnServer(server) &&
                ((server.getBaseEntitlement() instanceof SaltEntitlement) ||
                 (server.getBaseEntitlement() instanceof ForeignEntitlement));
    }
}
