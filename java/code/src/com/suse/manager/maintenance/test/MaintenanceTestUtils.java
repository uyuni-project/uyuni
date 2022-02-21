/*
 * Copyright (c) 2020--2021 SUSE LLC
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
package com.suse.manager.maintenance.test;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MaintenanceTestUtils {

    /**
     * Create an Errata Action for the given server at a specific point in time
     *
     * @param user the user
     * @param type action type
     * @param server the server
     * @param datetime time template for earliest action. Example: "2020-04-21T09:00:00+01:00"
     * @param prerequisite dependend action
     * @return the Action
     * @throws Exception if any failure occurs
     */
    public static Action createActionForServerAt(User user, ActionType type, Server server, String datetime,
            Action prerequisite) throws Exception {
        Action action = ActionFactoryTest.createAction(user, type);
        action.setPrerequisite(prerequisite);
        ZonedDateTime start = ZonedDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        action.setEarliestAction(Date.from(start.toInstant()));

        ServerAction serverAction = ServerActionTest.createServerAction(server, action);
        serverAction.setStatus(ActionFactory.STATUS_QUEUED);

        action.addServerAction(serverAction);
        ActionManager.storeAction(action);
        return ActionFactory.lookupById(action.getId());
    }

    /**
     * Create an Errata Action for the given server at a specific point in time
     *
     * @param user the user
     * @param type action type
     * @param server the server
     * @param datetime time template for earliest action. Example: "2020-04-21T09:00:00+01:00"
     * @return the Action
     * @throws Exception if any failure occurs
     */
    public static Action createActionForServerAt(User user, ActionType type, Server server, String datetime)
            throws Exception {
        return createActionForServerAt(user, type, server, datetime, null);
    }

    private MaintenanceTestUtils() {
    }
}
