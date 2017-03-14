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
package com.redhat.rhn.frontend.xmlrpc.image;

import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageProfileException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * ImageInfoHandler
 */
public class ImageInfoHandler extends BaseHandler {

    /**
     * List all images visible for the logged in user
     * @param loggedInUser The current User
     * @return Array of ImageInfo Objects
     *
     * @xmlrpc.doc List available Images
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array() $ImageInfoSerializer #array_end()
     */
    public List<ImageInfo> listImages(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        return ImageInfoFactory.listImageInfos(loggedInUser.getOrg());
    }

    /**
     * Get Image Details
     * @param loggedInUser The Current User
     * @param imageId the Image id
     * @return $ImageOverviewSerializer
     */
    public ImageOverview getDetails(User loggedInUser, long imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageOverview> opt = ImageInfoFactory.lookupOverviewByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        return opt.get();
    }

    /**
     * Schedule an Image Build
     * @param loggedInUser The current User
     * @param profileLabel The profile label
     * @param version The version
     * @param buildHostId The system ID of the build host
     * @param earliestOccurrence Earliest occurrence of the image build
     * @return the image build action id
     *
     * @xmlrpc.doc Schedule an Image Build
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "profileLabel")
     * @xmlrpc.param #param_descr("string", "version", "version to build or empty")
     * @xmlrpc.param #param_desc("int", "buildHostId", "system id of the build host")
     * @xmlrpc.param #param_desc("dateTime.iso8601", "earliestOccurrence",
     * "Earliest the build can run.")
     * @xmlrpc.returntype int - ID of the build action created.
     */
    public Long scheduleImageBuild(User loggedInUser, String profileLabel, String version,
            long buildHostId, Date earliestOccurrence) {
        ensureImageAdmin(loggedInUser);
        if (StringUtils.isEmpty(profileLabel)) {
            throw new IllegalArgumentException("Profile label cannot be empty.");
        }
        ImageProfile prof = ImageProfileFactory.lookupByLabelAndOrg(profileLabel,
                loggedInUser.getOrg());
        if (prof == null) {
            throw new NoSuchImageProfileException();
        }

        Server buildHost = ServerFactory.lookupByIdAndOrg(buildHostId,
                loggedInUser.getOrg());
        if (buildHost == null) {
            throw new NoSuchSystemException();
        }
        if (!buildHost.hasContainerBuildHostEntitlement()) {
            throw new NoSuchSystemException(buildHost.getHostname() +
                    " is not a valid container buildhost");
        }

        return ImageInfoFactory.scheduleBuild(buildHostId, version, prof,
                earliestOccurrence, loggedInUser);
    }
}
