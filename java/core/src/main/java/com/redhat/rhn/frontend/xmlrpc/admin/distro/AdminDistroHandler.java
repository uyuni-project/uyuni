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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.admin.distro;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.manager.kickstart.tree.DistroUploadManager;

import com.suse.manager.webui.controllers.utils.MultipartRequestUtil;

import org.apache.commons.fileupload2.core.DiskFileItem;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AdminDistroHandler
 * @apidoc.namespace admin.distro
 * @apidoc.doc Provides methods to manage distribution upload data.
 */
public class AdminDistroHandler extends BaseHandler {

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
     * @param request the multipart HTTP request containing a file field named distro and optional name field
     * @return 1 on success
     *
     * @apidoc.doc Upload a distro ISO to the server distribution storage.
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    public int uploadDistro(User loggedInUser, HttpServletRequest request) {
        ensureSatAdmin(loggedInUser);

        try {
            if (!isMultipartRequest(request)) {
                throw new IOException("Distro upload requires multipart/form-data");
            }

            List<DiskFileItem> items = MultipartRequestUtil.parseMultipartRequest(request);
            DiskFileItem distroItem = MultipartRequestUtil.getRequiredFileItem(items, "distro");
            String filename = MultipartRequestUtil.findStringParam(items, "name")
                    .or(() -> MultipartRequestUtil.findStringParam(items, "filename"))
                    .orElse(distroItem.getName());

            distroUploadManager.uploadDistro(filename, distroItem.getInputStream());
            return 1;
        }
        catch (Exception e) {
            throw new RhnRuntimeException("Unable to upload distro", e);
        }
    }

    private static boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }
}
