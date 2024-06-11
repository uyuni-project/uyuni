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

package com.redhat.rhn.frontend.xmlrpc.system.appstreams;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.NoSuchAppStreamException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.webui.controllers.appstreams.response.ChannelAppStreamsResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handler for AppStream operations on systems

 * @apidoc.namespace system.appstreams
 * @apidoc.doc Provides methods to handle appstreams for systems.
 */
public class SystemAppStreamHandler extends BaseHandler {

    private static Logger log = LogManager.getLogger(SystemAppStreamHandler.class);

    /**
     * Schedule module stream enable.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param moduleStreams struct containing module and stream
     * @param earliestOccurrence Earliest occurrence of the module enable
     * @return appstreams changes action id
     * @apidoc.doc Schedule enabling of module streams. Invalid modules will be filtered out. If all provided
     * modules are invalid the request will fail.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_begin("moduleStreams")
     *     #struct_begin("Module Stream")
     *         #prop("string", "module")
     *         #prop("string", "stream")
     *     #struct_end()
     * #array_end()
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public int enable(User loggedInUser, Integer sid, List<Map<String, String>> moduleStreams,
                      Date earliestOccurrence) {
        try {
            Set<String> toEnable = new HashSet<>();
            Set<String> systemAppStreams = AppStreamsManager.getSystemAppStreams(sid.longValue(), loggedInUser);
            moduleStreams.forEach(moduleStream -> {
                String appStream = moduleStream.get("module") + ":" + moduleStream.get("stream");
                if (systemAppStreams.contains(appStream)) {
                    toEnable.add(appStream);
                }
                else {
                    log.warn("Appstream: {} not available. Skipping ...", appStream);
                }
            });
            if (toEnable.isEmpty()) {
                throw new NoSuchAppStreamException();
            }
            Long actionId = AppStreamsManager.scheduleAppStreamsChanges(
                    sid.longValue(),
                    toEnable,
                    new HashSet<>(),
                    loggedInUser,
                    Optional.empty(),
                    earliestOccurrence
            );
            return actionId.intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }
    }

    /**
     * Schedule module stream disable.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param moduleStreams struct containing module and stream
     * @param earliestOccurrence Earliest occurrence of the module enable
     * @return appstreams changes action id
     * @apidoc.doc Schedule disabling of module streams. Invalid modules will be filtered out. If all provided
     * modules are invalid the request will fail.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_begin("moduleStreams")
     *     #struct_begin("Module Stream")
     *         #prop("string", "module")
     *         #prop("string", "stream")
     *     #struct_end()
     * #array_end()
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public int disable(User loggedInUser, Integer sid, List<Map<String, String>> moduleStreams,
                       Date earliestOccurrence) {
        try {
            Set<String> toDisable = new HashSet<>();
            Set<String> systemAppStreams = AppStreamsManager.getSystemAppStreams(sid.longValue(), loggedInUser);
            moduleStreams.forEach(moduleStream -> {
                String appStream = moduleStream.get("module") + ":" + moduleStream.get("stream");
                if (systemAppStreams.contains(appStream)) {
                    toDisable.add(appStream);
                }
                else {
                    log.warn("Appstream: {} not available. Skipping ...", appStream);
                }
            });
            if (toDisable.isEmpty()) {
                throw new NoSuchAppStreamException();
            }
            Long actionId = AppStreamsManager.scheduleAppStreamsChanges(
                    sid.longValue(),
                    new HashSet<>(),
                    toDisable,
                    loggedInUser,
                    Optional.empty(),
                    earliestOccurrence
            );
            return actionId.intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }
    }

    /**
     * List available module streams for a given system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @return List of appstreams
     * @apidoc.doc List available module streams for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     * #return_array_begin()
     *     $ChannelAppStreamsResponseSerializer
     * #array_end()
     */
    @ReadOnly
    public List<ChannelAppStreamsResponse> listModuleStreams(User loggedInUser, Integer sid) {
        try {
            Server server = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);
            return server
                    .getChannels()
                    .stream()
                    .filter(Channel::isModular)
                    .map(channel -> new ChannelAppStreamsResponse(
                            channel,
                            AppStreamsManager.listChannelAppStreams(channel.getId()),
                            server::hasAppStreamModuleEnabled
                    ))
                    .collect(Collectors.toList());
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }
    }
}
