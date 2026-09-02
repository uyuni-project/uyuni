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
package com.redhat.rhn.frontend.xmlrpc.system;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.dto.SystemGroupsDTO;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.ShortSystemInfo;
import com.redhat.rhn.frontend.dto.SystemEventDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandlerApi;
import com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandlerApi;
import com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandlerApi;
import com.redhat.rhn.frontend.xmlrpc.user.UserHandlerApi;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;
import com.suse.manager.api.docs.PublicApiEndpoint;
import com.suse.manager.model.attestation.CoCoAttestationResult;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;
import com.suse.manager.xmlrpc.dto.SystemEventDetailsDto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link SystemHandler}.
 */
@Tag(name = "system",
    description = "Provides methods to access and modify registered system.")
public interface SystemHandlerApi {

    /** Shared summary of the deprecated service pack migration endpoints. */
    String SP_MIGRATION_SUMMARY = "Schedule a Product migration for a system. This call " +
        "is the recommended and supported way of migrating a system to the next Service " +
        "Pack. It will automatically find all mandatory product channels below a given " +
        "target base channel and subscribe the system accordingly. Any additional " +
        "optional channels can be subscribed by providing their labels. Note: This " +
        "method is deprecated and will be removed in a future API version. Please use " +
        "scheduleProductMigration instead.";

    /** As {@link #SP_MIGRATION_SUMMARY}, reproducing the legacy spelling of one variant. */
    String SP_MIGRATION_TYPO_SUMMARY = "Schedule a Prodcut migration for a system. This " +
        "call is the recommended and supported way of migrating a system to the next " +
        "Service Pack. It will automatically find all mandatory product channels below a " +
        "given target base channel and subscribe the system accordingly. Any additional " +
        "optional channels can be subscribed by providing their labels. Note: This " +
        "method is deprecated and will be removed in a future API version. Please use " +
        "scheduleProductMigration instead.";

    /** Shared summary of the product migration endpoints. */
    String PRODUCT_MIGRATION_SUMMARY = "Schedule a Product migration for a system. This " +
        "call is the recommended and supported way of migrating a system to the next " +
        "Service Pack. It will automatically find all mandatory product channels below a " +
        "given target base channel and subscribe the system accordingly. Any additional " +
        "optional channels can be subscribed by providing their labels.";

    /** As {@link #PRODUCT_MIGRATION_SUMMARY}, reproducing the legacy spelling of one variant. */
    String PRODUCT_MIGRATION_TYPO_SUMMARY = "Schedule a Prodcut migration for a system. " +
        "This call is the recommended and supported way of migrating a system to the " +
        "next Service Pack. It will automatically find all mandatory product channels " +
        "below a given target base channel and subscribe the system accordingly. Any " +
        "additional optional channels can be subscribed by providing their labels.";

    /** Shared summary of the distribution upgrade endpoints. */
    String DIST_UPGRADE_SUMMARY = "Schedule a dist upgrade for a system. This call takes " +
        "a list of channel labels that the system will be subscribed to before " +
        "performing the dist upgrade. Note: You can seriously damage your system with " +
        "this call, use it only if you really know what you are doing! Make sure that " +
        "the list of channel labels is complete and in any case do a dry run before " +
        "scheduling an actual dist upgrade.";

    /**
     * Obtains a reactivation key for a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the reactivation key
     */
    @ApiEndpointDoc(
        summary = "Obtains a reactivation key for this server.",
        requestClass = SidRequest.class,
        responseClass = StringResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "key")
    )
    String obtainReactivationKey(User loggedInUser, Integer sid);

    /**
     * Obtains a reactivation key for the server identified by a client certificate.
     *
     * @param clientCert the client certificate of the system
     * @return the reactivation key
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Obtains a reactivation key for this server.",
        requestClass = ClientCertRequest.class,
        responseClass = StringResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "key")
    )
    String obtainReactivationKey(String clientCert);

    /**
     * Adds an entitlement to a given server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param entitlementLevel the entitlement to add
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds an entitlement to a given server.",
        requestClass = UpgradeEntitlementRequest.class,
        isIntegerResponse = true
    )
    int upgradeEntitlement(User loggedInUser, Integer sid, String entitlementLevel);

    /**
     * Subscribes a server to the given child channels.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param channelIdsOrLabels the child channels to subscribe to
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Subscribe the given server to the child channels provided. This " +
            "method will unsubscribe the server from any child channels that the server " +
            "is currently subscribed to, but that are not included in the list. The user " +
            "may provide either a list of channel ids (int) or a list of channel labels " +
            "(string) as input. Changes to channel assignments on salt managed systems " +
            "will take effect at next highstate application.",
        requestClass = SetChildChannelsRequest.class,
        isIntegerResponse = true
    )
    int setChildChannels(User loggedInUser, Integer sid, List<?> channelIdsOrLabels);

    /**
     * Assigns the server to a new base channel.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param channelLabel the label of the new base channel
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Assigns the server to a new base channel. If the user provides an " +
            "empty string for the channelLabel, the current base channel and all child " +
            "channels will be removed from the system.",
        requestClass = SetBaseChannelRequest.class,
        isIntegerResponse = true
    )
    int setBaseChannel(User loggedInUser, Integer sid, String channelLabel);

    /**
     * Schedules an action to change the channels of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param baseChannelLabel the label of the base channel
     * @param childLabels the labels of the child channels
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule an action to change the channels of the given system. Works " +
            "for both traditional and Salt systems. This method accepts labels for the " +
            "base and child channels. If the user provides an empty string for the " +
            "channelLabel, the current base channel and all child channels will be " +
            "removed from the system.",
        requestClass = ScheduleChangeChannelsRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    long scheduleChangeChannels(User loggedInUser, Integer sid, String baseChannelLabel,
        List<String> childLabels, Date earliestOccurrence);

    /**
     * Schedules an action to change the channels of several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param baseChannelLabel the label of the base channel
     * @param childLabels the labels of the child channels
     * @param earliestOccurrence the time to schedule the action
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule an action to change the channels of the given system. Works " +
            "for both traditional and Salt systems. This method accepts labels for the " +
            "base and child channels. If the user provides an empty string for the " +
            "baseChannelLabel and an empty list for the childLabels, the current base " +
            "channel and all child channels will be removed from the system. If the user " +
            "provides only a different baseChannelLabel and an empty list for childLabels, " +
            "the new base channel is assigned to the system and we search for compatible " +
            "child channels for the assigned one. If the base channel stay empty, all the " +
            "child channels from the list should be compatible with the currently assigned " +
            "base channel. The currently assigned child channels are exchanged with the " +
            "channels provided in the childLabels list. When both baseChannelLabel and " +
            "childLabels are provided, the compatibility is checked, and the system gets " +
            "these new set of channels assigned.",
        requestClass = ScheduleChangeChannelsForSystemsRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionIds")
    )
    List<Long> scheduleChangeChannels(User loggedInUser, List<Integer> sids,
        String baseChannelLabel, List<String> childLabels, Date earliestOccurrence);

    /**
     * Lists the base channels a server may subscribe to.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the subscribable base channels
     */
    @ApiEndpointDoc(
        summary = "Returns a list of subscribable base channels.",
        method = HttpMethod.get,
        responseClass = SubscribableBaseChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Object[] listSubscribableBaseChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists all servers visible to the user.
     *
     * @param loggedInUser the current user
     * @return the servers visible to the user
     */
    @ApiEndpointDoc(
        summary = "Returns a list of all servers visible to the user.",
        method = HttpMethod.get,
        responseClass = ShortSystemInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listSystems(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the empty system profiles visible to the user.
     *
     * @param loggedInUser the current user
     * @return the empty system profiles
     */
    @ApiEndpointDoc(
        summary = "Returns a list of empty system profiles visible to user (created by " +
            "the createSystemProfile method).",
        method = HttpMethod.get,
        responseClass = EmptySystemProfileListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listEmptySystemProfiles(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the active servers visible to the user.
     *
     * @param loggedInUser the current user
     * @return the active servers
     */
    @ApiEndpointDoc(
        summary = "Returns a list of active servers visible to the user.",
        method = HttpMethod.get,
        responseClass = ShortSystemInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<ShortSystemInfo> listActiveSystems(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the details of the given active servers.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @return the details of the active servers
     */
    @ApiEndpointDoc(
        summary = "Given a list of server ids, returns a list of active servers' details " +
            "visible to the user.",
        method = HttpMethod.get,
        responseClass = ActiveSystemDetailsListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server details")
    )
    List<Map<String, Object>> listActiveSystemsDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sids", in = ParameterIn.QUERY, required = true) List<Integer> sids);

    /**
     * Lists the child channels a server may subscribe to.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the subscribable child channels
     */
    @ApiEndpointDoc(
        summary = "Returns a list of subscribable child channels. This only shows " +
            "channels the system is *not* currently subscribed to.",
        method = HttpMethod.get,
        responseClass = SubscribableChildChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "child channel")
    )
    Object[] listSubscribableChildChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the installed packages older than the given one.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param name the package name
     * @param version the package version
     * @param release the package release
     * @param epoch the package epoch
     * @return the older installed packages
     */
    @ApiEndpointDoc(
        summary = "Given a package name, version, release, and epoch, returns the list " +
            "of packages installed on the system with the same name that are older.",
        method = HttpMethod.get,
        responseClass = NvrePackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    Object[] listOlderInstalledPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true,
                description = "Package name.") String name,
        @Parameter(name = "version", in = ParameterIn.QUERY, required = true,
                description = "Package version.") String version,
        @Parameter(name = "release", in = ParameterIn.QUERY, required = true,
                description = "Package release.") String release,
        @Parameter(name = "epoch", in = ParameterIn.QUERY, required = true,
                description = "Package epoch.") String epoch);

    /**
     * Lists the installed packages newer than the given one.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param name the package name
     * @param version the package version
     * @param release the package release
     * @param epoch the package epoch
     * @return the newer installed packages
     */
    @ApiEndpointDoc(
        summary = "Given a package name, version, release, and epoch, returns the list " +
            "of packages installed on the system w/ the same name that are newer.",
        method = HttpMethod.get,
        responseClass = NvrePackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    Object[] listNewerInstalledPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true,
                description = "Package name.") String name,
        @Parameter(name = "version", in = ParameterIn.QUERY, required = true,
                description = "Package version.") String version,
        @Parameter(name = "release", in = ParameterIn.QUERY, required = true,
                description = "Package release.") String release,
        @Parameter(name = "epoch", in = ParameterIn.QUERY, required = true,
                description = "Package epoch.") String epoch);

    /**
     * Checks whether the package with the given NVR is installed on a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param name the package name
     * @param version the package version
     * @param release the package release
     * @return 1 if the package is installed, 0 otherwise
     */
    @ApiEndpointDoc(
        summary = "Check if the package with the given NVRE is installed on given system.",
        method = HttpMethod.get,
        isIntegerResponse = true,
        responseDescription = "1 if package exists, 0 if not, exception is thrown if an error occurs",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int isNvreInstalled(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true,
                description = "Package name.") String name,
        @Parameter(name = "version", in = ParameterIn.QUERY, required = true,
                description = "Package version.") String version,
        @Parameter(name = "release", in = ParameterIn.QUERY, required = true,
                description = "Package release.") String release);

    /**
     * Checks whether the package with the given NVRE is installed on a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param name the package name
     * @param version the package version
     * @param release the package release
     * @param epoch the package epoch
     * @return 1 if the package is installed, 0 otherwise
     */
    @ApiEndpointDoc(
        summary = "Is the package with the given NVRE installed on given system.",
        method = HttpMethod.get,
        isIntegerResponse = true,
        responseDescription = "1 if package exists, 0 if not, exception is thrown if an error occurs",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int isNvreInstalled(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true,
                description = "Package name.") String name,
        @Parameter(name = "version", in = ParameterIn.QUERY, required = true,
                description = "Package version.") String version,
        @Parameter(name = "release", in = ParameterIn.QUERY, required = true,
                description = "Package release.") String release,
        @Parameter(name = "epoch", in = ParameterIn.QUERY, required = true,
                description = "Package epoch.") String epoch);

    /**
     * Lists the latest upgradable packages of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the latest upgradable packages
     */
    @ApiEndpointDoc(
        summary = "Get the list of latest upgradable packages for a given system.",
        method = HttpMethod.get,
        responseClass = UpgradablePackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listLatestUpgradablePackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists all installable packages of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the installable packages
     */
    @ApiEndpointDoc(
        summary = "Get the list of all installable packages for a given system.",
        method = HttpMethod.get,
        responseClass = InstallablePackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listAllInstallablePackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the latest installable packages of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the latest installable packages
     */
    @ApiEndpointDoc(
        summary = "Get the list of latest installable packages for a given system.",
        method = HttpMethod.get,
        responseClass = InstallablePackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listLatestInstallablePackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the latest available version of a package for each of the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageName the name of the package
     * @return the latest available package of each system
     */
    @ApiEndpointDoc(
        summary = "Get the latest available version of a package for each system",
        method = HttpMethod.get,
        responseClass = LatestAvailablePackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<Map<String, Object>> listLatestAvailablePackage(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sids", in = ParameterIn.QUERY, required = true) List<Integer> sids,
        @Parameter(name = "packageName", in = ParameterIn.QUERY, required = true) String packageName);

    /**
     * Gets the entitlements of a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the entitlements of the server
     */
    @ApiEndpointDoc(
        summary = "Gets the entitlements for a given server.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "entitlement_label")
    )
    Object[] getEntitlements(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the system id file of a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the system id file
     */
    @ApiEndpointDoc(
        summary = "Get the system ID file for a given server.",
        requestClass = SidRequest.class,
        responseClass = StringResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    String downloadSystemId(User loggedInUser, Integer sid);

    /**
     * Lists the installed packages of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the installed packages
     */
    @ApiEndpointDoc(
        summary = "List the installed packages for a given system. Usage of " +
            "listInstalledPackages is preferred, as it returns architecture label " +
            "(not name).",
        method = HttpMethod.get,
        responseClass = InstalledPackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the installed packages of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the installed packages
     */
    @ApiEndpointDoc(
        summary = "List the installed packages for a given system.",
        method = HttpMethod.get,
        responseClass = InstalledPackageDetailsListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listInstalledPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the package lock status of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the package lock status
     */
    @ApiEndpointDoc(
        summary = "List current package locks status.",
        method = HttpMethod.get,
        responseClass = PackageLockStatusListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listPackagesLockStatus(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Deletes the given guest profiles of a host.
     *
     * @param loggedInUser the current user
     * @param hostId the id of the host
     * @param guestNames the names of the guest profiles to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete the specified list of guest profiles for a given host",
        requestClass = DeleteGuestProfilesRequest.class,
        isIntegerResponse = true
    )
    Integer deleteGuestProfiles(User loggedInUser, Integer hostId, List<String> guestNames);

    /**
     * Deletes the given systems asynchronously.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete systems given a list of system ids asynchronously.",
        requestClass = SidsRequest.class,
        isIntegerResponse = true
    )
    int deleteSystems(User loggedInUser, List<Integer> sids);

    /**
     * Deletes the given systems asynchronously.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param cleanupType the cleanup behaviour
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete systems given a list of system ids asynchronously.",
        requestClass = DeleteSystemsRequest.class,
        isIntegerResponse = true
    )
    int deleteSystems(User loggedInUser, List<Integer> sids, String cleanupType);

    /**
     * Deletes the system identified by a client certificate.
     *
     * @param clientCert the client certificate of the system
     * @return 1 on success
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Delete a system given its client certificate.",
        requestClass = ClientCertRequest.class,
        isIntegerResponse = true
    )
    int deleteSystem(String clientCert);

    /**
     * Deletes a system without cleanup.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a system given its server id synchronously without cleanup",
        requestClass = SidRequest.class,
        isIntegerResponse = true
    )
    int deleteSystem(User loggedInUser, Integer sid);

    /**
     * Deletes a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param cleanupType the cleanup behaviour
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a system given its server id synchronously",
        requestClass = DeleteSystemRequest.class,
        isIntegerResponse = true
    )
    int deleteSystem(User loggedInUser, Integer sid, String cleanupType);

    /**
     * Gets the addresses and hostname of a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the network information of the server
     */
    @ApiEndpointDoc(
        summary = "Get the addresses and hostname for a given server.",
        method = HttpMethod.get,
        responseClass = NetworkInfoResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "network info")
    )
    Map<String, String> getNetwork(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the addresses and hostname of the given servers.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @return the network information of the servers
     */
    @ApiEndpointDoc(
        summary = "Get the addresses and hostname for a given list of systems.",
        method = HttpMethod.get,
        responseClass = SystemNetworkInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "network info")
    )
    List<Map<String, Object>> getNetworkForSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sids", in = ParameterIn.QUERY, required = true) List<Integer> sids);

    /**
     * Gets the network devices of a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the network devices of the server
     */
    @ApiEndpointDoc(
        summary = "Returns the network devices for the given server.",
        method = HttpMethod.get,
        responseClass = NetworkDeviceListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "network device")
    )
    List<NetworkInterface> getNetworkDevices(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Sets the membership of a server in a server group.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param sgid the id of the server group
     * @param member whether the server belongs to the group
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set a servers membership in a given group.",
        requestClass = SetGroupMembershipRequest.class,
        isIntegerResponse = true
    )
    int setGroupMembership(User loggedInUser, Integer sid, Integer sgid, Boolean member);

    /**
     * Lists the groups available to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the available server groups
     */
    @ApiEndpointDoc(
        summary = "List the available groups for a given system.",
        method = HttpMethod.get,
        responseClass = SystemGroupListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system group")
    )
    Object[] listGroups(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the systems of a given user.
     *
     * @param loggedInUser the current user
     * @param login the login name of the user
     * @return the systems of the user
     */
    @ApiEndpointDoc(
        summary = "List systems for a given user.",
        method = HttpMethod.get,
        responseClass = ShortSystemInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<ShortSystemInfo> listUserSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "login", in = ParameterIn.QUERY, required = true,
                description = "User's login name.") String login);

    /**
     * Lists the systems of the logged in user.
     *
     * @param loggedInUser the current user
     * @return the systems of the user
     */
    @ApiEndpointDoc(
        summary = "List systems for the logged in user.",
        method = HttpMethod.get,
        responseClass = ShortSystemInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<ShortSystemInfo> listUserSystems(@Parameter(hidden = true) User loggedInUser);

    /**
     * Sets the custom values of a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param values the custom values to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set custom values for the specified server.",
        requestClass = SetCustomValuesRequest.class,
        isIntegerResponse = true
    )
    int setCustomValues(User loggedInUser, Integer sid, Map<String, String> values);

    /**
     * Gets the custom values of a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the custom values of the server
     */
    @ApiEndpointDoc(
        summary = "Get the custom data values defined for the server.",
        method = HttpMethod.get,
        responseClass = CustomValueResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "custom value")
    )
    Map<String, String> getCustomValues(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Deletes the custom values of a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param keys the custom information keys to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete the custom values defined for the custom system information " +
            "keys provided from the given system.<br/> (Note: Attempt to delete values of " +
            "non-existing keys throws exception. Attempt to delete value of existing key " +
            "which has assigned no values doesn't throw exception.)",
        requestClass = DeleteCustomValuesRequest.class,
        isIntegerResponse = true
    )
    int deleteCustomValues(User loggedInUser, Integer sid, List<String> keys);

    /**
     * Sets the profile name of a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param name the profile name
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the profile name for the server.",
        requestClass = SetProfileNameRequest.class,
        isIntegerResponse = true
    )
    int setProfileName(User loggedInUser, Integer sid, String name);

    /**
     * Adds a note to a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param subject the subject of the note
     * @param body the content of the note
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add a new note to the given server.",
        requestClass = AddNoteRequest.class,
        isIntegerResponse = true
    )
    int addNote(User loggedInUser, Integer sid, String subject, String body);

    /**
     * Deletes a note from a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param noteId the id of the note
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes the given note from the server.",
        requestClass = DeleteNoteRequest.class,
        isIntegerResponse = true
    )
    int deleteNote(User loggedInUser, Integer sid, Integer noteId);

    /**
     * Deletes all notes from a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes all notes from the server.",
        requestClass = SidRequest.class,
        isIntegerResponse = true
    )
    int deleteNotes(User loggedInUser, Integer sid);

    /**
     * Lists the scheduled actions of the given type on a server after a date.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param actionType the type of the action
     * @param earliestDate the earliest date to report
     * @return the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "List system actions of the specified type that were *scheduled* " +
            "against the given server after the specified date. \"actionType\" should be " +
            "exactly the string returned in the action_type field from the " +
            "listSystemEvents(sessionKey, serverId) method. For example, 'Package Install' " +
            "or 'Initiate a kickstart for a virtual guest.' Note: see also " +
            "system.getEventHistory method which returns a history of all events.",
        method = HttpMethod.get,
        responseClass = SystemActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    List<Map<String, Object>> listSystemEvents(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true,
                description = "ID of system.") Integer sid,
        @Parameter(name = "actionType", in = ParameterIn.QUERY, required = true,
                description = "Type of the action.") String actionType,
        @Parameter(name = "earliestDate", in = ParameterIn.QUERY, required = true)
            Date earliestDate);

    /**
     * Lists the scheduled actions on a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "List all system actions that were *scheduled* against the given " +
            "server. This may require the caller to filter the result to fetch actions " +
            "with a specific action type or to use the overloaded system.listSystemEvents " +
            "method with actionType as a parameter. Note: see also system.getEventHistory " +
            "method which returns a history of all events.",
        method = HttpMethod.get,
        responseClass = SystemActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    List<Map<String, Object>> listSystemEvents(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true,
                description = "ID of system.") Integer sid);

    /**
     * Lists the scheduled actions of the given type on a server.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param actionType the type of the action
     * @return the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "List system actions of the specified type that were *scheduled* " +
            "against the given server. \"actionType\" should be exactly the string " +
            "returned in the action_type field from the listSystemEvents(sessionKey, " +
            "serverId) method. For example, 'Package Install' or 'Initiate a kickstart " +
            "for a virtual guest.' Note: see also system.getEventHistory method which " +
            "returns a history of all events.",
        method = HttpMethod.get,
        responseClass = SystemActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    List<Map<String, Object>> listSystemEvents(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true,
                description = "ID of system.") Integer sid,
        @Parameter(name = "actionType", in = ParameterIn.QUERY, required = true,
                description = "Type of the action.") String actionType);

    /**
     * Lists the scheduled actions on a server after a date.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestDate the earliest date to report
     * @return the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "List system actions of the specified type that were *scheduled* " +
            "against the given server after the specified date. This may require the " +
            "caller to filter the result to fetch actions with a specific action type or " +
            "to use the overloaded system.listSystemEvents method with actionType as a " +
            "parameter. Note: see also system.getEventHistory method which returns a " +
            "history of all events.",
        method = HttpMethod.get,
        responseClass = SystemActionListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "action")
    )
    List<Map<String, Object>> listSystemEvents(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true,
                description = "ID of system.") Integer sid,
        @Parameter(name = "earliestDate", in = ParameterIn.QUERY, required = true)
            Date earliestDate);

    /**
     * Provisions a guest on the given host.
     *
     * @param loggedInUser the current user
     * @param sid the id of the host
     * @param guestName the name of the guest
     * @param profileName the kickstart profile to use
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Provision a guest on the host specified. Defaults to: memory=512MB, " +
            "vcpu=1, storage=3GB, mac_address=random.",
        requestClass = ProvisionVirtualGuestRequest.class,
        isIntegerResponse = true
    )
    int provisionVirtualGuest(User loggedInUser, Integer sid, String guestName,
        String profileName);

    /**
     * Provisions a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the system
     * @param profileName the profile to use
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Provision a system using the specified kickstart/autoinstallation profile.",
        requestClass = ProvisionSystemRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int provisionSystem(User loggedInUser, Integer sid, String profileName);

    /**
     * Provisions a system through a proxy.
     *
     * @param loggedInUser the current user
     * @param sid the id of the system
     * @param proxy the id of the proxy
     * @param profileName the profile to use
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Provision a system using the specified kickstart/autoinstallation profile.",
        requestClass = ProvisionSystemViaProxyRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int provisionSystem(User loggedInUser, Integer sid, Integer proxy, String profileName);

    /**
     * Provisions a system at the given time.
     *
     * @param loggedInUser the current user
     * @param sid the id of the system
     * @param profileName the profile to use
     * @param earliestDate the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Provision a system using the specified kickstart/autoinstallation profile.",
        requestClass = ProvisionSystemAtRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int provisionSystem(User loggedInUser, Integer sid, String profileName, Date earliestDate);

    /**
     * Provisions a system through a proxy at the given time.
     *
     * @param loggedInUser the current user
     * @param sid the id of the system
     * @param proxy the id of the proxy
     * @param profileName the profile to use
     * @param earliestDate the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Provision a system using the specified kickstart/autoinstallation profile.",
        requestClass = ProvisionSystemViaProxyAtRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int provisionSystem(User loggedInUser, Integer sid, Integer proxy, String profileName,
        Date earliestDate);

    /**
     * Provisions a system at the given time with advanced options.
     *
     * @param loggedInUser the current user
     * @param sid the id of the system
     * @param profileName the profile to use
     * @param earliestDate the time to schedule the action
     * @param advancedOptions the advanced provisioning options
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Provision a system using the specified kickstart/autoinstallation profile.",
        requestClass = ProvisionSystemWithOptionsRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int provisionSystem(User loggedInUser, Integer sid, String profileName, Date earliestDate,
        Map<String, String> advancedOptions);

    /**
     * Provisions a system through a proxy at the given time with advanced options.
     *
     * @param loggedInUser the current user
     * @param sid the id of the system
     * @param proxy the id of the proxy
     * @param profileName the profile to use
     * @param earliestDate the time to schedule the action
     * @param advancedOptions the advanced provisioning options
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Provision a system using the specified kickstart/autoinstallation profile.",
        requestClass = ProvisionSystemViaProxyWithOptionsRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int provisionSystem(User loggedInUser, Integer sid, Integer proxy, String profileName,
        Date earliestDate, Map<String, String> advancedOptions);

    /**
     * Provisions a guest with the given resources.
     *
     * @param loggedInUser the current user
     * @param sid the id of the host
     * @param guestName the name of the guest
     * @param profileName the kickstart profile to use
     * @param memoryMb the memory to allocate
     * @param vcpus the number of virtual CPUs
     * @param storageGb the size of the disk image
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Provision a guest on the host specified. This schedules the guest " +
            "for creation and will begin the provisioning process when the host checks " +
            "in or if OSAD is enabled will begin immediately. Defaults to mac_address=random.",
        requestClass = ProvisionVirtualGuestWithResourcesRequest.class,
        isIntegerResponse = true
    )
    int provisionVirtualGuest(User loggedInUser, Integer sid, String guestName,
        String profileName, Integer memoryMb, Integer vcpus, Integer storageGb);

    /**
     * Provisions a guest with the given resources and MAC address.
     *
     * @param loggedInUser the current user
     * @param sid the id of the host
     * @param guestName the name of the guest
     * @param profileName the kickstart profile to use
     * @param memoryMb the memory to allocate
     * @param vcpus the number of virtual CPUs
     * @param storageGb the size of the disk image
     * @param macAddress the MAC address of the guest
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Provision a guest on the host specified. This schedules the guest for " +
            "creation and will begin the provisioning process when the host checks in or " +
            "if OSAD is enabled will begin immediately.",
        requestClass = ProvisionVirtualGuestWithMacRequest.class,
        isIntegerResponse = true
    )
    int provisionVirtualGuest(User loggedInUser, Integer sid, String guestName,
        String profileName, Integer memoryMb, Integer vcpus, Integer storageGb,
        String macAddress);

    /**
     * Gets the system ids of the systems with the given name.
     *
     * @param loggedInUser the current user
     * @param name the name of the system
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "Get system IDs and last check in information for the given system name.",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemOverview> getId(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true) String name);

    /**
     * Gets the name and last check in information of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the name information of the system
     */
    @ApiEndpointDoc(
        summary = "Get system name and last check in information for the given system ID.",
        method = HttpMethod.get,
        responseClass = NameInfoResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "name info")
    )
    Map<String, Object> getName(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the registration date of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the registration date of the system
     */
    @ApiEndpointDoc(
        summary = "Returns the date the system was registered.",
        method = HttpMethod.get,
        responseClass = DateResponse.class,
        responseDescription = "The date the system was registered, in local time",
        legacyDocResponse = @LegacyDocResponse(name = "date", type = "dateTime.iso8601")
    )
    Date getRegistrationDate(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the child channels a server is subscribed to.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the subscribed child channels
     */
    @ApiEndpointDoc(
        summary = "Returns a list of subscribed child channels.",
        method = HttpMethod.get,
        responseClass = SubscribedChildChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    List<Channel> listSubscribedChildChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the systems whose name matches the given regular expression.
     *
     * @param loggedInUser the current user
     * @param regexp the regular expression to match
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "Returns a list of system IDs whose name matches the supplied regular " +
            "expression defined by Java representation of regular expressions " +
            "(http://docs.oracle.com/javase/1.5.0/docs/api/java/util/regex/Pattern.html)",
        requestClass = SearchByNameRequest.class,
        responseClass = ShortSystemInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<ShortSystemInfo> searchByName(User loggedInUser, String regexp);

    /**
     * Lists the users which can administer a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the administrators of the system
     */
    @ApiEndpointDoc(
        summary = "Returns a list of users which can administer the system.",
        method = HttpMethod.get,
        responseClass = AdministratorListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "user")
    )
    Object[] listAdministrators(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the running kernel of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the running kernel
     */
    @ApiEndpointDoc(
        summary = "Returns the running kernel of the given system.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "kernel")
    )
    String getRunningKernel(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the history items of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the history items of the system
     */
    @ApiEndpointDoc(
        summary = "Returns a list history items associated with the system, ordered from " +
            "newest to oldest. Note that the details may be empty for events that were " +
            "scheduled against the system (as compared to instant). For more information " +
            "on such events, see the system.listSystemEvents operation. Note: This " +
            "version of the method is deprecated and the return value will be changed in " +
            "a future API version. Please one of the other overloaded versions of " +
            "getEventHistory.",
        method = HttpMethod.get,
        responseClass = HistoryEventListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "history event")
    )
    Object[] getEventHistory(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the history items of a system after a date.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestDate the earliest date to report
     * @param offset the number of results to skip
     * @param limit the maximum number of results
     * @return the history items of the system
     */
    @ApiEndpointDoc(
        summary = "Returns a list of history items associated with the system happened " +
            "after the specified date. The list is paged and ordered from newest to oldest.",
        method = HttpMethod.get,
        responseClass = SystemEventListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system event")
    )
    List<SystemEventDto> getEventHistory(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "earliestDate", in = ParameterIn.QUERY, required = true)
            Date earliestDate,
        @Parameter(name = "offset", in = ParameterIn.QUERY, required = true,
                description = "Number of results to skip") Integer offset,
        @Parameter(name = "limit", in = ParameterIn.QUERY, required = true,
                description = "Maximum number of results") Integer limit);

    /**
     * Lists the history items of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param offset the number of results to skip
     * @param limit the maximum number of results
     * @return the history items of the system
     */
    @ApiEndpointDoc(
        summary = "Returns a list of history items associated with the system. The list " +
            "is paged and ordered from newest to oldest.",
        method = HttpMethod.get,
        responseClass = SystemEventListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system event")
    )
    List<SystemEventDto> getEventHistory(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "offset", in = ParameterIn.QUERY, required = true,
                description = "Number of results to skip") Integer offset,
        @Parameter(name = "limit", in = ParameterIn.QUERY, required = true,
                description = "Maximum number of results") Integer limit);

    /**
     * Lists the history items of a system after a date.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestDate the earliest date to report
     * @return the history items of the system
     */
    @ApiEndpointDoc(
        summary = "Returns a list of history items associated with the system happened " +
            "after the specified date. The list is ordered from newest to oldest.",
        method = HttpMethod.get,
        responseClass = SystemEventListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system event")
    )
    List<SystemEventDto> getEventHistory(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "earliestDate", in = ParameterIn.QUERY, required = true)
            Date earliestDate);

    /**
     * Gets the details of a system event.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param eid the id of the event
     * @return the details of the event
     */
    @ApiEndpointDoc(
        summary = "Returns the details of the event associated with the specified server " +
            "and event. The event id must be a value returned by the " +
            "system.getEventHistory API.",
        method = HttpMethod.get,
        responseClass = SystemEventDetailsResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system event")
    )
    SystemEventDetailsDto getEventDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "eid", in = ParameterIn.QUERY, required = true,
                description = "ID of the event") Integer eid);

    /**
     * Lists the errata relevant to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the relevant errata
     */
    @ApiEndpointDoc(
        summary = "Returns a list of all errata that are relevant to the system.",
        method = HttpMethod.get,
        responseClass = ErrataOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<ErrataOverview> getRelevantErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the errata relevant to the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @return the relevant errata of each system
     */
    @ApiEndpointDoc(
        summary = "Returns a list of all errata that are relevant to a list of systems.",
        method = HttpMethod.get,
        responseClass = ServerErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server_errata")
    )
    List<Map<String, Object>> getRelevantErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sids", in = ParameterIn.QUERY, required = true) List<Integer> sids);

    /**
     * Lists the errata of the given type relevant to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param advisoryType the type of the advisory
     * @return the relevant errata
     */
    @ApiEndpointDoc(
        summary = "Returns a list of all errata of the specified type that are relevant " +
            "to the system.",
        method = HttpMethod.get,
        responseClass = ErrataOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<ErrataOverview> getRelevantErrataByType(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "advisoryType", in = ParameterIn.QUERY, required = true,
                description = "type of advisory (one of of the following: 'Security " +
                    "Advisory', 'Product Enhancement Advisory', 'Bug Fix Advisory'")
            String advisoryType);

    /**
     * Lists the errata applicable to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the applicable errata
     */
    @ApiEndpointDoc(
        summary = "Provides an array of errata that are applicable to a given system.",
        method = HttpMethod.get,
        responseClass = ErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Errata[] getUnscheduledErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Schedules an action to apply errata updates to several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param errataIds the ids of the errata
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to multiple systems.",
        requestClass = ApplyErrataToSystemsRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids,
        List<Integer> errataIds);

    /**
     * Schedules an action to apply errata updates to several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param errataIds the ids of the errata
     * @param allowModules whether to allow the call despite modular content
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to multiple systems.",
        requestClass = ApplyErrataToSystemsWithModulesRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids,
        List<Integer> errataIds, Boolean allowModules);

    /**
     * Schedules an action to apply errata updates to several systems at a given time.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param errataIds the ids of the errata
     * @param earliestOccurrence the time to schedule the action
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to multiple systems at a " +
            "given date/time.",
        requestClass = ApplyErrataToSystemsAtRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids,
        List<Integer> errataIds, Date earliestOccurrence);

    /**
     * Schedules an action to apply errata updates to several systems at a given time.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param errataIds the ids of the errata
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to multiple systems at a " +
            "given date/time.",
        requestClass = ApplyErrataToSystemsAtWithModulesRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids,
        List<Integer> errataIds, Date earliestOccurrence, Boolean allowModules);

    /**
     * Schedules an action to apply errata updates to several systems at a given time.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param errataIds the ids of the errata
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @param onlyRelevant whether to apply only the relevant errata
     * @param allowVendorChange whether to allow a vendor change
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to multiple systems at a " +
            "given date/time.",
        requestClass = ApplyErrataToSystemsFullRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sids,
        List<Integer> errataIds, Date earliestOccurrence, Boolean allowModules,
        Boolean onlyRelevant, Boolean allowVendorChange);

    /**
     * Schedules an action to apply errata updates to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param errataIds the ids of the errata
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to a system.",
        requestClass = ApplyErrataRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, Integer sid, List<Integer> errataIds);

    /**
     * Schedules an action to apply errata updates to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param errataIds the ids of the errata
     * @param allowModules whether to allow the call despite modular content
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to a system.",
        requestClass = ApplyErrataWithModulesRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, Integer sid, List<Integer> errataIds,
        Boolean allowModules);

    /**
     * Schedules an action to apply errata updates to a system at a given time.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param errataIds the ids of the errata
     * @param earliestOccurrence the time to schedule the action
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to a system at a given " +
            "date/time.",
        requestClass = ApplyErrataAtRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, Integer sid, List<Integer> errataIds,
        Date earliestOccurrence);

    /**
     * Schedules an action to apply errata updates to a system at a given time.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param errataIds the ids of the errata
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to a system at a given " +
            "date/time.",
        requestClass = ApplyErrataAtWithModulesRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, Integer sid, List<Integer> errataIds,
        Date earliestOccurrence, Boolean allowModules);

    /**
     * Schedules an action to apply errata updates to the given systems at a given time.
     *
     * @param sid the ids of the servers
     * @param loggedInUser the current user
     * @param errataIds the ids of the errata
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @param onlyRelevant whether to apply only the relevant errata
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedules an action to apply errata updates to a system at a given " +
            "date/time.",
        requestClass = ApplyErrataOnlyRelevantRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    List<Long> scheduleApplyErrata(User loggedInUser, List<Integer> sid,
        List<Integer> errataIds, Date earliestOccurrence, Boolean allowModules,
        Boolean onlyRelevant);

    /**
     * Compares the packages installed on two systems.
     *
     * @param loggedInUser the current user
     * @param sid1 the id of the first server
     * @param sid2 the id of the second server
     * @return the package differences
     */
    @ApiEndpointDoc(
        summary = "Compares the packages installed on two systems.",
        requestClass = ComparePackagesRequest.class,
        responseClass = PackageMetadataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package metadata")
    )
    Object[] comparePackages(User loggedInUser, Integer sid1, Integer sid2);

    /**
     * Gets the DMI information of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the DMI information
     */
    @ApiEndpointDoc(
        summary = "Gets the DMI information of a system.",
        method = HttpMethod.get,
        responseClass = DmiResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "DMI")
    )
    Object getDmi(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the CPU information of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the CPU information
     */
    @ApiEndpointDoc(
        summary = "Gets the CPU information of a system.",
        method = HttpMethod.get,
        responseClass = CpuResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "CPU")
    )
    Object getCpu(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the memory information of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the memory information
     */
    @ApiEndpointDoc(
        summary = "Gets the memory information for a system.",
        method = HttpMethod.get,
        responseClass = MemoryResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "memory")
    )
    Map<String, Long> getMemory(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the devices of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the devices of the system
     */
    @ApiEndpointDoc(
        summary = "Gets a list of devices for a system.",
        method = HttpMethod.get,
        responseClass = DeviceListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "device")
    )
    Object[] getDevices(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Schedules a package installation for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageIds the ids of the packages
     * @param earliestOccurrence the time to schedule the action
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule package installation for several systems.",
        requestClass = SchedulePackagesForSystemsRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long[] schedulePackageInstall(User loggedInUser, List<Integer> sids,
        List<Integer> packageIds, Date earliestOccurrence);

    /**
     * Schedules a package installation for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageIds the ids of the packages
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule package installation for several systems.",
        requestClass = SchedulePackagesForSystemsWithModulesRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long[] schedulePackageInstall(User loggedInUser, List<Integer> sids,
        List<Integer> packageIds, Date earliestOccurrence, Boolean allowModules);

    /**
     * Schedules a full package update for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule full package update for several systems.",
        requestClass = SchedulePackageUpdateRequest.class,
        isIntegerResponse = true,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long schedulePackageUpdate(User loggedInUser, List<Integer> sids, Date earliestOccurrence);

    /**
     * Schedules a package installation for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageIds the ids of the packages
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule package installation for a system.",
        requestClass = SchedulePackagesRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long schedulePackageInstall(User loggedInUser, Integer sid, List<Integer> packageIds,
        Date earliestOccurrence);

    /**
     * Schedules a package installation for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageIds the ids of the packages
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule package installation for a system.",
        requestClass = SchedulePackagesWithModulesRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long schedulePackageInstall(User loggedInUser, Integer sid, List<Integer> packageIds,
        Date earliestOccurrence, Boolean allowModules);

    /**
     * Schedules a package installation for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageNevraList the packages to install
     * @param earliestOccurrence the time to schedule the action
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule package installation for several systems.",
        requestClass = ScheduleNevraForSystemsRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long[] schedulePackageInstallByNevra(User loggedInUser, List<Integer> sids,
        List<Map<String, String>> packageNevraList, Date earliestOccurrence);

    /**
     * Schedules a package installation for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageNevraList the packages to install
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule package installation for several systems.",
        requestClass = ScheduleNevraForSystemsWithModulesRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long[] schedulePackageInstallByNevra(User loggedInUser, List<Integer> sids,
        List<Map<String, String>> packageNevraList, Date earliestOccurrence,
        Boolean allowModules);

    /**
     * Schedules a package installation for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageNevraList the packages to install
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule package installation for a system.",
        requestClass = ScheduleNevraRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long schedulePackageInstallByNevra(User loggedInUser, Integer sid,
        List<Map<String, String>> packageNevraList, Date earliestOccurrence);

    /**
     * Schedules a package installation for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageNevraList the packages to install
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule package installation for a system.",
        requestClass = ScheduleNevraWithModulesRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long schedulePackageInstallByNevra(User loggedInUser, Integer sid,
        List<Map<String, String>> packageNevraList, Date earliestOccurrence,
        Boolean allowModules);

    /**
     * Schedules a package removal for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageIds the ids of the packages
     * @param earliestOccurrence the time to schedule the action
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule package removal for several systems.",
        requestClass = SchedulePackagesForSystemsRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long[] schedulePackageRemove(User loggedInUser, List<Integer> sids,
        List<Integer> packageIds, Date earliestOccurrence);

    /**
     * Schedules a package removal for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageIds the ids of the packages
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule package removal for several systems.",
        requestClass = SchedulePackagesForSystemsWithModulesRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long[] schedulePackageRemove(User loggedInUser, List<Integer> sids,
        List<Integer> packageIds, Date earliestOccurrence, Boolean allowModules);

    /**
     * Schedules a package removal for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageIds the ids of the packages
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule package removal for a system.",
        requestClass = SchedulePackagesRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int schedulePackageRemove(User loggedInUser, Integer sid, List<Integer> packageIds,
        Date earliestOccurrence);

    /**
     * Schedules a package removal for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageIds the ids of the packages
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule package removal for a system.",
        requestClass = SchedulePackagesWithModulesRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int schedulePackageRemove(User loggedInUser, Integer sid, List<Integer> packageIds,
        Date earliestOccurrence, Boolean allowModules);

    /**
     * Schedules a package removal for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageNevraList the packages to remove
     * @param earliestOccurrence the time to schedule the action
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule package removal for several systems.",
        requestClass = ScheduleNevraForSystemsRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long[] schedulePackageRemoveByNevra(User loggedInUser, List<Integer> sids,
        List<Map<String, String>> packageNevraList, Date earliestOccurrence);

    /**
     * Schedules a package removal for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param packageNevraList the packages to remove
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Schedule package removal for several systems.",
        requestClass = ScheduleNevraForSystemsWithModulesRequest.class,
        responseClass = LongListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long[] schedulePackageRemoveByNevra(User loggedInUser, List<Integer> sids,
        List<Map<String, String>> packageNevraList, Date earliestOccurrence,
        Boolean allowModules);

    /**
     * Schedules a package removal for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageNevraList the packages to remove
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule package removal for a system.",
        requestClass = ScheduleNevraRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int schedulePackageRemoveByNevra(User loggedInUser, Integer sid,
        List<Map<String, String>> packageNevraList, Date earliestOccurrence);

    /**
     * Schedules a package removal for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageNevraList the packages to remove
     * @param earliestOccurrence the time to schedule the action
     * @param allowModules whether to allow the call despite modular content
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule package removal for a system.",
        requestClass = ScheduleNevraWithModulesRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int schedulePackageRemoveByNevra(User loggedInUser, Integer sid,
        List<Map<String, String>> packageNevraList, Date earliestOccurrence,
        Boolean allowModules);

    /**
     * Schedules a package lock change for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param pkgIdsToLock the ids of the packages to lock
     * @param pkgIdsToUnlock the ids of the packages to unlock
     * @param earliestOccurrence the time to schedule the action
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedule package lock for a system.",
        requestClass = SchedulePackageLockChangeRequest.class,
        isIntegerResponse = true
    )
    Long schedulePackageLockChange(User loggedInUser, Integer sid, List<Integer> pkgIdsToLock,
        List<Integer> pkgIdsToUnlock, Date earliestOccurrence);

    /**
     * Lists the notes associated with a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the notes of the system
     */
    @ApiEndpointDoc(
        summary = "Provides a list of notes associated with a system.",
        method = HttpMethod.get,
        responseClass = NoteListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "note details")
    )
    Set<Note> listNotes(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the FQDNs associated with a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the FQDNs of the system
     */
    @ApiEndpointDoc(
        summary = "Provides a list of FQDNs associated with a system.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "fqdn")
    )
    List<String> listFqdns(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the installed packages of a system contained in the given channel.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param channelLabel the label of the channel
     * @return the packages of the system in the channel
     */
    @ApiEndpointDoc(
        summary = "Provides a list of packages installed on a system that are also " +
            "contained in the given channel. The installed package list did not include " +
            "arch information before RHEL 5, so it is arch unaware. RHEL 5 systems do " +
            "upload the arch information, and thus are arch aware.",
        method = HttpMethod.get,
        responseClass = ChannelPackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listPackagesFromChannel(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true)
            String channelLabel);

    /**
     * Schedules a hardware refresh for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule a hardware refresh for a system.",
        requestClass = ScheduleForSystemRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleHardwareRefresh(User loggedInUser, Integer sid, Date earliestOccurrence);

    /**
     * Schedules a package list refresh for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule a package list refresh for a system.",
        requestClass = ScheduleForSystemRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int schedulePackageRefresh(User loggedInUser, Integer sid, Date earliestOccurrence);

    /**
     * Schedules a script to run on several systems.
     *
     * @param loggedInUser the current user
     * @param label the label of the script action
     * @param sids the ids of the servers
     * @param username the user to run the script as
     * @param groupname the group to run the script as
     * @param timeout the timeout of the script
     * @param script the contents of the script
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule a script to run.",
        requestClass = ScheduleScriptRunLabelledForSystemsRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the script run action created. Can be used to fetch " +
            "results with system.getScriptResults",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    Integer scheduleScriptRun(User loggedInUser, String label, List<Integer> sids,
        String username, String groupname, Integer timeout, String script,
        Date earliestOccurrence);

    /**
     * Schedules a script to run on several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param username the user to run the script as
     * @param groupname the group to run the script as
     * @param timeout the timeout of the script
     * @param script the contents of the script
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule a script to run.",
        requestClass = ScheduleScriptRunForSystemsRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the script run action created. Can be used to fetch " +
            "results with system.getScriptResults",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    Integer scheduleScriptRun(User loggedInUser, List<Integer> sids, String username,
        String groupname, Integer timeout, String script, Date earliestOccurrence);

    /**
     * Schedules a script to run on a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param username the user to run the script as
     * @param groupname the group to run the script as
     * @param timeout the timeout of the script
     * @param script the contents of the script
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule a script to run.",
        requestClass = ScheduleScriptRunRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the script run action created. Can be used to fetch " +
            "results with system.getScriptResults",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    Integer scheduleScriptRun(User loggedInUser, Integer sid, String username,
        String groupname, Integer timeout, String script, Date earliestOccurrence);

    /**
     * Schedules a script to run on a system.
     *
     * @param loggedInUser the current user
     * @param label the label of the script action
     * @param sid the id of the server
     * @param username the user to run the script as
     * @param groupname the group to run the script as
     * @param timeout the timeout of the script
     * @param script the contents of the script
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule a script to run.",
        requestClass = ScheduleScriptRunLabelledRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the script run action created. Can be used to fetch " +
            "results with system.getScriptResults",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    Integer scheduleScriptRun(User loggedInUser, String label, Integer sid, String username,
        String groupname, Integer timeout, String script, Date earliestOccurrence);

    /**
     * Schedules a confidential compute attestation action.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule Confidential Compute Attestation Action",
        requestClass = ScheduleCoCoAttestationRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the script run action created. Can be used to fetch " +
            "results with system.getScriptResults",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    Integer scheduleCoCoAttestation(User loggedInUser, Integer sid, Date earliestOccurrence);

    /**
     * Gets the confidential compute attestation configuration of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the attestation configuration
     */
    @ApiEndpointDoc(
        summary = "Return the Confidential Compute Attestation configuration for the " +
            "given system id",
        method = HttpMethod.get,
        responseClass = CoCoAttestationConfigResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "coco_attestation_config")
    )
    ServerCoCoAttestationConfig getCoCoAttestationConfig(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true,
                description = "ID of the server to get the config for.") Integer sid);

    /**
     * Configures confidential compute attestation for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param enabled whether attestation is enabled
     * @param environmentType the environment type of the system
     * @param attestOnBoot whether to attest on boot
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Configure Confidential Compute Attestation for the given system",
        requestClass = SetCoCoAttestationConfigRequest.class,
        isIntegerResponse = true
    )
    Integer setCoCoAttestationConfig(User loggedInUser, Integer sid, Boolean enabled,
        String environmentType, Boolean attestOnBoot);

    /**
     * Fetches the results of a script execution.
     *
     * @param loggedInUser the current user
     * @param actionId the id of the script run action
     * @return the results of the script run
     */
    @ApiEndpointDoc(
        summary = "Fetch results from a script execution. Returns an empty array if no " +
            "results are yet available.",
        requestClass = ActionIdRequest.class,
        responseClass = ScriptResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "script result")
    )
    Object[] getScriptResults(User loggedInUser, Integer actionId);

    /**
     * Gets the details of a script run action.
     *
     * @param loggedInUser the current user
     * @param actionId the id of the script run action
     * @return the details of the script run action
     */
    @ApiEndpointDoc(
        summary = "Returns script details for script run actions",
        method = HttpMethod.get,
        responseClass = ScriptDetailsResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Script details")
    )
    Map<String, Object> getScriptActionDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "actionId", in = ParameterIn.QUERY, required = true,
                description = "ID of the script run action.") Integer actionId);

    /**
     * Schedules a reboot for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule a reboot for a system.",
        requestClass = ScheduleForSystemRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleReboot(User loggedInUser, Integer sid, Date earliestOccurrence);

    /**
     * Gets the details of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the details of the system
     */
    @ApiEndpointDoc(
        summary = "Get system details.",
        method = HttpMethod.get,
        responseClass = ServerDetailsResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "server details")
    )
    Object getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Sets the details of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param details the details to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set server details. All arguments are optional and will only be " +
            "modified if included in the struct.",
        requestClass = SetDetailsRequest.class,
        isIntegerResponse = true
    )
    Integer setDetails(User loggedInUser, Integer sid, Map<String, Object> details);

    /**
     * Sets the lock status of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param lockStatus whether the system is locked
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set server lock status.",
        requestClass = SetLockStatusRequest.class,
        isIntegerResponse = true
    )
    Integer setLockStatus(User loggedInUser, Integer sid, Boolean lockStatus);

    /**
     * Adds entitlements to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param entitlements the entitlements to add
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add entitlements to a server. Entitlements a server already has are " +
            "quietly ignored.",
        requestClass = AddEntitlementsRequest.class,
        isIntegerResponse = true
    )
    int addEntitlements(User loggedInUser, Integer sid, List<String> entitlements);

    /**
     * Removes entitlements from a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param entitlements the entitlements to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove addon entitlements from a server. Entitlements a server does " +
            "not have are quietly ignored.",
        requestClass = RemoveEntitlementsRequest.class,
        isIntegerResponse = true
    )
    int removeEntitlements(User loggedInUser, Integer sid, List<String> entitlements);

    /**
     * Unentitles the system identified by a client certificate.
     *
     * @param clientCert the client certificate of the system
     * @return 1 on success
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Unentitle the system completely",
        requestClass = ClientSystemIdRequest.class,
        isIntegerResponse = true
    )
    int unentitle(String clientCert);

    /**
     * Lists the package profiles of the organization.
     *
     * @param loggedInUser the current user
     * @return the package profiles
     */
    @ApiEndpointDoc(
        summary = "List the package profiles in this organization",
        method = HttpMethod.get,
        responseClass = PackageProfileListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package profile")
    )
    Object[] listPackageProfiles(@Parameter(hidden = true) User loggedInUser);

    /**
     * Deletes a package profile.
     *
     * @param loggedInUser the current user
     * @param profileId the id of the profile
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a package profile",
        requestClass = DeletePackageProfileRequest.class,
        isIntegerResponse = true
    )
    int deletePackageProfile(User loggedInUser, Integer profileId);

    /**
     * Creates a package profile from the installed packages of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param profileLabel the label of the profile
     * @param description the description of the profile
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a new stored Package Profile from a systems installed package list.",
        requestClass = CreatePackageProfileRequest.class,
        isIntegerResponse = true
    )
    int createPackageProfile(User loggedInUser, Integer sid, String profileLabel,
        String description);

    /**
     * Compares the packages of a system against a package profile.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param profileLabel the label of the profile
     * @return the package differences
     */
    @ApiEndpointDoc(
        summary = "Compare a system's packages against a package profile. In the result " +
            "returned, 'this_system' represents the server provided as an input and " +
            "'other_system' represents the profile provided as an input.",
        requestClass = ComparePackageProfileRequest.class,
        responseClass = PackageMetadataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package metadata")
    )
    Object[] comparePackageProfile(User loggedInUser, Integer sid, String profileLabel);

    /**
     * Lists the systems needing package updates.
     *
     * @param loggedInUser the current user
     * @return the systems needing package updates
     */
    @ApiEndpointDoc(
        summary = "Returns list of systems needing package updates.",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listOutOfDateSystems(@Parameter(hidden = true) User loggedInUser);

    /**
     * Synchronises packages from a source system to a target system.
     *
     * @param loggedInUser the current user
     * @param targetServerId the id of the target server
     * @param sourceServerId the id of the source server
     * @param packageIds the ids of the packages to sync
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Sync packages from a source system to a target.",
        requestClass = SyncPackagesWithSystemRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleSyncPackagesWithSystem(User loggedInUser, Integer targetServerId,
        Integer sourceServerId, List<Integer> packageIds, Date earliestOccurrence);

    /**
     * Lists the systems not associated with any system group.
     *
     * @param loggedInUser the current user
     * @return the ungrouped systems
     */
    @ApiEndpointDoc(
        summary = "List systems that are not associated with any system groups.",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemOverview> listUngroupedSystems(@Parameter(hidden = true) User loggedInUser);

    /**
     * Gets the base channel of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the base channel of the system
     */
    @ApiEndpointDoc(
        summary = "Provides the base channel of a given system",
        method = HttpMethod.get,
        responseClass = BaseChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Object getSubscribedBaseChannel(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the systems inactive for the default period.
     *
     * @param loggedInUser the current user
     * @return the inactive systems
     */
    @ApiEndpointDoc(
        summary = "Lists systems that have been inactive for the default period of " +
            "inactivity",
        method = HttpMethod.get,
        responseClass = ShortSystemInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<ShortSystemInfo> listInactiveSystems(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the systems inactive for the given number of days.
     *
     * @param loggedInUser the current user
     * @param days the number of days of inactivity
     * @return the inactive systems
     */
    @ApiEndpointDoc(
        summary = "Lists systems that have been inactive for the specified number of days..",
        method = HttpMethod.get,
        responseClass = ShortSystemInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<ShortSystemInfo> listInactiveSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "days", in = ParameterIn.QUERY, required = true) Integer days);

    /**
     * Gets the user who registered a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the user who registered the system
     */
    @ApiEndpointDoc(
        summary = "Returns information about the user who registered the system",
        requestClass = SidRequest.class,
        responseClass = UserResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "user")
    )
    User whoRegistered(User loggedInUser, Integer sid);

    /**
     * Lists the systems with the given installed package.
     *
     * @param loggedInUser the current user
     * @param pid the id of the package
     * @return the systems with the package installed
     */
    @ApiEndpointDoc(
        summary = "Lists the systems that have the given installed package",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemOverview> listSystemsWithPackage(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "pid", in = ParameterIn.QUERY, required = true,
                description = "the package id") Integer pid);

    /**
     * Lists the systems with the given installed package.
     *
     * @param loggedInUser the current user
     * @param name the name of the package
     * @param version the version of the package
     * @param release the release of the package
     * @return the systems with the package installed
     */
    @ApiEndpointDoc(
        summary = "Lists the systems that have the given installed package",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemOverview> listSystemsWithPackage(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true,
                description = "the package name") String name,
        @Parameter(name = "version", in = ParameterIn.QUERY, required = true,
                description = "the package version") String version,
        @Parameter(name = "release", in = ParameterIn.QUERY, required = true,
                description = "the package release") String release);

    /**
     * Lists the systems with the given entitlement.
     *
     * @param loggedInUser the current user
     * @param entitlementName the name of the entitlement
     * @return the systems with the entitlement
     */
    @ApiEndpointDoc(
        summary = "Lists the systems that have the given entitlement",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemOverview> listSystemsWithEntitlement(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "entitlementName", in = ParameterIn.QUERY, required = true,
                description = "the entitlement name") String entitlementName);

    /**
     * Lists the physical servers visible to the user.
     *
     * @param loggedInUser the current user
     * @return the physical servers
     */
    @ApiEndpointDoc(
        summary = "Returns a list of all Physical servers visible to the user.",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listPhysicalSystems(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the virtual hosts visible to the user.
     *
     * @param loggedInUser the current user
     * @return the virtual hosts
     */
    @ApiEndpointDoc(
        summary = "Lists the virtual hosts visible to the user",
        method = HttpMethod.get,
        responseClass = SystemOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemOverview> listVirtualHosts(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the virtual guests of a virtual host.
     *
     * @param loggedInUser the current user
     * @param sid the id of the virtual host
     * @return the virtual guests of the host
     */
    @ApiEndpointDoc(
        summary = "Lists the virtual guests for a given virtual host",
        method = HttpMethod.get,
        responseClass = VirtualSystemListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "virtual system")
    )
    List<VirtualSystemOverview> listVirtualGuests(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true,
                description = "the virtual host's id") Integer sid);

    /**
     * Lists the activation keys a system was registered with.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the activation keys
     */
    @ApiEndpointDoc(
        summary = "List the activation keys the system was registered with. An empty " +
            "list will be returned if an activation key was not used during registration.",
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "key")
    )
    List<String> listActivationKeys(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the proxies a system connects through.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the connection path of the system
     */
    @ApiEndpointDoc(
        summary = "Get the list of proxies that the given system connects through in " +
            "order to reach the server.",
        method = HttpMethod.get,
        responseClass = ServerPathListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "proxy connection path details")
    )
    Object[] getConnectionPath(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Creates a cobbler system record for a registered system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param ksLabel the kickstart label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Creates a cobbler system record with the specified kickstart label",
        requestClass = CreateSystemRecordRequest.class,
        isIntegerResponse = true
    )
    int createSystemRecord(User loggedInUser, Integer sid, String ksLabel);

    /**
     * Creates a cobbler system record for an unregistered system.
     *
     * @param loggedInUser the current user
     * @param systemName the name of the system
     * @param ksLabel the kickstart label
     * @param kOptions the kernel options
     * @param comment the comment of the record
     * @param netDevices the network devices of the system
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Creates a cobbler system record for a system that is not registered.",
        requestClass = CreateUnregisteredSystemRecordRequest.class,
        isIntegerResponse = true
    )
    int createSystemRecord(User loggedInUser, String systemName, String ksLabel,
        String kOptions, String comment, List<Map<String, String>> netDevices);

    /**
     * Creates a system profile for an unregistered system.
     *
     * @param loggedInUser the current user
     * @param systemName the name of the system
     * @param data the data of the system profile
     * @return the id of the created system
     */
    @ApiEndpointDoc(
        summary = "Creates a system record in database for a system that is not " +
            "registered. Either \"hwAddress\" or \"hostname\" prop must be specified in " +
            "the \"data\" struct. If a system(s) matching given data exists, a " +
            "SystemsExistFaultException is thrown which contains matching system IDs in " +
            "its message.",
        requestClass = CreateSystemProfileRequest.class,
        isIntegerResponse = true,
        responseDescription = "The id of the created system",
        legacyDocResponse = @LegacyDocResponse(name = "systemId")
    )
    int createSystemProfile(User loggedInUser, String systemName, Map<String, Object> data);

    /**
     * Lists the kickstart variables of a system record.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the kickstart variables of the system
     */
    @ApiEndpointDoc(
        summary = "Lists kickstart variables set in the system record for the specified " +
            "server. Note: This call assumes that a system record exists in cobbler for " +
            "the given system and will raise an XMLRPC fault if that is not the case. To " +
            "create a system record over xmlrpc use system.createSystemRecord",
        method = HttpMethod.get,
        responseClass = KickstartVariablesResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "System kickstart variables")
    )
    Map<String, Object> getVariables(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Sets the kickstart variables of a system record.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param netboot whether netboot is enabled
     * @param variables the kickstart variables to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets a list of kickstart variables in the cobbler system record for " +
            "the specified server. Note: This call assumes that a system record exists in " +
            "cobbler for the given system and will raise an XMLRPC fault if that is not " +
            "the case. To create a system record over xmlrpc use system.createSystemRecord",
        requestClass = SetVariablesRequest.class,
        isIntegerResponse = true
    )
    int setVariables(User loggedInUser, Integer sid, Boolean netboot,
        Map<String, Object> variables);

    /**
     * Lists the systems sharing an IP address.
     *
     * @param loggedInUser the current user
     * @return the duplicate groups
     */
    @ApiEndpointDoc(
        summary = "List duplicate systems by IP Address.",
        method = HttpMethod.get,
        responseClass = DuplicateByIpListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Duplicate Group")
    )
    List<Map<String, Object>> listDuplicatesByIp(
        @Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the systems sharing a MAC address.
     *
     * @param loggedInUser the current user
     * @return the duplicate groups
     */
    @ApiEndpointDoc(
        summary = "List duplicate systems by Mac Address.",
        method = HttpMethod.get,
        responseClass = DuplicateByMacListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Duplicate Group")
    )
    List<Map<String, Object>> listDuplicatesByMac(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the systems sharing a hostname.
     *
     * @param loggedInUser the current user
     * @return the duplicate groups
     */
    @ApiEndpointDoc(
        summary = "List duplicate systems by Hostname.",
        method = HttpMethod.get,
        responseClass = DuplicateByHostnameListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Duplicate Group")
    )
    List<Map<String, Object>> listDuplicatesByHostname(
        @Parameter(hidden = true) User loggedInUser);

    /**
     * Gets the system currency score multipliers.
     *
     * @param loggedInUser the current user
     * @return the score multipliers
     */
    @ApiEndpointDoc(
        summary = "Get the System Currency score multipliers",
        method = HttpMethod.get,
        responseClass = CurrencyMultiplierResponse.class,
        responseDescription = "Map of score multipliers",
        legacyDocResponse = @LegacyDocResponse(name = "multipliers", type = "map")
    )
    Map<String, Integer> getSystemCurrencyMultipliers(
        @Parameter(hidden = true) User loggedInUser);

    /**
     * Gets the system currency scores of the accessible servers.
     *
     * @param loggedInUser the current user
     * @return the system currency scores
     */
    @ApiEndpointDoc(
        summary = "Get the System Currency scores for all servers the user has access to",
        method = HttpMethod.get,
        responseClass = CurrencyScoreListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system currency")
    )
    List<Map<String, Long>> getSystemCurrencyScores(
        @Parameter(hidden = true) User loggedInUser);

    /**
     * Gets the UUID of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the UUID of the system
     */
    @ApiEndpointDoc(
        summary = "Get the UUID from the given system ID.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "uuid")
    )
    String getUuid(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Tags the latest snapshot of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param tagName the name of the tag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Tags latest system snapshot",
        requestClass = SnapshotTagRequest.class,
        isIntegerResponse = true
    )
    int tagLatestSnapshot(User loggedInUser, Integer sid, String tagName);

    /**
     * Deletes a tag from a system snapshot.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param tagName the name of the tag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes tag from system snapshot",
        requestClass = SnapshotTagRequest.class,
        isIntegerResponse = true
    )
    int deleteTagFromSnapshot(User loggedInUser, Integer sid, String tagName);

    /**
     * Lists the systems with extra packages.
     *
     * @param loggedInUser the current user
     * @return the systems with extra packages
     */
    @ApiEndpointDoc(
        summary = "List systems with extra packages",
        method = HttpMethod.get,
        responseClass = ExtraPackagesSystemListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listSystemsWithExtraPackages(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the extra packages of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the extra packages of the system
     */
    @ApiEndpointDoc(
        summary = "List extra packages for a system",
        method = HttpMethod.get,
        responseClass = ExtraPackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listExtraPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Sets the primary network interface of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param interfaceName the name of the interface
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets new primary network interface",
        requestClass = SetPrimaryInterfaceRequest.class,
        isIntegerResponse = true
    )
    int setPrimaryInterface(User loggedInUser, Integer sid, String interfaceName);

    /**
     * Sets the primary FQDN of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param fqdn the FQDN to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Sets new primary FQDN",
        requestClass = SetPrimaryFqdnRequest.class,
        isIntegerResponse = true
    )
    int setPrimaryFqdn(User loggedInUser, Integer sid, String fqdn);

    /**
     * Schedules an update of the client certificate.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule update of client certificate",
        requestClass = SidRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int scheduleCertificateUpdate(User loggedInUser, Integer sid);

    /**
     * Schedules an update of the client certificate at a given time.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule update of client certificate at given date and time",
        requestClass = ScheduleForSystemRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int scheduleCertificateUpdate(User loggedInUser, Integer sid, Date earliestOccurrence);

    /**
     * Sends an OSA ping to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "send a ping to a system using OSA",
        requestClass = SidRequest.class,
        isIntegerResponse = true
    )
    int sendOsaPing(User loggedInUser, Integer sid);

    /**
     * Gets the details of an OSA ping sent to a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the details of the ping
     */
    @ApiEndpointDoc(
        summary = "get details about a ping sent to a system using OSA",
        method = HttpMethod.get,
        responseClass = OsaPingResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "osaPing")
    )
    Map<String, Object> getOsaPing(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the possible migration targets of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the migration targets
     */
    @ApiEndpointDoc(
        summary = "List possible migration targets for a system",
        method = HttpMethod.get,
        responseClass = MigrationTargetListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "migrationtarget")
    )
    List<Map<String, Object>> listMigrationTargets(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the possible migration targets of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param excludeTargetWhereMissingSuccessors whether to exclude incomplete targets
     * @return the migration targets
     */
    @ApiEndpointDoc(
        summary = "List possible migration targets for a system, if " +
            "excludeTargetWhereMissingSuccessors is false then valid targets without some " +
            "successors will also be listed.",
        method = HttpMethod.get,
        responseClass = MigrationTargetListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "migrationtarget")
    )
    List<Map<String, Object>> listMigrationTargets(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "excludeTargetWhereMissingSuccessors", in = ParameterIn.QUERY,
                required = true) boolean excludeTargetWhereMissingSuccessors);

    /**
     * Lists the eligible migration targets of a system, including channel details.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the migration targets with their channels
     */
    @ApiEndpointDoc(
        summary = "Lists the eligible migration targets for a given server, including " +
            "channel details.",
        method = HttpMethod.get,
        responseClass = MigrationTargetWithChannelsListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "migration target")
    )
    List<Map<String, Object>> listMigrationTargetsWithChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Schedules a service pack migration for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = SP_MIGRATION_SUMMARY,
        requestClass = MigrationRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleSPMigration(User loggedInUser, Integer sid, String baseChannelLabel,
        List<String> optionalChildChannels, Boolean dryRun, Date earliestOccurrence);

    /**
     * Schedules a service pack migration for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param allowVendorChange whether to allow a vendor change
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = SP_MIGRATION_SUMMARY,
        requestClass = MigrationWithVendorChangeRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleSPMigration(User loggedInUser, Integer sid, String baseChannelLabel,
        List<String> optionalChildChannels, Boolean dryRun, Boolean allowVendorChange,
        Date earliestOccurrence);

    /**
     * Schedules a service pack migration to a given target.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param targetIdent the identifier of the migration target
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = SP_MIGRATION_TYPO_SUMMARY,
        requestClass = MigrationToTargetRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleSPMigration(User loggedInUser, Integer sid, String targetIdent,
        String baseChannelLabel, List<String> optionalChildChannels, Boolean dryRun,
        Date earliestOccurrence);

    /**
     * Schedules a service pack migration to a given target.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param targetIdent the identifier of the migration target
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param allowVendorChange whether to allow a vendor change
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = SP_MIGRATION_SUMMARY,
        requestClass = MigrationToTargetWithVendorChangeRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleSPMigration(User loggedInUser, Integer sid, String targetIdent,
        String baseChannelLabel, List<String> optionalChildChannels, Boolean dryRun,
        Boolean allowVendorChange, Date earliestOccurrence);

    /**
     * Schedules a product migration for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = PRODUCT_MIGRATION_SUMMARY,
        requestClass = MigrationRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleProductMigration(User loggedInUser, Integer sid, String baseChannelLabel,
        List<String> optionalChildChannels, Boolean dryRun, Date earliestOccurrence);

    /**
     * Schedules a product migration for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param allowVendorChange whether to allow a vendor change
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = PRODUCT_MIGRATION_SUMMARY,
        requestClass = MigrationWithVendorChangeRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleProductMigration(User loggedInUser, Integer sid, String baseChannelLabel,
        List<String> optionalChildChannels, Boolean dryRun, Boolean allowVendorChange,
        Date earliestOccurrence);

    /**
     * Schedules a product migration to a given target.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param targetIdent the identifier of the migration target
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = PRODUCT_MIGRATION_TYPO_SUMMARY,
        requestClass = MigrationToTargetRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleProductMigration(User loggedInUser, Integer sid, String targetIdent,
        String baseChannelLabel, List<String> optionalChildChannels, Boolean dryRun,
        Date earliestOccurrence);

    /**
     * Schedules a product migration to a given target.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param targetIdent the identifier of the migration target
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param allowVendorChange whether to allow a vendor change
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = PRODUCT_MIGRATION_SUMMARY,
        requestClass = MigrationToTargetWithVendorChangeRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleProductMigration(User loggedInUser, Integer sid, String targetIdent,
        String baseChannelLabel, List<String> optionalChildChannels, Boolean dryRun,
        Boolean allowVendorChange, Date earliestOccurrence);

    /**
     * Schedules a product migration to a given target.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param targetIdent the identifier of the migration target
     * @param baseChannelLabel the label of the target base channel
     * @param optionalChildChannels the optional child channels
     * @param dryRun whether to perform a dry run
     * @param allowVendorChange whether to allow a vendor change
     * @param removeProductsWithNoSuccessorAfterMigration whether to remove products
     *        without successors
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = PRODUCT_MIGRATION_SUMMARY,
        requestClass = MigrationFullRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleProductMigration(User loggedInUser, Integer sid, String targetIdent,
        String baseChannelLabel, List<String> optionalChildChannels, boolean dryRun,
        boolean allowVendorChange, boolean removeProductsWithNoSuccessorAfterMigration,
        Date earliestOccurrence);

    /**
     * Schedules a distribution upgrade for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param channels the labels of the channels to subscribe to
     * @param dryRun whether to perform a dry run
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = DIST_UPGRADE_SUMMARY,
        requestClass = DistUpgradeRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleDistUpgrade(User loggedInUser, Integer sid, List<String> channels,
        boolean dryRun, Date earliestOccurrence);

    /**
     * Schedules a distribution upgrade for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param channels the labels of the channels to subscribe to
     * @param dryRun whether to perform a dry run
     * @param allowVendorChange whether to allow a vendor change
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = DIST_UPGRADE_SUMMARY,
        requestClass = DistUpgradeWithVendorChangeRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleDistUpgrade(User loggedInUser, Integer sid, List<String> channels,
        boolean dryRun, boolean allowVendorChange, Date earliestOccurrence);

    /**
     * Lists the systems that require a reboot.
     *
     * @param loggedInUser the current user
     * @return the systems requiring a reboot
     */
    @ApiEndpointDoc(
        summary = "List systems that require reboot.",
        method = HttpMethod.get,
        responseClass = SuggestedRebootListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listSuggestedReboot(@Parameter(hidden = true) User loggedInUser);

    /**
     * Gets the installed products of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the installed products
     */
    @ApiEndpointDoc(
        summary = "Get a list of installed products for given system",
        method = HttpMethod.get,
        responseClass = InstalledProductListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "installed product")
    )
    List<SUSEInstalledProduct> getInstalledProducts(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the active kernel live patching version of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the kernel live patching version
     */
    @ApiEndpointDoc(
        summary = "Returns the currently active kernel live patching version relative to " +
            "the running kernel version of the system, or empty string if live patching " +
            "feature is not in use for the given system.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        legacyDocResponse = @LegacyDocResponse(plainText = true, type = "string")
    )
    String getKernelLivePatch(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Bootstraps a system for management via Salt or Salt SSH.
     *
     * @param user the current user
     * @param host the hostname or IP address of the target
     * @param sshPort the SSH port of the target
     * @param sshUser the SSH user on the target
     * @param sshPassword the SSH password of the given user
     * @param activationKey the activation key
     * @param saltSSH whether to manage the system with Salt SSH
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Bootstrap a system for management via either Salt or Salt SSH.",
        requestClass = BootstrapRequest.class,
        isIntegerResponse = true
    )
    int bootstrap(User user, String host, Integer sshPort, String sshUser,
        String sshPassword, String activationKey, Boolean saltSSH);

    /**
     * Bootstraps a system using an SSH private key.
     *
     * @param user the current user
     * @param host the hostname or IP address of the target
     * @param sshPort the SSH port of the target
     * @param sshUser the SSH user on the target
     * @param sshPrivKey the SSH private key
     * @param sshPrivKeyPass the passphrase of the key
     * @param activationKey the activation key
     * @param saltSSH whether to manage the system with Salt SSH
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Bootstrap a system for management via either Salt or Salt SSH. Use " +
            "SSH private key for authentication.",
        requestClass = BootstrapWithKeyRequest.class,
        isIntegerResponse = true
    )
    int bootstrapWithPrivateSshKey(User user, String host, Integer sshPort, String sshUser,
        String sshPrivKey, String sshPrivKeyPass, String activationKey, Boolean saltSSH);

    /**
     * Bootstraps a system through a proxy.
     *
     * @param user the current user
     * @param host the hostname or IP address of the target
     * @param sshPort the SSH port of the target
     * @param sshUser the SSH user on the target
     * @param sshPassword the SSH password of the given user
     * @param activationKey the activation key
     * @param proxyId the id of the proxy
     * @param saltSSH whether to manage the system with Salt SSH
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Bootstrap a system for management via either Salt or Salt SSH.",
        requestClass = BootstrapViaProxyRequest.class,
        isIntegerResponse = true
    )
    int bootstrap(User user, String host, Integer sshPort, String sshUser,
        String sshPassword, String activationKey, Integer proxyId, Boolean saltSSH);

    /**
     * Bootstraps a system through a proxy using an SSH private key.
     *
     * @param user the current user
     * @param host the hostname or IP address of the target
     * @param sshPort the SSH port of the target
     * @param sshUser the SSH user on the target
     * @param sshPrivKey the SSH private key
     * @param sshPrivKeyPass the passphrase of the key
     * @param activationKey the activation key
     * @param proxyId the id of the proxy
     * @param saltSSH whether to manage the system with Salt SSH
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Bootstrap a system for management via either Salt or Salt SSH. Use " +
            "SSH private key for authentication.",
        requestClass = BootstrapWithKeyViaProxyRequest.class,
        isIntegerResponse = true
    )
    int bootstrapWithPrivateSshKey(User user, String host, Integer sshPort, String sshUser,
        String sshPrivKey, String sshPrivKeyPass, String activationKey, Integer proxyId,
        Boolean saltSSH);

    /**
     * Bootstraps a system with a reactivation key.
     *
     * @param user the current user
     * @param host the hostname or IP address of the target
     * @param sshPort the SSH port of the target
     * @param sshUser the SSH user on the target
     * @param sshPassword the SSH password of the given user
     * @param activationKey the activation key
     * @param reactivationKey the reactivation key
     * @param saltSSH whether to manage the system with Salt SSH
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Bootstrap a system for management via either Salt or Salt SSH.",
        requestClass = BootstrapWithReactivationRequest.class,
        isIntegerResponse = true
    )
    int bootstrap(User user, String host, Integer sshPort, String sshUser,
        String sshPassword, String activationKey, String reactivationKey, Boolean saltSSH);

    /**
     * Bootstraps a system with a reactivation key using an SSH private key.
     *
     * @param user the current user
     * @param host the hostname or IP address of the target
     * @param sshPort the SSH port of the target
     * @param sshUser the SSH user on the target
     * @param sshPrivKey the SSH private key
     * @param sshPrivKeyPass the passphrase of the key
     * @param activationKey the activation key
     * @param reactivationKey the reactivation key
     * @param saltSSH whether to manage the system with Salt SSH
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Bootstrap a system for management via either Salt or Salt SSH. Use " +
            "SSH private key for authentication.",
        requestClass = BootstrapWithKeyAndReactivationRequest.class,
        isIntegerResponse = true
    )
    int bootstrapWithPrivateSshKey(User user, String host, Integer sshPort, String sshUser,
        String sshPrivKey, String sshPrivKeyPass, String activationKey,
        String reactivationKey, Boolean saltSSH);

    /**
     * Bootstraps a system with a reactivation key through a proxy.
     *
     * @param user the current user
     * @param host the hostname or IP address of the target
     * @param sshPort the SSH port of the target
     * @param sshUser the SSH user on the target
     * @param sshPassword the SSH password of the given user
     * @param activationKey the activation key
     * @param reactivationKey the reactivation key
     * @param proxyId the id of the proxy
     * @param saltSSH whether to manage the system with Salt SSH
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Bootstrap a system for management via either Salt or Salt SSH.",
        requestClass = BootstrapWithReactivationViaProxyRequest.class,
        isIntegerResponse = true
    )
    int bootstrap(User user, String host, Integer sshPort, String sshUser,
        String sshPassword, String activationKey, String reactivationKey, Integer proxyId,
        Boolean saltSSH);

    /**
     * Bootstraps a system with a reactivation key through a proxy using an SSH private key.
     *
     * @param user the current user
     * @param host the hostname or IP address of the target
     * @param sshPort the SSH port of the target
     * @param sshUser the SSH user on the target
     * @param sshPrivKey the SSH private key
     * @param sshPrivKeyPass the passphrase of the key
     * @param activationKey the activation key
     * @param reactivationKey the reactivation key
     * @param proxyId the id of the proxy
     * @param saltSSH whether to manage the system with Salt SSH
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Bootstrap a system for management via either Salt or Salt SSH. Use " +
            "SSH private key for authentication.",
        requestClass = BootstrapWithKeyAndReactivationViaProxyRequest.class,
        isIntegerResponse = true
    )
    int bootstrapWithPrivateSshKey(User user, String host, Integer sshPort, String sshUser,
        String sshPrivKey, String sshPrivKeyPass, String activationKey,
        String reactivationKey, Integer proxyId, Boolean saltSSH);

    /**
     * Schedules a highstate application for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliestOccurrence the time to schedule the action
     * @param test whether to run the states in test-only mode
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule highstate application for a given system.",
        requestClass = ApplyHighstateRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleApplyHighstate(User loggedInUser, Integer sid, Date earliestOccurrence,
        Boolean test);

    /**
     * Schedules a highstate application for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param earliestOccurrence the time to schedule the action
     * @param test whether to run the states in test-only mode
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule highstate application for a given system.",
        requestClass = ApplyHighstateForSystemsRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleApplyHighstate(User loggedInUser, List<Integer> sids,
        Date earliestOccurrence, Boolean test);

    /**
     * Schedules a state application for a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param stateNames the names of the states to apply
     * @param earliestOccurrence the time to schedule the action
     * @param test whether to run the states in test-only mode
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule highstate application for a given system.",
        requestClass = ApplyStatesRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleApplyStates(User loggedInUser, Integer sid, List<String> stateNames,
        Date earliestOccurrence, Boolean test);

    /**
     * Schedules a state application for several systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param stateNames the names of the states to apply
     * @param earliestOccurrence the time to schedule the action
     * @param test whether to run the states in test-only mode
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule highstate application for a given system.",
        requestClass = ApplyStatesForSystemsRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    Long scheduleApplyStates(User loggedInUser, List<Integer> sids, List<String> stateNames,
        Date earliestOccurrence, Boolean test);

    /**
     * Updates the package state of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param packageName the name of the package
     * @param state the desired package state
     * @param versionConstraint the version constraint
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Update the package state of a given system (High state would be " +
            "needed to actually install/remove the package)",
        requestClass = UpdatePackageStateRequest.class,
        responseClass = StringResponse.class,
        legacyDocResponse = @LegacyDocResponse(plainText = true,
            type = "1 on success, exception on failure")
    )
    int updatePackageState(User loggedInUser, Integer sid, String packageName, Integer state,
        Integer versionConstraint);

    /**
     * Gets the map of Salt minion ids to system ids.
     *
     * @param loggedInUser the current user
     * @return the minion id map
     */
    @ApiEndpointDoc(
        summary = "Return a map from Salt minion IDs to System IDs. Map entries are " +
            "limited to systems that are visible by the current user.",
        method = HttpMethod.get,
        responseClass = MinionIdMapResponse.class,
        responseDescription = "minion IDs to system IDs",
        legacyDocResponse = @LegacyDocResponse(name = "id_map", type = "map")
    )
    Map<String, Long> getMinionIdMap(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the package states of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the package states of the system
     */
    @ApiEndpointDoc(
        summary = "List possible migration targets for a system",
        method = HttpMethod.get,
        responseClass = PackageStateListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package state")
    )
    Set<PackageState> listPackageState(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the group membership of the systems with the given entitlement.
     *
     * @param loggedInUser the current user
     * @param entitlement the entitlement to filter on
     * @return the group membership of the systems
     */
    @ApiEndpointDoc(
        summary = "Returns the groups information a system is member of, for all the " +
            "systems visible to the passed user and that are entitled with the passed " +
            "entitlement.",
        method = HttpMethod.get,
        responseClass = SystemGroupsListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemGroupsDTO> listSystemGroupsForSystemsWithEntitlement(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "entitlement", in = ParameterIn.QUERY, required = true)
            String entitlement);

    /**
     * Gets the pillar data of a system.
     *
     * @param loggedInUser the current user
     * @param systemId the id of the system
     * @param category the pillar category
     * @return the pillar data
     */
    @ApiEndpointDoc(
        summary = "Get pillar data of given category for given system",
        method = HttpMethod.get,
        responseClass = PillarResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "the pillar data", type = "struct")
    )
    Map<String, Object> getPillar(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemId", in = ParameterIn.QUERY, required = true)
            Integer systemId,
        @Parameter(name = "category", in = ParameterIn.QUERY, required = true)
            String category);

    /**
     * Gets the pillar data of a minion.
     *
     * @param loggedInUser the current user
     * @param minionId the id of the minion
     * @param category the pillar category
     * @return the pillar data
     */
    @ApiEndpointDoc(
        summary = "Get pillar data of given category for given system",
        method = HttpMethod.get,
        responseClass = PillarResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "the pillar data", type = "struct")
    )
    Map<String, Object> getPillar(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "minionId", in = ParameterIn.QUERY, required = true)
            String minionId,
        @Parameter(name = "category", in = ParameterIn.QUERY, required = true)
            String category);

    /**
     * Sets the pillar data of a system.
     *
     * @param loggedInUser the current user
     * @param systemId the id of the system
     * @param category the pillar category
     * @param pillarData the pillar data to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set pillar data of a system.",
        requestClass = SetPillarBySystemIdRequest.class,
        isIntegerResponse = true
    )
    int setPillar(User loggedInUser, Integer systemId, String category,
        Map<String, Object> pillarData);

    /**
     * Sets the pillar data of a minion.
     *
     * @param loggedInUser the current user
     * @param minionId the id of the minion
     * @param category the pillar category
     * @param pillarData the pillar data to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set pillar data of a system.",
        requestClass = SetPillarByMinionIdRequest.class,
        isIntegerResponse = true
    )
    int setPillar(User loggedInUser, String minionId, String category,
        Map<String, Object> pillarData);

    /**
     * Refreshes all the pillar data of the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @return the ids of the systems that could not be refreshed
     */
    @ApiEndpointDoc(
        summary = "refresh all the pillar data of a list of systems.",
        requestClass = RefreshPillarRequest.class,
        responseClass = IntegerListResponse.class,
        responseDescription = "System IDs which couldn't be refreshed",
        legacyDocResponse = @LegacyDocResponse(name = "skippedIds")
    )
    List<Integer> refreshPillar(User loggedInUser, List<Integer> sids);

    /**
     * Refreshes a subset of the pillar data of the given systems.
     *
     * @param loggedInUser the current user
     * @param subset the subset of the pillar to refresh
     * @param sids the ids of the servers
     * @return the ids of the systems that could not be refreshed
     */
    @ApiEndpointDoc(
        summary = "refresh the pillar data of a list of systems. The subset value " +
            "represents the pillar to be refreshed and can be one of 'general', " +
            "'group_membership', 'virtualization' or 'custom_info'.",
        requestClass = RefreshPillarSubsetRequest.class,
        responseClass = IntegerListResponse.class,
        responseDescription = "System IDs which couldn't be refreshed",
        legacyDocResponse = @LegacyDocResponse(name = "skippedIds")
    )
    List<Integer> refreshPillar(User loggedInUser, String subset, List<Integer> sids);

    /**
     * Connects the given systems to another proxy.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the servers
     * @param proxyId the id of the proxy
     * @return the ids of the scheduled actions
     */
    @ApiEndpointDoc(
        summary = "Connect given systems to another proxy.",
        requestClass = ChangeProxyRequest.class,
        responseClass = LongListResponse.class,
        responseDescription = "list of scheduled action ids",
        legacyDocResponse = @LegacyDocResponse(name = "actionIds")
    )
    List<Long> changeProxy(User loggedInUser, List<Integer> sids, Integer proxyId);

    /**
     * Lists the attestation reports of a system after a date.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliest the earliest date to report
     * @return the attestation reports
     */
    @ApiEndpointDoc(
        summary = "Return a list of reports with its results for the given filters",
        method = HttpMethod.get,
        responseClass = CoCoAttestationReportListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "reportResults")
    )
    List<ServerCoCoAttestationReport> listCoCoAttestationReports(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "earliest", in = ParameterIn.QUERY, required = true) Date earliest);

    /**
     * Lists the attestation reports of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param offset the number of reports to skip
     * @param limit the maximum number of reports
     * @return the attestation reports
     */
    @ApiEndpointDoc(
        summary = "Return a list of reports with its results for the given filters",
        method = HttpMethod.get,
        responseClass = CoCoAttestationReportListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "reportResults")
    )
    List<ServerCoCoAttestationReport> listCoCoAttestationReports(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "offset", in = ParameterIn.QUERY, required = true,
                description = "Number of reports to skip") Integer offset,
        @Parameter(name = "limit", in = ParameterIn.QUERY, required = true,
                description = "Maximum number of reports") Integer limit);

    /**
     * Lists the attestation reports of a system after a date.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param earliest the earliest date to report
     * @param offset the number of reports to skip
     * @param limit the maximum number of reports
     * @return the attestation reports
     */
    @ApiEndpointDoc(
        summary = "Return a list of reports with its results for the given filters",
        method = HttpMethod.get,
        responseClass = CoCoAttestationReportListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "reportResults")
    )
    List<ServerCoCoAttestationReport> listCoCoAttestationReports(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "earliest", in = ParameterIn.QUERY, required = true) Date earliest,
        @Parameter(name = "offset", in = ParameterIn.QUERY, required = true,
                description = "Number of reports to skip") Integer offset,
        @Parameter(name = "limit", in = ParameterIn.QUERY, required = true,
                description = "Maximum number of reports") Integer limit);

    /**
     * Gets the latest attestation report of a system.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @return the latest attestation report
     */
    @ApiEndpointDoc(
        summary = "Return the latest report for the given system",
        method = HttpMethod.get,
        responseClass = CoCoAttestationReportResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "reportResults")
    )
    ServerCoCoAttestationReport getLatestCoCoAttestationReport(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Gets the details of an attestation result.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param resultId the id of the result
     * @return the details of the result
     */
    @ApiEndpointDoc(
        summary = "Return a specific results with all details",
        method = HttpMethod.get,
        responseClass = CoCoAttestationResultResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "result")
    )
    CoCoAttestationResult getCoCoAttestationResultDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid,
        @Parameter(name = "resultId", in = ParameterIn.QUERY, required = true)
            Integer resultId);

    /**
     * Schedules an upload of support data to SCC.
     *
     * @param loggedInUser the current user
     * @param sid the id of the server
     * @param caseNumber the SCC case number
     * @param parameter the additional parameter of the collection tool
     * @param uploadGeo the location of the upload server
     * @param earliestOccurrence the time to schedule the action
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Schedule an action to get and upload support data from the specified " +
            "system to SCC.",
        requestClass = SupportDataUploadRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the action scheduled, otherwise exception thrown on error",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    Integer scheduleSupportDataUpload(User loggedInUser, Integer sid, String caseNumber,
        String parameter, String uploadGeo, Date earliestOccurrence);

    @Schema(name = "ApiResponseString")
    interface StringResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseLongList")
    interface LongListResponse extends ApiResponseWrapper<List<Long>> { }

    @Schema(name = "ApiResponseSubscribableBaseChannelList")
    interface SubscribableBaseChannelListResponse
        extends ApiResponseWrapper<List<SubscribableBaseChannelDoc>> { }

    @Schema(name = "ApiResponseShortSystemInfoList")
    interface ShortSystemInfoListResponse
        extends ApiResponseWrapper<List<ProxyHandlerApi.ShortSystemInfoDoc>> { }

    @Schema(name = "ApiResponseEmptySystemProfileList")
    interface EmptySystemProfileListResponse
        extends ApiResponseWrapper<List<EmptySystemProfileDoc>> { }

    @Schema(name = "SystemSidRequest")
    interface SidRequest {

        /**
         * @return the id of the server
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();
    }

    @Schema(name = "SystemClientCertRequest")
    interface ClientCertRequest {

        /**
         * @return the client certificate of the system
         */
        @Schema(description = "client certificate of the system",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getClientCert();
    }

    @Schema(name = "SystemUpgradeEntitlementRequest")
    @JsonPropertyOrder({"sid", "entitlementLevel"})
    interface UpgradeEntitlementRequest extends SidRequest {

        /**
         * @return the entitlement to add
         */
        @Schema(description = "One of: 'enterprise_entitled' or 'virtualization_host'.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getEntitlementLevel();
    }

    @Schema(name = "SystemSetChildChannelsRequest")
    @JsonPropertyOrder({"sid", "channelIdsOrLabels"})
    interface SetChildChannelsRequest extends SidRequest {

        /**
         * @return the child channels to subscribe the server to
         */
        @Schema(description = "channelId (deprecated) or channelLabel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannelIdsOrLabels();
    }

    @Schema(name = "SystemSetBaseChannelRequest")
    @JsonPropertyOrder({"sid", "channelLabel"})
    interface SetBaseChannelRequest extends SidRequest {

        /**
         * @return the label of the new base channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "SystemScheduleChangeChannelsRequest")
    @JsonPropertyOrder({"sid", "baseChannelLabel", "childLabels", "earliestOccurrence"})
    interface ScheduleChangeChannelsRequest extends SidRequest {

        /**
         * @return the label of the base channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseChannelLabel();

        /**
         * @return the labels of the child channels
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChildLabels();

        /**
         * @return the time to schedule the action
         */
        @Schema(description = "the time/date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemScheduleChangeChannelsForSystemsRequest")
    @JsonPropertyOrder({"sids", "baseChannelLabel", "childLabels", "earliestOccurrence"})
    interface ScheduleChangeChannelsForSystemsRequest {

        /**
         * @return the ids of the servers
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the label of the base channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseChannelLabel();

        /**
         * @return the labels of the child channels
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChildLabels();

        /**
         * @return the time to schedule the action
         */
        @Schema(description = "the time/date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemSubscribableBaseChannelDoc")
    @JsonPropertyOrder({"id", "name", "label", "currentBase"})
    interface SubscribableBaseChannelDoc {

        /**
         * @return the id of the base channel
         */
        @Schema(description = "Base Channel ID.", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the channel
         */
        @Schema(description = "Name of channel.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the label of the channel
         */
        @Schema(description = "Label of Channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return whether this is the current base channel
         */
        @Schema(name = "current_base", description = "1 indicates it is the current base channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getCurrentBase();
    }

    @Schema(name = "SystemEmptySystemProfileDoc")
    @JsonPropertyOrder({"id", "name", "created", "hwAddress"})
    interface EmptySystemProfileDoc {

        /**
         * @return the id of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the creation time of the server
         */
        @Schema(description = "Server creation time", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreated();

        /**
         * @return the hardware addresses of the system
         */
        @Schema(name = "hw_address", description = "HW address",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getHwAddress();
    }

    @Schema(name = "ApiResponseStringList")
    interface StringListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseActiveSystemDetailsList")
    interface ActiveSystemDetailsListResponse
        extends ApiResponseWrapper<List<ActiveSystemDetailsDoc>> { }

    @Schema(name = "ApiResponseSubscribableChildChannelList")
    interface SubscribableChildChannelListResponse
        extends ApiResponseWrapper<List<SubscribableChildChannelDoc>> { }

    @Schema(name = "ApiResponseNvrePackageList")
    interface NvrePackageListResponse extends ApiResponseWrapper<List<NvrePackageDoc>> { }

    @Schema(name = "ApiResponseUpgradablePackageList")
    interface UpgradablePackageListResponse
        extends ApiResponseWrapper<List<UpgradablePackageDoc>> { }

    @Schema(name = "ApiResponseInstallablePackageList")
    interface InstallablePackageListResponse
        extends ApiResponseWrapper<List<InstallablePackageDoc>> { }

    @Schema(name = "ApiResponseLatestAvailablePackageList")
    interface LatestAvailablePackageListResponse
        extends ApiResponseWrapper<List<LatestAvailablePackageDoc>> { }

    @Schema(name = "ApiResponseInstalledPackageList")
    interface InstalledPackageListResponse
        extends ApiResponseWrapper<List<InstalledPackageDoc>> { }

    @Schema(name = "ApiResponseInstalledPackageDetailsList")
    interface InstalledPackageDetailsListResponse
        extends ApiResponseWrapper<List<InstalledPackageDetailsDoc>> { }

    @Schema(name = "SystemActiveSystemDetailsDoc")
    @JsonPropertyOrder({"id", "name", "payg", "minionId", "lastCheckin", "ram", "swap",
        "networkDevices", "dmiInfo", "cpuInfo", "subscribedChannels", "activeGuestSystemIds"})
    interface ActiveSystemDetailsDoc {

        /**
         * @return the id of the server
         */
        @Schema(description = "The server's id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the server
         */
        @Schema(description = "The server's name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return whether the server instance is pay-as-you-go
         */
        @Schema(description = "Whether the server instance is payg or not",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPayg();

        /**
         * @return the minion id of the server
         */
        @Schema(name = "minion_id",
                description = "The server's minion id, in case it is a salt minion client",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMinionId();

        /**
         * @return the last time the server successfully checked in
         */
        @Schema(name = "last_checkin",
                description = "Last time server successfully checked in (in UTC)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastCheckin();

        /**
         * @return the amount of physical memory in MB
         */
        @Schema(description = "The amount of physical memory in MB.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRam();

        /**
         * @return the amount of swap space in MB
         */
        @Schema(description = "The amount of swap space in MB.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSwap();

        /**
         * @return the network devices of the server
         */
        @Schema(name = "network_devices", description = "The server's network devices",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "network device")
        NetworkDeviceDoc getNetworkDevices();

        /**
         * @return the DMI information of the server
         */
        @Schema(name = "dmi_info", description = "The server's dmi info",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "DMI")
        DmiDoc getDmiInfo();

        /**
         * @return the CPU information of the server
         */
        @Schema(name = "cpu_info", description = "The server's cpu info",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "CPU")
        CpuDoc getCpuInfo();

        /**
         * @return the channels the server is subscribed to
         */
        @Schema(name = "subscribed_channels", description = "List of subscribed channels",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "channel")
        List<SubscribedChannelDoc> getSubscribedChannels();

        /**
         * @return the system ids of the active virtual guests
         */
        @Schema(name = "active_guest_system_ids",
                description = "List of virtual guest system ids for active guests",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "guest_id")
        List<Integer> getActiveGuestSystemIds();
    }

    @Schema(name = "SystemSubscribedChannelDoc")
    @JsonPropertyOrder({"channelId", "channelLabel"})
    interface SubscribedChannelDoc {

        /**
         * @return the id of the channel
         */
        @Schema(name = "channel_id", description = "The channel id.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getChannelId();

        /**
         * @return the label of the channel
         */
        @Schema(name = "channel_label", description = "The channel label.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "SystemNetworkDeviceDoc")
    @JsonPropertyOrder({"ip", "interfaceName", "netmask", "hardwareAddress", "module", "broadcast",
        "ipv6", "ipv4"})
    interface NetworkDeviceDoc {

        /**
         * @return the IP address assigned to this network device
         */
        @Schema(description = "IP address assigned to this network device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getIp();

        /**
         * @return the network interface assigned to the device
         */
        @Schema(name = "interface",
                description = "network interface assigned to device, e.g. eth0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getInterfaceName();

        /**
         * @return the network mask assigned to the device
         */
        @Schema(description = "network mask assigned to device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getNetmask();

        /**
         * @return the hardware address of the device
         */
        @Schema(name = "hardware_address", description = "hardware address of device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHardwareAddress();

        /**
         * @return the network driver used for this device
         */
        @Schema(description = "network driver used for this device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getModule();

        /**
         * @return the broadcast address of the device
         */
        @Schema(description = "broadcast address for device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBroadcast();

        /**
         * @return the IPv6 addresses of the device
         */
        @Schema(description = "the list of IPv6 addresses",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "ipv6 address")
        List<Ipv6AddressDoc> getIpv6();

        /**
         * @return the IPv4 addresses of the device
         */
        @Schema(description = "the list of IPv4 addresses",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "ipv4 address")
        List<Ipv4AddressDoc> getIpv4();
    }

    @Schema(name = "SystemIpv6AddressDoc")
    @JsonPropertyOrder({"address", "netmask", "scope"})
    interface Ipv6AddressDoc {

        /**
         * @return the IPv6 address of this network device
         */
        @Schema(description = "IPv6 address of this network device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAddress();

        /**
         * @return the IPv6 netmask of this network device
         */
        @Schema(description = "IPv6 netmask of this network device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getNetmask();

        /**
         * @return the scope of the IPv6 address
         */
        @Schema(description = "IPv6 address scope", requiredMode = Schema.RequiredMode.REQUIRED)
        String getScope();
    }

    @Schema(name = "SystemIpv4AddressDoc")
    @JsonPropertyOrder({"address", "netmask", "broadcast"})
    interface Ipv4AddressDoc {

        /**
         * @return the IPv4 address of this network device
         */
        @Schema(description = "IPv4 address of this network device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAddress();

        /**
         * @return the IPv4 netmask of this network device
         */
        @Schema(description = "IPv4 netmask of this network device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getNetmask();

        /**
         * @return the IPv4 broadcast address of this network device
         */
        @Schema(description = "IPv4 broadcast address of this network device",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBroadcast();
    }

    @Schema(name = "SystemDmiDoc")
    @JsonPropertyOrder({"vendor", "system", "product", "asset", "board", "biosRelease",
        "biosVendor", "biosVersion"})
    interface DmiDoc {

        /**
         * @return the DMI vendor
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVendor();

        /**
         * @return the DMI system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystem();

        /**
         * @return the DMI product
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getProduct();

        /**
         * @return the DMI asset
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAsset();

        /**
         * @return the DMI board
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getBoard();

        /**
         * @return the BIOS release
         */
        @Schema(name = "bios_release", description = "(optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBiosRelease();

        /**
         * @return the BIOS vendor
         */
        @Schema(name = "bios_vendor", description = "(optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBiosVendor();

        /**
         * @return the BIOS version
         */
        @Schema(name = "bios_version", description = "(optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBiosVersion();
    }

    @Schema(name = "SystemCpuDoc")
    @JsonPropertyOrder({"cache", "family", "mhz", "flags", "model", "vendor", "arch",
        "stepping", "count", "socketCount", "coreCount", "threadCount"})
    interface CpuDoc {

        /**
         * @return the CPU cache
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCache();

        /**
         * @return the CPU family
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFamily();

        /**
         * @return the CPU frequency
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getMhz();

        /**
         * @return the CPU flags
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFlags();

        /**
         * @return the CPU model
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getModel();

        /**
         * @return the CPU vendor
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVendor();

        /**
         * @return the CPU architecture
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the CPU stepping
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStepping();

        /**
         * @return the CPU count
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCount();

        /**
         * @return the number of sockets
         */
        @Schema(name = "socket_count", description = "if available",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSocketCount();

        /**
         * @return the number of cores per socket
         */
        @Schema(name = "core_count", description = "if available, number of cores per socket",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getCoreCount();

        /**
         * @return the number of threads per core
         */
        @Schema(name = "thread_count", description = "if available, number of threads per core",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getThreadCount();
    }

    @Schema(name = "SystemSubscribableChildChannelDoc")
    @JsonPropertyOrder({"id", "name", "label", "summary", "hasLicense", "gpgKeyUrl"})
    interface SubscribableChildChannelDoc {

        /**
         * @return the id of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the label of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the summary of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the license of the channel
         */
        @Schema(name = "has_license", requiredMode = Schema.RequiredMode.REQUIRED)
        String getHasLicense();

        /**
         * @return the GPG key url of the channel
         */
        @Schema(name = "gpg_key_url", requiredMode = Schema.RequiredMode.REQUIRED)
        String getGpgKeyUrl();
    }

    @Schema(name = "SystemNvrePackageDoc")
    @JsonPropertyOrder({"name", "version", "release", "epoch"})
    interface NvrePackageDoc {

        /**
         * @return the name of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the version of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the release of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the epoch of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();
    }

    @Schema(name = "SystemUpgradablePackageDoc")
    @JsonPropertyOrder({"name", "arch", "fromVersion", "fromRelease", "fromEpoch", "toVersion",
        "toRelease", "toEpoch", "toPackageId"})
    interface UpgradablePackageDoc {

        /**
         * @return the name of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the architecture of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the installed version
         */
        @Schema(name = "from_version", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFromVersion();

        /**
         * @return the installed release
         */
        @Schema(name = "from_release", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFromRelease();

        /**
         * @return the installed epoch
         */
        @Schema(name = "from_epoch", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFromEpoch();

        /**
         * @return the upgradable version
         */
        @Schema(name = "to_version", requiredMode = Schema.RequiredMode.REQUIRED)
        String getToVersion();

        /**
         * @return the upgradable release
         */
        @Schema(name = "to_release", requiredMode = Schema.RequiredMode.REQUIRED)
        String getToRelease();

        /**
         * @return the upgradable epoch
         */
        @Schema(name = "to_epoch", requiredMode = Schema.RequiredMode.REQUIRED)
        String getToEpoch();

        /**
         * @return the id of the upgradable package
         */
        @Schema(name = "to_package_id", requiredMode = Schema.RequiredMode.REQUIRED)
        String getToPackageId();
    }

    @Schema(name = "SystemInstallablePackageDoc")
    @JsonPropertyOrder({"name", "version", "release", "epoch", "id", "archLabel"})
    interface InstallablePackageDoc extends NvrePackageDoc {

        /**
         * @return the id of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the architecture label of the package
         */
        @Schema(name = "arch_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchLabel();
    }

    @Schema(name = "SystemLatestAvailablePackageDoc")
    @JsonPropertyOrder({"id", "name", "packageInfo"})
    interface LatestAvailablePackageDoc {

        /**
         * @return the id of the server
         */
        @Schema(description = "server ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the server
         */
        @Schema(description = "server name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the latest available package
         */
        @Schema(name = "package", description = "package structure",
                requiredMode = Schema.RequiredMode.REQUIRED)
        AvailablePackageDoc getPackageInfo();
    }

    @Schema(name = "SystemAvailablePackageDoc")
    @LegacyDocResponse(name = "package")
    @JsonPropertyOrder({"id", "name", "version", "release", "epoch", "arch"})
    interface AvailablePackageDoc extends NvrePackageDoc {

        /**
         * @return the id of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the architecture of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();
    }

    @Schema(name = "SystemInstalledPackageDoc")
    @JsonPropertyOrder({"name", "version", "release", "epoch", "arch", "installtime"})
    interface InstalledPackageDoc extends NvrePackageDoc {

        /**
         * @return the architecture name of the package
         */
        @Schema(description = "Architecture name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the installation time of the package
         */
        @Schema(description = "returned only if known", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "date")
        Date getInstalltime();
    }

    @Schema(name = "SystemInstalledPackageDetailsDoc")
    @JsonPropertyOrder({"packageId", "name", "epoch", "version", "release", "arch",
        "installtime", "retracted"})
    interface InstalledPackageDetailsDoc {

        /**
         * @return the id of the package
         */
        @Schema(name = "package_id",
                description = "PackageID, -1 if package is installed but not available in " +
                    "subscribed channels",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPackageId();

        /**
         * @return the name of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the epoch of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

        /**
         * @return the version of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the release of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the architecture label of the package
         */
        @Schema(description = "architecture label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the installation time of the package
         */
        @Schema(description = "returned only if known", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "date")
        Date getInstalltime();

        /**
         * @return whether the package is retracted
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRetracted();
    }

    @Schema(name = "ApiResponsePackageLockStatusList")
    interface PackageLockStatusListResponse
        extends ApiResponseWrapper<List<PackageLockStatusDoc>> { }

    @Schema(name = "ApiResponseNetworkInfo")
    interface NetworkInfoResponse extends ApiResponseWrapper<NetworkInfoDoc> { }

    @Schema(name = "ApiResponseSystemNetworkInfoList")
    interface SystemNetworkInfoListResponse
        extends ApiResponseWrapper<List<SystemNetworkInfoDoc>> { }

    @Schema(name = "ApiResponseNetworkDeviceList")
    interface NetworkDeviceListResponse extends ApiResponseWrapper<List<NetworkDeviceDoc>> { }

    @Schema(name = "ApiResponseSystemServerGroupList")
    interface SystemGroupListResponse extends ApiResponseWrapper<List<SystemGroupDoc>> { }

    @Schema(name = "ApiResponseCustomValue")
    interface CustomValueResponse extends ApiResponseWrapper<CustomValueDoc> { }

    @Schema(name = "ApiResponseSystemActionList")
    interface SystemActionListResponse extends ApiResponseWrapper<List<SystemActionDoc>> { }

    @Schema(name = "SystemSidsRequest")
    interface SidsRequest {

        /**
         * @return the ids of the servers
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();
    }

    @Schema(name = "SystemDeleteGuestProfilesRequest")
    @JsonPropertyOrder({"hostId", "guestNames"})
    interface DeleteGuestProfilesRequest {

        /**
         * @return the id of the host
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getHostId();

        /**
         * @return the names of the guest profiles
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getGuestNames();
    }

    @Schema(name = "SystemDeleteSystemsRequest")
    @JsonPropertyOrder({"sids", "cleanupType"})
    interface DeleteSystemsRequest extends SidsRequest {

        /**
         * @return the cleanup behaviour
         */
        @Schema(description = "Possible values: 'FAIL_ON_CLEANUP_ERR' - fail in case of " +
                    "cleanup error, 'NO_CLEANUP' - do not cleanup, just delete, " +
                    "'FORCE_DELETE' - Try cleanup first but delete server anyway in case " +
                    "of error",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCleanupType();
    }

    @Schema(name = "SystemDeleteSystemRequest")
    @JsonPropertyOrder({"sid", "cleanupType"})
    interface DeleteSystemRequest extends SidRequest {

        /**
         * @return the cleanup behaviour
         */
        @Schema(description = "Possible values: 'FAIL_ON_CLEANUP_ERR' - fail in case of " +
                    "cleanup error, 'NO_CLEANUP' - do not cleanup, just delete, " +
                    "'FORCE_DELETE' - Try cleanup first but delete server anyway in case " +
                    "of error",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCleanupType();
    }

    @Schema(name = "SystemSetGroupMembershipRequest")
    @JsonPropertyOrder({"sid", "sgid", "member"})
    interface SetGroupMembershipRequest extends SidRequest {

        /**
         * @return the id of the server group
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSgid();

        /**
         * @return whether the server belongs to the group
         */
        @Schema(description = "'1' to assign the given server to the given server group, " +
                    "'0' to remove the given server from the given server group.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getMember();
    }

    @Schema(name = "SystemSetCustomValuesRequest")
    @JsonPropertyOrder({"sid", "values"})
    interface SetCustomValuesRequest extends SidRequest {

        /**
         * @return the custom values to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "values")
        CustomValuesDoc getValues();
    }

    @Schema(name = "SystemCustomValuesDoc")
    @JsonPropertyOrder({"customInfoLabel", "value"})
    interface CustomValuesDoc {

        /**
         * @return the label of the custom information
         */
        @Schema(name = "custom info label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCustomInfoLabel();

        /**
         * @return the value of the custom information
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getValue();
    }

    @Schema(name = "SystemCustomValueDoc")
    interface CustomValueDoc {

        /**
         * @return the label of the custom information
         */
        @Schema(name = "custom info label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCustomInfoLabel();
    }

    @Schema(name = "SystemDeleteCustomValuesRequest")
    @JsonPropertyOrder({"sid", "keys"})
    interface DeleteCustomValuesRequest extends SidRequest {

        /**
         * @return the custom information keys to delete
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getKeys();
    }

    @Schema(name = "SystemSetProfileNameRequest")
    @JsonPropertyOrder({"sid", "name"})
    interface SetProfileNameRequest extends SidRequest {

        /**
         * @return the profile name
         */
        @Schema(description = "Name of the profile.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "SystemAddNoteRequest")
    @JsonPropertyOrder({"sid", "subject", "body"})
    interface AddNoteRequest extends SidRequest {

        /**
         * @return the subject of the note
         */
        @Schema(description = "What the note is about.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSubject();

        /**
         * @return the content of the note
         */
        @Schema(description = "Content of the note.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBody();
    }

    @Schema(name = "SystemDeleteNoteRequest")
    @JsonPropertyOrder({"sid", "noteId"})
    interface DeleteNoteRequest extends SidRequest {

        /**
         * @return the id of the note
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getNoteId();
    }

    @Schema(name = "SystemPackageLockStatusDoc")
    @JsonPropertyOrder({"packageId", "name", "epoch", "version", "release", "arch",
        "pendingStatus"})
    interface PackageLockStatusDoc {

        /**
         * @return the id of the package
         */
        @Schema(name = "package_id",
                description = "PackageID, -1 if package is locked but not available in " +
                    "subscribed channels",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPackageId();

        /**
         * @return the name of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the epoch of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

        /**
         * @return the version of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the release of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the architecture label of the package
         */
        @Schema(description = "architecture label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the pending lock status of the package
         */
        @Schema(name = "pending status",
                description = "return only if there is a pending locking",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPendingStatus();
    }

    @Schema(name = "SystemNetworkInfoDoc")
    @JsonPropertyOrder({"ip", "ip6", "hostname"})
    interface NetworkInfoDoc {

        /**
         * @return the IPv4 address of the server
         */
        @Schema(description = "IPv4 address of server", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIp();

        /**
         * @return the IPv6 address of the server
         */
        @Schema(description = "IPv6 address of server", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIp6();

        /**
         * @return the hostname of the server
         */
        @Schema(description = "Hostname of server", requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();
    }

    @Schema(name = "SystemSystemNetworkInfoDoc")
    @JsonPropertyOrder({"systemId", "ip", "ip6", "hostname", "primaryFqdn"})
    interface SystemNetworkInfoDoc {

        /**
         * @return the id of the system
         */
        @Schema(name = "system_id", description = "ID of the system",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();

        /**
         * @return the IPv4 address of the system
         */
        @Schema(description = "IPv4 address of system", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIp();

        /**
         * @return the IPv6 address of the system
         */
        @Schema(description = "IPv6 address of system", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIp6();

        /**
         * @return the hostname of the system
         */
        @Schema(description = "Hostname of system", requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();

        /**
         * @return the primary FQDN of the system
         */
        @Schema(name = "primary_fqdn", description = "Primary FQDN of system",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPrimaryFqdn();
    }

    @Schema(name = "SystemServerGroupDoc")
    @JsonPropertyOrder({"id", "subscribed", "systemGroupName", "sgid"})
    interface SystemGroupDoc {

        /**
         * @return the id of the server group
         */
        @Schema(description = "server group id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return whether the server is subscribed to this group
         */
        @Schema(description = "1 if the given server is subscribed to this server group, " +
                    "0 otherwise",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSubscribed();

        /**
         * @return the name of the server group
         */
        @Schema(name = "system_group_name", description = "Name of the server group",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemGroupName();

        /**
         * @return the id of the server group
         */
        @Schema(description = "server group id (Deprecated)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSgid();
    }

    @Schema(name = "SystemActionDoc")
    @JsonPropertyOrder({"failedCount", "modified", "modifiedDate", "created", "createdDate",
        "actionType", "successfulCount", "earliestAction", "archived", "schedulerUser",
        "prerequisite", "name", "id", "version", "completionTime", "completedDate",
        "pickupTime", "pickupDate", "resultMsg", "additionalInfo"})
    interface SystemActionDoc {

        /**
         * @return the number of times the action failed
         */
        @Schema(name = "failed_count", description = "Number of times action failed.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getFailedCount();

        /**
         * @return the modification date
         */
        @Schema(description = "Date modified. (Deprecated by modified_date)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getModified();

        /**
         * @return the modification date
         */
        @Schema(name = "modified_date", description = "Date modified.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getModifiedDate();

        /**
         * @return the creation date
         */
        @Schema(description = "Date created. (Deprecated by created_date)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCreated();

        /**
         * @return the creation date
         */
        @Schema(name = "created_date", description = "Date created.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreatedDate();

        /**
         * @return the type of the action
         */
        @Schema(name = "action_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getActionType();

        /**
         * @return the number of times the action was successful
         */
        @Schema(name = "successful_count",
                description = "Number of times action was successful.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSuccessfulCount();

        /**
         * @return the earliest date this action will occur
         */
        @Schema(name = "earliest_action",
                description = "Earliest date this action will occur.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getEarliestAction();

        /**
         * @return whether this action is archived
         */
        @Schema(description = "If this action is archived. (1 or 0)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getArchived();

        /**
         * @return the user who scheduled the action
         */
        @Schema(name = "scheduler_user",
                description = "available only if concrete user has scheduled the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSchedulerUser();

        /**
         * @return the prerequisite action
         */
        @Schema(description = "Pre-requisite action. (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPrerequisite();

        /**
         * @return the name of this action
         */
        @Schema(description = "Name of this action.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the id of this action
         */
        @Schema(description = "Id of this action.", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the version of the action
         */
        @Schema(description = "Version of action.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the completion time of the event
         */
        @Schema(name = "completion_time",
                description = "The date/time the event was completed. Format -&gt;" +
                    "YYYY-MM-dd hh:mm:ss.ms Eg -&gt;2007-06-04 13:58:13.0. (optional) " +
                    "(Deprecated by completed_date)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCompletionTime();

        /**
         * @return the completion date of the event
         */
        @Schema(name = "completed_date",
                description = "The date/time the event was completed. (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCompletedDate();

        /**
         * @return the pickup time of the action
         */
        @Schema(name = "pickup_time",
                description = "The date/time the action was picked up. Format -&gt;" +
                    "YYYY-MM-dd hh:mm:ss.ms Eg -&gt;2007-06-04 13:58:13.0. (optional) " +
                    "(Deprecated by pickup_date)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPickupTime();

        /**
         * @return the pickup date of the action
         */
        @Schema(name = "pickup_date",
                description = "The date/time the action was picked up. (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getPickupDate();

        /**
         * @return the result of the action
         */
        @Schema(name = "result_msg",
                description = "The result string after the action executes at the client " +
                    "machine. (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getResultMsg();

        /**
         * @return additional information about the event
         */
        @Schema(name = "additional_info",
                description = "This array contains additional information for the event, " +
                    "if available.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "info")
        List<SystemActionInfoDoc> getAdditionalInfo();
    }

    @Schema(name = "SystemActionInfoDoc")
    @JsonPropertyOrder({"detail", "result"})
    interface SystemActionInfoDoc {

        /**
         * @return the detail of the event
         */
        @Schema(description = "The detail provided depends on the specific event. For " +
                    "example, for a package event, this will be the package name, for an " +
                    "errata event, this will be the advisory name and synopsis, for a " +
                    "config file event, this will be path and optional revision " +
                    "information...etc.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getDetail();

        /**
         * @return the result of the event
         */
        @Schema(description = "The result (if included) depends on the specific event. " +
                    "For example, for a package or errata event, no result is included, " +
                    "for a config file event, the result might include an error (if one " +
                    "occurred, such as the file was missing) or in the case of a config " +
                    "file comparison it might include the differences found.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getResult();
    }

    @Schema(name = "ApiResponseSystemOverviewList")
    interface SystemOverviewListResponse
        extends ApiResponseWrapper<List<ServerGroupHandlerApi.SystemOverviewDoc>> { }

    @Schema(name = "ApiResponseNameInfo")
    interface NameInfoResponse extends ApiResponseWrapper<NameInfoDoc> { }

    @Schema(name = "ApiResponseDate")
    interface DateResponse extends ApiResponseWrapper<Date> { }

    @Schema(name = "ApiResponseSubscribedChildChannelList")
    interface SubscribedChildChannelListResponse
        extends ApiResponseWrapper<List<ChannelAppStreamHandlerApi.ChannelDoc>> { }

    @Schema(name = "ApiResponseAdministratorList")
    interface AdministratorListResponse
        extends ApiResponseWrapper<List<UserHandlerApi.UserDoc>> { }

    @Schema(name = "ApiResponseHistoryEventList")
    interface HistoryEventListResponse extends ApiResponseWrapper<List<HistoryEventDoc>> { }

    @Schema(name = "ApiResponseSystemEventList")
    interface SystemEventListResponse extends ApiResponseWrapper<List<SystemEventDoc>> { }

    @Schema(name = "SystemProvisionVirtualGuestRequest")
    @JsonPropertyOrder({"sid", "guestName", "profileName"})
    interface ProvisionVirtualGuestRequest {

        /**
         * @return the id of the host
         */
        @Schema(description = "ID of host to provision guest on.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the name of the guest
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getGuestName();

        /**
         * @return the kickstart profile to use
         */
        @Schema(description = "Kickstart profile to use.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileName();
    }

    @Schema(name = "SystemProvisionVirtualGuestWithResourcesRequest")
    @JsonPropertyOrder({"sid", "guestName", "profileName", "memoryMb", "vcpus", "storageGb"})
    interface ProvisionVirtualGuestWithResourcesRequest {

        /**
         * @return the id of the host
         */
        @Schema(description = "ID of host to provision guest on.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the name of the guest
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getGuestName();

        /**
         * @return the kickstart profile to use
         */
        @Schema(description = "Kickstart Profile to use.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileName();

        /**
         * @return the memory to allocate to the guest
         */
        @Schema(description = "Memory to allocate to the guest",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getMemoryMb();

        /**
         * @return the number of virtual CPUs
         */
        @Schema(description = "Number of virtual CPUs to allocate to the guest.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getVcpus();

        /**
         * @return the size of the disk image
         */
        @Schema(description = "Size of the guests disk image.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getStorageGb();
    }

    @Schema(name = "SystemProvisionVirtualGuestWithMacRequest")
    @JsonPropertyOrder({"sid", "guestName", "profileName", "memoryMb", "vcpus", "storageGb",
        "macAddress"})
    interface ProvisionVirtualGuestWithMacRequest
        extends ProvisionVirtualGuestWithResourcesRequest {

        /**
         * @return the MAC address of the guest
         */
        @Schema(description = "macAddress to give the guest's virtual networking hardware.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMacAddress();
    }

    @Schema(name = "SystemProvisionSystemRequest")
    @JsonPropertyOrder({"sid", "profileName"})
    interface ProvisionSystemRequest {

        /**
         * @return the id of the system
         */
        @Schema(description = "ID of the system to be provisioned.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the profile to use
         */
        @Schema(description = "Profile to use.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileName();
    }

    @Schema(name = "SystemProvisionSystemViaProxyRequest")
    @JsonPropertyOrder({"sid", "proxy", "profileName"})
    interface ProvisionSystemViaProxyRequest extends ProvisionSystemRequest {

        /**
         * @return the id of the proxy
         */
        @Schema(description = "ID of the proxy to use.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxy();
    }

    @Schema(name = "SystemProvisionSystemAtRequest")
    @JsonPropertyOrder({"sid", "profileName", "earliestDate"})
    interface ProvisionSystemAtRequest extends ProvisionSystemRequest {

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestDate();
    }

    @Schema(name = "SystemProvisionSystemViaProxyAtRequest")
    @JsonPropertyOrder({"sid", "proxy", "profileName", "earliestDate"})
    interface ProvisionSystemViaProxyAtRequest extends ProvisionSystemViaProxyRequest {

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestDate();
    }

    @Schema(name = "SystemProvisionSystemWithOptionsRequest")
    @JsonPropertyOrder({"sid", "profileName", "earliestDate", "advancedOptions"})
    interface ProvisionSystemWithOptionsRequest extends ProvisionSystemAtRequest {

        /**
         * @return the advanced provisioning options
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "advancedOptions")
        AdvancedOptionsDoc getAdvancedOptions();
    }

    @Schema(name = "SystemProvisionSystemViaProxyWithOptionsRequest")
    @JsonPropertyOrder({"sid", "proxy", "profileName", "earliestDate", "advancedOptions"})
    interface ProvisionSystemViaProxyWithOptionsRequest extends ProvisionSystemViaProxyAtRequest {

        /**
         * @return the advanced provisioning options
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "advancedOptions")
        AdvancedOptionsDoc getAdvancedOptions();
    }

    @Schema(name = "SystemAdvancedOptionsDoc")
    @JsonPropertyOrder({"kernelOptions", "postKernelOptions"})
    interface AdvancedOptionsDoc {

        /**
         * @return the custom kernel options
         */
        @Schema(name = "kernel_options", description = "custom kernel options",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getKernelOptions();

        /**
         * @return the custom post kernel options
         */
        @Schema(name = "post_kernel_options", description = "custom post kernel options",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPostKernelOptions();
    }

    @Schema(name = "SystemSearchByNameRequest")
    interface SearchByNameRequest {

        /**
         * @return the regular expression to match
         */
        @Schema(description = "A regular expression",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegexp();
    }

    @Schema(name = "SystemNameInfoDoc")
    @JsonPropertyOrder({"id", "name", "lastCheckin"})
    interface NameInfoDoc {

        /**
         * @return the id of the server
         */
        @Schema(description = "Server id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the server
         */
        @Schema(description = "Server name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the last time the server successfully checked in
         */
        @Schema(name = "last_checkin",
                description = "Last time server successfully checked in",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastCheckin();
    }

    @Schema(name = "SystemHistoryEventDoc")
    @JsonPropertyOrder({"completed", "summary", "details"})
    interface HistoryEventDoc {

        /**
         * @return the date the event occurred
         */
        @Schema(description = "date that the event occurred (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCompleted();

        /**
         * @return the summary of the event
         */
        @Schema(description = "summary of the event", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the details of the event
         */
        @Schema(description = "details of the event", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDetails();
    }

    @Schema(name = "SystemSystemEventDoc")
    @JsonPropertyOrder({"id", "historyType", "status", "summary", "completed"})
    interface SystemEventDoc {

        /**
         * @return the id of the event
         */
        @Schema(description = "ID of the event", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the type of the history event
         */
        @Schema(name = "history_type", description = "type of history event",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHistoryType();

        /**
         * @return the status of the event
         */
        @Schema(description = "status of the event", requiredMode = Schema.RequiredMode.REQUIRED)
        String getStatus();

        /**
         * @return the summary of the event
         */
        @Schema(description = "summary of the event", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the date the event occurred
         */
        @Schema(description = "date that the event occurred",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCompleted();
    }

    @Schema(name = "ApiResponseSystemEventDetails")
    interface SystemEventDetailsResponse extends ApiResponseWrapper<SystemEventDetailsDoc> { }

    @Schema(name = "ApiResponseSystemErrataOverviewList")
    interface ErrataOverviewListResponse extends ApiResponseWrapper<List<ErrataOverviewDoc>> { }

    @Schema(name = "ApiResponseServerErrataList")
    interface ServerErrataListResponse extends ApiResponseWrapper<List<ServerErrataDoc>> { }

    @Schema(name = "ApiResponseSystemErrataList")
    interface ErrataListResponse extends ApiResponseWrapper<List<ErrataDoc>> { }

    @Schema(name = "ApiResponsePackageMetadataList")
    interface PackageMetadataListResponse
        extends ApiResponseWrapper<List<PackageMetadataDoc>> { }

    @Schema(name = "SystemApplyErrataToSystemsRequest")
    @JsonPropertyOrder({"sids", "errataIds"})
    interface ApplyErrataToSystemsRequest extends SidsRequest {

        /**
         * @return the ids of the errata
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getErrataIds();
    }

    @Schema(name = "SystemApplyErrataToSystemsWithModulesRequest")
    @JsonPropertyOrder({"sids", "errataIds", "allowModules"})
    interface ApplyErrataToSystemsWithModulesRequest extends ApplyErrataToSystemsRequest {

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();
    }

    @Schema(name = "SystemApplyErrataToSystemsAtRequest")
    @JsonPropertyOrder({"sids", "errataIds", "earliestOccurrence"})
    interface ApplyErrataToSystemsAtRequest extends ApplyErrataToSystemsRequest {

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemApplyErrataToSystemsAtWithModulesRequest")
    @JsonPropertyOrder({"sids", "errataIds", "earliestOccurrence", "allowModules"})
    interface ApplyErrataToSystemsAtWithModulesRequest extends ApplyErrataToSystemsAtRequest {

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();
    }

    @Schema(name = "SystemApplyErrataToSystemsFullRequest")
    @JsonPropertyOrder({"sids", "errataIds", "earliestOccurrence", "allowModules",
        "onlyRelevant", "allowVendorChange"})
    interface ApplyErrataToSystemsFullRequest extends ApplyErrataToSystemsAtWithModulesRequest {

        /**
         * @return whether to apply only the relevant errata
         */
        @Schema(description = "If true not all erratas are applied to all systems. " +
                    "Systems get only the erratas relevant for them.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOnlyRelevant();

        /**
         * @return whether to allow a vendor change
         */
        @Schema(description = "Allow vendor change during the errata update",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowVendorChange();
    }

    @Schema(name = "SystemApplyErrataRequest")
    @JsonPropertyOrder({"sid", "errataIds"})
    interface ApplyErrataRequest extends SidRequest {

        /**
         * @return the ids of the errata
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getErrataIds();
    }

    @Schema(name = "SystemApplyErrataWithModulesRequest")
    @JsonPropertyOrder({"sid", "errataIds", "allowModules"})
    interface ApplyErrataWithModulesRequest extends ApplyErrataRequest {

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();
    }

    @Schema(name = "SystemApplyErrataAtRequest")
    @JsonPropertyOrder({"sid", "errataIds", "earliestOccurrence"})
    interface ApplyErrataAtRequest extends ApplyErrataRequest {

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemApplyErrataAtWithModulesRequest")
    @JsonPropertyOrder({"sid", "errataIds", "earliestOccurrence", "allowModules"})
    interface ApplyErrataAtWithModulesRequest extends ApplyErrataAtRequest {

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();
    }

    @Schema(name = "SystemApplyErrataOnlyRelevantRequest")
    @JsonPropertyOrder({"sid", "errataIds", "earliestOccurrence", "allowModules",
        "onlyRelevant"})
    interface ApplyErrataOnlyRelevantRequest {

        /**
         * @return the ids of the servers
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSid();

        /**
         * @return the ids of the errata
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getErrataIds();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();

        /**
         * @return whether to apply only the relevant errata
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOnlyRelevant();
    }

    @Schema(name = "SystemComparePackagesRequest")
    @JsonPropertyOrder({"sid1", "sid2"})
    interface ComparePackagesRequest {

        /**
         * @return the id of the first server
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid1();

        /**
         * @return the id of the second server
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid2();
    }

    @Schema(name = "SystemEventDetailsDoc")
    @JsonPropertyOrder({"id", "historyType", "status", "summary", "created", "pickedUp",
        "completed", "earliestAction", "resultMsg", "resultCode", "additionalInfo"})
    interface SystemEventDetailsDoc {

        /**
         * @return the id of the event
         */
        @Schema(description = "ID of the event", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the type of the history event
         */
        @Schema(name = "history_type", description = "type of history event",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHistoryType();

        /**
         * @return the status of the event
         */
        @Schema(description = "status of the event", requiredMode = Schema.RequiredMode.REQUIRED)
        String getStatus();

        /**
         * @return the summary of the event
         */
        @Schema(description = "summary of the event", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the date the event was created
         */
        @Schema(description = "date that the event was created",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreated();

        /**
         * @return the date the event was picked up
         */
        @Schema(name = "picked_up", description = "date that the event was picked up",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getPickedUp();

        /**
         * @return the date the event occurred
         */
        @Schema(description = "date that the event occurred",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCompleted();

        /**
         * @return the earliest date this action could occur
         */
        @Schema(name = "earliest_action",
                description = "earliest date this action could occur",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestAction();

        /**
         * @return the result string of the action
         */
        @Schema(name = "result_msg",
                description = "the result string of the action executed on the client " +
                    "machine (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getResultMsg();

        /**
         * @return the result code of the action
         */
        @Schema(name = "result_code",
                description = "the result code of the action executed on the client " +
                    "machine (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getResultCode();

        /**
         * @return additional information about the event
         */
        @Schema(name = "additional_info",
                description = "additional information for the event, if available",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "info")
        List<SystemActionInfoDoc> getAdditionalInfo();
    }

    @Schema(name = "SystemErrataOverviewDoc")
    @JsonPropertyOrder({"id", "issueDate", "date", "updateDate", "advisorySynopsis",
        "advisoryType", "advisoryStatus", "advisoryName", "rebootSuggested",
        "restartSuggested"})
    interface ErrataOverviewDoc {

        /**
         * @return the id of the erratum
         */
        @Schema(description = "errata ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the date the erratum was updated
         */
        @Schema(name = "issue_date", description = "the date erratum was updated (deprecated)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getIssueDate();

        /**
         * @return the date the erratum was created
         */
        @Schema(description = "the date erratum was created (deprecated)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getDate();

        /**
         * @return the date the erratum was updated
         */
        @Schema(name = "update_date", description = "the date erratum was updated (deprecated)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateDate();

        /**
         * @return the summary of the erratum
         */
        @Schema(name = "advisory_synopsis", description = "summary of the erratum",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisorySynopsis();

        /**
         * @return the type of the advisory
         */
        @Schema(name = "advisory_type",
                description = "type label such as 'Security', 'Bug Fix'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryType();

        /**
         * @return the status of the advisory
         */
        @Schema(name = "advisory_status",
                description = "status label such as 'final', 'testing', 'stable', " +
                    "'pending', 'retracted' or 'unpushed'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryStatus();

        /**
         * @return the name of the advisory
         */
        @Schema(name = "advisory_name", description = "name such as 'RHSA', etc.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();

        /**
         * @return whether a system reboot is advisable
         */
        @Schema(name = "reboot_suggested",
                description = "A boolean flag signaling whether a system reboot is " +
                    "advisable following the application of the errata. Typical example " +
                    "is upon kernel update.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRebootSuggested();

        /**
         * @return whether a package manager restart is advisable
         */
        @Schema(name = "restart_suggested",
                description = "A boolean flag signaling a weather reboot of the package " +
                    "manager is advisable following the application of the errata. This " +
                    "is commonly used to address update stack issues before proceeding " +
                    "with other updates.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRestartSuggested();
    }

    @Schema(name = "SystemServerErrataDoc")
    @JsonPropertyOrder({"systemId", "errata"})
    interface ServerErrataDoc {

        /**
         * @return the id of the system
         */
        @Schema(name = "system_id", description = "The ID of the system",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemId();

        /**
         * @return the errata available for the system
         */
        @Schema(description = "An array of available errata infos",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "errata")
        List<ErrataOverviewDoc> getErrata();
    }

    @Schema(name = "SystemErrataDoc")
    @JsonPropertyOrder({"id", "date", "advisoryType", "advisoryStatus", "advisoryName",
        "advisorySynopsis"})
    interface ErrataDoc {

        /**
         * @return the id of the erratum
         */
        @Schema(description = "errata ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the date the erratum was created
         */
        @Schema(description = "the date erratum was created",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getDate();

        /**
         * @return the type of the advisory
         */
        @Schema(name = "advisory_type", description = "type of the advisory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryType();

        /**
         * @return the status of the advisory
         */
        @Schema(name = "advisory_status", description = "status of the advisory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryStatus();

        /**
         * @return the name of the advisory
         */
        @Schema(name = "advisory_name", description = "name of the advisory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();

        /**
         * @return the summary of the erratum
         */
        @Schema(name = "advisory_synopsis", description = "summary of the erratum",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisorySynopsis();
    }

    @Schema(name = "SystemPackageMetadataDoc")
    @JsonPropertyOrder({"packageNameId", "packageName", "packageEpoch", "packageVersion",
        "packageRelease", "packageArch", "thisSystem", "otherSystem", "comparison"})
    interface PackageMetadataDoc {

        /**
         * @return the id of the package name
         */
        @Schema(name = "package_name_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPackageNameId();

        /**
         * @return the name of the package
         */
        @Schema(name = "package_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageName();

        /**
         * @return the epoch of the package
         */
        @Schema(name = "package_epoch", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageEpoch();

        /**
         * @return the version of the package
         */
        @Schema(name = "package_version", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageVersion();

        /**
         * @return the release of the package
         */
        @Schema(name = "package_release", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageRelease();

        /**
         * @return the architecture of the package
         */
        @Schema(name = "package_arch", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageArch();

        /**
         * @return the version of the package on this system
         */
        @Schema(name = "this_system", description = "version of package on this system",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getThisSystem();

        /**
         * @return the version of the package on the other system
         */
        @Schema(name = "other_system", description = "version of package on the other system",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOtherSystem();

        /**
         * @return the comparison result
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"0 - no difference", "1 - package on this system only",
                    "2 - newer package version on this system",
                    "3 - package on other system only",
                    "4 - newer package version on other system"})
        Integer getComparison();
    }

    @Schema(name = "ApiResponseDmi")
    interface DmiResponse extends ApiResponseWrapper<DmiDoc> { }

    @Schema(name = "ApiResponseCpu")
    interface CpuResponse extends ApiResponseWrapper<CpuDoc> { }

    @Schema(name = "ApiResponseMemory")
    interface MemoryResponse extends ApiResponseWrapper<MemoryDoc> { }

    @Schema(name = "ApiResponseDeviceList")
    interface DeviceListResponse extends ApiResponseWrapper<List<DeviceDoc>> { }

    @Schema(name = "ApiResponseNoteList")
    interface NoteListResponse extends ApiResponseWrapper<List<NoteDoc>> { }

    @Schema(name = "SystemSchedulePackagesForSystemsRequest")
    @JsonPropertyOrder({"sids", "packageIds", "earliestOccurrence"})
    interface SchedulePackagesForSystemsRequest extends SidsRequest {

        /**
         * @return the ids of the packages
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPackageIds();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemSchedulePackagesForSystemsWithModulesRequest")
    @JsonPropertyOrder({"sids", "packageIds", "earliestOccurrence", "allowModules"})
    interface SchedulePackagesForSystemsWithModulesRequest
        extends SchedulePackagesForSystemsRequest {

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();
    }

    @Schema(name = "SystemSchedulePackageUpdateRequest")
    @JsonPropertyOrder({"sids", "earliestOccurrence"})
    interface SchedulePackageUpdateRequest extends SidsRequest {

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemSchedulePackagesRequest")
    @JsonPropertyOrder({"sid", "packageIds", "earliestOccurrence"})
    interface SchedulePackagesRequest extends SidRequest {

        /**
         * @return the ids of the packages
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPackageIds();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemSchedulePackagesWithModulesRequest")
    @JsonPropertyOrder({"sid", "packageIds", "earliestOccurrence", "allowModules"})
    interface SchedulePackagesWithModulesRequest extends SchedulePackagesRequest {

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();
    }

    @Schema(name = "SystemScheduleNevraForSystemsRequest")
    @JsonPropertyOrder({"sids", "packageNevraList", "earliestOccurrence"})
    interface ScheduleNevraForSystemsRequest extends SidsRequest {

        /**
         * @return the packages identified by their NEVRA
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "Package nevra")
        List<PackageNevraDoc> getPackageNevraList();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemScheduleNevraForSystemsWithModulesRequest")
    @JsonPropertyOrder({"sids", "packageNevraList", "earliestOccurrence", "allowModules"})
    interface ScheduleNevraForSystemsWithModulesRequest extends ScheduleNevraForSystemsRequest {

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();
    }

    @Schema(name = "SystemScheduleNevraRequest")
    @JsonPropertyOrder({"sid", "packageNevraList", "earliestOccurrence"})
    interface ScheduleNevraRequest extends SidRequest {

        /**
         * @return the packages identified by their NEVRA
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "Package nevra")
        List<PackageNevraDoc> getPackageNevraList();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemScheduleNevraWithModulesRequest")
    @JsonPropertyOrder({"sid", "packageNevraList", "earliestOccurrence", "allowModules"})
    interface ScheduleNevraWithModulesRequest extends ScheduleNevraRequest {

        /**
         * @return whether to allow the call despite modular content
         */
        @Schema(description = "Allow this API call, despite modular content being present",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowModules();
    }

    @Schema(name = "SystemSchedulePackageLockChangeRequest")
    @JsonPropertyOrder({"sid", "pkgIdsToLock", "pkgIdsToUnlock", "earliestOccurrence"})
    interface SchedulePackageLockChangeRequest extends SidRequest {

        /**
         * @return the ids of the packages to lock
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPkgIdsToLock();

        /**
         * @return the ids of the packages to unlock
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPkgIdsToUnlock();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemPackageNevraDoc")
    @JsonPropertyOrder({"packageName", "packageEpoch", "packageVersion", "packageRelease",
        "packageArch"})
    interface PackageNevraDoc {

        /**
         * @return the name of the package
         */
        @Schema(name = "package_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageName();

        /**
         * @return the epoch of the package
         */
        @Schema(name = "package_epoch", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageEpoch();

        /**
         * @return the version of the package
         */
        @Schema(name = "package_version", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageVersion();

        /**
         * @return the release of the package
         */
        @Schema(name = "package_release", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageRelease();

        /**
         * @return the architecture of the package
         */
        @Schema(name = "package_arch", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageArch();
    }

    @Schema(name = "SystemMemoryDoc")
    @JsonPropertyOrder({"ram", "swap"})
    interface MemoryDoc {

        /**
         * @return the amount of physical memory in MB
         */
        @Schema(description = "The amount of physical memory in MB.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRam();

        /**
         * @return the amount of swap space in MB
         */
        @Schema(description = "The amount of swap space in MB.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSwap();
    }

    @Schema(name = "SystemDeviceDoc")
    @JsonPropertyOrder({"device", "deviceClass", "driver", "description", "bus", "pcitype"})
    interface DeviceDoc {

        /**
         * @return the device
         */
        @Schema(description = "optional", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDevice();

        /**
         * @return the class of the device
         */
        @Schema(name = "device_class",
                description = "Includes CDROM, FIREWIRE, HD, USB, VIDEO, OTHER, etc.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getDeviceClass();

        /**
         * @return the driver of the device
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDriver();

        /**
         * @return the description of the device
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the bus of the device
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getBus();

        /**
         * @return the PCI type of the device
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPcitype();
    }

    @Schema(name = "SystemNoteDoc")
    @JsonPropertyOrder({"id", "subject", "note", "systemId", "creator", "updated"})
    interface NoteDoc {

        /**
         * @return the id of the note
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the subject of the note
         */
        @Schema(description = "subject of the note", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSubject();

        /**
         * @return the contents of the note
         */
        @Schema(description = "contents of the note", requiredMode = Schema.RequiredMode.REQUIRED)
        String getNote();

        /**
         * @return the id of the system associated with the note
         */
        @Schema(name = "system_id",
                description = "the ID of the system associated with the note",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();

        /**
         * @return the creator of the note
         */
        @Schema(description = "creator of the note if exists (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCreator();

        /**
         * @return the date of the last note update
         */
        @Schema(description = "date of the last note update",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "date")
        Date getUpdated();
    }

    @Schema(name = "ApiResponseChannelPackageList")
    interface ChannelPackageListResponse extends ApiResponseWrapper<List<PackageDoc>> { }

    @Schema(name = "ApiResponseCoCoAttestationConfig")
    interface CoCoAttestationConfigResponse
        extends ApiResponseWrapper<CoCoAttestationConfigDoc> { }

    @Schema(name = "ApiResponseScriptResultList")
    interface ScriptResultListResponse extends ApiResponseWrapper<List<ScriptResultDoc>> { }

    @Schema(name = "ApiResponseScriptDetails")
    interface ScriptDetailsResponse extends ApiResponseWrapper<ScriptDetailsDoc> { }

    @Schema(name = "ApiResponseServerDetails")
    interface ServerDetailsResponse extends ApiResponseWrapper<ServerDetailsDoc> { }

    @Schema(name = "ApiResponsePackageProfileList")
    interface PackageProfileListResponse
        extends ApiResponseWrapper<List<PackageProfileDoc>> { }

    @Schema(name = "ApiResponseBaseChannel")
    interface BaseChannelResponse
        extends ApiResponseWrapper<ChannelAppStreamHandlerApi.ChannelDoc> { }

    @Schema(name = "SystemScheduleForSystemRequest")
    @JsonPropertyOrder({"sid", "earliestOccurrence"})
    interface ScheduleForSystemRequest extends SidRequest {

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemScheduleScriptRunForSystemsRequest")
    @JsonPropertyOrder({"sids", "username", "groupname", "timeout", "script",
        "earliestOccurrence"})
    interface ScheduleScriptRunForSystemsRequest {

        /**
         * @return the ids of the servers
         */
        @Schema(description = "System IDs of the servers to run the script on.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the user to run the script as
         */
        @Schema(description = "User to run script as.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();

        /**
         * @return the group to run the script as
         */
        @Schema(description = "Group to run script as.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getGroupname();

        /**
         * @return the timeout of the script
         */
        @Schema(description = "Seconds to allow the script to run before timing out.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTimeout();

        /**
         * @return the contents of the script
         */
        @Schema(description = "Contents of the script to run. Must start with a shebang " +
                    "(e.g. #!/bin/bash)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getScript();

        /**
         * @return the time to schedule the action
         */
        @Schema(description = "Earliest the script can run.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemScheduleScriptRunLabelledForSystemsRequest")
    @JsonPropertyOrder({"label", "sids", "username", "groupname", "timeout", "script",
        "earliestOccurrence"})
    interface ScheduleScriptRunLabelledForSystemsRequest
        extends ScheduleScriptRunForSystemsRequest {

        /**
         * @return the label of the script action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "SystemScheduleScriptRunRequest")
    @JsonPropertyOrder({"sid", "username", "groupname", "timeout", "script",
        "earliestOccurrence"})
    interface ScheduleScriptRunRequest {

        /**
         * @return the id of the server
         */
        @Schema(description = "ID of the server to run the script on.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the user to run the script as
         */
        @Schema(description = "User to run script as.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();

        /**
         * @return the group to run the script as
         */
        @Schema(description = "Group to run script as.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getGroupname();

        /**
         * @return the timeout of the script
         */
        @Schema(description = "Seconds to allow the script to run before timing out.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTimeout();

        /**
         * @return the contents of the script
         */
        @Schema(description = "Contents of the script to run. Must start with a shebang " +
                    "(e.g. #!/bin/bash)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getScript();

        /**
         * @return the time to schedule the action
         */
        @Schema(description = "Earliest the script can run.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemScheduleScriptRunLabelledRequest")
    @JsonPropertyOrder({"label", "sid", "username", "groupname", "timeout", "script",
        "earliestOccurrence"})
    interface ScheduleScriptRunLabelledRequest extends ScheduleScriptRunRequest {

        /**
         * @return the label of the script action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "SystemScheduleCoCoAttestationRequest")
    @JsonPropertyOrder({"sid", "earliestOccurrence"})
    interface ScheduleCoCoAttestationRequest {

        /**
         * @return the id of the server
         */
        @Schema(description = "ID of the server to run the script on.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the time to schedule the action
         */
        @Schema(description = "Earliest the script can run.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemSetCoCoAttestationConfigRequest")
    @JsonPropertyOrder({"sid", "enabled", "environmentType", "attestOnBoot"})
    interface SetCoCoAttestationConfigRequest {

        /**
         * @return the id of the server
         */
        @Schema(description = "ID of the server to get the config for.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return whether attestation is enabled
         */
        @Schema(description = "set the enabled state for Confidential Compute Attestation",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();

        /**
         * @return the environment type of the system
         */
        @Schema(description = "set the environment type of the system:",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"KVM_AMD_EPYC_MILAN", "KVM_AMD_EPYC_GENOA",
                    "KVM_AMD_EPYC_BERGAMO", "KVM_AMD_EPYC_SIENA", "KVM_AMD_EPYC_TURIN"})
        String getEnvironmentType();

        /**
         * @return whether the attestation should be performed on system boot
         */
        @Schema(description = "set if the attestation should be performed on system boot",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAttestOnBoot();
    }

    @Schema(name = "SystemActionIdRequest")
    interface ActionIdRequest {

        /**
         * @return the id of the script run action
         */
        @Schema(description = "ID of the script run action.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getActionId();
    }

    @Schema(name = "SystemSetDetailsRequest")
    @JsonPropertyOrder({"sid", "details"})
    interface SetDetailsRequest {

        /**
         * @return the id of the server
         */
        @Schema(description = "ID of server to lookup details for.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the details to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "details")
        SetDetailsDoc getDetails();
    }

    @Schema(name = "SystemSetDetailsDoc")
    @JsonPropertyOrder({"profileName", "baseEntitlement", "autoErrataUpdate", "description",
        "address1", "address2", "city", "state", "country", "building", "room", "rack",
        "contactMethod"})
    interface SetDetailsDoc {

        /**
         * @return the profile name of the system
         */
        @Schema(name = "profile_name", description = "System's profile name",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileName();

        /**
         * @return the base entitlement label of the system
         */
        @Schema(name = "base_entitlement",
                description = "System's base entitlement label. (enterprise_entitled or " +
                    "unentitle)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseEntitlement();

        /**
         * @return whether auto errata updates are enabled
         */
        @Schema(name = "auto_errata_update",
                description = "True if system has auto errata updates enabled",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAutoErrataUpdate();

        /**
         * @return the description of the system
         */
        @Schema(description = "System description", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the first address line of the system
         */
        @Schema(description = "System's address line 1.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAddress1();

        /**
         * @return the second address line of the system
         */
        @Schema(description = "System's address line 2.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAddress2();

        /**
         * @return the city of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCity();

        /**
         * @return the state of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getState();

        /**
         * @return the country of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCountry();

        /**
         * @return the building of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getBuilding();

        /**
         * @return the room of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRoom();

        /**
         * @return the rack of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRack();

        /**
         * @return the contact method of the system
         */
        @Schema(name = "contact_method", description = "One of the following:",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"default", "ssh-push", "ssh-push-tunnel"})
        String getContactMethod();
    }

    @Schema(name = "SystemSetLockStatusRequest")
    @JsonPropertyOrder({"sid", "lockStatus"})
    interface SetLockStatusRequest extends SidRequest {

        /**
         * @return whether the system is locked
         */
        @Schema(description = "true to lock the system, false to unlock the system.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getLockStatus();
    }

    @Schema(name = "SystemAddEntitlementsRequest")
    @JsonPropertyOrder({"sid", "entitlements"})
    interface AddEntitlementsRequest extends SidRequest {

        /**
         * @return the entitlements to add
         */
        @Schema(description = "one of following: virtualization_host, enterprise_entitled",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getEntitlements();
    }

    @Schema(name = "SystemRemoveEntitlementsRequest")
    @JsonPropertyOrder({"sid", "entitlements"})
    interface RemoveEntitlementsRequest extends SidRequest {

        /**
         * @return the entitlements to remove
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getEntitlements();
    }

    @Schema(name = "SystemClientSystemIdRequest")
    interface ClientSystemIdRequest {

        /**
         * @return the client system id file
         */
        @Schema(description = "client system id file",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getClientCert();
    }

    @Schema(name = "SystemDeletePackageProfileRequest")
    interface DeletePackageProfileRequest {

        /**
         * @return the id of the profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProfileId();
    }

    @Schema(name = "SystemCreatePackageProfileRequest")
    @JsonPropertyOrder({"sid", "profileLabel", "description"})
    interface CreatePackageProfileRequest extends SidRequest {

        /**
         * @return the label of the profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();

        /**
         * @return the description of the profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "SystemComparePackageProfileRequest")
    @JsonPropertyOrder({"sid", "profileLabel"})
    interface ComparePackageProfileRequest extends SidRequest {

        /**
         * @return the label of the profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileLabel();
    }

    @Schema(name = "SystemSyncPackagesWithSystemRequest")
    @JsonPropertyOrder({"targetServerId", "sourceServerId", "packageIds", "earliestOccurrence"})
    interface SyncPackagesWithSystemRequest {

        /**
         * @return the id of the target server
         */
        @Schema(description = "Target system to apply package changes to.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTargetServerId();

        /**
         * @return the id of the source server
         */
        @Schema(description = "Source system to retrieve package state from.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSourceServerId();

        /**
         * @return the ids of the packages to sync
         */
        @Schema(description = "Package IDs to be synced.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPackageIds();

        /**
         * @return the time to schedule the action
         */
        @Schema(description = "Date to schedule action for",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemPackageDoc")
    @JsonPropertyOrder({"name", "version", "release", "epoch", "id", "archLabel",
        "lastModified", "path", "partOfRetractedPatch", "provider"})
    interface PackageDoc extends InstallablePackageDoc {

        /**
         * @return the last modification date of the package
         */
        @Schema(name = "last_modified", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastModified();

        /**
         * @return the path of the package on the file system
         */
        @Schema(description = "the path on that file system that the package resides",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return whether the package is part of a retracted patch
         */
        @Schema(name = "part_of_retracted_patch",
                description = "true if the package is a part of a retracted patch",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPartOfRetractedPatch();

        /**
         * @return the provider of the package
         */
        @Schema(description = "the provider of the package, determined by the gpg key it " +
                    "was signed with.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProvider();
    }

    @Schema(name = "SystemCoCoAttestationConfigDoc")
    @JsonPropertyOrder({"enabled", "environmentType", "systemId"})
    interface CoCoAttestationConfigDoc {

        /**
         * @return whether attestation is enabled for this system
         */
        @Schema(description = "true if Confidential Compute Attestation is enabled for " +
                    "this system",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getEnabled();

        /**
         * @return the configured environment type
         */
        @Schema(name = "environment_type", description = "the configured environment type",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getEnvironmentType();

        /**
         * @return the id of the system
         */
        @Schema(name = "system_id", description = "the ID of the system",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();
    }

    @Schema(name = "SystemScriptResultDoc")
    @JsonPropertyOrder({"serverId", "startDate", "stopDate", "returnCode", "output",
        "outputEnc64"})
    interface ScriptResultDoc {

        /**
         * @return the id of the server the script runs on
         */
        @Schema(description = "ID of the server the script runs on",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getServerId();

        /**
         * @return the time the script began execution
         */
        @Schema(description = "time script began execution",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getStartDate();

        /**
         * @return the time the script stopped execution
         */
        @Schema(description = "time script stopped execution",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getStopDate();

        /**
         * @return the return code of the script execution
         */
        @Schema(description = "script execution return code",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getReturnCode();

        /**
         * @return the output of the script
         */
        @Schema(description = "output of the script (base64 encoded according to the " +
                    "output_enc64 attribute)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOutput();

        /**
         * @return whether the output is base64 encoded
         */
        @Schema(name = "output_enc64", description = "identifies base64 encoded output",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOutputEnc64();
    }

    @Schema(name = "SystemScriptDetailsDoc")
    @JsonPropertyOrder({"id", "content", "runAsUser", "runAsGroup", "timeout", "result"})
    interface ScriptDetailsDoc {

        /**
         * @return the id of the action
         */
        @Schema(description = "action id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the content of the script
         */
        @Schema(description = "script content", requiredMode = Schema.RequiredMode.REQUIRED)
        String getContent();

        /**
         * @return the user the script runs as
         */
        @Schema(name = "run_as_user", description = "Run as user",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRunAsUser();

        /**
         * @return the group the script runs as
         */
        @Schema(name = "run_as_group", description = "Run as group",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRunAsGroup();

        /**
         * @return the timeout in seconds
         */
        @Schema(description = "Timeout in seconds", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTimeout();

        /**
         * @return the results of the script run
         */
        @Schema(description = "results of the script run",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "script result")
        List<ScriptResultDoc> getResult();
    }

    @Schema(name = "SystemServerDetailsDoc")
    @JsonPropertyOrder({"id", "profileName", "machineId", "payg", "minionId",
        "baseEntitlement", "addonEntitlements", "autoUpdate", "release", "address1",
        "address2", "city", "state", "country", "building", "room", "rack", "description",
        "hostname", "lastBoot", "osaStatus", "lockStatus", "virtualization", "contactMethod"})
    interface ServerDetailsDoc {

        /**
         * @return the id of the system
         */
        @Schema(description = "system ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the profile name of the system
         */
        @Schema(name = "profile_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileName();

        /**
         * @return the machine id of the system
         */
        @Schema(name = "machine_id", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMachineId();

        /**
         * @return whether the server instance is pay-as-you-go
         */
        @Schema(description = "Whether the server instance is payg or not",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getPayg();

        /**
         * @return the minion id of the system
         */
        @Schema(name = "minion_id", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMinionId();

        /**
         * @return the base entitlement label of the system
         */
        @Schema(name = "base_entitlement", description = "system's base entitlement label",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseEntitlement();

        /**
         * @return the addon entitlement labels of the system
         */
        @Schema(name = "addon_entitlements",
                description = "system's addon entitlements labels, currently only " +
                    "'virtualization_host'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getAddonEntitlements();

        /**
         * @return whether auto errata updates are enabled
         */
        @Schema(name = "auto_update",
                description = "true if system has auto errata updates enabled",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAutoUpdate();

        /**
         * @return the operating system release
         */
        @Schema(description = "the operating system release (i.e. 4AS, 5Server)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the first address line of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAddress1();

        /**
         * @return the second address line of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAddress2();

        /**
         * @return the city of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCity();

        /**
         * @return the state of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getState();

        /**
         * @return the country of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCountry();

        /**
         * @return the building of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getBuilding();

        /**
         * @return the room of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRoom();

        /**
         * @return the rack of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRack();

        /**
         * @return the description of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the hostname of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();

        /**
         * @return the last boot time of the system
         */
        @Schema(name = "last_boot", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastBoot();

        /**
         * @return the OSA status of the system
         */
        @Schema(name = "osa_status",
                description = "either 'unknown', 'offline', or 'online'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOsaStatus();

        /**
         * @return the lock status of the system
         */
        @Schema(name = "lock_status",
                description = "True indicates that the system is locked. False indicates " +
                    "that the system is unlocked.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getLockStatus();

        /**
         * @return the virtualization type of the system
         */
        @Schema(description = "virtualization type - for virtual guests only (optional)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getVirtualization();

        /**
         * @return the contact method of the system
         */
        @Schema(name = "contact_method", description = "one of the following:",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"default", "ssh-push", "ssh-push-tunnel"})
        String getContactMethod();
    }

    @Schema(name = "SystemPackageProfileDoc")
    @JsonPropertyOrder({"id", "name", "channel"})
    interface PackageProfileDoc {

        /**
         * @return the id of the profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the channel of the profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannel();
    }

    @Schema(name = "ApiResponseSystemUser")
    interface UserResponse extends ApiResponseWrapper<UserHandlerApi.UserDoc> { }

    @Schema(name = "ApiResponseVirtualSystemList")
    interface VirtualSystemListResponse extends ApiResponseWrapper<List<VirtualSystemDoc>> { }

    @Schema(name = "ApiResponseServerPathList")
    interface ServerPathListResponse extends ApiResponseWrapper<List<ServerPathDoc>> { }

    @Schema(name = "ApiResponseKickstartVariables")
    interface KickstartVariablesResponse
        extends ApiResponseWrapper<KickstartVariablesDoc> { }

    @Schema(name = "ApiResponseDuplicateByIpList")
    interface DuplicateByIpListResponse
        extends ApiResponseWrapper<List<DuplicateByIpDoc>> { }

    @Schema(name = "ApiResponseDuplicateByMacList")
    interface DuplicateByMacListResponse
        extends ApiResponseWrapper<List<DuplicateByMacDoc>> { }

    @Schema(name = "ApiResponseDuplicateByHostnameList")
    interface DuplicateByHostnameListResponse
        extends ApiResponseWrapper<List<DuplicateByHostnameDoc>> { }

    @Schema(name = "ApiResponseCurrencyMultipliers")
    interface CurrencyMultiplierResponse
        extends ApiResponseWrapper<Map<String, Integer>> { }

    @Schema(name = "ApiResponseCurrencyScoreList")
    interface CurrencyScoreListResponse
        extends ApiResponseWrapper<List<CurrencyScoreDoc>> { }

    @Schema(name = "ApiResponseExtraPackagesSystemList")
    interface ExtraPackagesSystemListResponse
        extends ApiResponseWrapper<List<ExtraPackagesSystemDoc>> { }

    @Schema(name = "ApiResponseExtraPackageList")
    interface ExtraPackageListResponse extends ApiResponseWrapper<List<ExtraPackageDoc>> { }

    @Schema(name = "ApiResponseOsaPing")
    interface OsaPingResponse extends ApiResponseWrapper<OsaPingDoc> { }

    @Schema(name = "ApiResponseMigrationTargetList")
    interface MigrationTargetListResponse
        extends ApiResponseWrapper<List<MigrationTargetDoc>> { }

    @Schema(name = "ApiResponseMigrationTargetWithChannelsList")
    interface MigrationTargetWithChannelsListResponse
        extends ApiResponseWrapper<List<MigrationTargetWithChannelsDoc>> { }

    @Schema(name = "ApiResponseSuggestedRebootList")
    interface SuggestedRebootListResponse
        extends ApiResponseWrapper<List<SuggestedRebootDoc>> { }

    @Schema(name = "ApiResponseInstalledProductList")
    interface InstalledProductListResponse
        extends ApiResponseWrapper<List<InstalledProductDoc>> { }

    @Schema(name = "ApiResponseMinionIdMap")
    interface MinionIdMapResponse extends ApiResponseWrapper<Map<String, Long>> { }

    @Schema(name = "ApiResponsePackageStateList")
    interface PackageStateListResponse extends ApiResponseWrapper<List<PackageStateDoc>> { }

    @Schema(name = "ApiResponseSystemGroupsList")
    interface SystemGroupsListResponse
        extends ApiResponseWrapper<List<SystemGroupMembershipDoc>> { }

    @Schema(name = "ApiResponsePillar")
    interface PillarResponse extends ApiResponseWrapper<Map<String, Object>> { }

    @Schema(name = "ApiResponseIntegerList")
    interface IntegerListResponse extends ApiResponseWrapper<List<Integer>> { }

    @Schema(name = "ApiResponseCoCoAttestationReportList")
    interface CoCoAttestationReportListResponse
        extends ApiResponseWrapper<List<CoCoAttestationReportDoc>> { }

    @Schema(name = "ApiResponseCoCoAttestationReport")
    interface CoCoAttestationReportResponse
        extends ApiResponseWrapper<CoCoAttestationReportDoc> { }

    @Schema(name = "ApiResponseCoCoAttestationResult")
    interface CoCoAttestationResultResponse
        extends ApiResponseWrapper<CoCoAttestationResultDoc> { }

    @Schema(name = "SystemCreateSystemRecordRequest")
    @JsonPropertyOrder({"sid", "ksLabel"})
    interface CreateSystemRecordRequest extends SidRequest {

        /**
         * @return the kickstart label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();
    }

    @Schema(name = "SystemCreateUnregisteredSystemRecordRequest")
    @JsonPropertyOrder({"systemName", "ksLabel", "koptions", "comment", "netDevices"})
    interface CreateUnregisteredSystemRecordRequest {

        /**
         * @return the name of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemName();

        /**
         * @return the kickstart label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the kernel options
         */
        @Schema(name = "kOptions", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKOptions();

        /**
         * @return the comment of the record
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getComment();

        /**
         * @return the network devices of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "network device")
        List<CobblerNetworkDeviceDoc> getNetDevices();
    }

    @Schema(name = "SystemCobblerNetworkDeviceDoc")
    @JsonPropertyOrder({"name", "ip", "mac", "dnsname"})
    interface CobblerNetworkDeviceDoc {

        /**
         * @return the name of the device
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the IP address of the device
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getIp();

        /**
         * @return the MAC address of the device
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getMac();

        /**
         * @return the DNS name of the device
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDnsname();
    }

    @Schema(name = "SystemCreateSystemProfileRequest")
    @JsonPropertyOrder({"systemName", "data"})
    interface CreateSystemProfileRequest {

        /**
         * @return the name of the system
         */
        @Schema(description = "System name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemName();

        /**
         * @return the data of the system profile
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "data")
        SystemProfileDataDoc getData();
    }

    @Schema(name = "SystemProfileDataDoc")
    @JsonPropertyOrder({"hwAddress", "hostname"})
    interface SystemProfileDataDoc {

        /**
         * @return the hardware address of the network interface
         */
        @Schema(description = "The HW address of the network interface (MAC)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHwAddress();

        /**
         * @return the hostname of the profile
         */
        @Schema(description = "The hostname of the profile",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();
    }

    @Schema(name = "SystemSetVariablesRequest")
    @JsonPropertyOrder({"sid", "netboot", "variables"})
    interface SetVariablesRequest extends SidRequest {

        /**
         * @return whether netboot is enabled
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getNetboot();

        /**
         * @return the kickstart variables to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "variables")
        KickstartVariableDoc getVariables();
    }

    @Schema(name = "SystemKickstartVariableDoc")
    @JsonPropertyOrder({"key", "value"})
    interface KickstartVariableDoc {

        /**
         * @return the name of the variable
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKey();

        /**
         * @return the value of the variable
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "string or int")
        String getValue();
    }

    @Schema(name = "SystemSnapshotTagRequest")
    @JsonPropertyOrder({"sid", "tagName"})
    interface SnapshotTagRequest extends SidRequest {

        /**
         * @return the name of the tag
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getTagName();
    }

    @Schema(name = "SystemSetPrimaryInterfaceRequest")
    @JsonPropertyOrder({"sid", "interfaceName"})
    interface SetPrimaryInterfaceRequest extends SidRequest {

        /**
         * @return the name of the interface
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getInterfaceName();
    }

    @Schema(name = "SystemSetPrimaryFqdnRequest")
    @JsonPropertyOrder({"sid", "fqdn"})
    interface SetPrimaryFqdnRequest extends SidRequest {

        /**
         * @return the FQDN to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();
    }

    @Schema(name = "SystemMigrationRequest")
    @JsonPropertyOrder({"sid", "baseChannelLabel", "optionalChildChannels", "dryRun",
        "earliestOccurrence"})
    interface MigrationRequest extends SidRequest {

        /**
         * @return the label of the target base channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseChannelLabel();

        /**
         * @return the optional child channels
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getOptionalChildChannels();

        /**
         * @return whether to perform a dry run
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getDryRun();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemMigrationWithVendorChangeRequest")
    @JsonPropertyOrder({"sid", "baseChannelLabel", "optionalChildChannels", "dryRun",
        "allowVendorChange", "earliestOccurrence"})
    interface MigrationWithVendorChangeRequest extends MigrationRequest {

        /**
         * @return whether to allow a vendor change
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowVendorChange();
    }

    @Schema(name = "SystemMigrationToTargetRequest")
    @JsonPropertyOrder({"sid", "targetIdent", "baseChannelLabel", "optionalChildChannels",
        "dryRun", "earliestOccurrence"})
    interface MigrationToTargetRequest extends MigrationRequest {

        /**
         * @return the identifier of the migration target
         */
        @Schema(description = "Identifier for the selected migration target. Use " +
                    "listMigrationTargets to list the identifiers",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getTargetIdent();
    }

    @Schema(name = "SystemMigrationToTargetWithVendorChangeRequest")
    @JsonPropertyOrder({"sid", "targetIdent", "baseChannelLabel", "optionalChildChannels",
        "dryRun", "allowVendorChange", "earliestOccurrence"})
    interface MigrationToTargetWithVendorChangeRequest extends MigrationToTargetRequest {

        /**
         * @return whether to allow a vendor change
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowVendorChange();
    }

    @Schema(name = "SystemMigrationFullRequest")
    @JsonPropertyOrder({"sid", "targetIdent", "baseChannelLabel", "optionalChildChannels",
        "dryRun", "allowVendorChange", "removeProductsWithNoSuccessorAfterMigration",
        "earliestOccurrence"})
    interface MigrationFullRequest extends MigrationToTargetWithVendorChangeRequest {

        /**
         * @return whether to remove products without successors
         */
        @Schema(description = "set to remove products which have no successors. This flag " +
                    "will only have effect if targetIdent will also be specified",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRemoveProductsWithNoSuccessorAfterMigration();
    }

    @Schema(name = "SystemDistUpgradeRequest")
    @JsonPropertyOrder({"sid", "channels", "dryRun", "earliestOccurrence"})
    interface DistUpgradeRequest extends SidRequest {

        /**
         * @return the labels of the channels to subscribe to
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannels();

        /**
         * @return whether to perform a dry run
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getDryRun();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemDistUpgradeWithVendorChangeRequest")
    @JsonPropertyOrder({"sid", "channels", "dryRun", "allowVendorChange",
        "earliestOccurrence"})
    interface DistUpgradeWithVendorChangeRequest extends DistUpgradeRequest {

        /**
         * @return whether to allow a vendor change
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAllowVendorChange();
    }

    @Schema(name = "SystemBootstrapRequest")
    @JsonPropertyOrder({"host", "sshPort", "sshUser", "sshPassword", "activationKey",
        "saltSSH"})
    interface BootstrapRequest {

        /**
         * @return the hostname or IP address of the target
         */
        @Schema(description = "Hostname or IP address of target",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHost();

        /**
         * @return the SSH port of the target
         */
        @Schema(description = "SSH port on target machine",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSshPort();

        /**
         * @return the SSH user on the target
         */
        @Schema(description = "SSH user on target machine",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSshUser();

        /**
         * @return the SSH password of the given user
         */
        @Schema(description = "SSH password of given user",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSshPassword();

        /**
         * @return the activation key
         */
        @Schema(description = "Activation key", requiredMode = Schema.RequiredMode.REQUIRED)
        String getActivationKey();

        /**
         * @return whether to manage the system with Salt SSH
         */
        @Schema(name = "saltSSH", description = "Manage system with Salt SSH",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getSaltSSH();
    }

    @Schema(name = "SystemBootstrapViaProxyRequest")
    @JsonPropertyOrder({"host", "sshPort", "sshUser", "sshPassword", "activationKey",
        "proxyId", "saltSSH"})
    interface BootstrapViaProxyRequest extends BootstrapRequest {

        /**
         * @return the id of the proxy
         */
        @Schema(description = "Proxy ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyId();
    }

    @Schema(name = "SystemBootstrapWithReactivationRequest")
    @JsonPropertyOrder({"host", "sshPort", "sshUser", "sshPassword", "activationKey",
        "reactivationKey", "saltSSH"})
    interface BootstrapWithReactivationRequest extends BootstrapRequest {

        /**
         * @return the reactivation key
         */
        @Schema(description = "Reactivation key", requiredMode = Schema.RequiredMode.REQUIRED)
        String getReactivationKey();
    }

    @Schema(name = "SystemBootstrapWithReactivationViaProxyRequest")
    @JsonPropertyOrder({"host", "sshPort", "sshUser", "sshPassword", "activationKey",
        "reactivationKey", "proxyId", "saltSSH"})
    interface BootstrapWithReactivationViaProxyRequest
        extends BootstrapWithReactivationRequest {

        /**
         * @return the id of the proxy
         */
        @Schema(description = "Proxy ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyId();
    }

    @Schema(name = "SystemBootstrapWithKeyRequest")
    @JsonPropertyOrder({"host", "sshPort", "sshUser", "sshPrivKey", "sshPrivKeyPass",
        "activationKey", "saltSSH"})
    interface BootstrapWithKeyRequest {

        /**
         * @return the hostname or IP address of the target
         */
        @Schema(description = "Hostname or IP address of target",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHost();

        /**
         * @return the SSH port of the target
         */
        @Schema(description = "SSH port on target machine",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSshPort();

        /**
         * @return the SSH user on the target
         */
        @Schema(description = "SSH user on target machine",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSshUser();

        /**
         * @return the SSH private key
         */
        @Schema(description = "SSH private key as a string in PEM format",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSshPrivKey();

        /**
         * @return the passphrase of the key
         */
        @Schema(description = "SSH passphrase for the key (use empty string for no " +
                    "passphrase)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSshPrivKeyPass();

        /**
         * @return the activation key
         */
        @Schema(description = "Activation key", requiredMode = Schema.RequiredMode.REQUIRED)
        String getActivationKey();

        /**
         * @return whether to manage the system with Salt SSH
         */
        @Schema(name = "saltSSH", description = "Manage system with Salt SSH",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getSaltSSH();
    }

    @Schema(name = "SystemBootstrapWithKeyViaProxyRequest")
    @JsonPropertyOrder({"host", "sshPort", "sshUser", "sshPrivKey", "sshPrivKeyPass",
        "activationKey", "proxyId", "saltSSH"})
    interface BootstrapWithKeyViaProxyRequest extends BootstrapWithKeyRequest {

        /**
         * @return the id of the proxy
         */
        @Schema(description = "Proxy ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyId();
    }

    @Schema(name = "SystemBootstrapWithKeyAndReactivationRequest")
    @JsonPropertyOrder({"host", "sshPort", "sshUser", "sshPrivKey", "sshPrivKeyPass",
        "activationKey", "reactivationKey", "saltSSH"})
    interface BootstrapWithKeyAndReactivationRequest extends BootstrapWithKeyRequest {

        /**
         * @return the reactivation key
         */
        @Schema(description = "Reactivation key", requiredMode = Schema.RequiredMode.REQUIRED)
        String getReactivationKey();
    }

    @Schema(name = "SystemBootstrapWithKeyAndReactivationViaProxyRequest")
    @JsonPropertyOrder({"host", "sshPort", "sshUser", "sshPrivKey", "sshPrivKeyPass",
        "activationKey", "reactivationKey", "proxyId", "saltSSH"})
    interface BootstrapWithKeyAndReactivationViaProxyRequest
        extends BootstrapWithKeyAndReactivationRequest {

        /**
         * @return the id of the proxy
         */
        @Schema(description = "Proxy ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyId();
    }

    @Schema(name = "SystemApplyHighstateRequest")
    @JsonPropertyOrder({"sid", "earliestOccurrence", "test"})
    interface ApplyHighstateRequest extends ScheduleForSystemRequest {

        /**
         * @return whether to run the states in test-only mode
         */
        @Schema(description = "Run states in test-only mode",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTest();
    }

    @Schema(name = "SystemApplyHighstateForSystemsRequest")
    @JsonPropertyOrder({"sids", "earliestOccurrence", "test"})
    interface ApplyHighstateForSystemsRequest extends SidsRequest {

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();

        /**
         * @return whether to run the states in test-only mode
         */
        @Schema(description = "Run states in test-only mode",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTest();
    }

    @Schema(name = "SystemApplyStatesRequest")
    @JsonPropertyOrder({"sid", "stateNames", "earliestOccurrence", "test"})
    interface ApplyStatesRequest extends SidRequest {

        /**
         * @return the names of the states to apply
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getStateNames();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();

        /**
         * @return whether to run the states in test-only mode
         */
        @Schema(description = "Run states in test-only mode",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTest();
    }

    @Schema(name = "SystemApplyStatesForSystemsRequest")
    @JsonPropertyOrder({"sids", "stateNames", "earliestOccurrence", "test"})
    interface ApplyStatesForSystemsRequest extends SidsRequest {

        /**
         * @return the names of the states to apply
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getStateNames();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();

        /**
         * @return whether to run the states in test-only mode
         */
        @Schema(description = "Run states in test-only mode",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getTest();
    }

    @Schema(name = "SystemUpdatePackageStateRequest")
    @JsonPropertyOrder({"sid", "packageName", "state", "versionConstraint"})
    interface UpdatePackageStateRequest extends SidRequest {

        /**
         * @return the name of the package
         */
        @Schema(description = "Name of the package",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageName();

        /**
         * @return the desired package state
         */
        @Schema(description = "0 = installed, 1 = removed, 2 = unmanaged ",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getState();

        /**
         * @return the version constraint
         */
        @Schema(description = "0 = latest, 1 = any ",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getVersionConstraint();
    }

    @Schema(name = "SystemSetPillarBySystemIdRequest")
    @JsonPropertyOrder({"systemId", "category", "pillarData"})
    interface SetPillarBySystemIdRequest {

        /**
         * @return the id of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();

        /**
         * @return the pillar category
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCategory();

        /**
         * @return the pillar data to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "struct")
        Map<String, Object> getPillarData();
    }

    @Schema(name = "SystemSetPillarByMinionIdRequest")
    @JsonPropertyOrder({"minionId", "category", "pillarData"})
    interface SetPillarByMinionIdRequest {

        /**
         * @return the id of the minion
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getMinionId();

        /**
         * @return the pillar category
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCategory();

        /**
         * @return the pillar data to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "struct")
        Map<String, Object> getPillarData();
    }

    @Schema(name = "SystemRefreshPillarRequest")
    interface RefreshPillarRequest {

        /**
         * @return the ids of the servers
         */
        @Schema(description = "System IDs to be refreshed. If empty, all systems will be " +
                    "refreshed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();
    }

    @Schema(name = "SystemRefreshPillarSubsetRequest")
    @JsonPropertyOrder({"subset", "sids"})
    interface RefreshPillarSubsetRequest extends RefreshPillarRequest {

        /**
         * @return the subset of the pillar to refresh
         */
        @Schema(description = "subset of the pillar to refresh.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSubset();
    }

    @Schema(name = "SystemChangeProxyRequest")
    @JsonPropertyOrder({"sids", "proxyId"})
    interface ChangeProxyRequest extends SidsRequest {

        /**
         * @return the id of the proxy
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyId();
    }

    @Schema(name = "SystemSupportDataUploadRequest")
    @JsonPropertyOrder({"sid", "caseNumber", "parameter", "uploadGeo", "earliestOccurrence"})
    interface SupportDataUploadRequest extends SidRequest {

        /**
         * @return the SCC case number
         */
        @Schema(description = "The SCC case number",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCaseNumber();

        /**
         * @return the additional parameter of the collection tool
         */
        @Schema(description = "Additional parameter for the tool which collect the data " +
                    "from the system. Can be empty",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getParameter();

        /**
         * @return the location of the upload server
         */
        @Schema(description = "The location of the upload server [EU, US]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUploadGeo();

        /**
         * @return the time to schedule the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemVirtualSystemDoc")
    @JsonPropertyOrder({"id", "name", "guestName", "lastCheckin", "uuid"})
    interface VirtualSystemDoc {

        /**
         * @return the id of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the virtual guest name
         */
        @Schema(name = "guest_name",
                description = "the virtual guest name as provided by the virtual host",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getGuestName();

        /**
         * @return the last time the server successfully checked in
         */
        @Schema(name = "last_checkin",
                description = "last time the server successfully checked in",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastCheckin();

        /**
         * @return the UUID of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getUuid();
    }

    @Schema(name = "SystemServerPathDoc")
    @JsonPropertyOrder({"position", "id", "hostname"})
    interface ServerPathDoc {

        /**
         * @return the position of the proxy in the chain
         */
        @Schema(description = "position of proxy in chain. The proxy that the system " +
                    "connects directly to is listed in position 1.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPosition();

        /**
         * @return the id of the proxy system
         */
        @Schema(description = "proxy system ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the hostname of the proxy
         */
        @Schema(description = "proxy host name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();
    }

    @Schema(name = "SystemKickstartVariablesDoc")
    @JsonPropertyOrder({"netboot", "kickstartVariables"})
    interface KickstartVariablesDoc {

        /**
         * @return whether netboot is enabled
         */
        @Schema(description = "netboot enabled", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getNetboot();

        /**
         * @return the kickstart variables of the system
         */
        @Schema(name = "kickstart variables", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "kickstart variable")
        List<KickstartVariableDoc> getKickstartVariables();
    }

    @Schema(name = "SystemDuplicateByIpDoc")
    @JsonPropertyOrder({"ip", "systems"})
    interface DuplicateByIpDoc {

        /**
         * @return the shared IP address
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getIp();

        /**
         * @return the systems sharing the address
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "system")
        List<NetworkDtoDoc> getSystems();
    }

    @Schema(name = "SystemDuplicateByMacDoc")
    @JsonPropertyOrder({"mac", "systems"})
    interface DuplicateByMacDoc {

        /**
         * @return the shared MAC address
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getMac();

        /**
         * @return the systems sharing the address
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "system")
        List<NetworkDtoDoc> getSystems();
    }

    @Schema(name = "SystemDuplicateByHostnameDoc")
    @JsonPropertyOrder({"hostname", "systems"})
    interface DuplicateByHostnameDoc {

        /**
         * @return the shared hostname
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();

        /**
         * @return the systems sharing the hostname
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "system")
        List<NetworkDtoDoc> getSystems();
    }

    @Schema(name = "SystemNetworkDtoDoc")
    @JsonPropertyOrder({"systemId", "systemName", "lastCheckin"})
    interface NetworkDtoDoc {

        /**
         * @return the id of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();

        /**
         * @return the name of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSystemName();

        /**
         * @return the last time the server successfully checked in
         */
        @Schema(name = "last_checkin",
                description = "last time the server successfully checked in",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastCheckin();
    }

    @Schema(name = "SystemCurrencyScoreDoc")
    @JsonPropertyOrder({"sid", "crit", "imp", "mod", "low", "bug", "enh", "score"})
    interface CurrencyScoreDoc {

        /**
         * @return the id of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the number of critical security errata
         */
        @Schema(description = "critical security errata count",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getCrit();

        /**
         * @return the number of important security errata
         */
        @Schema(description = "important security errata count",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getImp();

        /**
         * @return the number of moderate security errata
         */
        @Schema(description = "moderate security errata count",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getMod();

        /**
         * @return the number of low security errata
         */
        @Schema(description = "low security errata count",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getLow();

        /**
         * @return the number of bug fix errata
         */
        @Schema(description = "bug fix errata count",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getBug();

        /**
         * @return the number of enhancement errata
         */
        @Schema(description = "enhancement errata count",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getEnh();

        /**
         * @return the system currency score
         */
        @Schema(description = "system currency score",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getScore();
    }

    @Schema(name = "SystemExtraPackagesSystemDoc")
    @JsonPropertyOrder({"id", "name", "extraPkgCount"})
    interface ExtraPackagesSystemDoc {

        /**
         * @return the id of the system
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the profile name of the system
         */
        @Schema(description = "System profile name",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the number of extra packages
         */
        @Schema(name = "extra_pkg_count", description = "Extra packages count",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getExtraPkgCount();
    }

    @Schema(name = "SystemExtraPackageDoc")
    @JsonPropertyOrder({"name", "version", "release", "epoch", "arch", "installtime"})
    interface ExtraPackageDoc {

        /**
         * @return the name of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the version of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the release of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the epoch of the package
         */
        @Schema(description = "returned only if non-zero",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

        /**
         * @return the architecture of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the installation time of the package
         */
        @Schema(description = "returned only if known",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "date")
        Date getInstalltime();
    }

    @Schema(name = "SystemOsaPingDoc")
    @JsonPropertyOrder({"state", "lastMessageTime", "lastPingTime"})
    interface OsaPingDoc {

        /**
         * @return the state of the system
         */
        @Schema(description = "state of the system (unknown, online, offline)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getState();

        /**
         * @return the time of the last received response
         */
        @Schema(description = "time of the last received response (1970/01/01 00:00:00 if " +
                    "never received a response)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastMessageTime();

        /**
         * @return the time of the last sent ping
         */
        @Schema(description = "time of the last sent ping (1970/01/01 00:00:00 if no ping " +
                    "is pending",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastPingTime();
    }

    @Schema(name = "SystemMigrationTargetDoc")
    @JsonPropertyOrder({"ident", "friendly"})
    interface MigrationTargetDoc {

        /**
         * @return the identifier of the target
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getIdent();

        /**
         * @return the friendly name of the target
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFriendly();
    }

    @Schema(name = "SystemSuggestedRebootDoc")
    @JsonPropertyOrder({"id", "name"})
    interface SuggestedRebootDoc {

        /**
         * @return the id of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the system
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "SystemInstalledProductDoc")
    @JsonPropertyOrder({"name", "isBaseProduct", "version", "arch", "release", "friendlyName"})
    interface InstalledProductDoc {

        /**
         * @return the name of the product
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return whether this is the base product
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getIsBaseProduct();

        /**
         * @return the version of the product
         */
        @Schema(description = "returned only if applies",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the architecture of the product
         */
        @Schema(description = "returned only if applies",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the release of the product
         */
        @Schema(description = "returned only if applies",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the friendly name of the product
         */
        @Schema(description = "returned only if available",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFriendlyName();
    }

    @Schema(name = "SystemPackageStateDoc")
    @JsonPropertyOrder({"id", "name", "stateRevisionId", "packageStateTypeId",
        "versionConstraintId"})
    interface PackageStateDoc {

        /**
         * @return the id of the package state
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the package
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the id of the state revision
         */
        @Schema(name = "state_revision_id", description = "state revision ID",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getStateRevisionId();

        /**
         * @return the package state type
         */
        @Schema(name = "package_state_type_id", description = "'INSTALLED' or 'REMOVED'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPackageStateTypeId();

        /**
         * @return the version constraint
         */
        @Schema(name = "version_constraint_id", description = "'LATEST' or 'ANY'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersionConstraintId();
    }

    @Schema(name = "SystemGroupMembershipDoc")
    @JsonPropertyOrder({"id", "systemGroups"})
    interface SystemGroupMembershipDoc {

        /**
         * @return the id of the system
         */
        @Schema(description = "system ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the groups the system belongs to
         */
        @Schema(name = "system_groups", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "system group")
        List<SystemGroupIdDoc> getSystemGroups();
    }

    @Schema(name = "SystemGroupIdDoc")
    @JsonPropertyOrder({"id", "name"})
    interface SystemGroupIdDoc {

        /**
         * @return the id of the system group
         */
        @Schema(description = "system group ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the system group
         */
        @Schema(description = "system group name",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "SystemCoCoAttestationReportDoc")
    @JsonPropertyOrder({"reportId", "reportStatus", "reportCreated", "reportModified",
        "results"})
    interface CoCoAttestationReportDoc {

        /**
         * @return the id of the report
         */
        @Schema(name = "report_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getReportId();

        /**
         * @return the status of the report
         */
        @Schema(name = "report_status", requiredMode = Schema.RequiredMode.REQUIRED)
        String getReportStatus();

        /**
         * @return the creation time of the report
         */
        @Schema(name = "report_created", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getReportCreated();

        /**
         * @return the modification time of the report
         */
        @Schema(name = "report_modified", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getReportModified();

        /**
         * @return the results of the report
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "result")
        List<CoCoAttestationReportResultDoc> getResults();
    }

    @Schema(name = "SystemCoCoAttestationReportResultDoc")
    @JsonPropertyOrder({"resultId", "resultType", "resultStatus", "resultDescription",
        "resultAttested"})
    interface CoCoAttestationReportResultDoc {

        /**
         * @return the id of the result
         */
        @Schema(name = "result_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getResultId();

        /**
         * @return the type of the result
         */
        @Schema(name = "result_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getResultType();

        /**
         * @return the status of the result
         */
        @Schema(name = "result_status", requiredMode = Schema.RequiredMode.REQUIRED)
        String getResultStatus();

        /**
         * @return the description of the result
         */
        @Schema(name = "result_description", requiredMode = Schema.RequiredMode.REQUIRED)
        String getResultDescription();

        /**
         * @return the attestation time of the result
         */
        @Schema(name = "result_attested", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getResultAttested();
    }

    @Schema(name = "SystemCoCoAttestationResultDoc")
    @JsonPropertyOrder({"resultId", "resultType", "resultStatus", "resultDescription",
        "resultAttested", "resultDetails"})
    interface CoCoAttestationResultDoc extends CoCoAttestationReportResultDoc {

        /**
         * @return the details of the result
         */
        @Schema(name = "result_details", requiredMode = Schema.RequiredMode.REQUIRED)
        String getResultDetails();
    }

    @Schema(name = "SystemMigrationTargetWithChannelsDoc")
    @JsonPropertyOrder({"ident", "friendly", "channelOptions"})
    interface MigrationTargetWithChannelsDoc {

        /**
         * @return the identifier of the target product set
         */
        @Schema(description = "Product IDs for the target product set ( e.g. " +
                    "'[1894, 1905]')",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getIdent();

        /**
         * @return the friendly name of the target product set
         */
        @Schema(description = "Friendly name of the target product set",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFriendly();

        /**
         * @return the channel options of the target
         */
        @Schema(name = "channel_options", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "channel option")
        List<MigrationChannelOptionDoc> getChannelOptions();
    }

    @Schema(name = "SystemMigrationChannelOptionDoc")
    @JsonPropertyOrder({"baseChannelLabel", "baseChannelName", "childChannels"})
    interface MigrationChannelOptionDoc {

        /**
         * @return the label of the base channel
         */
        @Schema(name = "base_channel_label", description = "Label of the base channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseChannelLabel();

        /**
         * @return the name of the base channel
         */
        @Schema(name = "base_channel_name", description = "Name of the base channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseChannelName();

        /**
         * @return the child channels of the option
         */
        @Schema(name = "child_channels", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "child channel")
        List<MigrationChildChannelDoc> getChildChannels();
    }

    @Schema(name = "SystemMigrationChildChannelDoc")
    @JsonPropertyOrder({"label", "name", "mandatory"})
    interface MigrationChildChannelDoc {

        /**
         * @return the label of the channel
         */
        @Schema(description = "Channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the channel
         */
        @Schema(description = "Channel name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return whether the channel is mandatory
         */
        @Schema(description = "Whether the channel is mandatory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getMandatory();
    }
}
