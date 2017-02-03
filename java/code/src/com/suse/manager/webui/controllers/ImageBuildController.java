/**
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.reactor.messaging.ImageBuildEventMessage;
import com.suse.manager.webui.utils.gson.JsonResult;
import org.apache.commons.lang.StringUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Spark controller class for image building.
 */
public class ImageBuildController {

    private static final Gson GSON = new GsonBuilder().create();

    private ImageBuildController() { }

    /**
     * Returns a view to display build page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView buildView(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();

        // Parse optional profile id
        if (StringUtils.isNotBlank(req.params("profileId"))) {
            Long profileId = Long.parseLong(req.params("profileId"));
            model.put("profile_id", profileId);
        }
        else {
            model.put("profile_id", null);
        }

        return new ModelAndView(model, "content_management/build.jade");
    }

    /**
     * Gets a JSON list of Docker Build Host entitled systems
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getBuildHosts(Request req, Response res, User user) {
        ServerGroup sg = ServerGroupFactory
                .lookupEntitled(EntitlementManager.DOCKER_BUILD_HOST, user.getOrg());

        return json(res,
                getServerStreamJson(SystemManager.systemsInGroupShort(sg.getId())));
    }

    /**
     * Creates a list of JSON objects for a list of {@link SystemOverview}
     * instances, having only 'id' and 'name' properties
     *
     * @param systemList the system overview list
     * @return the list of JSON objects
     */
    private static List<JsonObject> getServerStreamJson(List<SystemOverview> systemList) {
        return systemList.stream().map(s -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", s.getId());
            json.addProperty("name", s.getName());
            return json;
        }).collect(Collectors.toList());
    }

    public static class BuildRequest {
        private long buildHostId;
        private String tag;

        public long getBuildHostId() {
            return buildHostId;
        }

        public String getTag() {
            return tag;
        }
    }

    public static Object build(Request request, Response response, User user) {
        response.type("application/json");

        BuildRequest buildRequest = GSON.fromJson(request.body(), BuildRequest.class);

        Long profileId = Long.parseLong(request.params("id"));
        Optional<ImageProfile> maybeProfile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());

        return maybeProfile.flatMap(ImageProfile::asDockerfileProfile).map(profile -> {
            MessageQueue.publish(new ImageBuildEventMessage(
                    buildRequest.getBuildHostId(), user.getId(), buildRequest.getTag(), profile
            ));
            return GSON.toJson(new JsonResult(true, Collections.singletonList("")));
        }).orElseGet(
                () -> GSON.toJson(new JsonResult(true, Collections.singletonList(
                        "Image profile with id " + profileId + "does not exist")))
        );
    }
}
