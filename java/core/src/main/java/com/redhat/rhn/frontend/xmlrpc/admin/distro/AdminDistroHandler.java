/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.admin.distro;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.IOFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.manager.kickstart.tree.DistroUploadManager;

import com.suse.manager.api.ApiIgnore;
import com.suse.manager.api.ApiType;
import com.suse.manager.webui.controllers.utils.MultipartRequestUtil;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AdminDistroHandler
 * @apidoc.namespace admin.distro
 * @apidoc.doc Provides methods to manage distribution upload data.
 */
public class AdminDistroHandler extends BaseHandler {

    private static final Logger LOG = LogManager.getLogger(AdminDistroHandler.class);

    /** Largest accepted distribution name, the file name length limit of common filesystems. */
    private static final int MAX_NAME_BYTES = 255;

    private final DistroUploadManager distroUploadManager;

    /**
     * Create handler with the default distro upload manager.
     */
    public AdminDistroHandler() {
        this(new DistroUploadManager());
    }

    /**
     * Create handler with a specific distro upload manager.
     * @param distroUploadManagerIn the upload manager
     */
    public AdminDistroHandler(DistroUploadManager distroUploadManagerIn) {
        this.distroUploadManager = distroUploadManagerIn;
    }

    /**
     * Upload a distro ISO to the server distribution storage.
     * @param loggedInUser the current user
     * @param request the multipart HTTP request containing the ISO in a file field named distro, and
     * optionally the distribution name in a form field named either name or filename
     * @return 1 on success
     *
     * @apidoc.doc Upload a distro ISO to the server distribution storage. The ISO is extracted into
     * a distribution tree named after the given name, or the ISO file name without its ".iso" suffix.
     * This method is only available in the HTTP API. The request must use multipart/form-data
     * encoding, with the ISO in a file field named "distro" and optionally the distribution name in a
     * form field named "name" or "filename".
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    @ApiIgnore(ApiType.XMLRPC)
    public int uploadDistro(User loggedInUser, HttpServletRequest request) {
        ensureSatAdmin(loggedInUser);

        if (!distroUploadManager.tryStartUpload()) {
            throw new IOFaultException("Another distribution upload is already in progress");
        }
        List<DiskFileItem> items = List.of();
        try {
            if (!isMultipartRequest(request)) {
                throw new IOException("Distro upload requires multipart/form-data");
            }

            distroUploadManager.checkUploadSize(request.getContentLengthLong());
            Path spoolPath = distroUploadManager.prepareStagingPath();
            items = MultipartRequestUtil.parseMultipartRequest(request, spoolPath);
            storeDistro(items);
            return 1;
        }
        catch (IOException e) {
            throw new IOFaultException(e);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidArgsException(e.getMessage());
        }
        finally {
            deleteSpooledItems(items);
            distroUploadManager.finishUpload();
        }
    }

    private void storeDistro(List<DiskFileItem> items) throws IOException {
        DiskFileItem distroItem = MultipartRequestUtil.getRequiredFileItem(items, "distro");
        String filename = findNonBlankParam(items, "name")
                .or(() -> findNonBlankParam(items, "filename"))
                .orElse(distroItem.getName());

        distroUploadManager.uploadDistro(filename, isoPath -> moveInto(distroItem, isoPath));
    }

    private static Optional<String> findNonBlankParam(List<DiskFileItem> items, String name) {
        // Check the size before decoding, as the request size limit also applies to form fields
        boolean tooLong = items.stream()
                .anyMatch(item -> item.isFormField() && name.equals(item.getFieldName()) &&
                        item.getSize() > MAX_NAME_BYTES);
        if (tooLong) {
            throw new IllegalArgumentException("Parameter '" + name + "' exceeds " + MAX_NAME_BYTES + " bytes");
        }
        return MultipartRequestUtil.findStringParam(items, name).filter(value -> !value.isBlank());
    }

    /**
     * Remove spooled upload content that was not moved into place, e.g. after a failed upload.
     */
    private static void deleteSpooledItems(List<DiskFileItem> items) {
        for (DiskFileItem item : items) {
            try {
                item.delete();
            }
            catch (IOException e) {
                LOG.warn("Unable to delete spooled distro upload file {}", item.getPath(), e);
            }
        }
    }

    /**
     * Move an uploaded item to its target path.
     */
    private static void moveInto(DiskFileItem item, Path target) throws IOException {
        if (item.isInMemory()) {
            try (InputStream stream = item.getInputStream()) {
                Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        else {
            Files.move(item.getPath(), target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }
}
