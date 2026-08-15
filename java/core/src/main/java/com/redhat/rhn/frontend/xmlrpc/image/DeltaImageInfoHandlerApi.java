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
package com.redhat.rhn.frontend.xmlrpc.image;

import com.redhat.rhn.domain.image.DeltaImageInfo;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link DeltaImageInfoHandler}.
 */
@Tag(name = "image.delta", description = "Provides methods to access and modify delta images.")
public interface DeltaImageInfoHandlerApi {

    /**
     * Import a delta image and schedule an inspect afterwards.
     *
     * @param loggedInUser the current user
     * @param sourceImageId the source image id
     * @param targetImageId the target image id
     * @param file the file path
     * @param pillar the pillar data
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Import an image and schedule an inspect afterwards. The \"size\" entries in the pillar\n" +
                "should be passed as string.",
        requestClass = CreateDeltaImageRequest.class,
        isIntegerResponse = true
    )
    Long createDeltaImage(User loggedInUser, Integer sourceImageId, Integer targetImageId,
                          String file, Map<String, Object> pillar);

    /**
     * Get details of a delta image.
     *
     * @param loggedInUser the current user
     * @param sourceImageId the source image id
     * @param targetImageId the target image id
     * @return the delta image details
     */
    @ApiEndpointDoc(
        summary = "Get details of an Image",
        method = HttpMethod.get,
        responseClass = DeltaImageResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "delta image information")
    )
    DeltaImageInfo getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sourceImageId", in = ParameterIn.QUERY, required = true) Integer sourceImageId,
        @Parameter(name = "targetImageId", in = ParameterIn.QUERY, required = true) Integer targetImageId);

    /**
     * List all delta images visible for the logged in user.
     *
     * @param loggedInUser the current user
     * @return the list of available delta images
     */
    @ApiEndpointDoc(
        summary = "List available DeltaImages",
        method = HttpMethod.get,
        responseClass = DeltaImageListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "delta image information")
    )
    List<DeltaImageInfo> listDeltas(@Parameter(hidden = true) User loggedInUser);

    @Schema(name = "CreateDeltaImageRequest")
    @JsonPropertyOrder({"sourceImageId", "targetImageId", "file", "pillar"})
    interface CreateDeltaImageRequest {

        /**
         * @return the source image id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSourceImageId();

        /**
         * @return the target image id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTargetImageId();

        /**
         * @return the file path
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFile();

        /**
         * The documented contract asks for the size entries as strings, so the pillar values
         * are left free-form rather than constrained to nested objects.
         *
         * @return the pillar data
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        @LegacyDocResponse(type = "struct")
        Map<String, Object> getPillar();
    }

    @Schema(name = "DeltaImage")
    @JsonPropertyOrder({"sourceId", "targetId", "file", "pillar"})
    interface DeltaImageDoc {

        /**
         * @return the source image id
         */
        @Schema(name = "source_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSourceId();

        /**
         * @return the target image id
         */
        @Schema(name = "target_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTargetId();

        /**
         * @return the file path
         */
        @Schema(description = "file path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFile();

        /**
         * The serializer stringifies the size entries, so the pillar values are deliberately
         * left free-form rather than constrained to nested objects.
         *
         * @return the pillar data
         */
        @Schema(description = "pillar data", requiredMode = Schema.RequiredMode.REQUIRED,
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        Map<String, Object> getPillar();
    }

    @Schema(name = "ApiResponseDeltaImage")
    interface DeltaImageResponse extends ApiResponseWrapper<DeltaImageDoc> { }

    @Schema(name = "ApiResponseDeltaImageList")
    interface DeltaImageListResponse extends ApiResponseWrapper<List<DeltaImageDoc>> { }
}
