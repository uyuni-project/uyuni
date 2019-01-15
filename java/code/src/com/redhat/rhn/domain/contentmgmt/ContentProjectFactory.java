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
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *  HibernateFactory for the {@link com.redhat.rhn.domain.contentmgmt.ContentProject} class.
 */
public class ContentProjectFactory extends HibernateFactory {

    private static final ContentProjectFactory INSTANCE = new ContentProjectFactory();
    private static Logger log = Logger.getLogger(ContentProjectFactory.class);

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
        INSTANCE.saveObject(contentProject);
    }

    /**
     * Save a Content Environment in DB
     * @param contentEnvironment the content environment
     */
    public static void save(ContentEnvironment contentEnvironment) {
        INSTANCE.saveObject(contentEnvironment);
    }

    /**
     * Delete a {@link ContentEnvironment} from the database.
     * @param contentEnvironment Environment to be deleted.
     */
    protected static void remove(ContentEnvironment contentEnvironment) {
        INSTANCE.removeObject(contentEnvironment);
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
     * Insert the new environment in the path after the given environment.
     * @param newEnv new environment to be inserted
     * @param after insert after this
     * @throws ContentManagementException if projects of given environments mismatch
     */
    public static void insertEnvironment(ContentEnvironment newEnv, ContentEnvironment after)
            throws ContentManagementException {

        if (!newEnv.getContentProject().equals(after.getContentProject())) {
            throw new ContentManagementException("Environments from different Projects");
        }

        after.getNextEnvironmentOpt().ifPresent(successor -> {
            after.setNextEnvironment(newEnv);
            newEnv.setPrevEnvironment(after);
            newEnv.setNextEnvironment(successor);
            successor.setPrevEnvironment(newEnv);
            save(successor);
        });
        if (!after.getNextEnvironmentOpt().isPresent()) {
            after.setNextEnvironment(newEnv);
            newEnv.setPrevEnvironment(after);
        }
        save(newEnv);
        save(after);
    }

    /**
     * Remove an environment from the path. It take care that chain stay intact
     * @param toRemove environment to remove.
     */
    public static void removeEnvironment(ContentEnvironment toRemove) {
        if (toRemove.getNextEnvironmentOpt().isPresent()) {
            ContentEnvironment next = toRemove.getNextEnvironmentOpt().get();
            if (toRemove.getPrevEnvironmentOpt().isPresent()) {
                ContentEnvironment prev = toRemove.getPrevEnvironmentOpt().get();
                prev.setNextEnvironment(next);
                next.setPrevEnvironment(prev);
                save(prev);
            }
            else {
                // First Env - change the project
                ContentProject project = toRemove.getContentProject();
                project.setFirstEnvironment(next);
                next.setPrevEnvironment(null);
                save(project);
            }
            save(next);
        }
        else if (toRemove.getPrevEnvironmentOpt().isPresent()) {
            ContentEnvironment prev = toRemove.getPrevEnvironmentOpt().get();
            prev.setNextEnvironment(null);
            save(prev);
        }
        toRemove.setPrevEnvironment(null);
        toRemove.setNextEnvironment(null);
        remove(toRemove);
    }

    /**
     * Lists all Environments of a Content Project
     *
     * @param project the Content Project
     * @return Environments of the Content Project
     */
    public static List<ContentEnvironment> listProjectEnvironments(ContentProject project) {
        List<ContentEnvironment> result = new LinkedList<>();
        Optional<ContentEnvironment> env = project.getFirstEnvironmentOpt();
        while (env.isPresent()) {
            result.add(env.get());
            env = env.get().getNextEnvironmentOpt();
        }
        return result;
    }

    /**
     * Looks up SoftwareEnvironmentTarget with a given channel
     *
     * @param channel the channel
     * @return Optional of {@link com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget}
     */
    public static Optional<SoftwareEnvironmentTarget> lookupEnvironmentTargetByChannel(Channel channel) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SoftwareEnvironmentTarget> query = builder.createQuery(SoftwareEnvironmentTarget.class);
        Root<SoftwareEnvironmentTarget> from = query.from(SoftwareEnvironmentTarget.class);
        query.where(builder.equal(from.get("channel"), channel));
        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Save a Project source
     *
     * @param source the project source
     */
    public static void save(ProjectSource source) {
        INSTANCE.saveObject(source);
    }

    /**
     * Save an Environment Target
     * @param target the Environment Target
     */
    public static void save(EnvironmentTarget target) {
        INSTANCE.saveObject(target);
    }

    /**
     * List Project Sources in a Content Project
     *
     * @param cp the content project
     * @return the sources in the project
     */
    public static List<ProjectSource> listProjectSourcesByProject(ContentProject cp) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ProjectSource> query = builder.createQuery(ProjectSource.class);
        Root<ProjectSource> root = query.from(ProjectSource.class);
        query.where(builder.equal(root.get("contentProject"), cp));
        return getSession().createQuery(query).list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
