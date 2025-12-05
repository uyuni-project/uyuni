/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.manager.action.ActionManager;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * ConfigDeployAction - Class representing TYPE_CONFIGFILES_DEPLOY
 */
@Entity
@DiscriminatorValue("16")
public class ConfigDeployAction extends ConfigAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Map<Long, MinionSummary> targetMap = minionSummaries.stream().
                collect(Collectors.toMap(MinionSummary::getServerId, minionId-> minionId));

        Map<MinionSummary, Set<ConfigRevision>> serverConfigMap = getConfigRevisionActions()
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
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (serverAction.isStatusCompleted()) {
            serverAction.setResultMsg(LocalizationService.getInstance().getMessage("configfiles.deployed"));
        }
        else {
            serverAction.setResultMsg(SaltUtils.getJsonResultWithPrettyPrint(jsonResult));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> createActionSpecificDetails(ServerAction serverAction) {
        final List<Map<String, String>> additionalInfo = new ArrayList<>();

        // retrieve the details associated with the action...
        DataResult<Row> files = ActionManager.getConfigFileDeployList(getId());
        for (Row file : files) {
            Map<String, String> info = new HashMap<>();
            String path = (String) file.get("path");
            path += " (rev. " + file.get("revision") + ")";
            info.put("detail", path);
            String error = (String) file.get("failure_reason");
            if (error != null) {
                info.put("result", error);
            }
            additionalInfo.add(info);
        }

        return additionalInfo;
    }
}
