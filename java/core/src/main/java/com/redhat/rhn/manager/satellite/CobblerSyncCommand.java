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

package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand;

import java.util.HashMap;
import java.util.Map;


/**
 * @author paji
 */
public class CobblerSyncCommand extends CobblerCommand {
    /**
     * @param userIn user object
     */
    public CobblerSyncCommand(User userIn) {
        super(userIn);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError store() {
        Map<String, Object> args = new HashMap<>();
        args.put("verbose", Boolean.TRUE);
        // background_sync return the event_id and not a boolean
        invokeXMLRPC("background_sync", args, xmlRpcToken);
        return null;
    }

}
