/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionResult;
import com.redhat.rhn.domain.action.salt.StateResult;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusGuestAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.channel.NoBaseChannelFoundException;
import com.redhat.rhn.domain.dto.SystemGroupsDTO;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.profile.DuplicateProfileNameException;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.Location;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.server.PushClient;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.server.SnapshotTag;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.ActivationKeyDto;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.frontend.dto.HistoryEvent;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.PackageMetadata;
import com.redhat.rhn.frontend.dto.ProfileOverviewDto;
import com.redhat.rhn.frontend.dto.ServerPath;
import com.redhat.rhn.frontend.dto.ShortSystemInfo;
import com.redhat.rhn.frontend.dto.SystemCurrency;
import com.redhat.rhn.frontend.dto.SystemEventDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.frontend.events.SsmDeleteServersEvent;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidActionTypeException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelListException;
import com.redhat.rhn.frontend.xmlrpc.InvalidEntitlementException;
import com.redhat.rhn.frontend.xmlrpc.InvalidPackageArchException;
import com.redhat.rhn.frontend.xmlrpc.InvalidPackageException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidProfileLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidSystemException;
import com.redhat.rhn.frontend.xmlrpc.MethodInvalidParamException;
import com.redhat.rhn.frontend.xmlrpc.ModulesNotAllowedException;
import com.redhat.rhn.frontend.xmlrpc.NoActionInScheduleException;
import com.redhat.rhn.frontend.xmlrpc.NoPushClientException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchActionException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchCobblerSystemRecordException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchFQDNException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchNetworkInterfaceException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchPackageException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSnapshotTagException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.ProfileNameTooLongException;
import com.redhat.rhn.frontend.xmlrpc.ProfileNameTooShortException;
import com.redhat.rhn.frontend.xmlrpc.ProfileNoBaseChannelException;
import com.redhat.rhn.frontend.xmlrpc.PtfMasterFault;
import com.redhat.rhn.frontend.xmlrpc.PtfPackageFault;
import com.redhat.rhn.frontend.xmlrpc.RetractedPackageFault;
import com.redhat.rhn.frontend.xmlrpc.RhnXmlRpcServer;
import com.redhat.rhn.frontend.xmlrpc.SnapshotTagAlreadyExistsException;
import com.redhat.rhn.frontend.xmlrpc.SystemIdInstantiationException;
import com.redhat.rhn.frontend.xmlrpc.SystemsExistFaultException;
import com.redhat.rhn.frontend.xmlrpc.SystemsNotDeletedException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.UndefinedCustomFieldsException;
import com.redhat.rhn.frontend.xmlrpc.UnrecognizedCountryException;
import com.redhat.rhn.frontend.xmlrpc.UnsupportedOperationException;
import com.redhat.rhn.frontend.xmlrpc.XmlRpcLoggingInvocationProcessor;
import com.redhat.rhn.frontend.xmlrpc.kickstart.XmlRpcKickstartHelper;
import com.redhat.rhn.frontend.xmlrpc.user.XmlRpcUserHelper;
import com.redhat.rhn.manager.MissingCapabilityException;
import com.redhat.rhn.manager.MissingEntitlementException;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.distupgrade.DistUpgradeException;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.kickstart.KickstartFormatter;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.kickstart.ProvisionVirtualInstanceCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerUnregisteredSystemCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerUnregisteredSystemCreateCommand.CobblerNetworkInterface;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.profile.ProfileManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;
import com.redhat.rhn.manager.system.DuplicateSystemGrouping;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.SystemsExistException;
import com.redhat.rhn.manager.system.UpdateBaseChannelCommand;
import com.redhat.rhn.manager.system.UpdateChildChannelsCommand;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.api.ApiIgnore;
import com.suse.manager.api.ReadOnly;
import com.suse.manager.virtualization.VirtualizationActionHelper;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestSetterActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsBaseActionJson;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.xmlrpc.NoSuchHistoryEventException;
import com.suse.manager.xmlrpc.dto.SystemEventDetailsDto;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.SystemRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SystemHandler
 * @apidoc.namespace system
 * @apidoc.doc Provides methods to access and modify registered system.
 */
public class SystemHandler extends BaseHandler {

    private static Logger log = LogManager.getLogger(SystemHandler.class);
    private final TaskomaticApi taskomaticApi;
    private final XmlRpcSystemHelper xmlRpcSystemHelper;

    private SystemEntitlementManager systemEntitlementManager;
    private SystemManager systemManager;
    private final ServerGroupManager serverGroupManager;

    /**
     * Instantiates a new system handler.
     *
     * @param taskomaticApiIn the taskomatic api
     * @param xmlRpcSystemHelperIn the xml rpc system helper
     * @param systemEntitlementManagerIn the system entitlement manager
     * @param systemManagerIn the system manager
     * @param serverGroupManagerIn
     */
    public SystemHandler(TaskomaticApi taskomaticApiIn, XmlRpcSystemHelper xmlRpcSystemHelperIn,
            SystemEntitlementManager systemEntitlementManagerIn,
            SystemManager systemManagerIn, ServerGroupManager serverGroupManagerIn) {
        this.taskomaticApi = taskomaticApiIn;
        this.xmlRpcSystemHelper = xmlRpcSystemHelperIn;
        this.systemEntitlementManager = systemEntitlementManagerIn;
        this.systemManager = systemManagerIn;
        this.serverGroupManager = serverGroupManagerIn;
    }

    /**
     * Get a reactivation key for this server.
     *
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @return Returns the reactivation key string for the given server
     * @throws FaultException A FaultException is thrown if:
     *   - The server corresponding to the sid cannot be found
     *   - The server doesn't have the "agent smith" feature
     *
     * @apidoc.doc Obtains a reactivation key for this server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #param("string", "key")
     */
    public String obtainReactivationKey(User loggedInUser, Integer sid)
            throws FaultException {
        //Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        return getReactivationKey(loggedInUser, server);
    }

    private String getReactivationKey(User loggedInUser, Server server) {
        //check for agent smith feature...
        if (!SystemManager.serverHasFeature(server.getId(), "ftr_agent_smith")) {
            throw new PermissionCheckFailureException();
        }

        // if there are any existing reactivation keys, remove them before
        // creating a new one... there should only be 1; however, earlier
        // versions of the API did not remove the existing reactivation keys;
        // therefore, it is possible that multiple will be returned...
        ActivationKeyFactory.removeKeysForServer(server.getId());

        String note = "Reactivation key for " + server.getName() + ".";
        ActivationKey key = ActivationKeyManager.getInstance().
                createNewReActivationKey(loggedInUser, server, note);

        key.setUsageLimit(1L);

        // Return the "key" for this activation key :-/
        return key.getKey();
    }

    /**
     * Get a reactivation key for this server.
     *
     * @param clientCert  client certificate of the system.
     * @return Returns the reactivation key string for the given server
     * @throws FaultException A FaultException is thrown if:
     *   - The server corresponding to the sid cannot be found
     *   - The server doesn't have the "agent smith" feature
     * @throws MethodInvalidParamException thrown if certificate is invalid.
     * @since 10.10
     * @apidoc.doc Obtains a reactivation key for this server.
     * @apidoc.param #param_desc("string", "clientCert", "client certificate of the system")
     * @apidoc.returntype #param("string", "key")
     */
    public String obtainReactivationKey(String clientCert)
            throws FaultException, MethodInvalidParamException {
        Server server = validateClientCertificate(clientCert);
        return getReactivationKey(server.getOrg().getActiveOrgAdmins().get(0), server);
    }

    /**
     * Adds an entitlement to a given server.
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @param entitlementLevel The entitlement to add to the server
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The server corresponding to the sid cannot be found
     *   - The logged in user cannot access the system
     *   - The entitlement cannot be found
     *   - The server cannot be entitled with the given entitlement
     *
     * @apidoc.doc Adds an entitlement to a given server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "entitlementLevel", "One of:
     *          'enterprise_entitled' or 'virtualization_host'.")
     * @apidoc.returntype #return_int_success()
     */
    public int upgradeEntitlement(User loggedInUser, Integer sid, String entitlementLevel)
            throws FaultException {
        //Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        final Entitlement entitlement = EntitlementManager.getByName(entitlementLevel);

        // Make sure we got a valid entitlement and the server can be entitled to it
        validateEntitlements(Arrays.asList(entitlement));
        if (!this.systemEntitlementManager.canEntitleServer(server, entitlement)) {
            throw new PermissionCheckFailureException();
        }

        this.systemEntitlementManager.addEntitlementToServer(server, entitlement);
        SystemManager.snapshotServer(server, LocalizationService.getInstance()
                .getMessage("snapshots.entitlements"));

        return 1;
    }

    /**
     * Subscribe the given server to the child channels provided.  This
     * method will unsubscribe the server from any child channels that the server
     * is currently subscribed to, but that are not included in the list.  The user may
     * provide either a list of channel ids (int) or a list of channel labels (string) as
     * input.
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @param channelIdsOrLabels The list of channel ids or labels this server should
     * be subscribed to.
     * @return Returns 1 if successful, exception otherwise.
     * @throws FaultException A FaultException is thrown if:
     *   - the server corresponding to sid cannot be found.
     *   - the channel corresponding to cid is not a valid child channel.
     *   - the user doesn't have subscribe access to any one of the current or
     *     new child channels.
     * @deprecated being replaced by system.scheduleChangeChannels(string sessionKey,
     * int serverId, String baseChannelLabel, array_single channelLabels, date earliestOccurrence).
     * This method will schedule an action for changing the child channels immediately.
     *
     * @apidoc.doc Subscribe the given server to the child channels provided.  This
     * method will unsubscribe the server from any child channels that the server
     * is currently subscribed to, but that are not included in the list.  The user may
     * provide either a list of channel ids (int) or a list of channel labels (string) as
     * input. Changes to channel assignments on salt managed systems will take effect
     * at next highstate application.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sidd")
     * @apidoc.param #array_single_desc("int (deprecated) or string", "channelIdsOrLabels", "channelId (deprecated)
     * or channelLabel")
     * @apidoc.returntype #return_int_success()
     */
    @Deprecated
    public int setChildChannels(User loggedInUser, Integer sid,
            List channelIdsOrLabels)
                    throws FaultException {

        //Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        // Determine if user passed in a list of channel ids or labels... note: the list
        // must contain all ids or labels (i.e. not a combination of both)
        boolean receivedLabels = false;
        if (!channelIdsOrLabels.isEmpty()) {
            if (channelIdsOrLabels.get(0) instanceof String) {
                receivedLabels = true;
            }

            // check to make sure that the objects are all the same type
            for (Object object : channelIdsOrLabels) {
                if (receivedLabels) {
                    if (!(object instanceof String)) {
                        throw new InvalidChannelListException();
                    }
                }
                else {
                    if (!(object instanceof Integer)) {
                        throw new InvalidChannelListException();
                    }
                }
            }
        }

        List<Long> channelIds = new ArrayList<>();
        if (receivedLabels) {
            channelIds = ChannelFactory.getChannelIds(channelIdsOrLabels);

            // if we weren't able to retrieve channel ids for all labels provided,
            // one or more of the labels must be invalid...
            if (channelIds.size() != channelIdsOrLabels.size()) {
                throw new InvalidChannelLabelException();
            }
        }
        else {
            // unfortunately, the interface only allows Integer input (not Long);
            // therefore, convert the input to Long, since channel ids are
            // internally represented as Long
            for (Object channelId : channelIdsOrLabels) {
                channelIds.add(Long.valueOf((Integer) channelId));
            }
        }

        UpdateChildChannelsCommand cmd = new UpdateChildChannelsCommand(loggedInUser,
                server, channelIds);
        cmd.setScheduleApplyChannelsState(true);
        cmd.store();

        SystemManager.snapshotServer(server, LocalizationService
                .getInstance().getMessage("snapshots.childchannel"));

        return 1;
    }

    /**
     * Sets the base channel for the given server to the given channel
     * @param loggedInUser The current user
     * @param sid The id for the server
     * @param cid The id for the channel
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - the server corresponding to sid cannot be found.
     *   - the channel corresponding to cid is not a base channel.
     *   - the user doesn't have subscribe access to either the current or
     *     the new base channel.
     * @deprecated being replaced by system.setBaseChannel(string sessionKey,
     * int serverId, string channelLabel)
     *
     * @apidoc.doc Assigns the server to a new baseChannel.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("int", "cid", "channel ID")
     * @apidoc.returntype #return_int_success()
     */
    @Deprecated
    public int setBaseChannel(User loggedInUser, Integer sid, Integer cid)
            throws FaultException {
        //Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);
        UpdateBaseChannelCommand cmd =
                new UpdateBaseChannelCommand(
                        loggedInUser, server, cid.longValue());
        cmd.setScheduleApplyChannelsState(true);
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new InvalidChannelException(
                    LocalizationService.getInstance()
                    .getMessage(ve.getKey(), ve.getValues()));
        }
        return 1;
    }

    /**
     * Sets the base channel for the given server to the given channel
     * @param loggedInUser The current user
     * @param sid The id for the server
     * @param channelLabel The id for the channel
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - the server corresponding to sid cannot be found.
     *   - the channel corresponding to cid is not a base channel.
     *   - the user doesn't have subscribe access to either the current or
     *     the new base channel.
     * @deprecated being replaced by system.scheduleChangeChannels(string sessionKey,
     * int serverId, String baseChannelLabel, array_single channelLabels, date earliestOccurrence).
     *
     *
     * @apidoc.doc Assigns the server to a new base channel.  If the user provides an empty
     * string for the channelLabel, the current base channel and all child channels will
     * be removed from the system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "channelLabel")
     * @apidoc.returntype #return_int_success()
     */
    @Deprecated
    public int setBaseChannel(User loggedInUser, Integer sid, String channelLabel)
            throws FaultException {
        Server server = lookupServer(loggedInUser, sid);

        UpdateBaseChannelCommand cmd = null;
        if (StringUtils.isEmpty(channelLabel)) {
            // if user provides an empty string for the channel label, they are requesting
            // to remove the base channel
            cmd = new UpdateBaseChannelCommand(loggedInUser, server, -1L);
        }
        else {
            List<String> channelLabels = new ArrayList<>();
            channelLabels.add(channelLabel);

            List<Long> channelIds = ChannelFactory.getChannelIds(channelLabels);

            if (!channelIds.isEmpty()) {
                cmd = new UpdateBaseChannelCommand(loggedInUser, server, channelIds.get(0));
                cmd.setScheduleApplyChannelsState(true);
            }
            else {
                throw new InvalidChannelLabelException();
            }
        }
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new InvalidChannelException(LocalizationService.getInstance()
                    .getMessage(ve.getKey(), ve.getValues()));
        }
        SystemManager.snapshotServer(server, LocalizationService
                .getInstance().getMessage("snapshots.basechannel"));
        return 1;
    }

    /**
     * Schedule an action to change the channels of the given system. Works for both traditional
     * and Salt systems.
     * To remove the base channel provide an empty string must be provided.
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param baseChannelLabel The label of the base channel to subscribe to
     * @param childLabels The list of child channel labels to subscribe to
     * @param earliestOccurrence Earliest occurrence of the errata update
     * be subscribed to.
     * @return an action id, exception thrown otherwise
     * @since 19.0
     *
     * @apidoc.doc Schedule an action to change the channels of the given system. Works for both traditional
     * and Salt systems.
     * This method accepts labels for the base and child channels.
     * If the user provides an empty string for the channelLabel, the current base channel and
     * all child channels will be removed from the system.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "childLabels")
     * @apidoc.param  #param_desc($date, "earliestOccurrence", "the time/date to schedule the action")
     * @apidoc.returntype #param_desc("int", "id", "ID of the action scheduled, otherwise exception thrown
     * on error")
     */
    public long scheduleChangeChannels(User loggedInUser, Integer sid, String baseChannelLabel,
                                       List childLabels, Date earliestOccurrence) {
        return scheduleChangeChannels(loggedInUser, singletonList(sid), baseChannelLabel, childLabels,
                earliestOccurrence).stream().findFirst().orElseThrow(NoActionInScheduleException::new);
    }

    /**
     * Schedule an action to change the channels of a list of clients. Works for both traditional
     * and Salt clients.
     * To remove the base channel provide an empty string must be provided.
     * @param loggedInUser The current user
     * @param sids list of IDs of the servers
     * @param baseChannelLabel The label of the base channel to subscribe to
     * @param childLabels The list of child channel labels to subscribe to
     * @param earliestOccurrence Earliest occurrence of the errata update
     * be subscribed to.
     * @return an action id, exception thrown otherwise
     * @since 19.0
     *
     * @apidoc.doc Schedule an action to change the channels of the given system. Works for both traditional
     * and Salt systems.
     * This method accepts labels for the base and child channels.
     * If the user provides an empty string for the channelLabel, the current base channel and
     * all child channels will be removed from the system.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "childLabels")
     * @apidoc.param  #param_desc($date, "earliestOccurrence", "the time/date to schedule the action")
     * @apidoc.returntype #array_single("long", "actionIds")
     */
    public List<Long> scheduleChangeChannels(User loggedInUser, List<Integer> sids, String baseChannelLabel,
                                             List childLabels, Date earliestOccurrence) {
        //Get the logged in user and server
        Set<Long> servers = sids.stream()
                .map(sid -> lookupServer(loggedInUser, sid))
                .map(Server::getId)
                .collect(toSet());
        Optional<Channel> baseChannel = Optional.empty();

        // base channel
        if (StringUtils.isNotEmpty(baseChannelLabel)) {
            List<Long> channelIds = ChannelFactory.getChannelIds(singletonList(baseChannelLabel));
            long baseChannelId = channelIds.stream().findFirst().orElseThrow(InvalidChannelLabelException::new);
            baseChannel = Optional.of(ChannelManager.lookupByIdAndUser(baseChannelId, loggedInUser));
        }
        // else if the user provides an empty string for the channel label, they are requesting
        // to remove the base channel

        // check if user passed a list of labels for the child channels
        if (childLabels.stream().anyMatch(e -> !(e instanceof String))) {
            throw new InvalidChannelListException();
        }

        List<Long> channelIds = !childLabels.isEmpty() ?
                ChannelFactory.getChannelIds(childLabels) : Collections.emptyList();

        // if we weren't able to retrieve channel ids for all labels provided,
        // one or more of the labels must be invalid...
        if (channelIds.size() != childLabels.size()) {
            throw new InvalidChannelLabelException();
        }

        List<Channel> childChannels = channelIds.stream()
                .map(cid -> ChannelFactory.lookupByIdAndUser(cid, loggedInUser))
                .collect(Collectors.toList());

        try {
            Set<Action> action = ActionChainManager.scheduleSubscribeChannelsAction(loggedInUser,
                    servers,
                    baseChannel,
                    childChannels,
                    earliestOccurrence, null);
            return action.stream().map(Action::getId).collect(toList());
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Gets a list of base channels subscribable by the logged in user for the server with
     * the given id.
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @return Returns an array of maps representing the base channels the logged in user
     * can subscribe this system to.
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns a list of subscribable base channels.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *  #return_array_begin()
     *      #struct_begin("channel")
     *          #prop_desc("int" "id" "Base Channel ID.")
     *          #prop_desc("string" "name" "Name of channel.")
     *          #prop_desc("string" "label" "Label of Channel")
     *          #prop_desc("int", "current_base", "1 indicates it is the current base
     *                                      channel")
     *      #struct_end()
     *  #array_end()
     *
     */
    @ReadOnly
    public Object[] listSubscribableBaseChannels(User loggedInUser, Integer sid)
            throws FaultException {

        //Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);
        Channel baseChannel = server.getBaseChannel();
        List<Map<String, Object>> returnList = new ArrayList<>();

        List<EssentialChannelDto> list =
                ChannelManager.listBaseChannelsForSystem(loggedInUser, server);
        for (EssentialChannelDto ch : list) {
            Boolean currentBase = (baseChannel != null) &&
                    baseChannel.getId().equals(ch.getId());
            returnList.add(createChannelMap(ch, currentBase));
        }

        return returnList.toArray();
    }

    /**
     * Gets a list of all systems visible to user
     * @param loggedInUser The current user
     * @return Returns an array of maps representing all systems visible to user
     *
     * @throws FaultException A FaultException is thrown if a valid user can not be found
     * from the passed in session key
     *
     * @apidoc.doc Returns a list of all servers visible to the user.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ShortSystemInfoSerializer
     *      #array_end()
     */
    @ReadOnly
    public Object[] listSystems(User loggedInUser) throws FaultException {
        DataResult<ShortSystemInfo> dr = SystemManager.systemListShort(loggedInUser, null);
        dr.elaborate();
        return dr.toArray();
    }

    /**
     * Returns a list of empty system profiles visible to user (created by createSystemProfile).
     *
     * @param loggedInUser - the user
     * @return array of empty system profiles
     *
     * @apidoc.doc Returns a list of empty system profiles visible to user (created by the createSystemProfile method).
     * @apidoc.param #session_key()
     *
     * @apidoc.returntype
     *          #return_array_begin()
     *              $EmptySystemProfileSerializer
     *          #array_end()
     */
    @ReadOnly
    public Object[] listEmptySystemProfiles(User loggedInUser) {
        return SystemManager.listEmptySystemProfiles(loggedInUser, null).toArray();
    }

    /**
     * Gets a list of all active systems visible to user
     * @param loggedInUser The current user
     * @return Returns an array of maps representing all active systems visible to user
     *
     * @throws FaultException A FaultException is thrown if a valid user can not be found
     * from the passed in session key
     *
     * @apidoc.doc Returns a list of active servers visible to the user.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ShortSystemInfoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ShortSystemInfo> listActiveSystems(User loggedInUser)
            throws FaultException {
        return SystemManager.systemListShortActive(loggedInUser, null);
    }

    private Date convertLocalToUtc(Date in) {
        Calendar c = Calendar.getInstance();
        c.setTime(in);
        TimeZone z = c.getTimeZone();
        int offset = z.getRawOffset();
        if (z.inDaylightTime(in)) {
            offset += z.getDSTSavings();
        }
        int offsetHrs = offset / 1000 / 60 / 60;
        int offsetMins = offset / 1000 / 60 % 60;
        c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
        c.add(Calendar.MINUTE, (-offsetMins));
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Given a list of server ids, will return details about the
     * systems that are active and visible to the user
     * @param loggedInUser The current user
     * @param sids A list of ids to get info for
     * @return a list of maps representing the details for the active systems
     *
     * @throws FaultException A FaultException is thrown if the user cannot
     * be found from the session key
     *
     * @apidoc.doc Given a list of server ids, returns a list of active servers'
     * details visible to the user.
     * @apidoc.param #session_key()
     * @apidoc.param  #array_single("int", "sids")
     * @apidoc.returntype
     *   #return_array_begin()
     *     #struct_begin("server details")
     *       #prop_desc("int", "id", "The server's id")
     *       #prop_desc("string", "name", "The server's name")
     *       #prop_desc("string", "minion_id", "The server's minion id, in case it is a salt minion client")
     *       #prop_desc("$date", "last_checkin",
     *         "Last time server successfully checked in (in UTC)")
     *       #prop_desc("int", "ram", "The amount of physical memory in MB.")
     *       #prop_desc("int", "swap", "The amount of swap space in MB.")
     *       #prop_desc("struct", "network_devices", "The server's network devices")
     *       $NetworkInterfaceSerializer
     *       #prop_desc("struct", "dmi_info", "The server's dmi info")
     *       $DmiSerializer
     *       #prop_desc("struct", "cpu_info", "The server's cpu info")
     *       $CpuSerializer
     *       #prop_desc("array", "subscribed_channels", "List of subscribed channels")
     *         #return_array_begin()
     *           #struct_begin("channel")
     *             #prop_desc("int", "channel_id", "The channel id.")
     *             #prop_desc("string", "channel_label", "The channel label.")
     *           #struct_end()
     *         #array_end()
     *       #prop_desc("array", "active_guest_system_ids",
     *           "List of virtual guest system ids for active guests")
     *         #return_array_begin()
     *           #prop_desc("int", "guest_id", "The guest's system id.")
     *         #array_end()
     *     #struct_end()
     *   #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listActiveSystemsDetails(
            User loggedInUser, List<Integer> sids) throws FaultException {
        List<Server> servers = xmlRpcSystemHelper.lookupServers(
                loggedInUser, sids);
        List<Map<String, Object>> ret = new ArrayList<>();
        for (Server server : servers) {
            if (!server.isInactive()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", server.getId());
                m.put("name", server.getName());
                m.put("minion_id", server.getMinionId());
                m.put("last_checkin", convertLocalToUtc(server.getLastCheckin()));

                m.put("ram", server.getRam());
                m.put("swap", server.getSwap());

                CPU cpu = server.getCpu();
                m.put("cpu_info", Objects.requireNonNullElseGet(cpu, HashMap::new));

                Dmi dmi = server.getDmi();
                m.put("dmi_info", Objects.requireNonNullElseGet(dmi, HashMap::new));

                m.put("network_devices",
                        new ArrayList<>(server
                                .getNetworkInterfaces()));

                List<Map<String, Object>> channels = new ArrayList<>();
                Channel base = server.getBaseChannel();
                if (base != null) {
                    Map<String, Object> basec = new HashMap<>();
                    basec.put("channel_id", base.getId());
                    basec.put("channel_label", base.getLabel());
                    channels.add(basec);
                    for (Channel child : server.getChildChannels()) {
                        Map<String, Object> childc = new HashMap<>();
                        childc.put("channel_id", child.getId());
                        childc.put("channel_label", child.getLabel());
                        channels.add(childc);
                    }
                }
                m.put("subscribed_channels", channels);

                Collection<VirtualInstance> guests = server.getGuests();
                List<Long> guestList = new ArrayList<>();
                for (VirtualInstance guest : guests) {
                    Server g = guest.getGuestSystem();
                    if (g != null && !g.isInactive()) {
                        guestList.add(g.getId());
                    }
                }

                ret.add(m);
            }
        }
        return ret;
    }

    private Map<String, Object> createChannelMap(EssentialChannelDto channel,
            Boolean currentBase) {
        Map<String, Object> ret = new HashMap<>();

        ret.put("id", channel.getId());
        ret.put("name", channel.getName());
        ret.put("label", channel.getLabel());
        ret.put("current_base", currentBase ? Integer.valueOf(1) : Integer.valueOf(0));
        return ret;
    }

    /**
     * List the child channels that this system can subscribe to.
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the channels this server could
     * subscribe too.
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns a list of subscribable child channels.  This only shows channels
     * the system is *not* currently subscribed to.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("child channel")
     *              #prop("int", "id")
     *              #prop("string", "name")
     *              #prop("string", "label")
     *              #prop("string", "summary")
     *              #prop("string", "has_license")
     *              #prop("string", "gpg_key_url")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public Object[] listSubscribableChildChannels(User loggedInUser, Integer sid)
            throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);
        Channel baseChannel = server.getBaseChannel();
        List<Map<String, Object>> returnList = new ArrayList<>();

        //make sure channel is not null
        if (baseChannel == null) {
            //return empty array since we can't have any child channels without a base
            return returnList.toArray();
        }

        DataResult<Map<String, Object>> dr =
                SystemManager.subscribableChannels(server.getId(),
                loggedInUser.getId(), baseChannel.getId());

        //TODO: This should go away once we teach marquee how to deal with nulls in a list.
        //      Luckily, this list shouldn't be too long.
        for (Map<String, Object> row : dr) {
            Map<String, Object> channel = new HashMap<>();

            channel.put("id", row.get("id"));
            channel.put("label", row.get("label"));
            channel.put("name", row.get("name"));
            channel.put("summary", row.get("summary"));
            channel.put("has_license", "");
            channel.put("gpg_key_url", StringUtils.defaultString(
                    (String) row.get("gpg_key_url")));

            returnList.add(channel);
        }

        return returnList.toArray();
    }

    /**
     * Given a package name + version + release + epoch, returns the list of
     * packages installed on the system w/ the same name that are older.
     * @param loggedInUser The current user
     * @param sid The id of the system you're checking
     * @param name The name of the package you're checking
     * @param version The version of the package
     * @param release The release of the package
     * @param epoch The epoch of the package
     * @return Returns a list of packages installed on the system with the same
     * name that are older.
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found or if no package with the given name is found.
     *
     * @apidoc.doc Given a package name, version, release, and epoch, returns
     * the list of packages installed on the system with the same name that are
     * older.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "name", "Package name.")
     * @apidoc.param #param_desc("string", "version", "Package version.")
     * @apidoc.param #param_desc("string", "release", "Package release.")
     * @apidoc.param #param_desc("string", "epoch",  "Package epoch.")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("package")
     *              #prop("string", "name")
     *              #prop("string", "version")
     *              #prop("string", "release")
     *              #prop("string", "epoch")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public Object[] listOlderInstalledPackages(User loggedInUser, Integer sid,
            String name, String version, String release, String epoch)
                    throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        PackageEvr inputEvr = new PackageEvr(
                epoch == null ? "0" : epoch,
                version == null ? "" : version,
                release == null ? "" : release,
                server.getPackageType()
        );

        List toCheck = packagesToCheck(server, name);

        List returnList = new ArrayList<>();
        /*
         * Loop through the packages to check and compare the evr parts to what was
         * passed in from the user. If the package is older, add it to returnList.
         */
        for (Object oIn : toCheck) {
            Map pkg = (Map) oIn;

            String pkgName = (String) pkg.get("name");
            String pkgVersion = (String) pkg.get("version");
            String pkgRelease = (String) pkg.get("release");
            String pkgEpoch = (String) pkg.get("epoch");

            PackageEvr pkgEvr = new PackageEvr(
                    pkgEpoch == null ? "0" : pkgEpoch,
                    pkgVersion == null ? "" : pkgVersion,
                    pkgRelease == null ? "" : pkgRelease,
                    server.getPackageType()
            );

            if (0 > pkgEvr.compareTo(inputEvr)) {
                returnList.add(fillOutPackage(pkgName, pkgVersion, pkgRelease, pkgEpoch));
            }
        }

        return returnList.toArray();
    }

    /**
     * Given a package name + version + release + epoch, returns the list of
     * packages installed on the system w/ the same name that are newer.
     * @param loggedInUser The current user
     * @param sid The id of the system you're checking
     * @param name The name of the package you're checking
     * @param version The version of the package
     * @param release The release of the package
     * @param epoch The epoch of the package
     * @return Returns a list of packages installed onNAME the system with the same
     * name that are newer.
     * @throws FaultException A FaultException is thrown if the server
     * corresponding to sid cannot be found or if no package with the given name
     * is found.
     *
     * @apidoc.doc Given a package name, version, release, and epoch, returns the
     * list of packages installed on the system w/ the same name that are newer.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "name", "Package name.")
     * @apidoc.param #param_desc("string", "version", "Package version.")
     * @apidoc.param #param_desc("string", "release", "Package release.")
     * @apidoc.param #param_desc("string", "epoch",  "Package epoch.")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("package")
     *              #prop("string", "name")
     *              #prop("string", "version")
     *              #prop("string", "release")
     *              #prop("string", "epoch")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public Object[] listNewerInstalledPackages(User loggedInUser, Integer sid,
            String name, String version, String release, String epoch)
                    throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        PackageEvr inputEvr = new PackageEvr(
                epoch == null ? "0" : epoch,
                version == null ? "" : version,
                release == null ? "" : release,
                server.getPackageType()
        );

        List toCheck = packagesToCheck(server, name);
        List returnList = new ArrayList<>();
        /*
         * Loop through the packages to check and compare the evr parts to what was
         * passed in from the user. If the package is newer, add it to returnList.
         */
        for (Object oIn : toCheck) {
            Map pkg = (Map) oIn;
            String pkgName = (String) pkg.get("name");
            String pkgVersion = (String) pkg.get("version");
            String pkgRelease = (String) pkg.get("release");
            String pkgEpoch = (String) pkg.get("epoch");

            PackageEvr pkgEvr = new PackageEvr(
                    pkgEpoch == null ? "0" : pkgEpoch,
                    pkgVersion == null ? "" : pkgVersion,
                    pkgRelease == null ? "" : pkgRelease,
                    server.getPackageType()
            );

            if (0 < pkgEvr.compareTo(inputEvr)) {
                returnList.add(fillOutPackage(pkgName, pkgVersion, pkgRelease, pkgEpoch));
            }
        }

        return returnList.toArray();
    }

    /**
     * Private helper method to retrieve a list of packages by package name
     * @param server The server the packages are installed on
     * @param name The name of the package
     * @return Returns a list of packages with the given name installed on the give server
     * @throws NoSuchPackageException A no such package exception is thrown when no packages
     * with the given name are installed on the server.
     */
    private List<Map<String, Object>> packagesToCheck(Server server, String name)
            throws NoSuchPackageException {
        DataResult<Map<String, Object>> installed = SystemManager.installedPackages(server.getId());

        List<Map<String, Object>> toCheck = new ArrayList<>();
        // Get a list of packages with matching name
        for (Map<String, Object> pkg : installed) {
            String pkgName = StringUtils.trim((String) pkg.get("name"));
            if (pkgName.equals(StringUtils.trim(name))) {
                toCheck.add(pkg);
            }
        }

        if (toCheck.isEmpty()) {
            throw new NoSuchPackageException();
        }

        return toCheck;
    }

    /**
     * Private helper method to fillout a map representing a package
     * @param pkgName The name of the package
     * @param pkgVersion The version of the package
     * @param pkgRelease The release of the package
     * @param pkgEpoch The epoch of the package
     * @return Returns a map representing a package
     */
    private Map<String, String> fillOutPackage(String pkgName,
            String pkgVersion, String pkgRelease,
            String pkgEpoch) {
        Map<String, String> map = new HashMap<>();
        map.put("name", StringUtils.defaultString(pkgName));
        map.put("version", StringUtils.defaultString(pkgVersion));
        map.put("release", StringUtils.defaultString(pkgRelease));
        map.put("epoch", StringUtils.defaultString(pkgEpoch));
        return map;
    }

    /**
     * Is the package with the given NVRE installed on given system
     * @param loggedInUser The current user
     * @param sid The sid for the server in question
     * @param name The name of the package
     * @param version The version of the package
     * @param release The release of the package
     * @return Returns 1 if package is installed, 0 if not.
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Check if the package with the given NVRE is installed on given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "name", "Package name.")
     * @apidoc.param #param_desc("string", "version","Package version.")
     * @apidoc.param #param_desc("string", "release", "Package release.")
     * @apidoc.returntype #param_desc("int", "status", "1 if package exists, 0 if not, exception is thrown
     * if an error occurs")
     */
    @ReadOnly
    public int isNvreInstalled(User loggedInUser, Integer sid, String name,
            String version, String release) throws FaultException {
        //Set epoch to an empty string
        return isNvreInstalled(loggedInUser, sid, name, version, release, null);
    }

    /**
     * Is the package with the given NVRE installed on given system
     * @param loggedInUser The current user
     * @param sid The sid for the server in question
     * @param name The name of the package
     * @param version The version of the package
     * @param release The release of the package
     * @param epoch The epoch of the package
     * @return Returns 1 if package is installed, 0 if not.
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Is the package with the given NVRE installed on given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "name", "Package name.")
     * @apidoc.param #param_desc("string", "version", "Package version.")
     * @apidoc.param #param_desc("string", "release", "Package release.")
     * @apidoc.param #param_desc("string", "epoch",  "Package epoch.")
     * @apidoc.returntype #param_desc("int", "status", "1 if package exists, 0 if not, exception is thrown
     * if an error occurs")
     */
    @ReadOnly
    public int isNvreInstalled(User loggedInUser, Integer sid, String name,
            String version, String release, String epoch) throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        DataResult<Map<String, Object>> packages = SystemManager.installedPackages(server.getId());

        /*
         * Loop through the packages for this system and check each attribute. Use
         * StringUtils.trim() to disregard whitespace on either ends of the string.
         */
        for (Map<String, Object> pkg : packages) {
            //Check name
            String pkgName = StringUtils.trim((String) pkg.get("name"));
            if (!pkgName.equals(StringUtils.trim(name))) {
                continue;
            }

            //Check version
            String pkgVersion = StringUtils.trim((String) pkg.get("version"));
            if (!pkgVersion.equals(StringUtils.trim(version))) {
                continue;
            }

            //Check release
            String pkgRelease = StringUtils.trim((String) pkg.get("release"));
            if (!pkgRelease.equals(StringUtils.trim(release))) {
                continue;
            }

            //Check epoch
            String pkgEpoch = StringUtils.trim((String) pkg.get("epoch"));
            // If epoch is null, we arrived here from the isNvreInstalled(...n,v,r) method;
            // therefore, just skip the comparison
            if ((epoch != null) && !pkgEpoch.equals(StringUtils.trim(epoch))) {
                continue;
            }

            // If we get here, NVRE matches so return true
            return 1;
        }

        //package not installed
        return 0;
    }

    /**
     * Get the list of latest upgradable packages for a given system
     * @param loggedInUser The current user
     * @param sid The id for the system in question
     * @return Returns an array of maps representing the latest upgradable packages
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Get the list of latest upgradable packages for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     * #return_array_begin()
     *      #struct_begin("package")
     *          #prop("string", "name")
     *          #prop("string", "arch")
     *          #prop("string", "from_version")
     *          #prop("string", "from_release")
     *          #prop("string", "from_epoch")
     *          #prop("string", "to_version")
     *          #prop("string", "to_release")
     *          #prop("string", "to_epoch")
     *          #prop("string", "to_package_id")
     *      #struct_end()
     * #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listLatestUpgradablePackages(User loggedInUser,
            Integer sid) throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        return SystemManager.latestUpgradablePackages(server.getId());
    }

    /**
     * Get the list of all installable packages for a given system.
     * @param loggedInUser The current user
     * @param sid The id for the system in question
     * @return Returns an array of maps representing the latest installable packages
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Get the list of all installable packages for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     * #return_array_begin()
     *      #struct_begin("package")
     *          #prop("string", "name")
     *          #prop("string", "version")
     *          #prop("string", "release")
     *          #prop("string", "epoch")
     *          #prop("int", "id")
     *          #prop("string", "arch_label")
     *      #struct_end()
     * #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listAllInstallablePackages(User loggedInUser,
            Integer sid) throws FaultException {
        Server server = lookupServer(loggedInUser, sid);
        return SystemManager.allInstallablePackages(server.getId());
    }

    /**
     * Get the list of latest installable packages for a given system.
     * @param loggedInUser The current user
     * @param sid The id for the system in question
     * @return Returns an array of maps representing the latest installable packages
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Get the list of latest installable packages for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     * #return_array_begin()
     *      #struct_begin("package")
     *          #prop("string", "name")
     *          #prop("string", "version")
     *          #prop("string", "release")
     *          #prop("string", "epoch")
     *          #prop("int", "id")
     *          #prop("string", "arch_label")
     *      #struct_end()
     * #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listLatestInstallablePackages(User loggedInUser,
            Integer sid) throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        return SystemManager.latestInstallablePackages(server.getId());
    }

    /**
     * Get the latest available version of a package for each system
     * @param loggedInUser The current user
     * @param sids The IDs of the systems in question
     * @param packageName the package name
     * @return Returns an a map with the latest available package for each system
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Get the latest available version of a package for each system
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param("string", "packageName")
     * @apidoc.returntype
     *     #return_array_begin()
     *         #struct_begin("system")
     *             #prop_desc("int", "id", "server ID")
     *             #prop_desc("string", "name", "server name")
     *             #prop_desc("struct", "package", "package structure")
     *                 #struct_begin("package")
     *                     #prop("int", "id")
     *                     #prop("string", "name")
     *                     #prop("string", "version")
     *                     #prop("string", "release")
     *                     #prop("string", "epoch")
     *                     #prop("string", "arch")
     *                #struct_end()
     *        #struct_end()
     *    #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listLatestAvailablePackage(User loggedInUser,
            List<Integer> sids, String packageName) throws FaultException {

        List<Map<String, Object>> list = new ArrayList<>();

        for (Integer sid : sids) {
            Server server = lookupServer(loggedInUser, sid);

            Map<String, Object> systemMap = new HashMap<>();

            // get the package name ID
            Map<String, Long> pkgEvr = PackageManager.lookupEvrIdByPackageName(sid.longValue(), packageName);

            if (pkgEvr != null) {
                // find the latest package available to each system
                Package pkg = PackageManager.guestimatePackageBySystem(sid.longValue(),
                        pkgEvr.get("name_id"), pkgEvr.get("evr_id"),
                        null, loggedInUser.getOrg());

                // build the hash to return
                if (pkg != null && !pkg.isPartOfRetractedPatch()) {
                    Map<String, Object> pkgMap = new HashMap<>();
                    pkgMap.put("id", pkg.getId());
                    pkgMap.put("name", pkg.getPackageName().getName());
                    pkgMap.put("version", pkg.getPackageEvr().getVersion());
                    pkgMap.put("release", pkg.getPackageEvr().getRelease());
                    pkgMap.put("arch", pkg.getPackageArch().getLabel());

                    if (pkg.getPackageEvr().getEpoch() != null) {
                        pkgMap.put("epoch", pkg.getPackageEvr().getEpoch());
                    }
                    else {
                        pkgMap.put("epoch", "");
                    }

                    systemMap.put("id", sid);
                    systemMap.put("name", server.getName());
                    systemMap.put("package", pkgMap);

                    list.add(systemMap);
                }
            }
        }

        return list;
    }

    /**
     * Gets the entitlements for a given server.
     * @param loggedInUser The current user
     * @param sid The id for the system in question
     * @return Returns an array of entitlement labels for the system
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Gets the entitlements for a given server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #array_single("string", "entitlement_label")
     */
    @ReadOnly
    public Object[] getEntitlements(User loggedInUser, Integer sid) throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        // A list of entitlements to return
        List<String> entitlements = new ArrayList<>();

        // Loop through the entitlement objects for this server and stick
        // label into the entitlements list to return
        for (Entitlement entitlement : server.getEntitlements()) {
            entitlements.add(entitlement.getLabel());
        }

        return entitlements.toArray();
    }

    /**
     * Get the system_id file for a given server
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns the system_id file for the server
     * @throws FaultException A FaultException is thrown if the server
     * corresponding to sid cannot be found or if the system_id file cannot
     * be generated.
     *
     * @apidoc.doc Get the system ID file for a given server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #param("string", "id")
     */
    public String downloadSystemId(User loggedInUser, Integer sid) throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        // Try to generate the cert.
        try {
            ClientCertificate cert = SystemManager.createClientCertificate(server);
            return cert.asXml();
        }
        catch (InstantiationException e) {
            // Convert to fault exception
            throw new SystemIdInstantiationException();
        }
    }

    /**
     * List the installed packages for a given system.
     *
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the packages installed on a system
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     * @deprecated This is here for backwards compatibility: The method returns architecture name,
     * whereas the other endpoints return/accept architecture label.
     * Instead of this method, use listInstalledPackages preferably.
     *
     * @apidoc.doc List the installed packages for a given system. Usage of listInstalledPackages is preferred,
     * as it returns architecture label (not name).
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("package")
     *                 #prop("string", "name")
     *                 #prop("string", "version")
     *                 #prop("string", "release")
     *                 #prop("string", "epoch")
     *                 #prop_desc("string", "arch", "Architecture name")
     *                 #prop_desc("date", "installtime", "returned only if known")
     *          #struct_end()
     *      #array_end()
     */
    @Deprecated
    @ReadOnly
    public List<Map<String, Object>> listPackages(User loggedInUser, Integer sid)
            throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        return SystemManager.installedPackages(server.getId());
    }

    /**
     * List the installed packages for a given system.
     *
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the packages installed on a system
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc List the installed packages for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("package")
     *                 #prop_desc("int", "package_id", "PackageID, -1 if package is installed but not available in
     *                 subscribed channels")
     *                 #prop("string", "name")
     *                 #prop("string", "epoch")
     *                 #prop("string", "version")
     *                 #prop("string", "release")
     *                 #prop_desc("string", "arch", "architecture label")
     *                 #prop_desc("date", "installtime", "returned only if known")
     *                 #prop("boolean", "retracted")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listInstalledPackages(User loggedInUser, Integer sid)
            throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);
        DataResult<PackageListItem> packageListItems = PackageManager.systemPackageList(server.getId(), null);
        packageListItems.elaborate();
        List<Map<String, Object>> maps = packageListItems.stream().map(pi -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("package_id", Objects.isNull(pi.getPackageId()) ? -1 : pi.getPackageId().intValue());
            item.put("name", pi.getName());
            item.put("epoch", Optional.ofNullable(pi.getEpoch()).orElse(" "));
            item.put("version", pi.getVersion());
            item.put("release", pi.getRelease());
            item.put("arch", Optional.ofNullable(pi.getArch())
                    .orElseGet(() -> LocalizationService.getInstance().getMessage("Unknown")));
            Optional.ofNullable(pi.getInstallTimeObj()).ifPresent(it -> item.put("installtime", it));
            item.put("retracted", pi.isRetracted());
            return item;
        }).collect(toList());
        return maps;
    }

    /**
     * List current package locks status.
     * @param loggedInUser The current user
     * @param sid System ID
     * @return Returns an array of maps representing the packages locked on a system


     * @apidoc.doc List current package locks status.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("package")
     *                 #prop_desc("int", "package_id", "PackageID, -1 if package is locked but not available in
     *                 subscribed channels")
     *                 #prop("string", "name")
     *                 #prop("string", "epoch")
     *                 #prop("string", "version")
     *                 #prop("string", "release")
     *                 #prop_desc("string", "arch", "architecture label")
     *                 #prop_desc("string", "pending status", "return only if there is a pending locking")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listPackagesLockStatus(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);

        DataResult<PackageListItem> lockedPackagesResult =
                PackageManager.systemLockedPackages(server.getId(), null);

        return lockedPackagesResult.stream().map(pi -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("package_id", pi.getPackageId().intValue());
            item.put("name", pi.getName());
            item.put("epoch", Optional.ofNullable(pi.getEpoch()).orElse(" "));
            item.put("version", pi.getVersion());
            item.put("release", pi.getRelease());
            item.put("arch", Optional.ofNullable(pi.getArch()).orElse(" "));
            if (pi.getPending() != null) {
                item.put("pending status", pi.getPending().equals("L") ? "Locking" : "Unlocking");
            }
            return item;
        }).collect(toList());
    }

    /**
     * Delete the specified list of guest profiles for a given host.
     * @param loggedInUser The current user
     * @param hostId The id of the host system.
     * @param guestNames List of guest names to delete.
     * @return 1 in case of success, traceback otherwise.
     *
     * @apidoc.doc Delete the specified list of guest profiles for a given host
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "hostId")
     * @apidoc.param #array_single("string", "guestNames")
     * @apidoc.returntype #return_int_success()
     */
    public Integer deleteGuestProfiles(User loggedInUser, Integer hostId,
            List<String> guestNames) {
        Server server = lookupServer(loggedInUser, hostId);

        if (server != null && !server.isVirtualHost()) {
            throw new FaultException(1005, "notAHostSystem",
                    "The system ID specified (" + hostId +
                    ") does not represent a host system");
        }

        List<String> availableGuests = new ArrayList<>();

        for (VirtualInstance vi : server.getGuests()) {
            availableGuests.add(vi.getName());
        }

        for (String gn : guestNames) {
            if (!availableGuests.contains(gn)) {
                throw new InvalidSystemException();
            }
        }

        for (VirtualInstance vi : server.getGuests()) {
            if (!guestNames.contains(vi.getName())) {
                continue;
            }

            if (vi.isRegisteredGuest()) {
                throw new SystemsNotDeletedException("Unable to delete guest profile " +
                        vi.getName() + ": the guest is registered.");
            }
            VirtualInstanceFactory.getInstance().deleteVirtualInstanceOnly(vi);
        }

        return 1;
    }

    /**
     * Delete systems given a list of system ids asynchronously without cleanup.
     * This call queues the systems for deletion
     * @param loggedInUser The current user
     * @param sids A list of systems ids to delete
     * @return Returns the number of systems deleted if successful, fault exception
     * containing ids of systems not deleted otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Delete systems given a list of system ids asynchronously.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteSystems(User loggedInUser, List<Integer> sids)
            throws FaultException {
        return deleteSystems(loggedInUser, sids, "NO_CLEANUP");
    }

    /**
     * Delete systems given a list of system ids asynchronously.
     * This call queues the systems for deletion
     * @param loggedInUser The current user
     * @param sids A list of systems ids to delete
     * @param cleanupType one of FAIL_ON_CLEANUP_ERR, NO_CLEANUP or FORCE_DELETE
     * @return Returns the number of systems deleted if successful, fault exception
     * containing ids of systems not deleted otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Delete systems given a list of system ids asynchronously.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param_desc("string", "cleanupType", "Possible values:
     *  'FAIL_ON_CLEANUP_ERR' - fail in case of cleanup error,
     *  'NO_CLEANUP' - do not cleanup, just delete,
     *  'FORCE_DELETE' - Try cleanup first but delete server anyway in case of error")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteSystems(User loggedInUser, List<Integer> sids, String cleanupType)
            throws FaultException {

        List<Integer> skippedSids = new ArrayList<>();
        List<Long> deletion = new LinkedList<>();
        // Loop through the sids and try to delete the server
        for (Integer sysId : sids) {
            if (SystemManager.isAvailableToUser(loggedInUser, sysId.longValue())) {
                deletion.add(sysId.longValue());
            }
            else {
                log.warn("system {} is not available to user, hence will not be deleted", sysId);
                skippedSids.add(sysId);
            }
        }

        // Fire the request off asynchronously
        SsmDeleteServersEvent event =
                new SsmDeleteServersEvent(loggedInUser, deletion,
                        SystemManager.ServerCleanupType.fromString(cleanupType).orElseThrow(() ->
                        new IllegalArgumentException("Invalid server cleanup type value: " + cleanupType)));
        MessageQueue.publish(event);

        // If we skipped any systems, create an error message and throw a FaultException
        if (!skippedSids.isEmpty()) {
            StringBuilder msg = new StringBuilder(
                    "The following systems were NOT deleted: ");
            for (Integer sid :  skippedSids) {
                msg.append("\n" + sid);
            }
            throw new SystemsNotDeletedException(msg.toString());
        }

        return 1;
    }


    /**
     * Delete a system given its client certificate.
     *
     * @param clientCert  client certificate of the system.
     * @return 1 on success
     * @throws FaultException A FaultException is thrown if:
     *   - The server corresponding to the sid cannot be found
     * @throws MethodInvalidParamException thrown if certificate is invalid.
     * @since 10.10
     * @apidoc.doc Delete a system given its client certificate.
     * @apidoc.param #param_desc("string", "clientCert", "client certificate of the system")
     * @apidoc.returntype #return_int_success()
     */

    public int deleteSystem(String clientCert) throws FaultException {
        Server server = validateClientCertificate(clientCert);
        systemManager.deleteServerAndCleanup(server.getOrg().getActiveOrgAdmins().get(0),
                server.getId(),
                SystemManager.ServerCleanupType.NO_CLEANUP
                );
        return 1;
    }

    /**
     * Delete a system given its server id synchronously without cleanup
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @return 1 on success
     * @throws FaultException A FaultException is thrown if:
     *   - The server corresponding to the sid cannot be found
     * @apidoc.doc Delete a system given its server id synchronously without cleanup
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteSystem(User loggedInUser, Integer sid)
            throws FaultException {
        return deleteSystem(loggedInUser, sid, "NO_CLEANUP");
    }

    /**
     * Delete a system given its server id synchronously
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @param cleanupType one of FAIL_ON_CLEANUP_ERR, NO_CLEANUP or FORCE_DELETE
     * @return 1 on success
     * @throws FaultException A FaultException is thrown if:
     *   - The server corresponding to the sid cannot be found
     * @apidoc.doc Delete a system given its server id synchronously
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "cleanupType", "Possible values:
     *  'FAIL_ON_CLEANUP_ERR' - fail in case of cleanup error,
     *  'NO_CLEANUP' - do not cleanup, just delete,
     *  'FORCE_DELETE' - Try cleanup first but delete server anyway in case of error")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteSystem(User loggedInUser, Integer sid, String cleanupType)
            throws FaultException {

        Server server = lookupServer(loggedInUser, sid);
        systemManager.deleteServerAndCleanup(loggedInUser,
                server.getId(),
                SystemManager.ServerCleanupType.fromString(cleanupType).orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Invalid server cleanup type value: " + cleanupType))
        );
        return 1;
    }

    /**
     * Get the addresses and hostname for a given server
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @return Returns a map containing the servers addresses and hostname attributes
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Get the addresses and hostname for a given server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *          #struct_begin("network info")
     *              #prop_desc("string", "ip", "IPv4 address of server")
     *              #prop_desc("string", "ip6", "IPv6 address of server")
     *              #prop_desc("string", "hostname", "Hostname of server")
     *          #struct_end()
     */
    @ReadOnly
    public Map<String, String> getNetwork(User loggedInUser, Integer sid)
            throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        // Get the ip, ip6 and hostname for the server
        String ip = server.getIpAddress();
        String ip6 = server.getIp6Address();
        String hostname = server.getHostname();

        // Stick in a map and return
        Map<String, String> network = new HashMap<>();
        network.put("ip", StringUtils.defaultString(ip));
        network.put("ip6", StringUtils.defaultString(ip6));
        network.put("hostname", StringUtils.defaultString(hostname));

        return network;
    }

    /**
     * Get the addresses and hostname for a given list of system
     * @param loggedInUser The current user
     * @param sids the IDs of the systems
     * @return Returns a list of maps containing the systems IP addresses and hostname
     * @throws FaultException A FaultException is thrown if the systems cannot be found.
     *
     * @apidoc.doc Get the addresses and hostname for a given list of systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.returntype
     *   #return_array_begin()
     *     #struct_begin("network info")
     *       #prop_desc("int", "system_id", "ID of the system")
     *       #prop_desc("string", "ip", "IPv4 address of system")
     *       #prop_desc("string", "ip6", "IPv6 address of system")
     *       #prop_desc("string", "hostname", "Hostname of system")
     *       #prop_desc("string", "primary_fqdn", "Primary FQDN of system")
     *     #struct_end()
     *   #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> getNetworkForSystems(User loggedInUser, List<Integer> sids)
            throws FaultException {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Server> servers = this.xmlRpcSystemHelper.lookupServers(loggedInUser, sids);

        for (Server server : servers) {
            Map<String, Object> network = new HashMap<>();
            network.put("system_id", server.getId());
            network.put("ip", StringUtils.defaultString(server.getIpAddress()));
            network.put("ip6", StringUtils.defaultString(server.getIp6Address()));
            network.put("hostname", StringUtils.defaultString(server.getHostname()));
            ServerFQDN fqdn = server.findPrimaryFqdn();
            network.put("primary_fqdn", StringUtils.defaultString(fqdn != null ? fqdn.getName() : null));
            result.add(network);
        }
        return result;
    }

    /**
     * Get a list of network devices for a given server.
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @return Returns an array of maps representing a network device for the server.
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns the network devices for the given server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $NetworkInterfaceSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<NetworkInterface> getNetworkDevices(User loggedInUser,
            Integer sid)
                    throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);
        Set<NetworkInterface> devices = server.getNetworkInterfaces();
        return new ArrayList<>(devices);
    }

    /**
     * Set a servers membership in a given group
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @param sgid The id of the server group
     * @param member Should this server be a member of this group?
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Set a servers membership in a given group.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("int", "sgid")
     * @apidoc.param #param_desc("boolean", "member",  "'1' to assign the given server to
     * the given server group, '0' to remove the given server from the given server
     * group.")
     * @apidoc.returntype #return_int_success()
     */
    public int setGroupMembership(User loggedInUser, Integer sid, Integer sgid,
            Boolean member) throws FaultException {
        // Get the logged in user and server
        ensureSystemGroupAdmin(loggedInUser);
        Server server = lookupServer(loggedInUser, sid);
        try {
            ManagedServerGroup group = serverGroupManager.lookup(sgid.longValue(),
                    loggedInUser);


            List<Server> servers = new ArrayList<>(1);
            servers.add(server);

            if (member) {
                //add to server group
                serverGroupManager.addServers(group, servers, loggedInUser);
            }
            else {
                //remove from server group
                serverGroupManager.removeServers(group, servers, loggedInUser);
            }
        }
        catch (LookupException le) {
            throw new PermissionCheckFailureException(le);
        }

        return 1;
    }

    /**
     * List the available groups for a given system
     * @param loggedInUser The current user
     * @param sid The id for the server in question
     * @return Returns an array of maps representing a system group
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc List the available groups for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *  #return_array_begin()
     *      #struct_begin("system group")
     *          #prop_desc("int", "id", "server group id")
     *          #prop_desc("int", "subscribed", "1 if the given server is subscribed
     *               to this server group, 0 otherwise")
     *          #prop_desc("string", "system_group_name", "Name of the server group")
     *          #prop_desc("string", "sgid", "server group id (Deprecated)")
     *      #struct_end()
     *  #array_end()
     */
    @ReadOnly
    public Object[] listGroups(User loggedInUser, Integer sid) throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        DataResult<Map<String, Object>> groups =
                SystemManager.availableSystemGroups(server, loggedInUser);
        List<Map<String, Object>> returnList = new ArrayList<>();


        // More stupid data munging...
        for (Map<String, Object> map : groups) {
            Map<String, Object> row = new HashMap<>();

            row.put("id", map.get("id"));
            row.put("sgid", map.get("id").toString());
            row.put("system_group_name",
                    StringUtils.defaultString((String) map.get("group_name")));
            row.put("subscribed", map.get("is_system_member"));
            returnList.add(row);
        }

        return returnList.toArray();
    }

    /**
     * List systems for a given user
     * @param loggedInUser The current user
     * @param login The login for the target user
     * @return Returns an array of maps representing a system
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc List systems for a given user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.returntype
     *          #return_array_begin()
     *              $ShortSystemInfoSerializer
     *          #array_end()
     */
    @ReadOnly
    public List<ShortSystemInfo> listUserSystems(User loggedInUser, String login)
            throws FaultException {
        // Get the logged in user
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);
        return SystemManager.systemListShort(target, null);
    }

    /**
     * List systems for the logged in user
     * @param loggedInUser The current user
     * @return Returns an array of maps representing a system
     *
     * @apidoc.doc List systems for the logged in user.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *          #return_array_begin()
     *              $ShortSystemInfoSerializer
     *          #array_end()
     */
    @ReadOnly
    public List<ShortSystemInfo> listUserSystems(User loggedInUser) {
        // Get the logged in user
        return SystemManager.systemListShort(loggedInUser, null);
    }

    /**
     * Set custom values for the specified server.
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @param values A map containing the new set of custom data values for this server
     * @return Returns a 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Set custom values for the specified server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param
     *    #struct_begin("values")
     *      #prop("string", "custom info label")
     *      #prop("string", "value")
     *    #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setCustomValues(User loggedInUser, Integer sid, Map<String, String> values)
            throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);
        Org org = loggedInUser.getOrg();
        List<String> skippedKeys = new ArrayList<>();

        /*
         * Loop through the map the user sent us. Check to make sure that the org has the
         * corresponding custom data key. If so, update the value, if not, add the key to
         * the skippedKeys list so we can throw a fault exception later and tell the user
         * which keys were skipped.
         */
        Set<String> keys = values.keySet();
        for (String label : keys) {
            if (org.hasCustomDataKey(label) && !StringUtils.isBlank(values.get(label))) {
                server.addCustomDataValue(label, values.get(label), loggedInUser);
            }
            else {
                // Add label to skippedKeys list
                skippedKeys.add(label);
            }
        }

        server.asMinionServer().ifPresent(minion -> MinionPillarManager.INSTANCE.generatePillar(minion, false,
            MinionPillarManager.PillarSubset.CUSTOM_INFO));

        // If we skipped any keys, we need to throw an exception and let the user know.
        if (!skippedKeys.isEmpty()) {
            // We need to throw an exception. Append each undefined key to the
            // exception message.
            StringBuilder msg = new StringBuilder("One or more of the following " +
                    "custom info fields was not defined: ");

            for (String label : skippedKeys) {
                msg.append("\n" + label);
            }

            throw new UndefinedCustomFieldsException(msg.toString());
        }

        return 1;
    }

    /**
     * Get the custom data values defined for the server
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @return Returns a map containing the defined custom data values for the given server.
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found or if the name is invalid.
     *
     * @apidoc.doc Get the custom data values defined for the server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #struct_begin("custom value")
     *          #prop("string", "custom info label")
     *      #struct_end()
     */
    @ReadOnly
    public Map<String, String> getCustomValues(User loggedInUser, Integer sid)
        throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        Set<CustomDataValue> customDataValues = server.getCustomDataValues();
        Map<String, String> returnMap = new HashMap<>();

        /*
         * Loop through the customDataValues set for the server. We're only interested in
         * the key and value information from the CustomDataValue object.
         */
        for (CustomDataValue val : customDataValues) {
            if (val.getValue() != null) {
                returnMap.put(val.getKey().getLabel(), val.getValue());
            }
            else {
                returnMap.put(val.getKey().getLabel(), new String(""));
            }
        }

        return returnMap;
    }

    /**
     * Delete the custom values defined for the custom system information keys
     * provided from the given system.
     * @param loggedInUser The current user
     * @param sid The id of the server in question
     * @param keys A list of custom data labels/keys to delete from the server
     * @return Returns a 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Delete the custom values defined for the custom system information keys
     * provided from the given system.<br/>
     * (Note: Attempt to delete values of non-existing keys throws exception. Attempt to
     * delete value of existing key which has assigned no values doesn't throw exception.)
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param  #array_single("string", "keys")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteCustomValues(User loggedInUser, Integer sid, List<String> keys)
            throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);
        loggedInUser.getOrg();
        List<String> skippedKeys = new ArrayList<>();

        /*
         * Loop through the list the user sent us. Check to make sure that the org has the
         * corresponding custom data key. If so, remove the value, if not, add the key to
         * the skippedKeys list so we can throw a fault exception later and tell the user
         * which keys were skipped.
         */
        for (String label : keys) {
            CustomDataKey key = OrgFactory.lookupKeyByLabelAndOrg(label,
                    loggedInUser.getOrg());

            // Does the custom data key exist?
            if (key == null || key.getLabel() == null) {
                // Add label to skippedKeys list
                skippedKeys.add(label);
            }
            else {
                ServerFactory.removeCustomDataValue(server, key);
            }
        }

        // If we skipped any keys, we need to throw an exception and let the user know.
        if (!skippedKeys.isEmpty()) {
            // We need to throw an exception. Append each undefined key to the
            // exception message.
            StringBuilder msg = new StringBuilder("One or more of the following " +
                "custom info keys was not defined: ");

            for (String label : skippedKeys) {
                msg.append("\n" + label);
            }
            throw new UndefinedCustomFieldsException(msg.toString());
        }
        return 1;
    }

    /**
     * Set the profile name for the server
     * @param loggedInUser The current user
     * @param sid The id for the server in question
     * @param name The new profile name for the server
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found or if the name is invalid.
     *
     * @apidoc.doc Set the profile name for the server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "name", "Name of the profile.")
     * @apidoc.returntype #return_int_success()
     */
    public int setProfileName(User loggedInUser, Integer sid, String name)
            throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        //Do some validation on the name string
        name = StringUtils.trim(name); //trim the whitespace
        validateProfileName(name);
        server.setName(name);
        SystemManager.storeServer(server);

        return 1;
    }


    private void validateProfileName(String name) throws FaultException {
        if (name == null || name.length() < 2) { //too short
            throw new ProfileNameTooShortException();
        }

        if (name.length() > 128) { //too long
            throw new ProfileNameTooLongException();
        }
    }

    /**
     * Add a new note to the given server
     * @param loggedInUser The current user
     * @param sid The id for the server to add the note to
     * @param subject The subject of the note
     * @param body The body for the note
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Add a new note to the given server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "subject", "What the note is about.")
     * @apidoc.param #param_desc("string", "body", "Content of the note.")
     * @apidoc.returntype #return_int_success()
     */
    public int addNote(User loggedInUser, Integer sid, String subject, String body)
            throws FaultException {
        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        server.addNote(loggedInUser, subject, body);
        SystemManager.storeServer(server);

        return 1;
    }

    /**
     * Deletes the given note from the server.
     *
     * @param loggedInUser The current user
     * @param sid        identifies the server on which the note resides
     * @param noteId     identifies the note to delete
     * @return 1 if successful, exception otherwise
     * @throws NoSuchSystemException A NoSuchSystemException is thrown if the server
     * corresponding to sid cannot be found.
     *
     * @apidoc.doc Deletes the given note from the server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("int", "noteId")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteNote(User loggedInUser, Integer sid, Integer noteId) {
        if (sid == null) {
            throw new IllegalArgumentException("sid cannot be null");
        }

        if (noteId == null) {
            throw new IllegalArgumentException("nid cannot be null");
        }

        SystemManager.deleteNote(loggedInUser, sid.longValue(), noteId.longValue());

        return 1;
    }

    /**
     * Deletes all notes from the server.
     *
     * @param loggedInUser The current user
     * @param sid        identifies the server on which the note resides
     * @return 1 if successful, exception otherwise
     * @throws NoSuchSystemException A NoSuchSystemException is thrown if the server
     * corresponding to sid cannot be found.
     *
     * @apidoc.doc Deletes all notes from the server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteNotes(User loggedInUser, Integer sid) {
        if (sid == null) {
            throw new IllegalArgumentException("sid cannot be null");
        }

        SystemManager.deleteNotes(loggedInUser, sid.longValue());

        return 1;
    }

    /**
     * List all the events of a given type for a given server created after the specified date.
     * @param loggedInUser The current user
     * @param sid The id of the server you are wanting to lookup
     * @param actionType type of the action
     * @param earliestDate the minimum creation date for the events retrieved
     * @return Returns an array of maps representing a system
     * @since 10.8
     *
     * @apidoc.doc List system actions of the specified type that were *scheduled* against the given server after the
     * specified date. "actionType" should be exactly the string returned in the action_type field
     * from the listSystemEvents(sessionKey, serverId) method. For example,
     * 'Package Install' or 'Initiate a kickstart for a virtual guest.'
     * Note: see also system.getEventHistory method which returns a history of all events.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of system.")
     * @apidoc.param #param_desc("string", "actionType", "Type of the action.")
     * @apidoc.param #param("$date", "earliestDate")
     * @apidoc.returntype
     *  #return_array_begin()
     *      #struct_begin("action")
     *          #prop_desc("int", "failed_count", "Number of times action failed.")
     *          #prop_desc("string", "modified", "Date modified. (Deprecated by
     *                     modified_date)")
     *          #prop_desc($date, "modified_date", "Date modified.")
     *          #prop_desc("string", "created", "Date created. (Deprecated by
     *                     created_date)")
     *          #prop_desc($date, "created_date", "Date created.")
     *          #prop("string", "action_type")
     *          #prop_desc("int", "successful_count",
     *                     "Number of times action was successful.")
     *          #prop_desc("string", "earliest_action", "Earliest date this action
     *                     will occur.")
     *          #prop_desc("int", "archived", "If this action is archived. (1 or 0)")
     *          #prop_desc("string", "scheduler_user", "available only if concrete user
     *                     has scheduled the action")
     *          #prop_desc("string", "prerequisite", "Pre-requisite action. (optional)")
     *          #prop_desc("string", "name", "Name of this action.")
     *          #prop_desc("int", "id", "Id of this action.")
     *          #prop_desc("string", "version", "Version of action.")
     *          #prop_desc("string", "completion_time", "The date/time the event was
     *                     completed. Format -&gt;YYYY-MM-dd hh:mm:ss.ms
     *                     Eg -&gt;2007-06-04 13:58:13.0. (optional)
     *                     (Deprecated by completed_date)")
     *          #prop_desc($date, "completed_date", "The date/time the event was completed.
     *                     (optional)")
     *          #prop_desc("string", "pickup_time", "The date/time the action was picked
     *                     up. Format -&gt;YYYY-MM-dd hh:mm:ss.ms
     *                     Eg -&gt;2007-06-04 13:58:13.0. (optional)
     *                     (Deprecated by pickup_date)")
     *          #prop_desc($date, "pickup_date", "The date/time the action was picked up.
     *                     (optional)")
     *          #prop_desc("string", "result_msg", "The result string after the action
     *                     executes at the client machine. (optional)")
     *          #prop_array_begin_desc("additional_info", "This array contains additional
     *              information for the event, if available.")
     *              #struct_begin("info")
     *                  #prop_desc("string", "detail", "The detail provided depends on the
     *                  specific event.  For example, for a package event, this will be the
     *                  package name, for an errata event, this will be the advisory name
     *                  and synopsis, for a config file event, this will be path and
     *                  optional revision information...etc.")
     *                  #prop_desc("string", "result", "The result (if included) depends
     *                  on the specific event.  For example, for a package or errata event,
     *                  no result is included, for a config file event, the result might
     *                  include an error (if one occurred, such as the file was missing)
     *                  or in the case of a config file comparison it might include the
     *                  differences found.")
     *              #struct_end()
     *          #prop_array_end()
     *      #struct_end()
     *  #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listSystemEvents(User loggedInUser, Integer sid, String actionType,
                                                      Date earliestDate) {

        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        List<ServerAction> sActions = ActionFactory.listServerActionsForServer(server, actionType, earliestDate);

        // In order to support bug 501224, this method is being updated to populate
        // the result vs having the serializer do so.  The reason is that in order to
        // support this bug, we want to be able to return some additional detail for the
        // various events in the system history; however, those details are stored in
        // different database tables depending upon the event type.  This includes
        // information like, the specific errata applied, pkgs installed/removed/
        // upgraded/verified, config files uploaded, deployed or compared...etc.

        List<Map<String, Object>> results = new ArrayList<>();

        for (ServerAction sAction : sActions) {

            Map<String, Object> result = new HashMap<>();

            Action action = sAction.getParentAction();

            if (action.getFailedCount() != null) {
                result.put("failed_count", action.getFailedCount());
            }
            if (action.getActionType().getName() != null) {
                result.put("action_type", action.getActionType().getName());
            }
            if (action.getSuccessfulCount() != null) {
                result.put("successful_count", action.getSuccessfulCount());
            }
            if (action.getEarliestAction() != null) {
                result.put("earliest_action", action.getEarliestAction().toString());
            }
            if (action.getArchived() != null) {
                result.put("archived", action.getArchived());
            }
            if ((action.getSchedulerUser() != null) &&
                    (action.getSchedulerUser().getLogin() != null)) {
                result.put("scheduler_user", action.getSchedulerUser().getLogin());
            }
            if (action.getPrerequisite() != null) {
                result.put("prerequisite", action.getPrerequisite().getId());
            }
            if (action.getName() != null) {
                result.put("name", action.getName());
            }
            if (action.getId() != null) {
                result.put("id", action.getId());
            }
            if (action.getVersion() != null) {
                result.put("version", action.getVersion().toString());
            }

            if (sAction.getCompletionTime() != null) {
                result.put("completion_time", sAction.getCompletionTime().toString());
            }
            if (sAction.getPickupTime() != null) {
                result.put("pickup_time", sAction.getPickupTime().toString());
            }
            if (sAction.getModified() != null) {
                result.put("modified", sAction.getModified().toString());
                result.put("modified_date", sAction.getModified());
            }
            if (sAction.getCreated() != null) {
                result.put("created", sAction.getCreated().toString());
                result.put("created_date", sAction.getCreated());
            }
            if (sAction.getCompletionTime() != null) {
                result.put("completed_date", sAction.getCompletionTime());
            }
            if (sAction.getPickupTime() != null) {
                result.put("pickup_date", sAction.getPickupTime());
            }
            if (sAction.getResultMsg() != null) {
                result.put("result_msg", sAction.getResultMsg());
            }

            final List<Map<String, String>> additionalInfo = createActionSpecificDetails(action, sAction);
            if (!additionalInfo.isEmpty()) {
                result.put("additional_info", additionalInfo);
            }

            results.add(result);
        }
        return results;
    }

    private List<Map<String, String>> createActionSpecificDetails(Action action, ServerAction serverAction) {
        // depending on the event type, we need to retrieve additional information
        // and store that information in the result
        final ActionType type = action.getActionType();
        final List<Map<String, String>> additionalInfo = new ArrayList<>();

        if (type.equals(ActionFactory.TYPE_PACKAGES_REMOVE) ||
                type.equals(ActionFactory.TYPE_PACKAGES_UPDATE) ||
                type.equals(ActionFactory.TYPE_PACKAGES_VERIFY)) {

            // retrieve the list of package names associated with the action...
            DataResult<Row> pkgs = ActionManager.getPackageList(action.getId(), null);
            for (Row pkg : pkgs) {
                String detail = (String) pkg.get("nvre");

                Map<String, String> info = new HashMap<>();
                info.put("detail", detail);
                additionalInfo.add(info);
            }
        }
        else if (type.equals(ActionFactory.TYPE_ERRATA)) {

            // retrieve the errata that were associated with the action...
            DataResult errata = ActionManager.getErrataList(action.getId());
            for (Object erratumIn : errata) {
                Map erratum = (Map) erratumIn;
                String detail = (String) erratum.get("advisory");
                detail += " (" + erratum.get("synopsis") + ")";

                Map<String, String> info = new HashMap<>();
                info.put("detail", detail);
                additionalInfo.add(info);
            }
        }
        else if (type.equals(ActionFactory.TYPE_CONFIGFILES_UPLOAD) ||
                type.equals(ActionFactory.TYPE_CONFIGFILES_MTIME_UPLOAD)) {

            // retrieve the details associated with the action...
            DataResult files = ActionManager.getConfigFileUploadList(action.getId());
            for (Object fileIn : files) {
                Map file = (Map) fileIn;

                Map<String, String> info = new HashMap<>();
                info.put("detail", (String) file.get("path"));
                String error = (String) file.get("failure_reason");
                if (error != null) {
                    info.put("result", error);
                }
                additionalInfo.add(info);
            }
        }
        else if (type.equals(ActionFactory.TYPE_CONFIGFILES_DEPLOY)) {

            // retrieve the details associated with the action...
            DataResult files = ActionManager.getConfigFileDeployList(action.getId());
            for (Object fileIn : files) {
                Map file = (Map) fileIn;

                Map<String, String> info = new HashMap<>();
                String path = (String) file.get("path");
                path += " (rev. " + file.get("revision") + ")";
                info.put("detail", path);
                String error = (String) file.get("failure_reason");
                if (error != null) {
                    info.put("result", error);
                }
                additionalInfo.add(info);
            }
        }
        else if (type.equals(ActionFactory.TYPE_CONFIGFILES_DIFF)) {

            // retrieve the details associated with the action...
            DataResult files = ActionManager.getConfigFileDiffList(action.getId());
            for (Object fileIn : files) {
                Map file = (Map) fileIn;

                Map<String, String> info = new HashMap<>();
                String path = (String) file.get("path");
                path += " (rev. " + file.get("revision") + ")";
                info.put("detail", path);

                String error = (String) file.get("failure_reason");
                if (error != null) {
                    info.put("result", error);
                }
                else {
                    // if there wasn't an error, check to see if there was a difference
                    // detected...
                    String diffString = HibernateFactory.getBlobContents(
                            file.get("diff"));
                    if (diffString != null) {
                        info.put("result", diffString);
                    }
                }
                additionalInfo.add(info);
            }
        }
        else if (type.equals(ActionFactory.TYPE_APPLY_STATES)) {
            final ApplyStatesActionDetails detail = ActionFactory.lookupApplyStatesActionDetails(action.getId());
            if (detail != null) {
                final Optional<ApplyStatesActionResult> serverResult = detail.getResult(serverAction.getServerId());

                final String output = serverResult.flatMap(ApplyStatesActionResult::getResult)
                                                  .orElse(Collections.emptyList())
                                                  .stream()
                                                  .sorted(Comparator.comparing(StateResult::getRunNum))
                                                  .map(StateResult::toString)
                                                  .collect(Collectors.joining());

                final String returnCode = serverResult.map(ApplyStatesActionResult::getReturnCode)
                                                      .map(Object::toString)
                                                      .orElse("");

                additionalInfo.add(Map.of("detail", output, "result", returnCode));
            }
        }

        return additionalInfo;
    }

    /**
     * List all the events for a given server.
     * @param loggedInUser The current user
     * @param sid The id of the server you are wanting to lookup
     * @return Returns an array of maps representing a system
     * @since 10.8
     *
     * @apidoc.doc List all system actions that were *scheduled* against the given server.
     * This may require the caller to filter the result to fetch actions with a specific action type or
     * to use the overloaded system.listSystemEvents method with actionType as a parameter.
     * Note: see also system.getEventHistory method which returns a history of all events.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of system.")
     * @apidoc.returntype
     *  #return_array_begin()
     *      #struct_begin("action")
     *          #prop_desc("int", "failed_count", "Number of times action failed.")
     *          #prop_desc("string", "modified", "Date modified. (Deprecated by
     *                     modified_date)")
     *          #prop_desc($date, "modified_date", "Date modified.")
     *          #prop_desc("string", "created", "Date created. (Deprecated by
     *                     created_date)")
     *          #prop_desc($date, "created_date", "Date created.")
     *          #prop("string", "action_type")
     *          #prop_desc("int", "successful_count",
     *                     "Number of times action was successful.")
     *          #prop_desc("string", "earliest_action", "Earliest date this action
     *                     will occur.")
     *          #prop_desc("int", "archived", "If this action is archived. (1 or 0)")
     *          #prop_desc("string", "scheduler_user", "available only if concrete user
     *                     has scheduled the action")
     *          #prop_desc("string", "prerequisite", "Pre-requisite action. (optional)")
     *          #prop_desc("string", "name", "Name of this action.")
     *          #prop_desc("int", "id", "Id of this action.")
     *          #prop_desc("string", "version", "Version of action.")
     *          #prop_desc("string", "completion_time", "The date/time the event was
     *                     completed. Format -&gt;YYYY-MM-dd hh:mm:ss.ms
     *                     Eg -&gt;2007-06-04 13:58:13.0. (optional)
     *                     (Deprecated by completed_date)")
     *          #prop_desc($date, "completed_date", "The date/time the event was completed.
     *                     (optional)")
     *          #prop_desc("string", "pickup_time", "The date/time the action was picked
     *                     up. Format -&gt;YYYY-MM-dd hh:mm:ss.ms
     *                     Eg -&gt;2007-06-04 13:58:13.0. (optional)
     *                     (Deprecated by pickup_date)")
     *          #prop_desc($date, "pickup_date", "The date/time the action was picked up.
     *                     (optional)")
     *          #prop_desc("string", "result_msg", "The result string after the action
     *                     executes at the client machine. (optional)")
     *          #prop_array_begin_desc("additional_info", "This array contains additional
     *              information for the event, if available.")
     *              #struct_begin("info")
     *                  #prop_desc("string", "detail", "The detail provided depends on the
     *                  specific event.  For example, for a package event, this will be the
     *                  package name, for an errata event, this will be the advisory name
     *                  and synopsis, for a config file event, this will be path and
     *                  optional revision information...etc.")
     *                  #prop_desc("string", "result", "The result (if included) depends
     *                  on the specific event.  For example, for a package or errata event,
     *                  no result is included, for a config file event, the result might
     *                  include an error (if one occurred, such as the file was missing)
     *                  or in the case of a config file comparison it might include the
     *                  differences found.")
     *              #struct_end()
     *          #prop_array_end()
     *      #struct_end()
     *  #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listSystemEvents(User loggedInUser, Integer sid) {
        return listSystemEvents(loggedInUser, sid, null, null);
    }

    /**
     * List all the events of a given type for a given server.
     * @param loggedInUser The current user
     * @param sid The id of the server you are wanting to lookup
     * @param actionType type of the action
     * @return Returns an array of maps representing a system
     * @since 10.8
     *
     * @apidoc.doc List system actions of the specified type that were *scheduled* against the given server.
     * "actionType" should be exactly the string returned in the action_type field
     * from the listSystemEvents(sessionKey, serverId) method. For example,
     * 'Package Install' or 'Initiate a kickstart for a virtual guest.'
     * Note: see also system.getEventHistory method which returns a history of all events.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of system.")
     * @apidoc.param #param_desc("string", "actionType", "Type of the action.")
     * @apidoc.returntype
     *  #return_array_begin()
     *      #struct_begin("action")
     *          #prop_desc("int", "failed_count", "Number of times action failed.")
     *          #prop_desc("string", "modified", "Date modified. (Deprecated by
     *                     modified_date)")
     *          #prop_desc($date, "modified_date", "Date modified.")
     *          #prop_desc("string", "created", "Date created. (Deprecated by
     *                     created_date)")
     *          #prop_desc($date, "created_date", "Date created.")
     *          #prop("string", "action_type")
     *          #prop_desc("int", "successful_count",
     *                     "Number of times action was successful.")
     *          #prop_desc("string", "earliest_action", "Earliest date this action
     *                     will occur.")
     *          #prop_desc("int", "archived", "If this action is archived. (1 or 0)")
     *          #prop_desc("string", "scheduler_user", "available only if concrete user
     *                     has scheduled the action")
     *          #prop_desc("string", "prerequisite", "Pre-requisite action. (optional)")
     *          #prop_desc("string", "name", "Name of this action.")
     *          #prop_desc("int", "id", "Id of this action.")
     *          #prop_desc("string", "version", "Version of action.")
     *          #prop_desc("string", "completion_time", "The date/time the event was
     *                     completed. Format -&gt;YYYY-MM-dd hh:mm:ss.ms
     *                     Eg -&gt;2007-06-04 13:58:13.0. (optional)
     *                     (Deprecated by completed_date)")
     *          #prop_desc($date, "completed_date", "The date/time the event was completed.
     *                     (optional)")
     *          #prop_desc("string", "pickup_time", "The date/time the action was picked
     *                     up. Format -&gt;YYYY-MM-dd hh:mm:ss.ms
     *                     Eg -&gt;2007-06-04 13:58:13.0. (optional)
     *                     (Deprecated by pickup_date)")
     *          #prop_desc($date, "pickup_date", "The date/time the action was picked up.
     *                     (optional)")
     *          #prop_desc("string", "result_msg", "The result string after the action
     *                     executes at the client machine. (optional)")
     *          #prop_array_begin_desc("additional_info", "This array contains additional
     *              information for the event, if available.")
     *              #struct_begin("info")
     *                  #prop_desc("string", "detail", "The detail provided depends on the
     *                  specific event.  For example, for a package event, this will be the
     *                  package name, for an errata event, this will be the advisory name
     *                  and synopsis, for a config file event, this will be path and
     *                  optional revision information...etc.")
     *                  #prop_desc("string", "result", "The result (if included) depends
     *                  on the specific event.  For example, for a package or errata event,
     *                  no result is included, for a config file event, the result might
     *                  include an error (if one occurred, such as the file was missing)
     *                  or in the case of a config file comparison it might include the
     *                  differences found.")
     *              #struct_end()
     *          #prop_array_end()
     *      #struct_end()
     *  #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listSystemEvents(User loggedInUser, Integer sid, String actionType) {
        return listSystemEvents(loggedInUser, sid, actionType, null);
    }

    /**
     * List all the events for a given server created after the specified date.
     * @param loggedInUser The current user
     * @param sid The id of the server you are wanting to lookup
     * @param earliestDate the minimum creation date for the events retrieved
     * @return Returns an array of maps representing a system
     * @since 10.8
     *
     * @apidoc.doc List system actions of the specified type that were *scheduled* against the given server after the
     * specified date. This may require the caller to filter the result to fetch actions with a specific action type or
     * to use the overloaded system.listSystemEvents method with actionType as a parameter.
     * Note: see also system.getEventHistory method which returns a history of all events.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of system.")
     * @apidoc.param #param("$date", "earliestDate")
     * @apidoc.returntype
     *  #return_array_begin()
     *      #struct_begin("action")
     *          #prop_desc("int", "failed_count", "Number of times action failed.")
     *          #prop_desc("string", "modified", "Date modified. (Deprecated by
     *                     modified_date)")
     *          #prop_desc($date, "modified_date", "Date modified.")
     *          #prop_desc("string", "created", "Date created. (Deprecated by
     *                     created_date)")
     *          #prop_desc($date, "created_date", "Date created.")
     *          #prop("string", "action_type")
     *          #prop_desc("int", "successful_count",
     *                     "Number of times action was successful.")
     *          #prop_desc("string", "earliest_action", "Earliest date this action
     *                     will occur.")
     *          #prop_desc("int", "archived", "If this action is archived. (1 or 0)")
     *          #prop_desc("string", "scheduler_user", "available only if concrete user
     *                     has scheduled the action")
     *          #prop_desc("string", "prerequisite", "Pre-requisite action. (optional)")
     *          #prop_desc("string", "name", "Name of this action.")
     *          #prop_desc("int", "id", "Id of this action.")
     *          #prop_desc("string", "version", "Version of action.")
     *          #prop_desc("string", "completion_time", "The date/time the event was
     *                     completed. Format -&gt;YYYY-MM-dd hh:mm:ss.ms
     *                     Eg -&gt;2007-06-04 13:58:13.0. (optional)
     *                     (Deprecated by completed_date)")
     *          #prop_desc($date, "completed_date", "The date/time the event was completed.
     *                     (optional)")
     *          #prop_desc("string", "pickup_time", "The date/time the action was picked
     *                     up. Format -&gt;YYYY-MM-dd hh:mm:ss.ms
     *                     Eg -&gt;2007-06-04 13:58:13.0. (optional)
     *                     (Deprecated by pickup_date)")
     *          #prop_desc($date, "pickup_date", "The date/time the action was picked up.
     *                     (optional)")
     *          #prop_desc("string", "result_msg", "The result string after the action
     *                     executes at the client machine. (optional)")
     *          #prop_array_begin_desc("additional_info", "This array contains additional
     *              information for the event, if available.")
     *              #struct_begin("info")
     *                  #prop_desc("string", "detail", "The detail provided depends on the
     *                  specific event.  For example, for a package event, this will be the
     *                  package name, for an errata event, this will be the advisory name
     *                  and synopsis, for a config file event, this will be path and
     *                  optional revision information...etc.")
     *                  #prop_desc("string", "result", "The result (if included) depends
     *                  on the specific event.  For example, for a package or errata event,
     *                  no result is included, for a config file event, the result might
     *                  include an error (if one occurred, such as the file was missing)
     *                  or in the case of a config file comparison it might include the
     *                  differences found.")
     *              #struct_end()
     *          #prop_array_end()
     *      #struct_end()
     *  #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listSystemEvents(User loggedInUser, Integer sid, Date earliestDate) {
        return listSystemEvents(loggedInUser, sid, null, earliestDate);
    }

    /**
     *
     * Provision a guest on the server specified.  Defaults to: memory=512, vcpu=1,
     * storage=3GB.
     *
     * @param loggedInUser The current user
     * @param sid of server to provision guest on
     * @param guestName to assign to guest
     * @param profileName of Kickstart Profile to use.
     * @return Returns 1 if successful, exception otherwise
     *
     * @apidoc.doc Provision a guest on the host specified.  Defaults to:
     * memory=512MB, vcpu=1, storage=3GB, mac_address=random.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of host to provision guest on.")
     * @apidoc.param #param("string", "guestName")
     * @apidoc.param #param_desc("string", "profileName", "Kickstart profile to use.")
     * @apidoc.returntype #return_int_success()
     */
    public int provisionVirtualGuest(User loggedInUser, Integer sid, String guestName,
            String profileName) {
        return provisionVirtualGuest(loggedInUser, sid, guestName, profileName,
                512, 1, 3, "");
    }

    /**
     * Provision a system using the specified kickstart/autoinstallation profile.
     *
     * @param loggedInUser The current user
     * @param sid of the system to be provisioned
     * @param profileName of Profile to be used.
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * id cannot be found or profile is not found.
     *
     * @apidoc.doc Provision a system using the specified kickstart/autoinstallation profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of the system to be provisioned.")
     * @apidoc.param #param_desc("string", "profileName", "Profile to use.")
     * @apidoc.returntype #param_desc("int", "id", "ID of the action scheduled, otherwise exception thrown
     * on error")
     */
    public int provisionSystem(User loggedInUser, Integer sid, String profileName)
            throws FaultException {
        log.debug("provisionSystem called.");

        // Lookup the server so we can validate it exists and throw error if not.
        Server server = lookupServer(loggedInUser, sid);
        if (server.hasEntitlement(EntitlementManager.FOREIGN)) {
            throw new FaultException(-2, "provisionError",
                    "System does not have required entitlement");
        }

        KickstartData ksdata = KickstartFactory.
                lookupKickstartDataByLabelAndOrgId(profileName,
                        loggedInUser.getOrg().getId());
        if (ksdata == null) {
            throw new FaultException(-3, "kickstartProfileNotFound",
                    "No Kickstart Profile found with label: " + profileName);
        }

        String host = RhnXmlRpcServer.getServerName();


        KickstartScheduleCommand cmd = new KickstartScheduleCommand(
                Long.valueOf(sid),
                ksdata.getId(), loggedInUser, new Date(), host);
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new FaultException(-2, "provisionError",
                    LocalizationService.getInstance().getMessage(ve.getKey()));
        }
        return cmd.getScheduledAction().getId().intValue();
    }

    /**
     * Provision a system using the specified kickstart/autoinstallation profile at specified time.
     *
     * @param loggedInUser The current user
     * @param sid of the system to be provisioned
     * @param profileName of Profile to be used.
     * @param earliestDate when the autoinstallation needs to be scheduled
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * id cannot be found or profile is not found.
     *
     * @apidoc.doc Provision a system using the specified kickstart/autoinstallation profile.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of the system to be provisioned.")
     * @apidoc.param #param_desc("string", "profileName", "Profile to use.")
     * @apidoc.param #param("$date", "earliestDate")
     * @apidoc.returntype #param_desc("int", "id", "ID of the action scheduled, otherwise exception thrown
     * on error")
     */
    public int provisionSystem(User loggedInUser, Integer sid,
            String profileName, Date earliestDate)
                    throws FaultException {
        log.debug("provisionSystem called.");

        // Lookup the server so we can validate it exists and throw error if not.
        Server server = lookupServer(loggedInUser, sid);
        if (server.hasEntitlement(EntitlementManager.FOREIGN)) {
            throw new FaultException(-2, "provisionError",
                    "System does not have required entitlement");
        }

        KickstartData ksdata = KickstartFactory.
                lookupKickstartDataByLabelAndOrgId(profileName,
                        loggedInUser.getOrg().getId());
        if (ksdata == null) {
            throw new FaultException(-3, "kickstartProfileNotFound",
                    "No Kickstart Profile found with label: " + profileName);
        }

        String host = RhnXmlRpcServer.getServerName();

        KickstartScheduleCommand cmd = new KickstartScheduleCommand(
                Long.valueOf(sid),
                ksdata.getId(), loggedInUser, earliestDate, host);
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new FaultException(-2, "provisionError",
                    LocalizationService.getInstance().getMessage(ve.getKey()));
        }
        return cmd.getScheduledAction().getId().intValue();
    }

    /**
     * Provision a guest on the server specified.
     *
     * @param loggedInUser The current user
     * @param sid of server to provision guest on
     * @param guestName to assign to guest
     * @param profileName of Kickstart Profile to use.
     * @param memoryMb to allocate to the guest (maxMemory)
     * @param vcpus to assign
     * @param storageGb to assign to disk
     * @return Returns 1 if successful, exception otherwise
     *
     * @apidoc.doc Provision a guest on the host specified.  This schedules the guest
     * for creation and will begin the provisioning process when the host checks in
     * or if OSAD is enabled will begin immediately. Defaults to mac_address=random.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of host to provision guest on.")
     * @apidoc.param #param("string", "guestName")
     * @apidoc.param #param_desc("string", "profileName", "Kickstart Profile to use.")
     * @apidoc.param #param_desc("int", "memoryMb", "Memory to allocate to the guest")
     * @apidoc.param #param_desc("int", "vcpus", "Number of virtual CPUs to allocate to
     *                                          the guest.")
     * @apidoc.param #param_desc("int", "storageGb", "Size of the guests disk image.")
     * @apidoc.returntype #return_int_success()
     */
    public int provisionVirtualGuest(User loggedInUser, Integer sid, String guestName,
            String profileName, Integer memoryMb, Integer vcpus, Integer storageGb) {
        return provisionVirtualGuest(loggedInUser, sid, guestName, profileName,
                memoryMb, vcpus, storageGb, "");
    }

    /**
     * Provision a guest on the server specified.
     *
     * @param loggedInUser The current user
     * @param sid of server to provision guest on
     * @param guestName to assign to guest
     * @param profileName of Kickstart Profile to use.
     * @param memoryMb to allocate to the guest (maxMemory)
     * @param vcpus to assign
     * @param storageGb to assign to disk
     * @param macAddress to assign
     * @return Returns 1 if successful, exception otherwise
     *
     * @apidoc.doc Provision a guest on the host specified.  This schedules the guest
     * for creation and will begin the provisioning process when the host checks in
     * or if OSAD is enabled will begin immediately.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of host to provision guest on.")
     * @apidoc.param #param("string", "guestName")
     * @apidoc.param #param_desc("string", "profileName", "Kickstart Profile to use.")
     * @apidoc.param #param_desc("int", "memoryMb", "Memory to allocate to the guest")
     * @apidoc.param #param_desc("int", "vcpus", "Number of virtual CPUs to allocate to
     *                                          the guest.")
     * @apidoc.param #param_desc("int", "storageGb", "Size of the guests disk image.")
     * @apidoc.param #param_desc("string", "macAddress", "macAddress to give the guest's
     *                                          virtual networking hardware.")
     * @apidoc.returntype #return_int_success()
     */
    public int provisionVirtualGuest(User loggedInUser, Integer sid,
            String guestName, String profileName, Integer memoryMb,
            Integer vcpus, Integer storageGb, String macAddress) {
        log.debug("provisionVirtualGuest called.");
        // Lookup the server so we can validate it exists and throw error if not.
        lookupServer(loggedInUser, sid);
        KickstartData ksdata = KickstartFactory.
                lookupKickstartDataByLabelAndOrgId(profileName, loggedInUser
                        .getOrg().getId());

        if (ksdata == null) {
            throw new FaultException(-3, "kickstartProfileNotFound",
                    "No Kickstart Profile found with label: " + profileName);
        }

        ProvisionVirtualInstanceCommand cmd = new ProvisionVirtualInstanceCommand(
                sid.longValue(), ksdata.getId(), loggedInUser, new Date(),
                ConfigDefaults.get().getCobblerHost());

        cmd.setGuestName(guestName);
        cmd.setMemoryAllocation(Long.valueOf(memoryMb));
        cmd.setVirtualCpus(Long.valueOf(vcpus.toString()));
        cmd.setLocalStorageSize(Long.valueOf(storageGb));
        // setting an empty string generates a random mac address
        cmd.setMacAddress(macAddress);
        // setting an empty string generates a default virt path
        cmd.setFilePath("");
        // Store the new KickstartSession to the DB.
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new FaultException(-2, "provisionError",
                    LocalizationService.getInstance().getMessage(
                            ve.getKey(), ve.getValues()));
        }

        return 1;
    }


    /**
     * Private helper method to lookup a server from an sid, and throws a FaultException
     * if the server cannot be found.
     * @param user The user looking up the server
     * @param sid The id of the server we're looking for
     * @return Returns the server corresponding to sid
     * @throws NoSuchSystemException A NoSuchSystemException is thrown if the server
     * corresponding to sid cannot be found.
     */
    private Server lookupServer(User user, Integer sid) throws NoSuchSystemException {
        return xmlRpcSystemHelper.lookupServer(user, sid);
    }

    /**
     * Get system IDs and last check in information for the given system name.
     * @param loggedInUser The current user
     * @param name of the server
     * @return Object[]  Integer Array containing system Ids with the given name
     *
     * @apidoc.doc Get system IDs and last check in information for the given system name.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "name")
     * @apidoc.returntype
     *          #return_array_begin()
     *              $SystemOverviewSerializer
     *          #array_end()
     */
    @ReadOnly
    public List<SystemOverview> getId(User loggedInUser, String name) {

        return SystemManager.listSystemsByName(loggedInUser, name);
    }

    /**
     * Get system name and last check in information for the given system ID.
     * @param loggedInUser The current user
     * @param sid of the server
     * @return Map containing server id, name and last checkin date
     *
     * @apidoc.doc Get system name and last check in information for the given system ID.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "sid")
     * @apidoc.returntype
     *  #struct_begin("name info")
     *      #prop_desc("int", "id", "Server id")
     *      #prop_desc("string", "name", "Server name")
     *      #prop_desc("$date", "last_checkin", "Last time server
     *              successfully checked in")
     *  #struct_end()
     */
    @ReadOnly
    public Map<String, Object> getName(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        Map<String, Object> name = new HashMap<>();
        name.put("id", server.getId());
        name.put("name", server.getName());
        name.put("last_checkin", server.getLastCheckin());
        return name;
    }

    /**
     * Provides the Date that the system was registered
     * @param loggedInUser The current user
     * @param sid  the ServerId of the system
     * @return Date the date the system was registered
     *
     * @apidoc.doc Returns the date the system was registered.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #param_desc("$date", "date", "The date the system was registered,
     * in local time")
     */
    @ReadOnly
    public Date getRegistrationDate(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        return server.getCreated();
    }


    /**
     * List the child channels that this system is subscribed to.
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the channels this server is
     * subscribed too.
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns a list of subscribed child channels.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ChannelSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<Channel> listSubscribedChildChannels(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        Set<Channel> childChannels = server.getChildChannels();

        if (childChannels == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(childChannels);
    }


    /**
     * Searching the system names using the regular expression
     *   passed in
     *
     * @param loggedInUser The current user
     * @param regexp regular expression to search with.  See the api for the
     *  Patter object for java specific regular expression details
     * @return an array of Integers containing the system Ids
     *
     * @apidoc.doc Returns a list of system IDs whose name matches
     *  the supplied regular expression(defined by
     *  <a href="http://docs.oracle.com/javase/1.5.0/docs/api/java/util/regex/Pattern.html"
     *  target="_blank">
     * Java representation of regular expressions</a>)
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "regexp",  "A regular expression")
     *
     * @apidoc.returntype
     *          #return_array_begin()
     *              $ShortSystemInfoSerializer
     *          #array_end()
     *
     */
    public List<ShortSystemInfo> searchByName(User loggedInUser, String regexp) {
        List<ShortSystemInfo> systems =  SystemManager.systemListShort(loggedInUser, null);
        List<ShortSystemInfo> returnList = new ArrayList<>();

        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);

        for (ShortSystemInfo system : systems) {
            Matcher match = pattern.matcher(system.getName());
            if (match.find()) {
                returnList.add(system);
            }
        }
        return returnList;
    }

    /**
     * Lists the administrators of a given system.  This includes Org Admins as well
     *      as system group users of groups that the system is in.
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the users that can
     *              administer the system
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns a list of users which can administer the system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *              $UserSerializer
     *      #array_end()
     */
    @ReadOnly
    public Object[] listAdministrators(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        return ServerFactory.listAdministrators(server).toArray();
    }

    /**
     * Returns the running kernel of the given system.
     *
     * @param loggedInUser The current user
     * @param sid Server ID to lookup.
     * @return Running kernel string.
     *
     * @apidoc.doc Returns the running kernel of the given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #param("string", "kernel")
     */
    @ReadOnly
    public String getRunningKernel(User loggedInUser, Integer sid) {
        try {
            Server server = SystemManager.lookupByIdAndUser(sid.longValue(),
                    loggedInUser);
            if (server.getRunningKernel() != null) {
                return server.getRunningKernel();
            }
            return LocalizationService.getInstance().getMessage(
                    "server.runningkernel.unknown");
        }
        catch (LookupException e) {
            throw new NoSuchSystemException(e);
        }
    }

    /**
     * Lists the server history of a system.  Ordered from oldest to newest.
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the server history items
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     * @deprecated This version of the method is deprecated and the return value will be changed
     * in a future API version. Please one of the other overloaded versions of getEventHistory.
     *
     * @apidoc.doc Returns a list history items associated with the system, ordered
     *             from newest to oldest. Note that the details may be empty for
     *             events that were scheduled against the system (as compared to instant).
     *             For more information on such events, see the system.listSystemEvents
     *             operation.
     *             Note: This version of the method is deprecated and the return value will be changed in a
     *             future API version. Please one of the other overloaded versions of getEventHistory.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *           $HistoryEventSerializer
     *      #array_end()
     */
    @Deprecated
    @ReadOnly
    public Object[] getEventHistory(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        List<HistoryEvent> history = ServerFactory.getServerHistory(server);
        return history.toArray();
    }

    /**
     * Lists the server history of a system after the date specified. The result list is paged and ordered from oldest
     * to newest.
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @param earliestDate the minimum completion date for the events retrieved
     * @param offset the number of results to skip
     * @param limit the maximum number of results to return
     * @return Returns an array of maps representing the server history items
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns a list of history items associated with the system happened after the specified date.
     *             The list is paged and ordered from newest to oldest.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("$date", "earliestDate")
     * @apidoc.param #param_desc("int", "offset", "Number of results to skip")
     * @apidoc.param #param_desc("int", "limit", "Maximum number of results")
     * @apidoc.returntype
     *      #return_array_begin()
     *           $SystemEventDtoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<SystemEventDto> getEventHistory(User loggedInUser, Integer sid, Date earliestDate, Integer offset,
                                                Integer limit) {

        final Server server = lookupServer(loggedInUser, sid);
        return SystemManager.systemEventHistory(server, loggedInUser.getOrg(), earliestDate, offset, limit);
    }

    /**
     * Lists the server history of a system. The result list is paged and ordered from oldest to newest.
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @param offset the number of results to skip
     * @param limit the maximum number of results to return
     * @return Returns an array of maps representing the server history items
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns a list of history items associated with the system.
     *             The list is paged and ordered from newest to oldest.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("int", "offset", "Number of results to skip")
     * @apidoc.param #param_desc("int", "limit", "Maximum number of results")
     * @apidoc.returntype
     *      #return_array_begin()
     *           $SystemEventDtoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<SystemEventDto> getEventHistory(User loggedInUser, Integer sid, Integer offset, Integer limit) {
        return getEventHistory(loggedInUser, sid, null, offset, limit);
    }

    /**
     * Lists the server history of a system after the date specified. The result list is ordered from oldest
     * to newest.
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @param earliestDate the minimum completion date for the events retrieved
     * @return Returns an array of maps representing the server history items
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns a list of history items associated with the system happened after the specified date.
     *             The list is ordered from newest to oldest.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("$date", "earliestDate")
     * @apidoc.returntype
     *      #return_array_begin()
     *           $SystemEventDtoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<SystemEventDto> getEventHistory(User loggedInUser, Integer sid, Date earliestDate) {
        return getEventHistory(loggedInUser, sid, earliestDate, null, null);
    }

    /**
     * Returns the details of a history event.
     *
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @param eid The id of the event in question
     * @return Returns the details of the requested event
     * @throws FaultException A FaultException is thrown if the server corresponding to sid or the event corresponding
     * to the eid cannot be found.

     * @apidoc.doc Returns the details of the event associated with the specified server and event.
     *             The event id must be a value returned by the system.getEventHistory API.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("int", "eid", "ID of the event")
     * @apidoc.returntype
     *      #return_array_begin()
     *           $SystemEventDetailsDtoSerializer
     *      #array_end()
    */
    @ReadOnly
    public SystemEventDetailsDto getEventDetails(User loggedInUser, Integer sid, Integer eid) {

        final Server server = lookupServer(loggedInUser, sid);
        final SystemEventDetailsDto eventDetail = SystemManager.systemEventDetails(server.getId(),
                loggedInUser.getOrg().getId(), eid.longValue());

        if (eventDetail == null) {
            throw new NoSuchHistoryEventException("No such history event for server - sid = " + sid + ", eid = " + eid);
        }

        if (eventDetail.getHistoryType() != null) {
            // This is an action related entry this we can extract additional information
            final Action action = ActionManager.lookupAction(loggedInUser, eventDetail.getId());
            final ServerAction serverAction = ActionFactory.getServerActionForServerAndAction(server, action);

            eventDetail.setEarliestAction(action.getEarliestAction());
            eventDetail.setResultMsg(serverAction.getResultMsg());
            eventDetail.setResultCode(serverAction.getResultCode());

            eventDetail.setAdditionalInfo(createActionSpecificDetails(action, serverAction));
        }

        return eventDetail;
    }

    /**
     * Returns a list of all errata that are relevant to the system.
     *
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the errata that can be applied
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Returns a list of all errata that are relevant to the system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ErrataOverview> getRelevantErrata(User loggedInUser, Integer sid) {

        Server server = lookupServer(loggedInUser, sid);
        DataResult<ErrataOverview> dr = SystemManager.relevantErrata(
                loggedInUser, server.getId());
        return dr;
    }

    /**
     * Returns a list of all errata of the specified type that are relevant to the system.
     * @param loggedInUser The current user
     * @param sid serverId
     * @param advisoryType The type of advisory (one of the following:
     * "Security Advisory", "Product Enhancement Advisory",
     * "Bug Fix Advisory")
     * @return Returns an array of maps representing errata relevant to the system.
     *
     * @throws FaultException A FaultException is thrown if a valid user can not be found
     * from the passed in session key or if the server corresponding to the serverId
     * cannot be found.
     *
     * @apidoc.doc Returns a list of all errata of the specified type that are
     * relevant to the system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "advisoryType", "type of advisory (one of
     * of the following: 'Security Advisory', 'Product Enhancement Advisory',
     * 'Bug Fix Advisory'")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ErrataOverview> getRelevantErrataByType(User loggedInUser, Integer sid,
            String advisoryType) throws FaultException {

        Server server = lookupServer(loggedInUser, sid);

        DataResult<ErrataOverview> dr = SystemManager.relevantErrataByType(loggedInUser,
                server.getId(), advisoryType);

        return dr;
    }

    /**
     * Lists all the relevant unscheduled errata for a system.
     *
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the errata that can be applied
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Provides an array of errata that are applicable to a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataSerializer
     *      #array_end()
     */
    @ReadOnly
    public Errata[] getUnscheduledErrata(User loggedInUser, Integer sid) {

        Server server = lookupServer(loggedInUser, sid);
        DataResult<Errata> dr = SystemManager.unscheduledErrata(loggedInUser,
                server.getId(), null);
        dr.elaborate();
        return dr.toArray(new Errata []{});
    }

    /**
     * Schedules an action to apply errata updates to multiple systems.
     * @param loggedInUser The current user
     * @param sids List of server IDs to apply the errata to (as Integers)
     * @param errataIds List of errata IDs to apply (as Integers)
     * @return list of action ids, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Schedules an action to apply errata updates to multiple systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids,
            List<Integer> errataIds) {
        return scheduleApplyErrata(loggedInUser, sids, errataIds, null, false);
    }

    /**
     * Schedules an action to apply errata updates to multiple systems.
     * @param loggedInUser The current user
     * @param sids List of server IDs to apply the errata to (as Integers)
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param allowModules Allow this API call, despite modular content being present
     * @return list of action ids, exception thrown otherwise
     * @since 21
     *
     * @apidoc.doc Schedules an action to apply errata updates to multiple systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids,
                                          List<Integer> errataIds, Boolean allowModules) {
        return scheduleApplyErrata(loggedInUser, sids, errataIds, null, allowModules);
    }

    /**
     * Schedules an action to apply errata updates to multiple systems at a specified time.
     * @param loggedInUser The current user
     * @param sids List of server IDs to apply the errata to (as Integers)
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param earliestOccurrence Earliest occurrence of the errata update
     * @return list of action ids, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Schedules an action to apply errata updates to multiple systems at a
     * given date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids,
            List<Integer> errataIds, Date earliestOccurrence) {

        return scheduleApplyErrata(loggedInUser, sids, errataIds, earliestOccurrence, false);
    }

    /**
     * Schedules an action to apply errata updates to multiple systems at a specified time.
     * @param loggedInUser The current user
     * @param sids List of server IDs to apply the errata to (as Integers)
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param earliestOccurrence Earliest occurrence of the errata update
     * @param allowModules Allow this API call, despite modular content being present
     * @return list of action ids, exception thrown otherwise
     * @since 21
     *
     * @apidoc.doc Schedules an action to apply errata updates to multiple systems at a
     * given date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids, List<Integer> errataIds,
                                          Date earliestOccurrence, Boolean allowModules) {

        return scheduleApplyErrata(loggedInUser, sids, errataIds, earliestOccurrence, allowModules, true);
    }

    /**
     * Schedules an action to apply errata updates to multiple systems at a specified time.
     * @param loggedInUser The current user
     * @param sids List of server IDs to apply the errata to (as Integers)
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param earliestOccurrence Earliest occurrence of the errata update
     * @param allowModules Allow this API call, despite modular content being present
     * @param onlyRelevant If true not all erratas are applied to all systems.
     *        Systems get only the erratas relevant for them.
     * @return list of action ids, exception thrown otherwise
     * @since 24
     *
     * @apidoc.doc Schedules an action to apply errata updates to multiple systems at a
     * given date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.param #param_desc("boolean", "onlyRelevant",
     *          "If true not all erratas are applied to all systems. Systems get only the erratas relevant for them.")
     * @param allowVendorChange boolean
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids, List<Integer> errataIds,
                                          Date earliestOccurrence, Boolean allowModules,
                                          Boolean onlyRelevant, Boolean allowVendorChange) {

        // we need long values to pass to ErrataManager.applyErrataHelper
        List<Long> serverIds = sids.stream()
            .map(Integer::longValue)
            .collect(toList());

        if (!allowModules) {
            for (Long sid : serverIds) {
                Server server = SystemManager.lookupByIdAndUser(sid, loggedInUser);
                for (Channel channel : server.getChannels()) {
                    if (channel.getModules() != null) {
                        throw new ModulesNotAllowedException();
                    }
                }
            }
        }
        List<Long> eids = errataIds.stream()
            .map(Integer::longValue)
            .collect(toList());

        try {
            return ErrataManager.applyErrataHelper(loggedInUser,
                    serverIds, eids, earliestOccurrence, onlyRelevant, allowVendorChange);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Schedules an action to apply errata updates to a system.
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param errataIds List of errata IDs to apply (as Integers)
     * @return list of action ids, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Schedules an action to apply errata updates to a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, Integer sid,
            List<Integer> errataIds) {
        List<Integer> serverIds = new ArrayList<>();
        serverIds.add(sid);

        return scheduleApplyErrata(loggedInUser, serverIds, errataIds, false);
    }

    /**
     * Schedules an action to apply errata updates to a system.
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param allowModules Allow this API call, despite modular content being present
     * @return list of action ids, exception thrown otherwise
     * @since 21
     *
     * @apidoc.doc Schedules an action to apply errata updates to a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, Integer sid, List<Integer> errataIds,
                                          Boolean allowModules) {
        List<Integer> serverIds = new ArrayList<>();
        serverIds.add(sid);

        return scheduleApplyErrata(loggedInUser, serverIds, errataIds, allowModules);
    }

    /**
     * Schedules an action to apply errata updates to a system at a specified time.
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param earliestOccurrence Earliest occurrence of the errata update
     * @return list of action ids, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Schedules an action to apply errata updates to a system at a
     * given date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, Integer sid,
            List<Integer> errataIds, Date earliestOccurrence) {
        List<Integer> serverIds = new ArrayList<>();
        serverIds.add(sid);

        return scheduleApplyErrata(loggedInUser, serverIds, errataIds, earliestOccurrence, false);
    }

    /**
     * Schedules an action to apply errata updates to a system at a specified time.
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param earliestOccurrence Earliest occurrence of the errata update
     * @param allowModules Allow this API call, despite modular content being present
     * @return list of action ids, exception thrown otherwise
     * @since 21
     *
     * @apidoc.doc Schedules an action to apply errata updates to a system at a
     * given date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, Integer sid, List<Integer> errataIds,
                                          Date earliestOccurrence, Boolean allowModules) {
        List<Integer> serverIds = new ArrayList<>();
        serverIds.add(sid);

        return scheduleApplyErrata(loggedInUser, serverIds, errataIds, earliestOccurrence, allowModules);
    }

   /**
     * Schedules an action to apply errata updates to a a list of systems at a specified time.
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param earliestOccurrence Earliest occurrence of the errata update
     * @param allowModules Allow this API call, despite modular content being present
     * @param onlyRelevant boolean
     * @return list of action ids, exception thrown otherwise
     * @since 24
     *
     * @apidoc.doc Schedules an action to apply errata updates to a system at a
     * given date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.param #param("boolean", "onlyRelevant")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sid, List<Integer> errataIds,
                                         Date earliestOccurrence, Boolean allowModules, Boolean onlyRelevant) {
        return scheduleApplyErrata(loggedInUser, sid, errataIds, earliestOccurrence, allowModules,
                onlyRelevant, false);

}
    /**
     * Compares the packages installed on two systems.
     *
     * @param loggedInUser The current user
     * @param sid1 This system's ID
     * @param sid2 Other system's ID
     * @return Array of PackageMetadata
     *
     * @apidoc.doc Compares the packages installed on two systems.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid1")
     * @apidoc.param #param("int", "sid2")
     * @apidoc.returntype
     *          #return_array_begin()
     *              $PackageMetadataSerializer
     *          #array_end()
     *
     */
    public Object [] comparePackages(User loggedInUser, Integer sid1, Integer sid2) {

        Server target = null;
        Server source = null;
        try {
            target = SystemManager.lookupByIdAndUser(sid1.longValue(),
                    loggedInUser);
            source = SystemManager.lookupByIdAndUser(sid2.longValue(),
                    loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        // Check that the two systems are compatible for comparison:
        if (!isCompatible(loggedInUser, target, source)) {
            throw new InvalidSystemException();
        }

        DataResult result = null;
        try {
            result = ProfileManager.compareServerToServer(
                    sid1.longValue(),
                    sid2.longValue(), loggedInUser.getOrg().getId(), null);
        }
        catch (MissingEntitlementException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingEntitlementException();
        }
        return result.toArray();
    }

    /**
     * Gets the hardware profile of a specific system
     *
     * @param loggedInUser The current user
     * @param sid This system's ID
     * @return Map contianing the DMI information of the system
     *
     * @apidoc.doc Gets the DMI information of a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      $DmiSerializer
     */
    @ReadOnly
    public Object getDmi(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        Dmi dmi = server.getDmi();
        if (dmi == null) {
            return new HashMap<String, String>();
        }
        return dmi;
    }

    /**
     * Gets the hardware profile of a specific system
     *
     * @param loggedInUser The current user
     * @param sid This system's ID
     * @return Map contianing the CPU info of the system
     *
     * @apidoc.doc Gets the CPU information of a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      $CpuSerializer
     */
    @ReadOnly
    public Object getCpu(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        CPU cpu = server.getCpu();
        if (cpu == null) {
            return new HashMap<String, String>();
        }
        return cpu;
    }

    /**
     * Gets the memory information of a specific system
     *
     * @param loggedInUser The current user
     * @param sid This system's ID
     * @return Map containing the memory profile
     *
     * @apidoc.doc Gets the memory information for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *  #struct_begin("memory")
     *      #prop_desc("int", "ram", "The amount of physical memory in MB.")
     *      #prop_desc("int", "swap", "The amount of swap space in MB.")
     *  #struct_end()
     */
    @ReadOnly
    public Map<String, Long> getMemory(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        Map<String, Long> memory = new HashMap<>();
        memory.put("swap", server.getSwap());
        memory.put("ram", server.getRam());
        return memory;
    }

    /**
     * Provides an array of devices for a system
     *
     * @param loggedInUser The current user
     * @param sid This system's ID
     * @return array continaing device Maps
     *
     * @apidoc.doc Gets a list of devices for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *          #return_array_begin()
     *              $DeviceSerializer
     *              #array_end()
     */
    @ReadOnly
    public Object[] getDevices(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        Set<Device> devices = server.getDevices();
        return devices.toArray();
    }


    /**
     * Private helper method is called by other methods to perform package action.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param packageMaps List of maps with packages metadata "name_id", "evr_id", "arch_id"
     * @param earliestOccurrence Earliest occurrence of the package install
     * @return package action id
     */
    private Long[] schedulePackagesAction(User loggedInUser, List<Integer> sids,
            List<Map<String, Long>> packageMaps, Date earliestOccurrence, ActionType acT, Boolean allowModules) {

        List<Long> actionIds = new ArrayList<>();

        if (!allowModules) {
            boolean hasModules = false;
            for (Integer sid : sids) {
                Server server = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);
                for (Channel channel : server.getChannels()) {
                    if (channel.getModules() != null) {
                        hasModules = true;
                        break;
                    }
                }
            }
            if (hasModules) {
                throw new ModulesNotAllowedException();
            }
        }

        List<Package> packages = packageMaps.stream().flatMap(packageMap -> {
            Org org = loggedInUser.getOrg();
            Long nameId = packageMap.get("name_id");
            Long evrId = packageMap.get("evr_id");
            Long archId = packageMap.get("arch_id");

            return PackageFactory.lookupByNevraIds(org, nameId, evrId, archId).stream();
        }).collect(toList());

        if (ActionFactory.TYPE_PACKAGES_UPDATE.equals(acT)) {
            List<Tuple2<Long, Long>> pidsidpairs = ErrataFactory.retractedPackages(
                    packages.stream().map(Package::getId).collect(toList()),
                    sids.stream().map(Integer::longValue).collect(toList())
            );
            if (!pidsidpairs.isEmpty()) {
                throw new RetractedPackageFault(pidsidpairs.stream().map(Tuple2::getA).collect(toList()));
            }
        }

        // Check if the package is part of a PTF. If true it cannot be manually installed/updated/ removed
        List<Long> ptfPackages = packages.stream().filter(Package::isPartOfPtf).map(Package::getId).collect(toList());
        if (!ptfPackages.isEmpty()) {
            throw new PtfPackageFault(ptfPackages);
        }

        // PTF master packages cannot be removed
        if (ActionFactory.TYPE_PACKAGES_REMOVE.equals(acT)) {
            List<Long> ptfMasterPackages = packages.stream()
                                                   .filter(Package::isMasterPtfPackage)
                                                   .map(Package::getId)
                                                   .collect(toList());
            if (!ptfMasterPackages.isEmpty()) {
                throw new PtfMasterFault(ptfMasterPackages);
            }
        }

        for (Integer sid : sids) {
            Server server = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);

            // Would be nice to do this check at the Manager layer but upset many tests,
            // some of which were not cooperative when being fixed. Placing here for now.
            if (!(SystemManager.hasEntitlement(server.getId(),
                    EntitlementManager.MANAGEMENT) ||
                    SystemManager.hasEntitlement(server.getId(),
                    EntitlementManager.SALT))) {
                throw new MissingEntitlementException(
                        EntitlementManager.MANAGEMENT.getHumanReadableLabel() + " or " +
                        EntitlementManager.SALT.getHumanReadableLabel());
            }

            Action action = null;
            try {

                action = ActionManager.schedulePackageAction(loggedInUser, packageMaps, acT,
                        earliestOccurrence, server);
            }
            catch (MissingEntitlementException e) {
                throw new com.redhat.rhn.frontend.xmlrpc.MissingEntitlementException();
            }
            catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
                throw new TaskomaticApiException(e.getMessage());
            }

            actionIds.add(action.getId());
        }
        return actionIds.toArray(new Long[0]);
    }


    /**
     * Private helper method is called by other methods to perform package action.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param earliestOccurrence Earliest occurrence of the package install
     * @return package action id
     */
    private Long schedulePackagesUpdateAction(User loggedInUser, List<Integer> sids,
            Date earliestOccurrence, ActionType acT) {
        HashSet<Long> lsids = new HashSet<>();
        for (Integer sid : sids) {
            Server server;
            try {
                server = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);
            }
            catch (LookupException e) {
                throw new NoSuchSystemException(e.getMessage());
            }

            // Would be nice to do this check at the Manager layer but upset many tests,
            // some of which were not cooperative when being fixed. Placing here for now.
            if (!SystemManager.hasEntitlement(server.getId(), EntitlementManager.SALT)) {
                throw new MissingEntitlementException(
                        EntitlementManager.SALT.getHumanReadableLabel());
            }
            lsids.add(server.getId());
        }

        try {
            return ActionManager.schedulePackageAction(loggedInUser, null, acT,
                    earliestOccurrence, lsids).getId();
        }
        catch (MissingEntitlementException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingEntitlementException();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Private helper method to build a list of maps in the format the ActionManager wants.
     *
     * @param user The current user
     * @param packageIds List of package IDs to install (as Integers)
     * @return list of maps with packages metadata in format ActionManager wants
     */
    private List<Map<String, Long>> packageIdsToMaps(User user, List<Integer> packageIds) {

        List<Map<String, Long>> packageMaps = new LinkedList<>();

        for (Integer pkgId : packageIds) {

            Map<String, Long> pkgMap = new HashMap<>();

            Package p = PackageManager.lookupByIdAndUser(pkgId.longValue(), user);

            if (p == null) {
                throw new InvalidPackageException("cannot find package with name " +
                        pkgId + " in Satellite database");
            }

            pkgMap.put("name_id", p.getPackageName().getId());
            pkgMap.put("evr_id", p.getPackageEvr().getId());
            pkgMap.put("arch_id", p.getPackageArch().getId());
            packageMaps.add(pkgMap);
        }

        if (packageMaps.isEmpty()) {
            throw new InvalidParameterException("No packages to install/remove.");
        }

        return packageMaps;
    }

    /**
     * Private helper method to build a list of maps in the format the ActionManager wants.
     *
     * @param user The current user
     * @param packageNevraList array of dictionaries with package nevra
     * @return list of maps with packages metadata in format ActionManager wants
     */
    private List<Map<String, Long>> packageNevrasToMaps(User user,
            List<Map<String, String>> packageNevraList, Boolean lookupNevra) {

        List<Map<String, Long>> packageMaps = new LinkedList<>();

        for (Map<String, String> packageNevra : packageNevraList) {

            Map<String, Long> pkgMap = new HashMap<>();
            PackageArch arch = PackageFactory
                    .lookupPackageArchByLabel(packageNevra.get("package_arch"));

            String epoch = packageNevra.get("package_epoch");

            if (arch == null) {
                throw new InvalidPackageArchException(packageNevra.get("package_arch"));
            }
            if (epoch != null && epoch.equals("")) {
                epoch = null;
            }

            List<Package> pl = PackageFactory.lookupByNevra(user.getOrg(),
                    packageNevra.get("package_name"), packageNevra.get("package_version"),
                    packageNevra.get("package_release"), epoch, arch);

            if (pl == null || pl.isEmpty()) {
                PackageName pkgName =  PackageFactory.lookupPackageName(packageNevra.get("package_name"));
                if (pkgName == null || !lookupNevra) {
                    throw new InvalidPackageException(packageNevra.get("package_name"));
                }
                pkgMap.put("name_id", pkgName.getId());
                pkgMap.put("evr_id", PackageEvrFactory.lookupOrCreatePackageEvr(epoch,
                        packageNevra.get("package_version"), packageNevra.get("package_release"),
                        arch.getArchType().getPackageType()).getId());
                pkgMap.put("arch_id", arch.getId());
            }
            // in case if we have more than one package with
            // the same nevra we pick up the first one
            else {
                pkgMap.put("name_id", pl.get(0).getPackageName().getId());
                pkgMap.put("evr_id", pl.get(0).getPackageEvr().getId());
                pkgMap.put("arch_id", pl.get(0).getPackageArch().getId());
            }
            packageMaps.add(pkgMap);
        }

        if (packageMaps.isEmpty()) {
            throw new InvalidParameterException("No packages to install/remove.");
        }
        return packageMaps;
    }

    /**
     * Schedule package installation for several systems.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param packageIds List of package IDs to install (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package install
     * @return package action id
     *
     * @apidoc.doc Schedule package installation for several systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public Long[] schedulePackageInstall(User loggedInUser, List<Integer> sids,
            List<Integer> packageIds, Date earliestOccurrence) {

        return schedulePackageInstall(loggedInUser, sids,
                packageIds, earliestOccurrence, false);
    }

    /**
     * Schedule package installation for several systems.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param packageIds List of package IDs to install (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package install
     * @param allowModules Allow this API call, despite modular content being present
     * @return package action id
     * @since 21
     *
     * @apidoc.doc Schedule package installation for several systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public Long[] schedulePackageInstall(User loggedInUser, List<Integer> sids,
                                         List<Integer> packageIds, Date earliestOccurrence, Boolean allowModules) {

        List<Tuple2<Long, Long>> retracted = ErrataFactory.retractedPackages(
                packageIds.stream().map(Integer::longValue).collect(toList()),
                sids.stream().map(Integer::longValue).collect(toList()));

        List<Long> retractedPids = retracted.stream()
                .map(Tuple2::getA)
                .collect(toList());
        if (retracted.isEmpty()) {
            return schedulePackagesAction(loggedInUser, sids,
                    packageIdsToMaps(loggedInUser, packageIds), earliestOccurrence,
                    ActionFactory.TYPE_PACKAGES_UPDATE, allowModules);
        }
        else {
            throw new RetractedPackageFault(retractedPids);
        }
    }

    /**
     * Schedule full package update for several systems.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param earliestOccurrence Earliest occurrence of the package install
     * @return package action id
     * @since 25
     *
     * @apidoc.doc Schedule full package update for several systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param("int", "actionId")
     */
    public Long schedulePackageUpdate(User loggedInUser, List<Integer> sids,
                                        Date earliestOccurrence) {

        return schedulePackagesUpdateAction(loggedInUser, sids,
                earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_UPDATE);
    }

    /**
     * Schedule package installation for a system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param packageIds List of package IDs to install (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package install
     * @return package action id
     * @since 13.0
     *
     * @apidoc.doc Schedule package installation for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long schedulePackageInstall(User loggedInUser, final Integer sid,
            List<Integer> packageIds, Date earliestOccurrence) {
            return schedulePackageInstall(loggedInUser, Collections.singletonList(sid),
                    packageIds, earliestOccurrence, false)[0];
    }

    /**
     * Schedule package installation for a system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param packageIds List of package IDs to install (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package install
     * @param allowModules Allow this API call, despite modular content being present
     * @return package action id
     * @since 21
     *
     * @apidoc.doc Schedule package installation for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long schedulePackageInstall(User loggedInUser, final Integer sid,
                                       List<Integer> packageIds, Date earliestOccurrence, Boolean allowModules) {
        return schedulePackageInstall(loggedInUser, Collections.singletonList(sid),
                packageIds, earliestOccurrence, allowModules)[0];
    }

    /**
    * Schedule package installation for several systems.
    *
    * @param loggedInUser The current user
    * @param sids IDs of the servers
    * @param packageNevraList array of dictionaries with package nevra
    * @param earliestOccurrence Earliest occurrence of the package install
    * @return package action id
    *
    * @apidoc.doc Schedule package installation for several systems.
    * @apidoc.param #session_key()
    * @apidoc.param #array_single("int", "sids")
    * @apidoc.param #array_begin("packageNevraList")
    *                   #struct_begin("Package Nevra")
    *                          #prop("string", "package_name")
    *                          #prop("string", "package_epoch")
    *                          #prop("string", "package_version")
    *                          #prop("string", "package_release")
    *                          #prop("string", "package_arch")
    *
    *                   #struct_end()
    *               #array_end()
    * @apidoc.param #param("$date", "earliestOccurrence")
    * @apidoc.returntype #array_single("int", "actionId")
    */
    public Long[] schedulePackageInstallByNevra(User loggedInUser, List<Integer> sids,
            List<Map<String, String>> packageNevraList, Date earliestOccurrence) {

        return schedulePackageInstallByNevra(loggedInUser, sids,
                packageNevraList, earliestOccurrence, false);
    }

    /**
     * Schedule package installation for several systems.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param packageNevraList array of dictionaries with package nevra
     * @param earliestOccurrence Earliest occurrence of the package install
     * @param allowModules Allow this API call, despite modular content being present
     * @return package action id
     * @since 21
     *
     * @apidoc.doc Schedule package installation for several systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_begin("packageNevraList")
     *                   #struct_begin("Package nevra")
     *                          #prop("string", "package_name")
     *                          #prop("string", "package_epoch")
     *                          #prop("string", "package_version")
     *                          #prop("string", "package_release")
     *                          #prop("string", "package_arch")
     *
     *                   #struct_end()
     *               #array_end()
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public Long[] schedulePackageInstallByNevra(User loggedInUser, List<Integer> sids, List<Map<String,
            String>> packageNevraList, Date earliestOccurrence, Boolean allowModules) {

        return schedulePackagesAction(loggedInUser, sids,
                packageNevrasToMaps(loggedInUser, packageNevraList, false), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_UPDATE, allowModules);
    }

    /**
    * Schedule package installation for a system.
    *
    * @param loggedInUser The current user
    * @param sid ID of the server
    * @param packageNevraList array of dictionaries with package nevra
    * @param earliestOccurrence Earliest occurrence of the package install
    * @return package action id
    *
    * @apidoc.doc Schedule package installation for a system.
    * @apidoc.param #session_key()
    * @apidoc.param #param("int", "sid")
    * @apidoc.param #array_begin("packageNevraList")
    *                   #struct_begin("Package nevra")
    *                          #prop("string", "package_name")
    *                          #prop("string", "package_epoch")
    *                          #prop("string", "package_version")
    *                          #prop("string", "package_release")
    *                          #prop("string", "package_arch")
    *
    *                   #struct_end()
    *               #array_end()
    * @apidoc.param #param("$date", "earliestOccurrence")
    * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
    */
    public Long schedulePackageInstallByNevra(User loggedInUser, final Integer sid,
            List<Map<String, String>> packageNevraList, Date earliestOccurrence) {

        return schedulePackageInstallByNevra(loggedInUser, Collections.singletonList(sid),
                packageNevraList, earliestOccurrence, false)[0];
    }

    /**
     * Schedule package installation for a system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param packageNevraList array of dictionaries with package nevra
     * @param earliestOccurrence Earliest occurrence of the package install
     * @param allowModules Allow this API call, despite modular content being present
     * @return package action id
     * @since 21
     *
     * @apidoc.doc Schedule package installation for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_begin("packageNevraList")
     *                   #struct_begin("Package nevra")
     *                          #prop("string", "package_name")
     *                          #prop("string", "package_epoch")
     *                          #prop("string", "package_version")
     *                          #prop("string", "package_release")
     *                          #prop("string", "package_arch")
     *
     *                   #struct_end()
     *               #array_end()
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *              "Allow this API call, despite modular content being present")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long schedulePackageInstallByNevra(User loggedInUser, final Integer sid, List<Map<String,
            String>> packageNevraList, Date earliestOccurrence, Boolean allowModules) {

        return schedulePackageInstallByNevra(loggedInUser, Collections.singletonList(sid),
                packageNevraList, earliestOccurrence, allowModules)[0];
    }

    /**
     * Schedule package removal for several systems.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param packageIds List of package IDs to install (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package removal
     * @return package action id
     *
     * @apidoc.doc Schedule package removal for several systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public Long[] schedulePackageRemove(User loggedInUser, List<Integer> sids,
            List<Integer> packageIds, Date earliestOccurrence) {

        return schedulePackagesAction(loggedInUser, sids,
                packageIdsToMaps(loggedInUser, packageIds), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_REMOVE, false);
    }

    /**
     * Schedule package removal for several systems.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param packageIds List of package IDs to install (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package removal
     * @param allowModules Allow this API call, despite modular content being present
     * @return package action id
     * @since 21
     *
     * @apidoc.doc Schedule package removal for several systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public Long[] schedulePackageRemove(User loggedInUser, List<Integer> sids,
                                        List<Integer> packageIds, Date earliestOccurrence, Boolean allowModules) {

        return schedulePackagesAction(loggedInUser, sids,
                packageIdsToMaps(loggedInUser, packageIds), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_REMOVE, allowModules);
    }

    /**
     * Schedule package removal for a system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param packageIds List of package IDs to remove (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package removal
     * @return 1 if successful, exception thrown otherwise
     *
     * @apidoc.doc Schedule package removal for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public int schedulePackageRemove(User loggedInUser, Integer sid,
            List<Integer> packageIds, Date earliestOccurrence) {

        List<Integer> sids = new ArrayList<>();
        sids.add(sid);

        return schedulePackagesAction(loggedInUser, sids,
                packageIdsToMaps(loggedInUser, packageIds), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_REMOVE, false)[0].intValue();
    }

    /**
     * Schedule package removal for a system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param packageIds List of package IDs to remove (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package removal
     * @param allowModules Allow this API call, despite modular content being present
     * @return 1 if successful, exception thrown otherwise
     * @since 21
     *
     * @apidoc.doc Schedule package removal for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public int schedulePackageRemove(User loggedInUser, Integer sid,
                                     List<Integer> packageIds, Date earliestOccurrence, Boolean allowModules) {

        List<Integer> sids = new ArrayList<>();
        sids.add(sid);

        return schedulePackagesAction(loggedInUser, sids,
                packageIdsToMaps(loggedInUser, packageIds), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_REMOVE, allowModules)[0].intValue();
    }

    /**
     * Schedule package removal for several systems using it's nevra.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param packageNevraList array of dictionaries with package nevra
     * @param earliestOccurrence Earliest occurrence of the package removal
     * @return package action id
     *
     * @apidoc.doc Schedule package removal for several systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_begin("packageNevraList")
     *                   #struct_begin("Package nevra")
     *                          #prop("string", "package_name")
     *                          #prop("string", "package_epoch")
     *                          #prop("string", "package_version")
     *                          #prop("string", "package_release")
     *                          #prop("string", "package_arch")
     *
     *                   #struct_end()
     *               #array_end()
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public Long[] schedulePackageRemoveByNevra(User loggedInUser, List<Integer> sids,
            List<Map<String, String>> packageNevraList, Date earliestOccurrence) {

        return schedulePackagesAction(loggedInUser, sids,
                packageNevrasToMaps(loggedInUser, packageNevraList, true), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_REMOVE, false);
    }

    /**
     * Schedule package removal for several systems using it's nevra.
     *
     * @param loggedInUser The current user
     * @param sids IDs of the servers
     * @param packageNevraList array of dictionaries with package nevra
     * @param earliestOccurrence Earliest occurrence of the package removal
     * @param allowModules Allow this API call, despite modular content being present
     * @return package action id
     * @since 21
     *
     * @apidoc.doc Schedule package removal for several systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_begin("packageNevraList")
     *                   #struct_begin("Package nevra")
     *                          #prop("string", "package_name")
     *                          #prop("string", "package_epoch")
     *                          #prop("string", "package_version")
     *                          #prop("string", "package_release")
     *                          #prop("string", "package_arch")
     *
     *                   #struct_end()
     *               #array_end()
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public Long[] schedulePackageRemoveByNevra(User loggedInUser, List<Integer> sids, List<Map<String,
            String>> packageNevraList, Date earliestOccurrence, Boolean allowModules) {

        return schedulePackagesAction(loggedInUser, sids,
                packageNevrasToMaps(loggedInUser, packageNevraList, true), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_REMOVE, allowModules);
    }

    /**
    * Schedule package removal for a system using it's nevra.
    *
    * @param loggedInUser The current user
    * @param sid ID of the server
    * @param packageNevraList array of dictionaries with package nevra
    * @param earliestOccurrence Earliest occurrence of the package removal
    * @return package action id
    *
    * @apidoc.doc Schedule package removal for a system.
    * @apidoc.param #session_key()
    * @apidoc.param #param("int", "sid")
    * @apidoc.param #array_begin("packageNevraList")
    *                   #struct_begin("Package nevra")
    *                          #prop("string", "package_name")
    *                          #prop("string", "package_epoch")
    *                          #prop("string", "package_version")
    *                          #prop("string", "package_release")
    *                          #prop("string", "package_arch")
    *
    *                   #struct_end()
    *               #array_end()
    * @apidoc.param #param("$date", "earliestOccurrence")
    * @apidoc.returntype #array_single("int", "actionId")
    */
    public int schedulePackageRemoveByNevra(User loggedInUser, final Integer sid,
            List<Map<String, String>> packageNevraList, Date earliestOccurrence) {

        List<Integer> sids = new ArrayList<>();
        sids.add(sid);

        return schedulePackagesAction(loggedInUser, sids,
                packageNevrasToMaps(loggedInUser, packageNevraList, true), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_REMOVE, false)[0].intValue();
    }

    /**
     * Schedule package removal for a system using it's nevra.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param packageNevraList array of dictionaries with package nevra
     * @param earliestOccurrence Earliest occurrence of the package removal
     * @param allowModules Allow this API call, despite modular content being present
     * @return package action id
     * @since 21
     *
     * @apidoc.doc Schedule package removal for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_begin("packageNevraList")
     *                   #struct_begin("Package nevra")
     *                          #prop("string", "package_name")
     *                          #prop("string", "package_epoch")
     *                          #prop("string", "package_version")
     *                          #prop("string", "package_release")
     *                          #prop("string", "package_arch")
     *
     *                   #struct_end()
     *               #array_end()
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "allowModules",
     *          "Allow this API call, despite modular content being present")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public int schedulePackageRemoveByNevra(User loggedInUser, final Integer sid, List<Map<String,
            String>> packageNevraList, Date earliestOccurrence, Boolean allowModules) {

        List<Integer> sids = new ArrayList<>();
        sids.add(sid);

        return schedulePackagesAction(loggedInUser, sids,
                packageNevrasToMaps(loggedInUser, packageNevraList, true), earliestOccurrence,
                ActionFactory.TYPE_PACKAGES_REMOVE, allowModules)[0].intValue();
    }

    /**
     * Schedule package lock for a system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server
     * @param pkgIdsToLock List of package IDs to lock (as Integers)
     * @param pkgIdsToUnlock List of package IDs to lock (as Integers)
     * @param earliestOccurrence Earliest occurrence of the package removal
     * @return package action id
     *
     * @apidoc.doc Schedule package lock for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("int", "pkgIdsToLock")
     * @apidoc.param #array_single("int", "pkgIdsToUnlock")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #return_int_success()
     */
    public Long schedulePackageLockChange(User loggedInUser, Integer sid,
                                          List<Integer> pkgIdsToLock,
                                          List<Integer> pkgIdsToUnlock, Date earliestOccurrence) {

        Server server = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);
        DataResult<PackageListItem> lockedPackagesResult =
                PackageManager.systemLockedPackages(server.getId(), null);

        Set<Package> pkgsAlreadyLocked = new HashSet<>();
        List<Package> pkgsFindAlreadyLocked = PackageManager.lookupByIdAndUser(
                lockedPackagesResult.stream().map(PackageListItem::getPackageId)
                        .collect(Collectors.toList()), loggedInUser);
        pkgsAlreadyLocked.addAll(pkgsFindAlreadyLocked);

        Set<Package> pkgsToLock = new HashSet<>();
        List<Package> pkgsFindToLock = PackageManager.lookupByIdAndUser(pkgIdsToLock

                .stream().map(Integer::longValue).collect(toList()), loggedInUser);
        pkgsFindToLock.stream().filter(Objects::nonNull).forEach(pkgsToLock::add);

        pkgsToLock.removeAll(pkgsAlreadyLocked);
        pkgsToLock.forEach(x -> x.setLockPending(Boolean.TRUE));
        PackageManager.lockPackages(server.getId(), pkgsToLock);
        PackageManager.setPendingStatusOnLockedPackages(pkgsToLock, PackageManager.PKG_PENDING_LOCK);

        Set<Package> pkgsToUnlock = new HashSet<>();
        List<Package> pkgsFindToUnlock = PackageManager.lookupByIdAndUser(pkgIdsToUnlock
                .stream().map(Integer::longValue).collect(toList()), loggedInUser);
        pkgsFindToUnlock.stream().filter(Objects::nonNull).forEach(pkgsToUnlock::add);

        pkgsToUnlock.forEach(x -> x.setLockPending(Boolean.TRUE));
        PackageManager.setPendingStatusOnLockedPackages(pkgsToUnlock, PackageManager.PKG_PENDING_UNLOCK);

        Set<Package> allPkgsWithAction = new HashSet<>();
        allPkgsWithAction.addAll(pkgsToLock);
        allPkgsWithAction.addAll(pkgsAlreadyLocked);

        try {
            // we should schedule an action for all the packages
            Action a = ActionManager.schedulePackageLock(loggedInUser, allPkgsWithAction, earliestOccurrence, server);
            return a.getId();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }
    /**
     * Lists all of the notes that are associated with a system.
     *   If no notes are found it should return an empty set.
     * @param loggedInUser The current user
     * @param sid the system id
     * @return Array of Note objects associated with the given system
     *
     * @apidoc.doc Provides a list of notes associated with a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *  #return_array_begin()
     *      $NoteSerializer
     *  #array_end()
     */
    @ReadOnly
    public Set<Note> listNotes(User loggedInUser , Integer sid) {
        Server server = SystemManager.lookupByIdAndUser(sid.longValue(),
                loggedInUser);
        return server.getNotes();
    }

    /**
     * Lists all of the FQDNs that are associated with a system.
     * If no FQDNs are found it should return an empty set.
     * @param loggedInUser The current user
     * @param sid the system id
     * @return Array of FQDNs objects associated with the given system
     *
     * @apidoc.doc Provides a list of FQDNs associated with a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #array_single("string", "fqdn")
     */
    @ReadOnly
    public List<String> listFqdns(User loggedInUser, Integer sid) {
        return ServerFactory.listFqdns(sid.longValue());
    }

    /**
     * Lists all of the packages that are installed on a system that also belong
     *  to a particular channel.  NOTE: when the arch for an installed package is
     *  unavailable we do not take it into concern, meaning that it is arch unaware.
     *  This is usually the case for RHEL 4 or older.  RHEL 5 started uploading
     *  arch information, so that information is taken into account when matching
     *  packages.
     * @param loggedInUser The current user
     * @param sid the system Id
     * @param channelLabel the channel label
     * @return Array of Package objects representing the intersection of the channel
     *          packages and the system's installed packages
     *
     *
     * @apidoc.doc Provides a list of packages installed on a system that are also
     *          contained in the given channel.  The installed package list did not
     *          include arch information before RHEL 5, so it is arch unaware.  RHEL 5
     *          systems do upload the arch information, and thus are arch aware.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "channelLabel")
     * @apidoc.returntype
     *  #return_array_begin()
     *      $PackageSerializer
     *  #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listPackagesFromChannel(User loggedInUser,
            Integer sid,
            String channelLabel) {
        SystemManager.lookupByIdAndUser(sid.longValue(),
                loggedInUser);
        Channel channel = ChannelFactory.lookupByLabelAndUser(channelLabel,
                loggedInUser);
        return SystemManager.packagesFromChannel(sid.longValue(), channel.getId());
    }

    /**
     * Schedule a hardware refresh for a system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server.
     * @param earliestOccurrence Earliest occurrence of the hardware refresh.
     * @return action id, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Schedule a hardware refresh for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleHardwareRefresh(User loggedInUser, Integer sid,
            Date earliestOccurrence) {
        Server server = SystemManager.lookupByIdAndUser(sid.longValue(),
                loggedInUser);

        try {
            Action a = ActionManager.scheduleHardwareRefreshAction(loggedInUser, server,
                    earliestOccurrence);
            Action action = ActionFactory.save(a);
            taskomaticApi.scheduleActionExecution(action);
            return action.getId();
        }
        catch (MissingEntitlementException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingEntitlementException();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Schedule a package list refresh for a system.
     *
     * @param loggedInUser The current user
     * @param sid ID of the server.
     * @param earliestOccurrence Earliest occurrence of the refresh.
     * @return the id of the action scheduled, exception thrown otherwise
     *
     * @apidoc.doc Schedule a package list refresh for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "id", "ID of the action scheduled, otherwise exception thrown
     * on error")
     */
    public int schedulePackageRefresh(User loggedInUser, Integer sid,
            Date earliestOccurrence) {
        Server server = SystemManager.lookupByIdAndUser(sid.longValue(),
                loggedInUser);

        try {
            Action a = ActionManager.schedulePackageRefresh(loggedInUser, server,
                    earliestOccurrence);
            ActionFactory.save(a);

            return a.getId().intValue();
        }
        catch (MissingEntitlementException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingEntitlementException();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Schedule a script to run.
     *
     * @param loggedInUser           The current user
     * @param label                  Text description
     * @param sids                   IDs of the servers to run the script on.
     * @param username               User to run script as.
     * @param groupname              Group to run script as.
     * @param timeout                Seconds to allow the script to run before timing out.
     * @param script                 Contents of the script to run.
     * @param earliestOccurrence     Earliest the script can run.
     * @return ID of the new script action.
     * @apidoc.doc Schedule a script to run.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param #array_single_desc("int", "sids", "System IDs of the servers to run the script on.")
     * @apidoc.param #param_desc("string", "username", "User to run script as.")
     * @apidoc.param #param_desc("string", "groupname", "Group to run script as.")
     * @apidoc.param #param_desc("int", "timeout", "Seconds to allow the script to run
     *before timing out.")
     * @apidoc.param #param_desc("string", "script", "Contents of the script to run.
     * Must start with a shebang (e.g. #!/bin/bash)")
     * @apidoc.param #param_desc("$date", "earliestOccurrence",
     * "Earliest the script can run.")
     * @apidoc.returntype #param_desc("int", "id", "ID of the script run action created. Can be used to fetch
     * results with system.getScriptResults")
     */
    public Integer scheduleScriptRun(User loggedInUser, String label, List<Integer>
            sids, String username, String groupname, Integer timeout, String script,
                                     Date earliestOccurrence) {

        ScriptActionDetails scriptDetails = ActionManager.createScript(username, groupname,
                timeout.longValue(), script);
        ScriptAction action = null;

        List<Long> servers = new ArrayList<>();

        for (Integer sidAsInt : sids) {
            Long sid = sidAsInt.longValue();
            try {
                SystemManager.lookupByIdAndUser(sid,
                        loggedInUser);
                servers.add(sid);
            }
            catch (LookupException e) {
                throw new NoSuchSystemException();
            }
        }

        try {
            action = ActionManager.scheduleScriptRun(loggedInUser, servers,
                    label, scriptDetails, earliestOccurrence);
        }
        catch (MissingCapabilityException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingCapabilityException();
        }
        catch (MissingEntitlementException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingEntitlementException();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }

        return action.getId().intValue();
    }

    /**
     * Schedule a script to run.
     *
     * @param loggedInUser           The current user
     * @param sids                   IDs of the servers to run the script on.
     * @param username               User to run script as.
     * @param groupname              Group to run script as.
     * @param timeout                Seconds to allow the script to run before timing out.
     * @param script                 Contents of the script to run.
     * @param earliestOccurrence     Earliest the script can run.
     * @return ID of the new script action.
     * @apidoc.doc Schedule a script to run.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single_desc("int", "sids", "System IDs of the servers to run the script on.")
     * @apidoc.param #param_desc("string", "username", "User to run script as.")
     * @apidoc.param #param_desc("string", "groupname", "Group to run script as.")
     * @apidoc.param #param_desc("int", "timeout", "Seconds to allow the script to run
     *before timing out.")
     * @apidoc.param #param_desc("string", "script", "Contents of the script to run.
     * Must start with a shebang (e.g. #!/bin/bash)")
     * @apidoc.param #param_desc("$date", "earliestOccurrence",
     * "Earliest the script can run.")
     * @apidoc.returntype #param_desc("int", "id", "ID of the script run action created. Can be used to fetch
     * results with system.getScriptResults")
     */
    public Integer scheduleScriptRun(User loggedInUser, List<Integer>
            sids, String username, String groupname, Integer timeout, String script,
                                     Date earliestOccurrence) {
        return scheduleScriptRun(loggedInUser, null, sids, username, groupname,
                timeout, script, earliestOccurrence);

    }

    /**
     * Schedule a script to run.
     *
     * @param loggedInUser           The current user
     * @param sid                    ID of the server to run the script on.
     * @param username               User to run script as.
     * @param groupname              Group to run script as.
     * @param timeout                Seconds to allow the script to run before timing out.
     * @param script                 Contents of the script to run.
     * @param earliestOccurrence     Earliest the script can run.
     * @return ID of the new script action.
     * @apidoc.doc Schedule a script to run.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid",
     * "ID of the server to run the script on.")
     * @apidoc.param #param_desc("string", "username", "User to run script as.")
     * @apidoc.param #param_desc("string", "groupname", "Group to run script as.")
     * @apidoc.param #param_desc("int", "timeout", "Seconds to allow the script to run
     *before timing out.")
     * @apidoc.param #param_desc("string", "script", "Contents of the script to run.
     * Must start with a shebang (e.g. #!/bin/bash)")
     * @apidoc.param #param_desc("$date", "earliestOccurrence",
     * "Earliest the script can run.")
     * @apidoc.returntype #param_desc("int", "id", "ID of the script run action created. Can be used to fetch
     * results with system.getScriptResults")
     */
    public Integer scheduleScriptRun(User loggedInUser, Integer sid, String username,
                                     String groupname, Integer timeout, String script,
                                     Date earliestOccurrence) {

        List<Integer> systemIds = new ArrayList<>();
        systemIds.add(sid);

        return scheduleScriptRun(loggedInUser, null, systemIds, username, groupname,
                timeout,
                script, earliestOccurrence);
    }

    /**
     * Schedule a script to run.
     *
     * @param loggedInUser           The current user
     * @param label                  Text description
     * @param sid                    ID of the server to run the script on.
     * @param username               User to run script as.
     * @param groupname              Group to run script as.
     * @param timeout                Seconds to allow the script to run before timing out.
     * @param script                 Contents of the script to run.
     * @param earliestOccurrence     Earliest the script can run.
     * @return ID of the new script action.
     * @apidoc.doc Schedule a script to run.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param #param_desc("int", "sid",
     * "ID of the server to run the script on.")
     * @apidoc.param #param_desc("string", "username", "User to run script as.")
     * @apidoc.param #param_desc("string", "groupname", "Group to run script as.")
     * @apidoc.param #param_desc("int", "timeout", "Seconds to allow the script to run
     *before timing out.")
     * @apidoc.param #param_desc("string", "script", "Contents of the script to run.
     * Must start with a shebang (e.g. #!/bin/bash)")
     * @apidoc.param #param_desc("$date", "earliestOccurrence",
     * "Earliest the script can run.")
     * @apidoc.returntype #param_desc("int", "id", "ID of the script run action created. Can be used to fetch
     * results with system.getScriptResults")
     */
    public Integer scheduleScriptRun(User loggedInUser, String label, Integer sid, String
            username, String groupname, Integer timeout, String script, Date earliestOccurrence) {

        List<Integer> systemIds = new ArrayList<>();
        systemIds.add(sid);

        return scheduleScriptRun(loggedInUser, label, systemIds, username, groupname,
                timeout,
                script, earliestOccurrence);
    }

    /**
     * Fetch results from a script execution. Returns an empty array if no results are
     * yet available.
     *
     * @param loggedInUser The current user
     * @param actionId ID of the script run action.
     * @return Array of ScriptResult objects.
     *
     * @apidoc.doc Fetch results from a script execution. Returns an empty array if no
     * results are yet available.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "actionId", "ID of the script run action.")
     * @apidoc.returntype
     *          #return_array_begin()
     *              $ScriptResultSerializer
     *         #array_end()
     */
    public Object [] getScriptResults(User loggedInUser, Integer actionId) {
        ScriptRunAction action = lookupScriptRunAction(actionId, loggedInUser);
        ScriptActionDetails details = action.getScriptActionDetails();

        if (details.getResults() == null) {
            return new Object [] {};
        }

        List<ScriptResult> results = new LinkedList<>();
        for (ScriptResult r : details.getResults()) {
            results.add(r);
        }
        return results.toArray();
    }

    /**
     * Returns action script contents for script run actions
     * @param loggedInUser The current user
     * @param actionId action identifier
     * @return script details
     *
     * @apidoc.doc Returns script details for script run actions
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "actionId", "ID of the script run action.")
     * @apidoc.returntype
     *      #struct_begin("Script details")
     *          #prop_desc("int" "id" "action id")
     *          #prop_desc("string" "content" "script content")
     *          #prop_desc("string" "run_as_user" "Run as user")
     *          #prop_desc("string" "run_as_group" "Run as group")
     *          #prop_desc("int" "timeout" "Timeout in seconds")
     *          #return_array_begin()
     *              $ScriptResultSerializer
     *          #array_end()
     *      #struct_end()
     */
    @ReadOnly
    public Map<String, Object> getScriptActionDetails(User loggedInUser, Integer actionId) {
        Map<String, Object> retDetails = new HashMap<>();
        ScriptRunAction action = lookupScriptRunAction(actionId, loggedInUser);
        ScriptActionDetails details = action.getScriptActionDetails();
        retDetails.put("id", action.getId());
        retDetails.put("content", details.getScriptContents());
        retDetails.put("run_as_user", details.getUsername());
        retDetails.put("run_as_group", details.getGroupname());
        retDetails.put("timeout", details.getTimeout());

        if (details.getResults() != null) {
            List<ScriptResult> results = new LinkedList<>();
            for (ScriptResult r : details.getResults()) {
                results.add(r);
            }
            retDetails.put("result", results.toArray());
        }
        return retDetails;
    }

    private ScriptRunAction lookupScriptRunAction(Integer actionId, User loggedInUser) {
        ScriptRunAction action = null;
        try {
            action = (ScriptRunAction)ActionManager.lookupAction(loggedInUser,
                    actionId.longValue());
        }
        catch (LookupException e) {
            throw new NoSuchActionException(actionId.toString(), e);
        }
        catch (ClassCastException e) {
            throw new InvalidActionTypeException(e);
        }
        return action;
    }

    /**
     * Schedule a system reboot
     *
     * @param loggedInUser The current user
     * @param sid ID of the server.
     * @param earliestOccurrence Earliest occurrence of the reboot.
     * @return action id, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Schedule a reboot for a system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleReboot(User loggedInUser, Integer sid,
            Date earliestOccurrence) {
        try {
            Server server = SystemManager.lookupByIdAndUser(sid.longValue(),
                    loggedInUser);

            Action a = ActionManager.scheduleRebootAction(loggedInUser, server,
                    earliestOccurrence);
            a = ActionFactory.save(a);
            taskomaticApi.scheduleActionExecution(a);
            return a.getId();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Get system details.
     *
     * @param loggedInUser The current user
     * @param sid ID of server to lookup details for.
     * @return Server object. (converted to XMLRPC struct by serializer)
     *
     * @apidoc.doc Get system details.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *          $ServerSerializer
     */
    @ReadOnly
    public Object getDetails(User loggedInUser, Integer sid) {
        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(sid.longValue(),
                    loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }
        return server; // serializer will take care of the rest
    }


    /**
     * Set server details.
     *
     * @param loggedInUser The current user
     * @param sid ID of server to lookup details for.
     * @param details Map of (optional) system details to be set.
     * @return 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc Set server details. All arguments are optional and will only be modified
     * if included in the struct.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "ID of server to lookup details for.")
     * @apidoc.param
     *      #struct_begin("details")
     *          #prop_desc("string", "profile_name", "System's profile name")
     *          #prop_desc("string", "base_entitlement", "System's base entitlement label.
     *                      (enterprise_entitled or unentitle)")
     *           #prop_desc("boolean", "auto_errata_update", "True if system has
     *                          auto errata updates enabled")
     *           #prop_desc("string", "description", "System description")
     *           #prop_desc("string", "address1", "System's address line 1.")
     *           #prop_desc("string", "address2", "System's address line 2.")
     *           #prop("string", "city")
     *           #prop("string", "state")
     *           #prop("string", "country")
     *           #prop("string", "building")
     *           #prop("string", "room")
     *           #prop("string", "rack")
     *           #prop_desc("string", "contact_method", "One of the following:")
     *             #options()
     *               #item("default")
     *               #item("ssh-push")
     *               #item("ssh-push-tunnel")
     *             #options_end()
     *     #struct_end()
     *
     *  @apidoc.returntype #return_int_success()
     */
    public Integer setDetails(User loggedInUser, Integer sid,
            Map<String, Object> details) {

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<>();
        validKeys.add("profile_name");
        validKeys.add("base_entitlement");
        validKeys.add("auto_errata_update");
        validKeys.add("address1");
        validKeys.add("address2");
        validKeys.add("city");
        validKeys.add("state");
        validKeys.add("country");
        validKeys.add("building");
        validKeys.add("room");
        validKeys.add("rack");
        validKeys.add("description");
        validKeys.add("contact_method");
        validateMap(validKeys, details);

        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(sid.longValue(),
                    loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        if (details.containsKey("profile_name")) {
            String name = (String)details.get("profile_name");
            name = StringUtils.trim(name);
            validateProfileName(name);
            server.setName(name);
        }

        if (details.containsKey("description")) {
            server.setDescription((String)details.get("description"));
        }

        if (details.containsKey("base_entitlement")) {
            // Raise exception if user attempts to set base entitlement but isn't an org
            // admin:
            if (!loggedInUser.hasRole(RoleFactory.ORG_ADMIN)) {
                throw new PermissionCheckFailureException();
            }

            String selectedEnt = (String)details.get("base_entitlement");
            Entitlement base = EntitlementManager.getByName(selectedEnt);
            if (base != null) {
                systemEntitlementManager.setBaseEntitlement(server, base);
            }
            else if (selectedEnt.equals("unentitle")) {
                systemEntitlementManager.removeAllServerEntitlements(server);
            }
        }

        if (details.containsKey("auto_errata_update")) {
            Boolean autoUpdate = (Boolean)details.get("auto_errata_update");

            if (autoUpdate) {
                server.setAutoUpdate("Y");
            }
            else {
                server.setAutoUpdate("N");
            }
        }

        if (server.getLocation() == null) {
            Location l = new Location();
            server.setLocation(l);
            l.setServer(server);
        }

        if (details.containsKey("address1")) {
            server.getLocation().setAddress1((String)details.get("address1"));
        }
        if (details.containsKey("address2")) {
            server.getLocation().setAddress2((String)details.get("address2"));
        }
        if (details.containsKey("city")) {
            server.getLocation().setCity((String)details.get("city"));
        }
        if (details.containsKey("state")) {
            server.getLocation().setState((String)details.get("state"));
        }
        if (details.containsKey("country")) {
            String country = (String)details.get("country");
            Map<String, String> map = LocalizationService.getInstance()
                    .availableCountries();
            if (country.length() > 2 ||
                    !map.containsValue(country)) {
                throw new UnrecognizedCountryException(country);
            }
            server.getLocation().setCountry(country);
        }
        if (details.containsKey("building")) {
            server.getLocation().setBuilding((String)details.get("building"));
        }
        if (details.containsKey("room")) {
            server.getLocation().setRoom((String)details.get("room"));
        }
        if (details.containsKey("rack")) {
            server.getLocation().setRack((String)details.get("rack"));
        }

        if (details.containsKey("contact_method")) {
            ContactMethod contactMethod = ServerFactory.findContactMethodByLabel(
                    (String) details.get("contact_method"));
            if (server.asMinionServer().isPresent()) {
                throw new FaultException(-1, "contactMethodChangeNotAllowed",
                        "Changing the contact method is not allowed for Salt systems.");
            }
            else if (contactMethod != null) {
                server.setContactMethod(contactMethod);
            }
            else {
                throw new FaultException(-1, "invalidContactMethod",
                        "Invalid contact method: " + details.get("contact_method"));
            }
        }

        return 1;
    }

    /**
     * Set server lock status.
     *
     * @param loggedInUser The current user
     * @param sid ID of server to lookup details for.
     * @param lockStatus to set. True to lock the system, False to unlock the system.
     * @return 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc Set server lock status.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("boolean", "lockStatus", "true to lock the system,
     * false to unlock the system.")
     *
     *  @apidoc.returntype #return_int_success()
     */
    public Integer setLockStatus(User loggedInUser, Integer sid, Boolean lockStatus) {
        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(sid.longValue(),
                    loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        LocalizationService ls = LocalizationService.getInstance();

        if (server.hasEntitlement(EntitlementManager.SALT)) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingCapabilityException(
                    "System locking/unlocking",
                    server);
        }
        else {
            if (lockStatus) {
                // lock the server, if it isn't already locked.
                if (server.getLock() == null) {
                    SystemManager.lockServer(loggedInUser, server,
                            ls.getMessage("sdc.details.overview.lock.reason"));
                }
            }
            else {
                // unlock the server, if it isn't already locked.
                if (server.getLock() != null) {
                    SystemManager.unlockServer(loggedInUser, server);
                }
            }
            return 1;
        }
    }

    /**
     * Add addon entitlements to a server. Entitlements a server already has are simply
     * ignored.
     *
     * @param loggedInUser The current user
     * @param sid ID of server.
     * @param entitlements List of addon entitlement labels to add.
     * @return 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc Add entitlements to a server. Entitlements a server already has
     * are quietly ignored.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("string", "entitlements", "one of following:
     * virtualization_host, enterprise_entitled")
     * @apidoc.returntype #return_int_success()
     */
    public int addEntitlements(User loggedInUser, Integer sid,
            List<String> entitlements) {
        boolean needsSnapshot = false;
        Entitlement entitlement = null;
        List<Entitlement> entitlementL = new ArrayList<>();
        for (String e : entitlements) {
            entitlement = EntitlementManager.getByName(e);
            entitlementL.add(entitlement);
        }
        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(sid.longValue(),
                    loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        validateEntitlements(entitlementL);

        List<String> addOnEnts = new LinkedList<>(entitlements);
        // first process base entitlements
        for (Entitlement en : EntitlementManager.getBaseEntitlements()) {
            if (addOnEnts.contains(en.getLabel())) {
                addOnEnts.remove(en.getLabel());
                systemEntitlementManager.setBaseEntitlement(server, en);
            }
        }

        // put a more intelligible exception
        if ((server.getBaseEntitlement() == null) && (!addOnEnts.isEmpty())) {
            throw new InvalidEntitlementException("Base entitlement missing");
        }

        for (String addOnEntIn : addOnEnts) {

            Entitlement ent = EntitlementManager.getByName(addOnEntIn);

            // Ignore if the system already has this entitlement:
            if (server.hasEntitlement(ent)) {
                log.debug("System {} already has entitlement: {}", server.getName(), ent.getLabel());
                continue;
            }

            if (this.systemEntitlementManager.canEntitleServer(server, ent)) {
                ValidatorResult vr = this.systemEntitlementManager.addEntitlementToServer(server, ent);
                needsSnapshot = true;
                if (!vr.getErrors().isEmpty()) {
                    throw new InvalidEntitlementException();
                }
            }
            else {
                throw new InvalidEntitlementException();
            }
        }

        if (needsSnapshot) {
            SystemManager.snapshotServer(server, LocalizationService
                    .getInstance().getMessage("snapshots.entitlements"));
        }

        return 1;
    }

    /**
     * Remove addon entitlements from a server.
     *
     * @param loggedInUser The current user
     * @param sid ID of server.
     * @param entitlements List of addon entitlement labels to remove.
     * @return 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc Remove addon entitlements from a server. Entitlements a server does
     * not have are quietly ignored.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("string", "entitlements")
     * @apidoc.returntype #return_int_success()
     */
    public int removeEntitlements(User loggedInUser, Integer sid,
            List<String> entitlements) {
        boolean needsSnapshot = false;
        List<Entitlement> entitlementL = new ArrayList<>();
        Entitlement entitlement;
        for (String e : entitlements) {
            entitlement = EntitlementManager.getByName(e);
            entitlementL.add(entitlement);
        }

        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(sid.longValue(),
                    loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        validateEntitlements(entitlementL);

        List<Entitlement> baseEnts = new LinkedList<>();

        for (String entitlementIn : entitlements) {
            Entitlement ent = EntitlementManager.getByName(entitlementIn);
            if (ent.isBase()) {
                baseEnts.add(ent);
                continue;
            }
            systemEntitlementManager.removeServerEntitlement(server, ent);
            needsSnapshot = true;
        }

        // process base entitlements at the end
        if (!baseEnts.isEmpty()) {
            // means unentile the whole system
            systemEntitlementManager.removeAllServerEntitlements(server);
            needsSnapshot = true;
        }

        if (needsSnapshot) {
            SystemManager.snapshotServer(server, LocalizationService
                    .getInstance().getMessage("snapshots.entitlements"));
        }

        return 1;
    }

    /**
     * Unentitle the system completely
     * @param clientCert client system id file
     * @return 1 if successful
     *
     * @apidoc.doc Unentitle the system completely
     * @apidoc.param #param_desc("string", "clientCert", "client system id file")
     * @apidoc.returntype #return_int_success()
     */
    public int unentitle(String clientCert) {
        Server server = validateClientCertificate(clientCert);
        if (server.getBaseEntitlement().isPermanent()) {
            // a permanent entitlement is not changeable by API
            throw new InvalidEntitlementException();
        }
        systemEntitlementManager.removeAllServerEntitlements(server);
        SystemManager.snapshotServer(server, LocalizationService
                .getInstance().getMessage("snapshots.entitlements"));
        return 1;
    }

    /**
     * returns uuid and other transition data for the system according to the mapping file
     * @param clientCert client certificate
     * @return map containing transition data (hostname, uuid, system_id, timestamp)
     * @throws FileNotFoundException in case no transition data are available
     * @throws NoSuchSystemException in case no transition data for the specific system
     * were found
     *
     * @apidoc.ignore Since this API is used for transition of systems and
     * is not useful to external users of the API, the typical XMLRPC API documentation
     * is not being included.
     */
    public Map transitionDataForSystem(String clientCert) throws FileNotFoundException,
        NoSuchSystemException {
        final File transitionFolder =  new File("/usr/share/rhn/transition");
        final String csvUuid = "uuid";
        final String csvSystemId = "system_id";
        final String csvStamp = "timestamp";
        final String csvHostname = "hostname";

        Server server = validateClientCertificate(clientCert);
        String systemIdStr = server.getId().toString();
        Map<String, Object> map = new HashMap<>();
        map.put(csvStamp, 0);

        File[] files = transitionFolder.listFiles();
        if (files == null) {
            throw new FileNotFoundException("Transition data not available");
        }
        for (File file : files) {
            Pattern pattern = Pattern.compile("id_to_uuid-(\\d+).map");
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                Integer fileStamp;
                try {
                    fileStamp = Integer.parseInt(matcher.group(1));
                }
                catch (NumberFormatException nfe) {
                    // not our file, skip it
                    log.debug("Skipping {}", file.getName());
                    break;
                }

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    String[] header = null;
                    Integer systemIdPos = null, uuidPos = null;
                    while ((line = br.readLine()) != null) {
                        if (header == null) {
                            header = line.split(",");
                            for (int i = 0; i < header.length; i++) {
                                if (header[i].equals(csvUuid)) {
                                    uuidPos = i;
                                }
                                if (header[i].equals(csvSystemId)) {
                                    systemIdPos = i;
                                }
                            }
                            if (uuidPos == null || systemIdPos == null) {
                                log.warn("Unexpected format of mapping file {}", file.getName());
                                break;
                            }
                            continue;
                        }
                        String[] record = line.split(",");
                        if (record.length <= uuidPos || record.length <= systemIdPos) {
                            log.warn("Unexpected format of mapping file {}", file.getName());
                            break;
                        }
                        if (record[systemIdPos].equals(systemIdStr) &&
                                fileStamp > (Integer)map.get(csvStamp)) {
                            map.put(csvUuid, record[uuidPos]);
                            map.put(csvSystemId, record[systemIdPos]);
                            map.put(csvStamp, fileStamp);
                            String[] cmd = {"rpm", "--qf=%{NAME}",
                                    "-qf", file.getAbsolutePath()};
                            map.remove(csvHostname);
                            SystemCommandExecutor ce = new SystemCommandExecutor();
                            if (ce.execute(cmd) == 0) {
                                Pattern rpmPattern = Pattern.compile(
                                        "system-profile-transition-(\\S+)-" + fileStamp +
                                        "\n$");
                                matcher = rpmPattern.matcher(ce.getLastCommandOutput());
                                if (matcher.find()) {
                                    map.put(csvHostname, matcher.group(1));
                                }
                            }
                       }
                    }
                }
                catch (IOException e) {
                    log.warn("Cannot read {}", file.getName());
                }
            }
        }

        if (!map.containsKey(csvUuid)) {
            throw new NoSuchSystemException("No transition data for system " + systemIdStr);
        }
        return map;
    }

    /**
     * Lists the package profiles in this organization
     *
     * @param loggedInUser The current user
     * @return 1 on success
     *
     * @apidoc.doc List the package profiles in this organization
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *  #return_array_begin()
     *      $ProfileOverviewDtoSerializer
     *  #array_end()
     */
    @ReadOnly
    public Object[] listPackageProfiles(User loggedInUser) {
        DataResult<ProfileOverviewDto> profiles = ProfileManager.listProfileOverviews(
                loggedInUser.getOrg().getId());

        return profiles.toArray();
    }

    /**
     * Delete a package profile
     *
     * @param loggedInUser The current user
     * @param profileId The package profile ID to delete.
     * @return 1 on success
     *
     * @apidoc.doc Delete a package profile
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "profileId")
     * @apidoc.returntype #return_int_success()
     */
    public int deletePackageProfile(User loggedInUser, Integer profileId) {

        // make sure the user can access this profile
        Profile profile = ProfileManager.lookupByIdAndOrg(profileId.longValue(),
                loggedInUser.getOrg());

        return ProfileManager.deleteProfile(profile);
    }

    /**
     * Creates a new stored Package Profile
     *
     * @param loggedInUser The current user
     * @param sid ID of server to lookup details for.
     * @param profileLabel the label of the profile to be created
     * @param description the description of the profile to be created
     * @return 1 on success
     *
     * @apidoc.doc Create a new stored Package Profile from a systems
     *      installed package list.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "profileLabel")
     * @apidoc.param #param("string", "description")
     * @apidoc.returntype #return_int_success()
     */
    public int createPackageProfile(User loggedInUser, Integer sid,
            String profileLabel, String description) {

        Server server = SystemManager.lookupByIdAndUser(sid.longValue(),
                loggedInUser);

        try {
            Profile profile = ProfileManager.createProfile(loggedInUser, server,
                    profileLabel, description);
            ProfileManager.copyFrom(server, profile);
        }
        catch (DuplicateProfileNameException dbe) {
            throw new DuplicateProfileNameException("Package Profile already exists " +
                    "with name: " + profileLabel);
        }
        catch (NoBaseChannelFoundException nbcfe) {
            throw new ProfileNoBaseChannelException();
        }

        ProfileFactory.findByNameAndOrgId(profileLabel,
                loggedInUser.getOrg().getId());

        return 1;
    }

    /**
     * Compare a system's packages against a package profile.
     *
     * @param loggedInUser The current user
     * @param sid ID of server
     * @param profileLabel the label of the package profile
     * @return 1 on success
     *
     * @apidoc.doc Compare a system's packages against a package profile.  In
     * the result returned, 'this_system' represents the server provided as an input
     * and 'other_system' represents the profile provided as an input.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "profileLabel")
     * @apidoc.returntype
     *          #return_array_begin()
     *              $PackageMetadataSerializer
     *          #array_end()
     */
    public Object[] comparePackageProfile(User loggedInUser, Integer sid,
            String profileLabel) {

        SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);

        Profile profile = ProfileFactory.findByNameAndOrgId(profileLabel,
                loggedInUser.getOrg().getId());

        if (profile == null) {
            throw new InvalidProfileLabelException(profileLabel);
        }

        DataResult<PackageMetadata> dr = ProfileManager.compareServerToProfile(sid.longValue(), profile.getId(),
                loggedInUser.getOrg().getId(), null);

        return dr.toArray();
    }

    /**
     * Returns list of systems which have packages needing updates
     * @param loggedInUser The current user
     * @return Returns an array of SystemOverview objects (which are then
     *          serialized using SystemOverviewSerializer)
     *
     * @apidoc.doc Returns list of systems needing package updates.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *          #return_array_begin()
     *              $SystemOverviewSerializer
     *          #array_end()
     *
     */
    @ReadOnly
    public Object[] listOutOfDateSystems(User loggedInUser) {
        DataResult<SystemOverview> list = SystemManager.outOfDateList(loggedInUser);
        list.elaborate();
        return list.toArray();
    }

    /**
     * Sync packages from a source system to a target.
     *
     * @param loggedInUser The current user
     * @param targetServerId Target system to apply package changes to.
     * @param sourceServerId Source system to retrieve package state from.
     * @param packageIds List of package IDs to be synced.
     * @param earliestOccurrence Earliest occurrence of action.
     * @return action id, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Sync packages from a source system to a target.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "targetServerId", "Target system to apply package
     *                  changes to.")
     * @apidoc.param #param_desc("int", "sourceServerId", "Source system to retrieve
     *                  package state from.")
     * @apidoc.param  #array_single_desc("int", "packageIds", "Package IDs to be synced.")
     * @apidoc.param #param_desc("$date", "earliestOccurrence", "Date to schedule action for")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleSyncPackagesWithSystem(User loggedInUser, Integer targetServerId,
            Integer sourceServerId,
            List<Integer> packageIds, Date earliestOccurrence) {

        Server target = null;
        Server source = null;
        try {
            target = SystemManager.lookupByIdAndUser(targetServerId.longValue(),
                    loggedInUser);
            source = SystemManager.lookupByIdAndUser(sourceServerId.longValue(),
                    loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        if (!isCompatible(loggedInUser, target, source)) {
            throw new InvalidSystemException();
        }

        // For each of the package ids provided, retrieve the pkg id combo
        // which includes name_id|evr_id|arch_id
        Set<String> pkgIdCombos = new HashSet<>();
        for (Integer i : packageIds) {
            Package pkg = PackageManager.lookupByIdAndUser(i.longValue(), loggedInUser);

            if (pkg != null) {
                StringBuilder idCombo = new StringBuilder();
                idCombo.append(pkg.getPackageName().getId()).append("|");
                if (pkg.getPackageEvr() != null) {
                    idCombo.append(pkg.getPackageEvr().getId()).append("|");
                }
                if (pkg.getPackageArch() != null) {
                    idCombo.append(pkg.getPackageArch().getId());
                }
                pkgIdCombos.add(idCombo.toString());
            }
        }

        Action action = null;
        try {
           action = ProfileManager.syncToSystem(loggedInUser,
                   targetServerId.longValue(),
                   sourceServerId.longValue(), pkgIdCombos, null,
                    earliestOccurrence);
        }
        catch (MissingEntitlementException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingEntitlementException();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        if (action == null) {
            throw new InvalidParameterException("No packages to sync");
        }
        return action.getId();
    }

    /**
     * Returns true if the two systems are compatible for a package sync.
     *
     * @param user User making the request.
     * @param target Target server.
     * @param source Source server.
     * @return True is systems are compatible, false otherwise.
     */
    private boolean isCompatible(User user, Server target, Server source) {
        List<Row> compatibleServers = SystemManager.compatibleWithServer(user, target);
        boolean found = false;
        for (Row m : compatibleServers) {
            Long currentId = (Long) m.get("id");
            if (currentId.longValue() == source.getId().longValue()) {
                found = true;
                break;
            }
        }
        return found;
    }


    /**
     * list systems that are not in any system group
     * @param loggedInUser The current user
     * @return A list of Maps containing ID,name, and last checkin
     *
     * @apidoc.doc List systems that are not associated with any system groups.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $SystemOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<SystemOverview> listUngroupedSystems(User loggedInUser) {
        return SystemManager.ungroupedList(loggedInUser);
    }


    /**
     * Gets the base channel for a particular system
     * @param loggedInUser The current user
     * @param sid SystemID of the system in question
     * @return Channel that is the base channel
     *
     * @apidoc.doc Provides the base channel of a given system
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      $ChannelSerializer
     */
    @ReadOnly
    public Object getSubscribedBaseChannel(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        Channel base = server.getBaseChannel();
        if (base == null) {
            return new HashMap<String, String>();
        }
        return base;
    }


    /**
     * Gets the list of inactive systems using the default inactive period
     * @param loggedInUser The current user
     * @return list of inactive systems
     *
     * @apidoc.doc Lists systems that have been inactive for the default period of
     *          inactivity
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ShortSystemInfoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ShortSystemInfo> listInactiveSystems(User loggedInUser) {
        return SystemManager.systemListShortInactive(loggedInUser, null);
    }


    /**
     * Gets the list of inactive systems using the provided  inactive period
     * @param loggedInUser The current user
     * @param days the number of days for inactivity you want
     * @return list of inactive systems
     *
     * @apidoc.doc Lists systems that have been inactive for the specified
     *      number of days..
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "days")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ShortSystemInfoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ShortSystemInfo> listInactiveSystems(User loggedInUser,
            Integer days) {
        return SystemManager.systemListShortInactive(loggedInUser, days, null);
    }

    /**
     * Retrieve the user who registered a particular system
     * @param loggedInUser The current user
     * @param sid the id of the system in question
     * @return the User
     *
     * @apidoc.doc Returns information about the user who registered the system
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "Id of the system in question")
     * @apidoc.returntype
     *          $UserSerializer
     */
    public User whoRegistered(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        return server.getCreator();
    }

    /**
     * returns a list of SystemOverview objects that contain the given package id
     * @param loggedInUser The current user
     * @param pid the package id to search for
     * @return an array of systemOverview objects
     *
     * @apidoc.doc Lists the systems that have the given installed package
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "pid", "the package id")
     * @apidoc.returntype
     *           #return_array_begin()
     *              $SystemOverviewSerializer
     *           #array_end()
     */
    @ReadOnly
    public List<SystemOverview> listSystemsWithPackage(User loggedInUser,
            Integer pid) {
        Package pack = PackageFactory.lookupByIdAndOrg(
                pid.longValue(), loggedInUser.getOrg());
        if (pack == null) {
            throw new InvalidPackageException(pid.toString());
        }
        return SystemManager.listSystemsWithPackage(loggedInUser, pid.longValue());
    }

    /**
     * returns a list of SystemOverview objects that contain a package given it's NVR
     * @param loggedInUser The current user
     * @param name package name
     * @param version package version
     * @param release package release
     *
     * @return an array of systemOverview objects
     *
     * @apidoc.doc Lists the systems that have the given installed package
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "name", "the package name")
     * @apidoc.param #param_desc("string", "version", "the package version")
     * @apidoc.param #param_desc("string", "release", "the package release")
     * @apidoc.returntype
     *              #return_array_begin()
     *                  $SystemOverviewSerializer
     *              #array_end()
     */
    @ReadOnly
    public List<SystemOverview> listSystemsWithPackage(User loggedInUser,
            String name, String version,
            String release) {
        return SystemManager.listSystemsWithPackage(loggedInUser,
                name, version, release);
    }

    /**
     * Method to list systems having a given entitlement
     *
     * @param loggedInUser the current user
     * @param entitlementName the entitlement name to look for
     * @return an array of systemOverview objects
     *
     * @apidoc.doc Lists the systems that have the given entitlement
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "entitlementName", "the entitlement name")
     * @apidoc.returntype
     *              #return_array_begin()
     *                  $SystemOverviewSerializer
     *              #array_end()
     */
    @ReadOnly
    public List<SystemOverview> listSystemsWithEntitlement(User loggedInUser, String entitlementName) {
        Entitlement entitlement = EntitlementManager.getByName(entitlementName);
        if (entitlement == null) {
            throw new InvalidEntitlementException(entitlementName);
        }
        return SystemManager.listSystemsWithEntitlement(loggedInUser, entitlement);
    }

    /**
     * Returns whether there are traditional systems registered
     *
     * @param loggedInUser The current user
     * @return true if there is at least one traditional system registered or else false
     *
     * @apidoc.ignore this endpoint is used only internally to determine if a warning
     * about traditional stack deprecation should be displayed
     */
    @ReadOnly
    public boolean hasTraditionalSystems(User loggedInUser) {
        return SystemManager.hasTraditionalSystems(loggedInUser);
    }

    /**
     * Gets a list of all Physical systems visible to user
     * @param loggedInUser The current user
     * @return Returns an array of maps representing all systems visible to user
     *
     * @throws FaultException A FaultException is thrown if a valid user can not be found
     * from the passed in session key
     *
     * @apidoc.doc Returns a list of all Physical servers visible to the user.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $SystemOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public Object[] listPhysicalSystems(User loggedInUser) throws FaultException {
        DataResult<SystemOverview> dr = SystemManager.physicalList(loggedInUser);
        dr.elaborate();
        return dr.toArray();
    }

    /**
     * Gets a list of virtual hosts for the current user
     * @param loggedInUser The current user
     * @return list of SystemOverview objects
     *
     * @apidoc.doc Lists the virtual hosts visible to the user
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *       $SystemOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<SystemOverview> listVirtualHosts(User loggedInUser) {
        return SystemManager.listVirtualHosts(loggedInUser);
    }

    /**
     * Gets a list of virtual guests for the given host
     * @param loggedInUser The current user
     * @param sid the host system id
     * @return list of VirtualSystemOverview objects
     *
     * @apidoc.doc Lists the virtual guests for a given virtual host
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "the virtual host's id")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $VirtualSystemOverviewSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<VirtualSystemOverview> listVirtualGuests(User loggedInUser,
            Integer sid) {
        DataResult<VirtualSystemOverview> result = SystemManager
                .virtualGuestsForHostList(loggedInUser,
                        sid.longValue(), null);
        result.elaborate();
        return result;
    }

    /**
     * Schedules an action to set the guests memory usage
     * @param loggedInUser The current user
     * @param sid the server ID of the guest
     * @param memory the amount of memory to set the guest to use
     * @return the action id of the scheduled action
     *
     * @apidoc.doc Schedule an action of a guest's host, to set that guest's memory
     *          allocation
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "The guest's system id")
     * @apidoc.param #param_desc("int", "memory", "The amount of memory to
     *          allocate to the guest")
     *  @apidoc.returntype #param_desc("int", "actionID", "the action Id for the schedule action
     *              on the host system")
     *
     */
    public int setGuestMemory(User loggedInUser, Integer sid, Integer memory) {
        VirtualInstance vi = VirtualInstanceFactory.getInstance().lookupByGuestId(
                loggedInUser.getOrg(), sid.longValue());

        try {
            return VirtualizationActionHelper.scheduleAction(
                    vi.getUuid(),
                    loggedInUser,
                    vi.getHostSystem(),
                    VirtualizationActionHelper.getGuestSetterActionCreator(
                            ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY,
                            (data) -> memory * 1024,
                            (action, value) -> ((VirtualizationSetMemoryGuestAction)action).setMemory(value),
                            Map.of(vi.getUuid(), vi.getGuestSystem().getName())
                    ),
                    new VirtualGuestSetterActionJson());
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }


    /**
     * Schedules an actino to set the guests CPU allocation
     * @param loggedInUser The current user
     * @param sid the server ID of the guest
     * @param numOfCpus the num of cpus to set
     * @return the action id of the scheduled action
     *
     * @apidoc.doc Schedule an action of a guest's host, to set that guest's CPU
     *          allocation
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "The guest's system id")
     * @apidoc.param #param_desc("int", "numOfCpus", "The number of virtual cpus to
     *          allocate to the guest")
     *  @apidoc.returntype #param_desc("int", "actionID", "the action Id for the schedule action
     *              on the host system")
     *
     */
    public int setGuestCpus(User loggedInUser, Integer sid, Integer numOfCpus) {
        VirtualInstance vi = VirtualInstanceFactory.getInstance().lookupByGuestId(
                loggedInUser.getOrg(), sid.longValue());

        try {
            return VirtualizationActionHelper.scheduleAction(
                    vi.getUuid(),
                    loggedInUser,
                    vi.getHostSystem(),
                    VirtualizationActionHelper.getGuestSetterActionCreator(
                            ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS,
                            (data) -> numOfCpus,
                            (action, value) -> ((VirtualizationSetVcpusGuestAction)action).setVcpu(value),
                            Map.of(vi.getUuid(), vi.getGuestSystem().getName())
                    ),
                    new VirtualGuestSetterActionJson());
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     *  schedules the specified action on the guest
     * @param loggedInUser The current user
     * @param sid the id of the system
     * @param state one of the following: 'start', 'suspend', 'resume', 'restart',
     *          'shutdown'
     * @param date the date to schedule it
     * @return action ID
     *
     * @apidoc.doc Schedules a guest action for the specified virtual guest for a given
     *          date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "the system Id of the guest")
     * @apidoc.param #param_desc("string", "state", "One of the following actions  'start',
     *          'suspend', 'resume', 'restart', 'shutdown'.")
     * @apidoc.param  #param_desc($date, "date", "the time/date to schedule the action")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public int scheduleGuestAction(User loggedInUser, Integer sid, String state,
            Date date) {
        VirtualInstance vi = VirtualInstanceFactory.getInstance().lookupByGuestId(
                loggedInUser.getOrg(), sid.longValue());

        ActionType action;
        if (state.equals("start")) {
            action = ActionFactory.TYPE_VIRTUALIZATION_START;
        }
        else if (state.equals("suspend")) {
            action = ActionFactory.TYPE_VIRTUALIZATION_SUSPEND;
        }
        else if (state.equals("resume")) {
            action = ActionFactory.TYPE_VIRTUALIZATION_RESUME;
        }
        else if (state.equals("restart")) {
            action = ActionFactory.TYPE_VIRTUALIZATION_REBOOT;
        }
        else if (state.equals("shutdown")) {
            action = ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN;
        }
        else {
            throw new InvalidActionTypeException();
        }

        try {
            VirtualGuestsBaseActionJson data = new VirtualGuestsBaseActionJson();
            data.setUuids(List.of(vi.getUuid()));
            data.setForce(false);
            data.setEarliest(
                Optional.ofNullable(date).map((localDate) -> {
                    ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
                    return LocalDateTime.ofInstant(localDate.toInstant(), zoneId);
                })
            );
            return VirtualizationActionHelper.scheduleAction(
                    vi.getUuid(),
                    loggedInUser,
                    vi.getHostSystem(),
                    VirtualizationActionHelper.getGuestActionCreator(
                            action,
                            Map.of(vi.getUuid(), vi.getGuestSystem().getName())),
                    data);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     *  schedules the specified action on the guest
     * @param loggedInUser The current user
     * @param sid the id of the system
     * @param state one of the following: 'start', 'suspend', 'resume', 'restart',
     *          'shutdown'
     * @return action ID
     *
     * @apidoc.doc Schedules a guest action for the specified virtual guest for the
     *          current time.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "the system Id of the guest")
     * @apidoc.param #param_desc("string", "state", "One of the following actions  'start',
     *          'suspend', 'resume', 'restart', 'shutdown'.")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public int scheduleGuestAction(User loggedInUser, Integer sid, String state) {
        return scheduleGuestAction(loggedInUser, sid, state, null);
    }

    /**
     * List the activation keys the system was registered with.
     * @param loggedInUser The current user
     * @param sid the host system id
     * @return list of keys
     *
     * @apidoc.doc List the activation keys the system was registered with.  An empty
     * list will be returned if an activation key was not used during registration.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #array_single ("string", "key")
     */
    @ReadOnly
    public List<String> listActivationKeys(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);

        DataResult<ActivationKeyDto> result = SystemManager.getActivationKeys(server);

        List<String> returnList = new ArrayList<>();
        for (ActivationKeyDto key : result) {
            returnList.add(key.getToken());
        }
        return returnList;
    }

    /**
     * Get the list of proxies that the given system connects
     * through in order to reach the server.
     * @param loggedInUser The current user
     * @param sid The id of the system in question
     * @return Returns an array of maps representing the proxies the system is connected
     * through
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * sid cannot be found.
     *
     * @apidoc.doc Get the list of proxies that the given system connects
     * through in order to reach the server.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ServerPathSerializer
     *      #array_end()
     */
    @ReadOnly
    public Object[] getConnectionPath(User loggedInUser, Integer sid)
            throws FaultException {

        // Get the logged in user and server
        Server server = lookupServer(loggedInUser, sid);

        DataResult<ServerPath> dr = SystemManager.getConnectionPath(server.getId());
        return dr.toArray();
    }

    /**
     * Authenticates the client system by the client cert and looks up system record.
     * @param clientcert Client certificate.
     * @return SystemRecord.
     */
    private SystemRecord getSystemRecordFromClientCert(String clientcert) {
        Server server = validateClientCertificate(clientcert);
        return server.getCobblerObject(null);
    }

    /**
     * Method to setup the static network configuration for a given server
     * This is used by spacewalkkoan if the user selects static networking option
     * in the advanced configuration section during provisioning.
     * It basically adds $static_network variable to the cobbler system record
     * which gets rendered during the kickstart.
     * @param clientcert the client certificate or the system id file
     * @param data a map holding the network details like ip, gateway,
     *              name servers, ip, netmask and hostname.
     *
     * @return 1 on success exception otherwise.
     *
     * @apidoc.ignore Since this API is for internal integration between services and
     * is not useful to external users of the API, the typical XMLRPC API documentation
     * is not being included.
     */
    public int setupStaticNetwork(String clientcert, Map<String, Object> data) {
        SystemRecord rec = getSystemRecordFromClientCert(clientcert);
        if (rec == null) {
            throw new NoSuchSystemException();
        }

        String device = (String)data.get("device");
        String gateway = (String)data.get("gateway");
        List<String> nameservers = (List<String>)data.get("nameservers");
        String ip = (String)data.get("ip");
        String netmask = (String)data.get("netmask");
        String hostName = (String)data.get("hostname");
        String command = KickstartFormatter.makeStaticNetworkCommand(device, ip, gateway,
                nameservers.get(0), netmask, hostName);
        rec.setHostName(hostName);
        rec.setGateway(gateway);
        rec.setNameServers(Optional.of(nameservers));
        Map<String, Object> meta = rec.getKsMeta().get();
        meta.put(KickstartFormatter.STATIC_NETWORK_VAR, command);
        rec.setKsMeta(Optional.of(meta));
        rec.save();
        return 1;
    }

    /**
     * Method to setup the static network IPv4 and IPv6 configuration for a given server
     * This is used by spacewalkkoan if the user selects static networking option
     * in the advanced configuration section during provisioning.
     * It basically adds $static_network variable to the cobbler system record
     * which gets rendered during the kickstart.
     * @param clientcert the client certificate or the system id file
     * @param data a map holding the IPv4 network details like ip, gateway,
     *              name servers, ip, netmask and hostname.
     *
     * @param data6 a map holding the IPv6 network details like ip, netmask, gateway
     *              and device.
     * @return 1 on success exception otherwise.
     *
     * @apidoc.ignore Since this API is for internal integration between services and
     * is not useful to external users of the API, the typical XMLRPC API documentation
     * is not being included.
     */
    public int setupStaticNetwork(String clientcert, Map<String, Object> data,
            Map<String, Object> data6) {
        SystemRecord rec = getSystemRecordFromClientCert(clientcert);
        if (rec == null) {
            throw new NoSuchSystemException();
        }

        String device = (String)data.get("device");
        // General network info
        String hostName = (String)data.get("hostname");
        List<String> nameservers = (List<String>)data.get("nameservers");
        // IPv4 network info
        String ip4 = (String)data.get("ip");
        String nm4 = (String)data.get("netmask");
        String gw4 = (String)data.get("gateway");
        // IPv6 network info
        String ip6 = (String) data6.get("ip");
        String nm6 = (String) data6.get("netmask");
        String gw6 = (String) data6.get("gateway");

        Map<String, Object> meta = rec.getKsMeta().get();
        String ipv6GatewayMeta = (String) meta.get(KickstartFormatter.USE_IPV6_GATEWAY);
        boolean preferIpv6Gateway = false;
        if (ipv6GatewayMeta != null && ipv6GatewayMeta.equals("true")) {
            preferIpv6Gateway = true;
        }
        String ksDistro = (String) meta.get(KickstartFormatter.KS_DISTRO);

        String command = KickstartFormatter.makeStaticNetworkCommand(device, hostName,
                nameservers.get(0), ip4, nm4, gw4, ip6, nm6, gw6,
                preferIpv6Gateway, ksDistro);

        rec.setHostName(hostName);
        rec.setGateway((preferIpv6Gateway) ? gw6 : gw4);
        rec.setNameServers(Optional.of(nameservers));
        meta.put(KickstartFormatter.STATIC_NETWORK_VAR, command);
        rec.setKsMeta(Optional.of(meta));
        rec.save();
        return 1;
    }

    private KickstartData lookupKsData(String label, Org org) {
        return XmlRpcKickstartHelper.getInstance().lookupKsData(label, org);
    }

    /**
     * Creates a cobbler system record
     * @param loggedInUser The current user
     * @param sid the host system id
     * @param ksLabel identifies the kickstart profile
     *
     * @return int - 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc Creates a cobbler system record with the specified kickstart label
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "ksLabel")
     * @apidoc.returntype #return_int_success()
     */
    public int createSystemRecord(User loggedInUser, Integer sid, String ksLabel) {
        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(sid.longValue(),
                    loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        if (server.hasEntitlement(EntitlementManager.FOREIGN)) {
            throw new FaultException(-2, "provisionError",
                    "System does not have required entitlement");
        }

        KickstartData ksData = lookupKsData(ksLabel, loggedInUser.getOrg());
        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(
                loggedInUser, ksData.getCobblerObject(loggedInUser).getName(),
                ksData, server.getName(), loggedInUser.getOrg().getId());
        cmd.store();

        return 1;
    }

    /**
     * Creates a cobbler system record for a system that is not (yet) registered.
     * @param loggedInUser the currently logged in user
     * @param systemName server name
     * @param ksLabel kickstart profile label
     * @param kOptions kernel options
     * @param comment comment
     * @param netDevices list of network interfaces
     * @return int - 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc Creates a cobbler system record for a system that is not registered.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemName")
     * @apidoc.param #param("string", "ksLabel")
     * @apidoc.param #param("string", "kOptions")
     * @apidoc.param #param("string", "comment")
     * @apidoc.param
     *      #array_begin("netDevices")
     *          #struct_begin("network device")
     *              #prop("string", "name")
     *              #prop("string", "mac")
     *              #prop("string", "ip")
     *              #prop("string", "dnsname")
     *          #struct_end()
     *      #array_end()
     * @apidoc.returntype #return_int_success()
     */
    public int createSystemRecord(User loggedInUser, String systemName, String ksLabel,
            String kOptions, String comment, List<Map<String, String>> netDevices) {
        // Determine the user and lookup the kickstart profile
        KickstartData ksData = lookupKsData(ksLabel, loggedInUser.getOrg());

        // Create a server object
        Server server = ServerFactory.createServer();
        server.setName(systemName);
        server.setOrg(loggedInUser.getOrg());

        // Create cobbler command
        CobblerUnregisteredSystemCreateCommand cmd;
        cmd = new CobblerUnregisteredSystemCreateCommand(loggedInUser, server,
                ksData.getCobblerObject(loggedInUser).getName());

        // Set network device information to the server
        for (Map<String, String> map : netDevices) {
            // FIXME: why do we need this?
            CobblerNetworkInterface device = cmd.new CobblerNetworkInterface();
            device.setName(map.get("name"));
            device.setIpaddr(map.get("ip"));
            device.setHwaddr(map.get("mac"));
            device.setDnsname(map.get("dnsname"));
            // Only add this interface, if either MAC or IP is valid
            if (device.isMacValid() || device.isIpValid()) {
                if (device.getName() == null || device.getName().isEmpty()) {
                    throw new FaultException(-2, "networkDeviceError",
                            "Network device name needs to be specified, e.g. 'eth0'");
                }
                server.addNetworkInterface(device);
            }
        }
        // One device is needed at least
        if (server.getNetworkInterfaces().isEmpty()) {
            throw new FaultException(-2, "networkDeviceError",
                    "At least one valid network device is needed");
        }

        // Store the command
        cmd.setKernelOptions(kOptions);
        cmd.setComment(comment);
        cmd.store();

        return 1;
    }

    /**
     * Creates a system profile in database for a system that is not (yet) registered.
     * Either "hwAddress" or "hostname" prop must be specified in the "data" struct.
     * @param loggedInUser the currently logged in user
     * @param systemName system name
     * @param data the data about system
     * @throws SystemsExistFaultException - when system(s) matching given data exists
     * @throws java.lang.IllegalArgumentException when the input data contains insufficient information or
     * if the format of the hardware address is invalid
     * @return int - ID of the created system on success, exception thrown otherwise.
     *
     * @apidoc.doc Creates a system record in database for a system that is not registered.
     * Either "hwAddress" or "hostname" prop must be specified in the "data" struct.
     * If a system(s) matching given data exists, a SystemsExistFaultException is thrown which
     * contains matching system IDs in its message.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "systemName", "System name")
     * @apidoc.param
     *  #struct_begin("data")
     *      #prop_desc("string", "hwAddress", "The HW address of the network interface (MAC)")
     *      #prop_desc("string", "hostname", "The hostname of the profile")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "systemId", "The id of the created system")
     */
    public int createSystemProfile(User loggedInUser, String systemName, Map<String, Object> data) {
        try {
            return systemManager.createSystemProfile(loggedInUser, systemName, data).getId().intValue();
        }
        catch (SystemsExistException e) {
            throw new SystemsExistFaultException(e.getSystemIds());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidParameterException("Can't create system", e);
        }
    }

    /**
     * Returns a list of kickstart variables set for the specified server
     *
     * @param loggedInUser The current user
     * @param sid          identifies the server
     * @return map of kickstart variables set for the specified server
     *
     * @apidoc.doc Lists kickstart variables set  in the system record
     *  for the specified server.
     *  Note: This call assumes that a system record exists in cobbler for the
     *  given system and will raise an XMLRPC fault if that is not the case.
     *  To create a system record over xmlrpc use system.createSystemRecord
     *
     *  To create a system record in the Web UI  please go to
     *  System -&gt; &lt;Specified System&gt; -&gt; Provisioning -&gt;
     *  Select a Kickstart profile -&gt; Create Cobbler System Record.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #struct_begin("System kickstart variables")
     *          #prop_desc("boolean" "netboot" "netboot enabled")
     *          #prop_array_begin("kickstart variables")
     *              #struct_begin("kickstart variable")
     *                  #prop("string", "key")
     *                  #prop("string or int", "value")
     *              #struct_end()
     *          #prop_array_end()
     *      #struct_end()
     */
    @ReadOnly
    public Map<String, Object> getVariables(User loggedInUser, Integer sid) {

        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        if (server.hasEntitlement(EntitlementManager.FOREIGN)) {
            throw new FaultException(-2, "provisionError",
                    "System does not have required entitlement");
        }

        SystemRecord rec = SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(loggedInUser), server.getCobblerId());
        if (rec == null) {
            throw new NoSuchCobblerSystemRecordException();
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("netboot", rec.isNetbootEnabled());
        vars.put("variables", rec.getKsMeta());

        return vars;
    }

    /**
     * Sets a list of kickstart variables for the specified server
     *
     * @param loggedInUser The current user
     * @param sid          identifies the server
     * @param netboot      netboot enabled
     * @param variables    list of system kickstart variables to set
     * @return int - 1 on success, exception thrown otherwise
     *
     * @apidoc.doc Sets a list of kickstart variables in the cobbler system record
     * for the specified server.
     *  Note: This call assumes that a system record exists in cobbler for the
     *  given system and will raise an XMLRPC fault if that is not the case.
     *  To create a system record over xmlrpc use system.createSystemRecord
     *
     *  To create a system record in the Web UI  please go to
     *  System -&gt; &lt;Specified System&gt; -&gt; Provisioning -&gt;
     *  Select a Kickstart profile -&gt; Create Cobbler System Record.
     *
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("boolean","netboot")
     * @apidoc.param
     *      #struct_begin("variables")
     *          #prop("string", "key")
     *          #prop("string or int", "value")
     *      #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setVariables(User loggedInUser, Integer sid, Boolean netboot,
            Map<String, Object> variables) {

        Server server = null;
        try {
            server = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException();
        }

        if (server.hasEntitlement(EntitlementManager.FOREIGN)) {
            throw new FaultException(-2, "provisionError",
                    "System does not have required entitlement");
        }

        SystemRecord rec = SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(loggedInUser), server.getCobblerId());
        if (rec == null) {
            throw new NoSuchCobblerSystemRecordException();
        }

        rec.enableNetboot(netboot);
        rec.setKsMeta(Optional.of(variables));
        rec.save();

        return 1;
    }


    private List<Map<String, Object>> transformDuplicate(
            List<DuplicateSystemGrouping> list, String propName) {
        List<Map<String, Object>> toRet = new ArrayList<>();
        for (DuplicateSystemGrouping b : list) {
            Map<String, Object> map = new HashMap<>();
            map.put(propName, b.getKey());
            map.put("systems", b.getSystems());
            toRet.add(map);
        }
        return toRet;
    }

    /**
     * List Duplicates by IP
     * @param loggedInUser The current user
     * @return List of Duplicates
     *
     *
     * @apidoc.doc List duplicate systems by IP Address.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *           #struct_begin("Duplicate Group")
     *                   #prop("string", "ip")
     *                   #prop_array_begin("systems")
     *                      $NetworkDtoSerializer
     *                   #prop_array_end()
     *           #struct_end()
     *      #array_end()
     **/
    @ReadOnly
    public List<Map<String, Object>> listDuplicatesByIp(User loggedInUser) {
        List<DuplicateSystemGrouping> list =
                SystemManager.listDuplicatesByIP(loggedInUser, 0L);
        return transformDuplicate(list, "ip");
    }

    /**
     * List Duplicates by Mac Address
     * @param loggedInUser The current user
     * @return List of Duplicates
     *
     *
     * @apidoc.doc List duplicate systems by Mac Address.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *           #struct_begin("Duplicate Group")
     *                   #prop("string", "mac")
     *                   #prop_array_begin("systems")
     *                      $NetworkDtoSerializer
     *                   #prop_array_end()
     *           #struct_end()
     *      #array_end()
     **/
    @ReadOnly
    public List listDuplicatesByMac(User loggedInUser) {
        List<DuplicateSystemGrouping> list =
                SystemManager.listDuplicatesByMac(loggedInUser, 0L);
        return transformDuplicate(list, "mac");
    }

    /**
     * List Duplicates by Hostname
     * @param loggedInUser The current user
     * @return List of Duplicates
     *
     *
     * @apidoc.doc List duplicate systems by Hostname.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *           #struct_begin("Duplicate Group")
     *                   #prop("string", "hostname")
     *                   #prop_array_begin("systems")
     *                      $NetworkDtoSerializer
     *                   #prop_array_end()
     *           #struct_end()
     *      #array_end()
     **/
    @ReadOnly
    public List<Map<String, Object>> listDuplicatesByHostname(User loggedInUser) {
        List<DuplicateSystemGrouping> list =
                SystemManager.listDuplicatesByHostname(loggedInUser, 0L);
        return transformDuplicate(list, "hostname");
    }

    /**
     * Get the System Currency score multipliers
     * @param loggedInUser The current user
     * @return the score multipliers used by the System Currency page
     *
     * @apidoc.doc Get the System Currency score multipliers
     *  @apidoc.param #session_key()
     * @apidoc.returntype #param_desc("map", "multipliers", "Map of score multipliers")
     */
    @ReadOnly
    public Map<String, Integer> getSystemCurrencyMultipliers(User loggedInUser) {
        Map<String, Integer> multipliers = new HashMap<>();
        multipliers.put("scCrit", ConfigDefaults.get().getSCCrit());
        multipliers.put("scImp", ConfigDefaults.get().getSCImp());
        multipliers.put("scMod", ConfigDefaults.get().getSCMod());
        multipliers.put("scLow", ConfigDefaults.get().getSCLow());
        multipliers.put("scBug", ConfigDefaults.get().getSCBug());
        multipliers.put("scEnh", ConfigDefaults.get().getSCEnh());
        return multipliers;
    }

    /**
     * Get System Currency scores for all servers the user has access to
     * @param loggedInUser The current user
     * @return List of user visible systems and a breakdown of the security,
     * bug fix and enhancement errata counts plus a score based on the default
     * system currency multipliers.
     *
     * @apidoc.doc Get the System Currency scores for all servers the user has access to
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("system currency")
     *              #prop("int", "sid")
     *              #prop("int", "critical security errata count")
     *              #prop("int", "important security errata count")
     *              #prop("int", "moderate security errata count")
     *              #prop("int", "low security errata count")
     *              #prop("int", "bug fix errata count")
     *              #prop("int", "enhancement errata count")
     *              #prop("int", "system currency score")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public List<Map<String, Long>> getSystemCurrencyScores(User loggedInUser) {
        DataResult<SystemCurrency> dr = SystemManager.systemCurrencyList(loggedInUser,
                null);
        List<Map<String, Long>> l = new ArrayList<>();
        for (SystemCurrency systemCurrencyIn : dr) {
            Map<String, Long> m = new HashMap<>();
            SystemCurrency s = systemCurrencyIn;
            m.put("sid", s.getId());
            m.put("crit", s.getCritical());
            m.put("imp", s.getImportant());
            m.put("mod", s.getModerate());
            m.put("low", s.getLow());
            m.put("bug", s.getBug());
            m.put("enh", s.getEnhancement());
            m.put("score", s.getScore());
            l.add(m);
        }

        return l;
    }

    /**
     * Get the UUID for the given system ID.
     * @param loggedInUser The current user
     * @param sid of the server
     * @return UUID string
     *
     * @apidoc.doc Get the UUID from the given system ID.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #param("string", "uuid")
     */
    @ReadOnly
    public String getUuid(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);

        if (server.isVirtualGuest()) {
            return server.getVirtualInstance().getUuid();
        }
        return "";
    }

    /**
     * Tags latest system snapshot
     * @param loggedInUser The current user
     * @param sid server id
     * @param tagName tag
     * @return 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc Tags latest system snapshot
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "tagName")
     * @apidoc.returntype #return_int_success()
     */
    public int tagLatestSnapshot(User loggedInUser, Integer sid, String tagName) {
        Server server = lookupServer(loggedInUser, sid);
        if (!(server.hasEntitlement(EntitlementManager.MANAGEMENT))) {
            throw new FaultException(-2, "provisionError",
                    "System does not support snapshots");
        }
        List<ServerSnapshot> snps = ServerFactory.listSnapshots(loggedInUser.getOrg(),
                server, null, null);
        if (snps.isEmpty()) {
            SystemManager.snapshotServer(server, "Initial snapshot");
            snps = ServerFactory.listSnapshots(loggedInUser.getOrg(), server, null, null);
        }
        if (!snps.get(0).addTag(tagName)) {
            throw new SnapshotTagAlreadyExistsException(tagName);
        }
        return 1;
    }

    /**
     * Deletes tag from system snapshot
     * @param loggedInUser The current user
     * @param sid server id
     * @param tagName tag
     * @return 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc Deletes tag from system snapshot
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "tagName")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteTagFromSnapshot(User loggedInUser, Integer sid, String tagName) {
        Server server = lookupServer(loggedInUser, sid);
        SnapshotTag tag = ServerFactory.lookupSnapshotTagbyName(tagName);
        if (tag == null) {
            throw new NoSuchSnapshotTagException(tagName);
        }
        ServerFactory.removeTagFromSnapshot(server.getId(), tag);
        return 1;
    }

    /**
     * List systems with extra packages
     * @param loggedInUser The current user
     * @return Array of systems with extra packages
     *
     * @apidoc.doc List systems with extra packages
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *     #return_array_begin()
     *         #struct_begin("system")
     *             #prop_desc("int", "id", "System ID")
     *             #prop_desc("string", "name", "System profile name")
     *             #prop_desc("int", "extra_pkg_count", "Extra packages count")
     *         #struct_end()
     *     #array_end()
     */
    @ReadOnly
    public Object[] listSystemsWithExtraPackages(User loggedInUser) {
        return SystemManager.getExtraPackagesSystems(loggedInUser).toArray();
    }

    /**
     * List extra packages for given system
     * @param loggedInUser The current user
     * @param sid Server ID
     * @return Array of extra packages for given system
     *
     * @apidoc.doc List extra packages for a system
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("package")
     *                 #prop("string", "name")
     *                 #prop("string", "version")
     *                 #prop("string", "release")
     *                 #prop_desc("string", "epoch", "returned only if non-zero")
     *                 #prop("string", "arch")
     *                 #prop_desc("date", "installtime", "returned only if known")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listExtraPackages(User loggedInUser,
            Integer sid) {
        DataResult<PackageListItem> dr =
                SystemManager.listExtraPackages(Long.valueOf(sid));

        List<Map<String, Object>> returnList = new ArrayList<>();

        for (PackageListItem row : dr) {
            Map<String, Object> pkg = new HashMap<>();

            pkg.put("name", row.getName());
            pkg.put("version", row.getVersion());
            pkg.put("release", row.getRelease());
            if (row.getEpoch() != null) {
                pkg.put("epoch", row.getEpoch());
            }
            if (row.getArch() != null) {
                pkg.put("arch", row.getArch());
            }
            pkg.put("installtime", row.getInstallTime());

            returnList.add(pkg);
        }

        return returnList;
    }

    /**
     * Sets new primary network interface
     * @param loggedInUser The current user
     * @param sid Server ID
     * @param interfaceName Interface name
     * @return 1 if success, exception thrown otherwise
     * @apidoc.doc Sets new primary network interface
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "interfaceName")
     * @apidoc.returntype #return_int_success()
     */
    public int setPrimaryInterface(User loggedInUser, Integer sid,
            String interfaceName) {
        Server server = lookupServer(loggedInUser, sid);

        if (!server.existsActiveInterfaceWithName(interfaceName)) {
            throw new NoSuchNetworkInterfaceException("No such network interface: " +
                    interfaceName);
        }
        server.setPrimaryInterfaceWithName(interfaceName);
        return 1;
    }

    /**
     * Sets new primary FQDN
     * @param loggedInUser The current user
     * @param sid Server ID
     * @param fqdn Primary FQDN
     * @return 1 if success, exception thrown otherwise
     * @throws Exception If FQDN does not exist Exception is thrown
     *
     * @apidoc.doc Sets new primary FQDN
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "fqdn")
     * @apidoc.returntype #return_int_success()
     */
    public int setPrimaryFqdn(User loggedInUser, Integer sid, String fqdn) {
        Server server = lookupServer(loggedInUser, sid);
        server.lookupFqdn(fqdn).orElseThrow(() -> new NoSuchFQDNException(fqdn));
        server.setPrimaryFQDNWithName(fqdn);
        return 1;
    }

    /**
     * Schedule update of client certificate
     * @param loggedInUser The current user
     * @param sid Server Id
     * @return ID of the action if the action scheduling succeeded, exception otherwise
     *
     * @apidoc.doc Schedule update of client certificate
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public int scheduleCertificateUpdate(User loggedInUser, Integer sid) {
        return scheduleCertificateUpdate(loggedInUser, sid, new Date());
    }

    /**
     * Schedule update of client certificate at given date and time
     * @param loggedInUser The current user
     * @param sid Server Id
     * @param earliestOccurrence The date of earliest occurence
     * @return ID of the action if the action scheduling succeeded, exception otherwise
     *
     * @apidoc.doc Schedule update of client certificate at given date and time
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public int scheduleCertificateUpdate(User loggedInUser, Integer sid, Date earliestOccurrence) {
        Server server = lookupServer(loggedInUser, sid);

        if (server == null) {
            throw new InvalidSystemException();
        }

        Action action = null;
        try {
            action = ActionManager.scheduleCertificateUpdate(loggedInUser,
                     server,
                     null);
        }
        catch (MissingCapabilityException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingCapabilityException();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }

        return action.getId().intValue();
    }

    /**
     * send a ping to a system using OSA
     * @param loggedInUser the session key
     * @param sid server id
     * @return 1 on success, exception thrown otherwise.
     *
     * @apidoc.doc send a ping to a system using OSA
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "serverId")
     * @apidoc.returntype #return_int_success()
     */
    public int sendOsaPing(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        if (server == null) {
            throw new InvalidSystemException();
        }

        PushClient client = null;
        client = server.getPushClient();
        if (client == null) {
            throw new NoPushClientException();
        }
        client.setLastPingTime(new Date());
        client.setNextActionTime(null);
        SystemManager.storeServer(server);
        return 1;
    }
    /**
     * get details about a ping sent to a system using OSA
     * @param loggedInUser the session key
     * @param sid server id
     * @return details about a ping sent to a system using OSA
     *
     * @apidoc.doc get details about a ping sent to a system using OSA
     * @apidoc.param #param("User", "loggedInUser")
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #struct_begin("osaPing")
     *          #prop_desc("string" "state"
     *          "state of the system (unknown, online, offline)")
     *          #prop_desc("$date" "lastMessageTime"
     *          "time of the last received response
     *          (1970/01/01 00:00:00 if never received a response)")
     *          #prop_desc("$date" "lastPingTime"
     *          "time of the last sent ping
     *          (1970/01/01 00:00:00 if no ping is pending")
     *      #struct_end()
     */
    @ReadOnly
    public Map<String, Object> getOsaPing(User loggedInUser, Integer sid) {
        Server server = lookupServer(loggedInUser, sid);
        Map<String, Object> map = new HashMap<>();
        if (server.getPushClient() != null) {
            if (server.getPushClient().getState().getName() == null) {
                map.put("state", "unknown");
            }
            else {
                map.put("state", server.getPushClient().getState().getName());
            }
            if (server.getPushClient().getLastMessageTime() == null) {
                map.put("lastMessageTime", new Date(0));
            }
            else {
                map.put("lastMessageTime", server.getPushClient().getLastMessageTime());
            }
            if (server.getPushClient().getLastPingTime() == null) {
                map.put("lastPingTime", new Date(0));
            }
            else {
                map.put("lastPingTime", server.getPushClient().getLastPingTime());
            }
        }
        else {
            map.put("state", "unknown");
            map.put("lastMessageTime", new Date(0));
            map.put("lastPingTime", new Date(0));
        }
        return map;
    }

    /**
     * List possible migration targets for given system
     * @param loggedInUser The current user
     * @param sid Server ID
     * @return Array of migration targets for given system
     *
     * @apidoc.doc List possible migration targets for a system
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("migrationtarget")
     *                 #prop("string", "ident")
     *                 #prop("string", "friendly")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listMigrationTargets(User loggedInUser, Integer sid) {
        return listMigrationTargets(loggedInUser, sid, true);

    }
    /**
     * List possible migration targets for given system
     * @param loggedInUser The current user
     * @param sid Server ID
     * @param excludeTargetWhereMissingSuccessors exclude target if any extension has missing successor
     * @return Array of migration targets for given system
     *
     * @apidoc.doc List possible migration targets for a system, if excludeTargetWhereMissingSuccessors is false then
     * valid targets without some successors will also be listed.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("boolean", "excludeTargetWhereMissingSuccessors")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("migrationtarget")
     *                 #prop("string", "ident")
     *                 #prop("string", "friendly")
     *          #struct_end()
     *      #array_end()
     */
    public List<Map<String, Object>> listMigrationTargets(User loggedInUser,
            Integer sid, boolean excludeTargetWhereMissingSuccessors) {
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        Server server = lookupServer(loggedInUser, sid);
        Optional<SUSEProductSet> installedProducts = server.getInstalledProductSet();
        if (!installedProducts.isPresent()) {
            throw new FaultException(-1, "listMigrationTargetError",
                    "Server has no Products installed.");
        }
        ChannelArch arch = server.getServerArch().getCompatibleChannelArch();
        List<SUSEProductSet> migrationTargets = DistUpgradeManager.
                getTargetProductSets(installedProducts, arch, loggedInUser);
        if (excludeTargetWhereMissingSuccessors) {
            migrationTargets = DistUpgradeManager.removeIncompatibleTargets(
                    installedProducts, migrationTargets,  Optional.empty());
        }
        for (SUSEProductSet ps : migrationTargets) {
            if (!ps.getIsEveryChannelSynced()) {
                continue;
            }
            Map<String, Object> target = new HashMap<>();

            target.put("ident", ps.getSerializedProductIDs());
            target.put("friendly", ps.toString());
            returnList.add(target);
        }

        return returnList;
    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack.
     *
     * This call automatically select the nearest possible migration target.
     *
     * It will automatically find all mandatory product channels below a given
     * target base channel and subscribe the system accordingly. Any additional
     * optional channels can be subscribed by providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     * @deprecated being replaced by scheduleProductMigration(User loggedInUser, Integer sid,
     * String baseChannelLabel, List(String) optionalChildChannels, boolean dryRun, Date earliest)
     *
     * @apidoc.doc Schedule a Product migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     *
     * Note: This method is deprecated and will be removed in a future API version. Please use
     * scheduleProductMigration instead.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    @Deprecated
    public Long scheduleSPMigration(User loggedInUser, Integer sid, String baseChannelLabel,
                                    List<String> optionalChildChannels, Boolean dryRun, Date earliestOccurrence) {
        return scheduleProductMigration(loggedInUser, sid, baseChannelLabel, optionalChildChannels, dryRun,
                false, earliestOccurrence);
    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack.
     *
     * This call automatically select the nearest possible migration target.
     *
     * It will automatically find all mandatory product channels below a given
     * target base channel and subscribe the system accordingly. Any additional
     * optional channels can be subscribed by providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param allowVendorChange set to true to allow vendor change
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     * @deprecated being replaced by scheduleProductMigration(User loggedInUser, Integer sid,
     * String baseChannelLabel, List(String) optionalChildChannels, boolean dryRun, boolean allowVendorChange,
     * Date earliest)
     *
     * @apidoc.doc Schedule a Product migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     *
     * Note: This method is deprecated and will be removed in a future API version. Please use
     * scheduleProductMigration instead.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("boolean", "allowVendorChange")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    @Deprecated
    public Long scheduleSPMigration(User loggedInUser, Integer sid, String baseChannelLabel,
            List<String> optionalChildChannels, Boolean dryRun, Boolean allowVendorChange, Date earliestOccurrence) {
        return scheduleProductMigration(loggedInUser, sid, null, baseChannelLabel,
                optionalChildChannels, dryRun, allowVendorChange, false, earliestOccurrence);
    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack. It will automatically
     * find all mandatory product channels below a given target base channel and subscribe
     * the system accordingly. Any additional optional channels can be subscribed by
     * providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param targetIdent identifier for the selected migration
     *                    target ({@link #listMigrationTargets})
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     * @deprecated being replaced by scheduleProductMigration(User loggedInUser, Integer sid,
     * String targetIdent, String baseChannelLabel, List(String) optionalChildChannels, boolean dryRun,
     * Date earliest)
     *
     * @apidoc.doc Schedule a Prodcut migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     *
     * Note: This method is deprecated and will be removed in a future API version. Please use
     * scheduleProductMigration instead.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "targetIdent", " identifier for the selected migration target.
     *  User listMigrationTargets to list the identifiers ")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    @Deprecated
    public Long scheduleSPMigration(User loggedInUser, Integer sid, String targetIdent,
                                    String baseChannelLabel, List<String> optionalChildChannels, Boolean dryRun,
                                    Date earliestOccurrence) {
       return scheduleProductMigration(loggedInUser, sid, targetIdent, baseChannelLabel, optionalChildChannels,
                dryRun, false, earliestOccurrence);
    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack. It will automatically
     * find all mandatory product channels below a given target base channel and subscribe
     * the system accordingly. Any additional optional channels can be subscribed by
     * providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param targetIdent identifier for the selected migration
     *                    target ({@link #listMigrationTargets})
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param allowVendorChange set to true to allow vendor change
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     * @deprecated being replaced by scheduleProductMigration(User loggedInUser, Integer sid,
     * String targetIdent, String baseChannelLabel, List(String) optionalChildChannels, boolean dryRun,
     * boolean allowVendorChange, Date earliest)
     *
     * @apidoc.doc Schedule a Product migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     *
     * Note: This method is deprecated and will be removed in a future API version. Please use
     * scheduleProductMigration instead.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "targetIdent",
     * "Identifier for the selected migration target. Use listMigrationTargets to list the identifiers")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("boolean", "allowVendorChange")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    @Deprecated
    public Long scheduleSPMigration(User loggedInUser, Integer sid, String targetIdent,
            String baseChannelLabel, List<String> optionalChildChannels, Boolean dryRun,
            Boolean allowVendorChange, Date earliestOccurrence) {
        return scheduleProductMigration(loggedInUser, sid, targetIdent, baseChannelLabel, optionalChildChannels,
                dryRun, allowVendorChange, earliestOccurrence);
    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack.
     *
     * This call automatically select the nearest possible migration target.
     *
     * It will automatically find all mandatory product channels below a given
     * target base channel and subscribe the system accordingly. Any additional
     * optional channels can be subscribed by providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     *
     * @apidoc.doc Schedule a Product migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleProductMigration(User loggedInUser, Integer sid, String baseChannelLabel,
                                         List<String> optionalChildChannels, Boolean dryRun, Date earliestOccurrence) {
        return scheduleProductMigration(loggedInUser, sid, baseChannelLabel, optionalChildChannels, dryRun,
                false, earliestOccurrence);
    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack.
     *
     * This call automatically select the nearest possible migration target.
     *
     * It will automatically find all mandatory product channels below a given
     * target base channel and subscribe the system accordingly. Any additional
     * optional channels can be subscribed by providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param allowVendorChange set to true to allow vendor change
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     *
     * @apidoc.doc Schedule a Product migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("boolean", "allowVendorChange")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleProductMigration(User loggedInUser, Integer sid, String baseChannelLabel,
                                         List<String> optionalChildChannels, Boolean dryRun, Boolean allowVendorChange,
                                         Date earliestOccurrence) {
        return scheduleProductMigration(loggedInUser, sid, null, baseChannelLabel,
                optionalChildChannels, dryRun, allowVendorChange, earliestOccurrence);
    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack. It will automatically
     * find all mandatory product channels below a given target base channel and subscribe
     * the system accordingly. Any additional optional channels can be subscribed by
     * providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param targetIdent identifier for the selected migration
     *                    target ({@link #listMigrationTargets})
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     *
     * @apidoc.doc Schedule a Prodcut migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "targetIdent",
     * "Identifier for the selected migration target. Use listMigrationTargets to list the identifiers")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleProductMigration(User loggedInUser, Integer sid, String targetIdent,
                                         String baseChannelLabel, List<String> optionalChildChannels, Boolean dryRun,
                                         Date earliestOccurrence) {
        return scheduleProductMigration(loggedInUser, sid, targetIdent, baseChannelLabel, optionalChildChannels,
                dryRun, false, earliestOccurrence);
    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack. It will automatically
     * find all mandatory product channels below a given target base channel and subscribe
     * the system accordingly. Any additional optional channels can be subscribed by
     * providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param targetIdent identifier for the selected migration
     *                    target ({@link #listMigrationTargets})
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param allowVendorChange set to true to allow vendor change
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     *
     * @apidoc.doc Schedule a Product migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "targetIdent",
     * "Identifier for the selected migration target. Use listMigrationTargets to list the identifiers")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("boolean", "allowVendorChange")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleProductMigration(User loggedInUser, Integer sid, String targetIdent,
                                         String baseChannelLabel, List<String> optionalChildChannels, Boolean dryRun,
                                         Boolean allowVendorChange, Date earliestOccurrence) {
       return scheduleProductMigration(loggedInUser, sid, targetIdent, baseChannelLabel, optionalChildChannels, dryRun,
                allowVendorChange, false, earliestOccurrence);

    }

    /**
     * Schedule a Product migration for a system. This call is the recommended and
     * supported way of migrating a system to the next Service Pack. It will automatically
     * find all mandatory product channels below a given target base channel and subscribe
     * the system accordingly. Any additional optional channels can be subscribed by
     * providing their labels.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param targetIdent identifier for the selected migration
     *                    target ({@link #listMigrationTargets})
     * @param baseChannelLabel label of the target base channel
     * @param optionalChildChannels labels of optional child channels to subscribe
     * @param dryRun set to true to perform a dry run
     * @param allowVendorChange set to true to allow vendor change
     * @param removeProductsWithNoSuccessorAfterMigration set to remove products which have no successors
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     *
     * @apidoc.doc Schedule a Product migration for a system. This call is the
     * recommended and supported way of migrating a system to the next Service Pack. It will
     * automatically find all mandatory product channels below a given target base channel
     * and subscribe the system accordingly. Any additional optional channels can be
     * subscribed by providing their labels.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "targetIdent",
     * "Identifier for the selected migration target - User listMigrationTargets to list the identifiers ")
     * @apidoc.param #param("string", "baseChannelLabel")
     * @apidoc.param #array_single("string", "optionalChildChannels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("boolean", "allowVendorChange")
     * @apidoc.param #param_desc("boolean", "removeProductsWithNoSuccessorAfterMigration","set to remove products which
     * have no successors. This flag will only have effect if targetIdent will also be specified")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleProductMigration(User loggedInUser, Integer sid, String targetIdent,
                                         String baseChannelLabel, List<String> optionalChildChannels, boolean dryRun,
                                         boolean allowVendorChange,
                                         boolean removeProductsWithNoSuccessorAfterMigration, Date earliestOccurrence) {
        // Perform checks on the server
        Server server = null;
        try {
            server = DistUpgradeManager.performServerChecks(sid.longValue(), loggedInUser);
        }
        catch (DistUpgradeException e) {
            throw new FaultException(-1, "distUpgradeServerError", e.getMessage());
        }

        // Check validity of optional child channels and initialize list of channel IDs
        Set<Long> channelIDs = null;
        optionalChildChannels.add(baseChannelLabel);
        try {
            channelIDs =
              DistUpgradeManager.performChannelChecks(optionalChildChannels, loggedInUser);
        }
        catch (DistUpgradeException e) {
            throw new FaultException(-1, "distUpgradeChannelError", e.getMessage());
        }

        // Find target products for the migration
        Optional<SUSEProductSet> installedProducts = server.getInstalledProductSet();
        ChannelArch arch = server.getServerArch().getCompatibleChannelArch();
        List<SUSEProductSet> targets = DistUpgradeManager.getTargetProductSets(
                installedProducts, arch, loggedInUser);
        // Consider the targets where some extensions have missing successors but only if user explicitly mention
        // targetIdent && set the flag removeProductsWithNoSuccessorAfterMigration as true
        if (!removeProductsWithNoSuccessorAfterMigration || StringUtils.isBlank(targetIdent)) {
            targets = DistUpgradeManager.removeIncompatibleTargets(installedProducts, targets, Optional.empty());
        }
        if (!targets.isEmpty()) {
            SUSEProductSet targetProducts = null;
            if (StringUtils.isBlank(targetIdent)) {
                log.info("Target migration id is empty. " +
                        "Looking for the closest product version having synced channels.");
                List<SUSEProductSet> syncedTargets = targets.stream()
                        .filter(ps -> {
                            if (log.isDebugEnabled()) {
                                if (ps.getIsEveryChannelSynced()) {
                                    log.debug("{} is completely synced.", ps);
                                }
                                else {
                                    log.debug("Discarding {}. Is not completely synced.", ps);
                                }
                            }
                            return ps.getIsEveryChannelSynced();
                        })
                        .collect(toList());
                targetProducts = !syncedTargets.isEmpty() ? syncedTargets.get(syncedTargets.size() - 1) : null;
                log.info("Using migration target: {}", targetProducts);
            }
            else {
                for (SUSEProductSet target : targets) {
                    String ident = target.getSerializedProductIDs();
                    if (ident.equals(targetIdent)) {
                        targetProducts = target;
                        break;
                    }
                }
            }
            if (targetProducts == null) {
                String targetsInfo = "Possible targets with incompletely synced channels: " +
                        System.getProperty("line.separator") +
                        targets.stream().map(t -> t + " : " +
                                t.getMissingChannelsMessage())
                                .collect(Collectors.joining(System.getProperty("line.separator")));
                log.error("No target products found for migration: {}", targetsInfo);
                throw new FaultException(-1, "productMigrationNoTarget",
                        "No target found for Product migration. " + targetsInfo);
            }
            if (!targetProducts.getIsEveryChannelSynced()) {
                throw new FaultException(-1, "productMigrationNoTarget",
                        "Target not available, the following channels are not synced: " +
                        targetProducts.getMissingChannelsMessage());
            }

            // See if vendor channels are matching the given base channel
            EssentialChannelDto baseChannel = DistUpgradeManager.getProductBaseChannelDto(
                    targetProducts.getBaseProduct().getId(), arch);

            try {
                if (baseChannel != null &&
                        baseChannel.getLabel().equals(baseChannelLabel)) {
                    List<EssentialChannelDto> channels = DistUpgradeManager.
                            getRequiredChannels(targetProducts, baseChannel.getId());
                    for (EssentialChannelDto channel : channels) {
                        channelIDs.add(channel.getId());
                    }
                    return DistUpgradeManager.scheduleDistUpgrade(loggedInUser, server,
                            targetProducts, channelIDs, dryRun, allowVendorChange, earliestOccurrence);
                }

                // Consider alternatives (cloned channel trees)
                SortedMap<ClonedChannel, List<Long>> alternatives = DistUpgradeManager.
                        getAlternatives(targetProducts, arch, loggedInUser);
                for (ClonedChannel clonedBaseChannel : alternatives.keySet()) {
                    if (clonedBaseChannel.getLabel().equals(baseChannelLabel)) {
                        channelIDs.addAll(alternatives.get(clonedBaseChannel));
                        return DistUpgradeManager.scheduleDistUpgrade(loggedInUser, server,
                                targetProducts, channelIDs, dryRun, allowVendorChange, earliestOccurrence);
                    }
                }
            }
            catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
                throw new TaskomaticApiException(e.getMessage());
            }
        }

        // We didn't find target products if we are still here
        throw new FaultException(-1, "productMigrationNoTarget",
                "No target found for Product migration");
    }

    /**
     * Schedule a dist upgrade for a system. This call takes a list of channel labels that
     * the system will be subscribed to before performing the dist upgrade.
     *
     * Note: You can seriously damage your system with this call, use it only if you really
     * know what you are doing! Make sure that the list of channel labels is complete and
     * in any case do a dry run before scheduling an actual dist upgrade.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param channels labels of channels to subscribe to
     * @param dryRun set to true to perform a dry run
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     *
     * @apidoc.doc Schedule a dist upgrade for a system. This call takes a list of channel
     * labels that the system will be subscribed to before performing the dist upgrade.
     * Note: You can seriously damage your system with this call, use it only if you really
     * know what you are doing! Make sure that the list of channel labels is complete and in
     * any case do a dry run before scheduling an actual dist upgrade.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("string", "channels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleDistUpgrade(User loggedInUser, Integer sid, List<String> channels,
                                    boolean dryRun, Date earliestOccurrence) {
        // for older calls that don't use vendor change
        return scheduleDistUpgrade(loggedInUser, sid, channels, dryRun, false, earliestOccurrence);
    }

    /**
     * Schedule a dist upgrade for a system. This call takes a list of channel labels that
     * the system will be subscribed to before performing the dist upgrade.
     *
     * Note: You can seriously damage your system with this call, use it only if you really
     * know what you are doing! Make sure that the list of channel labels is complete and
     * in any case do a dry run before scheduling an actual dist upgrade.
     *
     * @param loggedInUser the currently logged in user
     * @param sid ID of the server
     * @param channels labels of channels to subscribe to
     * @param dryRun set to true to perform a dry run
     * @param allowVendorChange set to true to allow vendor change
     * @param earliestOccurrence earliest occurrence of the migration
     * @return action id, exception thrown otherwise
     *
     * @apidoc.doc Schedule a dist upgrade for a system. This call takes a list of channel
     * labels that the system will be subscribed to before performing the dist upgrade.
     * Note: You can seriously damage your system with this call, use it only if you really
     * know what you are doing! Make sure that the list of channel labels is complete and in
     * any case do a dry run before scheduling an actual dist upgrade.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("string", "channels")
     * @apidoc.param #param("boolean", "dryRun")
     * @apidoc.param #param("boolean", "allowVendorChange")
     * @apidoc.param #param("$date",  "earliestOccurrence")
     * @apidoc.returntype #param("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleDistUpgrade(User loggedInUser, Integer sid, List<String> channels,
            boolean dryRun, boolean allowVendorChange, Date earliestOccurrence) {
        // Lookup the server and perform some checks
        Server server = null;
        try {
            server = DistUpgradeManager.performServerChecks(sid.longValue(), loggedInUser);
        }
        catch (DistUpgradeException e) {
            throw new FaultException(-1, "distUpgradeServerError", e.getMessage());
        }

        // Perform checks on the channels (while converting to a list of IDs)
        Set<Long> channelIDs = null;
        try {
            channelIDs = DistUpgradeManager.performChannelChecks(channels, loggedInUser);
            return DistUpgradeManager.scheduleDistUpgrade(loggedInUser, server, null,
                    channelIDs, dryRun, allowVendorChange, earliestOccurrence);
        }
        catch (DistUpgradeException e) {
            throw new FaultException(-1, "distUpgradeChannelError", e.getMessage());
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }
    /**
     * Method to list systems that require reboot
     * @param loggedInUser the session key
     * @return List of systems that require reboot
     *
     * @apidoc.doc List systems that require reboot.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("system")
     *              #prop("int" "id")
     *              #prop("string" "name")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public Object[] listSuggestedReboot(User loggedInUser) {
            return SystemManager.requiringRebootList(loggedInUser).toArray();
    }

    /**
     * Get a list of installed products for given system
     * @param loggedInUser The current user
     * @param sid Server ID
     * @return List of installed products for given system
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * the sid cannot be found
     *
     * @apidoc.doc Get a list of installed products for given system
     * @apidoc.param #param("User", "loggedInUser")
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $SUSEInstalledProductSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<SUSEInstalledProduct> getInstalledProducts(User loggedInUser,
            Integer sid) throws FaultException {
        Server server = lookupServer(loggedInUser, sid);

        //Ignore non-SUSE products
        return server.getInstalledProducts().stream()
                .filter(p -> p.getSUSEProduct() != null)
                .map(p -> new SUSEInstalledProduct(p.getName(), p.getVersion(),
                        p.getArch().getLabel(), p.getRelease(), p.isBaseproduct(),
                        p.getSUSEProduct().getFriendlyName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns current live patching version for the kernel, or empty string if live
     * patching feature is not in use for the system.
     *
     * @param loggedInUser The current user.
     * @param sid Server ID to lookup.
     * @return Current kernel live patching version.
     *
     * @apidoc.doc Returns the currently active kernel live patching version relative to
     * the running kernel version of the system, or empty string if live patching feature
     * is not in use for the given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype string
     */
    @ReadOnly
    public String getKernelLivePatch(User loggedInUser, Integer sid) {
        try {
            Server server = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser);
            return server.asMinionServer().map(MinionServer::getKernelLiveVersion)
                    .orElse("");
        }
        catch (LookupException e) {
            throw new NoSuchSystemException(e);
        }
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     *
     * NOTE: Arguments contain sensitive data, which is hidden from logging in {@link XmlRpcLoggingInvocationProcessor}
     *
     * @param user the current user
     * @param host hostname or IP address of the target machine
     * @param sshPort SSH port to be used on the target machine
     * @param sshUser SSH user to be used on the target machine
     * @param sshPassword SSH password of given user
     * @param activationKey activation key to be used for registration
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc Bootstrap a system for management via either Salt or Salt SSH.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "host", "Hostname or IP address of target")
     * @apidoc.param #param_desc("int", "sshPort", "SSH port on target machine")
     * @apidoc.param #param_desc("string", "sshUser", "SSH user on target machine")
     * @apidoc.param #param_desc("string", "sshPassword", "SSH password of given user")
     * @apidoc.param #param_desc("string", "activationKey", "Activation key")
     * @apidoc.param #param_desc("boolean", "saltSSH", "Manage system with Salt SSH")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrap(User user, String host, Integer sshPort, String sshUser,
            String sshPassword, String activationKey, Boolean saltSSH) {
        Optional<String> maybePassword = maybeString(sshPassword);
        List<String> activationKeys = maybeActivationKeys(activationKey);
        BootstrapParameters params = new BootstrapParameters(host, of(sshPort), sshUser, maybePassword, activationKeys,
                empty(), true, empty());
        log.debug("bootstrap called: {}", params);
        return xmlRpcSystemHelper.bootstrap(user, params, saltSSH);
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     * Use SSH private key for authentication.
     *
     * NOTE: Arguments contain sensitive data, which is hidden from logging in {@link XmlRpcLoggingInvocationProcessor}
     *
     * @param user the current user
     * @param host hostname or IP address of the target machine
     * @param sshPort SSH port to be used on the target machine
     * @param sshUser SSH user to be used on the target machine
     * @param sshPrivKey SSH private key as a string in PEM format
     * @param sshPrivKeyPass SSH passphrase for the key (use empty string for no passphrase)
     * @param activationKey activation key to be used for registration
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc Bootstrap a system for management via either Salt or Salt SSH.
     * Use SSH private key for authentication.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "host", "Hostname or IP address of target")
     * @apidoc.param #param_desc("int", "sshPort", "SSH port on target machine")
     * @apidoc.param #param_desc("string", "sshUser", "SSH user on target machine")
     * @apidoc.param #param_desc("string", "sshPrivKey", "SSH private key as a string in PEM format")
     * @apidoc.param #param_desc("string", "sshPrivKeyPass",
     * "SSH passphrase for the key (use empty string for no passphrase)")
     * @apidoc.param #param_desc("string", "activationKey", "Activation key")
     * @apidoc.param #param_desc("boolean", "saltSSH", "Manage system with Salt SSH")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrapWithPrivateSshKey(User user, String host, Integer sshPort, String sshUser,
            String sshPrivKey, String sshPrivKeyPass, String activationKey, Boolean saltSSH) {
        List<String> activationKeys = maybeActivationKeys(activationKey);
        BootstrapParameters params = new BootstrapParameters(host, of(sshPort), sshUser, sshPrivKey,
                maybeString(sshPrivKeyPass), activationKeys, empty(), true, empty());
        log.debug("bootstrapWithPrivateSshKey called: {}", params);
        return xmlRpcSystemHelper.bootstrap(user, params, saltSSH);
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     *
     * NOTE: Arguments contain sensitive data, which is hidden from logging in {@link XmlRpcLoggingInvocationProcessor}
     *
     * @param user the current user
     * @param host hostname or IP address of the target machine
     * @param sshPort SSH port to be used on the target machine
     * @param sshUser SSH user to be used on the target machine
     * @param sshPassword SSH password of given user
     * @param activationKey activation key to be used for registration
     * @param proxyId system ID of proxy to use
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc Bootstrap a system for management via either Salt or Salt SSH.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "host", "Hostname or IP address of target")
     * @apidoc.param #param_desc("int", "sshPort", "SSH port on target machine")
     * @apidoc.param #param_desc("string", "sshUser", "SSH user on target machine")
     * @apidoc.param #param_desc("string", "sshPassword", "SSH password of given user")
     * @apidoc.param #param_desc("string", "activationKey", "Activation key")
     * @apidoc.param #param_desc("int", "proxyId", "System ID of proxy to use")
     * @apidoc.param #param_desc("boolean", "saltSSH", "Manage system with Salt SSH")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrap(User user, String host, Integer sshPort, String sshUser,
            String sshPassword, String activationKey, Integer proxyId, Boolean saltSSH) {
        Optional<String> maybePassword = maybeString(sshPassword);
        List<String> activationKeys = maybeActivationKeys(activationKey);
        BootstrapParameters params = new BootstrapParameters(host, of(sshPort), sshUser, maybePassword, activationKeys,
                empty(), true, of(proxyId.longValue()));
        log.debug("bootstrap called with proxyId: {}", params);
        return xmlRpcSystemHelper.bootstrap(user, params, saltSSH);
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     * Use SSH private key for authentication.
     *
     * NOTE: Arguments contain sensitive data, which is hidden from logging in {@link XmlRpcLoggingInvocationProcessor}
     *
     * @param user the current user
     * @param host hostname or IP address of the target machine
     * @param sshPort SSH port to be used on the target machine
     * @param sshUser SSH user to be used on the target machine
     * @param sshPrivKey SSH private key as a string in PEM format
     * @param sshPrivKeyPass SSH passphrase for the key (use empty string for no passphrase)
     * @param activationKey activation key to be used for registration
     * @param proxyId system ID of proxy to use
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc Bootstrap a system for management via either Salt or Salt SSH.
     * Use SSH private key for authentication.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "host", "Hostname or IP address of target")
     * @apidoc.param #param_desc("int", "sshPort", "SSH port on target machine")
     * @apidoc.param #param_desc("string", "sshUser", "SSH user on target machine")
     * @apidoc.param #param_desc("string", "sshPrivKey", "SSH private key as a string in PEM format")
     * @apidoc.param #param_desc("string", "sshPrivKeyPass",
     * "SSH passphrase for the key (use empty string for no passphrase)")
     * @apidoc.param #param_desc("string", "activationKey", "Activation key")
     * @apidoc.param #param_desc("int", "proxyId", "System ID of proxy to use")
     * @apidoc.param #param_desc("boolean", "saltSSH", "Manage system with Salt SSH")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrapWithPrivateSshKey(User user, String host, Integer sshPort, String sshUser,
            String sshPrivKey, String sshPrivKeyPass, String activationKey, Integer proxyId, Boolean saltSSH) {
        List<String> activationKeys = maybeActivationKeys(activationKey);
        BootstrapParameters params = new BootstrapParameters(host, of(sshPort), sshUser, sshPrivKey,
                maybeString(sshPrivKeyPass), activationKeys, empty(), true, of(proxyId.longValue()));
        log.debug("bootstrapWithPrivateSshKey called with proxyId: {}", params);
        return xmlRpcSystemHelper.bootstrap(user, params, saltSSH);
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     *
     * NOTE: Arguments contain sensitive data, which is hidden from logging in {@link XmlRpcLoggingInvocationProcessor}
     *
     * @param user the current user
     * @param host hostname or IP address of the target machine
     * @param sshPort SSH port to be used on the target machine
     * @param sshUser SSH user to be used on the target machine
     * @param sshPassword SSH password of given user
     * @param activationKey activation key to be used for registration
     * @param reactivationKey reactivation key to be used for registration
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc Bootstrap a system for management via either Salt or Salt SSH.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "host", "Hostname or IP address of target")
     * @apidoc.param #param_desc("int", "sshPort", "SSH port on target machine")
     * @apidoc.param #param_desc("string", "sshUser", "SSH user on target machine")
     * @apidoc.param #param_desc("string", "sshPassword", "SSH password of given user")
     * @apidoc.param #param_desc("string", "activationKey", "Activation key")
     * @apidoc.param #param_desc("string", "reactivationKey", "Reactivation key")
     * @apidoc.param #param_desc("boolean", "saltSSH", "Manage system with Salt SSH")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrap(User user, String host, Integer sshPort, String sshUser,
            String sshPassword, String activationKey, String reactivationKey, Boolean saltSSH) {
        Optional<String> maybePassword = maybeString(sshPassword);
        List<String> activationKeys = maybeActivationKeys(activationKey);
        BootstrapParameters params = new BootstrapParameters(host, of(sshPort), sshUser, maybePassword, activationKeys,
                maybeString(reactivationKey), true, empty());
        log.debug("bootstrap called with re-activation key: {}", params);
        return xmlRpcSystemHelper.bootstrap(user, params, saltSSH);
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     * Use SSH private key for authentication.
     *
     * NOTE: Arguments contain sensitive data, which is hidden from logging in {@link XmlRpcLoggingInvocationProcessor}
     *
     * @param user the current user
     * @param host hostname or IP address of the target machine
     * @param sshPort SSH port to be used on the target machine
     * @param sshUser SSH user to be used on the target machine
     * @param sshPrivKey SSH private key as a string in PEM format
     * @param sshPrivKeyPass SSH passphrase for the key (use empty string for no passphrase)
     * @param activationKey activation key to be used for registration
     * @param reactivationKey reactivation key to be used for registration
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc Bootstrap a system for management via either Salt or Salt SSH.
     * Use SSH private key for authentication.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "host", "Hostname or IP address of target")
     * @apidoc.param #param_desc("int", "sshPort", "SSH port on target machine")
     * @apidoc.param #param_desc("string", "sshUser", "SSH user on target machine")
     * @apidoc.param #param_desc("string", "sshPrivKey", "SSH private key as a string in PEM format")
     * @apidoc.param #param_desc("string", "sshPrivKeyPass",
     * "SSH passphrase for the key (use empty string for no passphrase)")
     * @apidoc.param #param_desc("string", "activationKey", "Activation key")
     * @apidoc.param #param_desc("string", "reactivationKey", "Reactivation key")
     * @apidoc.param #param_desc("boolean", "saltSSH", "Manage system with Salt SSH")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrapWithPrivateSshKey(User user, String host, Integer sshPort, String sshUser,
            String sshPrivKey, String sshPrivKeyPass, String activationKey, String reactivationKey,
            Boolean saltSSH) {
        List<String> activationKeys = maybeActivationKeys(activationKey);
        BootstrapParameters params = new BootstrapParameters(host, of(sshPort), sshUser, sshPrivKey,
                maybeString(sshPrivKeyPass), activationKeys, maybeString(reactivationKey), true, empty());
        log.debug("bootstrapWithPrivateSshKey called with reactivationKey: {}", params);
        return xmlRpcSystemHelper.bootstrap(user, params, saltSSH);
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     *
     * NOTE: Arguments contain sensitive data, which is hidden from logging in {@link XmlRpcLoggingInvocationProcessor}
     *
     * @param user the current user
     * @param host hostname or IP address of the target machine
     * @param sshPort SSH port to be used on the target machine
     * @param sshUser SSH user to be used on the target machine
     * @param sshPassword SSH password of given user
     * @param activationKey activation key to be used for registration
     * @param reactivationKey reactivation key to be used for registration
     * @param proxyId system ID of proxy to use
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc Bootstrap a system for management via either Salt or Salt SSH.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "host", "Hostname or IP address of target")
     * @apidoc.param #param_desc("int", "sshPort", "SSH port on target machine")
     * @apidoc.param #param_desc("string", "sshUser", "SSH user on target machine")
     * @apidoc.param #param_desc("string", "sshPassword", "SSH password of given user")
     * @apidoc.param #param_desc("string", "activationKey", "Activation key")
     * @apidoc.param #param_desc("string", "reactivationKey", "Reactivation key")
     * @apidoc.param #param_desc("int", "proxyId", "System ID of proxy to use")
     * @apidoc.param #param_desc("boolean", "saltSSH", "Manage system with Salt SSH")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrap(User user, String host, Integer sshPort, String sshUser,
            String sshPassword, String activationKey, String reactivationKey, Integer proxyId,
            Boolean saltSSH) {
        Optional<String> maybePassword = maybeString(sshPassword);
        List<String> activationKeys = maybeActivationKeys(activationKey);
        BootstrapParameters params = new BootstrapParameters(host, of(sshPort), sshUser, maybePassword, activationKeys,
                maybeString(reactivationKey), true, of(proxyId.longValue()));
        log.debug("bootstrap called with re-activation key and proxyId: {}", params);
        return xmlRpcSystemHelper.bootstrap(user, params, saltSSH);
    }

    /**
     * Bootstrap a system for management via either Salt (minion/master) or Salt SSH.
     * Use SSH private key for authentication.
     *
     * NOTE: Arguments contain sensitive data, which is hidden from logging in {@link XmlRpcLoggingInvocationProcessor}
     *
     * @param user the current user
     * @param host hostname or IP address of the target machine
     * @param sshPort SSH port to be used on the target machine
     * @param sshUser SSH user to be used on the target machine
     * @param sshPrivKey SSH private key as a string in PEM format
     * @param sshPrivKeyPass SSH passphrase for the key (use empty string for no passphrase)
     * @param activationKey activation key to be used for registration
     * @param reactivationKey reactivation key to be used for registration
     * @param proxyId system ID of proxy to use
     * @param saltSSH manage system with Salt SSH
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc Bootstrap a system for management via either Salt or Salt SSH.
     * Use SSH private key for authentication.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "host", "Hostname or IP address of target")
     * @apidoc.param #param_desc("int", "sshPort", "SSH port on target machine")
     * @apidoc.param #param_desc("string", "sshUser", "SSH user on target machine")
     * @apidoc.param #param_desc("string", "sshPrivKey", "SSH private key as a string in PEM format")
     * @apidoc.param #param_desc("string", "sshPrivKeyPass",
     * "SSH passphrase for the key (use empty string for no passphrase)")
     * @apidoc.param #param_desc("string", "activationKey", "Activation key")
     * @apidoc.param #param_desc("string", "reactivationKey", "Reactivation key")
     * @apidoc.param #param_desc("int", "proxyId", "System ID of proxy to use")
     * @apidoc.param #param_desc("boolean", "saltSSH", "Manage system with Salt SSH")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrapWithPrivateSshKey(User user, String host, Integer sshPort, String sshUser,
            String sshPrivKey, String sshPrivKeyPass, String activationKey, String reactivationKey,
            Integer proxyId, Boolean saltSSH) {
        List<String> activationKeys = maybeActivationKeys(activationKey);
        BootstrapParameters params = new BootstrapParameters(host, of(sshPort), sshUser, sshPrivKey,
                maybeString(sshPrivKeyPass), activationKeys, maybeString(reactivationKey), true,
                of(proxyId.longValue()));
        log.debug("bootstrapWithPrivateSshKey called with reactivation key and proxyId: {}", params);
        return xmlRpcSystemHelper.bootstrap(user, params, saltSSH);
    }

    /**
     * Schedule highstate application for a given system.
     *
     * @param loggedInUser The current user
     * @param sid The system id of the target system
     * @param earliestOccurrence Earliest occurrence
     * @param test Run states in test-only mode
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Schedule highstate application for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "test", "Run states in test-only mode")
     * @apidoc.returntype #param("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleApplyHighstate(User loggedInUser, Integer sid, Date earliestOccurrence, Boolean test) {
        return scheduleApplyHighstate(loggedInUser, Arrays.asList(sid), earliestOccurrence, test);
    }

    /**
     * Schedule highstate application for a list of systems.
     *
     * @param loggedInUser The current user
     * @param sids The list of system id of the target systems
     * @param earliestOccurrence Earliest occurrence
     * @param test Run states in test-only mode
     * @return list of action id or exception thrown otherwise
     *
     * @apidoc.doc Schedule highstate application for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "test", "Run states in test-only mode")
     * @apidoc.returntype #param("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleApplyHighstate(User loggedInUser, List<Integer> sids, Date earliestOccurrence, Boolean test) {
        List<Long> sysids = sids.stream().map(Integer::longValue).collect(Collectors.toList());
        try {
            List<Long> visible = MinionServerFactory.lookupVisibleToUser(loggedInUser)
                    .map(Server::getId).collect(Collectors.toList());
            if (!visible.containsAll(sysids)) {
                sysids.removeAll(visible);
                throw new UnsupportedOperationException("Some System not managed with Salt: " + sysids);
            }

            Action a = ActionManager.scheduleApplyHighstate(loggedInUser, sysids, earliestOccurrence,
                    Optional.of(test));
            a = ActionFactory.save(a);
            taskomaticApi.scheduleActionExecution(a);
            return a.getId();
        }
        catch (LookupException e) {
            throw new NoSuchSystemException(e);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Schedule state application for a given system.
     *
     * @param loggedInUser The current user
     * @param sid The system id of the target system
     * @param stateNames A list of state names to be applied
     * @param earliestOccurrence Earliest occurrence
     * @param test Run states in test-only mode
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Schedule highstate application for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #array_single("string", "stateNames")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "test", "Run states in test-only mode")
     * @apidoc.returntype #param("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleApplyStates(User loggedInUser, Integer sid, List<String> stateNames,
            Date earliestOccurrence, Boolean test) {
        return scheduleApplyStates(loggedInUser, Arrays.asList(sid), stateNames,
                earliestOccurrence, test);
    }

    /**
     * Schedule state application for a list of systems.
     *
     * @param loggedInUser The current user
     * @param sids The list of system id of the target systems
     * @param stateNames A list of state names to be applied
     * @param earliestOccurrence Earliest occurrence
     * @param test Run states in test-only mode
     * @return list of action id or exception thrown otherwise
     *
     * @apidoc.doc Schedule highstate application for a given system.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #array_single("string", "stateNames")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param_desc("boolean", "test", "Run states in test-only mode")
     * @apidoc.returntype #param("int", "actionId", "The action id of the scheduled action")
     */
    public Long scheduleApplyStates(User loggedInUser, List<Integer> sids, List<String> stateNames,
            Date earliestOccurrence, Boolean test) {
        List<Long> sysids = sids.stream().map(Integer::longValue).collect(Collectors.toList());
        try {
            List<Long> visible = MinionServerFactory.lookupVisibleToUser(loggedInUser)
                    .map(Server::getId).collect(Collectors.toList());
            if (!visible.containsAll(sysids)) {
                sysids.removeAll(visible);
                throw new UnsupportedOperationException("Some System not managed with Salt: " + sysids);
            }

            Action a = ActionManager.scheduleApplyStates(loggedInUser, sysids, stateNames, earliestOccurrence,
                    Optional.of(test));
            a = ActionFactory.save(a);
            taskomaticApi.scheduleActionExecution(a);
            return a.getId();
        }
        catch (LookupException e) {
            throw new NoSuchSystemException(e);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Update the package state of a given system(High state would be needed to actually install/remove the package)
     *
     * @param loggedInUser The current user
     * @param sid The system id of the target system
     * @param packageName name of the package
     * @param state state of the package (0 = installed, 1= removed, 2 = unmanaged)
     * @param versionConstraint latest version should be installed or any (0 = latest, 1= any)
     * @return 1 on success, 0 on failure
     *
     * @apidoc.doc  Update the package state of a given system
     *                          (High state would be needed to actually install/remove the package)
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param_desc("string", "packageName", "Name of the package")
     * @apidoc.param #param_desc("int", "state", "0 = installed, 1 = removed, 2 = unmanaged ")
     * @apidoc.param #param_desc("int", "versionConstraint", "0 = latest, 1 = any ")
     * @apidoc.returntype 1 on success, exception on failure
     */
    public int updatePackageState(User loggedInUser, Integer sid, String packageName, Integer state,
                                  Integer versionConstraint) {
        try {
            //validation
            MinionServer minion = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser).asMinionServer()
                    .orElseThrow(() -> new UnsupportedOperationException("System not managed with Salt: " + sid));
            PackageStates vPkgState = PackageStates.byId(state)
                    .orElseThrow(()-> new IllegalArgumentException("Invalid package state"));
            VersionConstraints vVersionConstraint = VersionConstraints.byId(versionConstraint)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid version constraint"));
            PackageName pkgName = Optional.ofNullable(PackageManager.lookupPackageName(packageName))
                    .orElseThrow(() -> new IllegalArgumentException("No such package exists"));

            //update the state
           SystemManager.updatePackageState(loggedInUser, minion, pkgName, vPkgState, vVersionConstraint);
            return 1;
        }
        catch (LookupException e) {
            throw new NoSuchSystemException(e);
       }
    }

    /**
     * Return a map from Salt minion IDs to System IDs.
     * Map entries are limited to systems that are visible by the current user.
     *
     * @param loggedInUser The current user
     * @return the minion ID to system ID map
     *
     * @apidoc.doc Return a map from Salt minion IDs to System IDs.
     * Map entries are limited to systems that are visible by the current user.
     * @apidoc.param #session_key()
     * @apidoc.returntype #param_desc("map", "id_map", "minion IDs to system IDs")
     */
    @ReadOnly
    public Map<String, Long> getMinionIdMap(User loggedInUser) {
        return ServerFactory.getMinionIdMap(loggedInUser.getId());
    }

    /**
     * List possible migration targets for given system
     * @param loggedInUser The current user
     * @param sid Server ID
     * @return Array of migration targets for given system
     *
     * @apidoc.doc List possible migration targets for a system
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.returntype
     *      #return_array_begin()
     *           $PackageStateSerializer
     *      #array_end()
     */
    @ReadOnly
    public Set<PackageState> listPackageState(User loggedInUser, Integer sid) {
        MinionServer minion = SystemManager.lookupByIdAndUser(sid.longValue(), loggedInUser).asMinionServer()
                .orElseThrow(() -> new UnsupportedOperationException("System not managed with Salt: " + sid));
        return StateFactory.latestPackageStates(minion).orElse(Collections.emptySet());
    }

    /**
     * Gets the groups information a system is member of, for all the systems visible to the passed user and that are
     * entitled with the passed entitlement
     *
     * @param loggedInUser The current user
     * @param entitlement The entitlement
     * @return ta list of SystemGroupsInfo
     *
     * @throws FaultException A FaultException is thrown if a valid user can not
     * be found from the passed in session key
     *
     * @apidoc.doc Returns the groups information a system is member of, for all the systems visible to the passed user
     * and that are entitled with the passed entitlement.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "entitlement")
     * @apidoc.returntype
     *   #return_array_begin()
     *     $SystemGroupsDTOSerializer
     *   #array_end()
     */
    @ReadOnly
    public List<SystemGroupsDTO> listSystemGroupsForSystemsWithEntitlement(User loggedInUser, String entitlement) {
        return this.systemManager.retrieveSystemGroupsForSystemsWithEntitlementAndUser(loggedInUser, entitlement);
    }

    /**
     * Refresh all the pillar data of a list of systems.
     *
     * @param loggedInUser The current user
     * @param sids A list of systems ids to refresh
     * @return Returns the list of skipped systems IDs
     *
     * @apidoc.doc refresh all the pillar data of a list of systems.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.returntype #array_single("int", "skippedIds", "System IDs which couldn't be refreshed")
     */
    public List<Integer> refreshPillar(User loggedInUser, List<Integer> sids) {
        return refreshPillar(loggedInUser, null, sids);
    }

    /**
     * Refresh the pillar data of a list of systems.
     *
     * @param loggedInUser The current user
     * @param subset the string representation of the pillar subset
     * @param sids A list of systems ids to refresh
     * @return Returns the list of skipped systems IDs
     *
     * @apidoc.doc refresh the pillar data of a list of systems. The subset value represents the pillar to be refreshed
     * and can be one of 'general', 'group_membership', 'virtualization' or 'custom_info'.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "subset", "subset of the pillar to refresh.")
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.returntype #array_single("int", "skippedIds", "System IDs which couldn't be refreshed")
     */
    public List<Integer> refreshPillar(User loggedInUser, String subset, List<Integer> sids) {
        List<Integer> skipped = new ArrayList<>();
        MinionPillarManager.PillarSubset subsetValue = subset != null ?
                MinionPillarManager.PillarSubset.valueOf(subset.toUpperCase()) :
                null;
        for (Integer sysId : sids) {
            if (SystemManager.isAvailableToUser(loggedInUser, sysId.longValue())) {
                Server system = SystemManager.lookupByIdAndUser(Long.valueOf(sysId), loggedInUser);
                system.asMinionServer().ifPresentOrElse(
                    minionServer -> {
                        if (subsetValue != null) {
                            MinionPillarManager.INSTANCE.generatePillar(minionServer, true, subsetValue);
                        }
                        else {
                            MinionPillarManager.INSTANCE.generatePillar(minionServer);
                        }
                    },
                    () -> {
                        log.warn("system {} is not a salt minion, hence pillar will not be updated", sysId);
                        skipped.add(sysId);
                    }
                );
            }
            else {
                log.warn("system {} is not available to user, hence pillar will not be refreshed", sysId);
                skipped.add(sysId);
            }
        }
        return skipped;
    }

    /**
     * Connect given systems to another proxy.
     *
     * @param loggedInUser The current user
     * @param sids A list of systems ids
     * @param proxyId Id of the proxy or 0 for direct connection to SUMA server
     * @return Returns a list of scheduled action ids
     *
     * @apidoc.doc Connect given systems to another proxy.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.param #param("int", "proxyId")
     * @apidoc.returntype #array_single("int", "actionIds", "list of scheduled action ids")
     */

    public List<Long> changeProxy(User loggedInUser, List<Integer> sids, Integer proxyId) {
        List<Long> sysids = sids.stream().map(Integer::longValue).collect(Collectors.toList());
        try {
            return ActionManager.changeProxy(loggedInUser, sysids, proxyId.longValue());
        }
        catch (LookupException e) {
            throw new NoSuchSystemException(e);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        catch (java.lang.UnsupportedOperationException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    /**
     * Only needed for unit tests.
     * @return the {@link TaskomaticApi} instance used by this class
     * @apidoc.ignore
     */
    @ApiIgnore
    public TaskomaticApi getTaskomaticApi() {
        return taskomaticApi;
    }

    private static Optional<String> maybeString(String sshPassword) {
        return StringUtils.isEmpty(sshPassword) ? empty() : of(sshPassword);
    }

    private static List<String> maybeActivationKeys(String activationKey) {
        return StringUtils.isEmpty(activationKey) ? Collections.emptyList() : List.of(activationKey);
    }

}
