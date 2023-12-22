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
package com.redhat.rhn.domain.image;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.RegistryCredentials;
import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

/**
 * ImageStoreFactory
 */
public class ImageStoreFactory extends HibernateFactory {

    public static final ImageStoreType TYPE_OS_IMAGE = lookupStoreTypeByLabel("os_image").get();
    public static final ImageStoreType TYPE_REGISTRY = lookupStoreTypeByLabel("registry").get();

    private static final Logger LOG = LogManager.getLogger(ImageStoreFactory.class);

    private static ImageStoreFactory instance = new ImageStoreFactory();
    public static final String USER_KEY = "username";
    public static final String PASS_KEY = "password";

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
        return LOG;
    }

    /**
     * Delete a {@link ImageStore}.
     *
     * @param store the image store to delete
     */
    public static void delete(ImageStore store) {
        if (store.getStoreType().equals(ImageStoreFactory.TYPE_OS_IMAGE)) {
            throw new IllegalArgumentException("Cannot delete permanent OS Image store");
        }
        instance.removeObject(store);
    }

    /**
     * Save a {@link ImageStore}.
     *
     * @param store the image store to save
     */
    public static void save(ImageStore store) {
        if (store.getStoreType().equals(ImageStoreFactory.TYPE_OS_IMAGE)) {
            throw new IllegalArgumentException("Cannot update permanent OS Image store");
        }
        instance.saveObject(store);
    }

    /**
     * Lookup ImageStoreType from given label
     * @param label the label to search for
     * @return Returns the ImageStoreType
     */
    public static Optional<ImageStoreType> lookupStoreTypeByLabel(String label) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStoreType> criteria = builder.createQuery(ImageStoreType.class);
        Root<ImageStoreType> root = criteria.from(ImageStoreType.class);
        criteria.where(builder.equal(root.get("label"), label));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * Return a list of ImageStoreTypes
     * @return list of image store types
     */
    public static List<ImageStoreType> listImageStoreTypes() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStoreType> criteria = builder.createQuery(ImageStoreType.class);
        criteria.from(ImageStoreType.class);
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Lookup ImageStore by label and org
     * @param label the label
     * @param org the organization
     * @return Returns the ImageStore
     */
    public static Optional<ImageStore> lookupBylabelAndOrg(String label, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStore> criteria = builder.createQuery(ImageStore.class);
        Root<ImageStore> root = criteria.from(ImageStore.class);
        criteria.where(builder.and(
                builder.equal(root.get("label"), label),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).uniqueResultOptional();
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
     * Lookup multiple ImageStores by an id list and organization
     * @param ids ImageStore Id list
     * @param org the organization
     * @return Returns a list of ImageStores with the given ids if they exist
     * inside the organization
     */
    public static List<ImageStore> lookupByIdsAndOrg(List<Long> ids, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageStore> criteria = builder.createQuery(ImageStore.class);
        Root<ImageStore> root = criteria.from(ImageStore.class);
        criteria.where(builder.and(
                root.get("id").in(ids),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).getResultList();
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

    /**
     * Creates a db entity for credentials if the input params contain entry for
     * username and password.
     * @param params - non-null map of gatherer parameters
     * @param ist - image store type
     * @return new Credentials instance
     */
    public static RegistryCredentials createCredentials(Map<String, String> params, ImageStoreType ist) {
        if (!ist.equals(ImageStoreFactory.TYPE_REGISTRY)) {
            return null;
        }

        String user = params.get(USER_KEY);
        String password = params.get(PASS_KEY);

        if (StringUtils.isEmpty(user)) {
            return null;
        }

        RegistryCredentials registryCredentials = CredentialsFactory.createRegistryCredentials(user, password);
        CredentialsFactory.storeCredentials(registryCredentials);

        return registryCredentials;
    }
}
