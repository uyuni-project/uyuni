/*
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
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.image.ImageFile;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoCustomDataValue;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageProfileException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.activationkey.NoSuchActivationKeyException;
import com.redhat.rhn.frontend.xmlrpc.util.PillarUtils;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.webui.services.iface.SaltApi;

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
 * @apidoc.namespace image
 * @apidoc.doc Provides methods to access and modify images.
 */
public class ImageInfoHandler extends BaseHandler {

    private final SaltApi saltApi;

    /**
     * Constructor
     *
     * @param saltApiIn the salt API
     */
    public ImageInfoHandler(SaltApi saltApiIn) {
        saltApi = saltApiIn;
    }



    /**
     * List all images visible for the logged in user
     * @param loggedInUser The current User
     * @return Array of ImageInfo Objects
     *
     * @apidoc.doc List available images
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin() $ImageInfoSerializer #array_end()
     */
    @ReadOnly
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
     * @apidoc.doc Get details of an image
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "imageId")
     * @apidoc.returntype $ImageOverviewSerializer
     */
    @ReadOnly
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
     * Get Image Pillar
     * @param loggedInUser The Current User
     * @param imageId the Image id
     * @return the pillar
     *
     * @apidoc.doc Get pillar data of an image. The "size" entries are converted to string.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "imageId")
     * @apidoc.returntype #param("struct", "the pillar data")
     */
    @ReadOnly
    public Map<String, Object> getPillar(User loggedInUser, Integer imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageInfo> opt = ImageInfoFactory.lookupByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        return Optional.ofNullable(opt.get().getPillar())
                       .map(p -> PillarUtils.convertSizeToString(p.getPillar()))
                       .orElseGet(() -> new HashMap<String, Object>());
    }

    /**
     * Set Image Pillar
     * @param loggedInUser The Current User
     * @param imageId the Image id
     * @param pillarData the new pillar
     * @return 1 on success
     *
     * @apidoc.doc Set pillar data of an image. The "size" entries should be passed as string.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "imageId")
     * @apidoc.param #param("struct", "pillarData")
     * @apidoc.returntype #return_int_success()
     */
    public int setPillar(User loggedInUser, Integer imageId, Map<String, Object> pillarData) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageInfo> imageOpt = ImageInfoFactory.lookupByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!imageOpt.isPresent()) {
            throw new NoSuchImageException();
        }
        Map<String, Object> pillarDataFixed = PillarUtils.convertSizeToLong(pillarData);
        Optional.ofNullable(imageOpt.get().getPillar()).ifPresentOrElse(
            p -> p.setPillar(pillarDataFixed),
            () -> {
                Pillar newPillar = new Pillar("Image" + imageId, pillarDataFixed, loggedInUser.getOrg());
                HibernateFactory.getSession().save(newPillar);
                imageOpt.get().setPillar(newPillar);
            });
        return 1;
    }

    /**
     * @deprecated Schedule a Container image import
     * @param loggedInUser The current user
     * @param name The name
     * @param version The version
     * @param buildHostId The system ID of the build host
     * @param storeLabel The store label
     * @param activationKey The activation key
     * @param earliestOccurrence Earliest occurrence of the following image inspect
     * @return the image inspect action id
     *
     * @apidoc.doc Import an image and schedule an inspect afterwards
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "name", "image name as specified in the store")
     * @apidoc.param #param_desc("string", "version", "version to import or empty")
     * @apidoc.param #param_desc("int", "buildHostId", "system ID of the build host")
     * @apidoc.param #param("string", "storeLabel")
     * @apidoc.param #param_desc("string", "activationKey", "activation key to get the channel data from")
     * @apidoc.param #param_desc("$date", "earliestOccurrence", "earliest the following inspect can run")
     * @apidoc.returntype #param("int", "the ID of the inspect action created")
     */
    @Deprecated
    public Long importImage(User loggedInUser, String name, String version,
            Integer buildHostId, String storeLabel, String activationKey,
            Date earliestOccurrence) {
        return  importContainerImage(loggedInUser, name, version,
            buildHostId, storeLabel, activationKey, earliestOccurrence);
    }


    /**
     * Schedule a Container image import
     * @param loggedInUser The current user
     * @param name The name
     * @param version The version
     * @param buildHostId The system ID of the build host
     * @param storeLabel The store label
     * @param activationKey The activation key
     * @param earliestOccurrence Earliest occurrence of the following image inspect
     * @return the image inspect action id
     *
     * @apidoc.doc Import an image and schedule an inspect afterwards
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "name", "image name as specified in the store")
     * @apidoc.param #param_desc("string", "version", "version to import or empty")
     * @apidoc.param #param_desc("int", "buildHostId", "system ID of the build host")
     * @apidoc.param #param("string", "storeLabel")
     * @apidoc.param #param_desc("string", "activationKey", "activation key to get the channel data from")
     * @apidoc.param #param_desc("$date", "earliestOccurrence", "earliest the following inspect can run")
     * @apidoc.returntype #param("int", "the ID of the inspect action created")
     */
    public Long importContainerImage(User loggedInUser, String name, String version,
            Integer buildHostId, String storeLabel, String activationKey,
            Date earliestOccurrence) {
        ensureImageAdmin(loggedInUser);
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
     * Import an OS image
     * @param loggedInUser The current user
     * @param name The name
     * @param version The version
     * @param arch The architecture
     * @return the image id
     *
     * @apidoc.doc Import an image and schedule an inspect afterwards
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "name", "image name as specified in the store")
     * @apidoc.param #param_desc("string", "version", "version to import")
     * @apidoc.param #param_desc("string", "arch", "image architecture")
     * @apidoc.returntype #param("int", "the ID of the image")
     */
    public Long importOSImage(User loggedInUser, String name, String version, String arch) {
        ensureImageAdmin(loggedInUser);
        if (StringUtils.isEmpty(name)) {
            throw new InvalidParameterException("Image name cannot be empty.");
        }
        ImageStore store = ImageStoreFactory.lookupBylabelAndOrg("SUSE Manager OS Image Store", loggedInUser.getOrg())
                        .orElseThrow(NoSuchImageStoreException::new);

        // Create an image info entry
        ImageInfo info = new ImageInfo();

        info.setName(name);
        info.setVersion(version);
        info.setStore(store);
        info.setOrg(loggedInUser.getOrg());
        info.setImageType(ImageProfile.TYPE_KIWI);
        info.setBuilt(true);

        // Image arch should be the same as the build host
        // If this is not the case, we can set the correct value in the inspect action
        // return event
        info.setImageArch(ServerFactory.lookupServerArchByLabel(arch));
        ImageInfoFactory.save(info);


        ImageInfoFactory.updateRevision(info);

        ImageInfoFactory.save(info);

        return info.getId();

    }

    /**
     * Delete image file
     * @param loggedInUser The Current User
     * @param imageId the Image id
     * @param file the file name
     * @return 1 on success
     *
     * @apidoc.doc Delete image file
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "imageId", "ID of the image")
     * @apidoc.param #param_desc("string", "file", "the file name")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteImageFile(User loggedInUser, Integer imageId, String file) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageFile> opt = ImageInfoFactory.lookupImageFile(loggedInUser.getOrg(), file);

        if (!opt.isPresent()) {
            throw new EntityNotExistsFaultException(file);
        }

        if (opt.get().getImageInfo().getId() != imageId.longValue()) {
            // the found file belongs to different image
            // there is no file attached to this image
            throw new EntityNotExistsFaultException(file);
        }

        ImageInfoFactory.deleteImageFile(opt.get(), saltApi);

        return 1;
    }

    /**
     * Add image file to an OS image
     * @param loggedInUser The Current User
     * @param imageId the Image id
     * @param file the file name, it must exist in the store
     * @param type the image type
     * @param external the file is external
     * @return 1 on success
     *
     * @apidoc.doc Delete image file
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "imageId", "ID of the image")
     * @apidoc.param #param_desc("string", "file", "the file name, it must exist in the store")
     * @apidoc.param #param_desc("string", "type", "the image type")
     * @apidoc.param #param_desc("boolean", "external", "the file is external")
     * @apidoc.returntype #return_int_success()
     */
    public Long addImageFile(User loggedInUser, Integer imageId, String file, String type, Boolean external) {
        ensureImageAdmin(loggedInUser);

        Optional<ImageInfo> opt = ImageInfoFactory.lookupByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }

        if (ImageInfoFactory.lookupImageFile(loggedInUser.getOrg(), file).isPresent()) {
            throw new EntityExistsFaultException(file);
        }

        if (ImageInfoFactory.lookupDeltaImageFile(loggedInUser.getOrg(), file).isPresent()) {
            throw new EntityExistsFaultException(file);
        }

        ImageFile imageFile = new ImageFile();
        imageFile.setFile(file);
        imageFile.setType(type);
        imageFile.setExternal(external);
        imageFile.setImageInfo(opt.get());
        opt.get().getImageFiles().add(imageFile);
        return 1L;
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
     * @apidoc.doc Schedule an image build
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "profileLabel")
     * @apidoc.param #param_desc("string", "version", "version to build or empty")
     * @apidoc.param #param_desc("int", "buildHostId", "system id of the build host")
     * @apidoc.param #param_desc("$date", "earliestOccurrence", "earliest the build can run.")
     * @apidoc.returntype #param("int", "the ID of the build action created")
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

        validateBuildHost(buildHostId, loggedInUser.getOrg(), profile.getTargetStore().getStoreType().getLabel());

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
     * @apidoc.doc Returns a list of all errata that are relevant for the image
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "imageId")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ErrataOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ErrataOverview> getRelevantErrata(User loggedInUser, Integer imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageOverview> opt = ImageInfoFactory.lookupOverviewByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }

        List<Long> eids = opt.get().getPatches().stream()
                .map(Errata::getId)
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
     * @apidoc.doc List the installed packages on the given image
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "imageId")
     * @apidoc.returntype
     *      #return_array_begin()
     *          #struct_begin("package")
     *                 #prop("string", "name")
     *                 #prop("string", "version")
     *                 #prop("string", "release")
     *                 #prop("string", "epoch")
     *                 #prop("string", "arch")
     *          #struct_end()
     *      #array_end()
     */
    @ReadOnly
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
     * @apidoc.doc Get the custom data values defined for the image
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "imageId")
     * @apidoc.returntype
     *    #struct_begin("the map of custom labels to custom values")
     *      #prop("string", "custom info label")
     *      #prop("string", "value")
     *    #struct_end()
     */
    @ReadOnly
    public Map<String, String> getCustomValues(User loggedInUser, Integer imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageOverview> opt = ImageInfoFactory.lookupOverviewByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        return opt.get().getCustomDataValues().stream()
                .collect(Collectors.toMap(a -> a.getKey().getLabel(), ImageInfoCustomDataValue::getValue));
    }

    /**
     * Delete an Image
     * @param loggedInUser The current User
     * @param imageId the image id
     * @return 1 on success
     *
     * @apidoc.doc Delete an image
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "imageId")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, Integer imageId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageInfo> opt = ImageInfoFactory.lookupByIdAndOrg(imageId,
                loggedInUser.getOrg());
        if (!opt.isPresent()) {
            throw new NoSuchImageException();
        }
        ImageInfoFactory.deleteWithObsoletes(opt.get(), saltApi);
        return 1;
    }

    private Server validateBuildHost(long buildHostId, Org org, String storeTypeLabel) {
        Server buildHost = ServerFactory.lookupByIdAndOrg(buildHostId, org);
        if (buildHost == null) {
            throw new NoSuchSystemException();
        }
        Optional<ImageStoreType> imageStoreType = ImageStoreFactory.lookupStoreTypeByLabel(storeTypeLabel);
        return imageStoreType.map(storeType -> {
            if (storeType.equals(ImageStoreFactory.TYPE_REGISTRY) && !buildHost.hasContainerBuildHostEntitlement()) {
                throw new NoSuchSystemException(
                        buildHost.getHostname() + " is not a valid container buildhost");
            }
            else if (storeType.equals(ImageStoreFactory.TYPE_OS_IMAGE) && !buildHost.hasOSImageBuildHostEntitlement()) {
                throw new NoSuchSystemException(
                        buildHost.getHostname() + " is not a valid OS image buildhost");
            }
            return buildHost;
        }).orElseThrow(NoSuchSystemException::new);
    }
}
