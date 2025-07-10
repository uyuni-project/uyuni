/*
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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * ConfigDeployAction - Class representing TYPE_CONFIGFILES_DEPLOY
 */
public class ConfigDeployAction extends ConfigAction {


    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @return minion summaries grouped by local call
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
     * @param serverAction
     * @param jsonResult
     * @param auxArgs
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (serverAction.getStatus().equals(ActionFactory.STATUS_COMPLETED)) {
            serverAction.setResultMsg(LocalizationService.getInstance().getMessage("configfiles.deployed"));
        }
        else {
            serverAction.setResultMsg(SaltUtils.getJsonResultWithPrettyPrint(jsonResult));
        }
    }


    /**
     *
     *
     * the following stuff goes in ConfigDiffAction
     * parked here for a while
     *
     *
     */


    /**
     * @param serverAction
     * @param jsonResult
     * @param auxArgs
     * @param action
     */
    public static void handleUpdateServerActionConfigDiffAction(ServerAction serverAction, JsonElement jsonResult,
                                                                UpdateAuxArgs auxArgs, ConfigAction action) {
        handleFilesDiff(jsonResult, action);
        serverAction.setResultMsg(LocalizationService.getInstance().getMessage("configfiles.diffed"));
        /**
         * For comparison we are simply using file.managed state in dry-run mode, Salt doesn't return
         * 'result' attribute(actionFailed method check this attribute) when File(File, Dir, Symlink)
         * already exist on the system and action is considered as Failed even though there was no error.
         */
        serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
    }

    /**
     * Set the results based on the result from SALT
     * @param jsonResult response from SALT master
     * @param action main action
     */
    private static void handleFilesDiff(JsonElement jsonResult, ConfigAction action) {
        TypeToken<Map<String, FilesDiffResult>> typeToken = new TypeToken<>() {
        };
        Map<String, FilesDiffResult> results = Json.GSON.fromJson(jsonResult, typeToken.getType());
        Map<String, FilesDiffResult> diffResults = new HashMap<>();
        // We are only interested in results where files are different/new.
        results.values().stream().filter(fdr -> !fdr.isResult())
                .forEach(fdr -> diffResults.put(
                        fdr.getName()
                                .flatMap(x -> x.fold(arr -> Arrays.stream(arr).findFirst(), Optional::of))
                                .orElse(null),
                        fdr));

        action.getConfigRevisionActions().forEach(cra -> {
            ConfigRevision cr = cra.getConfigRevision();
            String fileName = cr.getConfigFile().getConfigFileName().getPath();
            FilesDiffResult mapFileResult = diffResults.get(fileName);
            boolean isNew = false;
            if (mapFileResult != null) {
                if (cr.isFile()) {
                    FilesDiffResult.FileResult filePchanges =
                            mapFileResult.getPChanges(FilesDiffResult.FileResult.class);
                    isNew = filePchanges.getNewfile().isPresent();
                }
                else if (cr.isSymlink()) {
                    FilesDiffResult.SymLinkResult symLinkPchanges =
                            mapFileResult.getPChanges(FilesDiffResult.SymLinkResult.class);
                    isNew = symLinkPchanges.getNewSymlink().isPresent();
                }
                else if (cr.isDirectory()) {
                    TypeToken<Map<String, FilesDiffResult.DirectoryResult>> typeTokenD =
                            new TypeToken<>() {
                            };
                    FilesDiffResult.DirectoryResult dirPchanges = mapFileResult.getPChanges(typeTokenD).get(fileName);
                    isNew = dirPchanges.getDirectory().isPresent();
                }
                if (isNew) {
                    cra.setFailureId(1L); // 1 is for missing file(Client does not have this file yet)
                }
                else {
                    ConfigRevisionActionResult cresult = new ConfigRevisionActionResult();
                    cresult.setConfigRevisionAction(cra);
                    String result = StringEscapeUtils
                            .unescapeJava(YamlHelper.INSTANCE.dump(mapFileResult.getPChanges()));
                    cresult.setResult(result.getBytes());
                    cresult.setCreated(new Date());
                    cresult.setModified(new Date());
                    cra.setConfigRevisionActionResult(cresult);
                    SystemManager.updateSystemOverview(cra.getServer());
                }
            }
        });
    }

}
