/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.server.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;

public class ServerActionFactory extends HibernateFactory {
    private static final Logger LOG = LogManager.getLogger(ServerActionFactory.class);

    private ServerActionFactory() {
        //utility classes should not have a public constructor
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Lookup a List of ServerAction objects for a given Server.
     *
     * @param serverIn you want to limit the list of Actions to
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn) {
        return ActionFactory.listServerActionsForServer(serverIn);
    }

    /**
     * Lookup a List of ServerAction objects for a given Server.
     *
     * @param serverIn             you want to limit the list of Actions to
     * @param actionTypeNameIn     you want to limit the list of Actions to
     * @param minimumCreatedDateIn you want to limit the completion date after
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                String actionTypeNameIn,
                                                                Date minimumCreatedDateIn) {
        return ActionFactory.listServerActionsForServer(serverIn, actionTypeNameIn, minimumCreatedDateIn);
    }

    /**
     * Lookup a List of ServerAction objects for a given Server and Action Type.
     *
     * @param serverIn         you want to limit the list of Actions to
     * @param actionTypeEnumIn you want to limit the list of Actions to
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                ActionTypeEnum actionTypeEnumIn) {
        List<ActionType> typesIn = List.of(ActionFactory.lookupActionTypeByEnum(actionTypeEnumIn));
        return ActionFactory.listServerActionsForServerAndTypes(serverIn, typesIn);
    }

    /**
     * Lookup a List of ServerAction objects in the given status for a given Server.
     *
     * @param serverIn     you want to limit the list of Actions to
     * @param statusIn to filter the ServerActions by
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn, ActionStatus statusIn) {
        return ActionFactory.listServerActionsForServer(serverIn, List.of(statusIn));
    }

    /**
     * Lookup a List of ServerAction objects in the given states for a given Server.
     *
     * @param serverIn     you want to limit the list of Actions to
     * @param statusListIn to filter the ServerActoins by
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                List<ActionStatus> statusListIn) {
        return ActionFactory.listServerActionsForServer(serverIn, statusListIn);
    }

    /**
     * Lookup a List of ServerAction objects in the given states for a given Server.
     *
     * @param serverIn             you want to limit the list of Actions to
     * @param statusListIn         to filter the ServerActions by
     * @param minimumCreatedDateIn to filter the ServerActions by
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                List<ActionStatus> statusListIn,
                                                                Date minimumCreatedDateIn) {
        return ActionFactory.listServerActionsForServer(serverIn, statusListIn, minimumCreatedDateIn);
    }

}
