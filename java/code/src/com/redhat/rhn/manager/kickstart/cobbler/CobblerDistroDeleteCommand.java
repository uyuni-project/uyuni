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
package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.satellite.CobblerSyncCommand;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.Distro;

/**
 * KickstartCobblerCommand - class to contain logic to communicate with cobbler
 */
public class CobblerDistroDeleteCommand extends CobblerDistroCommand {

    /**
     * Logger for this class
     */
    private static Logger log = LogManager.getLogger(CobblerDistroDeleteCommand.class);


    /**
     * Constructor
     * @param ksTreeIn to sync
     * @param userIn - user wanting to sync with cobbler
     */
    public CobblerDistroDeleteCommand(KickstartableTree ksTreeIn,
            User userIn) {
        super(ksTreeIn, userIn);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError store() {
        //Upgrade Scenario where a cobbler id will be null
        if (StringUtils.isBlank(tree.getCobblerId())) {
            return null;
        }

        CobblerConnection con = CobblerXMLRPCHelper.getConnection(user);
        Distro dis = Distro.lookupById(con, tree.getCobblerId());

        if (dis == null) {
            log.warn("No cobbler distro associated with this Tree.");
            return null;
        }
        if (!dis.remove()) {
            return new ValidatorError("cobbler.distro.remove_failed");
        }

        if (tree.getCobblerXenId() != null) {
            dis = Distro.lookupById(con, tree.getCobblerXenId());
            if (dis == null) {
                log.warn("No cobbler distro associated with this Tree.");
                return null;
            }
            if (!dis.remove()) {
                return new ValidatorError("cobbler.distro.remove_failed");
            }
        }

        return new CobblerSyncCommand(user).store();

    }


}
