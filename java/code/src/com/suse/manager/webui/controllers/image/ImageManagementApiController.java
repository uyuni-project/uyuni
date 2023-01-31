/*
 * Copyright (c) 2023 SUSE LLC
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
package com.suse.manager.webui.controllers.image;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static spark.Spark.get;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.image.beans.ListImage;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;

/**
 * Spark controller class for image store pages and API endpoints.
 */
public class ImageManagementApiController {

    private static final Gson GSON = Json.GSON;
    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;
    private static Logger log = LogManager.getLogger(ImageManagementApiController.class);

    private ImageManagementApiController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     */
    public static void initRoutes() {
        get("/manager/api/cm/imagestores/listimages/:id", withOrgAdmin(ImageManagementApiController::listImages));
    }


    private static String listImages(Request requestIn, Response responseIn, User userIn) {
        Long storeId;
        try {
            storeId = Long.parseLong(requestIn.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Optional<ImageStore> store = ImageStoreFactory.lookupByIdAndOrg(storeId,
                userIn.getOrg());

        if (store.isEmpty()) {
            return json(responseIn, HttpStatus.SC_BAD_REQUEST, ResultJson.error("not_found"));
        }

        try {
            List<ListImage> images = SkopeoCommandManager.getImageList(store.get());
            List<String> listNames = images.stream().map(t -> t.getName()).collect(Collectors.toList());
            JsonObject json = new JsonObject();
            json.addProperty("size", listNames.size());
            json.addProperty("images", GSON.toJson(listNames));
            return json(responseIn, ResultJson.success(json));
        }
        catch (RuntimeException e) {
            return json(responseIn, HttpStatus.SC_INTERNAL_SERVER_ERROR, ResultJson.error(e.getMessage()));
        }
    }
}
