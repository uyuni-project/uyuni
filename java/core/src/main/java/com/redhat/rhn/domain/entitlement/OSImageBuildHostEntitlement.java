/*
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.reactor.utils.ValueMap;

/**
 * OS Image build host entitlement
 */
public class OSImageBuildHostEntitlement extends Entitlement {

    /**
     * Constructor
     */
    public OSImageBuildHostEntitlement() {
        super(EntitlementManager.OSIMAGE_BUILD_HOST_ENTITLED);
    }

    OSImageBuildHostEntitlement(String labelIn) {
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
        return  super.isAllowedOnServer(server) &&
                server.getBaseEntitlement() instanceof SaltEntitlement &&
                server.doesOsSupportsOSImageBuilding() &&
                Config.get().getBoolean(ConfigDefaults.KIWI_OS_IMAGE_BUILDING_ENABLED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAllowedOnServer(Server server, ValueMap grains) {
        return isAllowedOnServer(server);
    }
}
