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
package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.satellite.CobblerSyncCommand;

import org.cobbler.SystemRecord;

/**
 * Rename system in cobbler
 */
public class CobblerSystemRenameCommand extends CobblerCommand {

    private Server server;
    private String newName;

    /**
     * Constructor
     * @param serverIn - Server instance being renamed.
     * @param newNameIn - new name
     */
    public CobblerSystemRenameCommand(Server serverIn, String newNameIn) {
        this.server = serverIn;
        this.newName = newNameIn;
    }

    /**
     * Rename the System in cobbler
     * @return ValidatorError
     */
    @Override
    public ValidatorError store() {
        SystemRecord rec = lookupExisting(server);
        if (rec != null) {
            rec.setName(CobblerSystemCreateCommand.getCobblerSystemRecordName(newName, server.getOrg().getId()));
            return new CobblerSyncCommand(user).store();
        }
        return null;
    }
}
