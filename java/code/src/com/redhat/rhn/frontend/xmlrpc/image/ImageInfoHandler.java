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

import com.redhat.rhn.FaultException;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.impl.PublishedErrata;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageProfileException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.activationkey.NoSuchActivationKeyException;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ImageInfoHandler
 * @xmlrpc.namespace image
 * @xmlrpc.doc Provides methods to access and modify images.
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
     * @return ImageOverview Object
     *
     * @xmlrpc.doc Get details of an Image
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "imageId")
     * @xmlrpc.returntype $ImageOverviewSerializer
     */
    public ImageOverview getDetails(User loggedInUser, Integer imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageOverview> opt = ImageInfoFactory.lookupOverviewByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        return opt.get();
    }

    /**
     * Schedule an image import
     * @param loggedInUser The current user
     * @param name The name
     * @param version The version
     * @param buildHostId The system ID of the build host
     * @param storeLabel The store label
     * @param activationKey The activation key
     * @param earliestOccurrence Earliest occurrence of the following image inspect
     * @return the image inspect action id
     *
     * @xmlrpc.doc Import an image and schedule an inspect afterwards
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("string", "name", "image name as specified in the
     * store")
     * @xmlrpc.param #param_desc("string", "version", "version to import or empty")
     * @xmlrpc.param #param_desc("int", "buildHostId", "system ID of the build
     * host")
     * @xmlrpc.param #param("string", "storeLabel")
     * @xmlrpc.param #param_desc("string", "activationKey", "activation key to get
     * the channel data from")
     * @xmlrpc.param #param_desc("dateTime.iso8601", "earliestOccurrence", "earliest
     * the following inspect can run")
     * @xmlrpc.returntype int - ID of the inspect action created
     */
    public Long importImage(User loggedInUser, String name, String version,
            Integer buildHostId, String storeLabel, String activationKey,
            Date earliestOccurrence) {
        if (StringUtils.isEmpty(name)) {
            throw new InvalidParameterException("Image name cannot be empty.");
        }
        else if (StringUtils.isEmpty(storeLabel)) {
            throw new InvalidParameterException("Store label cannot be empty.");
        }

        ImageStore store =
                ImageStoreFactory.lookupBylabelAndOrg(storeLabel, loggedInUser.getOrg())
                        .orElseThrow(NoSuchImageStoreException::new);

        ActivationKey key = null;
        if (!StringUtils.isEmpty(activationKey)) {
            key = ActivationKeyFactory.lookupByKey(activationKey);
            if (key == null) {
                throw new NoSuchActivationKeyException();
            }
        }

        validateBuildHost(buildHostId, loggedInUser.getOrg(), store.getStoreType().getLabel());

        try {
            return ImageInfoFactory.scheduleImport(buildHostId, name, version, store,
                    Optional.ofNullable(key).map(ActivationKey::getChannels),
                    earliestOccurrence, loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Schedule an image build
     * @param loggedInUser The current user
     * @param profileLabel The profile label
     * @param version The version
     * @param buildHostId The system ID of the build host
     * @param earliestOccurrence Earliest occurrence of the image build
     * @return the image build action id
     *
     * @xmlrpc.doc Schedule an image build
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "profileLabel")
     * @xmlrpc.param #param_desc("string", "version", "version to build or empty")
     * @xmlrpc.param #param_desc("int", "buildHostId", "system id of the build host")
     * @xmlrpc.param #param_desc("dateTime.iso8601", "earliestOccurrence",
     * "earliest the build can run.")
     * @xmlrpc.returntype int - ID of the build action created.
     */
    public Long scheduleImageBuild(User loggedInUser, String profileLabel, String version,
            Integer buildHostId, Date earliestOccurrence) {
        ensureImageAdmin(loggedInUser);
        if (StringUtils.isEmpty(profileLabel)) {
            throw new InvalidParameterException("Profile label cannot be empty.");
        }
        ImageProfile profile =
                ImageProfileFactory.lookupByLabelAndOrg(profileLabel, loggedInUser.getOrg())
                        .orElseThrow(NoSuchImageProfileException::new);

        validateBuildHost(buildHostId, loggedInUser.getOrg(), profile.getTargetStore().getLabel());

        try {
            return ImageInfoFactory.scheduleBuild(buildHostId, version, profile,
                    earliestOccurrence, loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Returns a list of all errata that are relevant for the image
     *
     * @param loggedInUser The current user
     * @param imageId The id of the image in question
     * @return Returns an array of maps representing the errata that can be applied
     * @throws FaultException A FaultException is thrown if the server corresponding to
     * imageId cannot be found.
     *
     * @xmlrpc.doc Returns a list of all errata that are relevant for the image
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "imageId")
     * @xmlrpc.returntype
     *      #array()
     *          $ErrataOverviewSerializer
     *      #array_end()
     */
    public List<ErrataOverview> getRelevantErrata(User loggedInUser, Integer imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageOverview> opt = ImageInfoFactory.lookupOverviewByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }

        List<Long> eids = opt.get().getPatches().stream()
                .map(PublishedErrata::getId)
                .collect(Collectors.toList());
        return ErrataFactory.search(eids, loggedInUser.getOrg());
    }

    /**
     * List the installed packages on the given Image.
     * @param loggedInUser The current user
     * @param imageId The id of the image in question
     * @return Returns an array of maps representing the packages installed on an image
     * @throws FaultException A FaultException is thrown if the image corresponding to
     * imageId cannot be found.
     *
     * @xmlrpc.doc List the installed packages on the given image.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "imageId")
     * @xmlrpc.returntype
     *      #array()
     *          #struct("package")
     *                 #prop("string", "name")
     *                 #prop("string", "version")
     *                 #prop("string", "release")
     *                 #prop("string", "epoch")
     *                 #prop("string", "arch")
     *          #struct_end()
     *      #array_end()
     */
    public List<Map<String, Object>> listPackages(User loggedInUser, Integer imageId)
            throws FaultException {
        ensureImageAdmin(loggedInUser);
        Optional<ImageOverview> opt = ImageInfoFactory.lookupOverviewByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        List<Map<String, Object>> ret = new ArrayList<>();
        opt.get().getPackages().forEach(pkg -> {
            Map<String, Object> pmap = new HashMap<>();
            pmap.put("name", pkg.getName().getName());
            pmap.put("version", pkg.getEvr().getVersion());
            pmap.put("release", pkg.getEvr().getRelease());
            pmap.put("epoch", pkg.getEvr().getEpoch());
            pmap.put("arch", pkg.getArch().getLabel());
            ret.add(pmap);
        });
        return ret;
    }

    /**
     * Get the custom data values defined for the Image
     * @param loggedInUser The current user
     * @param imageId the image ID
     * @return Returns a map containing the defined custom data values for the
     * given Image.
     *
     * @xmlrpc.doc Get the custom data values defined for the Image.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "imageId")
     * @xmlrpc.returntype
     *    #struct("Map of custom labels to custom values")
     *      #prop("string", "custom info label")
     *      #prop("string", "value")
     *    #struct_end()
     */
    public Map<String, String> getCustomValues(User loggedInUser, Integer imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageOverview> opt = ImageInfoFactory.lookupOverviewByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        return opt.get().getCustomDataValues().stream()
                .collect(Collectors.toMap(a -> a.getKey().getLabel(), a -> a.getValue()));
    }

    /**
     * Delete an Image
     * @param loggedInUser The current User
     * @param imageId the image id
     * @return 1 on success
     *
     * @xmlrpc.doc Delete an Image
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "imageId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, Integer imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageInfo> opt = ImageInfoFactory.lookupByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        ImageInfoFactory.delete(opt.get());
        return 1;
    }

    private Server validateBuildHost(long buildHostId, Org org, String storeTypeLabel) {
        Server buildHost = ServerFactory.lookupByIdAndOrg(buildHostId, org);
        if (buildHost == null) {
            throw new NoSuchSystemException();
        }
        Optional<ImageStoreType> imageStoreType = ImageStoreFactory.lookupStoreTypeByLabel(storeTypeLabel);
        imageStoreType.ifPresent(storeType -> {
            if (storeType.equals(ImageStoreFactory.TYPE_REGISTRY)) {
                if (!buildHost.hasContainerBuildHostEntitlement()) {
                    throw new NoSuchSystemException(
                            buildHost.getHostname() + " is not a valid container buildhost");
                }
            }
            else if (storeType.equals(ImageStoreFactory.TYPE_OS_IMAGE)) {
                if (!buildHost.hasOSImageBuildHostEntitlement()) {
                    throw new NoSuchSystemException(
                            buildHost.getHostname() + " is not a valid OS image buildhost");
                }
            }
        });

        return buildHost;
    }
}
