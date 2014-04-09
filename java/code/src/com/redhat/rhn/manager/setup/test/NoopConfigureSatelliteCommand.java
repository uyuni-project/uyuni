/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;

/**
 * A {@link ConfigureSatelliteCommand} that will not try to write to rhn.conf for testing.
 */
public class NoopConfigureSatelliteCommand extends ConfigureSatelliteCommand {
    /**
     * {@inheritDoc}
     * @param userIn user
     */
    public NoopConfigureSatelliteCommand(User userIn) {
        super(userIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError[] storeConfiguration() {
        this.clearUpdates();
        return null;
    }
}
