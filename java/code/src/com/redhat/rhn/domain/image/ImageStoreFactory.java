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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

/**
 * ImageStoreFactory
 */
public class ImageStoreFactory extends HibernateFactory {

    private static ImageStoreFactory instance = new ImageStoreFactory();
    private static Logger log = Logger.getLogger(ImageStoreFactory.class);

    /**
     * Default constructor.
     * (public for testing reasons so that we can override it in tests)
     */
    private ImageStoreFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(ImageStoreFactory.class);
        }
        return log;
    }

    /**
     * Delete a {@link ImageStore}.
     *
     * @param store the image store to delete
     */
    public static void delete(ImageStore store) {
        instance.removeObject(store);
    }

    /**
     * Save a {@link ImageStore}.
     *
     * @param store the image store to save
     */
    public static void save(ImageStore store) {
        instance.saveObject(store);
    }

    /**
     * Lookup ImageStoreType from given label
     * @param label the label to search for
     * @return Returns the ImageStoreType
     */
    public static ImageStoreType lookupStoreTypeByLabel(String label) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStoreType> criteria = builder.createQuery(ImageStoreType.class);
        Root<ImageStoreType> root = criteria.from(ImageStoreType.class);
        criteria.where(builder.equal(root.get("label"), label));
        return (ImageStoreType) getSession().createQuery(criteria).getSingleResult();
    }

    /**
     * Lookup ImageStore by label and org
     * @param label the label
     * @param org the organization
     * @return Returns the ImageStore
     */
    public static ImageStore lookupBylabelAndOrg(String label, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStore> criteria = builder.createQuery(ImageStore.class);
        Root<ImageStore> root = criteria.from(ImageStore.class);
        criteria.where(builder.and(
                builder.equal(root.get("label"), label),
                builder.equal(root.get("org"), org)));
        return (ImageStore) getSession().createQuery(criteria).getSingleResult();
    }

    /**
     * List image stores by type label and org
     * @param typeLabel the type label
     * @param org the organization
     * @return Returns the ImageStore
     */
    public static List<ImageStore> listByTypeLabelAndOrg(String typeLabel, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStore> q = builder.createQuery(ImageStore.class);

        Root<ImageStore> root = q.from(ImageStore.class);
        Join<ImageStore, ImageStoreType> types = root.join("storeType");
        q.where(builder.and(
                builder.equal(types.get("label"), typeLabel),
                builder.equal(root.get("org"), org)
        ));

        return getSession().createQuery(q).getResultList();
    }

    /**
     * Lookup ImageStore by id and organization
     * @param id ImageStore Id
     * @param org the organization
     * @return Returns the ImageStore with the given id if it exists inside the organization
     */
    public static Optional<ImageStore> lookupByIdAndOrg(long id, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStore> criteria = builder.createQuery(ImageStore.class);
        Root<ImageStore> root = criteria.from(ImageStore.class);
        criteria.where(builder.and(
                builder.equal(root.get("id"), id),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * Lookup ImageStore by Id
     * @param id ImageStore Id
     * @return Returns the ImageStore with the given id if it exists
     */
    public static Optional<ImageStore> lookupById(long id) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStore> criteria = builder.createQuery(ImageStore.class);
        Root<ImageStore> root = criteria.from(ImageStore.class);
        criteria.where(builder.equal(root.get("id"), id));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * List all ImageStores of the given organization
     * @param org the organization
     * @return Returns a list of ImageStores
     */
    public static List<ImageStore> listImageStores(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStore> criteria = builder.createQuery(ImageStore.class);
        Root<ImageStore> root = criteria.from(ImageStore.class);
        criteria.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(criteria).getResultList();
    }
}
