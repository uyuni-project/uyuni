/*
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

import static com.suse.utils.Opt.consume;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.contentmgmt.ProjectSource.Type;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *  HibernateFactory for the {@link com.redhat.rhn.domain.contentmgmt.ContentProject} class and related classes.
 */
public class ContentProjectFactory extends HibernateFactory {

    private static final ContentProjectFactory INSTANCE = new ContentProjectFactory();
    private static Logger log = Logger.getLogger(ContentProjectFactory.class);

    // forbid  instantiation
    private ContentProjectFactory() {
        super();
    }

    /**
     * Save the ContentFilter
     *
     * @param contentFilter the content filter
     */
    public static void save(ContentFilter contentFilter) {
        INSTANCE.saveObject(contentFilter);
    }

    /**
     * Save the ContentProject
     *
     * @param contentProject the ContentProject
     */
    public static void save(ContentProject contentProject) {
        INSTANCE.saveObject(contentProject);
    }

    /**
     * Remove a Content Project
     *
     * @param contentProject the Content Project to remove
     * @return the number of object affected
     */
    public static int remove(ContentProject contentProject) {
        return INSTANCE.removeObject(contentProject);
    }

    /**
     * Looks up a ContentProject by label and organization
     *
     * @param label the label
     * @param org the org
     * @return Optional with ContentProject with given label
     */
    public static Optional<ContentProject> lookupProjectByLabelAndOrg(String label, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentProject> criteria = builder.createQuery(ContentProject.class);
        Root<ContentProject> root = criteria.from(ContentProject.class);
        criteria.where(builder.and(
                builder.equal(root.get("label"), label),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * Looks up a ContentProject by name and organization
     *
     * @param name the name
     * @param org the org
     * @return Optional with ContentProject with given name
     */
    public static Optional<ContentProject> lookupProjectByNameAndOrg(String name, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentProject> criteria = builder.createQuery(ContentProject.class);
        Root<ContentProject> root = criteria.from(ContentProject.class);
        criteria.where(builder.and(
                builder.equal(root.get("name"), name),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * List all ContentProjects with given organization
     * @param org the organization
     * @return the ContentProjects in given organization
     */
    public static List<ContentProject> listProjects(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentProject> query = builder.createQuery(ContentProject.class);
        Root<ContentProject> root = query.from(ContentProject.class);
        query.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(query).list();
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
    private static void remove(ContentEnvironment contentEnvironment) {
        INSTANCE.removeObject(contentEnvironment);
    }

    /**
     * Lists all Environments of a Content Project with the respect to their ordering.
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
        return unmodifiableList(result);
    }

    /**
     * Look up Content Environment by label and Content Project label
     *
     * @param label the Environment label
     * @param project the Content Project
     * @return matching Environment
     */
    public static Optional<ContentEnvironment> lookupEnvironmentByLabelAndProject(String label,
            ContentProject project) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentEnvironment> criteria = builder.createQuery(ContentEnvironment.class);
        Root<ContentEnvironment> root = criteria.from(ContentEnvironment.class);
        criteria.where(builder.and(
                builder.equal(root.get("label"), label),
                builder.equal(root.get("contentProject"), project)));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * Insert the new environment in the path after the given environment.
     * @param newEnv new environment to be inserted
     * @param predecessor optional predecessor:
     *                    if defined -> insert after predecessor
     *                    if empty -> insert as 1st environment in the project
     * @throws ContentManagementException if projects of given environments mismatch
     */
    public static void insertEnvironment(ContentEnvironment newEnv, Optional<ContentEnvironment> predecessor)
            throws ContentManagementException {
        consume(
                predecessor,
                () -> insertFirstEnvironment(newEnv),
                (pred) -> appendEnvironment(newEnv, pred));
    }

    private static void insertFirstEnvironment(ContentEnvironment newEnv) {
        ContentProject contentProject = newEnv.getContentProject();

        ContentEnvironment oldFirstEnvironment = contentProject.getFirstEnvironment();
        if (oldFirstEnvironment != null) {
            newEnv.setNextEnvironment(oldFirstEnvironment);
            oldFirstEnvironment.setPrevEnvironment(newEnv);
            save(oldFirstEnvironment);
        }

        contentProject.setFirstEnvironment(newEnv);
        save(contentProject);
        save(newEnv);
    }

    private static void appendEnvironment(ContentEnvironment newEnv, ContentEnvironment predecessor) {
        if (!newEnv.getContentProject().equals(predecessor.getContentProject())) {
            throw new ContentManagementException("Environments from different Projects");
        }

        predecessor.getNextEnvironmentOpt().ifPresent(successor -> {
            predecessor.setNextEnvironment(newEnv);
            newEnv.setPrevEnvironment(predecessor);
            newEnv.setNextEnvironment(successor);
            successor.setPrevEnvironment(newEnv);
            save(successor);
        });
        if (!predecessor.getNextEnvironmentOpt().isPresent()) {
            predecessor.setNextEnvironment(newEnv);
            newEnv.setPrevEnvironment(predecessor);
        }
        save(newEnv);
        save(predecessor);
    }

    /**
     * Remove an environment from the path. Take care that chain stays intact.
     * Also remove all channels belonging to this environment.
     *
     * @param toRemove environment to remove.
     */
    public static void removeEnvironment(ContentEnvironment toRemove) {
        // let's purge all the targets in the environment firstly
        new ArrayList<>(toRemove.getTargets()).stream()
                .sorted((t1, t2) -> Boolean.compare(// make sure a parent channel goes first
                        t1.asSoftwareTarget().map(t -> t.getChannel().isBaseChannel()).orElse(false),
                        t2.asSoftwareTarget().map(t -> t.getChannel().isBaseChannel()).orElse(false)))
                .forEach(ContentProjectFactory::purgeTarget);

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
        else {
            // Only Env - change the project
            ContentProject project = toRemove.getContentProject();
            project.setFirstEnvironment(null);
            save(project);
        }
        toRemove.setPrevEnvironment(null);
        toRemove.setNextEnvironment(null);
        remove(toRemove);
    }

    /**
     * Save an Environment Target
     * @param target the Environment Target
     */
    public static void save(EnvironmentTarget target) {
        INSTANCE.saveObject(target);
    }

    /**
     * Purge the Environment Target - delete it and delete its underlying resource (e.g. {@link Channel}) too.
     * Reconstructs the 'original-clone' relation in case it'd be broken by the channel deletion.
     *
     * @param target the Environment Target
     */
    public static void purgeTarget(EnvironmentTarget target) {
        boolean hasDistributions = target
                .asSoftwareTarget()
                .map(SoftwareEnvironmentTarget::getChannel)
                .map(Channel::containsDistributions)
                .orElse(false);

        if (hasDistributions) {
            throw new ContentManagementException("The target " + target.toString() +
                    " is being used in an autoinstallation profile. Cannot remove.");
        }

        // firstly fix the original/clone relations of channels
        target.asSoftwareTarget().ifPresent(swTgt -> {
            Optional<Channel> prevChannel = swTgt.getChannel().asCloned().map(c -> c.getOriginal());
            List<ClonedChannel> nextChannels = lookupClonesInProject(swTgt.getChannel(),
                    swTgt.getContentEnvironment().getContentProject());
            // if both next and previous channel exist -> fix the original-clone relation
            prevChannel.ifPresent(prev -> nextChannels.forEach(next -> next.setOriginal(prev)));
        });

        // then remove the target and its channel
        target.getContentEnvironment().removeTarget(target);
        INSTANCE.removeObject(target);
        target.asSoftwareTarget().map(swTgt -> swTgt.getChannel()).ifPresent(channel -> {
            HibernateFactory.getSession().evict(channel);
            ChannelFactory.remove(channel);
        });
    }

    /**
     * Looks up SoftwareEnvironmentTarget with a given channel
     *
     * @param label the {@link Channel} label
     * @param user the user
     * @return Optional of {@link com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget}
     */
    public static Optional<SoftwareEnvironmentTarget> lookupEnvironmentTargetByChannelLabel(String label, User user) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SoftwareEnvironmentTarget> query = builder.createQuery(SoftwareEnvironmentTarget.class);
        Root<SoftwareEnvironmentTarget> from = query.from(SoftwareEnvironmentTarget.class);
        query.where(builder.equal(from.get("channel").get("label"), label));
        return getSession().createQuery(query)
                .uniqueResultOptional()
                .filter(tgt -> ChannelFactory.isAccessibleByUser(tgt.getChannel().getLabel(), user.getId()));
    }

    /**
     * List all SoftwareEnvironmentTarget
     * @return list with all SoftwareEnvironmentTarget
     */
    public static List<SoftwareEnvironmentTarget> listSoftwareEnvironmentTarget() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SoftwareEnvironmentTarget> query = builder.createQuery(SoftwareEnvironmentTarget.class);
        query.from(SoftwareEnvironmentTarget.class);
        return getSession().createQuery(query).list();
    }

    /**
     * Looks up SoftwareEnvironmentTarget with given id
     *
     * @param label the {@link Channel} label
     * @return Optional of {@link com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget}
     */
    public static Optional<SoftwareEnvironmentTarget> lookupEnvironmentTargetByChannelLabel(String label) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SoftwareEnvironmentTarget> query = builder.createQuery(SoftwareEnvironmentTarget.class);
        Root<SoftwareEnvironmentTarget> from = query.from(SoftwareEnvironmentTarget.class);
        query.where(builder.equal(from.get("channel").get("label"), label));
        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Looks up SoftwareEnvironmentTarget with a given channel
     *
     * @param id the id
     * @return Optional of {@link com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget}
     */
    public static Optional<SoftwareEnvironmentTarget> lookupSwEnvironmentTargetById(long id) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SoftwareEnvironmentTarget> query = builder.createQuery(SoftwareEnvironmentTarget.class);
        Root<SoftwareEnvironmentTarget> from = query.from(SoftwareEnvironmentTarget.class);
        query.where(builder.equal(from.get("id"), id));
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
     * Save the Content Project history entry
     *
     * @param entry the Content Project history entry
     */
    private static void save(ContentProjectHistoryEntry entry) {
        INSTANCE.saveObject(entry);
    }

    /**
     * Add a history entry to a Content Project
     *
     * @param project Content Project
     * @param entry the history entry
     */
    public static void addHistoryEntryToProject(ContentProject project, ContentProjectHistoryEntry entry) {
        List<ContentProjectHistoryEntry> entries = project.getHistoryEntries();
        entry.setVersion(latestHistoryEntryVersion(project).orElse(0L) + 1);
        entry.setContentProject(project);
        save(entry);
        entries.add(entry);
        save(project);
    }

    private static Optional<Long> latestHistoryEntryVersion(ContentProject project) {
        return HibernateFactory.getSession().getNamedQuery("ContentProjectHistoryEntry.latestEntryVersion")
                .setParameter("project", project)
                .uniqueResultOptional();
    }

    /**
     * Remove a Project Source
     *
     * @param source the Source
     * @return the number of object affected
     */
    public static int remove(ProjectSource source) {
        return INSTANCE.removeObject(source);
    }

    /**
     * Look up Project Source based with given Project and Channel
     *
     * @param project the Project
     * @param sourceType the Source type
     * @param sourceLabel the Source label
     * @param user the User
     * @return Optional with matching Source
     */
    public static Optional<ProjectSource> lookupProjectSource(ContentProject project, Type sourceType,
            String sourceLabel, User user) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ProjectSource> query = builder.createQuery(sourceType.getSourceClass());
        Root<ProjectSource> root = query.from(sourceType.getSourceClass());

        Predicate sourcePredicate;
        switch (sourceType) {
            case SW_CHANNEL:
                if (!ChannelFactory.isAccessibleByUser(sourceLabel, user.getId())) {
                    return empty();
                }
                sourcePredicate = builder.equal(root.get("channel").get("label"), sourceLabel);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Source type " + sourceType);
        }

        query.where(
                builder.and(
                        builder.equal(root.get("contentProject"), project),
                        sourcePredicate));
        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Look up {@link ClonedChannel}s of given {@link Channel} in given {@link ContentProject}
     *
     * HACK: Normally, this method should return an Optional of ClonedChannel instead of a List, as very channel in
     * a Content Project should have at most 1 successor. However, the past versions of SUSE Manager did not keep
     * the original-clone relation consistent and channels with more clones could appear inside a Content Project.
     *
     * @param channel the channel
     * @param project the project
     * @return the successors of the channel in the project
     */
    public static List<ClonedChannel> lookupClonesInProject(Channel channel, ContentProject project) {
        Stream<Channel> clones = HibernateFactory.getSession()
                .createQuery("SELECT tgt.channel FROM SoftwareEnvironmentTarget tgt " +
                        "WHERE tgt.contentEnvironment.contentProject = :project " +
                        "AND tgt.channel.original = :channel")
                .setParameter("project", project)
                .setParameter("channel", channel)
                .stream();
        return clones.flatMap(c -> c.asCloned().stream()).collect(Collectors.toList());
    }

    /**
     * List filters visible to given user
     *
     * @param user the user
     * @return the filters
     */
    public static List<ContentFilter> listFilters(User user) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentFilter> criteria = builder.createQuery(ContentFilter.class);
        Root<ContentFilter> root = criteria.from(ContentFilter.class);
        criteria.where(builder.equal(root.get("org"), user.getOrg()));
        return getSession().createQuery(criteria).list();
    }

    /**
     * Look up filter by id
     *
     * @param id the id
     * @return the matching filter
     */
    public static Optional<ContentFilter> lookupFilterById(Long id) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentFilter> criteria = builder.createQuery(ContentFilter.class);
        Root<ContentFilter> root = criteria.from(ContentFilter.class);
        criteria.where(builder.equal(root.get("id"), id));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * Looks up a ContentProject by label and organization
     *
     * @param name the name
     * @param org the org
     * @return Optional with ContentProject with given label
     */
    public static Optional<ContentFilter> lookupFilterByNameAndOrg(String name, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentFilter> criteria = builder.createQuery(ContentFilter.class);
        Root<ContentFilter> root = criteria.from(ContentFilter.class);
        criteria.where(builder.and(
                builder.equal(root.get("name"), name),
                builder.equal(root.get("org"), org)));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    /**
     * Create a new {@link ContentFilter}
     *
     * @param name the filter name
     * @param rule the filter {@link ContentFilter.Rule}
     * @param entityType the entity type that the filter will deal with
     * @param criteria the {@link FilterCriteria} for filtering
     * @param user the User
     * @return the created filter
     */
    public static ContentFilter createFilter(String name, ContentFilter.Rule rule, ContentFilter.EntityType entityType,
            FilterCriteria criteria, User user) {
        ContentFilter filter;
        switch (entityType) {
            case PACKAGE:
                filter = new PackageFilter();
                break;
            case ERRATUM:
                filter = new ErrataFilter();
                break;
            case MODULE:
                filter = new ModuleFilter();
                break;
            default:
                throw new IllegalArgumentException("Incompatible type " + entityType);
        }

        filter.setName(name);
        filter.setOrg(user.getOrg());
        filter.setRule(rule);
        filter.setCriteria(criteria);
        INSTANCE.saveObject(filter);
        return filter;
    }

    /**
     * Update a {@link ContentFilter}
     *
     * @param filter the filter to update
     * @param name optional with name to update
     * @param rule optional with {@link ContentFilter.Rule} to update
     * @param criteria optional with {@link FilterCriteria} to update
     * @return the updated filter
     */
    public static ContentFilter updateFilter(ContentFilter filter, Optional<String> name,
            Optional<ContentFilter.Rule> rule, Optional<FilterCriteria> criteria) {
        name.ifPresent(n -> filter.setName(n));
        rule.ifPresent(r -> filter.setRule(r));
        criteria.ifPresent(c -> filter.setCriteria(c));
        listFilterProjectsRelation(filter).stream()
                .filter(projectFilter -> projectFilter.getState() == ContentProjectFilter.State.BUILT)
                .forEach(projectFilter -> projectFilter.setState(ContentProjectFilter.State.EDITED));
        return filter;
    }

    /**
     * Remove {@link ContentFilter}
     *
     * @param filter the filter
     * @return true if removed
     */
    public static boolean remove(ContentFilter filter) {
        return INSTANCE.removeObject(filter) != 0;
    }

    /**
     * List {@link ContentProject}s using given {@link ContentFilter}
     *
     * @param filter the Filter
     * @return list of Projects
     */
    public static List<ContentProject> listFilterProjects(ContentFilter filter) {
        return HibernateFactory.getSession()
                .createQuery("SELECT cp FROM ContentProject cp " +
                        "WHERE cp.id IN (SELECT cpf.project.id FROM ContentProjectFilter cpf " +
                        "WHERE cpf.filter.id = :fid)")
                .setParameter("fid", filter.getId())
                .list();
    }

    /**
     * List {@link ContentProjectFilter}s using given {@link ContentFilter}
     *
     * @param filter the Filter
     * @return list of Projects
     */
    public static List<ContentProjectFilter> listFilterProjectsRelation(ContentFilter filter) {
        return HibernateFactory.getSession()
                .createQuery("SELECT cpf FROM ContentProjectFilter cpf " +
                        "WHERE cpf.filter.id = :fid")
                .setParameter("fid", filter.getId())
                .list();
    }

    /**
     * Remove {@link ContentProjectFilter}
     * @param filter the filter
     */
    public static void remove(ContentProjectFilter filter) {
        INSTANCE.removeObject(filter);
    }

    /**
     * Set all BUILDING {@link EnvironmentTarget}s to FAILED state.
     *
     * @return the number of updated targets
     */
    public static int failStaleTargets() {
        return HibernateFactory.getSession()
                .createQuery("UPDATE EnvironmentTarget tgt " +
                        "SET tgt.status = :statusFailed " +
                        "WHERE tgt.status = :statusBuilding")
                .setParameter("statusFailed", EnvironmentTarget.Status.FAILED)
                .setParameter("statusBuilding", EnvironmentTarget.Status.BUILDING)
                .executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
