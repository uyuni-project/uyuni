/*
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
package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.SystemRecord;

/**
 * Changes power management settings for a server.
 */
public class CobblerUnregisteredPowerSettingsUpdateCommand extends CobblerPowerSettingsUpdateCommand {

    /** The log. */
    private static Logger log = LogManager.getLogger(CobblerUnregisteredPowerSettingsUpdateCommand.class);

    /** The server to update. */
    private String label;

    /**
     * Standard constructor. Empty parameters strings can be used to leave
     * existing values untouched.
     * @param userIn the user running this command
     * @param labelIn cobbler system name (prefix)
     * @param powerTypeIn the new power management scheme
     * @param powerAddressIn the new power management IP address or hostname
     * @param powerUsernameIn the new power management username
     * @param powerPasswordIn the new power management password
     * @param powerIdIn the new power management id
     */
    public CobblerUnregisteredPowerSettingsUpdateCommand(User userIn, String labelIn,
        String powerTypeIn, String powerAddressIn, String powerUsernameIn,
        String powerPasswordIn, String powerIdIn) {
        super(userIn, null, powerTypeIn, powerAddressIn, powerUsernameIn, powerPasswordIn, powerIdIn);
        label = labelIn;
    }

    protected String getIdent() {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        label = label.replace(' ', '_').replaceAll("[^a-zA-Z0-9_\\-\\.]", "");
        return label + sep + user.getOrg().getId();
    }

    protected SystemRecord getSystemRecordForSystem() {
        SystemRecord rec = SystemRecord.lookupByName(
                CobblerXMLRPCHelper.getConnection(user), getIdent());
        if (rec == null) {
            log.info("System with cobbler name " + getIdent() + " not found.");
        }
        return rec;
    }
}
