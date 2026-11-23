/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.manager.contentmgmt;

import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Helpers shared by the {@link ContentManager} tests.
 */
public final class ContentManagerTestUtils {

    private ContentManagerTestUtils() { }

    /**
     * Creates a content project with a chain of environments, attaches a software channel source, builds the
     * first environment and promotes it down to the last one, so every environment has a hotfix channel
     * labeled {@code <projectLabel>-<envLabel>-hotfix}.
     *
     * @param contentManager the content manager
     * @param user the user, whose organization owns the project
     * @param projectLabel the project label
     * @param projectSource the software channel source of the project
     * @param envLabels the environment labels, from the first to the last
     */
    public static void buildProjectWithEnvironments(
            ContentManager contentManager,
            User user,
            String projectLabel,
            Channel projectSource,
            String... envLabels
    ) {
        buildProjectWithEnvironments(contentManager, user, projectLabel, List.of(projectSource), envLabels);
    }

    /**
     * Same as {@link #buildProjectWithEnvironments(ContentManager, User, String, Channel, String...)}, attaching
     * several software channel sources, the parent channel first.
     *
     * @param contentManager the content manager
     * @param user the user, whose organization owns the project
     * @param projectLabel the project label
     * @param projectSources the software channel sources of the project
     * @param envLabels the environment labels, from the first to the last
     */
    public static void buildProjectWithEnvironments(
            ContentManager contentManager,
            User user,
            String projectLabel,
            List<Channel> projectSources,
            String... envLabels
    ) {
        ContentProjectFactory.save(new ContentProject(projectLabel, projectLabel, "desc", user.getOrg()));
        Optional<String> predecessor = empty();
        for (String envLabel : envLabels) {
            contentManager.createEnvironment(projectLabel, predecessor, envLabel, envLabel, "desc", false, user);
            predecessor = of(envLabel);
        }
        for (Channel projectSource : projectSources) {
            contentManager.attachSource(projectLabel, SW_CHANNEL, projectSource.getLabel(), empty(), user);
        }
        contentManager.buildProject(projectLabel, empty(), false, user);
        for (int i = 0; i < envLabels.length - 1; i++) {
            contentManager.promoteProject(projectLabel, envLabels[i], false, user);
        }
    }

    /**
     * Returns the hotfix target of an environment.
     *
     * @param env the environment
     * @return the hotfix target, if any
     */
    public static Optional<SoftwareEnvironmentTarget> getHotfixTarget(ContentEnvironment env) {
        return env.getTargets().stream()
                .flatMap(t -> t.asSoftwareTarget().stream())
                .filter(SoftwareEnvironmentTarget::isHotfix)
                .findFirst();
    }

    /**
     * Returns the channels of an environment, except the hotfix channel which is not cloned from a source.
     *
     * @param env the environment
     * @return the channels
     */
    public static Set<Channel> getRegularChannels(ContentEnvironment env) {
        return env.getTargets().stream()
                .flatMap(t -> t.asSoftwareTarget().stream())
                .filter(t -> !t.isHotfix())
                .map(SoftwareEnvironmentTarget::getChannel)
                .collect(toSet());
    }
}
