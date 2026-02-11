/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.action.script;

import static com.suse.manager.webui.services.SaltConstants.SALT_FS_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.SCRIPTS_DIR;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.download.DownloadManager;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * ScriptRunAction
 */
@Entity
@DiscriminatorValue("30")
public class ScriptRunAction extends ScriptAction {
    /* Logger for this class */
    private static final Logger LOG = LogManager.getLogger(ScriptRunAction.class);

    private static boolean skipCommandScriptPerms = false;
    /**
     * Only used in unit tests.
     * @param skipCommandScriptPermsIn to set
     */
    public static void setSkipCommandScriptPerms(boolean skipCommandScriptPermsIn) {
        skipCommandScriptPerms = skipCommandScriptPermsIn;
    }

    private static final SaltUtils SALT_UTILS = GlobalInstanceHolder.SALT_UTILS;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHistoryDetails(Server server, User currentUser) {
        LocalizationService ls = LocalizationService.getInstance();
        StringBuilder retval = new StringBuilder();
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.runAs",
                StringEscapeUtils.escapeHtml4(getScriptActionDetails().getUsername()),
                StringEscapeUtils.escapeHtml4(getScriptActionDetails().getGroupname()))
        );
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.timeout",
                getScriptActionDetails().getTimeout()));
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.scriptContents"));
        retval.append("</br><pre>");
        retval.append(StringEscapeUtils.escapeHtml4(getScriptActionDetails()
                .getScriptContents()));
        retval.append("</pre></br>");
        for (ScriptResult sr : getScriptActionDetails().getResults()) {
            if (sr.getServerId().equals(server.getId())) {
                retval.append(ls.getMessage("system.event.scriptStart", sr.getStartDate()));
                retval.append("</br>");
                retval.append(ls.getMessage("system.event.scriptEnd", sr.getStopDate()));
                retval.append("</br>");
                retval.append(ls.getMessage("system.event.scriptReturnCode", sr
                        .getReturnCode().toString()));
                retval.append("</br>");
                retval.append(ls.getMessage("system.event.scriptRawOutput"));
                retval.append("<a data-senna-off=\"true\" target=\"_blank\" href=\"" +
                        DownloadManager.getScriptRawOutputDownloadPath(
                                this.getId(), sr.getActionScriptId(), currentUser) +
                        "\">");
                retval.append(ls.getMessage("system.event.downloadRawOutput"));
                retval.append("</a>");
                retval.append("</br>");
                retval.append(ls.getMessage("system.event.scriptFilteredOutput"));
                retval.append("</br>");
                retval.append("<pre>");
                retval.append(StringEscapeUtils.escapeHtml4(sr.getOutputContents()));
                retval.append("</pre>");
            }
        }
        return retval.toString();
    }

    @Override
    public void onCancelAction() {
        if (allServersFinished()) {
            FileUtils.deleteFile(SALT_UTILS.getScriptPath(getId()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minions) {
        String script = getScriptActionDetails().getScriptContents();

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        // write script to /srv/susemanager/salt/scripts/script_<action_id>.sh
        Path scriptFile = SALT_UTILS.getScriptPath(getId());
        try {
            // make sure parent dir exists
            if (!Files.exists(scriptFile) && !Files.exists(scriptFile.getParent())) {
                FileAttribute<Set<PosixFilePermission>> dirAttributes =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"));

                Files.createDirectory(scriptFile.getParent(), dirAttributes);
                // make sure correct user is set
            }

            if (!skipCommandScriptPerms) {
                setFileOwner(scriptFile.getParent());
            }

            // In case of action retry, the files script files will be already created.
            if (!Files.exists(scriptFile)) {
                FileAttribute<Set<PosixFilePermission>> fileAttributes =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"));
                Files.createFile(scriptFile, fileAttributes);
                org.apache.commons.io.FileUtils.writeStringToFile(scriptFile.toFile(),
                        script.replace("\r\n", "\n"), StandardCharsets.UTF_8);
            }

            if (!skipCommandScriptPerms) {
                setFileOwner(scriptFile);
            }

            // state.apply remotecommands
            Map<String, Object> pillar = new HashMap<>();
            pillar.put("mgr_remote_cmd_script", SALT_FS_PREFIX + SCRIPTS_DIR + "/" + scriptFile.getFileName());
            pillar.put("mgr_remote_cmd_runas", getScriptActionDetails().getUsername());
            pillar.put("mgr_remote_cmd_timeout", getScriptActionDetails().getTimeout());
            ret.put(State.apply(List.of(SaltParameters.REMOTE_COMMANDS), Optional.of(pillar)), minions);
        }
        catch (IOException e) {
            String errorMsg = "Could not write script to file " + scriptFile + " - " + e;
            LOG.error(errorMsg, e);
            getServerActions().stream()
                    .filter(entry -> entry.getServer().asMinionServer()
                            .map(minionServer -> minions.contains(new MinionSummary(minionServer)))
                            .orElse(false))
                    .forEach(sa -> {
                        sa.fail("Error scheduling the action: " + errorMsg);
                        ActionFactory.save(sa);
                    });
        }
        return ret;
    }

    private void setFileOwner(Path path) throws IOException {
        FileSystem fileSystem = FileSystems.getDefault();
        UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
        UserPrincipal tomcatUser = service.lookupPrincipalByName("tomcat");

        Files.setOwner(path, tomcatUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (serverAction.isStatusFailed()) {
            serverAction.setResultMsg("Failed to execute script. [jid=" + auxArgs.getJid() + "]");
        }
        else {
            serverAction.setResultMsg("Script executed successfully. [jid=" +
                    auxArgs.getJid() + "]");
        }
        Map<String, StateApplyResult<CmdResult>> stateApplyResult = Json.GSON.fromJson(jsonResult,
                new TypeToken<Map<String, StateApplyResult<CmdResult>>>() { }.getType());
        CmdResult result = new CmdResult();
        if (stateApplyResult != null) {
            result = stateApplyResult.entrySet().stream()
                    .findFirst().map(e -> e.getValue().getChanges())
                    .orElseGet(CmdResult::new);
        }

        ScriptResult scriptResult = Optional.ofNullable(
                        getScriptActionDetails().getResults())
                .orElse(Collections.emptySet())
                .stream()
                .filter(res -> serverAction.getServerId().equals(res.getServerId()))
                .findFirst()
                .orElse(new ScriptResult());

        getScriptActionDetails().addResult(scriptResult);
        scriptResult.setActionScriptId(getScriptActionDetails().getId());
        scriptResult.setServerId(serverAction.getServerId());
        scriptResult.setReturnCode(auxArgs.getRetcode());

        // Start and end dates
        Date startDate = getCreated().before(getEarliestAction()) ? getEarliestAction() : getCreated();
        scriptResult.setStartDate(startDate);
        scriptResult.setStopDate(serverAction.getCompletionTime());

        // Depending on the status show stdout or stderr in the output
        scriptResult.setOutput(SaltUtils.printStdMessages(result.getStderr(), result.getStdout()).getBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInvalidResults() {
        HibernateFactory.getSession().createNativeQuery("""
                  DELETE FROM rhnServerActionScriptResult sr WHERE sr.action_script_id = (
                  SELECT s.id FROM rhnActionScript s WHERE s.action_id = :action)
                  AND sr.server_id IN
                  (SELECT sa.server_id FROM rhnServerAction sa WHERE sa.action_id = :action AND sa.status = :queued)
                  """)
                .setParameter("action", getId(), StandardBasicTypes.LONG)
                .setParameter("queued", ActionFactory.STATUS_QUEUED.getId(), StandardBasicTypes.LONG)
                .executeUpdate();
    }
}
