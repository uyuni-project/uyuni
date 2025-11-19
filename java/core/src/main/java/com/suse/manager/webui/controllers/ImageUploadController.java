/*
 * Copyright (c) 2022 SUSE LLC
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

import static com.suse.manager.webui.services.SaltConstants.SALT_FILE_GENERATION_TEMP_PATH;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.OSImageStoreUtils;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;

import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Controller class for image file upload.
 */
public class ImageUploadController {

    private static final Logger LOG = LogManager.getLogger(ImageUploadController.class);

    private ImageUploadController() { }

    /**
     * Initialize request routes for the pages served by ImageUploadController
     *
     */
    public static void initRoutes() {

        Spark.post("/manager/upload/image", withUser(ImageUploadController::uploadImage));
    }


    /**
     * Upload Image File
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String uploadImage(Request request, Response response, User user) {
        try {
            DiskFileItemFactory fileItemFactory = DiskFileItemFactory.builder()
                    .setBufferSize(0)
                    .setPath(SALT_FILE_GENERATION_TEMP_PATH)
                    .get();

            List<? extends FileItem> items = new JakartaServletFileUpload<>(fileItemFactory).parseRequest(request.raw());

            try {
                items.stream().forEach(item -> {
                    if (ImageInfoFactory.lookupDeltaImageFile(user.getOrg(), item.getName()).isPresent() ||
                        ImageInfoFactory.lookupImageFile(user.getOrg(), item.getName()).isPresent()) {
                        throw new EntityExistsException("Image file already exists");
                    }
                    DiskFileItem diskFileItem = (DiskFileItem) item;
                    if (diskFileItem == null) {
                        throw new RuntimeException("Can't get uploaded file");
                    }

                    Path tempFile = null;
                    try {
                        tempFile = Files.createTempFile("upload-", ".img");
                        diskFileItem.write(tempFile);

                    }
                    catch (IOException eIn) {
                        throw new RuntimeException("cannot create tmp file", eIn);
                    }

                    // copy file to final location using salt
                    GlobalInstanceHolder.SALT_API.copyFile(tempFile,
                        Paths.get(OSImageStoreUtils.getOSImageStorePathForOrg(user.getOrg()) + item.getName()))
                        .orElseThrow(() -> new RuntimeException("Can't move the image file"));
                });
            }
            finally {
                // do not rely on GC, delete the file now
                items.stream().forEach(item -> {
                    DiskFileItem diskFileItem = (DiskFileItem) item;
                    if (diskFileItem != null) {
                        try {
                            diskFileItem.delete();
                        }
                        catch (IOException eIn) {
                            LOG.error("cannot delete file", eIn);
                        }
                    }
                });
            }
        }
        catch (Exception e) {
            LOG.error("Image upload error", e);
            return internalServerError(response, e.getMessage());
        }
        return result(response, ResultJson.success(), new TypeToken<>() { });
    }
}
