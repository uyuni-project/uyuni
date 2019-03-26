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

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.ATTACHED;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.BUILT;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.DETACHED;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static com.redhat.rhn.manager.channel.CloneChannelCommand.CloneBehavior.EMPTY;
import static com.suse.utils.Opt.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
     * @param user - the user performing the action
     * @throws EntityNotExistsException - if Content Project with given label or Content Environment in the Project
     * is not found
     * @throws EntityExistsException - if Environment with given parameters already exists
     * @throws PermissionException if given user does not have required role
     * @return the created Content Environment
     */
    public static ContentEnvironment createEnvironment(String projectLabel, Optional<String> predecessorLabel,
            String label, String name, String description, User user) {
        ensureOrgAdmin(user);
        lookupEnvironment(label, projectLabel, user).ifPresent(e -> { throw new EntityExistsException(e); });
        return lookupProject(projectLabel, user)
                .map(cp -> {
                    ContentEnvironment newEnv = new ContentEnvironment(label, name, description, cp);
                    Optional<ContentEnvironment> predecessor = predecessorLabel.map(pl ->
                            ContentProjectFactory.lookupEnvironmentByLabelAndProject(pl, cp)
                                    .orElseThrow(() -> new EntityNotExistsException(ContentEnvironment.class, label)));
                    ContentProjectFactory.insertEnvironment(newEnv, predecessor);
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
        buildSoftwareSources(project, firstEnv, async, user);
        addHistoryEntry(message, user, project);
        firstEnv.increaseVersion();
    }

    /**
     * Build {@link SoftwareProjectSource}s assigned to {@link ContentProject}
     *
     * @param project the Project
     * @param firstEnv first Environment of the Project
     * @param async run the time-expensive operations asynchronously?
     * @param user the user
     */
    private static void buildSoftwareSources(ContentProject project, ContentEnvironment firstEnv, boolean async,
            User user) {
        Channel leaderChan = project.lookupSwSourceLeader()
                .map(l -> l.getChannel())
                .orElseThrow(() -> new ContentManagementException("Cannot publish  project: " + project.getLabel() +
                        " with no base channel associated with it."));
        Stream<Channel> otherChans = project.getSources().stream()
                .flatMap(s -> stream(s.asSoftwareSource()))
                .filter(src -> !src.getChannel().equals(leaderChan) && src.getState() != DETACHED)
                .map(s -> s.getChannel());

        // ensure targets for the sources exist
        List<Pair<Channel, Channel>> newSrcTgtPairs = cloneChannelsToEnv(leaderChan, otherChans, firstEnv, user);

        // remove targets that are not needed anymore
        Set<Channel> newTargets = newSrcTgtPairs.stream()
                .map(pair -> pair.getRight())
                .collect(toSet());
        ContentProjectFactory.lookupEnvironmentTargets(firstEnv)
                .flatMap(t -> stream(t.asSoftwareTarget()))
                .filter(tgt -> !newTargets.contains(tgt.getChannel()))
                .forEach(toRemove -> ContentProjectFactory.purgeTarget(toRemove));

        // remove the detached sources
        project.getSources().stream()
                .filter(src -> src.getState() != BUILT)
                .forEach(src -> {
                    if (src.getState() == ATTACHED) {
                        // newly ATTACHED sources get BUILT
                        src.setState(BUILT);
                    }
                    else {
                        // newly DETACHED sources get deleted
                        ContentProjectFactory.remove(src);
                    }
                });

        // align the contents
        newSrcTgtPairs
                .forEach(srcTgt -> ChannelManager.alignChannels(srcTgt.getLeft(), srcTgt.getRight(), async, user));
    }

    /**
     * Clone {@link Channel}s to given {@link ContentEnvironment}
     *
     * @param leader the "leader" Channel
     * @param channels the "non-leader" Channels
     * @param env the Environment to which the Sources are cloned
     * @param user the user
     * @return the List of [original Channel, newly cloned Channel] Pairs
     */
    private static List<Pair<Channel, Channel>> cloneChannelsToEnv(ContentEnvironment env, Channel leader,
            Stream<Channel> channels, User user) {
        // first make sure the leader exists
        Channel leaderTarget = lookupTargetChannel(leader, env, user)
                .map(tgt -> {
                    tgt.setParentChannel(null);
                    return tgt;
                })
                .orElseGet(() -> createSoftwareTarget(leader, empty(), env, user));

        // then do the same with the children
        Stream<Pair<Channel, Channel>> nonLeaderTargets = channels
                .map(src -> lookupTargetChannel(src, env, user)
                        .map(tgt -> {
                            tgt.setParentChannel(leaderTarget);
                            return Pair.of(src, tgt);
                        })
                        .orElseGet(() -> Pair.of(src, createSoftwareTarget(src, of(leaderTarget), env, user))));

        return Stream.concat(
                Stream.of(Pair.of(leader, leaderTarget)),
                nonLeaderTargets)
                .collect(toList());
    }

    private static Optional<Channel> lookupTargetChannel(Channel srcChannel, ContentEnvironment env, User user) {
        return ContentProjectFactory
                .lookupEnvironmentTargetByChannelLabel(channelLabelInEnvironment(srcChannel.getLabel(), env), user)
                .map(t -> t.getChannel());
    }

    private static Channel createSoftwareTarget(Channel sourceChannel, Optional<Channel> leader, ContentEnvironment env,
            User user) {
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
        ContentProjectFactory.save(target);
        return targetChannel;
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

    private static void addHistoryEntry(Optional<String> message, User user, ContentProject project) {
        ContentProjectHistoryEntry entry = new ContentProjectHistoryEntry();
        entry.setUser(user);
        entry.setMessage(message.orElse("Content Project build"));
        ContentProjectFactory.addHistoryEntryToProject(project, entry);
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
