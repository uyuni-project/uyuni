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
package com.redhat.rhn.frontend.xmlrpc.channel.software;

import static com.redhat.rhn.manager.channel.CloneChannelCommand.CloneBehavior.CURRENT_STATE;
import static com.redhat.rhn.manager.channel.CloneChannelCommand.CloneBehavior.ORIGINAL_STATE;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.client.InvalidCertificateException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.ContentSourceFilter;
import com.redhat.rhn.domain.channel.InvalidChannelRoleException;
import com.redhat.rhn.domain.errata.ClonedErrata;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.events.UpdateErrataCacheEvent;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelNameException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParentChannelException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchContentSourceException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchPackageException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoLabelException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoUrlException;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.user.XmlRpcUserHelper;
import com.redhat.rhn.manager.channel.ChannelEditor;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.manager.channel.CreateChannelCommand;
import com.redhat.rhn.manager.channel.UpdateChannelCommand;
import com.redhat.rhn.manager.channel.repo.BaseRepoCommand;
import com.redhat.rhn.manager.channel.repo.CreateRepoCommand;
import com.redhat.rhn.manager.channel.repo.EditRepoCommand;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.kickstart.crypto.NoSuchCryptoKeyException;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.task.TaskConstants;
import com.redhat.rhn.taskomatic.task.errata.ErrataCacheWorker;

import com.suse.manager.api.ApiIgnore;
import com.suse.manager.api.ReadOnly;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ChannelSoftwareHandler
 * @apidoc.namespace channel.software
 * @apidoc.doc Provides methods to access and modify many aspects of a channel.
 */
public class ChannelSoftwareHandler extends BaseHandler {

    private static Logger log = LogManager.getLogger(ChannelSoftwareHandler.class);
    private final TaskomaticApi taskomaticApi;
    private final XmlRpcSystemHelper xmlRpcSystemHelper;
    private final SystemHandler systemHandler;

    /**
     * Set the {@link TaskomaticApi} instance to use, only for unit tests.
     *
     * @param taskomaticApiIn the {@link TaskomaticApi}
     * @param xmlRpcSystemHelperIn XmlRpcSystemHelper
     * @param systemHandlerIn
     */
    public ChannelSoftwareHandler(TaskomaticApi taskomaticApiIn, XmlRpcSystemHelper xmlRpcSystemHelperIn,
                                  SystemHandler systemHandlerIn) {
        taskomaticApi = taskomaticApiIn;
        xmlRpcSystemHelper = xmlRpcSystemHelperIn;
        systemHandler = systemHandlerIn;
    }

    /**
     * Only needed for unit tests.
     * @return the {@link TaskomaticApi} instance used by this class
     *
     * @apidoc.ignore
     */
    @ApiIgnore
    public TaskomaticApi getTaskomaticApi() {
        return taskomaticApi;
    }
    /**
     * If you have synced a new channel then patches
     * will have been updated with the packages that are in the newly synced
     * channel. A cloned erratum will not have been automatically updated
     * however. If you cloned a channel that includes those cloned errata and
     * should include the new packages, they will not be included when they
     * should. This method lists the errata that will be updated if you run the
     * syncErrata method.
     * @param loggedInUser The current user
     * @param channelLabel Label of cloned channel to check
     * @return List of errata that are missing packages
     *
     * @apidoc.doc If you have synced a new channel then patches
     * will have been updated with the packages that are in the newly
     * synced channel. A cloned erratum will not have been automatically updated
     * however. If you cloned a channel that includes those cloned errata and
     * should include the new packages, they will not be included when they
     * should. This method lists the errata that will be updated if you run the
     * syncErrata method.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to update")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ErrataOverview> listErrataNeedingSync(User loggedInUser,
                String channelLabel) {
        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);

        return ChannelManager.listErrataNeedingResync(channel, loggedInUser);
    }

    /**
     * If you have synced a new channel then patches
     * will have been updated with the packages that are in the newly synced
     * channel. A cloned erratum will not have been automatically updated
     * however. If you cloned a channel that includes those cloned errata and
     * should include the new packages, they will not be included when they
     * should. This method updates all the errata in the given cloned channel
     * with packages that have recently been added, and ensures that all the
     * packages you expect are in the channel. It also updates cloned errata
     * attributes like advisoryStatus.
     * @param loggedInUser The current user
     * @param channelLabel Label of cloned channel to update
     * @return Returns 1 if successfull, FaultException otherwise
     * @throws NoSuchChannelException thrown if no channel is found.
     *
     * @apidoc.doc If you have synced a new channel then patches
     * will have been updated with the packages that are in the newly
     * synced channel. A cloned erratum will not have been automatically updated
     * however. If you cloned a channel that includes those cloned errata and
     * should include the new packages, they will not be included when they
     * should. This method updates all the errata in the given cloned channel
     * with packages that have recently been added, and ensures that all the
     * packages you expect are in the channel. It also updates cloned errata
     * attributes like advisoryStatus.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to update")
     * @apidoc.returntype  #return_int_success()
     */
    public Integer syncErrata(User loggedInUser, String channelLabel) {
        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        //Verify permissions
        if (!(UserManager.verifyChannelAdmin(loggedInUser, channel) ||
                loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN))) {
            throw new PermissionCheckFailureException();
        }

        List<ErrataOverview> errata = ChannelManager.listErrataNeedingResync(channel,
                loggedInUser);
        List<Long> eids = new ArrayList<>();
        for (ErrataOverview e : errata) {
            eids.add(e.getId());
        }

        List<PackageOverview> packages = ChannelManager
                .listErrataPackagesForResync(channel, loggedInUser);
        List<Long> pids = new ArrayList<>();
        for (PackageOverview p : packages) {
            pids.add(p.getId());
        }

        ChannelEditor.getInstance().addPackages(loggedInUser, channel, pids);

        for (Long eid : eids) {
            Errata e = ErrataManager.lookupErrata(eid, loggedInUser);
            if (e.isCloned()) {
                ErrataFactory.syncErrataDetails((ClonedErrata) e);
            }
            else {
                log.fatal("Tried to sync errata with id {} but it was not cloned", eid);
            }
        }
        return 1;
    }

    /**
     * Lists the packages with the latest version (including release and epoch)
     * for the unique package names
     * @param loggedInUser The current user
     * @param channelLabel Label of channel whose package are sought.
     * @return Lists the packages with the largest version (including release
     * and epoch) for the unique package names
     * @throws NoSuchChannelException thrown if no channel is found.
     *
     * @apidoc.doc Lists the packages with the latest version (including release and
     * epoch) for the given channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("package")
     *              #prop("string", "name")
     *              #prop("string", "version")
     *              #prop("string", "release")
     *              #prop("string", "epoch")
     *              #prop("int", "id")
     *              #prop("string", "arch_label")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public Object[] listLatestPackages(User loggedInUser, String channelLabel)
        throws NoSuchChannelException {

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);

        List<Map<String, Object>> pkgs = ChannelManager.latestPackagesInChannel(channel);
        return pkgs.toArray();
    }

    /**
     * Lists all packages in the channel, regardless of version, between the
     * given dates.
     * @param loggedInUser The current user
     * @param channelLabel Label of channel whose package are sought.
     * @param startDate last modified begin date (as a string)
     * @param endDate last modified end date (as a string)
     * @return all packages in the channel, regardless of version between the
     * given dates.
     * @throws NoSuchChannelException thrown if no channel is found.
     *
     * @apidoc.doc Lists all packages in the channel, regardless of package version,
     * between the given dates.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.param #param($date, "startDate")
     * @apidoc.param #param($date, "endDate")
     * @apidoc.returntype
     *      #return_array_begin()
     *              $PackageDtoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<PackageDto> listAllPackages(User loggedInUser, String channelLabel,
            Date startDate, Date endDate) throws NoSuchChannelException {

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        return ChannelManager.listAllPackages(channel, startDate, endDate);
    }

    /**
     * Lists all packages in the channel, regardless of version whose last
     * modified date is greater than given date.
     * @param loggedInUser The current user
     * @param channelLabel Label of channel whose package are sought.
     * @param startDate last modified begin date (as a string)
     * @return all packages in the channel, regardless of version whose last
     * modified date is greater than given date.
     * @throws NoSuchChannelException thrown if no channel is found.
     *
     * @apidoc.doc Lists all packages in the channel, regardless of version whose last
     * modified date is greater than given date.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.param #param($date, "startDate")
     * @apidoc.returntype
     *      #return_array_begin()
     *              $PackageDtoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<PackageDto> listAllPackages(User loggedInUser, String channelLabel,
            Date startDate) throws NoSuchChannelException {
        return listAllPackages(loggedInUser, channelLabel, startDate, null);
    }

    /**
     * Lists all packages in the channel, regardless of version
     * @param loggedInUser The current user
     * @param channelLabel Label of channel whose package are sought.
     * @return all packages in the channel, regardless of version
     * @throws NoSuchChannelException thrown if no channel is found.
     *
     * @apidoc.doc Lists all packages in the channel, regardless of the package version
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.returntype
     *      #return_array_begin()
     *              $PackageDtoSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<PackageDto> listAllPackages(User loggedInUser, String channelLabel)
        throws NoSuchChannelException {

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        return ChannelManager.listAllPackages(channel);
    }

    /**
     * Return Lists potential software channel arches that can be created
     * @param loggedInUser The current user
     * @return Lists potential software channel arches that can be created
     * @throws PermissionCheckFailureException thrown if the user is not a
     * channel admin
     *
     * @apidoc.doc Lists the potential software channel architectures that can be created
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *          #return_array_begin()
     *              $ChannelArchSerializer
     *          #array_end()
     */
    @ReadOnly
    public List<ChannelArch> listArches(User loggedInUser)
            throws PermissionCheckFailureException {
        if (!loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN)) {
            throw new PermissionCheckFailureException();
        }

        return ChannelManager.getChannelArchitectures();
    }

    /**
     * Deletes a software channel and then also schedule channel state for the minions which had this channel
     * @param loggedInUser The current user
     * @param channelLabel Label of channel to be deleted.
     * @return 1 if Channel was successfully deleted.
     * @throws PermissionCheckFailureException thrown if User has no access to
     * delete channel.
     * @throws NoSuchChannelException thrown if label is invalid.
     *
     * @apidoc.doc Deletes a custom software channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to delete")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String channelLabel)
            throws PermissionCheckFailureException, NoSuchChannelException {
        try {

            Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
            List<MinionServer> minions = ServerFactory.listMinionsByChannel(channel.getId());
            ChannelManager.deleteChannel(loggedInUser, channelLabel);
            ChannelManager.applyChannelState(loggedInUser, minions);
        }
        catch (InvalidChannelRoleException e) {
            throw new PermissionCheckFailureException(e);
        }
        catch (PermissionException e) {
            throw new FaultException(1234, "permissions", e.getMessage(), new String[] {});
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        catch (com.redhat.rhn.common.validator.ValidatorException e) {
            throw new ValidationException(e.getMessage());
        }
        return 1;
    }

    /**
     * Returns whether the channel is subscribable by any user in the
     * organization.
     * @param loggedInUser The current user
     * @param channelLabel Label of channel to be deleted.
     * @return 1 if the Channel is globally subscribable, 0 otherwise.
     *
     * @apidoc.doc Returns whether the channel is subscribable by any user
     * in the organization
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.returntype #param_desc("int", "subscribable", "1 if true, 0 otherwise")
     */
    @ReadOnly
    public int isGloballySubscribable(User loggedInUser, String channelLabel) {
        // TODO: this should return a boolean NOT an int

        // Make sure the channel exists:
        lookupChannelByLabel(loggedInUser, channelLabel);

        return ChannelManager.isGloballySubscribable(loggedInUser, channelLabel) ? 1 : 0;
    }

    /**
     * Returns the details of the given channel as a map with the following
     * keys:
     * @param loggedInUser The current user
     * @param channelLabel Label of channel whose details are sought.
     * @throws NoSuchChannelException thrown if no channel is found.
     * @return the channel requested.
     *
     * @apidoc.doc Returns details of the given channel as a map
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.returntype
     *     $ChannelSerializer
     */
    @ReadOnly
    public Channel getDetails(User loggedInUser, String channelLabel)
        throws NoSuchChannelException {
        return lookupChannelByLabel(loggedInUser, channelLabel);
    }

    /**
     * Returns the requested channel
     * @param loggedInUser The current user
     * @param id - id of channel wanted
     * @throws NoSuchChannelException thrown if no channel is found.
     * @return the channel requested.
     *
     * @apidoc.doc Returns details of the given channel as a map
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "id", "channel to query")
     * @apidoc.returntype
     *     $ChannelSerializer
     */
    @ReadOnly
    public Channel getDetails(User loggedInUser, Integer id)
        throws NoSuchChannelException {
        return lookupChannelById(loggedInUser, id.longValue());
    }

    /**
     * Allows to modify channel attributes
     * @param loggedInUser The current user
     * @param channelLabel label of channel to be modified
     * @param details map of channel attributes to be changed
     * @return 1 if edit was successful, exception thrown otherwise
     *
     * @apidoc.doc Allows to modify channel attributes
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel label")
     * @apidoc.param
     *  #struct_begin("details")
     *      #prop_desc("string", "checksum_label", "new channel repository checksum label
     *          (optional)")
     *      #prop_desc("string", "name", "new channel name (optional)")
     *      #prop_desc("string", "summary", "new channel summary (optional)")
     *      #prop_desc("string", "description", "new channel description (optional)")
     *      #prop_desc("string", "maintainer_name", "new channel maintainer name
     *          (optional)")
     *      #prop_desc("string", "maintainer_email", "new channel email address
     *          (optional)")
     *      #prop_desc("string", "maintainer_phone", "new channel phone number (optional)")
     *      #prop_desc("string", "gpg_key_url", "new channel gpg key url (optional)")
     *      #prop_desc("string", "gpg_key_id", "new channel gpg key id (optional)")
     *      #prop_desc("string", "gpg_key_fp", "new channel gpg key fingerprint
     *          (optional)")
     *      #prop_desc("string", "gpg_check", "enable/disable gpg check (optional)")
     *
     *  #struct_end()
     *@apidoc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, String channelLabel, Map<String, String> details) {
        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        return setDetails(loggedInUser, channel.getId().intValue(), details);
    }

    /**
     * Allows to modify channel attributes
     * @param loggedInUser The current user
     * @param channelId id of channel to be modified
     * @param details map of channel attributes to be changed
     * @return 1 if edit was successful, exception thrown otherwise
     *
     * @apidoc.doc Allows to modify channel attributes
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "channelId", "channel id")
     * @apidoc.param
     *  #struct_begin("details")
     *      #prop_desc("string", "checksum_label", "new channel repository checksum label
     *          (optional)")
     *      #prop_desc("string", "name", "new channel name (optional)")
     *      #prop_desc("string", "summary", "new channel summary (optional)")
     *      #prop_desc("string", "description", "new channel description (optional)")
     *      #prop_desc("string", "maintainer_name", "new channel maintainer name
     *          (optional)")
     *      #prop_desc("string", "maintainer_email", "new channel email address
     *          (optional)")
     *      #prop_desc("string", "maintainer_phone", "new channel phone number (optional)")
     *      #prop_desc("string", "gpg_key_url", "new channel gpg key url (optional)")
     *      #prop_desc("string", "gpg_key_id", "new channel gpg key id (optional)")
     *      #prop_desc("string", "gpg_key_fp", "new channel gpg key fingerprint
     *          (optional)")
     *      #prop_desc("string", "gpg_check", "enable/disable gpg check
     *          (optional)")
     *  #struct_end()

     *@apidoc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, Integer channelId, Map<String,
            String> details) {
        channelAdminPermCheck(loggedInUser);

        Channel channel = lookupChannelById(loggedInUser, channelId.longValue());
        Set<String> validKeys = new HashSet<>();
        validKeys.add("checksum_label");
        validKeys.add("name");
        validKeys.add("summary");
        validKeys.add("description");
        validKeys.add("maintainer_name");
        validKeys.add("maintainer_email");
        validKeys.add("maintainer_phone");
        validKeys.add("gpg_key_url");
        validKeys.add("gpg_key_id");
        validKeys.add("gpg_key_fp");
        validKeys.add("gpg_check");
        validKeys.add("vendor_channel");
        validateMap(validKeys, details);

        UpdateChannelCommand ucc = new UpdateChannelCommand(loggedInUser, channel);

        setChangedValues(ucc, details);

        ucc.update(channelId.longValue());
        ServerFactory.listMinionsByChannel(channelId).stream()
                .forEach(ms -> MinionPillarManager.INSTANCE.generatePillar(ms, false, Collections.emptySet()));
        return 1;
    }

    /**
     * Set the values to the command.
     * @param command CreateChannelCommand command
     * @param details Map Key/value pairs of changed values
     */
    private void setChangedValues(CreateChannelCommand command, Map<String, String> details) {

        if (details.containsKey("name")) {
            command.setName(details.get("name"));
        }

        if (details.containsKey("label")) {
            command.setLabel(details.get("label"));
        }

        if (details.containsKey("parent_label")) {
            command.setParentLabel(details.get("parent_label"));
        }

        if (details.containsKey("arch_label")) {
            command.setArchLabel(details.get("arch_label"));
        }

        if (details.containsKey("checksum")) {
            command.setChecksumLabel(details.get("checksum"));
        }
        else if (details.containsKey("checksum_label")) {
            command.setChecksumLabel(details.get("checksum_label"));
        }

        if (details.containsKey("summary")) {
            command.setSummary(details.get("summary"));
        }

        if (details.containsKey("description")) {
            command.setDescription(details.get("description"));
        }

        if (details.containsKey("gpg_key_url")) {
            command.setGpgKeyUrl(details.get("gpg_key_url"));
        }
        else if (details.containsKey("gpg_url")) {
            command.setGpgKeyUrl(details.get("gpg_url"));
        }

        if (details.containsKey("gpg_key_id")) {
            command.setGpgKeyId(details.get("gpg_key_id"));
        }
        else if (details.containsKey("gpg_id")) {
            command.setGpgKeyId(details.get("gpg_id"));
        }

        if (details.containsKey("gpg_key_fp")) {
            command.setGpgKeyFp(details.get("gpg_key_fp"));
        }
        else if (details.containsKey("gpg_fingerprint")) {
            command.setGpgKeyFp(details.get("gpg_fingerprint"));
        }

        if (details.containsKey("gpg_check")) {
            command.setGpgCheck(Boolean.parseBoolean(details.get("gpg_check")));
        }

        if (details.containsKey("vendor_channel")) {
            command.setVendorChannel(Boolean.parseBoolean(details.get("vendor_channel")));
        }

        if (details.containsKey("maintainer_name")) {
            command.setMaintainerName(details.get("maintainer_name"));
        }

        if (details.containsKey("maintainer_email")) {
            command.setMaintainerEmail(details.get("maintainer_email"));
        }

        if (details.containsKey("maintainer_phone")) {
            command.setMaintainerPhone(details.get("maintainer_phone"));
        }

    }

    /**
     * Creates a software channel, parent_channel_label can be empty string
     * @param loggedInUser The current user
     * @param label Channel label to be created
     * @param name Name of Channel
     * @param summary Channel Summary
     * @param archLabel Architecture label
     * @param parentLabel Parent Channel label (may be null)
     * @param checksumType checksum type for this channel
     * @param gpgKey a map consisting of string url, string id, string fingerprint
     * @param gpgCheck GPG check enable/disable
     * @return 1 if creation of channel succeeds.
     * @since 10.9
     * @throws PermissionCheckFailureException  thrown if user does not have
     * permission to create the channel.
     * @throws InvalidChannelNameException thrown if given name is in use or
     * otherwise, invalid.
     * @throws InvalidChannelLabelException throw if given label is in use or
     * otherwise, invalid.
     * @throws InvalidParentChannelException thrown if parent label is for a
     * channel that is not a base channel.
     *
     * @apidoc.doc Creates a software channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "label of the new channel")
     * @apidoc.param #param_desc("string", "name", "name of the new channel")
     * @apidoc.param #param_desc("string", "summary" "summary of the channel")
     * @apidoc.param #param_desc("string", "archLabel",
     *              "the label of the architecture the channel corresponds to,
     *              run channel.software.listArches API for complete listing")
     * @apidoc.param #param_desc("string", "parentLabel", "label of the parent of this
     *              channel, an empty string if it does not have one")
     * @apidoc.param #param_desc("string", "checksumType", "checksum type for this channel,
     *              used for yum repository metadata generation")
     *      #options()
     *          #item_desc ("sha1", "offers widest compatibility with clients")
     *          #item_desc ("sha256", "offers highest security, but is compatible
     *                        only with newer clients: Fedora 11 and newer,
     *                        or Enterprise Linux 6 and newer.")
     *      #options_end()
     * @apidoc.param
     *      #struct_begin("gpgKey")
     *          #prop_desc("string", "url", "GPG key URL")
     *          #prop_desc("string", "id", "GPG key ID")
     *          #prop_desc("string", "fingerprint", "GPG key Fingerprint")
     *      #struct_end()
     * @apidoc.param #param_desc("boolean", "gpgCheck", "true if the GPG check should be
     *     enabled by default, false otherwise")

     * @apidoc.returntype #param_desc("int", "status", "1 if the creation operation succeeded, 0 otherwise")
     */
    public int create(User loggedInUser, String label, String name,
                      String summary, String archLabel, String parentLabel,
                      String checksumType, Map<String, String> gpgKey, boolean gpgCheck)
            throws PermissionCheckFailureException, InvalidChannelLabelException,
            InvalidChannelNameException, InvalidParentChannelException {

        if (!loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN)) {
            throw new PermissionCheckFailureException();
        }
        CreateChannelCommand ccc = new CreateChannelCommand();
        ccc.setArchLabel(archLabel);
        ccc.setLabel(label);
        ccc.setName(name);
        ccc.setSummary(summary);
        ccc.setParentLabel(parentLabel);
        ccc.setUser(loggedInUser);
        ccc.setChecksumLabel(checksumType);
        ccc.setGpgKeyUrl(gpgKey.get("url"));
        ccc.setGpgKeyId(gpgKey.get("id"));
        ccc.setGpgKeyFp(gpgKey.get("fingerprint"));
        ccc.setGpgCheck(gpgCheck);

        return (ccc.create() != null) ? 1 : 0;
    }
    /**
     * Creates a software channel, parent_channel_label can be empty string
     * @param loggedInUser The current user
     * @param label Channel label to be created
     * @param name Name of Channel
     * @param summary Channel Summary
     * @param archLabel Architecture label
     * @param parentLabel Parent Channel label (may be null)
     * @param checksumType checksum type for this channel
     * @param gpgKey a map consisting of
     *      <li>string url</li>
     *      <li>string id</li>
     *      <li>string fingerprint</li>
     * @return 1 if creation of channel succeeds.
     * @since 10.9
     * @throws PermissionCheckFailureException  thrown if user does not have
     * permission to create the channel.
     * @throws InvalidChannelNameException thrown if given name is in use or
     * otherwise, invalid.
     * @throws InvalidChannelLabelException throw if given label is in use or
     * otherwise, invalid.
     * @throws InvalidParentChannelException thrown if parent label is for a
     * channel that is not a base channel.
     *
     * @apidoc.doc Creates a software channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "label of the new channel")
     * @apidoc.param #param_desc("string", "name", "name of the new channel")
     * @apidoc.param #param_desc("string", "summary" "summary of the channel")
     * @apidoc.param #param_desc("string", "archLabel",
     *              "the label of the architecture the channel corresponds to,
     *              run channel.software.listArches API for complete listing")
     * @apidoc.param #param_desc("string", "parentLabel", "label of the parent of this
     *              channel, an empty string if it does not have one")
     * @apidoc.param #param_desc("string", "checksumType", "checksum type for this channel,
     *              used for yum repository metadata generation")
     *      #options()
     *          #item_desc ("sha1", "offers widest compatibility with clients")
     *          #item_desc ("sha256", "offers highest security, but is compatible
     *                        only with newer clients: Fedora 11 and newer,
     *                        or Enterprise Linux 6 and newer.")
     *      #options_end()
     * @apidoc.param
     *      #struct_begin("gpgKey")
     *          #prop_desc("string", "url", "GPG key URL")
     *          #prop_desc("string", "id", "GPG key ID")
     *          #prop_desc("string", "fingerprint", "GPG key Fingerprint")
     *      #struct_end()
     * @apidoc.returntype #param_desc("int", "status", "1 if the creation operation succeeded, 0 otherwise")
     */
    public int create(User loggedInUser, String label, String name,
            String summary, String archLabel, String parentLabel, String checksumType,
            Map<String, String> gpgKey)
        throws PermissionCheckFailureException, InvalidChannelLabelException,
               InvalidChannelNameException, InvalidParentChannelException {

        return create(loggedInUser, label, name, summary, archLabel, parentLabel,
                checksumType, gpgKey, true);
    }

    /**
     * Creates a software channel, parent_channel_label can be empty string
     * @param loggedInUser The current user
     * @param label Channel label to be created
     * @param name Name of Channel
     * @param summary Channel Summary
     * @param archLabel Architecture label
     * @param parentLabel Parent Channel label (may be null)
     * @param checksumType checksum type for this channel
     * @return 1 if creation of channel succeeds.
     * @since 10.9
     * @throws PermissionCheckFailureException  thrown if user does not have
     * permission to create the channel.
     * @throws InvalidChannelNameException thrown if given name is in use or
     * otherwise, invalid.
     * @throws InvalidChannelLabelException throw if given label is in use or
     * otherwise, invalid.
     * @throws InvalidParentChannelException thrown if parent label is for a
     * channel that is not a base channel.
     *
     * @apidoc.doc Creates a software channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "label of the new channel")
     * @apidoc.param #param_desc("string", "name", "name of the new channel")
     * @apidoc.param #param_desc("string", "summary" "summary of the channel")
     * @apidoc.param #param_desc("string", "archLabel",
     *              "the label of the architecture the channel corresponds to,
     *              run channel.software.listArches API for complete listing")
     * @apidoc.param #param_desc("string", "parentLabel", "label of the parent of this
     *              channel, an empty string if it does not have one")
     * @apidoc.param #param_desc("string", "checksumType", "checksum type for this channel,
     *              used for yum repository metadata generation")
     *      #options()
     *          #item_desc ("sha1", "offers widest compatibility with clients")
     *          #item_desc ("sha256", "offers highest security, but is compatible
     *                        only with newer clients: Fedora 11 and newer,
     *                        or Enterprise Linux 6 and newer.")
     *      #options_end()
     * @apidoc.returntype #param_desc("int", "status", "1 if the creation operation succeeded, 0 otherwise")
     */

    public int create(User loggedInUser, String label, String name,
            String summary, String archLabel, String parentLabel, String checksumType)
        throws PermissionCheckFailureException, InvalidChannelLabelException,
               InvalidChannelNameException, InvalidParentChannelException {

        return create(loggedInUser, label, name,
                summary, archLabel, parentLabel, checksumType,
                new HashMap<>());
    }

    /**
     * Creates a software channel, parent_channel_label can be empty string
     * @param loggedInUser The current user
     * @param label Channel label to be created
     * @param name Name of Channel
     * @param summary Channel Summary
     * @param archLabel Architecture label
     * @param parentLabel Parent Channel label (may be null)
     * @return 1 if creation of channel succeeds.
     * @throws PermissionCheckFailureException  thrown if user does not have
     * permission to create the channel.
     * @throws InvalidChannelNameException thrown if given name is in use or
     * otherwise, invalid.
     * @throws InvalidChannelLabelException throw if given label is in use or
     * otherwise, invalid.
     * @throws InvalidParentChannelException thrown if parent label is for a
     * channel that is not a base channel.
     *
     * @apidoc.doc Creates a software channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "label of the new channel")
     * @apidoc.param #param_desc("string", "name", "name of the new channel")
     * @apidoc.param #param_desc("string", "summary" "summary of the channel")
     * @apidoc.param #param_desc("string", "archLabel",
     *              "the label of the architecture the channel corresponds to,
     *              run channel.software.listArches API for complete listing")
     * @apidoc.param #param_desc("string", "parentLabel", "label of the parent of this
     *              channel, an empty string if it does not have one")
     * @apidoc.returntype #param_desc("int", "status", "1 if the creation operation succeeded, 0 otherwise")
     */
    public int create(User loggedInUser, String label, String name,
            String summary, String archLabel, String parentLabel)
        throws PermissionCheckFailureException, InvalidChannelLabelException,
               InvalidChannelNameException, InvalidParentChannelException {

        return create(loggedInUser, label, name, summary, archLabel, parentLabel, "sha1");
    }

    /**
     * Set the contact/support information for given channel.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel to change
     * @param maintainerName The name of the channel maintainer
     * @param maintainerEmail The email address of the channel maintainer
     * @param maintainerPhone The phone number of the channel maintainer
     * @param supportPolicy The channel support polity
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The channelLabel is invalid
     *   - The user doesn't have channel admin permissions
     *
     * @apidoc.doc Set contact/support information for given channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "label of the channel")
     * @apidoc.param #param_desc("string", "maintainerName", "name of the channel
     * maintainer")
     * @apidoc.param #param_desc("string", "maintainerEmail", "email of the channel
     * maintainer")
     * @apidoc.param #param_desc("string", "maintainerPhone", "phone number of the channel
     * maintainer")
     * @apidoc.param #param_desc("string", "supportPolicy", "channel support policy")
     * @apidoc.returntype  #return_int_success()
     */
    public int setContactDetails(User loggedInUser, String channelLabel,
            String maintainerName, String maintainerEmail, String maintainerPhone,
            String supportPolicy)
        throws FaultException {

        if (!loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN)) {
            throw new PermissionCheckFailureException();
        }

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);

        channel.setMaintainerName(maintainerName);
        channel.setMaintainerEmail(maintainerEmail);
        channel.setMaintainerPhone(maintainerPhone);
        channel.setSupportPolicy(supportPolicy);

        ChannelFactory.save(channel);

        return 1;
    }

    /**
     * Returns list of subscribed systems for the given channel label.
     * @param loggedInUser The current user
     * @param channelLabel Label of the channel in question.
     * @return Returns an array of maps representing a system. Contains system id and
     * system name for each system subscribed to this channel.
     * @throws FaultException A FaultException is thrown if:
     *   - Logged in user is not a channel admin.
     *   - Channel does not exist.
     *
     * @apidoc.doc Returns list of subscribed systems for the given channel label
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.returntype
     *          #return_array_begin()
     *              #struct_begin("system")
     *                  #prop("int", "id")
     *                  #prop("string", "name")
     *              #struct_end()
     *           #array_end()
     */
    @ReadOnly
    public Object[] listSubscribedSystems(User loggedInUser, String channelLabel)
        throws FaultException {

        // Make sure user has access to the orgs channels
        if (!loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN)) {
            throw new PermissionCheckFailureException();
        }

        // Get the channel.
        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);

        DataResult<Map<String, Object>> dr =
                SystemManager.systemsSubscribedToChannel(channel, loggedInUser);
        for (Map<String, Object> sys : dr) {
            sys.remove("selectable");
        }
        return dr.toArray();
    }

    /**
     * Retrieve the channels for a given system id.
     * @param loggedInUser The current user
     * @param sid The id of the system in question.
     * @return Returns an array of maps representing the channels this system is
     * subscribed to.
     * @throws FaultException A FaultException is thrown if:
     *   - sessionKey is invalid
     *   - Server does not exist
     *   - User does not have access to system
     *
     * @apidoc.doc Returns a list of channels that a system is subscribed to for the
     * given system id
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "system ID")
     * @apidoc.returntype
     *          #return_array_begin()
     *              #struct_begin("channel")
     *                  #prop("string", "id")
     *                  #prop("string", "label")
     *                  #prop("string", "name")
     *              #struct_end()
     *           #array_end()
     */
    @ReadOnly
    public Object[] listSystemChannels(User loggedInUser, Integer sid)
        throws FaultException {
        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, sid);

        DataResult<Map<String, Object>> dr = SystemManager.channelsForServer(server);
        return dr.toArray();
    }

    /**
     * Set the subscribable flag for a given channel and user. If value is set to 'true',
     * this method will give the user subscribe permissions to the channel. Otherwise, this
     * method revokes that privilege.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel in question
     * @param login The login for the user in question
     * @param value The boolean value telling us whether to grant subscribe permission or
     * revoke it.
     * @return Returns 1 on success, FaultException otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The loggedInUser doesn't have permission to perform this action
     *   - The login, sessionKey, or channelLabel is invalid
     *
     * @apidoc.doc Set the subscribable flag for a given channel and user.
     * If value is set to 'true', this method will give the user
     * subscribe permissions to the channel. Otherwise, that privilege is revoked.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "label of the channel")
     * @apidoc.param #param_desc("string", "login", "login of the target user")
     * @apidoc.param #param_desc("boolean", "value", "value of the flag to set")
     * @apidoc.returntype #return_int_success()
     */
    public int setUserSubscribable(User loggedInUser, String channelLabel,
                   String login, Boolean value) throws FaultException {
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        //Verify permissions
        if (!(UserManager.verifyChannelAdmin(loggedInUser, channel) ||
              loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN))) {
            throw new PermissionCheckFailureException();
        }

        if (Boolean.TRUE.equals(value)) {
            // Add the 'subscribe' role for the target user to the channel
            ChannelManager.addSubscribeRole(target, channel);
        }
        else {
            // Remove the 'subscribe' role for the target user to the channel
            ChannelManager.removeSubscribeRole(target, channel);
        }

        return 1;
    }

    /**
     * Set the manageable flag for a given channel and user. If value is set to 'true',
     * this method will give the user manage permissions to the channel. Otherwise, this
     * method revokes that privilege.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel in question
     * @param login The login for the user in question
     * @param value The boolean value telling us whether to grant manage permission or
     * revoke it.
     * @return Returns 1 on success, FaultException otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The loggedInUser doesn't have permission to perform this action
     *   - The login, sessionKey, or channelLabel is invalid
     *
     * @apidoc.doc Set the manageable flag for a given channel and user.
     * If value is set to 'true', this method will give the user
     * manage permissions to the channel. Otherwise, that privilege is revoked.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "label of the channel")
     * @apidoc.param #param_desc("string", "login", "login of the target user")
     * @apidoc.param #param_desc("boolean", "value", "value of the flag to set")
     * @apidoc.returntype #return_int_success()
     */
    public int setUserManageable(User loggedInUser, String channelLabel,
                   String login, Boolean value) throws FaultException {
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        if (!channel.isCustom()) {
            throw new InvalidChannelException(
                    "Manageable flag is relevant for custom channels only.");
        }
        //Verify permissions
        if (!(UserManager.verifyChannelAdmin(loggedInUser, channel) ||
              loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN))) {
            throw new PermissionCheckFailureException();
        }

        if (Boolean.TRUE.equals(value)) {
            // Add the 'manage' role for the target user to the channel
            ChannelManager.addManageRole(target, channel);
        }
        else {
            // Remove the 'manage' role for the target user to the channel
            ChannelManager.removeManageRole(target, channel);
        }

        return 1;
    }

    /**
     * Returns whether the channel may be subscribed to by the given user.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel in question
     * @param login The login for the user in question
     * @return whether the channel may be subscribed to by the given user.
     * @throws FaultException thrown if
     *   - The loggedInUser doesn't have permission to perform this action
     *   - The login, sessionKey, or channelLabel is invalid
     *
     * @apidoc.doc Returns whether the channel may be subscribed to by the given user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "label of the channel")
     * @apidoc.param #param_desc("string", "login", "login of the target user")
     * @apidoc.returntype #param_desc("int", "status", "1 if subscribable, 0 if not")
     */
    @ReadOnly
    public int isUserSubscribable(User loggedInUser, String channelLabel,
            String login) throws FaultException {
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);

        Channel channel = lookupChannelByLabel(loggedInUser.getOrg(), channelLabel);
        //Verify permissions
        if (!(UserManager.verifyChannelAdmin(loggedInUser, channel) ||
              loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN))) {
            throw new PermissionCheckFailureException();
        }

        boolean flag = ChannelManager.verifyChannelSubscribe(target, channel.getId());
        return BooleanUtils.toInteger(flag);
    }

    /**
     * Returns whether the channel is existing
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel in question
     * @return whether the channel is existing
     *
     * @apidoc.doc Returns whether is existing
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "label of the channel")
     * @apidoc.returntype #param_desc("boolean", "result", "true if the channel exists")
     */
    @ReadOnly
    public boolean isExisting(User loggedInUser, String channelLabel) {
        return ChannelFactory.lookupByLabelAndUser(channelLabel, loggedInUser) != null;
    }

    /**
     * Returns whether the channel may be managed by the given user.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel in question
     * @param login The login for the user in question
     * @return whether the channel may be managed by the given user.
     * @throws FaultException thrown if
     *   - The loggedInUser doesn't have permission to perform this action
     *   - The login, sessionKey, or channelLabel is invalid
     *
     * @apidoc.doc Returns whether the channel may be managed by the given user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "label of the channel")
     * @apidoc.param #param_desc("string", "login", "login of the target user")
     * @apidoc.returntype #param_desc("int", "status", "1 if manageable, 0 if not")
     */
    @ReadOnly
    public int isUserManageable(User loggedInUser, String channelLabel,
            String login) throws FaultException {
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);

        Channel channel = lookupChannelByLabel(loggedInUser.getOrg(), channelLabel);
        if (!channel.isCustom()) {
            throw new InvalidChannelException(
                    "Manageable flag is relevant for custom channels only.");
        }
        //Verify permissions
        if (!(UserManager.verifyChannelAdmin(loggedInUser, channel) ||
              loggedInUser.hasRole(RoleFactory.CHANNEL_ADMIN))) {
            throw new PermissionCheckFailureException();
        }

        boolean flag = ChannelManager.verifyChannelManage(target, channel.getId());
        return BooleanUtils.toInteger(flag);
    }

    /**
     * Set globally subscribable attribute for given channel.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel to change
     * @param value The boolean value to set globally subscribable to.
     * @return Returns 1 if successful, exception otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionkey is invalid
     *   - The channel is invalid
     *   - The logged in user isn't a channel admin
     *
     * @apidoc.doc Set globally subscribable attribute for given channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "label of the channel")
     * @apidoc.param #param_desc("boolean", "value", "true if the channel is to be
     *          globally subscribable. False otherwise.")
     * @apidoc.returntype  #return_int_success()
     */
    public int setGloballySubscribable(User loggedInUser, String channelLabel,
                   boolean value) throws FaultException {
        Channel channel = lookupChannelByLabel(loggedInUser.getOrg(), channelLabel);

        try {
            if (!ChannelManager.verifyChannelAdmin(loggedInUser, channel.getId())) {
                throw new PermissionCheckFailureException();
            }
        }
        catch (InvalidChannelRoleException e) {
            throw new PermissionCheckFailureException();
        }

        channel.setGloballySubscribable(value, loggedInUser.getOrg());

        return 1;
    }

    /**
     * Adds a given list of packages to the given channel.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel
     * @param packageIds A list containing the ids of the packages to be added
     * @return Returns 1 if successfull, FaultException otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The user is not a channel admin for the channel
     *   - The channel is invalid
     *   - A package id is invalid
     *   - The user doesn't have access to one of the channels in the list
     *
     * @apidoc.doc Adds a given list of packages to the given channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "target channel")
     * @apidoc.param #array_single_desc("int", "packageIds", "ID of a package to
     *                                   add to the channel")
     * @apidoc.returntype  #return_int_success()
     */
    public int addPackages(User loggedInUser, String channelLabel, List<Long> packageIds)
        throws FaultException {
        Channel channel = lookupChannelByLabel(loggedInUser.getOrg(), channelLabel);

        // Try to add the list of packages to the channel. Catch any exceptions and
        // convert to FaultExceptions
        try {
            ChannelEditor.getInstance().addPackages(loggedInUser, channel, packageIds);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e.getMessage());
        }
        catch (LookupException le) {
            //This shouldn't happen, but if it does, it is because one of the packages
            //doesn't exist.
            throw new NoSuchPackageException(le);
        }

        //refresh channel with newest packages
        ChannelManager.refreshWithNewestPackages(channel, "api");

        /* Bugzilla # 177673 */
        scheduleErrataCacheUpdate(loggedInUser.getOrg(), channel, 3600000);

        //if we made it this far, the operation was a success!
        return 1;
    }

    /**
     * Removes a given list of errata from the given channel.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel
     * @param errataNames A list containing the advisory names of errata to remove
     * @param removePackages Boolean to remove packages from the channel also
     * @return Returns 1 if successfull, Exception otherwise
     *   - The user is not a channel admin for the channel
     *   - The channel is invalid
     *   - The user doesn't have access to one of the channels in the list
     *
     * @apidoc.doc Removes a given list of errata from the given channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "target channel")
     * @apidoc.param #array_single_desc("string", "errataNames", "name of an erratum to remove")
     * @apidoc.param #param_desc("boolean", "removePackages",
     *                          "true to remove packages from the channel")
     * @apidoc.returntype  #return_int_success()
     */
    public int removeErrata(User loggedInUser, String channelLabel,
            List<String> errataNames, boolean removePackages) {

        channelAdminPermCheck(loggedInUser);

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);

        if (!UserManager.verifyChannelAdmin(loggedInUser, channel)) {
            throw new PermissionCheckFailureException();
        }

        HashSet<Errata> errataToRemove = new HashSet<>();

        for (String erratumName : errataNames) {
            Errata erratum = ErrataManager.lookupByAdvisoryAndOrg(erratumName,
                    channel.getOrg());

            if (erratum != null) {
                errataToRemove.add(erratum);
                ErrataManager.removeErratumFromChannel(erratum, channel, loggedInUser);
            }
        }

        // remove packages from the channel if requested
        if (removePackages) {
            List<Long> packagesToRemove = new ArrayList<>();

            List<Long> channelPkgs = ChannelFactory.getPackageIds(channel.getId());

            for (Errata erratum : errataToRemove) {
                Set<Package> erratumPackageList = erratum.getPackages();

                for (Package pkg : erratumPackageList) {
                    // if the package is in the channel, remove it
                    if (channelPkgs.contains(pkg.getId())) {
                        packagesToRemove.add(pkg.getId());
                    }
                }
            }

            // remove the packages from the channel
            ChannelManager.removePackages(channel, packagesToRemove, loggedInUser);

            // refresh the channel
            ChannelManager.refreshWithNewestPackages(channel, "java::removeErrata");

            List<Long> cids = new ArrayList<>();
            cids.add(channel.getId());
            ErrataCacheManager.insertCacheForChannelPackagesAsync(cids, packagesToRemove);

        }

        return 1;
    }

    /**
     * Removes a given list of packages from the given channel.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel
     * @param packageIds A list containing the ids of the packages to be removed
     * @return Returns 1 if successfull, FaultException otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The user is not a channel admin for the channel
     *   - The channel is invalid
     *   - A package id is invalid
     *   - The user doesn't have access to one of the channels in the list
     *
     * @apidoc.doc Removes a given list of packages from the given channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "target channel")
     * @apidoc.param #array_single_desc("int", "packageIds", "ID of a package to
     *                                   remove from the channel")
     * @apidoc.returntype  #return_int_success()
     */
    public int removePackages(User loggedInUser, String channelLabel,
            List<Long> packageIds) throws FaultException {
        Channel channel = lookupChannelByLabel(loggedInUser.getOrg(), channelLabel);

        //Make sure the user is a channel admin for the given channel.
        if (!UserManager.verifyChannelAdmin(loggedInUser, channel)) {
            throw new PermissionCheckFailureException();
        }

        // Try to remove the list of packages from the channel. Catch any exceptions and
        // convert to FaultExceptions
        try {
            ChannelEditor.getInstance().removePackages(loggedInUser, channel, packageIds);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException();
        }
        catch (LookupException le) {
            //This shouldn't happen, but if it does, it is because one of the packages
            //doesn't exist.
            throw new NoSuchPackageException(le);
        }

        //refresh channel with newest packages
        ChannelManager.refreshWithNewestPackages(channel, "api");

        /* Bugzilla # 177673 */
        scheduleErrataCacheUpdate(loggedInUser.getOrg(), channel, 3600000);

        //if we made it this far, the operation was a success!
        return 1;
    }

    /**
     * Private helper method to create a new UpdateErrataCacheEvent and publish it to the
     * MessageQueue.
     * @param orgIn The org we're updating.
     */
    private void publishUpdateErrataCacheEvent(Org orgIn) {
        StopWatch sw = new StopWatch();
        if (log.isDebugEnabled()) {
            log.debug("Updating errata cache");
            sw.start();
        }

        UpdateErrataCacheEvent uece =
            new UpdateErrataCacheEvent(UpdateErrataCacheEvent.TYPE_ORG);
        uece.setOrgId(orgIn.getId());
        MessageQueue.publish(uece);

        if (log.isDebugEnabled()) {
            sw.stop();
            log.debug("Finished Updating errata cache. Took [{}]", sw.getTime());
        }
    }


    /**
     * List the errata applicable to a channel after given startDate
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel
     * @param startDate begin date
     * @return the errata applicable to a channel
     * @throws NoSuchChannelException thrown if there is no channel matching
     * channelLabel.
     *
     * @apidoc.doc List the errata applicable to a channel after given startDate
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.param #param($date, "startDate")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ErrataOverview> listErrata(User loggedInUser, String channelLabel,
            Date startDate) throws NoSuchChannelException {
        return listErrata(loggedInUser, channelLabel, startDate, null);
    }

    /**
     * List the errata applicable to a channel between startDate and endDate.
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel
     * @param startDate begin date
     * @param endDate end date
     * @return the errata applicable to a channel
     * @throws NoSuchChannelException thrown if there is no channel matching
     * channelLabel.
     *
     * @apidoc.doc List the errata applicable to a channel between startDate and endDate.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.param #param($date, "startDate")
     * @apidoc.param #param($date, "endDate")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataOverviewSerializer
     *      #array_end()
     */

    @ReadOnly
    public List<ErrataOverview> listErrata(User loggedInUser, String channelLabel,
            Date startDate, Date endDate) throws NoSuchChannelException {
        return listErrata(loggedInUser, channelLabel, startDate, endDate, false);
    }

    /**
     * List the errata applicable to a channel between startDate and endDate.
     * Allow to select errata by last modified date.
     * Support behaviour available in old versions. (needed for Dumper)
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel
     * @param startDate begin date
     * @param endDate end date
     * @param lastModified select by last modified timestamp or not
     * @return the errata applicable to a channel
     * @throws NoSuchChannelException thrown if there is no channel matching
     * channelLabel.
     *
     * @apidoc.doc List the errata applicable to a channel between startDate and endDate.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.param #param($date, "startDate")
     * @apidoc.param #param($date, "endDate")
     * @apidoc.param #param_desc("boolean", "lastModified",
     *     "select by last modified or not")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ErrataOverview> listErrata(User loggedInUser,
            String channelLabel, Date startDate, Date endDate,
            boolean lastModified) throws NoSuchChannelException {

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);

        DataResult<ErrataOverview> errata = ChannelManager.listErrata(channel, startDate,
                endDate, lastModified, loggedInUser);
        errata.elaborate();
        return errata;
    }

    /**
     * List the errata applicable to a channel
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel
     * @return the errata applicable to a channel
     * @throws NoSuchChannelException thrown if there is no channel matching
     * channelLabel.
     *
     * @apidoc.doc List the errata applicable to a channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.returntype
     *    #return_array_begin()
     *          $ErrataOverviewSerializer
     *    #array_end()
     */
    @ReadOnly
    public List<ErrataOverview> listErrata(User loggedInUser, String channelLabel)
        throws NoSuchChannelException {
        return listErrata(loggedInUser, channelLabel, (Date) null);
    }

    /**
     * List the errata of a specific type that are applicable to a channel
     * @param loggedInUser The current user
     * @param channelLabel The label for the channel
     * @param advisoryType The type of advisory (one of the following:
     * "Security Advisory", "Product Enhancement Advisory",
     * "Bug Fix Advisory")
     * @return the errata applicable to a channel
     * @throws NoSuchChannelException thrown if there is no channel matching
     * channelLabel.
     *
     * @apidoc.doc List the errata of a specific type that are applicable to a channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel to query")
     * @apidoc.param #param_desc("string", "advisoryType", "type of advisory (one of
     * of the following: 'Security Advisory', 'Product Enhancement Advisory',
     * 'Bug Fix Advisory'")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("errata")
     *              #prop_desc("string","advisory", "name of the advisory")
     *              #prop_desc("string","issue_date",
     *                         "date format follows YYYY-MM-DD HH24:MI:SS")
     *              #prop_desc("string","update_date",
     *                         "date format follows YYYY-MM-DD HH24:MI:SS")
     *              #prop("string","synopsis")
     *              #prop("string","advisory_type")
     *              #prop_desc("string","last_modified_date",
     *                         "date format follows YYYY-MM-DD HH24:MI:SS")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
    public Object[] listErrataByType(User loggedInUser, String channelLabel,
            String advisoryType) throws NoSuchChannelException {

        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);

        List<Map<String, Object>> errata =
                ChannelManager.listErrataByType(channel, advisoryType);
        return errata.toArray();
    }

    private void scheduleErrataCacheUpdate(Org org, Channel channel, long delay) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                                           "find_channel_in_task_queue");
        Map<String, Object> inParams = new HashMap<>();

        inParams.put("cid", channel.getId());
        DataResult dr = m.execute(inParams);

        delay /= (24 * 60 * 60);

        if (dr.isEmpty()) {
            WriteMode w = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                                                         "insert_into_task_queue");

            inParams = new HashMap<>();
            inParams.put("org_id", org.getId());
            inParams.put("task_name", ErrataCacheWorker.BY_CHANNEL);
            inParams.put("task_data", channel.getId());
            inParams.put("earliest", new Timestamp(System.currentTimeMillis() + delay));

            w.executeUpdate(inParams);
        }
        else {
            WriteMode w = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                                                         "update_task_queue");
            inParams = new HashMap<>();
            inParams.put("earliest", new Timestamp(System.currentTimeMillis() + delay));
            inParams.put("cid", channel.getId());

            w.executeUpdate(inParams);
        }
    }

    private Channel lookupChannelByLabel(User user, String label)
        throws NoSuchChannelException {

        Channel channel = ChannelFactory.lookupByLabelAndUser(label, user);
        if (channel == null) {
            throw new NoSuchChannelException(label);
        }

        return channel;
    }


    private Channel lookupChannelByLabel(Org org, String label)
        throws NoSuchChannelException {

        Channel channel = ChannelManager.lookupByLabel(
                org, label);
        if (channel == null) {
            throw new NoSuchChannelException(label);
        }

        return channel;
    }

    private Channel lookupChannelById(User user, Long id)
        throws NoSuchChannelException {

        Channel channel = ChannelManager.lookupByIdAndUser(id, user);
        if (channel == null) {
            throw new NoSuchChannelException(id);
        }

        return channel;
    }


    /**
     * Lists all packages for an Org that are not contained within any channel
     * @param loggedInUser The current user
     * @return list of Package objects not associated with a channel
     * @throws NoSuchChannelException thrown if no channel is found.
     *
     * @apidoc.doc Lists all packages that are not associated with a channel.  Typically
     *          these are custom packages.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *  #return_array_begin()
     *      $PackageSerializer
     *   #array_end()
     */
    @ReadOnly
    public Object[] listPackagesWithoutChannel(User loggedInUser) {
        ensureUserRole(loggedInUser, RoleFactory.CHANNEL_ADMIN);
        return PackageFactory.lookupOrphanPackages(loggedInUser.getOrg()).toArray();
    }

    /**
     * Clone a channel
     * @param loggedInUser The current user
     * @param originalLabel the label of the channel to clone
     * @param channelDetails a map consisting of
     *      string name
     *      string label
     *      string summary
     *      string parent_label (optional)
     *      string arch_label (optional)
     *      string gpg_key_url (optional), gpg_url left for historical reasons
     *      string gpg_key_id (optional), gpg_id left for historical reasons
     *      string gpg_key_fp (optional), gpg_fingerprint left for historical reasons
     *      string description (optional)
     * @param originalState if true, only the original packages of the channel to clone
     *          will be cloned.  Any updates will not be.
     * @return int id of clone channel
     *
     * @apidoc.doc Clone a channel.  If arch_label is omitted, the arch label of the
     *      original channel will be used. If parent_label is omitted, the clone will be
     *      a base channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "originalLabel")
     * @apidoc.param
     *      #struct_begin("channelDetails")
     *          #prop("string", "name")
     *          #prop("string", "label")
     *          #prop("string", "summary")
     *          #prop_desc("string", "parent_label", "(optional)")
     *          #prop_desc("string", "arch_label", "(optional)")
     *          #prop_desc("string", "gpg_key_url", "(optional),
     *              gpg_url might be used as well")
     *          #prop_desc("string", "gpg_key_id", "(optional),
     *              gpg_id might be used as well")
     *          #prop_desc("string", "gpg_key_fp", "(optional),
     *              gpg_fingerprint might be used as well")
     *          #prop_desc("string", "gpg_check", "(optional)")
     *          #prop_desc("string", "description", "(optional)")
     *          #prop_desc("string", "checksum", "either sha1 or sha256")
     *      #struct_end()
     * @apidoc.param #param("boolean", "originalState")
     * @apidoc.returntype #param_desc("int", "id", "the cloned channel ID")
     */
    public int clone(User loggedInUser, String originalLabel,
            Map<String, String> channelDetails, Boolean originalState) {

        channelAdminPermCheck(loggedInUser);
        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<>();
        validKeys.add("name");
        validKeys.add("label");
        validKeys.add("summary");
        validKeys.add("parent_label");
        validKeys.add("arch_label");
        validKeys.add("gpg_url");           // deprecated, left for compatibility reasons
        validKeys.add("gpg_id");            // deprecated, left for compatibility reasons
        validKeys.add("gpg_fingerprint");   // deprecated, left for compatibility reasons
        validKeys.add("gpg_key_url");
        validKeys.add("gpg_key_id");
        validKeys.add("gpg_key_fp");
        validKeys.add("gpg_check");
        validKeys.add("description");
        validKeys.add("checksum");
        validateMap(validKeys, channelDetails);

        Channel originalChan = lookupChannelByLabel(loggedInUser.getOrg(), originalLabel);

        CloneChannelCommand ccc = new CloneChannelCommand(
                Boolean.TRUE.equals(originalState) ? ORIGINAL_STATE : CURRENT_STATE, originalChan);

        ccc.setUser(loggedInUser);
        setChangedValues(ccc, channelDetails);

        Channel clone = ccc.create();
        return clone.getId().intValue();
    }

    /**
     * Checks whether a user is an org admin or channnel admin (and thus can admin
     *          a channel)
     * @param loggedInUser the user to check
     */
    private void channelAdminPermCheck(User loggedInUser) {
        Role channelRole = RoleFactory.lookupByLabel("channel_admin");
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");
        if (!loggedInUser.hasRole(channelRole) && !loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins and Channel Admins can clone or update " +
                    "channels.");
        }
    }

    /**
     * Merge a channel's errata into another channel.
     * @param loggedInUser The current user
     * @param mergeFromLabel the label of the channel to pull the errata from
     * @param mergeToLabel the label of the channel to push errata into
     * @return A list of errata that were merged.
     *
     * @apidoc.doc Merges all errata from one channel into another
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "mergeFromLabel", "the label of the
     * channel to pull errata from")
     * @apidoc.param #param_desc("string", "mergeToLabel", "the label to push the
     * errata into")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataSerializer
     *      #array_end()
     */
    public Object[] mergeErrata(User loggedInUser, String mergeFromLabel,
            String mergeToLabel) {
        channelAdminPermCheck(loggedInUser);

        Channel mergeFrom = lookupChannelByLabel(loggedInUser, mergeFromLabel);
        Channel mergeTo = lookupChannelByLabel(loggedInUser, mergeToLabel);

        if (!UserManager.verifyChannelAdmin(loggedInUser, mergeTo)) {
            throw new PermissionCheckFailureException();
        }

        Set<Errata> mergedErrata = ErrataManager.mergeErrataToChannel(loggedInUser, new HashSet(mergeFrom
                .getErratas()), mergeTo, mergeFrom);

        return mergedErrata.toArray();
    }

    /**
     * Merge a channel's errata into another channel based upon a given start/end date.
     * @param loggedInUser The current user
     * @param mergeFromLabel the label of the channel to pull the errata from
     * @param mergeToLabel the label of the channel to push errata into
     * @param startDate begin date
     * @param endDate end date
     * @return A list of errata that were merged.
     *
     * @apidoc.doc Merges all errata from one channel into another based upon a
     * given start/end date.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "mergeFromLabel", "the label of the
     * channel to pull errata from")
     * @apidoc.param #param_desc("string", "mergeToLabel", "the label to push the
     * errata into")
     * @apidoc.param #param("string", "startDate")
     * @apidoc.param #param("string", "endDate")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataSerializer
     *      #array_end()
     */
    public Object[] mergeErrata(User loggedInUser, String mergeFromLabel,
            String mergeToLabel, String startDate, String endDate) {
        channelAdminPermCheck(loggedInUser);

        Channel mergeFrom = lookupChannelByLabel(loggedInUser, mergeFromLabel);
        Channel mergeTo = lookupChannelByLabel(loggedInUser, mergeToLabel);

        if (!UserManager.verifyChannelAdmin(loggedInUser, mergeTo)) {
            throw new PermissionCheckFailureException();
        }

        List<Errata> fromErrata = ErrataFactory.lookupByChannelBetweenDates(
                loggedInUser.getOrg(), mergeFrom, startDate, endDate);

        Set<Errata> mergedErrata = ErrataManager.mergeErrataToChannel(loggedInUser,
                new HashSet<>(fromErrata), mergeTo, mergeFrom);

        return mergedErrata.toArray();
    }

    /**
     * Merge a list of errata from one channel into another channel
     * @param loggedInUser The current user
     * @param mergeFromLabel the label of the channel to pull the errata from
     * @param mergeToLabel the label of the channel to push errata into
     * @param errataNames the list of errata to merge
     * @return A list of errata that were merged.
     *
     * @apidoc.doc Merges a list of errata from one channel into another
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "mergeFromLabel", "the label of the
     * channel to pull errata from")
     * @apidoc.param #param_desc("string", "mergeToLabel", "the label to push the
     * errata into")
     * @apidoc.param
     *      #array_single_desc("string", "errataNames", "the advisory name of the errata to merge")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataSerializer
     *      #array_end()
     */
    public Object[] mergeErrata(User loggedInUser, String mergeFromLabel,
            String mergeToLabel, List<String> errataNames) {

        channelAdminPermCheck(loggedInUser);

        Channel mergeFrom = lookupChannelByLabel(loggedInUser, mergeFromLabel);
        Channel mergeTo = lookupChannelByLabel(loggedInUser, mergeToLabel);

        if (!UserManager.verifyChannelAdmin(loggedInUser, mergeTo)) {
            throw new PermissionCheckFailureException();
        }

        Set<Errata> sourceErrata = mergeFrom.getErratas();
        Set<Errata> errataToMerge = new HashSet<>();

        // make sure our errata exist in the "from" channel
        for (String erratumName : errataNames) {
            Errata toMerge = ErrataManager.lookupByAdvisoryAndOrg(erratumName,
                    mergeFrom.getOrg());

            for (Errata erratum : sourceErrata) {
                if (erratum.getAdvisoryName().equals(toMerge.getAdvisoryName())) {
                    errataToMerge.add(toMerge);
                    break;
                }
            }
        }

        Set<Errata> mergedErrata = ErrataManager.mergeErrataToChannel(loggedInUser, errataToMerge, mergeTo, mergeFrom);
        return mergedErrata.toArray();
    }

    /**
     * Merge a channel's packages into another channel.
     * @param loggedInUser The current user
     * @param mergeFromLabel the label of the channel to pull the packages from
     * @param mergeToLabel the label of the channel to push packages into
     * @return A list of packages that were merged.
     *
     * @apidoc.doc Merges all packages from one channel into another
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "mergeFromLabel", "the label of the
     *          channel to pull packages from")
     * @apidoc.param #param_desc("string", "mergeToLabel", "the label to push the
     *              packages into")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $PackageSerializer
     *      #array_end()
     */
    public Object[] mergePackages(User loggedInUser, String mergeFromLabel,
            String mergeToLabel) {
        return mergePackages(loggedInUser, mergeFromLabel, mergeToLabel, false);
    }

    /**
     * Merge a channel's packages into another channel.
     * @param loggedInUser The current user
     * @param mergeFromLabel the label of the channel to pull the packages from
     * @param mergeToLabel the label of the channel to push packages into
     * @param alignModules whether to align RHEL >= 8 modular data
     * @return A list of packages that were merged.
     *
     * @apidoc.doc Merges all packages from one channel into another
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "mergeFromLabel", "the label of the
     *          channel to pull packages from")
     * @apidoc.param #param_desc("string", "mergeToLabel", "the label to push the
     *              packages into")
     * @apidoc.param #param_desc("boolean", "alignModules", "align modular data of the target channel
     *              to the source channel (RHEL8 and higher)")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $PackageSerializer
     *      #array_end()
     */
    public Object[] mergePackages(User loggedInUser, String mergeFromLabel,
            String mergeToLabel, boolean alignModules) {

        Channel mergeFrom = lookupChannelByLabel(loggedInUser, mergeFromLabel);
        Channel mergeTo = lookupChannelByLabel(loggedInUser, mergeToLabel);

        if (!UserManager.verifyChannelAdmin(loggedInUser, mergeTo)) {
            throw new PermissionCheckFailureException();
        }

        List<Package> differentPackages = new ArrayList<>();

        Set<Package> toPacks = mergeTo.getPackages();
        Set<Package> fromPacks = mergeFrom.getPackages();
        List<Long> pids = new ArrayList<>();
        for (Package pack : fromPacks) {
            if (!toPacks.contains(pack)) {
                pids.add(pack.getId());
                differentPackages.add(pack);
            }
        }
        mergeTo.getPackages().addAll(differentPackages);
        ChannelFactory.save(mergeTo);
        ChannelManager.refreshWithNewestPackages(mergeTo, "java::mergePackages");

        if (alignModules) {
            mergeTo.cloneModulesFrom(mergeFrom);
        }

        List<Long> cids = new ArrayList<>();
        cids.add(mergeTo.getId());
        ErrataCacheManager.insertCacheForChannelPackagesAsync(cids, pids);
        return differentPackages.toArray();
    }

    /**
     * Align the metadata of a channel to another channel.
     *
     * @param loggedInUser the user
     * @param channelFromLabel the label of the source channel
     * @param channelToLabel the label of the target channel
     * @param metadataType the metadata type
     *
     * @return 1 when the channel metadata has been aligned
     * @throws PermissionCheckFailureException when user does not have access to the target channel
     *
     * @apidoc.doc Align the metadata of a channel to another channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelFromLabel", "the label of the source channel")
     * @apidoc.param #param_desc("string", "channelToLabel", "the label of the target channel")
     * @apidoc.param #param_desc("string", "metadataType", "the metadata type. Only 'modules' supported currently.")
     * @apidoc.returntype #param_desc("int", "result code", "1 when metadata has been aligned, 0 otherwise")
     */
    public int alignMetadata(User loggedInUser, String channelFromLabel, String channelToLabel, String metadataType) {
        Channel channelFrom = lookupChannelByLabel(loggedInUser, channelFromLabel);
        Channel channelTo = lookupChannelByLabel(loggedInUser, channelToLabel);

        if (!UserManager.verifyChannelAdmin(loggedInUser, channelTo)) {
            throw new PermissionCheckFailureException();
        }

        if (!metadataType.equals("modules")) {
            throw new InvalidParameterException("Only 'modules' metadata is currently supported.");
        }

        if (channelFrom.isModular()) {
            log.info("Aligning modular metadata of {} to {}", channelTo, channelFrom);
            channelTo.cloneModulesFrom(channelFrom);
            return 1;
        }

        return 0;
    }

    /**
     * Regenerate the errata cache for all the systems subscribed to a particular channel
     * @param loggedInUser The current user
     * @param channelLabel the channel label
     * @return int - 1 on success!
     *
     * @apidoc.doc Completely clear and regenerate the needed Errata and Package
     *      cache for all systems subscribed to the specified channel.  This should
     *      be used only if you believe your cache is incorrect for all the systems
     *      in a given channel. This will schedule an asynchronous action to actually
     *      do the processing.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "the label of the
     *          channel")
     * @apidoc.returntype  #return_int_success()
     *
     */
    public int regenerateNeededCache(User loggedInUser, String channelLabel) {
        channelAdminPermCheck(loggedInUser);
        Channel chan = lookupChannelByLabel(loggedInUser, channelLabel);
        List<Long> chanList = new ArrayList<>();
        chanList.add(chan.getId());
        ErrataCacheManager.updateCacheForChannelsAsync(chanList);
        return 1;
    }

    /**
     * Regenerate the errata cache for all systems subscribed.
     * @param loggedInUser The current user
     * @return int - 1 on success!
     *
     * @apidoc.doc Completely clear and regenerate the needed Errata and Package
     *      cache for all systems subscribed. You must be a #product() Admin to
     *      perform this action. This will schedule an asynchronous action to
     *      actually do the processing.
     * @apidoc.param #session_key()
     * @apidoc.returntype  #return_int_success()
     */
    public int regenerateNeededCache(User loggedInUser) {
        if (loggedInUser.hasRole(RoleFactory.SAT_ADMIN)) {
            Set<Channel> set = new HashSet<>();
            set.addAll(ChannelFactory.listAllBaseChannels());
            ErrataCacheManager.updateCacheForChannelsAsync(set);
        }
        else {
            throw new PermissionException(RoleFactory.SAT_ADMIN);
        }
        return 1;
    }

    /**
     * Regenerate the yum cache for a specific channel.
     * @param loggedInUser The current user
     * @param channelLabel the channel label
     * @param force force regeneration
     * @return int - 1 on success!
     *
     * @apidoc.doc Regenerate yum cache for the specified channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "the label of the
     *          channel")
     * @apidoc.param #param_desc("boolean", "force", "force cache regeneration")
     * @apidoc.returntype  #return_int_success()
     *
     */
    public int regenerateYumCache(User loggedInUser, String channelLabel, Boolean force) {
        channelAdminPermCheck(loggedInUser);
        lookupChannelByLabel(loggedInUser, channelLabel);

        ChannelManager.queueChannelChange(channelLabel,
                "api: regenerateYumCache", "api called", force);
        return 1;
    }

    /**
     * List the children of a channel
     * @param loggedInUser The current user
     * @param channelLabel the channel label
     * @return list of channel id's and labels
     *
     * @apidoc.doc List the children of a channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "the label of the channel")
     * @apidoc.returntype
     *      #return_array_begin()
     *              $ChannelSerializer
     *      #array_end()
     */
    @ReadOnly
    public Object[] listChildren(User loggedInUser, String channelLabel) {
        Channel chan = lookupChannelByLabel(loggedInUser, channelLabel);

        return ChannelFactory.getAccessibleChildChannels(chan, loggedInUser).toArray();
    }

    /**
    * Returns the last build date on the repodata for a channel
    * @param loggedInUser The current user
    * @param id - id of channel wanted
    * @throws NoSuchChannelException thrown if no channel is found.
    * @return the build date on the repodata of the channel requested
    *
    * @apidoc.doc Returns the last build date of the repomd.xml file
    * for the given channel as a localised string.
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("int", "id", "id of channel wanted")
    * @apidoc.returntype
    *   #param_desc("date", "date", "the last build date of the repomd.xml file as a localised string")
    */
    @ReadOnly
    public String getChannelLastBuildById(User loggedInUser, Integer id)
                                            throws NoSuchChannelException {
        String repoLastBuild =
                ChannelManager.getRepoLastBuild(lookupChannelById(loggedInUser,
                        id.longValue()));
        if (repoLastBuild == null) {
            return "";
        }
        return repoLastBuild;
    }

    /** Returns a list of ContentSource (repos) that the user can see
     * @param loggedInUser The current user
     * @return Lists the repos visible to the user
     * @apidoc.doc Returns a list of ContentSource (repos) that the user can see
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("map")
     *              #prop_desc("long","id", "ID of the repo")
     *              #prop_desc("string","label", "label of the repo")
     *              #prop_desc("string","sourceUrl", "URL of the repo")
     *          #struct_end()
     *      #array_end()
     **/
    @ReadOnly
    public List<Map<String, Object>> listUserRepos(User loggedInUser) {
        List<ContentSource> result = ChannelFactory
                .lookupContentSources(loggedInUser.getOrg());

        List<Map<String, Object>> list = new ArrayList<>();
        for (ContentSource cs : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cs.getId());
            map.put("label", cs.getLabel());
            map.put("sourceUrl", cs.getSourceUrl());
            list.add(map);
        }
        return list;
    }

   /**
    * Creates a repository
    * @param loggedInUser The current user
    * @param label of the repo to be created
    * @param type of the repo
    * @param url of the repo
    * @return new ContentSource
    *
    * @apidoc.doc Creates a repository
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("string", "label", "repository label")
    * @apidoc.param #param_desc("string", "type", "repository type (yum, uln...)")
    * @apidoc.param #param_desc("string", "url", "repository url")
    * @apidoc.returntype $ContentSourceSerializer
   **/
    public ContentSource createRepo(User loggedInUser, String label, String type,
            String url) {
        // empty strings for SSL-certificates descriptions
        String sslCaCert = "";
        String sslCliCert = "";
        String sslCliKey = "";

        return createRepo(loggedInUser, label, type, url, sslCaCert, sslCliCert, sslCliKey);
    }

    /**
     * Creates a repository
     * @param loggedInUser The current user
     * @param label of the repo to be created
     * @param type of the repo
     * @param url of the repo
     * @param sslCaCert CA certificate description
     * @param sslCliCert Client certificate description
     * @param sslCliKey Client key description
     * @return new ContentSource
     *
     * @apidoc.doc Creates a repository
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "repository label")
     * @apidoc.param #param_desc("string", "type",
     * "repository type (yum, uln...)")
     * @apidoc.param #param_desc("string", "url", "repository url")
     * @apidoc.param #param_desc("string", "sslCaCert", "SSL CA cert description")
     * @apidoc.param #param_desc("string", "sslCliCert", "SSL Client cert description")
     * @apidoc.param #param_desc("string", "sslCliKey", "SSL Client key description")
     * @apidoc.returntype $ContentSourceSerializer
     **/
    public ContentSource createRepo(User loggedInUser, String label, String type,
            String url, String sslCaCert, String sslCliCert, String sslCliKey) {

        return createRepo(loggedInUser, label, type, url, sslCaCert, sslCliCert, sslCliKey,
                false);
    }

    /**
     * Creates a repository
     * @param loggedInUser The current user
     * @param label of the repo to be created
     * @param type of the repo (YUM only for now)
     * @param url of the repo
     * @param sslCaCert CA certificate description
     * @param sslCliCert Client certificate description
     * @param sslCliKey Client key description
     * @param hasSignedMetadata Whether the repository has signed metadata
     * @return new ContentSource
     *
     * @apidoc.doc Creates a repository
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "repository label")
     * @apidoc.param #param_desc("string", "type",
     * "repository type (only YUM is supported)")
     * @apidoc.param #param_desc("string", "url", "repository url")
     * @apidoc.param #param_desc("string", "sslCaCert", "SSL CA cert description, or an
     *     empty string")
     * @apidoc.param #param_desc("string", "sslCliCert", "SSL Client cert description, or
     *     an empty string")
     * @apidoc.param #param_desc("string", "sslCliKey", "SSL Client key description, or an
     *     empty string")
     * @apidoc.param #param_desc("boolean", "hasSignedMetadata", "true if the repository
     *     has signed metadata, false otherwise")
     * @apidoc.returntype $ContentSourceSerializer
    **/
     public ContentSource createRepo(User loggedInUser, String label, String type,
             String url, String sslCaCert, String sslCliCert, String sslCliKey,
             boolean hasSignedMetadata) {

         if (StringUtils.isEmpty(label)) {
             throw new InvalidParameterException("label might not be empty");
         }

         if (StringUtils.isEmpty(url)) {
             throw new InvalidParameterException("url might not be empty");
         }

         type = type.toLowerCase();
         BaseRepoCommand repoCmd = new CreateRepoCommand(loggedInUser.getOrg());

         repoCmd.setLabel(label);
         repoCmd.setUrl(url);
         repoCmd.setMetadataSigned(hasSignedMetadata);

         repoCmd.setType(type);

         // check SSL-certificates parameters
         if (!StringUtils.isEmpty(sslCaCert)) {
             try {
                 // FIXME: Allow to set multiple SSL sets per custom repo - new API calls?
                 repoCmd.addSslSet(getKeyId(loggedInUser, sslCaCert),
                         getKeyId(loggedInUser, sslCliCert),
                         getKeyId(loggedInUser, sslCliKey));
             }
             catch (InvalidCertificateException e) {
                 throw new NoSuchCryptoKeyException(e.getMessage());
             }
         }
         else if (!StringUtils.isEmpty(sslCliCert) || !StringUtils.isEmpty(sslCliKey)) {
             log.warn("SSL CA Certificate is missing, ignoring other SSL Certs/Keys");
         }

         repoCmd.store();

         return ChannelFactory.lookupContentSourceByOrgAndLabel(loggedInUser.getOrg(), label);
     }

   /**
    * Removes a repository
    * @param loggedInUser The current user
    * @param id of the repo to be removed
    * @return Integer 1 on success
    *
    * @apidoc.doc Removes a repository
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("long", "id", "ID of repo to be removed")
    * @apidoc.returntype #return_int_success()
   **/
    public Integer removeRepo(User loggedInUser, Integer id) {
        ContentSource repo = lookupContentSourceById(id.longValue(), loggedInUser.getOrg());

        ChannelFactory.remove(repo);
        return 1;
    }

   /**
    * Removes a repository
    * @param loggedInUser The current user
    * @param label of the repo to be removed
    * @return Integer 1 on success
    *
    * @apidoc.doc Removes a repository
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("string", "label", "label of repo to be removed")
    * @apidoc.returntype #return_int_success()
   **/
    public Integer removeRepo(User loggedInUser, String label) {
        ContentSource repo = lookupContentSourceByLabel(label, loggedInUser.getOrg());
        ChannelFactory.clearContentSourceFilters(repo.getId());

        ChannelFactory.remove(repo);
        return 1;
    }

   /**
    * Associates a repository with a channel
    * @param loggedInUser The current user
    * @param channelLabel of the channel to use
    * @param repoLabel of the repo to associate
    * @return the channel with the newly associated repo
    *
    * @apidoc.doc Associates a repository with a channel
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("string", "channelLabel", "channel label")
    * @apidoc.param #param_desc("string", "repoLabel", "repository label")
    * @apidoc.returntype $ChannelSerializer
   **/
    public Channel associateRepo(User loggedInUser, String channelLabel, String repoLabel) {
        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        ContentSource repo = lookupContentSourceByLabel(repoLabel, loggedInUser.getOrg());

        Set<ContentSource> set = channel.getSources();
        set.add(repo);
        ChannelFactory.save(channel);

        return channel;
    }

   /**
    * Disassociates a repository from a channel
    * @param loggedInUser The current user
    * @param channelLabel of the channel to use
    * @param repoLabel of the repo to disassociate
    * @return the channel minus the disassociated repo
    *
    * @apidoc.doc Disassociates a repository from a channel
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("string", "channelLabel", "channel label")
    * @apidoc.param #param_desc("string", "repoLabel", "repository label")
    * @apidoc.returntype $ChannelSerializer
   **/
    public Channel disassociateRepo(User loggedInUser, String channelLabel, String repoLabel) {
        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        ContentSource repo = lookupContentSourceByLabel(repoLabel, loggedInUser.getOrg());

        Set<ContentSource> set = channel.getSources();
        set.remove(repo);
        channel.setSources(set);

        ChannelFactory.save(channel);

        return channel;
    }

   /**
    * Updates repository source URL
    * @param loggedInUser The current user
    * @param id ID of the repo
    * @param url new URL to use
    * @return the updated repo
    *
    * @apidoc.doc Updates repository source URL
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("int", "id", "repository ID")
    * @apidoc.param #param_desc("string", "url", "new repository URL")
    * @apidoc.returntype $ContentSourceSerializer
   **/
    public ContentSource updateRepoUrl(User loggedInUser, Integer id, String url) {
        ContentSource repo = lookupContentSourceById(id.longValue(), loggedInUser.getOrg());
        setRepoUrl(repo, url);
        ChannelFactory.save(repo);
        return repo;
    }

   /**
    * Updates repository source URL
    * @param loggedInUser The current user
    * @param label of the repo to use
    * @param url new URL to use
    * @return the updated repo
    *
    * @apidoc.doc Updates repository source URL
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("string", "label", "repository label")
    * @apidoc.param #param_desc("string", "url", "new repository URL")
    * @apidoc.returntype $ContentSourceSerializer
   **/
    public ContentSource updateRepoUrl(User loggedInUser, String label, String url) {
        ContentSource repo = lookupContentSourceByLabel(label, loggedInUser.getOrg());
        setRepoUrl(repo, url);
        ChannelFactory.save(repo);
        return repo;
    }

   /**
    * Updates repository SSL certificates
    * @param loggedInUser The current user
    * @param id ID of the repository
    * @param sslCaCert new CA certificate description
    * @param sslCliCert new Client certificate description
    * @param sslCliKey new Client key description
    * @return the updated repository
    *
    * @apidoc.doc Updates repository SSL certificates
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("int", "id", "repository ID")
    * @apidoc.param #param_desc("string", "sslCaCert", "SSL CA cert description")
    * @apidoc.param #param_desc("string", "sslCliCert", "SSL Client cert description")
    * @apidoc.param #param_desc("string", "sslCliKey", "SSL Client key description")
    * @apidoc.returntype $ContentSourceSerializer
   **/
    public ContentSource updateRepoSsl(User loggedInUser, Integer id,
            String sslCaCert, String sslCliCert, String sslCliKey) {

        ContentSource repo = ChannelFactory.lookupContentSource(id.longValue(),
                loggedInUser.getOrg());
        return updateRepoSsl(loggedInUser, repo.getLabel(), sslCaCert, sslCliCert,
                sslCliKey);
    }

   /**
    * Updates repository SSL certificates
    * @param loggedInUser The current user
    * @param label repository label
    * @param sslCaCert new CA certificate description
    * @param sslCliCert new Client certificate description
    * @param sslCliKey new Client key description
    * @return the updated repository
    *
    * @apidoc.doc Updates repository SSL certificates
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("string", "label", "repository label")
    * @apidoc.param #param_desc("string", "sslCaCert", "SSL CA cert description")
    * @apidoc.param #param_desc("string", "sslCliCert", "SSL Client cert description")
    * @apidoc.param #param_desc("string", "sslCliKey", "SSL Client key description")
    * @apidoc.returntype $ContentSourceSerializer
   **/
    public ContentSource updateRepoSsl(User loggedInUser, String label,
            String sslCaCert, String sslCliCert, String sslCliKey) {

        if (StringUtils.isEmpty(label)) {
            throw new InvalidParameterException("label might not be empty");
        }

        ContentSource repo = ChannelFactory.lookupContentSourceByOrgAndLabel(
                loggedInUser.getOrg(), label);

        if (repo == null) {
            throw new InvalidParameterException("no repo with label " + label);
        }

        EditRepoCommand repoEditor = new EditRepoCommand(loggedInUser, repo.getId());

        // set new SSL Certificates for the repository
        if (!StringUtils.isEmpty(sslCaCert)) {
            try {
                // FIXME: Allow to set multiple SSL sets per custom repo - new API calls?
                repoEditor.deleteAllSslSets();
                repoEditor.addSslSet(getKeyId(loggedInUser, sslCaCert),
                        getKeyId(loggedInUser, sslCliCert),
                        getKeyId(loggedInUser, sslCliKey));
            }
            catch (InvalidCertificateException e) {
                throw new NoSuchCryptoKeyException(e.getMessage());
            }
        }
        else if (!StringUtils.isEmpty(sslCliCert) || !StringUtils.isEmpty(sslCliKey)) {
            log.warn("SSL CA Certificate is missing, ignoring other SSL Certs/Keys");
        }

        // Store Repo
        repoEditor.store();
        repo = ChannelFactory.lookupContentSourceByOrgAndLabel(loggedInUser.getOrg(),
                label);
        return repo;
    }

   /**
    * Updates repository label
    * @param loggedInUser The current user
    * @param id ID of the repo
    * @param label new label
    * @return the updated repo
    *
    * @apidoc.doc Updates repository label
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("int", "id", "repository ID")
    * @apidoc.param #param_desc("string", "label", "new repository label")
    * @apidoc.returntype $ContentSourceSerializer
   **/
    public ContentSource updateRepoLabel(User loggedInUser, Integer id, String label) {
        ContentSource repo = lookupContentSourceById(id.longValue(), loggedInUser.getOrg());
        setRepoLabel(repo, label);
        ChannelFactory.save(repo);
        return repo;
    }

    /**
     * Updates repository label
     * @param loggedInUser The current user
     * @param label of the repo
     * @param newLabel new label
     * @return the updated repo
     *
     * @apidoc.doc Updates repository label
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "repository label")
     * @apidoc.param #param_desc("string", "newLabel", "new repository label")
     * @apidoc.returntype $ContentSourceSerializer
    **/
     public ContentSource updateRepoLabel(User loggedInUser, String label,
                     String newLabel) {
         ContentSource repo = lookupContentSourceByLabel(label, loggedInUser.getOrg());
         setRepoLabel(repo, newLabel);
         ChannelFactory.save(repo);
         return repo;
     }

   /**
    * Updates a repository
    * @param loggedInUser The current user
    * @param id ID of the repo
    * @param label new label
    * @param url new URL
    * @return the updated repo
    *
    * @apidoc.doc Updates a ContentSource (repo)
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("int", "id", "repository ID")
    * @apidoc.param #param_desc("string", "label", "new repository label")
    * @apidoc.param #param_desc("string", "url", "new repository URL")
    * @apidoc.returntype $ContentSourceSerializer
   **/
    public ContentSource updateRepo(User loggedInUser, Integer id, String label,
            String url) {
        ContentSource repo = lookupContentSourceById(id.longValue(), loggedInUser.getOrg());
        setRepoLabel(repo, label);
        setRepoUrl(repo, url);
        ChannelFactory.save(repo);
        return repo;
    }

    /**
     * Returns the details of the given repo
     * @param loggedInUser The current user
     * @param repoLabel Label of repo whose details are sought.
     * @return the repo requested.
     *
     * @apidoc.doc Returns details of the given repository
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "repoLabel", "repo to query")
     * @apidoc.returntype
     *     $ContentSourceSerializer
     */
    @ReadOnly
    public ContentSource getRepoDetails(User loggedInUser, String repoLabel) {
        return lookupContentSourceByLabel(repoLabel, loggedInUser.getOrg());
    }

    /**
     * Returns the details of the given repo
     * @param loggedInUser The current user
     * @param id ID of repo whose details are sought.
     * @return the repo requested.
     *
     * @apidoc.doc Returns details of the given repository
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "id", "repository ID")
     * @apidoc.returntype
     *     $ContentSourceSerializer
     */
    @ReadOnly
    public ContentSource getRepoDetails(User loggedInUser, Integer id) {
        return lookupContentSourceById(id.longValue(), loggedInUser.getOrg());
    }

    /**
     * Lists associated repos with the given channel
     * @param loggedInUser The current user
     * @param channelLabel channel label
     * @return list of associates repos
     *
     * @apidoc.doc Lists associated repos with the given channel
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel label")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ContentSourceSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ContentSource> listChannelRepos(User loggedInUser, String channelLabel) {
        Channel channel = lookupChannelByLabel(loggedInUser, channelLabel);
        return ChannelFactory.lookupContentSources(loggedInUser.getOrg(), channel);
    }

    /**
     * Trigger immediate repo synchronization
     * @param loggedInUser The current user
     * @param channelLabels channel labels
     * @return 1 on success
     *
     * @apidoc.doc Trigger immediate repo synchronization
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("string", "channelLabels")
     * @apidoc.returntype  #return_int_success()
     */
    public int syncRepo(User loggedInUser, List<String> channelLabels) {
        try {
            List<Channel> channels = new ArrayList<>(channelLabels.size());
            for (String channelLabel : channelLabels) {
                channels.add(lookupChannelByLabel(loggedInUser, channelLabel));
            }
            TaskomaticApi tapi = new TaskomaticApi();
            tapi.scheduleSingleRepoSync(channels);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return BaseHandler.VALID;
    }

    /**
     * Trigger immediate repo synchronization
     * @param loggedInUser The current user
     * @param channelLabel channel label
     * @return 1 on success
     *
     * @apidoc.doc Trigger immediate repo synchronization
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel label")
     * @apidoc.returntype  #return_int_success()
     */
    public int syncRepo(User loggedInUser, String channelLabel) {
        Channel chan = lookupChannelByLabel(loggedInUser, channelLabel);
        try {
            new TaskomaticApi().scheduleSingleRepoSync(chan, loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return 1;
    }

    /**
     * Trigger immediate repo synchronization
     * @param loggedInUser The current user
     * @param channelLabel channel label
     * @param params parameters
     * @return 1 on success
     *
     * @apidoc.doc Trigger immediate repo synchronization
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel label")
     * @apidoc.param
     *  #struct_begin("params")
     *    #prop_desc("boolean", "sync-kickstart", "create kickstartable tree - Optional")
     *    #prop_desc("boolean", "no-errata", "do not sync errata - Optional")
     *    #prop_desc("boolean", "fail", "terminate upon any error - Optional")
     *    #prop_desc("boolean", "latest", "only download latest packages - Optional")
     *  #struct_end()
     * @apidoc.returntype  #return_int_success()
     */
    public int syncRepo(User loggedInUser, String channelLabel,
                                               Map<String, String> params) {
        Channel chan = lookupChannelByLabel(loggedInUser, channelLabel);
        try {
            new TaskomaticApi().scheduleSingleRepoSync(chan, loggedInUser, params);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return 1;
    }

    /**
     * Schedule periodic repo synchronization
     * @param loggedInUser The current user
     * @param channelLabel channel label
     * @param cronExpr cron expression, if empty all periodic schedules will be disabled
     * @return 1 on success
     *
     * @apidoc.doc Schedule periodic repo synchronization
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel label")
     * @apidoc.param #param_desc("string", "cronExpr",
     *      "cron expression, if empty all periodic schedules will be disabled")
     * @apidoc.returntype  #return_int_success()
     */
    public int syncRepo(User loggedInUser, String channelLabel, String cronExpr) {
        try {
            Channel chan = lookupChannelByLabel(loggedInUser, channelLabel);
            if (StringUtils.isEmpty(cronExpr)) {
                new TaskomaticApi().unscheduleRepoSync(chan, loggedInUser);
            }
            else {
                new TaskomaticApi().scheduleRepoSync(chan, loggedInUser, cronExpr);
            }
            return 1;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Schedule periodic repo synchronization
     * @param loggedInUser The current user
     * @param channelLabel channel label
     * @param cronExpr cron expression, if empty all periodic schedules will be disabled
     * @param params parameters
     * @return 1 on success
     *
     * @apidoc.doc Schedule periodic repo synchronization
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel label")
     * @apidoc.param #param_desc("string", "cronExpr",
     *      "cron expression, if empty all periodic schedules will be disabled")
     * @apidoc.param
     *  #struct_begin("params")
     *    #prop_desc("boolean", "sync-kickstart", "create kickstartable tree - Optional")
     *    #prop_desc("boolean", "no-errata", "do not sync errata - Optional")
     *    #prop_desc("boolean", "fail", "terminate upon any error - Optional")
     *    #prop_desc("boolean", "latest", "only download latest packages - Optional")
     *  #struct_end()
     * @apidoc.returntype  #return_int_success()
     */
    public int syncRepo(User loggedInUser,
            String channelLabel, String cronExpr, Map<String, String> params) {
        try {
            Channel chan = lookupChannelByLabel(loggedInUser, channelLabel);
            TaskomaticApi tapi = new TaskomaticApi();
            if (!tapi.isRunning()) {
                tapi.scheduleSingleRepoSync(chan, loggedInUser);
                throw new TaskomaticApiException("Taskomatic is not running");
            }

            if (StringUtils.isEmpty(cronExpr)) {
                tapi.unscheduleRepoSync(chan, loggedInUser);
            }
            else {
                tapi.scheduleRepoSync(chan, loggedInUser, cronExpr, params);
            }

            return 1;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Returns repo synchronization quartz expression
     * @param loggedInUser The current user
     * @param channelLabel channel label
     * @return quartz expression
     *
     * @apidoc.doc Returns repo synchronization cron expression
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "channelLabel", "channel label")
     * @apidoc.returntype #param_desc("string", "expression", "quartz expression")
     */
    @ReadOnly
    public String getRepoSyncCronExpression(User loggedInUser, String channelLabel) {
        try {
            Channel chan = lookupChannelByLabel(loggedInUser, channelLabel);
            String cronExpr = new TaskomaticApi().getRepoSyncSchedule(chan, loggedInUser);
            if (StringUtils.isEmpty(cronExpr)) {
                return "";
            }
            return cronExpr;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

   /**
    * Lists the filters for a repo
    * @param loggedInUser The current user
    * @param label of the repo to use
    * @return list of filters
    *
    * @apidoc.doc Lists the filters for a repo
    * @apidoc.param #session_key()
    * @apidoc.param #param_desc("string", "label", "repository label")
    * @apidoc.returntype
    *      #return_array_begin()
    *          $ContentSourceFilterSerializer
    *      #array_end()
    *
   **/
   @ReadOnly
   public List<ContentSourceFilter> listRepoFilters(User loggedInUser, String label) {

        ContentSource cs = lookupContentSourceByLabel(label, loggedInUser.getOrg());

        return ChannelFactory.lookupContentSourceFiltersById(cs.getId());
    }

    /**
     * adds a filter for a given repo.
     * @param loggedInUser The current user
     * @param label of the repo to use
     * @param filterProps list of filters
     * @return sort order for the new filter
     *
     * @apidoc.doc Adds a filter for a given repo.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "repository label")
     * @apidoc.param
     *  #struct_begin("filterProps")
     *          #prop_desc("string", "filter", "string to filter on")
     *          #prop_desc("string", "flag", "+ for include, - for exclude")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "order", "sort order for new filter")
     */
    public int addRepoFilter(User loggedInUser, String label,
            Map<String, String> filterProps) {
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

        if (!loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins can add repo filters.");
        }

        ContentSource cs = lookupContentSourceByLabel(label, loggedInUser.getOrg());

        String flag = filterProps.get("flag");
        String filter = filterProps.get("filter");

        if (!(flag.equals("+") || flag.equals("-"))) {
            throw new InvalidParameterException("flag must be + or -");
        }

        // determine the highest sort order of existing filters
        int sortOrder = 0;
        for (ContentSourceFilter f : listRepoFilters(loggedInUser, label)) {
            sortOrder = Math.max(sortOrder, f.getSortOrder());
        }

        ContentSourceFilter newFilter = new ContentSourceFilter();
        newFilter.setSourceId(cs.getId());
        newFilter.setFlag(flag);
        newFilter.setFilter(filter);
        newFilter.setSortOrder(sortOrder + 1);

        ChannelFactory.save(newFilter);

        return sortOrder;
    }

    /**
     * Removes a filter for a given repo.
     * @param loggedInUser The current user
     * @param label of the repo to use
     * @param filterProps list of filters
     * @return 1 on success
     *
     * @apidoc.doc Removes a filter for a given repo.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "repository label")
     * @apidoc.param
     *  #struct_begin("filterProps")
     *          #prop_desc("string", "filter", "string to filter on")
     *          #prop_desc("string", "flag", "+ for include, - for exclude")
     *  #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    public int removeRepoFilter(User loggedInUser, String label,
            Map<String, String> filterProps) {
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

        if (!loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins can remove repo filters.");
        }

        //TODO is this necessary?
        lookupContentSourceByLabel(label, loggedInUser.getOrg());

        String flag = filterProps.get("flag");
        String filter = filterProps.get("filter");

        if (!(flag.equals("+") || flag.equals("-"))) {
            throw new InvalidParameterException("flag must be + or -");
        }

        // find the existing filter
        ContentSourceFilter oldFilter = null;
        for (ContentSourceFilter f : listRepoFilters(loggedInUser, label)) {
            if (flag.equals(f.getFlag()) && filter.equals(f.getFilter())) {
                oldFilter = f;
                break;
            }
        }

        if (oldFilter == null) {
            throw new InvalidParameterException("filter does not exist");
        }

        ChannelFactory.remove(oldFilter);

        return 1;
    }

    /**
     * replaces the existing set of filters for a given repo.
     * filters are ranked by their order in the array.
     * @param loggedInUser The current user
     * @param label of the repo to use
     * @param filterProps list of filters
     * @return 1 on success
     *
     * @apidoc.doc Replaces the existing set of filters for a given repo.
     * Filters are ranked by their order in the array.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "repository label")
     * @apidoc.param
     *  #array_begin("filterProps")
     *      #struct_begin("filter properties")
     *          #prop_desc("string", "filter", "string to filter on")
     *          #prop_desc("string", "flag", "+ for include, - for exclude")
     *      #struct_end()
     *  #array_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setRepoFilters(User loggedInUser, String label,
            List<Map<String, String>> filterProps) {
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

        if (!loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins can set repo filters.");
        }

        ContentSource cs = lookupContentSourceByLabel(label, loggedInUser.getOrg());

        List<ContentSourceFilter> filters = new ArrayList<>();

        int i = 1;
        for (Map<String, String> filterIn : filterProps) {
            String flag = filterIn.get("flag");
            String filter = filterIn.get("filter");

            if (!(flag.equals("+") || flag.equals("-"))) {
                throw new InvalidParameterException("flag must be + or -");
            }

            ContentSourceFilter f = new ContentSourceFilter();
            f.setSourceId(cs.getId());
            f.setFlag(flag);
            f.setFilter(filter);
            f.setSortOrder(i);

            filters.add(f);

            i++;
        }

        ChannelFactory.clearContentSourceFilters(cs.getId());

        for (ContentSourceFilter filter : filters) {
            ChannelFactory.save(filter);
        }

        return 1;
    }

    /**
     * Clears the filters for a repo
     * @param loggedInUser The current user
     * @param label of the repo to use
     * @return 1 on success
     *
     * @apidoc.doc Removes the filters for a repo
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "repository label")
     * @apidoc.returntype #return_int_success()
    **/
     public int clearRepoFilters(User loggedInUser, String label) {
         Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

         if (!loggedInUser.hasRole(orgAdminRole)) {
             throw new PermissionException("Only Org Admins can remove repo filters.");
         }

         ContentSource cs = lookupContentSourceByLabel(label, loggedInUser.getOrg());

         ChannelFactory.clearContentSourceFilters(cs.getId());

         return 1;
     }

     /**
     * Lists the filters for a vendor repo
     * @param loggedInUser The current user
     * @param label of the repo to use
     * @return list of filters
     *
     * @apidoc.ignore
    **/
    public List<ContentSourceFilter> listVendorRepoFilters(User loggedInUser, String label) {
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

        if (!loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins can list vendor repo filters.");
        }
        log.warn("Unsupported XMLRPC call used: listVendorRepoFilters {}", label);

        ContentSource cs = lookupVendorContentSourceByLabel(label);

        return ChannelFactory.lookupContentSourceFiltersById(cs.getId());
     }

    /**
     * adds a filter for a given Vendor repo.
     * This is unsupported
     *
     * @param loggedInUser The current user
     * @param label of the repo to use
     * @param filterIn list of filters
     * @return sort order for the new filter
     * @apidoc.ignore
     */
    public int addVendorRepoFilter(User loggedInUser, String label, Map<String, String> filterIn) {
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

        if (!loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins can add repo filters.");
        }
        log.warn("Unsupported XMLRPC call used: addVendorRepoFilters {}", label);

        ContentSource cs = lookupVendorContentSourceByLabel(label);

        String flag = filterIn.get("flag");
        String filter = filterIn.get("filter");

        if (!(flag.equals("+") || flag.equals("-"))) {
            throw new InvalidParameterException("flag must be + or -");
        }

        // determine the highest sort order of existing filters
        int sortOrder = 0;
        for (ContentSourceFilter f : listVendorRepoFilters(loggedInUser, label)) {
            sortOrder = Math.max(sortOrder, f.getSortOrder());
        }

        ContentSourceFilter newFilter = new ContentSourceFilter();
        newFilter.setSourceId(cs.getId());
        newFilter.setFlag(flag);
        newFilter.setFilter(filter);
        newFilter.setSortOrder(sortOrder + 1);

        ChannelFactory.save(newFilter);

        return sortOrder;
    }

    /**
     * Removes a filter for a given repo.
     * @param loggedInUser The current user
     * @param label of the repo to use
     * @param filterIn list of filters
     * @return 1 on success
     * @apidoc.ignore
     */
    public int removeVendorRepoFilter(User loggedInUser, String label,
            Map<String, String> filterIn) {
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

        if (!loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins can remove repo filters.");
        }
        log.warn("Unsupported XMLRPC call used: removeVendorRepoFilters {}", label);

        String flag = filterIn.get("flag");
        String filter = filterIn.get("filter");

        if (!(flag.equals("+") || flag.equals("-"))) {
            throw new InvalidParameterException("flag must be + or -");
        }

        // find the existing filter
        ContentSourceFilter oldFilter = null;
        for (ContentSourceFilter f : listVendorRepoFilters(loggedInUser, label)) {
            if (flag.equals(f.getFlag()) && filter.equals(f.getFilter())) {
                oldFilter = f;
                break;
            }
        }

        if (oldFilter == null) {
            throw new InvalidParameterException("filter does not exist");
        }

        ChannelFactory.remove(oldFilter);

        return 1;
    }

    /**
     * replaces the existing set of filters for a given repo.
     * filters are ranked by their order in the array.
     * @param loggedInUser The current user
     * @param label of the repo to use
     * @param filtersIn list of filters
     * @return 1 on success
     * @apidoc.ignore
     */
    public int setVendorRepoFilters(User loggedInUser, String label,
            List<Map<String, String>> filtersIn) {
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

        if (!loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins can set repo filters.");
        }
        log.warn("Unsupported XMLRPC call used: setVendorRepoFilters {}", label);

        ContentSource cs = lookupVendorContentSourceByLabel(label);

        List<ContentSourceFilter> filters = new ArrayList<>();

        int i = 1;
        for (Map<String, String> filterIn : filtersIn) {
            String flag = filterIn.get("flag");
            String filter = filterIn.get("filter");

            if (!(flag.equals("+") || flag.equals("-"))) {
                throw new InvalidParameterException("flag must be + or -");
            }

            ContentSourceFilter f = new ContentSourceFilter();
            f.setSourceId(cs.getId());
            f.setFlag(flag);
            f.setFilter(filter);
            f.setSortOrder(i);

            filters.add(f);

            i++;
        }

        ChannelFactory.clearContentSourceFilters(cs.getId());

        for (ContentSourceFilter filter : filters) {
            ChannelFactory.save(filter);
        }

        return 1;
    }

   /**
    * Clears the filters for a repo
    * @param loggedInUser The current user
    * @param label of the repo to use
    * @return 1 on success
    * @apidoc.ignore
   **/
    public int clearVendorRepoFilters(User loggedInUser, String label) {
        Role orgAdminRole = RoleFactory.lookupByLabel("org_admin");

        if (!loggedInUser.hasRole(orgAdminRole)) {
            throw new PermissionException("Only Org Admins can remove repo filters.");
        }
        log.warn("Unsupported XMLRPC call used: clearVendorRepoFilters {}", label);

        ContentSource cs = lookupVendorContentSourceByLabel(label);

        ChannelFactory.clearContentSourceFilters(cs.getId());

        return 1;
    }

    /**
     * Refresh pillar data and then schedule channels state on the given minions
     * @param user The current user
     * @param sids server ids for the minions
     * @return action id or 0 if no action is scheduled for any reason
     *
     * @apidoc.doc Refresh pillar data and then schedule channels state on the provided systems
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "sids")
     * @apidoc.returntype #array_single("int", "actionId")
     */

    public long applyChannelState(User user, List<Integer> sids) {
        try {
            List<Long> serverIds = sids.stream().map(Integer::longValue).collect(Collectors.toList());
            List<MinionServer> minionServers = MinionServerFactory.lookupByIds(serverIds).collect(Collectors.toList());
            return ChannelManager.applyChannelState(user, minionServers).orElse(0L);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    private ContentSource lookupContentSourceById(Long repoId, Org org) {
        ContentSource cs = ChannelFactory.lookupContentSource(repoId, org);
        if (cs == null) {
            throw new NoSuchContentSourceException(repoId);
        }
        return cs;
    }

    private ContentSource lookupContentSourceByLabel(String repoLabel, Org org) {
        ContentSource cs = ChannelFactory.lookupContentSourceByOrgAndLabel(org, repoLabel);
        if (cs == null) {
            throw new NoSuchContentSourceException(repoLabel);
        }
        return cs;
    }

    private ContentSource lookupVendorContentSourceByLabel(String repoLabel) {
        ContentSource cs = ChannelFactory.lookupVendorContentSourceByLabel(repoLabel);
        if (cs == null) {
            throw new NoSuchContentSourceException(repoLabel);
        }
        return cs;
    }

    private void setRepoLabel(ContentSource cs, String repoLabel) {
        if (StringUtils.isEmpty(repoLabel)) {
            throw new InvalidParameterException("label might not be empty");
        }
        if (ChannelFactory.lookupContentSourceByOrgAndLabel(cs.getOrg(), repoLabel) !=
                null) {
            throw new InvalidRepoLabelException(repoLabel);
        }
        cs.setLabel(repoLabel);
    }

    private void setRepoUrl(ContentSource cs, String repoUrl) {
        if (StringUtils.isEmpty(repoUrl)) {
            throw new InvalidParameterException("url might not be empty");
        }
        if (!ChannelFactory.lookupContentSourceByOrgAndRepo(cs.getOrg(),
                cs.getType(), repoUrl).isEmpty()) {
            throw new InvalidRepoUrlException(repoUrl);
        }
        cs.setSourceUrl(repoUrl);
    }

    private Long getKeyId(User loggedInUser, String keyDescription) {
        if (StringUtils.isEmpty(keyDescription)) {
            return null;
        }
        CryptoKey key = KickstartFactory.lookupCryptoKey(keyDescription,
                loggedInUser.getOrg());
        if (key == null) {
            throw new NoSuchCryptoKeyException("no key with such description - " +
                    keyDescription);
        }
        return key.getId();
    }
}
