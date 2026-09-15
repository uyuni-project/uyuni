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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.satellite;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;

/**
 * This class extends the {@link BaseConfigAction} class to provide specific command configuration for enabling SCC
 * data forwarding.
 */
public class SCCDataForwardingConfigAction extends BaseConfigAction {
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCommandClassName() {
        return Config.get().getString(
                "web.com.redhat.rhn.frontend.action.satellite.GeneralConfigAction.command",
                "com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigureSatelliteCommand getCommand(User user) {
        return (ConfigureSatelliteCommand) super.getCommand(user);
    }
}
