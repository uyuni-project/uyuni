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
package com.redhat.rhn.frontend.xmlrpc.channel.software;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.ContentSourceFilter;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandlerApi;
import com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandlerApi;
import com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandlerApi;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ChannelSoftwareHandler}.
 */
@Tag(name = "channel.software",
    description = "Provides methods to access and modify many aspects of a channel.")
public interface ChannelSoftwareHandlerApi {

    /**
     * Lists the errata that would be updated by a channel synchronisation.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to update
     * @return the errata needing synchronisation
     */
    @ApiEndpointDoc(
        summary = "If you have synced a new channel then patches will have been " +
            "updated with the packages that are in the newly synced channel. A cloned erratum " +
            "will not have been automatically updated however. If you cloned a channel that " +
            "includes those cloned errata and should include the new packages, they will not be " +
            "included when they should. This method lists the errata that will be updated if you " +
            "run the syncErrata method.",
        method = HttpMethod.get,
        responseClass = ErrataOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<ErrataOverview> listErrataNeedingSync(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to update") String channelLabel);

    /**
     * Updates the cloned errata of a channel with the packages recently added to it.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to update
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "If you have synced a new channel then patches will have been " +
            "updated with the packages that are in the newly synced channel. A cloned erratum " +
            "will not have been automatically updated however. If you cloned a channel that " +
            "includes those cloned errata and should include the new packages, they will not be " +
            "included when they should. This method updates all the errata in the given cloned " +
            "channel with packages that have recently been added, and ensures that all the " +
            "packages you expect are in the channel. It also updates cloned errata attributes " +
            "like advisoryStatus.",
        requestClass = SyncErrataRequest.class,
        isIntegerResponse = true
    )
    Integer syncErrata(User loggedInUser, String channelLabel);

    /**
     * Lists the latest packages of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @return the latest packages of the channel
     */
    @ApiEndpointDoc(
        summary = "Lists the packages with the latest version (including release and epoch) for " +
            "the given channel",
        method = HttpMethod.get,
        responseClass = LatestPackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    Object[] listLatestPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel);

    /**
     * Lists all packages of a channel modified between two dates.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @param startDate the start date
     * @param endDate the end date
     * @return the packages of the channel
     */
    @ApiEndpointDoc(
        summary = "Lists all packages in the channel, regardless of package version, between " +
            "the given dates.",
        method = HttpMethod.get,
        responseClass = PackageDtoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<PackageDto> listAllPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel,
        @Parameter(name = "startDate", in = ParameterIn.QUERY, required = true) Date startDate,
        @Parameter(name = "endDate", in = ParameterIn.QUERY, required = true) Date endDate);

    /**
     * Lists all packages of a channel modified after a date.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @param startDate the start date
     * @return the packages of the channel
     */
    @ApiEndpointDoc(
        summary = "Lists all packages in the channel, regardless of version whose last modified " +
            "date is greater than given date.",
        method = HttpMethod.get,
        responseClass = PackageDtoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<PackageDto> listAllPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel,
        @Parameter(name = "startDate", in = ParameterIn.QUERY, required = true) Date startDate);

    /**
     * Lists all packages of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @return the packages of the channel
     */
    @ApiEndpointDoc(
        summary = "Lists all packages in the channel, regardless of the package version",
        method = HttpMethod.get,
        responseClass = PackageDtoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<PackageDto> listAllPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel);

    /**
     * Lists the software channel architectures.
     *
     * @param loggedInUser the current user
     * @return the software channel architectures
     */
    @ApiEndpointDoc(
        summary = "Lists the potential software channel architectures that can be created",
        method = HttpMethod.get,
        responseClass = ChannelArchListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel arch")
    )
    List<ChannelArch> listArches(@Parameter(hidden = true) User loggedInUser);

    /**
     * Deletes a custom software channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes a custom software channel",
        requestClass = DeleteRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String channelLabel);

    /**
     * Returns whether a channel is globally subscribable.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @return 1 if subscribable, 0 otherwise
     */
    @ApiEndpointDoc(
        summary = "Returns whether the channel is subscribable by any user in the organization",
        method = HttpMethod.get,
        isIntegerResponse = true,
        responseDescription = "1 if true, 0 otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "subscribable")
    )
    int isGloballySubscribable(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel);

    /**
     * Returns the details of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @return the details of the channel
     */
    @ApiEndpointDoc(
        summary = "Returns details of the given channel as a map",
        method = HttpMethod.get,
        responseClass = SoftwareChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Channel getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel);

    /**
     * Returns the details of a channel.
     *
     * @param loggedInUser the current user
     * @param id the id of the channel to query
     * @return the details of the channel
     */
    @ApiEndpointDoc(
        summary = "Returns details of the given channel as a map",
        method = HttpMethod.get,
        responseClass = SoftwareChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Channel getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "id", in = ParameterIn.QUERY, required = true,
                description = "channel to query") Integer id);

    /**
     * Modifies the attributes of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @param details the attributes to modify
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Allows to modify channel attributes",
        requestClass = SetDetailsRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, String channelLabel, Map<String, String> details);

    /**
     * Modifies the attributes of a channel.
     *
     * @param loggedInUser the current user
     * @param channelId the channel id
     * @param details the attributes to modify
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Allows to modify channel attributes",
        requestClass = SetDetailsByIdRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, Integer channelId, Map<String, String> details);

    /**
     * Creates a software channel.
     *
     * @param loggedInUser the current user
     * @param label the label of the new channel
     * @param name the name of the new channel
     * @param summary the summary of the new channel
     * @param archLabel the architecture of the new channel
     * @param parentLabel the label of the parent channel
     * @param checksumType the checksum type of the new channel
     * @param gpgKey the GPG key of the new channel
     * @param gpgCheck whether the GPG check is enabled by default
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Creates a software channel",
        requestClass = CreateWithGpgCheckRequest.class,
        isIntegerResponse = true,
        responseDescription = "1 if the creation operation succeeded, 0 otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int create(User loggedInUser, String label, String name, String summary, String archLabel,
        String parentLabel, String checksumType, Map<String, String> gpgKey, boolean gpgCheck);

    /**
     * Creates a software channel.
     *
     * @param loggedInUser the current user
     * @param label the label of the new channel
     * @param name the name of the new channel
     * @param summary the summary of the new channel
     * @param archLabel the architecture of the new channel
     * @param parentLabel the label of the parent channel
     * @param checksumType the checksum type of the new channel
     * @param gpgKey the GPG key of the new channel
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Creates a software channel",
        requestClass = CreateWithGpgKeyRequest.class,
        isIntegerResponse = true,
        responseDescription = "1 if the creation operation succeeded, 0 otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int create(User loggedInUser, String label, String name, String summary, String archLabel,
        String parentLabel, String checksumType, Map<String, String> gpgKey);

    /**
     * Creates a software channel.
     *
     * @param loggedInUser the current user
     * @param label the label of the new channel
     * @param name the name of the new channel
     * @param summary the summary of the new channel
     * @param archLabel the architecture of the new channel
     * @param parentLabel the label of the parent channel
     * @param checksumType the checksum type of the new channel
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Creates a software channel",
        requestClass = CreateWithChecksumRequest.class,
        isIntegerResponse = true,
        responseDescription = "1 if the creation operation succeeded, 0 otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int create(User loggedInUser, String label, String name, String summary, String archLabel,
        String parentLabel, String checksumType);

    /**
     * Creates a software channel.
     *
     * @param loggedInUser the current user
     * @param label the label of the new channel
     * @param name the name of the new channel
     * @param summary the summary of the new channel
     * @param archLabel the architecture of the new channel
     * @param parentLabel the label of the parent channel
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Creates a software channel",
        requestClass = CreateRequest.class,
        isIntegerResponse = true,
        responseDescription = "1 if the creation operation succeeded, 0 otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int create(User loggedInUser, String label, String name, String summary, String archLabel,
        String parentLabel);

    /**
     * Sets the contact details of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @param maintainerName the name of the channel maintainer
     * @param maintainerEmail the email of the channel maintainer
     * @param maintainerPhone the phone number of the channel maintainer
     * @param supportPolicy the channel support policy
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set contact/support information for given channel.",
        requestClass = SetContactDetailsRequest.class,
        isIntegerResponse = true
    )
    int setContactDetails(User loggedInUser, String channelLabel, String maintainerName,
        String maintainerEmail, String maintainerPhone, String supportPolicy);

    /**
     * Lists the systems subscribed to a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @return the systems subscribed to the channel
     */
    @ApiEndpointDoc(
        summary = "Returns list of subscribed systems for the given channel label",
        method = HttpMethod.get,
        responseClass = SubscribedSystemListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listSubscribedSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel);

    /**
     * Lists the channels a system is subscribed to.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return the channels the system is subscribed to
     */
    @ApiEndpointDoc(
        summary = "Returns a list of channels that a system is subscribed to for the given " +
            "system id",
        method = HttpMethod.get,
        responseClass = SystemChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Object[] listSystemChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true,
                description = "system ID") Integer sid);

    /**
     * Sets the subscribable flag of a channel for a user.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @param login the login of the target user
     * @param value the value of the flag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the subscribable flag for a given channel and user. If value is set to " +
            "'true', this method will give the user subscribe permissions to the channel. " +
            "Otherwise, that privilege is revoked.",
        requestClass = SetUserSubscribableRequest.class,
        isIntegerResponse = true
    )
    int setUserSubscribable(User loggedInUser, String channelLabel, String login, Boolean value);

    /**
     * Sets the manageable flag of a channel for a user.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @param login the login of the target user
     * @param value the value of the flag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the manageable flag for a given channel and user. If value is set to " +
            "'true', this method will give the user manage permissions to the channel. " +
            "Otherwise, that privilege is revoked.",
        requestClass = SetUserManageableRequest.class,
        isIntegerResponse = true
    )
    int setUserManageable(User loggedInUser, String channelLabel, String login, Boolean value);

    /**
     * Returns whether a channel may be subscribed to by a user.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @param login the login of the target user
     * @return 1 if subscribable, 0 if not
     */
    @ApiEndpointDoc(
        summary = "Returns whether the channel may be subscribed to by the given user.",
        method = HttpMethod.get,
        isIntegerResponse = true,
        responseDescription = "1 if subscribable, 0 if not",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int isUserSubscribable(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "label of the channel") String channelLabel,
        @Parameter(name = "login", in = ParameterIn.QUERY, required = true,
                description = "login of the target user") String login);

    /**
     * Returns whether a channel exists.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @return true if the channel exists
     */
    @ApiEndpointDoc(
        summary = "Returns whether is existing",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "true if the channel exists",
        legacyDocResponse = @LegacyDocResponse(name = "result")
    )
    boolean isExisting(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "label of the channel") String channelLabel);

    /**
     * Returns whether a channel may be managed by a user.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @param login the login of the target user
     * @return 1 if manageable, 0 if not
     */
    @ApiEndpointDoc(
        summary = "Returns whether the channel may be managed by the given user.",
        method = HttpMethod.get,
        isIntegerResponse = true,
        responseDescription = "1 if manageable, 0 if not",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    int isUserManageable(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "label of the channel") String channelLabel,
        @Parameter(name = "login", in = ParameterIn.QUERY, required = true,
                description = "login of the target user") String login);

    /**
     * Sets the globally subscribable attribute of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @param value whether the channel is globally subscribable
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set globally subscribable attribute for given channel.",
        requestClass = SetGloballySubscribableRequest.class,
        isIntegerResponse = true
    )
    int setGloballySubscribable(User loggedInUser, String channelLabel, boolean value);

    /**
     * Adds packages to a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the target channel
     * @param packageIds the ids of the packages to add
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Adds a given list of packages to the given channel.",
        requestClass = AddPackagesRequest.class,
        isIntegerResponse = true
    )
    int addPackages(User loggedInUser, String channelLabel, List<Long> packageIds);

    /**
     * Removes errata from a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the target channel
     * @param errataNames the names of the errata to remove
     * @param removePackages whether to remove packages from the channel
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes a given list of errata from the given channel.",
        requestClass = RemoveErrataRequest.class,
        isIntegerResponse = true
    )
    int removeErrata(User loggedInUser, String channelLabel, List<String> errataNames,
        boolean removePackages);

    /**
     * Removes packages from a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the target channel
     * @param packageIds the ids of the packages to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes a given list of packages from the given channel.",
        requestClass = RemovePackagesRequest.class,
        isIntegerResponse = true
    )
    int removePackages(User loggedInUser, String channelLabel, List<Long> packageIds);

    /**
     * Lists the errata applicable to a channel after a date.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @param startDate the start date
     * @return the errata applicable to the channel
     */
    @ApiEndpointDoc(
        summary = "List the errata applicable to a channel after given startDate",
        method = HttpMethod.get,
        responseClass = ErrataOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<ErrataOverview> listErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel,
        @Parameter(name = "startDate", in = ParameterIn.QUERY, required = true) Date startDate);

    /**
     * Lists the errata applicable to a channel between two dates.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @param startDate the start date
     * @param endDate the end date
     * @return the errata applicable to the channel
     */
    @ApiEndpointDoc(
        summary = "List the errata applicable to a channel between startDate and endDate.",
        method = HttpMethod.get,
        responseClass = ErrataOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<ErrataOverview> listErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel,
        @Parameter(name = "startDate", in = ParameterIn.QUERY, required = true) Date startDate,
        @Parameter(name = "endDate", in = ParameterIn.QUERY, required = true) Date endDate);

    /**
     * Lists the errata applicable to a channel between two dates.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @param startDate the start date
     * @param endDate the end date
     * @param lastModified whether to select by last modified date
     * @return the errata applicable to the channel
     */
    @ApiEndpointDoc(
        summary = "List the errata applicable to a channel between startDate and endDate.",
        method = HttpMethod.get,
        responseClass = ErrataOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<ErrataOverview> listErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel,
        @Parameter(name = "startDate", in = ParameterIn.QUERY, required = true) Date startDate,
        @Parameter(name = "endDate", in = ParameterIn.QUERY, required = true) Date endDate,
        @Parameter(name = "lastModified", in = ParameterIn.QUERY, required = true,
                description = "select by last modified or not") boolean lastModified);

    /**
     * Lists the errata applicable to a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @return the errata applicable to the channel
     */
    @ApiEndpointDoc(
        summary = "List the errata applicable to a channel",
        method = HttpMethod.get,
        responseClass = ErrataOverviewListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<ErrataOverview> listErrata(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel);

    /**
     * Lists the errata of a given type that are applicable to a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel to query
     * @param advisoryType the type of advisory
     * @return the errata applicable to the channel
     */
    @ApiEndpointDoc(
        summary = "List the errata of a specific type that are applicable to a channel",
        method = HttpMethod.get,
        responseClass = ErrataByTypeListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Object[] listErrataByType(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel to query") String channelLabel,
        @Parameter(name = "advisoryType", in = ParameterIn.QUERY, required = true,
                description = "type of advisory (one of of the following: 'Security Advisory', " +
                    "'Product Enhancement Advisory', 'Bug Fix Advisory'") String advisoryType);

    /**
     * Lists the packages that are not associated with a channel.
     *
     * @param loggedInUser the current user
     * @return the packages without a channel
     */
    @ApiEndpointDoc(
        summary = "Lists all packages that are not associated with a channel. Typically these " +
            "are custom packages.",
        method = HttpMethod.get,
        responseClass = PackagesHandlerApi.PackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    Object[] listPackagesWithoutChannel(@Parameter(hidden = true) User loggedInUser);

    /**
     * Clones a channel.
     *
     * @param loggedInUser the current user
     * @param originalLabel the label of the channel to clone
     * @param channelDetails the details of the cloned channel
     * @param originalState whether to clone the original state
     * @return the id of the cloned channel
     */
    @ApiEndpointDoc(
        summary = "Clone a channel. If arch_label is omitted, the arch label of the original " +
            "channel will be used. If parent_label is omitted, the clone will be a base channel.",
        requestClass = CloneRequest.class,
        isIntegerResponse = true,
        responseDescription = "the cloned channel ID",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int clone(User loggedInUser, String originalLabel, Map<String, String> channelDetails,
        Boolean originalState);

    /**
     * Merges all errata from one channel into another.
     *
     * @param loggedInUser the current user
     * @param mergeFromLabel the channel to pull the errata from
     * @param mergeToLabel the channel to push the errata into
     * @return the merged errata
     */
    @ApiEndpointDoc(
        summary = "Merges all errata from one channel into another. It does not associate the " +
            "packages with the channel.",
        requestClass = MergeErrataRequest.class,
        responseClass = ErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Object[] mergeErrata(User loggedInUser, String mergeFromLabel, String mergeToLabel);

    /**
     * Merges the errata of a period from one channel into another.
     *
     * @param loggedInUser the current user
     * @param mergeFromLabel the channel to pull the errata from
     * @param mergeToLabel the channel to push the errata into
     * @param startDate the start date
     * @param endDate the end date
     * @return the merged errata
     */
    @ApiEndpointDoc(
        summary = "Merges all errata from one channel into another based upon a given start/end " +
            "date. It does not associate the packages with the channel.",
        requestClass = MergeErrataByDateRequest.class,
        responseClass = ErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Object[] mergeErrata(User loggedInUser, String mergeFromLabel, String mergeToLabel,
        String startDate, String endDate);

    /**
     * Merges a list of errata from one channel into another.
     *
     * @param loggedInUser the current user
     * @param mergeFromLabel the channel to pull the errata from
     * @param mergeToLabel the channel to push the errata into
     * @param errataNames the advisory names of the errata to merge
     * @return the merged errata
     */
    @ApiEndpointDoc(
        summary = "Merges a list of errata from one channel into another. It does not associate " +
            "the packages with the channel.",
        requestClass = MergeErrataByNameRequest.class,
        responseClass = ErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Object[] mergeErrata(User loggedInUser, String mergeFromLabel, String mergeToLabel,
        List<String> errataNames);

    /**
     * Merges all packages from one channel into another.
     *
     * @param loggedInUser the current user
     * @param mergeFromLabel the channel to pull the packages from
     * @param mergeToLabel the channel to push the packages into
     * @return the merged packages
     */
    @ApiEndpointDoc(
        summary = "Merges all packages from one channel into another",
        requestClass = MergePackagesRequest.class,
        responseClass = PackagesHandlerApi.PackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    Object[] mergePackages(User loggedInUser, String mergeFromLabel, String mergeToLabel);

    /**
     * Merges all packages from one channel into another.
     *
     * @param loggedInUser the current user
     * @param mergeFromLabel the channel to pull the packages from
     * @param mergeToLabel the channel to push the packages into
     * @param alignModules whether to align the modular data
     * @return the merged packages
     */
    @ApiEndpointDoc(
        summary = "Merges all packages from one channel into another",
        requestClass = MergePackagesAlignedRequest.class,
        responseClass = PackagesHandlerApi.PackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    Object[] mergePackages(User loggedInUser, String mergeFromLabel, String mergeToLabel,
        boolean alignModules);

    /**
     * Aligns the metadata of a channel to another channel.
     *
     * @param loggedInUser the current user
     * @param channelFromLabel the label of the source channel
     * @param channelToLabel the label of the target channel
     * @param metadataType the metadata type
     * @return 1 when the metadata has been aligned, 0 otherwise
     */
    @ApiEndpointDoc(
        summary = "Align the metadata of a channel to another channel.",
        requestClass = AlignMetadataRequest.class,
        isIntegerResponse = true,
        responseDescription = "1 when metadata has been aligned, 0 otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "result code")
    )
    int alignMetadata(User loggedInUser, String channelFromLabel, String channelToLabel,
        String metadataType);

    /**
     * Regenerates the needed cache of the systems subscribed to a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Completely clear and regenerate the needed Errata and Package cache for all " +
            "systems subscribed to the specified channel. This should be used only if you " +
            "believe your cache is incorrect for all the systems in a given channel. This will " +
            "schedule an asynchronous action to actually do the processing.",
        requestClass = RegenerateNeededCacheRequest.class,
        isIntegerResponse = true
    )
    int regenerateNeededCache(User loggedInUser, String channelLabel);

    /**
     * Regenerates the needed cache of all the subscribed systems.
     *
     * @param loggedInUser the current user
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Completely clear and regenerate the needed Errata and Package cache for all " +
            "systems subscribed. You must be a #product() Admin to perform this action. This will " +
            "schedule an asynchronous action to actually do the processing.",
        isIntegerResponse = true
    )
    int regenerateNeededCache(User loggedInUser);

    /**
     * Regenerates the yum cache of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @param force whether to force the cache regeneration
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Regenerate yum cache for the specified channel.",
        requestClass = RegenerateYumCacheRequest.class,
        isIntegerResponse = true
    )
    int regenerateYumCache(User loggedInUser, String channelLabel, Boolean force);

    /**
     * Lists the children of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @return the children of the channel
     */
    @ApiEndpointDoc(
        summary = "List the children of a channel",
        method = HttpMethod.get,
        responseClass = ChannelAppStreamHandlerApi.ChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Object[] listChildren(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "the label of the channel") String channelLabel);

    /**
     * Returns the last build date of the repository metadata of a channel.
     *
     * @param loggedInUser the current user
     * @param id the id of the channel
     * @return the last build date of the repository metadata
     */
    @ApiEndpointDoc(
        summary = "Returns the last build date of the repomd.xml file for the given channel as " +
            "a localised string.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        responseDescription = "the last build date of the repomd.xml file as a localised string",
        legacyDocResponse = @LegacyDocResponse(type = "date", name = "date")
    )
    String getChannelLastBuildById(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "id", in = ParameterIn.QUERY, required = true,
                description = "id of channel wanted") Integer id);

    /**
     * Lists the repositories the user can see.
     *
     * @param loggedInUser the current user
     * @return the repositories the user can see
     */
    @ApiEndpointDoc(
        summary = "Returns a list of ContentSource (repos) that the user can see",
        method = HttpMethod.get,
        responseClass = UserRepoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "map")
    )
    List<Map<String, Object>> listUserRepos(@Parameter(hidden = true) User loggedInUser);

    /**
     * Creates a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param type the repository type
     * @param url the repository url
     * @return the created repository
     */
    @ApiEndpointDoc(
        summary = "Creates a repository",
        requestClass = CreateRepoRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource createRepo(User loggedInUser, String label, String type, String url);

    /**
     * Creates a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param type the repository type
     * @param url the repository url
     * @param sslCaCert the SSL CA certificate description
     * @param sslCliCert the SSL client certificate description
     * @param sslCliKey the SSL client key description
     * @return the created repository
     */
    @ApiEndpointDoc(
        summary = "Creates a repository",
        requestClass = CreateRepoWithSslRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource createRepo(User loggedInUser, String label, String type, String url,
        String sslCaCert, String sslCliCert, String sslCliKey);

    /**
     * Creates a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param type the repository type
     * @param url the repository url
     * @param sslCaCert the SSL CA certificate description
     * @param sslCliCert the SSL client certificate description
     * @param sslCliKey the SSL client key description
     * @param hasSignedMetadata whether the repository has signed metadata
     * @return the created repository
     */
    @ApiEndpointDoc(
        summary = "Creates a repository",
        requestClass = CreateRepoWithMetadataRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource createRepo(User loggedInUser, String label, String type, String url,
        String sslCaCert, String sslCliCert, String sslCliKey, boolean hasSignedMetadata);

    /**
     * Removes a repository.
     *
     * @param loggedInUser the current user
     * @param id the id of the repository to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes a repository",
        requestClass = RemoveRepoByIdRequest.class,
        isIntegerResponse = true
    )
    Integer removeRepo(User loggedInUser, Integer id);

    /**
     * Removes a repository.
     *
     * @param loggedInUser the current user
     * @param label the label of the repository to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes a repository",
        requestClass = RemoveRepoByLabelRequest.class,
        isIntegerResponse = true
    )
    Integer removeRepo(User loggedInUser, String label);

    /**
     * Associates a repository with a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @param repoLabel the repository label
     * @return the channel the repository was associated with
     */
    @ApiEndpointDoc(
        summary = "Associates a repository with a channel",
        requestClass = AssociateRepoRequest.class,
        responseClass = SoftwareChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Channel associateRepo(User loggedInUser, String channelLabel, String repoLabel);

    /**
     * Disassociates a repository from a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @param repoLabel the repository label
     * @return the channel the repository was disassociated from
     */
    @ApiEndpointDoc(
        summary = "Disassociates a repository from a channel",
        requestClass = DisassociateRepoRequest.class,
        responseClass = SoftwareChannelResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Channel disassociateRepo(User loggedInUser, String channelLabel, String repoLabel);

    /**
     * Updates the source url of a repository.
     *
     * @param loggedInUser the current user
     * @param id the repository id
     * @param url the new repository url
     * @return the updated repository
     */
    @ApiEndpointDoc(
        summary = "Updates repository source URL",
        requestClass = UpdateRepoUrlByIdRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource updateRepoUrl(User loggedInUser, Integer id, String url);

    /**
     * Updates the source url of a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param url the new repository url
     * @return the updated repository
     */
    @ApiEndpointDoc(
        summary = "Updates repository source URL",
        requestClass = UpdateRepoUrlByLabelRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource updateRepoUrl(User loggedInUser, String label, String url);

    /**
     * Updates the SSL certificates of a repository.
     *
     * @param loggedInUser the current user
     * @param id the repository id
     * @param sslCaCert the SSL CA certificate description
     * @param sslCliCert the SSL client certificate description
     * @param sslCliKey the SSL client key description
     * @return the updated repository
     */
    @ApiEndpointDoc(
        summary = "Updates repository SSL certificates",
        requestClass = UpdateRepoSslByIdRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource updateRepoSsl(User loggedInUser, Integer id, String sslCaCert, String sslCliCert,
        String sslCliKey);

    /**
     * Updates the SSL certificates of a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param sslCaCert the SSL CA certificate description
     * @param sslCliCert the SSL client certificate description
     * @param sslCliKey the SSL client key description
     * @return the updated repository
     */
    @ApiEndpointDoc(
        summary = "Updates repository SSL certificates",
        requestClass = UpdateRepoSslByLabelRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource updateRepoSsl(User loggedInUser, String label, String sslCaCert,
        String sslCliCert, String sslCliKey);

    /**
     * Updates the label of a repository.
     *
     * @param loggedInUser the current user
     * @param id the repository id
     * @param label the new repository label
     * @return the updated repository
     */
    @ApiEndpointDoc(
        summary = "Updates repository label",
        requestClass = UpdateRepoLabelByIdRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource updateRepoLabel(User loggedInUser, Integer id, String label);

    /**
     * Updates the label of a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param newLabel the new repository label
     * @return the updated repository
     */
    @ApiEndpointDoc(
        summary = "Updates repository label",
        requestClass = UpdateRepoLabelByLabelRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource updateRepoLabel(User loggedInUser, String label, String newLabel);

    /**
     * Updates a repository.
     *
     * @param loggedInUser the current user
     * @param id the repository id
     * @param label the new repository label
     * @param url the new repository url
     * @return the updated repository
     */
    @ApiEndpointDoc(
        summary = "Updates a ContentSource (repo)",
        requestClass = UpdateRepoRequest.class,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource updateRepo(User loggedInUser, Integer id, String label, String url);

    /**
     * Returns the details of a repository.
     *
     * @param loggedInUser the current user
     * @param repoLabel the repository to query
     * @return the details of the repository
     */
    @ApiEndpointDoc(
        summary = "Returns details of the given repository",
        method = HttpMethod.get,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource getRepoDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "repoLabel", in = ParameterIn.QUERY, required = true,
                description = "repo to query") String repoLabel);

    /**
     * Returns the details of a repository.
     *
     * @param loggedInUser the current user
     * @param id the repository id
     * @return the details of the repository
     */
    @ApiEndpointDoc(
        summary = "Returns details of the given repository",
        method = HttpMethod.get,
        responseClass = ContentSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    ContentSource getRepoDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "id", in = ParameterIn.QUERY, required = true,
                description = "repository ID") Integer id);

    /**
     * Lists the repositories associated with a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @return the repositories associated with the channel
     */
    @ApiEndpointDoc(
        summary = "Lists associated repos with the given channel",
        method = HttpMethod.get,
        responseClass = ContentSourceListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    List<ContentSource> listChannelRepos(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel label") String channelLabel);

    /**
     * Triggers an immediate repository synchronisation of several channels.
     *
     * @param loggedInUser the current user
     * @param channelLabels the channel labels
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Trigger immediate repo synchronization",
        requestClass = SyncRepoListRequest.class,
        isIntegerResponse = true
    )
    int syncRepo(User loggedInUser, List<String> channelLabels);

    /**
     * Triggers an immediate repository synchronisation.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Trigger immediate repo synchronization",
        requestClass = SyncRepoRequest.class,
        isIntegerResponse = true
    )
    int syncRepo(User loggedInUser, String channelLabel);

    /**
     * Triggers an immediate repository synchronisation.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @param params the synchronisation parameters
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Trigger immediate repo synchronization",
        requestClass = SyncRepoWithParamsRequest.class,
        isIntegerResponse = true
    )
    int syncRepo(User loggedInUser, String channelLabel, Map<String, String> params);

    /**
     * Schedules a periodic repository synchronisation.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @param cronExpr the cron expression
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedule periodic repo synchronization",
        requestClass = SyncRepoScheduleRequest.class,
        isIntegerResponse = true
    )
    int syncRepo(User loggedInUser, String channelLabel, String cronExpr);

    /**
     * Schedules a periodic repository synchronisation.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @param cronExpr the cron expression
     * @param params the synchronisation parameters
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedule periodic repo synchronization",
        requestClass = SyncRepoScheduleWithParamsRequest.class,
        isIntegerResponse = true
    )
    int syncRepo(User loggedInUser, String channelLabel, String cronExpr,
        Map<String, String> params);

    /**
     * Returns the cron expression of the repository synchronisation of a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the channel label
     * @return the cron expression
     */
    @ApiEndpointDoc(
        summary = "Returns repo synchronization cron expression",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        responseDescription = "quartz expression",
        legacyDocResponse = @LegacyDocResponse(name = "expression")
    )
    String getRepoSyncCronExpression(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "channel label") String channelLabel);

    /**
     * Lists the filters of a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @return the filters of the repository
     */
    @ApiEndpointDoc(
        summary = "Lists the filters for a repo",
        method = HttpMethod.get,
        responseClass = ContentSourceFilterListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "filter")
    )
    List<ContentSourceFilter> listRepoFilters(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true,
                description = "repository label") String label);

    /**
     * Adds a filter to a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param filterProps the filter properties
     * @return the sort order of the new filter
     */
    @ApiEndpointDoc(
        summary = "Adds a filter for a given repo.",
        requestClass = AddRepoFilterRequest.class,
        isIntegerResponse = true,
        responseDescription = "sort order for new filter",
        legacyDocResponse = @LegacyDocResponse(name = "order")
    )
    int addRepoFilter(User loggedInUser, String label, Map<String, String> filterProps);

    /**
     * Removes a filter from a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param filterProps the filter properties
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes a filter for a given repo.",
        requestClass = RemoveRepoFilterRequest.class,
        isIntegerResponse = true
    )
    int removeRepoFilter(User loggedInUser, String label, Map<String, String> filterProps);

    /**
     * Replaces the filters of a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @param filterProps the filter properties
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Replaces the existing set of filters for a given repo. Filters are ranked by " +
            "their order in the array.",
        requestClass = SetRepoFiltersRequest.class,
        isIntegerResponse = true
    )
    int setRepoFilters(User loggedInUser, String label, List<Map<String, String>> filterProps);

    /**
     * Removes all the filters of a repository.
     *
     * @param loggedInUser the current user
     * @param label the repository label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Removes the filters for a repo",
        requestClass = ClearRepoFiltersRequest.class,
        isIntegerResponse = true
    )
    int clearRepoFilters(User loggedInUser, String label);

    /**
     * Schedules the channels state on the given systems.
     *
     * @param user the current user
     * @param sids the system ids
     * @return the id of the scheduled action
     */
    @ApiEndpointDoc(
        summary = "Refresh pillar data and then schedule channels state on the provided systems",
        requestClass = ApplyChannelStateRequest.class,
        isIntegerResponse = true,
        legacyDocResponse = @LegacyDocResponse(type = "array", name = "actionId")
    )
    long applyChannelState(User user, List<Integer> sids);

    /**
     * Returns whether a channel is synchronized automatically.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @return true if the channel is synchronized automatically
     */
    @ApiEndpointDoc(
        summary = "Returns whether is synchronized automatically",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "true if the channel is synchronized automatically",
        legacyDocResponse = @LegacyDocResponse(name = "result")
    )
    boolean isAutoSync(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "channelLabel", in = ParameterIn.QUERY, required = true,
                description = "label of the channel") String channelLabel);

    /**
     * Sets whether a channel is synchronized automatically.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel
     * @param autoSync whether the channel is synchronized automatically
     * @return the value set on the channel
     */
    @ApiEndpointDoc(
        summary = "Sets whether the channel is synchronized automatically",
        requestClass = SetAutoSyncRequest.class,
        responseClass = BooleanResponse.class,
        responseDescription = "The value set to the channel automatic synchronized option",
        legacyDocResponse = @LegacyDocResponse(name = "result")
    )
    boolean setAutoSync(User loggedInUser, String channelLabel, Boolean autoSync);

    @Schema(name = "ChannelLatestPackage")
    @JsonPropertyOrder({"name", "version", "release", "epoch", "id", "archLabel"})
    interface LatestPackageDoc {

        /**
         * @return the package name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the package version
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the package release
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the package epoch
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

        /**
         * @return the package id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the package architecture label
         */
        @Schema(name = "arch_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchLabel();
    }

    @Schema(name = "ChannelPackageDto")
    @JsonPropertyOrder({"name", "version", "release", "epoch", "checksum", "checksumType", "id",
        "archLabel", "lastModifiedDate", "retracted", "lastModified"})
    interface PackageDtoDoc {

        /**
         * @return the package name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the package version
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return the package release
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getRelease();

        /**
         * @return the package epoch
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

        /**
         * @return the package checksum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksum();

        /**
         * @return the package checksum type
         */
        @Schema(name = "checksum_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksumType();

        /**
         * @return the package id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the package architecture label
         */
        @Schema(name = "arch_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchLabel();

        /**
         * @return the last modification date of the package
         */
        @Schema(name = "last_modified_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastModifiedDate();

        /**
         * @return whether the package has been retracted
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean getRetracted();

        /**
         * @return the last modification date of the package
         */
        @Schema(name = "last_modified", description = "(deprecated)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastModified();
    }

    @Schema(name = "ChannelArch")
    @JsonPropertyOrder({"name", "label"})
    interface ChannelArchDoc {

        /**
         * @return the architecture name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the architecture label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "ChannelSubscribedSystem")
    @JsonPropertyOrder({"id", "name"})
    interface SubscribedSystemDoc {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the system name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "ChannelSystemChannel")
    @JsonPropertyOrder({"id", "label", "name"})
    interface SystemChannelDoc {

        /**
         * @return the channel id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getId();

        /**
         * @return the channel label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the channel name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "ChannelErrataByType")
    @JsonPropertyOrder({"advisory", "issueDate", "updateDate", "synopsis", "advisoryType",
        "lastModifiedDate"})
    interface ErrataByTypeDoc {

        /**
         * @return the name of the advisory
         */
        @Schema(description = "name of the advisory", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisory();

        /**
         * @return the issue date of the erratum
         */
        @Schema(name = "issue_date", description = "date format follows YYYY-MM-DD HH24:MI:SS",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getIssueDate();

        /**
         * @return the update date of the erratum
         */
        @Schema(name = "update_date", description = "date format follows YYYY-MM-DD HH24:MI:SS",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateDate();

        /**
         * @return the synopsis of the erratum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSynopsis();

        /**
         * @return the type of the advisory
         */
        @Schema(name = "advisory_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryType();

        /**
         * @return the last modification date of the erratum
         */
        @Schema(name = "last_modified_date",
                description = "date format follows YYYY-MM-DD HH24:MI:SS",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastModifiedDate();
    }

    @Schema(name = "ChannelErrata")
    @JsonPropertyOrder({"id", "date", "advisoryType", "advisoryStatus", "advisoryName",
        "advisorySynopsis"})
    interface ErrataDoc {

        /**
         * @return the id of the erratum
         */
        @Schema(description = "errata ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the creation date of the erratum
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
         * @return the synopsis of the erratum
         */
        @Schema(name = "advisory_synopsis", description = "summary of the erratum",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisorySynopsis();
    }

    @Schema(name = "ChannelUserRepo")
    @JsonPropertyOrder({"id", "label", "sourceUrl"})
    interface UserRepoDoc {

        /**
         * @return the id of the repository
         */
        @Schema(description = "ID of the repo", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "long")
        Long getId();

        /**
         * @return the label of the repository
         */
        @Schema(description = "label of the repo", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the url of the repository
         */
        @Schema(description = "URL of the repo", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSourceUrl();
    }

    @Schema(name = "ContentSourceSslInfo")
    @JsonPropertyOrder({"sslCaDesc", "sslCertDesc", "sslKeyDesc"})
    interface ContentSourceSslDoc {

        /**
         * @return the description of the SSL CA certificate
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCaDesc();

        /**
         * @return the description of the SSL client certificate
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCertDesc();

        /**
         * @return the description of the SSL client key
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslKeyDesc();
    }

    @Schema(name = "ContentSourceInfo")
    @JsonPropertyOrder({"id", "label", "sourceUrl", "type", "hasSignedMetadata",
        "sslContentSources"})
    interface ContentSourceDoc {

        /**
         * @return the id of the repository
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the label of the repository
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the url of the repository
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSourceUrl();

        /**
         * @return the type of the repository
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return whether the repository has signed metadata
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getHasSignedMetadata();

        /**
         * @return the SSL content sources of the repository
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "content source SSL")
        List<ContentSourceSslDoc> getSslContentSources();
    }

    @Schema(name = "ContentSourceFilterInfo")
    @JsonPropertyOrder({"sortOrder", "filter", "flag"})
    interface ContentSourceFilterDoc {

        /**
         * @return the sort order of the filter
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSortOrder();

        /**
         * @return the filter
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFilter();

        /**
         * @return the flag of the filter
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFlag();
    }

    @Schema(name = "ChannelSoftwareSyncErrataRequest")
    interface SyncErrataRequest {

        /**
         * @return the channel to update
         */
        @Schema(description = "channel to update", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "ChannelSoftwareDeleteRequest")
    interface DeleteRequest {

        /**
         * @return the channel to delete
         */
        @Schema(description = "channel to delete", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "ChannelSoftwareDetails")
    @JsonPropertyOrder({"checksumLabel", "name", "summary", "description", "maintainerName",
        "maintainerEmail", "maintainerPhone", "gpgKeyUrl", "gpgKeyId", "gpgKeyFp", "gpgCheck"})
    interface ChannelDetailsDoc {

        /**
         * @return the checksum label of the channel repository
         */
        @Schema(name = "checksum_label",
                description = "new channel repository checksum label (optional)")
        String getChecksumLabel();

        /**
         * @return the name of the channel
         */
        @Schema(description = "new channel name (optional)")
        String getName();

        /**
         * @return the summary of the channel
         */
        @Schema(description = "new channel summary (optional)")
        String getSummary();

        /**
         * @return the description of the channel
         */
        @Schema(description = "new channel description (optional)")
        String getDescription();

        /**
         * @return the name of the channel maintainer
         */
        @Schema(name = "maintainer_name", description = "new channel maintainer name (optional)")
        String getMaintainerName();

        /**
         * @return the email of the channel maintainer
         */
        @Schema(name = "maintainer_email", description = "new channel email address (optional)")
        String getMaintainerEmail();

        /**
         * @return the phone number of the channel maintainer
         */
        @Schema(name = "maintainer_phone", description = "new channel phone number (optional)")
        String getMaintainerPhone();

        /**
         * @return the GPG key url of the channel
         */
        @Schema(name = "gpg_key_url", description = "new channel gpg key url (optional)")
        String getGpgKeyUrl();

        /**
         * @return the GPG key id of the channel
         */
        @Schema(name = "gpg_key_id", description = "new channel gpg key id (optional)")
        String getGpgKeyId();

        /**
         * @return the GPG key fingerprint of the channel
         */
        @Schema(name = "gpg_key_fp", description = "new channel gpg key fingerprint (optional)")
        String getGpgKeyFp();

        /**
         * @return whether the GPG check is enabled
         */
        @Schema(name = "gpg_check", description = "enable/disable gpg check (optional)")
        String getGpgCheck();
    }

    @Schema(name = "ChannelSoftwareSetDetailsRequest")
    @JsonPropertyOrder({"channelLabel", "details"})
    interface SetDetailsRequest {

        /**
         * @return the channel label
         */
        @Schema(description = "channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the attributes to modify
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        ChannelDetailsDoc getDetails();
    }

    @Schema(name = "ChannelSoftwareSetDetailsByIdRequest")
    @JsonPropertyOrder({"channelId", "details"})
    interface SetDetailsByIdRequest {

        /**
         * @return the channel id
         */
        @Schema(description = "channel id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getChannelId();

        /**
         * @return the attributes to modify
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        ChannelDetailsDoc getDetails();
    }

    @Schema(name = "ChannelSoftwareGpgKey")
    @JsonPropertyOrder({"url", "id", "fingerprint"})
    interface GpgKeyDoc {

        /**
         * @return the GPG key url
         */
        @Schema(description = "GPG key URL")
        String getUrl();

        /**
         * @return the GPG key id
         */
        @Schema(description = "GPG key ID")
        String getId();

        /**
         * @return the GPG key fingerprint
         */
        @Schema(description = "GPG key Fingerprint")
        String getFingerprint();
    }

    @Schema(name = "ChannelSoftwareCreateWithGpgCheckRequest")
    @JsonPropertyOrder({"label", "name", "summary", "archLabel", "parentLabel", "checksumType",
        "gpgKey", "gpgCheck"})
    interface CreateWithGpgCheckRequest extends CreateWithGpgKeyRequest {

        /**
         * @return whether the GPG check is enabled by default
         */
        @Schema(description = "true if the GPG check should be enabled by default, false otherwise",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getGpgCheck();
    }

    @Schema(name = "ChannelSoftwareCreateWithGpgKeyRequest")
    @JsonPropertyOrder({"label", "name", "summary", "archLabel", "parentLabel", "checksumType",
        "gpgKey"})
    interface CreateWithGpgKeyRequest extends CreateWithChecksumRequest {

        /**
         * @return the GPG key of the channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        GpgKeyDoc getGpgKey();
    }

    @Schema(name = "ChannelSoftwareCreateWithChecksumRequest")
    @JsonPropertyOrder({"label", "name", "summary", "archLabel", "parentLabel", "checksumType"})
    interface CreateWithChecksumRequest extends CreateRequest {

        /**
         * @return the checksum type of the channel
         */
        @Schema(description = "checksum type for this channel, used for yum repository metadata " +
                    "generation",
                allowableValues = {"sha1", "sha256"},
                extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                    @ExtensionProperty(name = "sha1",
                        value = "offers widest compatibility with clients"),
                    @ExtensionProperty(name = "sha256",
                        value = "offers highest security, but is compatible only with newer " +
                            "clients: Fedora 11 and newer, or Enterprise Linux 6 and newer.")
                }),
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksumType();
    }

    @Schema(name = "ChannelSoftwareCreateRequest")
    @JsonPropertyOrder({"label", "name", "summary", "archLabel", "parentLabel"})
    interface CreateRequest {

        /**
         * @return the label of the new channel
         */
        @Schema(description = "label of the new channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name of the new channel
         */
        @Schema(description = "name of the new channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the summary of the new channel
         */
        @Schema(description = "summary of the channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the architecture of the new channel
         */
        @Schema(description = "the label of the architecture the channel corresponds to, run " +
                    "channel.software.listArches API for complete listing",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchLabel();

        /**
         * @return the label of the parent channel
         */
        @Schema(description = "label of the parent of this channel, an empty string if it does " +
                    "not have one",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getParentLabel();
    }

    @Schema(name = "ChannelSoftwareSetContactDetailsRequest")
    @JsonPropertyOrder({"channelLabel", "maintainerName", "maintainerEmail", "maintainerPhone",
        "supportPolicy"})
    interface SetContactDetailsRequest {

        /**
         * @return the label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the name of the channel maintainer
         */
        @Schema(description = "name of the channel maintainer",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMaintainerName();

        /**
         * @return the email of the channel maintainer
         */
        @Schema(description = "email of the channel maintainer",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMaintainerEmail();

        /**
         * @return the phone number of the channel maintainer
         */
        @Schema(description = "phone number of the channel maintainer",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMaintainerPhone();

        /**
         * @return the support policy of the channel
         */
        @Schema(description = "channel support policy",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSupportPolicy();
    }

    @Schema(name = "ChannelSoftwareSetUserSubscribableRequest")
    @JsonPropertyOrder({"channelLabel", "login", "value"})
    interface SetUserSubscribableRequest {

        /**
         * @return the label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the login of the target user
         */
        @Schema(description = "login of the target user",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the value of the flag
         */
        @Schema(description = "value of the flag to set",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getValue();
    }

    @Schema(name = "ChannelSoftwareSetUserManageableRequest")
    @JsonPropertyOrder({"channelLabel", "login", "value"})
    interface SetUserManageableRequest {

        /**
         * @return the label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the login of the target user
         */
        @Schema(description = "login of the target user",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the value of the flag
         */
        @Schema(description = "value of the flag to set",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getValue();
    }

    @Schema(name = "ChannelSoftwareSetGloballySubscribableRequest")
    @JsonPropertyOrder({"channelLabel", "value"})
    interface SetGloballySubscribableRequest {

        /**
         * @return the label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return whether the channel is globally subscribable
         */
        @Schema(description = "true if the channel is to be globally subscribable. False " +
                    "otherwise.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getValue();
    }

    @Schema(name = "ChannelSoftwareAddPackagesRequest")
    @JsonPropertyOrder({"channelLabel", "packageIds"})
    interface AddPackagesRequest {

        /**
         * @return the target channel
         */
        @Schema(description = "target channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the ids of the packages to add
         */
        @Schema(description = "ID of a package to add to the channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Long> getPackageIds();
    }

    @Schema(name = "ChannelSoftwareRemoveErrataRequest")
    @JsonPropertyOrder({"channelLabel", "errataNames", "removePackages"})
    interface RemoveErrataRequest {

        /**
         * @return the target channel
         */
        @Schema(description = "target channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the names of the errata to remove
         */
        @Schema(description = "name of an erratum to remove",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getErrataNames();

        /**
         * @return whether to remove the packages from the channel
         */
        @Schema(description = "true to remove packages from the channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRemovePackages();
    }

    @Schema(name = "ChannelSoftwareRemovePackagesRequest")
    @JsonPropertyOrder({"channelLabel", "packageIds"})
    interface RemovePackagesRequest {

        /**
         * @return the target channel
         */
        @Schema(description = "target channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the ids of the packages to remove
         */
        @Schema(description = "ID of a package to remove from the channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Long> getPackageIds();
    }

    @Schema(name = "ChannelSoftwareCloneDetails")
    @JsonPropertyOrder({"name", "label", "summary", "parentLabel", "archLabel", "gpgKeyUrl",
        "gpgKeyId", "gpgKeyFp", "gpgCheck", "description", "checksum"})
    interface CloneDetailsDoc {

        /**
         * @return the name of the cloned channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the label of the cloned channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the summary of the cloned channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the label of the parent channel
         */
        @Schema(name = "parent_label", description = "(optional)")
        String getParentLabel();

        /**
         * @return the architecture of the cloned channel
         */
        @Schema(name = "arch_label", description = "(optional)")
        String getArchLabel();

        /**
         * @return the GPG key url of the cloned channel
         */
        @Schema(name = "gpg_key_url",
                description = "(optional), gpg_url might be used as well")
        String getGpgKeyUrl();

        /**
         * @return the GPG key id of the cloned channel
         */
        @Schema(name = "gpg_key_id", description = "(optional), gpg_id might be used as well")
        String getGpgKeyId();

        /**
         * @return the GPG key fingerprint of the cloned channel
         */
        @Schema(name = "gpg_key_fp",
                description = "(optional), gpg_fingerprint might be used as well")
        String getGpgKeyFp();

        /**
         * @return whether the GPG check is enabled
         */
        @Schema(name = "gpg_check", description = "(optional)")
        String getGpgCheck();

        /**
         * @return the description of the cloned channel
         */
        @Schema(description = "(optional)")
        String getDescription();

        /**
         * @return the checksum type of the cloned channel
         */
        @Schema(description = "either sha1 or sha256")
        String getChecksum();
    }

    @Schema(name = "ChannelSoftwareCloneRequest")
    @JsonPropertyOrder({"originalLabel", "channelDetails", "originalState"})
    interface CloneRequest {

        /**
         * @return the label of the channel to clone
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getOriginalLabel();

        /**
         * @return the details of the cloned channel
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        CloneDetailsDoc getChannelDetails();

        /**
         * @return whether to clone the original state
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getOriginalState();
    }

    @Schema(name = "ChannelSoftwareMergeErrataRequest")
    @JsonPropertyOrder({"mergeFromLabel", "mergeToLabel"})
    interface MergeErrataRequest {

        /**
         * @return the channel to pull the errata from
         */
        @Schema(description = "the label of the channel to pull errata from",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMergeFromLabel();

        /**
         * @return the channel to push the errata into
         */
        @Schema(description = "the label to push the errata into",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMergeToLabel();
    }

    @Schema(name = "ChannelSoftwareMergeErrataByDateRequest")
    @JsonPropertyOrder({"mergeFromLabel", "mergeToLabel", "startDate", "endDate"})
    interface MergeErrataByDateRequest extends MergeErrataRequest {

        /**
         * @return the start date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStartDate();

        /**
         * @return the end date
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEndDate();
    }

    @Schema(name = "ChannelSoftwareMergeErrataByNameRequest")
    @JsonPropertyOrder({"mergeFromLabel", "mergeToLabel", "errataNames"})
    interface MergeErrataByNameRequest extends MergeErrataRequest {

        /**
         * @return the advisory names of the errata to merge
         */
        @Schema(description = "the advisory name of the errata to merge",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getErrataNames();
    }

    @Schema(name = "ChannelSoftwareMergePackagesRequest")
    @JsonPropertyOrder({"mergeFromLabel", "mergeToLabel"})
    interface MergePackagesRequest {

        /**
         * @return the channel to pull the packages from
         */
        @Schema(description = "the label of the channel to pull packages from",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMergeFromLabel();

        /**
         * @return the channel to push the packages into
         */
        @Schema(description = "the label to push the packages into",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMergeToLabel();
    }

    @Schema(name = "ChannelSoftwareMergePackagesAlignedRequest")
    @JsonPropertyOrder({"mergeFromLabel", "mergeToLabel", "alignModules"})
    interface MergePackagesAlignedRequest extends MergePackagesRequest {

        /**
         * @return whether to align the modular data
         */
        @Schema(description = "align modular data of the target channel to the source channel " +
                    "(RHEL8 and higher)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getAlignModules();
    }

    @Schema(name = "ChannelSoftwareAlignMetadataRequest")
    @JsonPropertyOrder({"channelFromLabel", "channelToLabel", "metadataType"})
    interface AlignMetadataRequest {

        /**
         * @return the label of the source channel
         */
        @Schema(description = "the label of the source channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelFromLabel();

        /**
         * @return the label of the target channel
         */
        @Schema(description = "the label of the target channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelToLabel();

        /**
         * @return the metadata type
         */
        @Schema(description = "the metadata type. Only 'modules' supported currently.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMetadataType();
    }

    @Schema(name = "ChannelSoftwareRegenerateNeededCacheRequest")
    interface RegenerateNeededCacheRequest {

        /**
         * @return the label of the channel
         */
        @Schema(description = "the label of the channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "ChannelSoftwareRegenerateYumCacheRequest")
    @JsonPropertyOrder({"channelLabel", "force"})
    interface RegenerateYumCacheRequest {

        /**
         * @return the label of the channel
         */
        @Schema(description = "the label of the channel",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return whether to force the cache regeneration
         */
        @Schema(description = "force cache regeneration",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getForce();
    }

    @Schema(name = "ChannelSoftwareCreateRepoRequest")
    @JsonPropertyOrder({"label", "type", "url"})
    interface CreateRepoRequest {

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the repository type
         */
        @Schema(description = "repository type (yum, uln...)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the repository url
         */
        @Schema(description = "repository url", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUrl();
    }

    @Schema(name = "ChannelSoftwareCreateRepoWithSslRequest")
    @JsonPropertyOrder({"label", "type", "url", "sslCaCert", "sslCliCert", "sslCliKey"})
    interface CreateRepoWithSslRequest extends CreateRepoRequest {

        /**
         * @return the description of the SSL CA certificate
         */
        @Schema(description = "SSL CA cert description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCaCert();

        /**
         * @return the description of the SSL client certificate
         */
        @Schema(description = "SSL Client cert description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCliCert();

        /**
         * @return the description of the SSL client key
         */
        @Schema(description = "SSL Client key description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCliKey();
    }

    @Schema(name = "ChannelSoftwareCreateRepoWithMetadataRequest")
    @JsonPropertyOrder({"label", "type", "url", "sslCaCert", "sslCliCert", "sslCliKey",
        "hasSignedMetadata"})
    interface CreateRepoWithMetadataRequest extends CreateRepoWithSslRequest {

        /**
         * @return whether the repository has signed metadata
         */
        @Schema(description = "true if the repository has signed metadata, false otherwise",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getHasSignedMetadata();
    }

    @Schema(name = "ChannelSoftwareRemoveRepoByIdRequest")
    interface RemoveRepoByIdRequest {

        /**
         * @return the id of the repository to remove
         */
        @Schema(description = "ID of repo to be removed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "long")
        Integer getId();
    }

    @Schema(name = "ChannelSoftwareRemoveRepoByLabelRequest")
    interface RemoveRepoByLabelRequest {

        /**
         * @return the label of the repository to remove
         */
        @Schema(description = "label of repo to be removed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "ChannelSoftwareAssociateRepoRequest")
    @JsonPropertyOrder({"channelLabel", "repoLabel"})
    interface AssociateRepoRequest {

        /**
         * @return the channel label
         */
        @Schema(description = "channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRepoLabel();
    }

    @Schema(name = "ChannelSoftwareDisassociateRepoRequest")
    @JsonPropertyOrder({"channelLabel", "repoLabel"})
    interface DisassociateRepoRequest {

        /**
         * @return the channel label
         */
        @Schema(description = "channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRepoLabel();
    }

    @Schema(name = "ChannelSoftwareUpdateRepoUrlByIdRequest")
    @JsonPropertyOrder({"id", "url"})
    interface UpdateRepoUrlByIdRequest {

        /**
         * @return the repository id
         */
        @Schema(description = "repository ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the new repository url
         */
        @Schema(description = "new repository URL", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUrl();
    }

    @Schema(name = "ChannelSoftwareUpdateRepoUrlByLabelRequest")
    @JsonPropertyOrder({"label", "url"})
    interface UpdateRepoUrlByLabelRequest {

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the new repository url
         */
        @Schema(description = "new repository URL", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUrl();
    }

    @Schema(name = "ChannelSoftwareUpdateRepoSslByIdRequest")
    @JsonPropertyOrder({"id", "sslCaCert", "sslCliCert", "sslCliKey"})
    interface UpdateRepoSslByIdRequest {

        /**
         * @return the repository id
         */
        @Schema(description = "repository ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the description of the SSL CA certificate
         */
        @Schema(description = "SSL CA cert description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCaCert();

        /**
         * @return the description of the SSL client certificate
         */
        @Schema(description = "SSL Client cert description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCliCert();

        /**
         * @return the description of the SSL client key
         */
        @Schema(description = "SSL Client key description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCliKey();
    }

    @Schema(name = "ChannelSoftwareUpdateRepoSslByLabelRequest")
    @JsonPropertyOrder({"label", "sslCaCert", "sslCliCert", "sslCliKey"})
    interface UpdateRepoSslByLabelRequest {

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the description of the SSL CA certificate
         */
        @Schema(description = "SSL CA cert description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCaCert();

        /**
         * @return the description of the SSL client certificate
         */
        @Schema(description = "SSL Client cert description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCliCert();

        /**
         * @return the description of the SSL client key
         */
        @Schema(description = "SSL Client key description",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslCliKey();
    }

    @Schema(name = "ChannelSoftwareUpdateRepoLabelByIdRequest")
    @JsonPropertyOrder({"id", "label"})
    interface UpdateRepoLabelByIdRequest {

        /**
         * @return the repository id
         */
        @Schema(description = "repository ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the new repository label
         */
        @Schema(description = "new repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "ChannelSoftwareUpdateRepoLabelByLabelRequest")
    @JsonPropertyOrder({"label", "newLabel"})
    interface UpdateRepoLabelByLabelRequest {

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the new repository label
         */
        @Schema(description = "new repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getNewLabel();
    }

    @Schema(name = "ChannelSoftwareUpdateRepoRequest")
    @JsonPropertyOrder({"id", "label", "url"})
    interface UpdateRepoRequest {

        /**
         * @return the repository id
         */
        @Schema(description = "repository ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the new repository label
         */
        @Schema(description = "new repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the new repository url
         */
        @Schema(description = "new repository URL", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUrl();
    }

    @Schema(name = "ChannelSoftwareSyncRepoListRequest")
    interface SyncRepoListRequest {

        /**
         * @return the channel labels
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannelLabels();
    }

    @Schema(name = "ChannelSoftwareSyncRepoRequest")
    interface SyncRepoRequest {

        /**
         * @return the channel label
         */
        @Schema(description = "channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();
    }

    @Schema(name = "ChannelSoftwareSyncParams")
    @JsonPropertyOrder({"syncKickstart", "noErrata", "fail", "latest"})
    interface SyncParamsDoc {

        /**
         * @return whether to create a kickstartable tree
         */
        @Schema(name = "sync-kickstart", description = "create kickstartable tree - Optional")
        Boolean getSyncKickstart();

        /**
         * @return whether to skip the errata synchronisation
         */
        @Schema(name = "no-errata", description = "do not sync errata - Optional")
        Boolean getNoErrata();

        /**
         * @return whether to terminate upon any error
         */
        @Schema(description = "terminate upon any error - Optional")
        Boolean getFail();

        /**
         * @return whether to download only the latest packages
         */
        @Schema(description = "only download latest packages - Optional")
        Boolean getLatest();
    }

    @Schema(name = "ChannelSoftwareSyncRepoWithParamsRequest")
    @JsonPropertyOrder({"channelLabel", "params"})
    interface SyncRepoWithParamsRequest {

        /**
         * @return the channel label
         */
        @Schema(description = "channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the synchronisation parameters
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        SyncParamsDoc getParams();
    }

    @Schema(name = "ChannelSoftwareSyncRepoScheduleRequest")
    @JsonPropertyOrder({"channelLabel", "cronExpr"})
    interface SyncRepoScheduleRequest {

        /**
         * @return the channel label
         */
        @Schema(description = "channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the cron expression
         */
        @Schema(description = "cron expression, if empty all periodic schedules will be disabled",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCronExpr();
    }

    @Schema(name = "ChannelSoftwareSyncRepoScheduleWithParamsRequest")
    @JsonPropertyOrder({"channelLabel", "cronExpr", "params"})
    interface SyncRepoScheduleWithParamsRequest extends SyncRepoScheduleRequest {

        /**
         * @return the synchronisation parameters
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        SyncParamsDoc getParams();
    }

    @Schema(name = "ChannelSoftwareRepoFilterProps")
    @JsonPropertyOrder({"filter", "flag"})
    interface RepoFilterPropsDoc {

        /**
         * @return the string to filter on
         */
        @Schema(description = "string to filter on")
        String getFilter();

        /**
         * @return the flag of the filter
         */
        @Schema(description = "+ for include, - for exclude")
        String getFlag();
    }

    @Schema(name = "ChannelSoftwareAddRepoFilterRequest")
    @JsonPropertyOrder({"label", "filterProps"})
    interface AddRepoFilterRequest {

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the filter properties
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        RepoFilterPropsDoc getFilterProps();
    }

    @Schema(name = "ChannelSoftwareRemoveRepoFilterRequest")
    @JsonPropertyOrder({"label", "filterProps"})
    interface RemoveRepoFilterRequest {

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the filter properties
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        RepoFilterPropsDoc getFilterProps();
    }

    @Schema(name = "ChannelSoftwareSetRepoFiltersRequest")
    @JsonPropertyOrder({"label", "filterProps"})
    interface SetRepoFiltersRequest {

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the filter properties
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "filter properties")
        List<RepoFilterPropsDoc> getFilterProps();
    }

    @Schema(name = "ChannelSoftwareClearRepoFiltersRequest")
    interface ClearRepoFiltersRequest {

        /**
         * @return the repository label
         */
        @Schema(description = "repository label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "ChannelSoftwareApplyChannelStateRequest")
    interface ApplyChannelStateRequest {

        /**
         * @return the system ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();
    }

    @Schema(name = "ChannelSoftwareSetAutoSyncRequest")
    @JsonPropertyOrder({"channelLabel", "autoSync"})
    interface SetAutoSyncRequest {

        /**
         * @return the label of the channel
         */
        @Schema(description = "label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the value of the automatic synchronization option
         */
        @Schema(description = "Value to set on the channel automatic synchronization",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "Boolean")
        Boolean getAutoSync();
    }

    @Schema(name = "ApiResponseString")
    interface StringResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ApiResponseChannelErrataOverviewList")
    interface ErrataOverviewListResponse
        extends ApiResponseWrapper<List<ImageInfoHandlerApi.ErrataOverviewDoc>> { }

    @Schema(name = "ApiResponseChannelLatestPackageList")
    interface LatestPackageListResponse extends ApiResponseWrapper<List<LatestPackageDoc>> { }

    @Schema(name = "ApiResponseChannelPackageDtoList")
    interface PackageDtoListResponse extends ApiResponseWrapper<List<PackageDtoDoc>> { }

    @Schema(name = "ApiResponseChannelArchList")
    interface ChannelArchListResponse extends ApiResponseWrapper<List<ChannelArchDoc>> { }

    @Schema(name = "ApiResponseSoftwareChannel")
    interface SoftwareChannelResponse
        extends ApiResponseWrapper<ChannelAppStreamHandlerApi.ChannelDoc> { }

    @Schema(name = "ApiResponseChannelSubscribedSystemList")
    interface SubscribedSystemListResponse extends ApiResponseWrapper<List<SubscribedSystemDoc>> { }

    @Schema(name = "ApiResponseChannelSystemChannelList")
    interface SystemChannelListResponse extends ApiResponseWrapper<List<SystemChannelDoc>> { }

    @Schema(name = "ApiResponseChannelErrataByTypeList")
    interface ErrataByTypeListResponse extends ApiResponseWrapper<List<ErrataByTypeDoc>> { }

    @Schema(name = "ApiResponseChannelErrataList")
    interface ErrataListResponse extends ApiResponseWrapper<List<ErrataDoc>> { }

    @Schema(name = "ApiResponseChannelUserRepoList")
    interface UserRepoListResponse extends ApiResponseWrapper<List<UserRepoDoc>> { }

    @Schema(name = "ApiResponseContentSource")
    interface ContentSourceResponse extends ApiResponseWrapper<ContentSourceDoc> { }

    @Schema(name = "ApiResponseContentSourceList")
    interface ContentSourceListResponse extends ApiResponseWrapper<List<ContentSourceDoc>> { }

    @Schema(name = "ApiResponseContentSourceFilterList")
    interface ContentSourceFilterListResponse
        extends ApiResponseWrapper<List<ContentSourceFilterDoc>> { }
}
