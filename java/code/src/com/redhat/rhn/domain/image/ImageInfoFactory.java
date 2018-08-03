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
package com.redhat.rhn.domain.image;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.ChecksumFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.Checksum;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.SHA1Checksum;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.SHA256Checksum;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.SHA384Checksum;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.SHA512Checksum;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Factory class for ImageInfo entity
 */
public class ImageInfoFactory extends HibernateFactory {

    private static ImageInfoFactory instance = new ImageInfoFactory();
    private static Logger log = Logger.getLogger(ImageInfoFactory.class);
    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    /**
     * Default constructor.
     * (public for testing reasons so that we can override it in tests)
     */
    private ImageInfoFactory() {
        super();
    }

    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(ImageInfoFactory.class);
        }
        return log;
    }

    /**
     * Schedule an Image Build
     * @param buildHostId The ID of the build host
     * @param version the tag/version of the resulting image
     * @param profile the profile
     * @param earliest earliest build
     * @param user the current user
     * @return the action ID
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Long scheduleBuild(long buildHostId, String version, ImageProfile profile,
            Date earliest, User user) throws TaskomaticApiException {
        MinionServer server = ServerFactory.lookupById(buildHostId).asMinionServer().get();

        if ((!server.hasContainerBuildHostEntitlement() && profile.asDockerfileProfile().isPresent()) ||
                (!server.hasOSImageBuildHostEntitlement() && profile.asKiwiProfile().isPresent())) {
            throw new IllegalArgumentException("Server is not a build host.");
        }

        if (profile.asDockerfileProfile().isPresent()) {
            version = version.isEmpty() ? "latest" : version;
        }

        // Schedule the build
        ImageBuildAction action = ActionManager.scheduleImageBuild(user,
                Collections.singletonList(server.getId()), version, profile, earliest);
        taskomaticApi.scheduleActionExecution(action);

        // Load or create an image info entry
        ImageInfo info =
                lookupByName(profile.getLabel(), version, profile.getTargetStore().getId())
                .orElseGet(ImageInfo::new);

        info.setName(profile.getLabel());
        info.setVersion(version);
        info.setStore(profile.getTargetStore());
        info.setOrg(server.getOrg());
        info.setBuildAction(action);
        info.setProfile(profile);
        info.setImageType(profile.getImageType());
        info.setBuildServer(server);
        if (profile.getToken() != null) {
            info.setChannels(new HashSet<>(profile.getToken().getChannels()));
        }

        // Image arch should be the same as the build host
        // If this is not the case, we can set the correct value in the inspect action
        // return event
        info.setImageArch(server.getServerArch());

        // Checksum will be available from inspect

        // Copy custom data values from image profile
        if (profile.getCustomDataValues() != null) {
            profile.getCustomDataValues().forEach(cdv -> info.getCustomDataValues()
                    .add(new ImageInfoCustomDataValue(cdv, info)));
        }

        save(info);
        return action.getId();
    }

    /**
     * Schedule inspect.
     *
     * @param image     the image
     * @param earliest  the earliest schedule date
     * @param user      the user
     * @return          the inspect action ID
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Long scheduleInspect(ImageInfo image, Date earliest, User user)
            throws TaskomaticApiException {
        MinionServer server = image.getBuildServer();

        if ((!server.hasContainerBuildHostEntitlement() && image.getImageType().equals(ImageProfile.TYPE_DOCKERFILE)) ||
                (!server.hasOSImageBuildHostEntitlement() && image.getImageType().equals(ImageProfile.TYPE_KIWI))) {
            throw new IllegalArgumentException("Server is not a build host.");
        }

        ImageInspectAction action = ActionManager.scheduleImageInspect(user,
                Collections.singletonList(server.getId()),
                Optional.ofNullable(image.getBuildAction() == null ? null : image.getBuildAction().getId()),
                image.getVersion(),
                image.getName(), image.getStore(), earliest);
        taskomaticApi.scheduleActionExecution(action);

        image.setInspectAction(action);
        ImageInfoFactory.save(image);

        return action.getId();
    }

    /**
     * Schedule an Image Import
     * @param buildHostId The ID of the build host
     * @param name the image name
     * @param version the tag/version of the image
     * @param store the image store
     * @param channels the set of channels used for inspection
     * @param earliest earliest build
     * @param user the current user
     * @return the action ID
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Long scheduleImport(long buildHostId, String name, String version,
            ImageStore store, Optional<Set<Channel>> channels, Date earliest, User user)
        throws TaskomaticApiException {
        MinionServer server = ServerFactory.lookupById(buildHostId).asMinionServer().get();

        if (!server.hasContainerBuildHostEntitlement()) {
            throw new IllegalArgumentException("Server is not a build host.");
        }

        // Check if the image name:version is available
        if (lookupByName(name, version, store.getId()).isPresent()) {
            throw new IllegalArgumentException("Image already exists.");
        }

        // Create an image info entry
        ImageInfo info = new ImageInfo();

        info.setExternalImage(true);
        info.setName(name);
        info.setVersion(version);
        // Import is only possible for container images
        info.setImageType(ImageProfile.TYPE_DOCKERFILE);
        info.setStore(store);
        info.setOrg(server.getOrg());
        info.setBuildServer(server);

        channels.ifPresent(ch -> info.setChannels(new HashSet<>(ch)));

        // Image arch should be the same as the build host
        // If this is not the case, we can set the correct value in the inspect action
        // return event
        info.setImageArch(server.getServerArch());

        save(info);
        return scheduleInspect(info, earliest, user);
    }

    /**
     * Save a {@link ImagePackage}.
     *
     * @param imagePackage the image package to save
     */
    public static void save(ImagePackage imagePackage) {
        instance.saveObject(imagePackage);
    }


        /**
         * Save a {@link ImageInfo}.
         *
         * @param imageInfo the image info to save
         */
    public static void save(ImageInfo imageInfo) {
        instance.saveObject(imageInfo);
    }

    /**
     * Delete a {@link ImageInfo}.
     *
     * @param imageInfo the image info to delete
     */
    public static void delete(ImageInfo imageInfo) {
        instance.removeObject(imageInfo);
    }

    /**
     * Lookup an ImageInfo by id
     * @param id the id
     * @return the image info
     */
    public static Optional<ImageInfo> lookupById(long id) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageInfo> query = builder.createQuery(ImageInfo.class);

        Root<ImageInfo> root = query.from(ImageInfo.class);
        query.where(builder.equal(root.get("id"), id));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Lookup an ImageInfo by image build action
     * @param action the build action
     * @return the optional image info
     */
    public static Optional<ImageInfo> lookupByBuildAction(ImageBuildAction action) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageInfo> query = builder.createQuery(ImageInfo.class);

        Root<ImageInfo> root = query.from(ImageInfo.class);
        query.where(builder.equal(root.get("buildAction"), action));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Lookup an image info by id and organization
     * @param id the id
     * @param org the organization
     * @return the image profile
     */
    public static Optional<ImageInfo> lookupByIdAndOrg(long id, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageInfo> query = builder.createQuery(ImageInfo.class);

        Root<ImageInfo> root = query.from(ImageInfo.class);
        query.where(builder.and(
                builder.equal(root.get("id"), id),
                builder.equal(root.get("org"), org)));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Lookup multiple images by an id list and organization
     * @param ids image info id list
     * @param org the organization
     * @return Returns a list of images with the given ids if they exist
     * inside the organization
     */
    public static List<ImageInfo> lookupByIdsAndOrg(List<Long> ids, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageInfo> criteria = builder.createQuery(ImageInfo.class);
        Root<ImageInfo> root = criteria.from(ImageInfo.class);
        criteria.where(builder.and(
                root.get("id").in(ids),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Lookup an image overview by id and organization
     * @param id the id
     * @param org the organization
     * @return the image profile
     */
    public static Optional<ImageOverview> lookupOverviewByIdAndOrg(long id, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageOverview> query = builder.createQuery(ImageOverview.class);

        Root<ImageOverview> root = query.from(ImageOverview.class);
        query.where(builder.and(
                builder.equal(root.get("id"), id),
                builder.equal(root.get("org"), org)));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * List all image infos
     * @return Returns a list of ImageInfos
     */
    public static List<ImageInfo> list() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageInfo> criteria = builder.createQuery(ImageInfo.class);
        Root<ImageInfo> root = criteria.from(ImageInfo.class);
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * List all image infos from a given organization
     * @param org the organization
     * @return Returns a list of ImageInfos
     */
    public static List<ImageInfo> listImageInfos(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageInfo> criteria = builder.createQuery(ImageInfo.class);
        Root<ImageInfo> root = criteria.from(ImageInfo.class);
        criteria.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Lookup an image info by name, version and image store.
     *
     * @param name             the name
     * @param version          the version/tag
     * @param imageStoreId the image store id
     * @return the optional
     */
    public static Optional<ImageInfo> lookupByName(String name, String version, long imageStoreId) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageInfo> query = builder.createQuery(ImageInfo.class);

        Root<ImageInfo> root = query.from(ImageInfo.class);
        query.where(builder.and(
                builder.equal(root.get("name"), name),
                StringUtils.isEmpty(version) ?
                        builder.isNull(root.get("version")) : builder.equal(root.get("version"), version),
                builder.equal(root.get("store"), imageStoreId)));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * List all image overviews from a given organization
     * @param org the organization
     * @return Returns a list of ImageProfiles
     */
    public static List<ImageOverview> listImageOverviews(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageOverview> criteria = builder.createQuery(ImageOverview.class);
        Root<ImageOverview> root = criteria.from(ImageOverview.class);
        criteria.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Convert a docker Checksum into a DB checksum
     * @param dockerChecksum the docker checksum
     * @return the db checksum
     */
    public static com.redhat.rhn.domain.common.Checksum convertChecksum(
            Checksum dockerChecksum) {
        String checksumType = "sha256";
        if (dockerChecksum instanceof SHA1Checksum) {
            checksumType = "sha1";
        }
        else  if (dockerChecksum instanceof SHA256Checksum) {
            checksumType = "sha256";
        }
        else if (dockerChecksum instanceof SHA384Checksum) {
            checksumType = "sha384";
        }
        else if (dockerChecksum instanceof SHA512Checksum) {
            checksumType = "sha512";
        }
        return ChecksumFactory.safeCreate(dockerChecksum.getChecksum(), checksumType);
    }

    /**
     * Convert the DB checksum into a docker checksum
     * @param checksum the db checksum
     * @return the docker checksum
     */
    public static Checksum convertChecksum(
            com.redhat.rhn.domain.common.Checksum checksum) {
        switch (checksum.getChecksumType().getLabel()) {
        case "sha1":
            return new SHA1Checksum(checksum.getChecksum());
        case "sha256":
            return new SHA256Checksum(checksum.getChecksum());
        case "sha384":
            return new SHA384Checksum(checksum.getChecksum());
        case "sha512":
            return new SHA512Checksum(checksum.getChecksum());
        default:
            throw new IllegalArgumentException("Checksumtype not supported");
        }
    }

    /**
     * @return the image build history
     */
    public static List<ImageBuildHistory> listBuildHistory() {
        CriteriaQuery<ImageBuildHistory> criteria = getSession()
                .getCriteriaBuilder()
                .createQuery(ImageBuildHistory.class);
        criteria.from(ImageBuildHistory.class);
        return getSession().createQuery(criteria).getResultList();
    }
}
