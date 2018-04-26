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
import com.redhat.rhn.domain.org.Org;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Factory class for ImageProfile entity
 */
public class ImageProfileFactory extends HibernateFactory {

    private static ImageProfileFactory instance = new ImageProfileFactory();
    private static Logger log = Logger.getLogger(ImageProfileFactory.class);

    /**
     * Default constructor.
     * (public for testing reasons so that we can override it in tests)
     */
    private ImageProfileFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(ImageProfileFactory.class);
        }
        return log;
    }

    /**
     * Save a {@link ImageProfile}.
     *
     * @param profile the image profile to save
     */
    public static void save(ImageProfile profile) {
        instance.saveObject(profile);
    }

    /**
     * Delete a {@link ImageProfile}.
     *
     * @param profile the image profile to delete
     */
    public static void delete(ImageProfile profile) {
        instance.removeObject(profile);
    }

    /**
     * Lookup an ImageProfile by id
     * @param id the id
     * @return the image profile
     */
    public static Optional<ImageProfile> lookupById(long id) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageProfile> query = builder.createQuery(ImageProfile.class);

        Root<ImageProfile> root = query.from(ImageProfile.class);
        query.where(builder.equal(root.get("profileId"), id));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Lookup multiple image profiles by an id list and organization
     * @param ids image profile id list
     * @param org the organization
     * @return Returns a list of image profiles with the given ids if it exists
     * inside the organization
     */
    public static List<ImageProfile> lookupByIdsAndOrg(List<Long> ids, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageProfile> criteria = builder.createQuery(ImageProfile.class);
        Root<ImageProfile> root = criteria.from(ImageProfile.class);
        criteria.where(builder.and(
                root.get("profileId").in(ids),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Lookup an ImageProfile by id and organization
     * @param id the id
     * @param org the organization
     * @return the image profile
     */
    public static Optional<ImageProfile> lookupByIdAndOrg(long id, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageProfile> query = builder.createQuery(ImageProfile.class);

        Root<ImageProfile> root = query.from(ImageProfile.class);
        query.where(builder.and(
                builder.equal(root.get("profileId"), id),
                builder.equal(root.get("org"), org)));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Lookup an ImageProfile by label and organization
     * @param label the label
     * @param org the organization
     * @return Returns the ImageProfile
     */
    public static Optional<ImageProfile> lookupByLabelAndOrg(String label, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageProfile> criteria = builder.createQuery(ImageProfile.class);
        Root<ImageProfile> root = criteria.from(ImageProfile.class);
        criteria.where(
                builder.and(
                        builder.equal(root.get("label"), label),
                        builder.equal(root.get("org"), org))
                );
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * List all ImageProfiles from a given organization
     * @param org the organization
     * @return Returns a list of ImageProfiles
     */
    public static List<ImageProfile> listImageProfiles(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageProfile> criteria = builder.createQuery(ImageProfile.class);
        Root<ImageProfile> root = criteria.from(ImageProfile.class);
        criteria.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Gets the valid store type for an image profile.
     *
     * @param profile the image profile
     * @return the valid store type for profile
     */
    public static ImageStoreType getStoreTypeForProfile(ImageProfile profile) {
        switch (profile.getImageType()) {
        case ImageProfile.TYPE_DOCKERFILE:
            return ImageStoreFactory.TYPE_REGISTRY;
        case ImageProfile.TYPE_KIWI:
            return ImageStoreFactory.TYPE_OS_IMAGE;
        default:
            throw new IllegalArgumentException("Invalid type for image profile: " + profile.getImageType());
        }
    }

    /**
     * Save a {@link ProfileCustomDataValue}.
     *
     * @param customDataValue the image profile to save
     */
    public static void save(ProfileCustomDataValue customDataValue) {
        instance.saveObject(customDataValue);
    }

    /**
     * Delete a {@link ProfileCustomDataValue}.
     *
     * @param customDataValue the profile custom data value to delete
     */
    public static void delete(ProfileCustomDataValue customDataValue) {
        instance.removeObject(customDataValue);
    }
}
