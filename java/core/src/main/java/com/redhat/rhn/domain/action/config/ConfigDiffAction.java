/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.html.HtmlTag;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult;
import com.suse.salt.netapi.calls.LocalCall;
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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * ConfigDiffAction - Class representing TYPE_CONFIGFILES_DIFF
 */
@Entity
@DiscriminatorValue("18")
public class ConfigDiffAction extends ConfigAction {

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
                        server.getId() + "&crid=" + rev.getConfigRevision().getId());
                a.addBody(rev.getConfigRevision().getConfigFile().getConfigFileName()
                        .getPath());
                retval.append(a.render());
                retval.append(" (rev. " + rev.getConfigRevision().getRevision() + ")");
                if (rev.getConfigRevisionActionResult() != null) {
                    a.setAttribute("href",
                            "/rhn/systems/details/configuration/ViewDiffResult.do?sid=" +
                                    server.getId() + "&acrid=" +
                                    rev.getConfigRevisionActionResult().getConfigRevisionAction()
                                            .getId());
                    a.setBody(ls.getMessage("system.event.configFiesDiffExist"));
                    retval.append(" ");
                    retval.append(a.render());
                }
                if (rev.getFailureId() != null) {
                    retval.append(" ");
                    retval.append(ls.getMessage("system.event.configFiesMissing"));
                }
                retval.append("</br>");
            }
        }
        return retval.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<Map<String, Object>> fileStates = getConfigRevisionActions().stream()
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        handleFilesDiff(jsonResult);
        serverAction.setResultMsg(LocalizationService.getInstance().getMessage("configfiles.diffed"));
        /*
         * For comparison we are simply using file.managed state in dry-run mode, Salt doesn't return
         * 'result' attribute(actionFailed method check this attribute) when File(File, Dir, Symlink)
         * already exist on the system and action is considered as Failed even though there was no error.
         */
        serverAction.setStatusCompleted();
    }

    /**
     * Set the results based on the result from SALT
     * @param jsonResult response from SALT master
     */
    private void handleFilesDiff(JsonElement jsonResult) {
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

        if (null == getConfigRevisionActions()) {
            return;
        }

        getConfigRevisionActions().forEach(cra -> {
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


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> createActionSpecificDetails(ServerAction serverAction) {
        final List<Map<String, String>> additionalInfo = new ArrayList<>();

        // retrieve the details associated with the action...
        DataResult<Row> files = ActionManager.getConfigFileDiffList(getId());
        for (Row file : files) {
            Map<String, String> info = new HashMap<>();
            String path = (String) file.get("path");
            path += " (rev. " + file.get("revision") + ")";
            info.put("detail", path);

            String error = (String) file.get("failure_reason");
            if (error != null) {
                info.put("result", error);
            }
            else {
                // if there wasn't an error, check to see if there was a difference
                // detected...
                String diffString = HibernateFactory.getBlobContents(
                        file.get("diff"));
                if (diffString != null) {
                    info.put("result", diffString);
                }
            }
            additionalInfo.add(info);
        }
        return additionalInfo;
    }
}
