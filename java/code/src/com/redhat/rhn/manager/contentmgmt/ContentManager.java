/**
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.manager.contentmgmt;

import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.ATTACHED;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.BUILT;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.DETACHED;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static com.redhat.rhn.manager.channel.CloneChannelCommand.CloneBehavior.EMPTY;
import static com.suse.utils.Opt.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.ProjectSource.Type;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.channel.CloneChannelCommand;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Content Management functionality
 */
public class ContentManager {

    private static final String DELIMITER = "-";

    // forbid instantiation
    private ContentManager() { }

    /**
     * Create a Content Project
     *
     * @param label - the label
     * @param name - the name
     * @param description - the description
     * @param user - the creator
     * @throws EntityExistsException if a project with given label already exists
     * @throws PermissionException if given user does not have required role
     * @return the created Content Project
     */
    public static ContentProject createProject(String label, String name, String description, User user) {
        ensureOrgAdmin(user);
        lookupProject(label, user).ifPresent(cp -> {
            throw new EntityExistsException(cp);
        });
        ContentProject contentProject = new ContentProject(label, name, description, user.getOrg());
        ContentProjectFactory.save(contentProject);
        return contentProject;
    }

    /**
     * List Projects visible to the user
     *
     * @param user the user
     * @return the Projects visible to the user
     */
    public static List<ContentProject> listProjects(User user) {
        return ContentProjectFactory.listProjects(user.getOrg());
    }

    /**
     * Look up Content Project by label
     *
     * @param label - the label
     * @param user - the user
     * @return Optional with matching Content Project
     */
    public static Optional<ContentProject> lookupProject(String label, User user) {
        return ContentProjectFactory.lookupProjectByLabelAndOrg(label, user.getOrg());
    }

    /**
     * Update Content Project
     *
     * @param label - the label for lookup
     * @param newName - new name
     * @param newDesc - new description
     * @param user - the user
     * @throws EntityNotExistsException - if Content Project with given label is not found
     * @throws PermissionException if given user does not have required role
     * @return the updated Content Project
     */
    public static ContentProject updateProject(String label, Optional<String> newName, Optional<String> newDesc,
            User user) {
        ensureOrgAdmin(user);
        return lookupProject(label, user)
                .map(cp -> {
                    newName.ifPresent(name -> cp.setName(name));
                    newDesc.ifPresent(desc -> cp.setDescription(desc));
                    return cp;
                })
                .orElseThrow(() -> new EntityNotExistsException(label));
    }

    /**
     * Remove Content Project
     *
     * @param label - the label
     * @param user - the user
     * @throws PermissionException if given user does not have required role
     * @throws EntityNotExistsException - if Content Project with given label is not found
     * @return the number of objects affected
     */
    public static int removeProject(String label, User user) {
        ensureOrgAdmin(user);
        return lookupProject(label, user)
                .map(cp -> ContentProjectFactory.remove(cp))
                .orElseThrow(() -> new EntityNotExistsException(label));
    }

    /**
     * Create Content Environment
     *
     * @param projectLabel - the Content Project label
     * @param predecessorLabel - the predecessor Environment label
     * @param label - the Environment label
     * @param name - the Environment name
     * @param description - the Environment description
     * @param async run the time-expensive operations asynchronously?
     * @param user - the user performing the action
     * @throws EntityNotExistsException - if Content Project with given label or Content Environment in the Project
     * is not found
     * @throws EntityExistsException - if Environment with given parameters already exists
     * @throws PermissionException if given user does not have required role
     * @return the created Content Environment
     */
    public static ContentEnvironment createEnvironment(String projectLabel, Optional<String> predecessorLabel,
            String label, String name, String description, boolean async, User user) {
        ensureOrgAdmin(user);
        lookupEnvironment(label, projectLabel, user).ifPresent(e -> { throw new EntityExistsException(e); });
        return lookupProject(projectLabel, user)
                .map(cp -> {
                    ContentEnvironment newEnv = new ContentEnvironment(label, name, description, cp);
                    Optional<ContentEnvironment> predecessor = predecessorLabel.map(pl ->
                            ContentProjectFactory.lookupEnvironmentByLabelAndProject(pl, cp)
                                    .orElseThrow(() -> new EntityNotExistsException(ContentEnvironment.class, label)));
                    ContentProjectFactory.insertEnvironment(newEnv, predecessor);
                    // TODO: for now only support populating non-first environments
                    // populating first environment will be implemented as soon as backward-promote is implemented
                    predecessor.ifPresent(p -> {
                        if (!p.getTargets().isEmpty()) {
                            promoteProject(projectLabel, p.getLabel(), true, user);
                        }
                    });
                    return newEnv;
                }).orElseThrow(() -> new EntityNotExistsException(ContentProject.class, projectLabel));
    }

    /**
     * List Environments in a Content Project with the respect to their ordering
     *
     * @param projectLabel - the Content Project label
     * @param user - the user
     * @throws EntityNotExistsException - if Content Project with given label is not found
     * @return the List of Content Environments with respect to their ordering
     */
    public static List<ContentEnvironment> listProjectEnvironments(String projectLabel, User user) {
        return lookupProject(projectLabel, user)
                .map(cp -> ContentProjectFactory.listProjectEnvironments(cp))
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
    }

    /**
     * Look up Content Environment based on its label, Content Project label and User
     *
     * @param envLabel - the Content Environment label
     * @param projectLabel - the Content Project label
     * @param user - the user
     * @throws EntityNotExistsException - if Content Project with given label is not found
     * @return the optional of matching Content Environment
     */
    public static Optional<ContentEnvironment> lookupEnvironment(String envLabel, String projectLabel, User user) {
        return lookupProject(projectLabel, user)
                .map(cp -> ContentProjectFactory.lookupEnvironmentByLabelAndProject(envLabel, cp))
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
    }

    /**
     * Update Content Environment
     *
     * @param envLabel - the Environment label
     * @param projectLabel - the Content Project label
     * @param newName - new name
     * @param newDescription - new description
     * @param user - the user
     * @throws EntityNotExistsException - if Project or Environment is not found
     * @throws PermissionException if given user does not have required role
     * @return the updated Environment
     */
    public static ContentEnvironment updateEnvironment(String envLabel, String projectLabel, Optional<String> newName,
            Optional<String> newDescription, User user) {
        ensureOrgAdmin(user);
        return lookupEnvironment(envLabel, projectLabel, user)
                .map(env -> {
                    newName.ifPresent(name -> env.setName(name));
                    newDescription.ifPresent(desc -> env.setDescription(desc));
                    return env;
                })
                .orElseThrow(() -> new EntityNotExistsException(envLabel));
    }

    /**
     * Remove a Content Environment
     *
     * @param envLabel - the Content Environment label
     * @param projectLabel - the Content Project label
     * @param user - the user
     * @throws PermissionException if given user does not have required role
     * @throws EntityNotExistsException - if Project or Environment is not found
     * @throws PermissionException if given user does not have required role
     * @return number of deleted objects
     */
    public static int removeEnvironment(String envLabel, String projectLabel, User user) {
        ensureOrgAdmin(user);
        return lookupEnvironment(envLabel, projectLabel, user)
                .map((env) -> {
                    ContentProjectFactory.removeEnvironment(env);
                    return 1;
                })
                .orElseThrow(() -> new EntityNotExistsException(envLabel));
    }

    /**
     * Create and attach a Source to given Project.
     * If the Source is already attached to the Project, this is a no-op.
     *
     * @param projectLabel - the Project label
     * @param sourceType - the Source Type (e.g. SW_CHANNEL)
     * @param sourceLabel - the Source label (e.g. SoftwareChannel label)
     * @param position - the position of the Source (Optional)
     * @param user the user
     * @throws EntityNotExistsException when either the Project or the Source reference (e.g. Channel) is not found
     * @throws java.lang.IllegalArgumentException if the sourceType is unsupported
     * @return the created or existing Source
     */
    public static ProjectSource attachSource(String projectLabel, Type sourceType, String sourceLabel,
            Optional<Integer> position, User user) {
        ensureOrgAdmin(user);
        ContentProject project = lookupProject(projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(ContentProject.class, projectLabel));

        Optional<? extends ProjectSource> source = lookupProjectSource(projectLabel, sourceType, sourceLabel, user);
        if (source.isPresent()) {
            ProjectSource src = source.get();
            if (src.getState() == DETACHED) {
                // if a source has been DETACHED and we attach it again -> it gets back to original state (BUILT)
                src.setState(BUILT);
            }
            if (position.isPresent()) {
                project.removeSource(src);
                project.addSource(src, position);
            }
            ContentProjectFactory.save(project);
            ContentProjectFactory.save(src);
            return src;
        }
        else if (sourceType == SW_CHANNEL) {
            Channel channel = getChannel(sourceLabel, user);
            SoftwareProjectSource newSource = new SoftwareProjectSource(project, channel);
            project.addSource(newSource, position);
            ContentProjectFactory.save(project);
            ContentProjectFactory.save(newSource);
            return newSource;
        }
        else {
            throw new IllegalArgumentException("Unsupported source type " + sourceType);
        }
    }

    private static Channel getChannel(String sourceLabel, User user) {
        try {
            return ChannelManager.lookupByLabelAndUser(sourceLabel, user);
        }
        catch (LookupException e) {
            throw new EntityNotExistsException(Channel.class, e);
        }
    }

    /**
     * Detach a Source from given Project
     *
     * @param projectLabel - the Project label
     * @param sourceType - the Source Type (e.g. SW_CHANNEL)
     * @param sourceLabel - the Source label (e.g. SoftwareChannel label)
     * @param user the user
     * @return number of Sources detached
     */
    public static int detachSource(String projectLabel, Type sourceType, String sourceLabel, User user) {
        ensureOrgAdmin(user);
        Optional<? extends ProjectSource> src = lookupProjectSource(projectLabel, sourceType, sourceLabel, user);
        return src.map(s -> {
            if (s.getState() == ATTACHED) {
                s.getContentProject().removeSource(s);
            }
            else {
                s.setState(DETACHED);
            }
            return 1;
        }).orElse(0);
    }

    /**
     * Look up Source
     *
     * @param projectLabel the Project label
     * @param sourceType the Source type
     * @param sourceLabel  the Source label
     * @param user the User
     * @throws EntityNotExistsException if Project with given label cannot be found
     * @return Optional with matching Source
     */
    public static Optional<? extends ProjectSource> lookupProjectSource(String projectLabel, Type sourceType,
            String sourceLabel, User user) {
        ContentProject project = lookupProject(projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
        return ContentProjectFactory.lookupProjectSource(project, sourceType, sourceLabel, user);
    }

    /**
     * List filters visible to given user
     *
     * @param user the user
     * @return the filters
     */
    public static List<ContentFilter> listFilters(User user) {
        return ContentProjectFactory.listFilters(user);
    }

    /**
     * Look up filter by id and user
     *
     * @param id the id
     * @param user the user
     * @return the matching filter
     */
    public static Optional<ContentFilter> lookupFilterById(Long id, User user) {
        Optional<ContentFilter> filter = ContentProjectFactory.lookupFilterById(id);
        return filter.filter(f -> f.getOrg().equals(user.getOrg()));
    }

    /**
     * Create a new {@link ContentFilter}
     *
     * @param name the filter name
     * @param rule the filter {@link ContentFilter.Rule}
     * @param entityType the entity type that the filter will deal with
     * @param criteria the {@link FilterCriteria} for filtering
     * @param user the user
     * @return the created filter
     */
    public static ContentFilter createFilter(String name, ContentFilter.Rule rule, ContentFilter.EntityType entityType,
            FilterCriteria criteria, User user) {
        ensureOrgAdmin(user);
        return ContentProjectFactory.createFilter(name, rule, entityType, criteria, user);
    }
    // todo check behavior consistency with other crud methods

    /**
     * Update a {@link ContentFilter}
     *
     * @param id the filter id
     * @param name optional with name to update
     * @param rule optional with {@link ContentFilter.Rule} to update
     * @param criteria optional with {@link FilterCriteria} to update
     * @param user the user
     * @return the updated filter
     */
    public static ContentFilter updateFilter(Long id, Optional<String> name, Optional<ContentFilter.Rule> rule,
            Optional<FilterCriteria> criteria, User user) {
        ensureOrgAdmin(user);
        ContentFilter filter = lookupFilterById(id, user)
                .orElseThrow(() -> new EntityNotExistsException(id));
        return ContentProjectFactory.updateFilter(filter, name, rule, criteria);
    }

    /**
     * Remove {@link ContentFilter}
     *
     * @param id the filter id
     * @param user the user
     * @return true if removed
     */
    public static boolean removeFilter(Long id, User user) {
        ensureOrgAdmin(user);
        ContentFilter filter = lookupFilterById(id, user)
                .orElseThrow(() -> new EntityNotExistsException(id));
        if (!ContentProjectFactory.listFilterProjects(filter).isEmpty()) {
            throw new ContentManagementException("Can't delete filter " + id + " - it is used in Content Projects");
        }
        return ContentProjectFactory.remove(filter);
    }

    /**
     * Attach a {@link ContentFilter} to a {@link ContentProject}
     *
     * @param projectLabel the project label
     * @param filterId the filter id
     * @param user the user
     * @return attached filter
     */
    public static ContentFilter attachFilter(String projectLabel, Long filterId, User user) {
        ensureOrgAdmin(user);
        ContentProject project = lookupProject(projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(ContentProject.class, projectLabel));
        ContentFilter filter = lookupFilterById(filterId, user)
                .orElseThrow(() -> new EntityNotExistsException(ContentFilter.class, filterId));
        project.attachFilter(filter);
        ContentProjectFactory.save(project);
        return filter;
    }

    /**
     * Detach a {@link ContentFilter} from a {@link ContentProject}
     *
     * @param projectLabel the project label
     * @param filterId the filter id
     * @param user the user
     */
    public static void detachFilter(String projectLabel, Long filterId, User user) {
        ensureOrgAdmin(user);
        ContentProject project = lookupProject(projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(ContentProject.class, projectLabel));
        ContentFilter filter = lookupFilterById(filterId, user)
                .orElseThrow(() -> new EntityNotExistsException(ContentFilter.class, filterId));
        project.detachFilter(filter);
        ContentProjectFactory.save(project);
    }

    /**
     * Promote given {@link ContentEnvironment} of given {@link ContentProject}
     * to the successor {@link ContentEnvironment}
     *
     * @param projectLabel the Project label
     * @param envLabel the Environment label
     * @param async run the time-expensive operations asynchronously? (in the test code it is useful to run them
     * synchronously)
     * @param user the user
     */
    public static void promoteProject(String projectLabel, String envLabel, boolean async, User user) {
        ensureOrgAdmin(user);
        ContentEnvironment env = lookupEnvironment(envLabel, projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(envLabel));
        ContentEnvironment nextEnv = env.getNextEnvironmentOpt()
                .orElseThrow(() -> new ContentManagementException("Environment " + envLabel +
                        " does not have successor"));

        Map<Boolean, List<Channel>> envChannels = env.getTargets().stream()
                .flatMap(tgt -> stream(tgt.asSoftwareTarget()))
                .map(tgt -> tgt.getChannel())
                .collect(partitioningBy(Channel::isBaseChannel));
        List<Channel> baseChannels = envChannels.get(true);
        List<Channel> childChannels = envChannels.get(false);

        if (baseChannels.size() != 1) {
            throw new IllegalStateException("Environment " + envLabel + " must have exactly one leader channel");
        }

        alignEnvironment(nextEnv, baseChannels.get(0), childChannels.stream(), async, user);

        nextEnv.setVersion(env.getVersion());
    }

    /**
     * Build a {@link ContentProject}
     *
     * @param projectLabel the Project label
     * @param message the optional message
     * @param async run the time-expensive operations asynchronously? (in the test code it is useful to run them
     * synchronously)
     * @param user the user
     */
    public static void buildProject(String projectLabel, Optional<String> message, boolean async, User user) {
        ensureOrgAdmin(user);
        ContentProject project = lookupProject(projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
        ContentEnvironment firstEnv = project.getFirstEnvironmentOpt()
                .orElseThrow(() -> new ContentManagementException("Cannot publish  project: " + projectLabel +
                        " with no environments."));
        buildSoftwareSources(firstEnv, async, user);
        ContentProjectHistoryEntry entry = addHistoryEntry(message, user, project);
        firstEnv.setVersion(entry.getVersion());
    }

    /**
     * Build {@link SoftwareProjectSource}s assigned to {@link ContentProject}
     *
     * @param firstEnv first Environment of the Project
     * @param async run the time-expensive operations asynchronously?
     * @param user the user
     */
    private static void buildSoftwareSources(ContentEnvironment firstEnv, boolean async, User user) {
        ContentProject project = firstEnv.getContentProject();
        Channel leader = project.lookupSwSourceLeader()
                .map(l -> l.getChannel())
                .orElseThrow(() -> new ContentManagementException("Cannot publish  project: " + project.getLabel() +
                        " with no base channel associated with it."));
        Stream<Channel> otherChannels = project.getSources().stream()
                .flatMap(s -> stream(s.asSoftwareSource()))
                .filter(src -> !src.getChannel().equals(leader) && src.getState() != DETACHED)
                .map(s -> s.getChannel());

        alignEnvironment(firstEnv, leader, otherChannels, async, user);

        Map<ProjectSource.State, List<ProjectSource>> sourcesToHandle = project.getSources().stream()
                .collect(groupingBy(src -> src.getState()));
        // newly attached sources get built
        sourcesToHandle.getOrDefault(ATTACHED, emptyList()).stream()
                .forEach(src -> src.setState(BUILT));
        // remove the detached sources
        sourcesToHandle.getOrDefault(DETACHED, emptyList()).stream()
                .forEach(src -> removeSource(src));

        Map<ContentProjectFilter.State, List<ContentProjectFilter>> filtersToHandle =
                project.getProjectFilters().stream().collect(groupingBy(f -> f.getState()));
        // newly attached filters get built
        filtersToHandle.getOrDefault(ContentProjectFilter.State.ATTACHED, emptyList()).stream()
                .forEach(f -> f.setState(ContentProjectFilter.State.BUILT));
        // remove the detached filters
        filtersToHandle.getOrDefault(ContentProjectFilter.State.DETACHED, emptyList()).stream()
                .forEach(f -> removeFilter(f));

    }

    private static void removeSource(ProjectSource source) {
        source.getContentProject().removeSource(source);
        ContentProjectFactory.remove(source);
    }

    private static void removeFilter(ContentProjectFilter filter) {
        filter.getProject().getProjectFilters().remove(filter);
        ContentProjectFactory.remove(filter);
    }

    /**
     * Align {@link ContentEnvironment} {@link Channel}s to given {@link Channel}s
     *
     * @param env the Environment
     * @param baseChannel the base Channel
     * @param childChannels the child Channels
     * @param async run the time-expensive operations asynchronously?
     * @param user the user
     */
    private static void alignEnvironment(ContentEnvironment env, Channel baseChannel, Stream<Channel> childChannels,
            boolean async, User user) {
        // ensure targets for the sources exist
        List<Pair<Channel, SoftwareEnvironmentTarget>> newSrcTgtPairs =
                cloneChannelsToEnv(env, baseChannel, childChannels, user);

        // remove targets that are not needed anymore
        Set<SoftwareEnvironmentTarget> newTargets = newSrcTgtPairs.stream()
                .map(pair -> pair.getRight())
                .collect(toSet());
        env.getTargets().stream()
                .flatMap(t -> stream(t.asSoftwareTarget()))
                .filter(tgt -> !newTargets.contains(tgt))
                .sorted((t1, t2) -> Boolean.compare(t1.getChannel().isBaseChannel(), t2.getChannel().isBaseChannel()))
                .forEach(toRemove -> ContentProjectFactory.purgeTarget(toRemove));

        // align the contents
        newSrcTgtPairs.forEach(srcTgt -> ChannelManager.
                alignEnvironmentTarget(srcTgt.getLeft(), srcTgt.getRight(), async, user));
    }

    /**
     * Clone {@link Channel}s to given {@link ContentEnvironment}
     *
     * @param env the Environment to which the Sources are cloned
     * @param leader the "leader" Channel
     * @param channels the "non-leader" Channels
     * @param user the user
     * @return the List of [original Channel, SoftwareEnvironmentTarget] Pairs
     */
    private static List<Pair<Channel, SoftwareEnvironmentTarget>> cloneChannelsToEnv(ContentEnvironment env,
            Channel leader, Stream<Channel> channels, User user) {
        // first make sure the leader exists
        SoftwareEnvironmentTarget leaderTarget = lookupTarget(leader, env, user)
                .map(tgt -> {
                    tgt.getChannel().setParentChannel(null);
                    return tgt;
                })
                .orElseGet(() -> createSoftwareTarget(leader, empty(), env, user));

        // then do the same with the children
        Stream<Pair<Channel, SoftwareEnvironmentTarget>> nonLeaderTargets = channels
                .map(src -> lookupTarget(src, env, user)
                        .map(tgt -> {
                            tgt.getChannel().setParentChannel(leaderTarget.getChannel());
                            return Pair.of(src, tgt);
                        })
                        .orElseGet(() ->
                                Pair.of(src, createSoftwareTarget(src, of(leaderTarget.getChannel()), env, user))));

        return Stream.concat(
                Stream.of(Pair.of(leader, leaderTarget)),
                nonLeaderTargets)
                .collect(toList());
    }

    private static Optional<SoftwareEnvironmentTarget> lookupTarget(Channel srcChannel, ContentEnvironment env,
            User user) {
        return ContentProjectFactory
                .lookupEnvironmentTargetByChannelLabel(channelLabelInEnvironment(srcChannel.getLabel(), env), user);
    }

    private static SoftwareEnvironmentTarget createSoftwareTarget(Channel sourceChannel, Optional<Channel> leader,
            ContentEnvironment env, User user) {
        String targetLabel = channelLabelInEnvironment(sourceChannel.getLabel(), env);

        Channel targetChannel = ofNullable(ChannelFactory.lookupByLabelAndUser(targetLabel, user))
                .orElseGet(() -> {
                    CloneChannelCommand cloneCmd = new CloneChannelCommand(EMPTY, sourceChannel);
                    cloneCmd.setUser(user);
                    cloneCmd.setName(channelLabelInEnvironment(sourceChannel.getName(), env));
                    cloneCmd.setLabel(targetLabel);
                    cloneCmd.setSummary(channelLabelInEnvironment(sourceChannel.getSummary(), env));
                    leader.ifPresent(l -> cloneCmd.setParentLabel(l.getLabel()));
                    return cloneCmd.create();
                });

        SoftwareEnvironmentTarget target = new SoftwareEnvironmentTarget(env, targetChannel);
        env.addTarget(target);
        return target;
    }

    /**
     * Create a channel label in given {@link ContentEnvironment} based on {@link Channel} label in previous
     * {@link ContentEnvironment}
     *
     * @param srcChannelLabel the source Channel label
     * @param env the Environment
     * @return the prefixed channel label
     */
    private static String channelLabelInEnvironment(String srcChannelLabel, ContentEnvironment env) {
        String envPrefix = prefixString(env);
        return env.getPrevEnvironmentOpt()
                .map(prevEnv -> srcChannelLabel.replaceAll("^" + prefixString(prevEnv), envPrefix))
                .orElse(envPrefix + srcChannelLabel);
    }

    /**
     * Create a prefix from given {@link ContentEnvironment}
     *
     * e.g. Environment with label "bar" in a Project with label "foo"
     * will return "foo-bar-".
     *
     * @param env the Environment
     * @return the prefix
     */
    private static String prefixString(ContentEnvironment env) {
        return env.getContentProject().getLabel() + DELIMITER + env.getLabel() + DELIMITER;
    }

    private static ContentProjectHistoryEntry addHistoryEntry(
            Optional<String> message, User user, ContentProject project
    ) {
        ContentProjectHistoryEntry entry = new ContentProjectHistoryEntry();
        entry.setUser(user);
        entry.setMessage(message.orElse("Content Project build"));
        ContentProjectFactory.addHistoryEntryToProject(project, entry);
        return entry;
    }

    /**
     * Ensures that given user has the Org admin role
     *
     * @param user the user
     * @throws com.redhat.rhn.common.security.PermissionException if the user does not have Org admin role
     */
    private static void ensureOrgAdmin(User user) {
        if (!user.hasRole(ORG_ADMIN)) {
            throw new PermissionException(ORG_ADMIN);
        }
    }
}
