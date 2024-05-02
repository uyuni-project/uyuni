/*
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

import static com.redhat.rhn.domain.contentmgmt.ContentProjectFactory.lookupClonesInProject;
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.ErrataFilter;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.ModuleFilter;
import com.redhat.rhn.domain.contentmgmt.PackageFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.ProjectSource.Type;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.contentmgmt.validation.ContentPropertiesValidator;
import com.redhat.rhn.domain.errata.ClonedErrata;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.AlignSoftwareTargetAction;
import com.redhat.rhn.frontend.events.AlignSoftwareTargetMsg;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;

import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Content Management functionality
 */
public class ContentManager {

    private static final Logger LOG = LogManager.getLogger(ContentManager.class);
    private static final String DELIMITER = "-";
    private ModulemdApi modulemdApi;

    /**
     * Initialize a new instance
     */
    public ContentManager() {
        this(null);
    }

    /**
     * Initialize a new instance specifying the libmodulemd API instance to use
     * @param modulemdApiIn the libmodulemd API to use when resolving modular dependencies
     */
    public ContentManager(ModulemdApi modulemdApiIn) {
        this.modulemdApi = Objects.requireNonNullElseGet(modulemdApiIn, ModulemdApi::new);
    }

    /**
     * Create a Content Project
     *
     * @param label the label
     * @param name the name
     * @param description the description
     * @param user the creator
     * @throws EntityExistsException if a project with given label already exists
     * @throws PermissionException if given user does not have required role
     * @throws com.redhat.rhn.common.validator.ValidatorException if validation violation occurs
     * @return the created Content Project
     */
    public ContentProject createProject(String label, String name, String description, User user) {
        ensureOrgAdmin(user);
        lookupProject(label, user).ifPresent(cp -> {
            throw new EntityExistsException(cp);
        });
        ContentPropertiesValidator.validateProjectProperties(label, name, user);
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
     * @param label the label
     * @param user the user
     * @return Optional with matching Content Project
     */
    public static Optional<ContentProject> lookupProject(String label, User user) {
        return ContentProjectFactory.lookupProjectByLabelAndOrg(label, user.getOrg());
    }

    /**
     * Look up Content Project by name
     *
     * @param name the label
     * @param user the user
     * @return Optional with matching Content Project
     */
    public static Optional<ContentProject> lookupProjectByNameAndOrg(String name, User user) {
        return ContentProjectFactory.lookupProjectByNameAndOrg(name, user.getOrg());
    }

    /**
     * Update Content Project
     *
     * @param label the label for lookup
     * @param newName new name
     * @param newDesc new description
     * @param user the user
     * @throws EntityNotExistsException if Content Project with given label is not found
     * @throws PermissionException if given user does not have required role
     * @throws com.redhat.rhn.common.validator.ValidatorException if validation violation occurs
     * @return the updated Content Project
     */
    public ContentProject updateProject(String label, Optional<String> newName, Optional<String> newDesc,
            User user) {
        ensureOrgAdmin(user);
        return lookupProject(label, user)
                .map(cp -> {
                    ContentPropertiesValidator.validateProjectProperties(label, newName.orElse(cp.getName()), user);
                    newName.ifPresent(cp::setName);
                    newDesc.ifPresent(cp::setDescription);
                    return cp;
                })
                .orElseThrow(() -> new EntityNotExistsException(label));
    }

    /**
     * Remove Content Project
     *
     * @param label the label
     * @param user the user
     * @throws PermissionException if given user does not have required role
     * @throws EntityNotExistsException if Content Project with given label is not found
     * @return the number of objects affected
     */
    public int removeProject(String label, User user) {
        ensureOrgAdmin(user);
        return lookupProject(label, user)
                .map(ContentProjectFactory::remove)
                .orElseThrow(() -> new EntityNotExistsException(label));
    }

    /**
     * Create Content Environment
     *
     * @param projectLabel the Content Project label
     * @param predecessorLabel the predecessor Environment label
     * @param label the Environment label
     * @param name the Environment name
     * @param description the Environment description
     * @param async run the time-expensive operations asynchronously?
     * @param user the user performing the action
     * @throws EntityNotExistsException if Content Project with given label or Content Environment in the Project
     * is not found
     * @throws EntityExistsException if Environment with given parameters already exists
     * @throws PermissionException if given user does not have required role
     * @throws com.redhat.rhn.common.validator.ValidatorException if validation violation occurs
     * @return the created Content Environment
     */
    public ContentEnvironment createEnvironment(String projectLabel, Optional<String> predecessorLabel,
            String label, String name, String description, boolean async, User user) {
        ensureOrgAdmin(user);
        lookupEnvironment(label, projectLabel, user).ifPresent(e -> {
            throw new EntityExistsException(e);
        });
        ContentPropertiesValidator.validateEnvironmentProperties(name, label);
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
                            promoteProject(projectLabel, p.getLabel(), async, user);
                        }
                    });
                    return newEnv;
                }).orElseThrow(() -> new EntityNotExistsException(ContentProject.class, projectLabel));
    }

    /**
     * List Environments in a Content Project with the respect to their ordering
     *
     * @param projectLabel the Content Project label
     * @param user the user
     * @throws EntityNotExistsException if Content Project with given label is not found
     * @return the List of Content Environments with respect to their ordering
     */
    public static List<ContentEnvironment> listProjectEnvironments(String projectLabel, User user) {
        return lookupProject(projectLabel, user)
                .map(ContentProjectFactory::listProjectEnvironments)
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
    }

    /**
     * Look up Content Environment based on its label, Content Project label and User
     *
     * @param envLabel the Content Environment label
     * @param projectLabel the Content Project label
     * @param user the user
     * @throws EntityNotExistsException if Content Project with given label is not found
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
     * @param envLabel the Environment label
     * @param projectLabel the Content Project label
     * @param newName new name
     * @param newDescription new description
     * @param user the user
     * @throws EntityNotExistsException if Project or Environment is not found
     * @throws PermissionException if given user does not have required role
     * @throws com.redhat.rhn.common.validator.ValidatorException if validation violation occurs
     * @return the updated Environment
     */
    public ContentEnvironment updateEnvironment(String envLabel, String projectLabel, Optional<String> newName,
            Optional<String> newDescription, User user) {
        ensureOrgAdmin(user);
        return lookupEnvironment(envLabel, projectLabel, user)
                .map(env -> {
                    ContentPropertiesValidator.validateEnvironmentProperties(
                            newName.orElse(env.getName()),
                            env.getLabel() // label can't be changed
                    );
                    newName.ifPresent(env::setName);
                    newDescription.ifPresent(env::setDescription);
                    return env;
                })
                .orElseThrow(() -> new EntityNotExistsException(envLabel));
    }

    /**
     * Remove a Content Environment
     *
     * @param envLabel the Content Environment label
     * @param projectLabel the Content Project label
     * @param user the user
     * @throws PermissionException if given user does not have required role
     * @throws EntityNotExistsException if Project or Environment is not found
     * @throws PermissionException if given user does not have required role
     * @return number of deleted objects
     */
    public int removeEnvironment(String envLabel, String projectLabel, User user) {
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
     * @param projectLabel the Project label
     * @param sourceType the Source Type (e.g. SW_CHANNEL)
     * @param sourceLabel the Source label (e.g. SoftwareChannel label)
     * @param position the position of the Source (Optional)
     * @param user the user
     * @throws EntityNotExistsException when either the Project or the Source reference (e.g. Channel) is not found
     * @throws java.lang.IllegalArgumentException if the sourceType is unsupported
     * @return the created or existing Source
     */
    public ProjectSource attachSource(String projectLabel, Type sourceType, String sourceLabel,
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
     * @param projectLabel the Project label
     * @param sourceType the Source Type (e.g. SW_CHANNEL)
     * @param sourceLabel the Source label (e.g. SoftwareChannel label)
     * @param user the user
     */
    public void detachSource(String projectLabel, Type sourceType, String sourceLabel, User user) {
        ensureOrgAdmin(user);
        ProjectSource src = lookupProjectSource(projectLabel, sourceType, sourceLabel, user)
                .orElseThrow(() -> (new EntityNotExistsException(sourceLabel)));
        if (src.getState() == ATTACHED) {
            src.getContentProject().removeSource(src);
        }
        else {
            src.setState(DETACHED);
        }
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
     * List {@link SoftwareProjectSource}s that have patches needing resync.
     *
     * @param user the User
     * @param project the Project
     * @return a list of {@link SoftwareProjectSource}s that have patches needing resync
     */
    public static Set<SoftwareProjectSource> listActiveSwSourcesWithUnsyncedPatches(User user, ContentProject project) {
        if (project.getFirstEnvironmentOpt().isEmpty()) {
            return Set.of();
        }
        ContentEnvironment firstEnv = project.getFirstEnvironmentOpt().orElseThrow();

        return project.getActiveSources().stream()
                .flatMap(src -> src.asSoftwareSource().stream())
                .filter(src -> {
                    // we are interested in sources that have targets with patches needing resync
                    Optional<SoftwareEnvironmentTarget> tgt = lookupTargetByChannel(src.getChannel(), firstEnv, user);
                    return tgt
                            .map(t -> !ChannelManager.listErrataNeedingResync(t.getChannel(), user).isEmpty())
                            .orElse(false);
                })
                .collect(toSet());
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
     * Look up filter by id and user
     *
     * @param name the name
     * @param user the user
     * @return the matching filter
     */
    public static Optional<ContentFilter> lookupFilterByNameAndOrg(String name, User user) {
        return  ContentProjectFactory.lookupFilterByNameAndOrg(name, user.getOrg());
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
    public ContentFilter createFilter(String name, ContentFilter.Rule rule, ContentFilter.EntityType entityType,
            FilterCriteria criteria, User user) {
        ensureOrgAdmin(user);
        lookupFilterByNameAndOrg(name, user).ifPresent(cp -> {
            throw new EntityExistsException(cp);
        });

        ContentPropertiesValidator.validateFilterProperties(name);

        if (ContentFilter.EntityType.MODULE.equals(entityType) && ContentFilter.Rule.DENY.equals(rule)) {
            // DENY rule is not applicable for module filters
            throw new IllegalArgumentException("DENY rule is not applicable to appstream filters.");
        }
        return ContentProjectFactory.createFilter(name, rule, entityType, criteria, user);
    }

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
    public ContentFilter updateFilter(Long id, Optional<String> name, Optional<ContentFilter.Rule> rule,
            Optional<FilterCriteria> criteria, User user) {
        ensureOrgAdmin(user);
        ContentFilter filter = lookupFilterById(id, user)
                .orElseThrow(() -> new EntityNotExistsException(id));

        ContentPropertiesValidator.validateFilterProperties(name.orElse(filter.getName()));

        return ContentProjectFactory.updateFilter(filter, name, rule, criteria);
    }

    /**
     * Remove {@link ContentFilter}
     *
     * @param id the filter id
     * @param user the user
     * @return true if removed
     */
    public boolean removeFilter(Long id, User user) {
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
    public ContentFilter attachFilter(String projectLabel, Long filterId, User user) {
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
    public void detachFilter(String projectLabel, Long filterId, User user) {
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
    public void promoteProject(String projectLabel, String envLabel, boolean async, User user) {
        ensureOrgAdmin(user);
        ContentEnvironment env = lookupEnvironment(envLabel, projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(envLabel));
        ContentEnvironment nextEnv = env.getNextEnvironmentOpt()
                .orElseThrow(() -> new ContentManagementException("Environment " + envLabel +
                        " does not have successor"));

        // if current Environment or the next 2 Environments in the chain are building -> FORBID promote
        // as it could affect the build in progress
        if (isEnvironmentBuilding(of(env)) || isEnvironmentBuilding(of(nextEnv)) ||
                isEnvironmentBuilding(nextEnv.getNextEnvironmentOpt())) {
            throw new ContentManagementException("Build/Promote already in progress");
        }

        Map<Boolean, List<Channel>> envChannels = env.getTargets().stream()
                .flatMap(tgt -> stream(tgt.asSoftwareTarget()))
                .map(SoftwareEnvironmentTarget::getChannel)
                .collect(partitioningBy(Channel::isBaseChannel));
        List<Channel> baseChannels = envChannels.get(true);
        List<Channel> childChannels = envChannels.get(false);

        if (baseChannels.size() != 1) {
            throw new IllegalStateException("Environment " + envLabel + " must have exactly one leader channel");
        }

        alignEnvironment(nextEnv, baseChannels.get(0), childChannels.stream(), emptyList(), async, user);

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
     * @throws EntityNotExistsException if Project does not exist
     * @throws ContentManagementException when there are no Environments in the Project
     */
    public void buildProject(String projectLabel, Optional<String> message, boolean async, User user) {
        ensureOrgAdmin(user);
        ContentProject project = lookupProject(projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
        buildProject(project, message, async, user);
    }

    /**
     * Build a {@link ContentProject}
     *
     * @param project the project
     * @param message the optional message
     * @param async run the time-expensive operations asynchronously? (in the test code it is useful to run them
     * synchronously)
     * @param user the user
     * @throws EntityNotExistsException if Project does not exist
     * @throws ContentManagementException when there are no Environments in the Project
     */
    public void buildProject(ContentProject project, Optional<String> message, boolean async, User user) {
        ensureOrgAdmin(user);
        ContentEnvironment firstEnv = project.getFirstEnvironmentOpt()
                .orElseThrow(() -> new ContentManagementException("Cannot publish  project: " + project.getLabel() +
                        " with no environments."));
        // 1st or 2nd Environments is BUILDING -> FORBID another build as it could affect the build in progress
        if (isEnvironmentBuilding(of(firstEnv)) || isEnvironmentBuilding(firstEnv.getNextEnvironmentOpt())) {
            throw new ContentManagementException("Build/Promote already in progress");
        }

        buildSoftwareSources(firstEnv, async, user);
        ContentProjectHistoryEntry entry = addHistoryEntry(message, user, project);
        firstEnv.setVersion(entry.getVersion());
    }

    // helper method to determine if given environment is BUILDING
    private Boolean isEnvironmentBuilding(Optional<ContentEnvironment> env) {
        return env.flatMap(e -> e.computeStatus().map(status -> status.equals(EnvironmentTarget.Status.BUILDING)))
                .orElse(false);
    }

    /**
     * Build {@link SoftwareProjectSource}s assigned to {@link ContentProject}
     *
     * @param firstEnv first Environment of the Project
     * @param async run the time-expensive operations asynchronously?
     * @param user the user
     * @throws ContentManagementException when there is no leader channel in the Project
     */
    private void buildSoftwareSources(ContentEnvironment firstEnv, boolean async, User user) {
        ContentProject project = firstEnv.getContentProject();
        Channel leader = project.lookupSwSourceLeader()
                .map(SoftwareProjectSource::getChannel)
                .orElseThrow(() -> new ContentManagementException("Cannot publish  project: " + project.getLabel() +
                        " with no base channel associated with it."));
        Stream<Channel> otherChannels = project.getSources().stream()
                .flatMap(s -> stream(s.asSoftwareSource()))
                .filter(src -> !src.getChannel().equals(leader) && src.getState() != DETACHED)
                .map(SoftwareProjectSource::getChannel);

        alignEnvironment(firstEnv, leader, otherChannels, project.getActiveFilters(), async, user);

        Map<ProjectSource.State, List<ProjectSource>> sourcesToHandle = project.getSources().stream()
                .collect(groupingBy(ProjectSource::getState));
        // newly attached sources get built
        sourcesToHandle.getOrDefault(ATTACHED, emptyList()).stream()
                .forEach(src -> src.setState(BUILT));
        // remove the detached sources
        sourcesToHandle.getOrDefault(DETACHED, emptyList()).stream()
                .forEach(this::removeSource);

        Map<ContentProjectFilter.State, List<ContentProjectFilter>> filtersToHandle =
                project.getProjectFilters().stream().collect(groupingBy(ContentProjectFilter::getState));
        // newly attached filters get built
        filtersToHandle.getOrDefault(ContentProjectFilter.State.ATTACHED, emptyList()).stream()
                .forEach(f -> f.setState(ContentProjectFilter.State.BUILT));
        filtersToHandle.getOrDefault(ContentProjectFilter.State.EDITED, emptyList()).stream()
                .forEach(f -> f.setState(ContentProjectFilter.State.BUILT));
        // remove the detached filters
        filtersToHandle.getOrDefault(ContentProjectFilter.State.DETACHED, emptyList()).stream()
                .forEach(this::removeFilter);

    }

    private void removeSource(ProjectSource source) {
        source.getContentProject().removeSource(source);
        ContentProjectFactory.remove(source);
    }

    private void removeFilter(ContentProjectFilter filter) {
        filter.getProject().getProjectFilters().remove(filter);
        ContentProjectFactory.remove(filter);
    }

    /**
     * Align {@link ContentEnvironment} {@link Channel}s to given {@link Channel}s
     *
     * @param env the Environment
     * @param baseChannel the base Channel
     * @param childChannels the child Channels
     * @param filters the {@link ContentFilter}s
     * @param async run the time-expensive operations asynchronously?
     * @param user the user
     */
    private void alignEnvironment(ContentEnvironment env, Channel baseChannel, Stream<Channel> childChannels,
            List<ContentFilter> filters, boolean async, User user) {
        // ensure targets for the sources exist
        List<Pair<Channel, SoftwareEnvironmentTarget>> newSrcTgtPairs =
                cloneChannelsToEnv(env, baseChannel, childChannels, user);

        // remove targets that are not needed anymore
        Set<SoftwareEnvironmentTarget> newTargets = newSrcTgtPairs.stream()
                .map(Pair::getRight)
                .collect(toSet());
        env.getTargets().stream()
                .flatMap(t -> stream(t.asSoftwareTarget()))
                .filter(tgt -> !newTargets.contains(tgt))
                .sorted((t1, t2) -> Boolean.compare(t1.getChannel().isBaseChannel(), t2.getChannel().isBaseChannel()))
                .forEach(ContentProjectFactory::purgeTarget);


        // Resolve filters for dependencies
        try {
            DependencyResolver resolver = new DependencyResolver(env.getContentProject(), this.modulemdApi);
            DependencyResolutionResult result = resolver.resolveFilters(filters);

            // align the contents
            newSrcTgtPairs.forEach(srcTgt -> alignEnvironmentTarget(srcTgt.getLeft(), srcTgt.getRight(),
                    result.getFilters(), async, user));
        }
        catch (DependencyResolutionException e) {
            // Build shouldn't be allowed if dependency resolution fails
            throw new RuntimeException(e);
        }

    }

    private static void stripModuleMetadata(Channel channel) {
        if (channel != null && channel.getModules() != null) {
            HibernateFactory.getSession().delete(channel.getModules());
            channel.setModules(null);
            AppStreamsManager.listChannelAppStreams(channel.getId()).forEach(a ->
                    HibernateFactory.getSession().delete(a)
            );
        }
    }

    private static void syncGpgKeyInfo(Channel source, Channel target) {
        target.setGPGCheck(source.isGPGCheck());
        target.setGPGKeyFp(source.getGPGKeyFp());
        target.setGPGKeyId(source.getGPGKeyId());
        target.setGPGKeyUrl(source.getGPGKeyUrl());
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
    private List<Pair<Channel, SoftwareEnvironmentTarget>> cloneChannelsToEnv(ContentEnvironment env,
            Channel leader, Stream<Channel> channels, User user) {
        boolean moduleFiltersPresent =
                !extractFiltersOfType(env.getContentProject().getActiveFilters(), ModuleFilter.class).isEmpty();

        // first make sure the leader exists
        SoftwareEnvironmentTarget leaderTarget = lookupTargetByChannel(leader, env, user)
                .map(tgt -> fixTargetProperties(tgt, leader, null, moduleFiltersPresent))
                .orElseGet(() -> createSoftwareTarget(leader, empty(), env, user, moduleFiltersPresent));

        // then do the same with the children
        Stream<Pair<Channel, SoftwareEnvironmentTarget>> nonLeaderTargets = channels
                .map(src -> lookupTargetByChannel(src, env, user)
                        .map(tgt -> fixTargetProperties(tgt, src, leaderTarget.getChannel(), moduleFiltersPresent))
                        .map(tgt -> Pair.of(src, tgt))
                        .orElseGet(() -> Pair.of(src, createSoftwareTarget(
                                src, of(leaderTarget.getChannel()), env, user, moduleFiltersPresent))));

        List<Pair<Channel, SoftwareEnvironmentTarget>> srcTgtPairs = Stream.concat(
                Stream.of(Pair.of(leader, leaderTarget)),
                nonLeaderTargets)
                .collect(toList());

        // Refresh pillar data for the assigned clients
        ServerFactory.listMinionsByChannel(leaderTarget.getChannel().getId()).forEach(ms ->
                MinionPillarManager.INSTANCE.generatePillar(ms, false, MinionPillarManager.PillarSubset.GENERAL));

        return srcTgtPairs;
    }

    /**
     * Fixes target properties:
     *
     * - parent-child relation
     * - original-clone relation of source and target. This makes sure that:
     *   1. Old clones of source in the same content project will become clones of the target
     *   2. Target will become clone of the source
     *
     * @param swTgt the target
     * @param newSource new source channel of the target
     * @param newParent new parent channel of the target
     * @param stripModuleData whether to strip the module data
     * @return the fixed target
     */
    private static SoftwareEnvironmentTarget fixTargetProperties(SoftwareEnvironmentTarget swTgt, Channel newSource,
            Channel newParent, boolean stripModuleData) {
        Channel tgt = swTgt.getChannel();
        // make sure parent is set correctly
        tgt.setParentChannel(newParent);

        // fix the original-clone relation
        tgt.asCloned().ifPresentOrElse(
                t -> t.setOriginal(newSource),
                () -> {
                    LOG.info("Channel is not a clone: {}. Adding clone info.", tgt);
                    ChannelManager.addCloneInfo(newSource.getId(), tgt.getId());
                });

        // handle the module data: if there are modules filters present, we strip them, even if the source is modular;
        // otherwise we set them according to the source channel modules
        if (stripModuleData) {
            stripModuleMetadata(tgt);
        }
        else {
            tgt.cloneModulesFrom(newSource);
        }

        // Sync GPG key info to target in case it's updated since last build
        syncGpgKeyInfo(newSource, tgt);

        return swTgt;
    }

    /**
     * Return a Software Target by given Channel in an environment.
     * @param srcChannel the source channel
     * @param env the CLM environment
     * @param user the user
     * @return found software target
     */
    public static Optional<SoftwareEnvironmentTarget> lookupTargetByChannel(Channel srcChannel, ContentEnvironment env,
            User user) {
        return ContentProjectFactory
                .lookupEnvironmentTargetByChannelLabel(channelLabelInEnvironment(srcChannel.getLabel(), env), user);
    }

    private SoftwareEnvironmentTarget createSoftwareTarget(Channel sourceChannel, Optional<Channel> leader,
            ContentEnvironment env, User user, boolean stripModularMetadata) {
        List<ClonedChannel> oldSuccessors = lookupClonesInProject(sourceChannel, env.getContentProject());
        String targetLabel = channelLabelInEnvironment(sourceChannel.getLabel(), env);

        Channel targetChannel = ofNullable(ChannelFactory.lookupByLabelAndUser(targetLabel, user))
                .orElseGet(() -> {
                    CloneChannelCommand cloneCmd = new CloneChannelCommand(EMPTY, sourceChannel);
                    cloneCmd.setUser(user);
                    cloneCmd.setName(channelLabelInEnvironment(sourceChannel.getName(), env));
                    cloneCmd.setLabel(targetLabel);
                    cloneCmd.setSummary(channelLabelInEnvironment(sourceChannel.getSummary(), env));
                    cloneCmd.setStripModularMetadata(stripModularMetadata);
                    leader.ifPresent(l -> cloneCmd.setParentLabel(l.getLabel()));
                    return cloneCmd.create();
                });

        // make sure the old successor of the sourceChannel in the project points to targetChannel now
        oldSuccessors.forEach(c -> c.setOriginal(targetChannel));

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

    private ContentProjectHistoryEntry addHistoryEntry(
            Optional<String> message, User user, ContentProject project
    ) {
        ContentProjectHistoryEntry entry = new ContentProjectHistoryEntry();
        entry.setUser(user);
        entry.setMessage(message.orElse("Content Project build"));
        ContentProjectFactory.addHistoryEntryToProject(project, entry);
        return entry;
    }

    /**
     * Align packages and errata of the {@link SoftwareEnvironmentTarget} to the source {@link Channel}
     *
     * @param src the source {@link Channel}
     * @param tgt the target {@link SoftwareEnvironmentTarget}
     * @param filters the {@link ContentFilter}s
     * @param async run this operation asynchronously?
     * @param user the user
     */
    public void alignEnvironmentTarget(Channel src, SoftwareEnvironmentTarget tgt, List<ContentFilter> filters,
            boolean async, User user) {
        // adjust the target status
        tgt.setStatus(EnvironmentTarget.Status.BUILDING);
        ContentProjectFactory.save(tgt);

        AlignSoftwareTargetMsg msg = new AlignSoftwareTargetMsg(src, tgt, filters, user);
        if (async) {
            MessageQueue.publish(msg);
        }
        else {
            new AlignSoftwareTargetAction().execute(msg);
        }
    }

    /**
     * Synchronously align packages and errata of the {@link Channel} to the source {@link Channel}
     * This method is potentially time-expensive and should be run asynchronously (@see alignEnvironmentTarget)
     *
     * @param filters the filters
     * @param src the source {@link Channel}
     * @param tgt the target {@link Channel}
     * @param user the user
     */
    public void alignEnvironmentTargetSync(Collection<ContentFilter> filters, Channel src, Channel tgt, User user) {
        List<PackageFilter> packageFilters = extractFiltersOfType(filters, PackageFilter.class);
        List<ErrataFilter> errataFilters = extractFiltersOfType(filters, ErrataFilter.class);

        Set<Package> oldTgtPackages = new HashSet<>(tgt.getPackages());

        // align packages
        alignPackages(src, tgt, packageFilters);

        // align errata and the cache (rhnServerNeededCache)
        alignErrata(src, tgt, errataFilters, user);

        // align the package cache
        // this must be done after aligning errata since some packages may belong to a retracted erratum and we don't
        // want them in the cache. For this we need the errata to be up-to-date in target
        alignPackageCache(tgt, oldTgtPackages);

        // a lot was inserted into tables at this point. Make sure stats are up-to-date before continuing
        analyzeAlignTables();

        // Also check if content of cloned errata needs alignment (advisory status etc.)
        if (user.getOrg().getOrgConfig().isClmSyncPatches()) {
            ChannelManager.listErrataNeedingResync(tgt, user).forEach(e -> {
                ClonedErrata cloned = (ClonedErrata) ErrataManager.lookupErrata(e.getId(), user);
                ErrataFactory.syncErrataDetails(cloned);
            });
        }

        // update the channel newest packages cache
        ChannelFactory.refreshNewestPackageCache(tgt, "java::alignPackages");

        // now request repo regen
        tgt.setLastModified(new Date());
        HibernateFactory.getSession().saveOrUpdate(tgt);
        ChannelManager.queueChannelChange(tgt.getLabel(), "java::alignChannel", "Channel aligned");
    }

    /**
     * Run database analyze in tables more affected by the CLM channel align
     */
    private void analyzeAlignTables() {
        ChannelFactory.analyzeChannelPackages();
        ChannelFactory.analyzeErrataPackages();
        ChannelFactory.analyzeChannelErrata();
        ChannelFactory.analyzeErrataCloned();
        ChannelFactory.analyzeErrata();
        ChannelFactory.analyzeServerNeededCache();
    }

    private void alignPackageCache(Channel channel, Set<Package> oldChannelPackages) {
        // remove entries for deleted packages
        Set<Package> removedPackages = new HashSet<>(oldChannelPackages);
        removedPackages.removeAll(channel.getPackages());
        ErrataCacheManager.deleteCacheEntriesForChannelPackages(channel.getId(), extractPackageIds(removedPackages));

        // add cache entries for new ones
        Set<Package> newTgtPackages = new HashSet<>(channel.getPackages());
        newTgtPackages.removeAll(oldChannelPackages);
        ErrataCacheManager.insertCacheForChannelPackages(channel.getId(), null, extractPackageIds(newTgtPackages));
    }

    // helper for extracting certain filter types
    // it's not optimal to run this method multiple times for same collection of filters, but at least it's clear
    private static <T> List<T> extractFiltersOfType(Collection<ContentFilter> filters, Class<T> type) {
        return filters.stream()
                .filter(f -> type.isAssignableFrom(f.getClass()))
                .map(f -> (T) f)
                .collect(toList());
    }

    private void alignPackages(Channel srcChannel, Channel tgtChannel, Collection<PackageFilter> filters) {
        tgtChannel.getPackages().clear();
        LOG.debug("Filtering {} entities through {} filter(s)", srcChannel.getPackages().size(), filters.size());
        Set<Package> newPackages = filterEntities(srcChannel.getPackages(), filters).getLeft();
        tgtChannel.getPackages().addAll(newPackages);
        ChannelFactory.save(tgtChannel);
    }

    /**
     * Align {@link Errata} of a target {@link Channel} to the source {@link Channel}
     *
     * Alignment has 4 steps:
     * 1. Compute included and excluded errata based on given source channel and {@link ErrataFilter}s
     * 2. Remove (truncate) those errata in target channel, which are not in included errata (or which do not have
     * original in the included errata)
     * 3. Remove the {@link Package}s from excluded errata from target channel
     * 4. Merge the included errata to target channel
     *
     * @param src the source {@link Channel}
     * @param tgt the target {@link Channel}
     * @param errataFilters the {@link ErrataFilter}s
     * @param user the {@link User}
     */
    private void alignErrata(Channel src, Channel tgt, Collection<ErrataFilter> errataFilters, User user) {
        LOG.debug("Filtering {} entities through {} filter(s)", src.getErratas().size(), errataFilters.size());
        Pair<Set<Errata>, Set<Errata>> partitionedErrata = filterEntities(src.getErratas(), errataFilters);
        Set<Errata> includedErrata = partitionedErrata.getLeft();
        Set<Errata> excludedErrata = partitionedErrata.getRight();

        // Truncate extra errata in target channel
        ErrataManager.truncateErrata(includedErrata, tgt, user);
        // Remove packages from excluded errata
        ErrataManager.removeErratumAndPackagesFromChannel(excludedErrata, includedErrata, tgt, user);
        // Merge the included errata
        ErrataManager.mergeErrataToChannel(user, includedErrata, tgt, src, false, false);
    }

    /**
     * Filters given entities based on given filters.
     *
     * Returns a Pair containing:
     * - Left side: set of entities that are kept (not filtered out)
     * - Right side: set of entities that are filtered out
     *
     * Entities are processed one-by-one by filters as follows:
     * - when any DENY filter is satisfied for an entity -> this entity gets filtered out
     * - when an ALLOW is satisfied for an entity -> this entity gets NOT filtered out (even if it had been filtered out
     *   by a DENY filter = ALLOW filters have higher priority)
     *
     * @param entities entities, e.g. packages
     * @param filters filters, e.g. package filters
     * @param <T> the type of the entity (e.g. Package)
     * @return Pair containing (left side) a set of entities not filtered-out
     * and (right side) a set of entities filtered out
     */
    private <T> Pair<Set<T>, Set<T>> filterEntities(Set<T> entities, Collection<? extends ContentFilter<T>> filters) {
        Map<ContentFilter.Rule, List<ContentFilter<T>>> filtersByRule = filters.stream()
                .collect(groupingBy(ContentFilter::getRule));
        List<ContentFilter<T>> denyFilters = filtersByRule.getOrDefault(ContentFilter.Rule.DENY, emptyList());
        List<ContentFilter<T>> allowFilters = filtersByRule.getOrDefault(ContentFilter.Rule.ALLOW, emptyList());

        // First add the denied packages by testing all DENY filters against a package, and then filter out any package
        // that is explicitly allowed by testing all ALLOW filters against the same package and negating the result.
        Set<T> denied = entities.stream().filter(
                e -> denyFilters.stream().anyMatch(f -> f.test(e)) &&
                allowFilters.stream().noneMatch(f -> f.test(e))
        ).collect(Collectors.toUnmodifiableSet());

        Set<T> allowed = new HashSet<>(entities);
        allowed.removeAll(denied);
        return Pair.of(allowed, denied);
    }

    private static List<Long> extractPackageIds(Collection<Package> packages) {
        return packages.stream().map(Package::getId).collect(toList());
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

    /**
     * @return the libmodulemd API instance
     */
    public ModulemdApi getModulemdApi() {
        return modulemdApi;
    }

    /**
     * @param modulemdApiIn the libmodulemd API instance
     */
    public void setModulemdApi(ModulemdApi modulemdApiIn) {
        this.modulemdApi = modulemdApiIn;
    }
}
