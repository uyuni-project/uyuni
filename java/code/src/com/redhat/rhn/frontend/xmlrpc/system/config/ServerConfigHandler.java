/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.system.config;

import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ConfigFileDto;
import com.redhat.rhn.frontend.dto.ConfigFileNameDto;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidOperationException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchConfigFilePathException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.UnsupportedOperationException;
import com.redhat.rhn.frontend.xmlrpc.configchannel.XmlRpcConfigChannelHelper;
import com.redhat.rhn.frontend.xmlrpc.serializer.ConfigFileNameDtoSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.ConfigRevisionSerializer;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.MissingCapabilityException;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.utils.MinionServerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ServerConfigChannelHandler
 * @apidoc.namespace system.config
 * @apidoc.doc Provides methods to access and modify many aspects of
 * configuration channels and server association.
 * basically system.config name space
 */
public class ServerConfigHandler extends BaseHandler {

    private final TaskomaticApi taskomaticApi;
    private final XmlRpcSystemHelper xmlRpcSystemHelper;


    /**
     * @param taskomaticApiIn the {@link TaskomaticApi}
     * @param xmlRpcSystemHelperIn XmlRpcSystemHelper
     */
    public ServerConfigHandler(TaskomaticApi taskomaticApiIn, XmlRpcSystemHelper xmlRpcSystemHelperIn) {
        taskomaticApi = taskomaticApiIn;
        xmlRpcSystemHelper = xmlRpcSystemHelperIn;
    }
    /**
     * List files in a given server
     * @param loggedInUser The current user
     * @param sid the server id
     * @param listLocal true if a list of paths in local override is desired
     *                  false if  list of paths in sandbox channel is desired
     * @return a list of dto's holding this info.
     *
     * @apidoc.doc Return the list of files in a given channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("int", "listLocal")
     *      #options()
     *          #item_desc ("1", "to return configuration files
     *              in the system's local override configuration channel")
     *          #item_desc ("0", "to return configuration files
     *              in the system's sandbox configuration channel")
     *      #options_end()
     *
     * @apidoc.returntype
     * #return_array_begin()
     * $ConfigFileNameDtoSerializer
     * #array_end()
     */
    @ReadOnly
    public List<ConfigFileNameDto> listFiles(User loggedInUser,
            Integer sid, Boolean listLocal) {
        ConfigurationManager cm = ConfigurationManager.getInstance();
        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, sid);
        if (listLocal) {
            return cm.listFileNamesForSystemQuick(loggedInUser, server, null);
        }
        List<ConfigFileNameDto> files = new LinkedList<>();
        List<ConfigFileDto> currentFiles = cm.listCurrentFiles(loggedInUser,
                server.getSandboxOverride(), null);
        for (ConfigFileDto dto : currentFiles) {
            files.add(ConfigFileNameDtoSerializer.toNameDto(dto,
                    ConfigChannelType.SANDBOX, null));
        }
        return files;
    }

    /**
     * Creates a NEW path(file/directory) with the given path or updates an existing path
     * with the given contents in a given server.
     * @param loggedInUser The current user
     * @param sid the server id.
     * @param path the path of the given text file.
     * @param isDir true if this is a directory path, false if its to be a file path
     * @param data a map containing properties pertaining to the given path..
     * for directory paths - 'data' will hold values for -&gt;
     *  owner, group, permissions
     * for file paths -  'data' will hold values for-&gt;
     *  contents, owner, group, permissions, macro-start-delimiter, macro-end-delimiter
     * @param commitToLocal true if we want to commit the file to
     * the server's local channel false if we want to commit it to sandbox.
     * @return returns the new created or updated config revision..
     * @since 10.2
     *
     * @apidoc.doc Create a new file (text or binary) or directory with the given path, or
     * update an existing path on a server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "path", "the configuration file/directory path")
     * @apidoc.param #param( "boolean", "isDir")
     *      #options()
     *          #item_desc ("True", "if the path is a directory")
     *          #item_desc ("False", "if the path is a file")
     *      #options_end()
     * @apidoc.param
     *   #struct_begin("data")
     *      #prop_desc("string", "contents",
     *              "Contents of the file (text or base64 encoded if binary)
     *                   ((only for non-directories)")
     *      #prop_desc("boolean", "contents_enc64", "Identifies base64 encoded content
     *                  (default: disabled, only for non-directories).")
     *      #prop_desc("string", "owner", "Owner of the file/directory.")
     *      #prop_desc("string", "group", "Group name of the file/directory.")
     *      #prop_desc("string", "permissions",
     *                          "Octal file/directory permissions (eg: 644)")
     *      #prop_desc("string", "macro-start-delimiter",
     *                  "Config file macro end delimiter. Use null or empty string
     *              to accept the default. (only for non-directories)")
     *      #prop_desc("string", "macro-end-delimiter",
     *                   "Config file macro end delimiter. Use null or empty string
     *              to accept the default. (only for non-directories)")
     *      #prop_desc("string", "selinux_ctx",
     *                   "SeLinux context (optional)")
     *      #prop_desc("int", "revision", "next revision number, auto increment for null")
     *      #prop_desc("boolean", "binary", "mark the binary content, if True,
     *      base64 encoded content is expected (only for non-directories)")
     *  #struct_end()
     * @apidoc.param #param("boolean", "commitToLocal")
     *      #options()
     *          #item_desc ("1", "to commit configuration files
     *              to the system's local override configuration channel")
     *          #item_desc ("0", "to commit configuration files
     *              to the system's sandbox configuration channel")
     *      #options_end()
     * @apidoc.returntype
     *              $ConfigRevisionSerializer
     */
    public ConfigRevision createOrUpdatePath(User loggedInUser,
            Integer sid,
            String path,
            Boolean isDir,
            Map<String, Object> data,
            Boolean commitToLocal) {

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<>();
        validKeys.add(ConfigRevisionSerializer.OWNER);
        validKeys.add(ConfigRevisionSerializer.GROUP);
        validKeys.add(ConfigRevisionSerializer.PERMISSIONS);
        validKeys.add(ConfigRevisionSerializer.REVISION);
        validKeys.add(ConfigRevisionSerializer.SELINUX_CTX);
        if (!isDir) {
            validKeys.add(ConfigRevisionSerializer.CONTENTS);
            validKeys.add(ConfigRevisionSerializer.CONTENTS_ENC64);
            validKeys.add(ConfigRevisionSerializer.MACRO_START);
            validKeys.add(ConfigRevisionSerializer.MACRO_END);
            validKeys.add(ConfigRevisionSerializer.BINARY);
        }
        validateMap(validKeys, data);

        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, sid);
        checkIfLocalPermissible(server);
        ConfigChannel channel;
        if (commitToLocal) {
            channel = server.getLocalOverride();
        }
        else {
            channel = server.getSandboxOverride();
        }
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        return configHelper.createOrUpdatePath(loggedInUser, channel, path,
                isDir ? ConfigFileType.dir() : ConfigFileType.file(), data);
    }


    /**
     * Creates a NEW symbolic link with the given path or updates an existing path
     * with the given target_path in a given server.
     * @param loggedInUser The current user
     * @param sid the server id.
     * @param path the path of the given text file.
     * @param data a map containing properties pertaining to the given path..
     * 'data' will hold values for -&gt;
     *      target_paths, selinux_ctx
     * @param commitToLocal true if we want to commit the file to
     * the server's local channel false if we want to commit it to sandbox.
     * @return returns the new created or updated config revision..
     * @since 10.2
     *
     * @apidoc.doc Create a new symbolic link with the given path, or
     * update an existing path.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "path", "the configuration file/directory path")
     * @apidoc.param
     *  #struct_begin("data")
     *      #prop_desc("string", "target_path",
     *              "The target path for the symbolic link")
     *      #prop_desc("string", "selinux_ctx", "SELinux Security context (optional)")
     *      #prop_desc("int", "revision", "next revision number, auto increment for null")
     *  #struct_end()
     * @apidoc.param #param("boolean", "commitToLocal")
     *      #options()
     *          #item_desc ("1", "to commit configuration files
     *              to the system's local override configuration channel")
     *          #item_desc ("0", "to commit configuration files
     *              to the system's sandbox configuration channel")
     *      #options_end()
     * @apidoc.returntype
     *              $ConfigRevisionSerializer
     */
    public ConfigRevision createOrUpdateSymlink(User loggedInUser,
            Integer sid,
            String path,
            Map<String, Object> data,
            Boolean commitToLocal) {

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<>();
        validKeys.add(ConfigRevisionSerializer.TARGET_PATH);
        validKeys.add(ConfigRevisionSerializer.SELINUX_CTX);
        validKeys.add(ConfigRevisionSerializer.REVISION);
        validateMap(validKeys, data);

        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, sid);
        checkIfLocalPermissible(server);
        ConfigChannel channel;
        if (commitToLocal) {
            channel = server.getLocalOverride();
        }
        else {
            channel = server.getSandboxOverride();
        }
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        return configHelper.createOrUpdatePath(loggedInUser, channel, path,
                ConfigFileType.symlink(), data);
    }

    /**
     * Given a list of paths and a server the method returns details about the latest
     * revisions of the paths.
     * @param loggedInUser The current user
     * @param sid the server id
     * @param paths a list of paths to examine.
     * @param searchLocal true look at local overrides, false
     *              to look at sandbox overrides
     * @return a list containing the latest config revisions of the requested paths.
     * @since 10.2
     *
     * @apidoc.doc Given a list of paths and a server, returns details about
     * the latest revisions of the paths.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single_desc("string" "paths", "paths to lookup on.")
     * @apidoc.param #param("boolean", "searchLocal")
     *      #options()
     *          #item_desc ("1", "to search configuration file paths
     *              in the system's local override configuration or
     *              systems subscribed central channels")
     *          #item_desc ("0", "to search configuration file paths
     *              in the system's sandbox configuration channel")
     *      #options_end()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ConfigRevisionSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ConfigRevision> lookupFileInfo(User loggedInUser,
            Integer sid, List<String> paths, Boolean searchLocal) {
        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, sid);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        List<ConfigRevision> revisions = new LinkedList<>();
        for (String path : paths) {
            ConfigFile cf;
            if (searchLocal) {
                cf = cm.lookupConfigFile(loggedInUser,
                        server.getLocalOverride().getId(), path);

                if (cf == null) {
                    cf = server.getConfigChannelStream()
                            .map(cn -> cm.lookupConfigFile(loggedInUser, cn.getId(), path))
                            .filter(Objects::nonNull).findFirst().orElse(null);
                }
            }
            else {
                cf = cm.lookupConfigFile(loggedInUser,
                        server.getSandboxOverride().getId(), path);
            }
            if (cf != null) {
                revisions.add(cf.getLatestConfigRevision());
            }
        }
        return revisions;
    }

    /**
     * Removes a list of paths from a local or sandbox channel of a server..
     * @param loggedInUser The current user
     * @param sid the server id to remove the files from..
     * @param paths the list of paths to delete.
     * @param deleteFromLocal true if we want to delete form local channel
     *                         false if we want to delete from sandbox..
     * @return 1 if successful with the operation errors out otherwise.
     *
     *
     * @apidoc.doc Removes file paths from a local or sandbox channel of a server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("string", "paths", "paths to remove.")
     * @apidoc.param #param("boolean", "deleteFromLocal")
     *      #options()
     *          #item_desc ("True", "to delete configuration file paths
     *              from the system's local override configuration channel")
     *          #item_desc ("False", "to delete configuration file paths
     *              from the system's sandbox configuration channel")
     *      #options_end()
     * @apidoc.returntype #return_int_success()
     *
     */
    public int deleteFiles(User loggedInUser,
            Integer sid,
            List<String> paths,
            Boolean deleteFromLocal) {
        ConfigurationManager cm = ConfigurationManager.getInstance();
        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, sid);
        List<ConfigFile> cfList = new ArrayList<>();
        for (String path : paths) {
            ConfigFile cf;
            if (deleteFromLocal) {
                cf = cm.lookupConfigFile(loggedInUser,
                        server.getLocalOverride().getId(), path);
            }
            else {
                cf = cm.lookupConfigFile(loggedInUser,
                        server.getSandboxOverride().getId(), path);
            }
            if (cf == null) {
                throw new NoSuchConfigFilePathException(path);
            }
            cfList.add(cf);
        }
        for (ConfigFile cf : cfList) {
            cm.deleteConfigFile(loggedInUser, cf);
        }
        return 1;
    }


    /**
     * Schedules a deploy action for all the configuration files
     * of a given list of servers.
     *
     * @param loggedInUser The current user
     * @param sids  list of IDs of the server to schedule the deploy action
     * @param date date of the deploy action..
     * @return 1 on success, raises exceptions otherwise.
     *
     * @apidoc.doc Schedules a deploy action for all the configuration files
     * on the given list of systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single_desc("int", "sids",
     *              "IDs of the systems to schedule configuration files deployment")
     * @apidoc.param #param_desc($date, "date",
     *                               "Earliest date for the deploy action.")
     * @apidoc.returntype #return_int_success()
     */
    public int deployAll(User loggedInUser, List<Number> sids, Date date) {
        List<Server> servers = new ArrayList<>(sids.size());
        for (Number sid : sids) {
            servers.add(xmlRpcSystemHelper.lookupServer(loggedInUser, sid));
        }
        ConfigurationManager manager = ConfigurationManager.getInstance();
        try {
            manager.deployConfiguration(loggedInUser, servers, date);
        }
        catch (MissingCapabilityException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingCapabilityException(
                    e.getCapability(), e.getServer());
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return 1;
    }

    /**
     * List all the global channels associated to a system
     * in the order of their ranking.
     * @param loggedInUser The current user
     * @param sid a system id
     * @return a list of global config channels associated to the given
     *          system in the order of their ranking..
     *
     * @apidoc.doc List all global('Normal', 'State') configuration channels associated to a
     *              system in the order of their ranking.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *  #return_array_begin()
     *  $ConfigChannelSerializer
     *  #array_end()
     */
    @ReadOnly
    public List<ConfigChannel> listChannels(User loggedInUser, Integer sid) {
        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, sid);
        return server.getConfigChannelList();
    }

    /**
     * Given a list of servers and configuration channels,
     * this method inserts the configuration channels to either the top or
     * the bottom (whichever you specify) of a system's subscribed
     * configuration channels list. The ordering of the configuration channels
     * provided in the add list is maintained while adding.
     * If one of the configuration channels in the 'add' list
     * has been previously subscribed by a server, the
     * subscribed channel will be re-ranked to the appropriate place.
     * @param loggedInUser The current user
     * @param sids a list of ids of servers to add the configuration channels to.
     * @param configChannelLabels set of configuration channels labels
     * @param addToTop if true inserts the configuration channels list to
     *                  the top of the configuration channels list of a server
     * @return 1 on success 0 on failure
     *
     * @apidoc.doc Given a list of servers and configuration channels,
     * this method appends the configuration channels to either the top or
     * the bottom (whichever you specify) of a system's subscribed
     * configuration channels list. The ordering of the configuration channels
     * provided in the add list is maintained while adding.
     * If one of the configuration channels in the 'add' list
     * has been previously subscribed by a server, the
     * subscribed channel will be re-ranked to the appropriate place.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single_desc("int", "sids",
     *              "IDs of the systems to add the channels to.")
     * @apidoc.param #array_single_desc("string", "configChannelLabels",
     *              "List of configuration channel labels in the ranked order.")
     * @apidoc.param #param("boolean", "addToTop")
     *      #options()
     *          #item_desc ("true", "to prepend the given channels
     *          list to the top of the configuration channels list of a server")
     *          #item_desc ("false", "to append the given  channels
     *          list to the bottom of the configuration channels list of a server")
     *      #options_end()
     *
     * @apidoc.returntype #return_int_success()
     */
    public int addChannels(User loggedInUser, List<Number> sids, List<String> configChannelLabels,
                           Boolean addToTop) {
        List<Server> servers = xmlRpcSystemHelper.lookupServersForSubscribe(loggedInUser, sids);
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        List<ConfigChannel> channels = configHelper.lookupGlobals(loggedInUser, configChannelLabels);

        //A state channel cannot be assigned to a traditional system so we simply fail the call if such case appears
        if (channels.stream().anyMatch(ConfigChannel::isStateChannel) &&
                servers.stream().anyMatch(srv->!MinionServerUtils.isMinionServer(srv))) {
            throw new InvalidOperationException(LocalizationService.getInstance()
                    .getMessage("state.channels.not.supported.for.traditional"));
        }

        List<ConfigChannel> channelsToAdd;
        for (Server server : servers) {
            // No need to do anything if we have to add exactly what is already set on the server
            if (channels.equals(server.getConfigChannelList())) {
                continue;
            }
            if (addToTop) {
                // Add the existing subscriptions to the end so they will be resubscribed
                // and their ranks will be overridden
                channelsToAdd = Stream
                        .concat(channels.stream(), server.getConfigChannelStream().filter(c -> !channels.contains(c)))
                        .collect(Collectors.toList());
            }
            else {
                // Channels are added to the end by default
                channelsToAdd = channels;
            }

            server.subscribeConfigChannels(channelsToAdd, loggedInUser);
            server.storeConfigChannels();
        }

        return 1;
    }

    /**
     * replaces the existing set of config channels for a given
     * list of servers.
     * Note: it ranks these channels according to the array order of
     * configChannelLabels method parameter
     * @param loggedInUser The current user
     * @param sids a list of ids of servers to change the config files for..
     * @param configChannelLabels sets channels labels
     * @return 1 on success 0 on failure
     *
     * @apidoc.doc Replace the existing set of config channels on the given servers.
     * Channels are ranked according to their order in the configChannelLabels
     * array.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single_desc("int", "sids",
     *              "IDs of the systems to set the channels on.")
     * @apidoc.param #array_single_desc("string", "configChannelLabels",
     *              "List of configuration channel labels in the ranked order.")
     *
     * @apidoc.returntype #return_int_success()
     */
    public int setChannels(User loggedInUser, List<Number> sids,
            List<String> configChannelLabels) {
        List<Server> servers = xmlRpcSystemHelper.lookupServersForSubscribe(loggedInUser, sids);
        XmlRpcConfigChannelHelper configHelper =
                XmlRpcConfigChannelHelper.getInstance();
        List<ConfigChannel> channels = configHelper.
                lookupGlobals(loggedInUser, configChannelLabels);

        servers.forEach(s -> {
            if (!channels.equals(s.getConfigChannelList())) {
                s.setConfigChannels(channels, loggedInUser);
                s.storeConfigChannels();
            }
        });
        return 1;
    }

    /**
     * removes selected channels from list of config channels provided
     * for a given list of servers.
     * @param loggedInUser The current user
     * @param sids the list of server ids.
     * @param configChannelLabels sets channels labels
     * @return 1 on success 0 on failure
     *
     * @apidoc.doc Remove config channels from the given servers.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single_desc("int", "sids", "the IDs of the systems from which you
     *              would like to remove configuration channels..")
     * @apidoc.param #array_single_desc("string", "configChannelLabels",
     *              "List of configuration channel labels to remove.")
     * @apidoc.returntype #return_int_success()
     */
    public int removeChannels(User loggedInUser, List<Number> sids,
            List<String> configChannelLabels) {
        List<Server> servers = xmlRpcSystemHelper.lookupServersForSubscribe(loggedInUser, sids);
        XmlRpcConfigChannelHelper configHelper =
                XmlRpcConfigChannelHelper.getInstance();
        List<ConfigChannel> channels = configHelper.
                lookupGlobals(loggedInUser, configChannelLabels);

        // Check if all the channels are successfully unsubscribed
        int channelCount = configChannelLabels.size();
        int[] countsExpected = servers.stream().mapToInt(s -> s.getConfigChannelCount() - channelCount).toArray();

        servers.forEach(s -> {
            if (channels.stream().anyMatch(ch -> s.getConfigChannelList().contains(ch))) {
                s.unsubscribeConfigChannels(channels, loggedInUser);
                s.storeConfigChannels();
            }
        });

        int[] countsActual = servers.stream().mapToInt(Server::getConfigChannelCount).toArray();
        return Arrays.equals(countsExpected, countsActual) ? 1 : 0;
    }

    /**
     * Schedule applying state configuration channels for a given system.
     *
     * @param user The current user
     * @param sids The system id of the target system
     * @param earliest Earliest occurrence
     * @param test Run states in test-only mode
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Schedule highstate application for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "test", "Run states in test-only mode")
     * @apidoc.returntype #param("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleApplyConfigChannel(User user, List<Integer> sids, Date earliest, Boolean test) {
        try {
            // Validate the given system id
            List<Server> servers = xmlRpcSystemHelper.lookupServers(user, sids);

            servers.stream().filter(srv->!MinionServerUtils.isMinionServer(srv)).findFirst().ifPresent(srv-> {
                throw new UnsupportedOperationException("Aborting. System not managed with Salt: " + srv.getId());
            });

            List<String> states = Collections.singletonList("custom");
            List<Long> serverIds = sids.stream().map(Integer::longValue).collect(toList());
            Action action = ActionManager.scheduleApplyStates(user, serverIds, states, earliest, Optional.of(test));
            taskomaticApi.scheduleActionExecution(action);
            return action.getId();
        }
        catch (LookupException e) {
            throw new NoSuchSystemException(e);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Check if operation is permissible
     * @param server server
     */
    private void checkIfLocalPermissible(Server server) {
        if (server.asMinionServer().isPresent()) {
            throw new InvalidOperationException(LocalizationService.getInstance()
                    .getMessage("channels.not.supported.for.minions"));
        }
    }
}
