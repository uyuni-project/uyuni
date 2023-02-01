/*
 * Copyright (c) 2023 SUSE LLC
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
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * {@link HibernateFactory} for {@link ImageSyncFactory}
 */
public class ImageSyncFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(ImageSyncFactory.class);

    /**
     * Save an {@link ImageSyncProject} and reload it
     * @param project the project to save
     */
    public void save(ImageSyncProject project) {
        saveObject(project);
    }

    /**
     * Save an {@link ImageSyncSource}
     * @param source the source to save
     */
    public void save(ImageSyncSource source) {
        saveObject(source);
    }

    /**
     * Remove an {@link ImageSyncProject}
     * @param project the project to remove
     */
    public void remove(ImageSyncProject project) {
        removeObject(project);
    }

    /**
     * Remove an {@link ImageSyncSource}
     * @param source the source to remove
     */
    public void remove(ImageSyncSource source) {
        removeObject(source);
    }

    /**
     * List {@link ImageSyncProject} available for the given User
     * @param user the user
     * @return Image Sync Projects
     */
    public List<ImageSyncProject> listProjectsByUser(User user) {
        return getSession()
                .createQuery("FROM ImageSyncProject WHERE org = :org", ImageSyncProject.class)
                .setParameter("org", user.getOrg())
                .getResultList();
    }

    /**
     * Lookup {@link ImageSyncProject} by project name and user
     * @param name the project name
     * @param user the user
     * @return optional Image Sync Project
     */
    public Optional<ImageSyncProject> lookupProjectByNameAndUser(String name, User user) {
        return getSession()
                .createQuery("FROM ImageSyncProject WHERE org = :org and name = :name", ImageSyncProject.class)
                .setParameter("org", user.getOrg())
                .setParameter("name", name)
                .uniqueResultOptional();
    }

    /**
     * Lookup {@link ImageSyncProject} by project id and user
     * @param id the project id
     * @param user the user
     * @return optional Image Sync Project
     */
    public Optional<ImageSyncProject> lookupProjectByIdAndUser(Long id, User user) {
        return getSession()
                .createQuery("FROM ImageSyncProject WHERE org = :org and id = :id", ImageSyncProject.class)
                .setParameter("org", user.getOrg())
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    /**
     * Lookup {@link ImageSyncSource} by id and user
     * @param sourceId the source id
     * @param user the calling user
     * @return optional Image Sync Source
     */
    public Optional<ImageSyncSource> lookupSourceByIdAndUser(Long sourceId, User user) {
        return getSession()
                .createQuery("FROM ImageSyncSource WHERE org = :org and id = :id", ImageSyncSource.class)
                .setParameter("org", user.getOrg())
                .setParameter("id", sourceId)
                .uniqueResultOptional();
    }

    /**
     * List all {@link ImageSyncSource} available for given User
     * @param user the user
     * @return returns a list of Image Sync Sources
     */
    public List<ImageSyncSource> listSources(User user) {
        return getSession()
                .createQuery("FROM ImageSyncSource WHERE org = :org", ImageSyncSource.class)
                .setParameter("org", user.getOrg())
                .getResultList();
    }

    /**
     * Lookup {@link ImageSyncProject} by project id
     * @param id the project id
     * @return optional Image Sync Project
     */
    public Optional<ImageSyncProject> lookupProjectById(Long id) {
        return getSession()
                .createQuery("FROM ImageSyncProject WHERE id = :id", ImageSyncProject.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    /**
     * List all {@link ImageSyncProject}
     * @return list with all existing images
     */
    public List<ImageSyncProject> listAll() {
        return getSession()
                .createQuery("FROM ImageSyncProject", ImageSyncProject.class)
                .getResultList();
    }


    @Override
    protected Logger getLogger() {
        return log;
    }
}
