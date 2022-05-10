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
package com.redhat.rhn.frontend.xmlrpc.image;

import com.redhat.rhn.domain.image.DeltaImageInfo;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageException;

import com.suse.manager.api.ReadOnly;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DeltaImageInfoHandler
 * @apidoc.namespace image.delta
 * @apidoc.doc Provides methods to access and modify delta images.
 */
public class DeltaImageInfoHandler extends BaseHandler {

    /**
     * List all delta images visible for the logged in user
     * @param loggedInUser The current User
     * @return Array of DeltaImageInfo Objects
     *
     * @apidoc.doc List available DeltaImages
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin() $DeltaImageSerializer #array_end()
     */
    @ReadOnly
    public List<DeltaImageInfo> listDeltas(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        return ImageInfoFactory.listDeltaImageInfos(loggedInUser.getOrg());
    }

    /**
     * Get Image Details
     * @param loggedInUser The Current User
     * @param sourceImageId the source Image id
     * @param targetImageId the target Image id
     * @return ImageOverview Object
     *
     * @apidoc.doc Get details of an Image
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sourceImageId")
     * @apidoc.param #param("int", "targetImageId")
     * @apidoc.returntype $DeltaImageSerializer
     */
    @ReadOnly
    public DeltaImageInfo getDetails(User loggedInUser, Integer sourceImageId, Integer targetImageId) {
        ensureImageAdmin(loggedInUser);
        Optional<DeltaImageInfo> opt = ImageInfoFactory.lookupDeltaImageInfo(sourceImageId, targetImageId);
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        return opt.get();
    }

    /**
     * Create DeltaImage record
     * @param loggedInUser The current user
     * @param sourceImageId the source Image id
     * @param targetImageId the target Image id
     * @param file the file path
     * @param pillar pillar data
     * @return 1 on success
     *
     * @apidoc.doc Import an image and schedule an inspect afterwards
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sourceImageId")
     * @apidoc.param #param("int", "targetImageId")
     * @apidoc.param #param("string", "file")
     * @apidoc.param #param("struct", "pillar")
     * @apidoc.returntype #return_int_success()
     */
    public Long createDeltaImage(User loggedInUser, Integer sourceImageId, Integer targetImageId,
            String file, Map<String, Object> pillar) {
        ensureImageAdmin(loggedInUser);

        Optional<ImageInfo> sourceOpt = ImageInfoFactory.lookupByIdAndOrg(sourceImageId,
                loggedInUser.getOrg());
        Optional<ImageInfo> targetOpt = ImageInfoFactory.lookupByIdAndOrg(targetImageId,
                loggedInUser.getOrg());

        if (!sourceOpt.isPresent() || !targetOpt.isPresent()) {
            throw new NoSuchImageException();
        }

        Optional<DeltaImageInfo> existing = ImageInfoFactory.lookupDeltaImageInfo(sourceImageId, targetImageId);
        if (existing.isPresent()) {
            throw new EntityExistsFaultException(existing.get());
        }

        ImageInfoFactory.createDeltaImageInfo(sourceOpt.get(), targetOpt.get(), file, pillar);

        return 1L;
    }

}
