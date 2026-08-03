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
package com.redhat.rhn.frontend.xmlrpc.packages.search;

import com.redhat.rhn.frontend.dto.PackageOverview;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link PackagesSearchHandler}.
 */
@Tag(name = "packages.search",
     description = "Methods to interface to package search capabilities in search server.")
public interface PackagesSearchHandlerApi {

    /**
     * Searches the package indexes by package name.
     *
     * @param sessionKey the session key
     * @param name the package name to search for
     * @return the matching packages
     */
    @ApiEndpointDoc(
        summary = "Search the lucene package indexes for all packages which match the given name.",
        method = HttpMethod.get,
        responseClass = PackageOverviewListResponse.class
    )
    List<PackageOverview> name(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(
            name = "name",
            description = "package name to search for",
            in = ParameterIn.QUERY,
            required = true
        ) String name
    );

    /**
     * Searches the package indexes by name or description.
     *
     * @param sessionKey the session key
     * @param query the text to match
     * @return the matching packages
     */
    @ApiEndpointDoc(
        summary = "Search the lucene package indexes for all packages which match the given query " +
                  "in name or description",
        method = HttpMethod.get,
        responseClass = PackageOverviewListResponse.class
    )
    List<PackageOverview> nameAndDescription(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(
            name = "query",
            description = "text to match in package name or description",
            in = ParameterIn.QUERY,
            required = true
        ) String query
    );

    /**
     * Searches the package indexes by name or summary.
     *
     * @param sessionKey the session key
     * @param query the text to match
     * @return the matching packages
     */
    @ApiEndpointDoc(
        summary = "Search the lucene package indexes for all packages which match the given query " +
                  "in name or summary.",
        method = HttpMethod.get,
        responseClass = PackageOverviewListResponse.class
    )
    List<PackageOverview> nameAndSummary(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(
            name = "query",
            description = "text to match in package name or summary",
            in = ParameterIn.QUERY,
            required = true
        ) String query
    );

    /**
     * Searches the package indexes with a Lucene Query Parser syntax query.
     *
     * @param sessionKey the session key
     * @param luceneQuery the query
     * @return the matching packages
     */
    @ApiEndpointDoc(
        summary = "Advanced method to search lucene indexes with a passed in query written " +
                  "in Lucene Query Parser syntax.",
        description = "Lucene Query Parser syntax is defined at lucene.apache.org " +
                      "(http://lucene.apache.org/java/3_5_0/queryparsersyntax.html).\n" +
                      "Fields searchable for Packages: name, epoch, version, release, arch, " +
                      "description, summary.\n" +
                      "Lucene Query Example: \"name:kernel AND version:2.6.18 AND -description:devel\"",
        method = HttpMethod.get,
        responseClass = PackageOverviewListResponse.class
    )
    List<PackageOverview> advanced(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(
            name = "luceneQuery",
            description = "a query written in the form of Lucene QueryParser Syntax",
            in = ParameterIn.QUERY,
            required = true
        ) String luceneQuery
    );

    /**
     * Searches the package indexes with a Lucene query, limited to a channel.
     *
     * @param sessionKey the session key
     * @param luceneQuery the query
     * @param channelLabel the channel label
     * @return the matching packages
     */
    @ApiEndpointDoc(
        summary = "Advanced method to search lucene indexes with a passed in query written " +
                  "in Lucene Query Parser syntax, additionally this method will limit results " +
                  "to those which are in the passed in channel label.",
        description = "Lucene Query Parser syntax is defined at lucene.apache.org " +
                      "(http://lucene.apache.org/java/3_5_0/queryparsersyntax.html).\n" +
                      "Fields searchable for Packages: name, epoch, version, release, arch, " +
                      "description, summary.\n" +
                      "Lucene Query Example: \"name:kernel AND version:2.6.18 AND -description:devel\"",
        method = HttpMethod.get,
        responseClass = PackageOverviewListResponse.class
    )
    List<PackageOverview> advancedWithChannel(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(
            name = "luceneQuery",
            description = "a query written in the form of Lucene QueryParser Syntax",
            in = ParameterIn.QUERY,
            required = true
        ) String luceneQuery,
        @Parameter(
            name = "channelLabel",
            description = "the channel Label",
            in = ParameterIn.QUERY,
            required = true
        ) String channelLabel
    );

    /**
     * Searches the package indexes with a Lucene query, limited to an activation key.
     *
     * @param sessionKey the session key
     * @param luceneQuery the query
     * @param activationKey the activation key
     * @return the matching packages
     */
    @ApiEndpointDoc(
        summary = "Advanced method to search lucene indexes with a passed in query written " +
                  "in Lucene Query Parser syntax, additionally this method will limit results " +
                  "to those which are associated with a given activation key.",
        description = "Lucene Query Parser syntax is defined at lucene.apache.org " +
                      "(http://lucene.apache.org/java/3_5_0/queryparsersyntax.html).\n" +
                      "Fields searchable for Packages: name, epoch, version, release, arch, " +
                      "description, summary.\n" +
                      "Lucene Query Example: \"name:kernel AND version:2.6.18 AND -description:devel\"",
        method = HttpMethod.get,
        responseClass = PackageOverviewListResponse.class
    )
    List<PackageOverview> advancedWithActKey(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(
            name = "luceneQuery",
            description = "a query written in the form of Lucene QueryParser Syntax",
            in = ParameterIn.QUERY,
            required = true
        ) String luceneQuery,
        @Parameter(
            name = "activationKey",
            description = "activation key to look for packages in",
            in = ParameterIn.QUERY,
            required = true
        ) String activationKey
    );

    @Schema(name = "PackageOverview")
    @JsonPropertyOrder({"id", "name", "summary", "description", "version", "release", "arch",
                        "epoch", "provider"})
    interface PackageOverviewDoc {

        /**
         * @return the package identifier
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long getId();

        /**
         * @return the package name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the package summary
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the package description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

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
         * @return the package architecture
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();

        /**
         * @return the package epoch
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getEpoch();

        /**
         * @return the package provider
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getProvider();
    }

    @Schema(name = "ApiResponsePackageOverviewList")
    interface PackageOverviewListResponse extends ApiResponseWrapper<List<PackageOverviewDoc>> { }
}
