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
package com.redhat.rhn.frontend.xmlrpc.system.appstreams;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;
import com.suse.manager.webui.controllers.appstreams.response.ChannelAppStreamsResponse;

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
 * API contract for {@link SystemAppStreamHandler}.
 */
@Tag(name = "system.appstreams", description = "Provides methods to handle appstreams for systems.")
public interface SystemAppStreamHandlerApi {

    /**
     * Schedule module stream enable.
     *
     * @param loggedInUser the current user
     * @param sid the server id
     * @param moduleStreams the module streams to enable
     * @param earliestOccurrence the earliest occurrence of the action
     * @return the scheduled action id
     */
    @ApiEndpointDoc(
        summary = "Schedule enabling of module streams. Invalid modules will be filtered out. If all provided\n" +
                "modules are invalid the request will fail.",
        requestClass = EnableRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int enable(User loggedInUser, Integer sid, List<Map<String, String>> moduleStreams, Date earliestOccurrence);

    /**
     * Schedule module stream disable.
     *
     * @param loggedInUser the current user
     * @param sid the server id
     * @param moduleStreams the module streams to disable
     * @param earliestOccurrence the earliest occurrence of the action
     * @return the scheduled action id
     */
    @ApiEndpointDoc(
        summary = "Schedule disabling of module streams. Invalid modules will be filtered out. If all provided\n" +
                "modules are invalid the request will fail.",
        requestClass = DisableRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int disable(User loggedInUser, Integer sid, List<Map<String, String>> moduleStreams, Date earliestOccurrence);

    /**
     * List the available module streams for a system.
     *
     * @param loggedInUser the current user
     * @param sid the server id
     * @return the available module streams
     */
    @ApiEndpointDoc(
        summary = "List available module streams for a given system.",
        method = HttpMethod.get,
        responseClass = ChannelAppStreamsListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "ChannelAppStreams")
    )
    List<ChannelAppStreamsResponse> listModuleStreams(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Schedule module stream enable for the SSM systems of a modular channel.
     *
     * @param loggedInUser the current user
     * @param channelId the channel id
     * @param moduleStreams the module streams to enable
     * @param earliestOccurrence the earliest occurrence of the action
     * @return the scheduled action id
     */
    @ApiEndpointDoc(
        summary = "Schedule enabling of module streams from a given modular channel for the SSM " +
                "(System Set Manager)\nsystems. Invalid modules will be filtered out. If all provided modules " +
                "are invalid the request will fail.",
        requestClass = SsmEnableRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int ssmEnable(User loggedInUser, Integer channelId, List<Map<String, String>> moduleStreams,
                  Date earliestOccurrence);

    /**
     * Schedule module stream disable for the SSM systems of a modular channel.
     *
     * @param loggedInUser the current user
     * @param channelId the channel id
     * @param moduleNames the module names to disable
     * @param earliestOccurrence the earliest occurrence of the action
     * @return the scheduled action id
     */
    @ApiEndpointDoc(
        summary = "Schedule disabling of module streams from a given modular channel for the SSM " +
                "(System Set Manager)\nsystems. Invalid modules will be filtered out. If all provided modules " +
                "are invalid the request will fail.",
        requestClass = SsmDisableRequest.class,
        isIntegerResponse = true,
        responseDescription = "The action id of the scheduled action",
        legacyDocResponse = @LegacyDocResponse(name = "actionId")
    )
    int ssmDisable(User loggedInUser, Integer channelId, List<String> moduleNames, Date earliestOccurrence);

    @Schema(name = "ModuleStream")
    @JsonPropertyOrder({"module", "stream"})
    interface ModuleStreamDoc {

        /**
         * @return the module name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getModule();

        /**
         * @return the stream name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStream();
    }

    @Schema(name = "SystemAppStreamEnableRequest")
    @JsonPropertyOrder({"sid", "moduleStreams", "earliestOccurrence"})
    interface EnableRequest {

        /**
         * @return the server id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the module streams to enable
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "Module Stream")
        List<ModuleStreamDoc> getModuleStreams();

        /**
         * @return the earliest occurrence of the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemAppStreamDisableRequest")
    @JsonPropertyOrder({"sid", "moduleStreams", "earliestOccurrence"})
    interface DisableRequest {

        /**
         * @return the server id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the module streams to disable
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "Module Stream")
        List<ModuleStreamDoc> getModuleStreams();

        /**
         * @return the earliest occurrence of the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemAppStreamSsmEnableRequest")
    @JsonPropertyOrder({"channelId", "moduleStreams", "earliestOccurrence"})
    interface SsmEnableRequest {

        /**
         * @return the channel id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getChannelId();

        /**
         * @return the module streams to enable
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "Module Stream")
        List<ModuleStreamDoc> getModuleStreams();

        /**
         * @return the earliest occurrence of the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "SystemAppStreamSsmDisableRequest")
    @JsonPropertyOrder({"channelId", "moduleNames", "earliestOccurrence"})
    interface SsmDisableRequest {

        /**
         * @return the channel id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getChannelId();

        /**
         * @return the module names to disable
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "moduleName")
        List<String> getModuleNames();

        /**
         * @return the earliest occurrence of the action
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliestOccurrence();
    }

    @Schema(name = "AppStream")
    @JsonPropertyOrder({"isEnabled", "stream", "module", "arch"})
    interface AppStreamDoc {

        /**
         * @return whether the module stream is enabled
         */
        @Schema(name = "is_enabled", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getIsEnabled();

        /**
         * @return the stream name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStream();

        /**
         * @return the module name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getModule();

        /**
         * @return the module architecture
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();
    }

    @Schema(name = "ChannelAppStreams")
    @JsonPropertyOrder({"channelLabel", "appStreams"})
    interface ChannelAppStreamsDoc {

        /**
         * @return the channel label
         */
        @Schema(name = "channel_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the app streams of the channel
         */
        @Schema(name = "app_streams", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "AppStream")
        List<AppStreamDoc> getAppStreams();
    }

    @Schema(name = "ApiResponseChannelAppStreamsList")
    interface ChannelAppStreamsListResponse extends ApiResponseWrapper<List<ChannelAppStreamsDoc>> { }
}
