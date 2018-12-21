/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.org.Org;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *  HibernateFactory for the {@link com.redhat.rhn.domain.contentmgmt.ContentProject} class.
 */
public class ContentProjectFactory extends HibernateFactory {

    private static final ContentProjectFactory instance = new ContentProjectFactory();
    private static Logger log = Logger.getLogger(ImageInfoFactory.class);

    // forbid  instantiation
    private ContentProjectFactory() {
        super();
    }

    /**
     * Save the ContentProject
     *
     * @param contentProject - the ContentProject
     */
    public static void save(ContentProject contentProject) {
        instance.saveObject(contentProject);
    }

    /**
     * Save a Content Environment in DB
     * @param contentEnvironment the content environment
     */
    public static void save(ContentEnvironment contentEnvironment) {
        instance.saveObject(contentEnvironment);
    }

    /**
     * Looks up a ContentProject by label
     *
     * @param label - the label
     * @return ContentProject with given label
     */
    public static ContentProject lookupContentProjectByLabel(String label) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentProject> criteria = builder.createQuery(ContentProject.class);
        Root<ContentProject> root = criteria.from(ContentProject.class);
        criteria.where(builder.equal(root.get("label"), label));
        return getSession().createQuery(criteria).getSingleResult();
    }

    /**
     * List all ContentProjects with given organization
     * @param org - the organization
     * @return the ContentProjects in given organization
     */
    public static List<ContentProject> listContentProjects(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentProject> query = builder.createQuery(ContentProject.class);
        Root<ContentProject> root = query.from(ContentProject.class);
        query.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(query).list();
    }

    /**
     * Get the 1st Envirnoment of the Content Project
     *
     * @param project the Content Project
     * @return the first Environment
     */
    public static Optional<ContentEnvironment> getFirstEnvironmentOfProject(ContentProject project) {
        return HibernateFactory.getSession()
                .createNamedQuery("ContentEnvironment.lookupFirstInProject")
                .setParameter("contentProject", project)
                .uniqueResultOptional();
    }

    /**
     * Get the predecessor to given Environment
     * @param env the environment
     * @return predecessor of given Environment or empty when no predecessor exists
     */
    public static Optional<ContentEnvironment> getPrevEnvironment(ContentEnvironment env) {
        return HibernateFactory.getSession()
                .createNamedQuery("ContentEnvironment.lookupPredecessor")
                .setParameter("env", env)
                .uniqueResultOptional();
    }

    /**
     * Prepend given environment to the given successor environment (or set it as a first environment of the project,
     * if no successor environment is given).
     *
     * @param env the environment
     * @param successor the optional successor environment
     */
    public static void prependEnvironment(ContentEnvironment env, Optional<ContentEnvironment> successor) {
        // disconnect environment from the environment path, if needed
        ContentProject contentProject = env.getContentProject();
        disconnectEnvironment(env);

        // if new successor is present -> prepend the env to it
        successor.ifPresent(succ -> {
            getPrevEnvironment(succ).ifPresent(succPrev -> {
                succPrev.setNextEnvironment(env);
                save(succPrev);
            });
            env.setNextEnvironment(succ);
        });

        // otherwise set this env as a first environment of a project
        if(!successor.isPresent()) {
            env.setNextEnvironment(getFirstEnvironmentOfProject(env.getContentProject()).orElse(null));
        }

        // reset content project
        env.setContentProject(contentProject);
        save(env);
    }

    private static void disconnectEnvironment(ContentEnvironment env) {
        getPrevEnvironment(env).ifPresent(pred -> {
            pred.setNextEnvironment(env.getNextEnvironment());
            save(pred);
        });

        env.setNextEnvironment(null);
        env.setContentProject(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
