/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.reactor.utils.ValueMap;

import org.apache.commons.lang3.StringUtils;


/**
 * VirtualizationEntitlement
 */
public class VirtualizationEntitlement extends Entitlement {

    /**
     * Constructor
     */
    public VirtualizationEntitlement() {
        super(EntitlementManager.VIRTUALIZATION_ENTITLED);
    }

    VirtualizationEntitlement(String labelIn) {
        super(labelIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPermanent() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBase() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAllowedOnServer(Server server) {
        return super.isAllowedOnServer(server) && !server.isVirtualGuest() &&
               !(server.getBaseEntitlement() instanceof ForeignEntitlement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAllowedOnServer(Server server, ValueMap grains) {
        String type = grains.getOptionalAsString("virtual").orElse("physical");
        String subtype = grains.getOptionalAsString("virtual_subtype").orElse("");
        return super.isAllowedOnServer(server) && !isVirtualGuest(type, subtype) &&
               !(server.getBaseEntitlement() instanceof ForeignEntitlement);
    }

    /**
     * Returns whether a system is a virtual guest or not according to its virtual and virtual_subtype grains.
     *
     * @param virtTypeLowerCase the virtual grain value
     * @param virtSubtype the virtual_subtype grain value
     *
     * @return if the system is virtual
     */
    public static boolean isVirtualGuest(String virtTypeLowerCase, String virtSubtype) {
        return StringUtils.isNotBlank(virtTypeLowerCase) &&
                !"physical".equals(virtTypeLowerCase) &&
                !("xen".equals(virtTypeLowerCase) && "Xen Dom0".equals(virtSubtype));
    }
}
