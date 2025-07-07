/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.html.HtmlTag;

import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ConfigAction - Class representation of the table rhnAction.
 */
public class ConfigAction extends Action {
    private Set<ConfigRevisionAction> configRevisionActions;

    /**
     * @return Returns the configRevisionActions.
     */
    public Set<ConfigRevisionAction> getConfigRevisionActions() {
        return configRevisionActions;
    }
    /**
     * @param configRevisionActionsIn The configRevisionActions to set.
     */
    public void setConfigRevisionActions(Set<ConfigRevisionAction>
                                            configRevisionActionsIn) {
        this.configRevisionActions = configRevisionActionsIn;
    }

    /**
     * Add a ConfigRevisionAction to the collection.
     * @param crIn the ConfigRevisionAction to add
     */
    public void addConfigRevisionAction(ConfigRevisionAction crIn) {
        if (configRevisionActions == null) {
            configRevisionActions = new HashSet<>();
        }
        crIn.setParentAction(this);
        configRevisionActions.add(crIn);
    }

    /**
     * Get the Formatter for this class but in this case we use
     * ConfigActionFormatter.
     *
     * {@inheritDoc}
     */
    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ConfigActionFormatter(this);
        }
        return formatter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHistoryDetails(Server server, User currentUser) {
        LocalizationService ls = LocalizationService.getInstance();
        StringBuilder retval = new StringBuilder();
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.configFiles"));
        retval.append("</br>");
        for (ConfigRevisionAction rev : this.getConfigRevisionActionsSorted()) {
            if (rev.getServer().equals(server)) {
                HtmlTag a = new HtmlTag("a");
                a.setAttribute("href", "/rhn/configuration/file/FileDetails.do?sid=" +
                        server.getId().toString() + "&crid=" +
                        rev.getConfigRevision().getId());
                a.addBody(rev.getConfigRevision().getConfigFile().getConfigFileName()
                        .getPath());
                retval.append(a.render());
                retval.append(" (rev. " + rev.getConfigRevision().getRevision() + ")");
                retval.append("</br>");
            }
        }
        return retval.toString();
    }

    /**
     * Sort the set of revision actions for their config file paths.
     * @return sorted list of revision actions
     */
    protected List<ConfigRevisionAction> getConfigRevisionActionsSorted() {
        List<ConfigRevisionAction> revisionActions = new ArrayList<>(
                this.getConfigRevisionActions());
        revisionActions.sort((o1, o2) -> {
            String p1 = o1.getConfigRevision().getConfigFile().
                    getConfigFileName().getPath();
            String p2 = o2.getConfigRevision().getConfigFile().
                    getConfigFileName().getPath();
            return p1.compareTo(p2);
        });
        return Collections.unmodifiableList(revisionActions);
    }


    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> deployFiles(List<MinionSummary> minionSummaries,
                                                               ConfigAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Map<Long, MinionSummary> targetMap = minionSummaries.stream().
                collect(Collectors.toMap(MinionSummary::getServerId, minionId-> minionId));

        Map<MinionSummary, Set<ConfigRevision>> serverConfigMap = action.getConfigRevisionActions()
                .stream()
                .filter(cra -> targetMap.containsKey(cra.getServer().getId()))
                .collect(Collectors.groupingBy(
                        cra -> targetMap.get(cra.getServer().getId()),
                        Collectors.mapping(ConfigRevisionAction::getConfigRevision, Collectors.toSet())));
        Map<Set<ConfigRevision>, Set<MinionSummary>> revsServersMap = serverConfigMap.entrySet()
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
        revsServersMap.forEach((configRevisions, selectedServers) -> {
            List<Map<String, Object>> fileStates = configRevisions
                    .stream()
                    .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                    .toList();
            ret.put(State.apply(List.of(SaltParameters.CONFIG_DEPLOY_FILES),
                            Optional.of(Collections.singletonMap(SaltParameters.PARAM_FILES, fileStates))),
                    new ArrayList<>(selectedServers));
        });
        return ret;
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> diffFiles(List<MinionSummary> minionSummaries,
                                                                   ConfigAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<Map<String, Object>> fileStates = action.getConfigRevisionActions().stream()
                .map(ConfigRevisionAction::getConfigRevision)
                .filter(revision -> revision.isFile() ||
                        revision.isDirectory() ||
                        revision.isSymlink())
                .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                .toList();
        ret.put(com.suse.salt.netapi.calls.modules.State.apply(
                List.of(SaltParameters.CONFIG_DIFF_FILES),
                Optional.of(Collections.singletonMap(SaltParameters.PARAM_FILES, fileStates)),
                Optional.of(true), Optional.of(true)), minionSummaries);
        return ret;
    }

}
