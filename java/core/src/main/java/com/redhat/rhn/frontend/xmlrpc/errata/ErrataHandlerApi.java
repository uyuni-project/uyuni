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
package com.redhat.rhn.frontend.xmlrpc.errata;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ErrataHandler}.
 */
@Tag(name = "errata", description = "Provides methods to access and modify errata.")
public interface ErrataHandlerApi {

    /**
     * Retrieves the details of an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @return the details of the erratum
     */
    @ApiEndpointDoc(
        summary = "Retrieves the details for the erratum matching the given advisory name.",
        method = HttpMethod.get,
        responseClass = ErratumResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "erratum")
    )
    Map<String, Object> getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "advisoryName", in = ParameterIn.QUERY, required = true) String advisoryName);

    /**
     * Sets the details of an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @param details the details to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = """
            Set erratum details. All arguments are optional and will only be modified
            if included in the struct. This method will only allow for modification of custom
            errata created either through the UI or API.\
            """,
        requestClass = SetDetailsRequest.class,
        isIntegerResponse = true
    )
    Integer setDetails(User loggedInUser, String advisoryName, Map<String, Object> details);

    /**
     * Lists the systems affected by an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @return the affected systems
     */
    @ApiEndpointDoc(
        summary = """
            Return the list of systems affected by the errata with the given advisory name.
            For those errata that are present in both vendor and user organizations under the same advisory name,
            this method retrieves the affected systems by both of them.\
            """,
        method = HttpMethod.get,
        responseClass = AffectedSystemListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listAffectedSystems(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "advisoryName", in = ParameterIn.QUERY, required = true) String advisoryName);

    /**
     * Returns the Bugzilla fixes of an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @return the Bugzilla fixes, keyed by bug id
     */
    @ApiEndpointDoc(
        summary = """
            Get the Bugzilla fixes for an erratum matching the given
            advisoryName. The bugs will be returned in a struct where the bug id is
            the key.  i.e. 208144="errata.bugzillaFixes Method Returns different
            results than docs say"
            For those errata that are present in both vendor and user organizations under the same advisory name,
            this method retrieves the list of Bugzilla fixes of both of them.\
            """,
        requestClass = AdvisoryNameRequest.class,
        responseClass = BugzillaFixesResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Bugzilla info")
    )
    Map<Long, String> bugzillaFixes(User loggedInUser, String advisoryName);

    /**
     * Lists the keywords of an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @return the keywords of the erratum
     */
    @ApiEndpointDoc(
        summary = """
            Get the keywords associated with an erratum matching the given advisory name.
            For those errata that are present in both vendor and user organizations under the same advisory name,
            this method retrieves the keywords of both of them.\
            """,
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        responseDescription = "keyword associated with erratum."
    )
    Object[] listKeywords(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "advisoryName", in = ParameterIn.QUERY, required = true) String advisoryName);

    /**
     * Lists the channels applicable to an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @return the applicable channels
     */
    @ApiEndpointDoc(
        summary = """
            Returns a list of channels applicable to the errata with the given advisory name.
            For those errata that are present in both vendor and user organizations under the same advisory name,
            this method retrieves the list of channels applicable of both of them.\
            """,
        method = HttpMethod.get,
        responseClass = ErrataChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel")
    )
    Object[] applicableToChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "advisoryName", in = ParameterIn.QUERY, required = true) String advisoryName);

    /**
     * Lists the CVEs of an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @return the CVE names
     */
    @ApiEndpointDoc(
        summary = """
            Returns a list of CVEs (http://cve.mitre.org/) applicable to the errata
            with the given advisory name. For those errata that are present in both vendor and user organizations \
            under the
            same advisory name, this method retrieves the list of CVEs of both of them.\
            """,
        method = HttpMethod.get,
        responseClass = StringListResponse.class,
        responseDescription = "CVE name"
    )
    List<String> listCves(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "advisoryName", in = ParameterIn.QUERY, required = true) String advisoryName);

    /**
     * Lists the packages affected by an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @return the affected packages
     */
    @ApiEndpointDoc(
        summary = """
            Returns a list of the packages affected by the errata with the given advisory name.
            For those errata that are present in both vendor and user organizations under the same advisory name,
            this method retrieves the packages of both of them.\
            """,
        method = HttpMethod.get,
        responseClass = ErrataPackageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package")
    )
    List<Map<String, Object>> listPackages(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "advisoryName", in = ParameterIn.QUERY, required = true) String advisoryName);

    /**
     * Adds packages to an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @param packageIds the ids of the packages to add
     * @return the number of packages added
     */
    @ApiEndpointDoc(
        summary = "Add a set of packages to an erratum with the given advisory name.\n" +
                "This method will only allow for modification of custom errata created either through " +
                "the UI or API.",
        requestClass = AdvisoryPackagesRequest.class,
        isIntegerResponse = true,
        responseDescription = "the number of packages added, exception otherwise"
    )
    int addPackages(User loggedInUser, String advisoryName, List<Integer> packageIds);

    /**
     * Removes packages from an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum
     * @param packageIds the ids of the packages to remove
     * @return the number of packages removed
     */
    @ApiEndpointDoc(
        summary = "Remove a set of packages from an erratum with the given advisory name.\n" +
                "This method will only allow for modification of custom errata created either through " +
                "the UI or API.",
        requestClass = AdvisoryPackagesRequest.class,
        isIntegerResponse = true,
        responseDescription = "the number of packages removed, exception otherwise"
    )
    int removePackages(User loggedInUser, String advisoryName, List<Integer> packageIds);

    /**
     * Clones errata into a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to clone into
     * @param advisoryNames the advisory names of the errata to clone
     * @return the cloned errata
     */
    @ApiEndpointDoc(
        summary = """
            Clone a list of errata into the specified channel.
            It only links the packages if the destination channel already contains an older version of the
            same package (same name and architecture). If the package is completely new to that channel,
            it will not be linked and the resulting behaviour will be the same as a channel.software.mergeErrata call.\
            """,
        requestClass = CloneErrataRequest.class,
        responseClass = ErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Object[] clone(User loggedInUser, String channelLabel, List<String> advisoryNames);

    /**
     * Asynchronously clones errata into a channel.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to clone into
     * @param advisoryNames the advisory names of the errata to clone
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Asynchronously clone a list of errata into the specified channel.",
        requestClass = CloneErrataRequest.class,
        isIntegerResponse = true
    )
    int cloneAsync(User loggedInUser, String channelLabel, List<String> advisoryNames);

    /**
     * Clones errata into a cloned channel according to the original errata.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to clone into
     * @param advisoryNames the advisory names of the errata to clone
     * @return the cloned errata
     */
    @ApiEndpointDoc(
        summary = "Clones a list of errata into a specified cloned channel according the original erratas.\n" +
                "It always links the packages to the target channel by searching all related packages among " +
                "all the parent clones.",
        requestClass = CloneErrataRequest.class,
        responseClass = ErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Object[] cloneAsOriginal(User loggedInUser, String channelLabel, List<String> advisoryNames);

    /**
     * Asynchronously clones errata into a cloned channel according to the original errata.
     *
     * @param loggedInUser the current user
     * @param channelLabel the label of the channel to clone into
     * @param advisoryNames the advisory names of the errata to clone
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = """
            Asynchronously clones a list of errata into a specified cloned channel
            according the original erratas.
            It always links the packages to the target channel by searching all related packages among all the parent \
            clones.\
            """,
        requestClass = CloneErrataRequest.class,
        isIntegerResponse = true
    )
    int cloneAsOriginalAsync(User loggedInUser, String channelLabel, List<String> advisoryNames);

    /**
     * Creates an erratum.
     *
     * @param loggedInUser the current user
     * @param errataInfo the erratum details
     * @param bugs the bugs to associate
     * @param keywords the keywords to associate
     * @param packageIds the packages to associate
     * @param channelLabels the channels to publish to
     * @return the created erratum
     */
    @ApiEndpointDoc(
        summary = "Create a custom errata",
        requestClass = CreateRequest.class,
        responseClass = ErrataResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Errata create(User loggedInUser, Map<String, Object> errataInfo, List<Map<String, Object>> bugs,
                  List<String> keywords, List<Integer> packageIds, List<String> channelLabels);

    /**
     * Deletes an erratum.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete an erratum.  This method will only allow for deletion\n" +
                "of custom errata created either through the UI or API.",
        requestClass = AdvisoryNameRequest.class,
        isIntegerResponse = true
    )
    Integer delete(User loggedInUser, String advisoryName);

    /**
     * Adds an existing erratum to a set of channels.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum to add
     * @param channelLabels the labels of the channels to add the erratum to
     * @return the added erratum
     */
    @ApiEndpointDoc(
        summary = "Adds an existing errata to a set of channels.",
        requestClass = PublishRequest.class,
        responseClass = ErrataResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Errata publish(User loggedInUser, String advisoryName, List<String> channelLabels);

    /**
     * Adds an existing cloned erratum to a set of cloned channels.
     *
     * @param loggedInUser the current user
     * @param advisoryName the advisory name of the erratum to add
     * @param channelLabels the labels of the channels to add the erratum to
     * @return the added erratum
     */
    @ApiEndpointDoc(
        summary = "Adds an existing cloned errata to a set of cloned\n" +
                "channels according to its original erratum",
        requestClass = PublishRequest.class,
        responseClass = ErrataResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    Errata publishAsOriginal(User loggedInUser, String advisoryName, List<String> channelLabels);

    /**
     * Looks up the errata associated with a CVE.
     *
     * @param loggedInUser the current user
     * @param cveName the name of the CVE
     * @return the errata associated with the CVE
     */
    @ApiEndpointDoc(
        summary = "Lookup the details for errata associated with the given CVE\n" +
                "(e.g. CVE-2008-3270)",
        method = HttpMethod.get,
        responseClass = ErrataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "errata")
    )
    List<Errata> findByCve(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "cveName", in = ParameterIn.QUERY, required = true) String cveName);

    @Schema(name = "ErrataAdvisoryNameRequest")
    interface AdvisoryNameRequest {

        /**
         * @return the advisory name of the erratum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();
    }

    @Schema(name = "ErrataAdvisoryPackagesRequest")
    @JsonPropertyOrder({"advisoryName", "packageIds"})
    interface AdvisoryPackagesRequest {

        /**
         * @return the advisory name of the erratum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();

        /**
         * @return the ids of the packages
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPackageIds();
    }

    @Schema(name = "ErrataCloneRequest")
    @JsonPropertyOrder({"channelLabel", "advisoryNames"})
    interface CloneErrataRequest {

        /**
         * @return the label of the channel to clone into
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the advisory names of the errata to clone
         */
        @Schema(description = "the advisory names of the errata to clone",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getAdvisoryNames();
    }

    @Schema(name = "ErrataPublishRequest")
    @JsonPropertyOrder({"advisoryName", "channelLabels"})
    interface PublishRequest {

        /**
         * @return the advisory name of the erratum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();

        /**
         * @return the labels of the channels to add the erratum to
         */
        @Schema(description = "list of channel labels to add to",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannelLabels();
    }

    @Schema(name = "ErrataCreateRequest")
    @JsonPropertyOrder({"errataInfo", "bugs", "keywords", "packageIds", "channelLabels"})
    interface CreateRequest {

        /**
         * @return the erratum details
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "errataInfo")
        ErrataInfoDoc getErrataInfo();

        /**
         * @return the bugs to associate
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "bug")
        List<ErrataBugDoc> getBugs();

        /**
         * @return the keywords to associate
         */
        @Schema(description = "list of keywords to associate with the errata",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getKeywords();

        /**
         * @return the packages to associate
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getPackageIds();

        /**
         * @return the channels to publish to
         */
        @Schema(description = "list of channels the errata should be published to",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannelLabels();
    }

    @Schema(name = "ErrataSetDetailsRequest")
    @JsonPropertyOrder({"advisoryName", "details"})
    interface SetDetailsRequest {

        /**
         * @return the advisory name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();

        /**
         * @return the details to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "details")
        ErrataDetailsDoc getDetails();
    }

    @Schema(name = "ErrataInfo")
    @JsonPropertyOrder({"synopsis", "advisoryName", "advisoryRelease", "advisoryType", "advisoryStatus",
        "product", "errataFrom", "topic", "description", "references", "notes", "solution", "severity"})
    interface ErrataInfoDoc {

        /**
         * @return the synopsis
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSynopsis();

        /**
         * @return the advisory name
         */
        @Schema(name = "advisory_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();

        /**
         * @return the advisory release
         */
        @Schema(name = "advisory_release", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getAdvisoryRelease();

        /**
         * @return the advisory type
         */
        @Schema(name = "advisory_type", description = """
            Type of advisory (one of the
            following: 'Security Advisory', 'Product Enhancement Advisory',
            or 'Bug Fix Advisory'\
            """, requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryType();

        /**
         * @return the advisory status
         */
        @Schema(name = "advisory_status", description = "Status of advisory (one of the\n" +
                "following: 'final', 'testing', 'stable', 'pending', 'retracted' or 'unpushed'")
        String getAdvisoryStatus();

        /**
         * @return the product
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getProduct();

        /**
         * @return the errata origin
         */
        String getErrataFrom();

        /**
         * @return the topic
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getTopic();

        /**
         * @return the description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the references
         */
        String getReferences();

        /**
         * @return the notes
         */
        String getNotes();

        /**
         * @return the solution
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSolution();

        /**
         * @return the severity
         */
        @Schema(description = """
            Severity of advisory (one of the
            following: 'Low', 'Moderate', 'Important', 'Critical'
            or 'Unspecified'\
            """)
        String getSeverity();
    }

    @Schema(name = "ErrataDetails")
    @JsonPropertyOrder({"synopsis", "advisoryName", "advisoryRelease", "advisoryType", "product",
        "issueDate", "updateDate", "errataFrom", "topic", "description", "references", "notes",
        "solution", "severity", "bugs", "keywords", "cves"})
    interface ErrataDetailsDoc {

        /**
         * @return the synopsis
         */
        String getSynopsis();

        /**
         * @return the advisory name
         */
        @Schema(name = "advisory_name")
        String getAdvisoryName();

        /**
         * @return the advisory release
         */
        @Schema(name = "advisory_release")
        Integer getAdvisoryRelease();

        /**
         * @return the advisory type
         */
        @Schema(name = "advisory_type", description = """
            Type of advisory (one of the
            following: 'Security Advisory', 'Product Enhancement Advisory',
            or 'Bug Fix Advisory'\
            """)
        String getAdvisoryType();

        /**
         * @return the product
         */
        String getProduct();

        /**
         * @return the issue date
         */
        @Schema(name = "issue_date")
        Date getIssueDate();

        /**
         * @return the update date
         */
        @Schema(name = "update_date")
        Date getUpdateDate();

        /**
         * @return the errata origin
         */
        String getErrataFrom();

        /**
         * @return the topic
         */
        String getTopic();

        /**
         * @return the description
         */
        String getDescription();

        /**
         * @return the references
         */
        String getReferences();

        /**
         * @return the notes
         */
        String getNotes();

        /**
         * @return the solution
         */
        String getSolution();

        /**
         * @return the severity
         */
        @Schema(description = """
            Severity of advisory (one of the
            following: 'Low', 'Moderate', 'Important', 'Critical'
            or 'Unspecified'\
            """)
        String getSeverity();

        /**
         * @return the bugs
         */
        @Schema(description = "'bugs' is the key into the struct")
        @LegacyDocResponse(name = "bug")
        List<ErrataBugDoc> getBugs();

        /**
         * @return the keywords
         */
        @Schema(description = "list of keywords to associate with the errata")
        List<String> getKeywords();

        /**
         * @return the CVEs
         */
        @Schema(description = "list of CVEs to associate with the errata")
        List<String> getCves();
    }

    @Schema(name = "ErrataBug")
    @JsonPropertyOrder({"id", "summary", "url"})
    interface ErrataBugDoc {

        /**
         * @return the bug id
         */
        @Schema(description = "Bug Id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the summary
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the URL
         */
        String getUrl();
    }

    @Schema(name = "Erratum", description = "erratum")
    @JsonPropertyOrder({"id", "issueDate", "updateDate", "lastModifiedDate", "release", "advisoryStatus",
        "vendorAdvisory", "product", "errataFrom", "solution", "description", "synopsis", "topic",
        "references", "notes", "type", "severity", "rebootSuggested", "restartSuggested"})
    interface ErratumDoc {

        /**
         * @return the erratum id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the issue date
         */
        @Schema(name = "issue_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIssueDate();

        /**
         * @return the update date
         */
        @Schema(name = "update_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUpdateDate();

        /**
         * @return the last modification date
         */
        @Schema(name = "last_modified_date", description = "last time the erratum was modified.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastModifiedDate();

        /**
         * @return the advisory release
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getRelease();

        /**
         * @return the advisory status
         */
        @Schema(name = "advisory_status", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryStatus();

        /**
         * @return the vendor advisory
         */
        @Schema(name = "vendor_advisory", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVendorAdvisory();

        /**
         * @return the product
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getProduct();

        /**
         * @return the errata origin
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getErrataFrom();

        /**
         * @return the solution
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSolution();

        /**
         * @return the description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the synopsis
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSynopsis();

        /**
         * @return the topic
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getTopic();

        /**
         * @return the references
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getReferences();

        /**
         * @return the notes
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getNotes();

        /**
         * @return the advisory type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the severity
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSeverity();

        /**
         * @return whether a reboot is suggested
         */
        @Schema(name = "reboot_suggested",
                description = "A boolean flag signaling whether a system reboot is\n" +
                "advisable following the application of the errata. Typical example is upon kernel update.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRebootSuggested();

        /**
         * @return whether a package manager restart is suggested
         */
        @Schema(name = "restart_suggested",
                description = """
                    A boolean flag signaling a weather reboot of
                    the package manager is advisable following the application of the errata. This is commonly
                    used to address update stack issues before proceeding with other updates.\
                    """, requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getRestartSuggested();
    }

    @Schema(name = "ErrataDoc", description = "errata")
    @JsonPropertyOrder({"id", "date", "advisoryType", "advisoryStatus", "advisoryName", "advisorySynopsis"})
    interface ErrataDoc {

        /**
         * @return the errata id
         */
        @Schema(description = "errata ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the creation date
         */
        @Schema(description = "the date erratum was created", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDate();

        /**
         * @return the advisory type
         */
        @Schema(name = "advisory_type", description = "type of the advisory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryType();

        /**
         * @return the advisory status
         */
        @Schema(name = "advisory_status", description = "status of the advisory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryStatus();

        /**
         * @return the advisory name
         */
        @Schema(name = "advisory_name", description = "name of the advisory",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisoryName();

        /**
         * @return the advisory synopsis
         */
        @Schema(name = "advisory_synopsis", description = "summary of the erratum",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvisorySynopsis();
    }

    @Schema(name = "ErrataAffectedSystem", description = "system")
    @JsonPropertyOrder({"id", "name", "lastCheckin", "created", "lastBoot", "extraPkgCount", "outdatedPkgCount"})
    interface AffectedSystemDoc {

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

        /**
         * @return the last check in time
         */
        @Schema(name = "last_checkin", description = "last time server\n" +
                "successfully checked in")
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastCheckin();

        /**
         * @return the registration time
         */
        @Schema(description = "server registration time")
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreated();

        /**
         * @return the last boot time
         */
        @Schema(name = "last_boot", description = "last server boot time")
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastBoot();

        /**
         * @return the number of packages outside the assigned channels
         */
        @Schema(name = "extra_pkg_count", description = "number of packages not belonging\n" +
                "to any assigned channel", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getExtraPkgCount();

        /**
         * @return the number of out-of-date packages
         */
        @Schema(name = "outdated_pkg_count", description = "number of out-of-date packages",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOutdatedPkgCount();
    }

    @Schema(name = "ErrataChannel", description = "channel")
    @JsonPropertyOrder({"channelId", "label", "name", "parentChannelLabel"})
    interface ErrataChannelDoc {

        /**
         * @return the channel id
         */
        @Schema(name = "channel_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getChannelId();

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

        /**
         * @return the label of the parent channel
         */
        @Schema(name = "parent_channel_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getParentChannelLabel();
    }

    @Schema(name = "ErrataPackage", description = "package")
    @JsonPropertyOrder({"id", "name", "epoch", "version", "release", "archLabel", "providingChannels",
        "buildHost", "description", "checksum", "checksumType", "vendor", "summary", "cookie", "license",
        "path", "file", "buildDate", "lastModifiedDate", "size", "payloadSize"})
    interface ErrataPackageDoc {

        /**
         * @return the package id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the package name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the package epoch
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

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
         * @return the architecture label
         */
        @Schema(name = "arch_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArchLabel();

        /**
         * @return the labels of the channels providing the package
         */
        @Schema(name = "providing_channels", description = "Channel label\n" +
                "providing this package.", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getProvidingChannels();

        /**
         * @return the build host
         */
        @Schema(name = "build_host", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBuildHost();

        /**
         * @return the package description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the package checksum
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksum();

        /**
         * @return the checksum type
         */
        @Schema(name = "checksum_type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChecksumType();

        /**
         * @return the package vendor
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getVendor();

        /**
         * @return the package summary
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the package cookie
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getCookie();

        /**
         * @return the package license
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLicense();

        /**
         * @return the package path
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the package file
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFile();

        /**
         * @return the build date
         */
        @Schema(name = "build_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBuildDate();

        /**
         * @return the last modification date
         */
        @Schema(name = "last_modified_date", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLastModifiedDate();

        /**
         * @return the package size
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSize();

        /**
         * @return the payload size
         */
        @Schema(name = "payload_size", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPayloadSize();
    }

    @Schema(name = "ErrataBugzillaFixes", description = "Bugzilla info")
    @JsonPropertyOrder({"bugzillaId", "bugSummary"})
    interface BugzillaFixesDoc {

        /**
         * @return the Bugzilla id
         */
        @Schema(name = "bugzilla_id", description = "actual bug number is the key into the\n" +
                "struct")
        String getBugzillaId();

        /**
         * @return the bug summary
         */
        @Schema(name = "bug_summary", description = "summary who's key is the bug id")
        String getBugSummary();
    }

    @Schema(name = "ApiResponseErratum")
    interface ErratumResponse extends ApiResponseWrapper<ErratumDoc> { }

    @Schema(name = "ApiResponseErrata")
    interface ErrataResponse extends ApiResponseWrapper<ErrataDoc> { }

    @Schema(name = "ApiResponseErrataList")
    interface ErrataListResponse extends ApiResponseWrapper<List<ErrataDoc>> { }

    @Schema(name = "ApiResponseErrataAffectedSystemList")
    interface AffectedSystemListResponse extends ApiResponseWrapper<List<AffectedSystemDoc>> { }

    @Schema(name = "ApiResponseErrataChannelList")
    interface ErrataChannelListResponse extends ApiResponseWrapper<List<ErrataChannelDoc>> { }

    @Schema(name = "ApiResponseErrataPackageList")
    interface ErrataPackageListResponse extends ApiResponseWrapper<List<ErrataPackageDoc>> { }

    @Schema(name = "ApiResponseErrataBugzillaFixes")
    interface BugzillaFixesResponse extends ApiResponseWrapper<BugzillaFixesDoc> { }

    @Schema(name = "ApiResponseErrataStringList")
    interface StringListResponse extends ApiResponseWrapper<List<String>> { }
}
