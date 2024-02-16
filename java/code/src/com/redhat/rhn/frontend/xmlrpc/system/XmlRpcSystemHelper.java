/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.system;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ShortSystemInfo;
import com.redhat.rhn.frontend.xmlrpc.BootstrapException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.controllers.bootstrap.BootstrapResult;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.utils.gson.BootstrapParameters;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * XmlRpcSystemHelper
 * Helper methods specific to the xml-rpc stack. This class should not be used outside of
 * the xml-rpc domain as it throws Xml-Rpc specific FaultExceptions.
 */
public class XmlRpcSystemHelper {

    private final RegularMinionBootstrapper regularMinionBootstrapper;
    private final SSHMinionBootstrapper sshMinionBootstrapper;

    /**
     * @param regularMinionBootstrapperIn bootstrapper for regular minions
     * @param sshMinionBootstrapperIn bootstrapper for ssh minions
     */
    public XmlRpcSystemHelper(RegularMinionBootstrapper regularMinionBootstrapperIn,
                              SSHMinionBootstrapper sshMinionBootstrapperIn) {
        regularMinionBootstrapper = regularMinionBootstrapperIn;
        sshMinionBootstrapper = sshMinionBootstrapperIn;
    }

    /**
     * Helper method to lookup a server from an sid, and throws a FaultException
     * if the server cannot be found.
     * @param user The user looking up the server
     * @param sid The id of the server we're looking for
     * @return Returns the server corresponding to sid
     * @throws NoSuchSystemException A NoSuchSystemException is thrown if the server
     * corresponding to sid cannot be found.
     */
    public Server lookupServer(User user, Number sid) throws NoSuchSystemException {
        Long serverId = sid.longValue();

        try {
            Server server = SystemManager.lookupByIdAndUser(serverId, user);

            // throw a no_such_system exception if the server was not found.
            if (server == null) {
                throw new NoSuchSystemException("No such system - sid = " + sid);
            }

            return server;
        }
        catch (LookupException e) {
            throw new NoSuchSystemException("No such system - sid = " + sid);
        }
    }

    /**
     * Helper method to lookup a bunch of servers from a list of  server ids,
     * and throws a FaultException
     * if the server cannot be found.
     * @param user The user looking up the server
     * @param serverIds The ids of the servers we're looking for
     * @return Returns a list of server corresponding to provided server id
     * @throws NoSuchSystemException A NoSuchSystemException is thrown if the server
     * corresponding to sid cannot be found.
     */
    public List<Server> lookupServers(User user, List<? extends Number> serverIds) throws NoSuchSystemException {
        try {
            return SystemManager.lookupByServerIdsAndUser(
                    serverIds.stream().map(Number::longValue).collect(Collectors.toList()), user.getId());
        }
        catch (LookupException e) {
            throw new NoSuchSystemException("No such systems found for user: " + user.getId());
        }
    }

    /**
     * Fast helper method to lookup a bunch of servers from a list of server ids optimized for channels subscriptions,
     * and throws a FaultException if the server cannot be found.
     *
     * @param user The user looking up the server
     * @param serverIds The ids of the servers we're looking for
     * @return Returns a list of server corresponding to provided server id
     * @throws NoSuchSystemException A NoSuchSystemException is thrown if the server
     * corresponding to sid cannot be found.
     */
    public List<Server> lookupServersForSubscribe(User user, List<? extends Number> serverIds)
            throws NoSuchSystemException {
        try {
            return ServerFactory.getSystemsForSubscribe(
                    serverIds.stream().map(Number::longValue).collect(Collectors.toList()), user);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException("No such systems found for user: " + user.getId());
        }
    }


    /**
     * Basically creates a Minimalist representation of a
     * server object.. This is what will be retuned
     * in most cases when some one requests a server..
     * @param server server to format
     * @return a short system info to get details on a server
     */
    public ShortSystemInfo format(Server server) {
        ShortSystemInfo serverMap = new ShortSystemInfo();
        serverMap.setId(server.getId());
        serverMap.setName(server.getName());
        serverMap.setLastCheckin(server.getLastCheckin().toString());
        serverMap.setLastBoot(server.getLastBoot());
        return serverMap;
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     *
     * @param user the current user
     * @param params bootstrap parameters
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     * @throws BootstrapException if any error occurs
     */
    public int bootstrap(User user, BootstrapParameters params, boolean saltSSH)
            throws BootstrapException {
        BootstrapResult result = Stream.of(saltSSH).map(ssh -> {
            if (ssh) {
                return sshMinionBootstrapper.bootstrap(params, user, ContactMethodUtil.getSSHMinionDefault());
            }
            else {
                return regularMinionBootstrapper.bootstrap(params, user, ContactMethodUtil.getRegularMinionDefault());
            }
        }).findAny().orElseThrow(() -> new BootstrapException("No result for " + params.getHost()));

        // Determine the result, throw BootstrapException in case of failure
        if (!result.isSuccess()) {
            throw new BootstrapException(result.getMessages().toString());
        }
        return 1;
    }
}
