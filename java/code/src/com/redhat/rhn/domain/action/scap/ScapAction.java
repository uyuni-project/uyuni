/*
 * Copyright (c) 2012--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.scap;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.audit.ScapManager;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Openscap;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ScapAction - Class representing TYPE_SCAP_*.
 */
public class ScapAction extends Action {
    private static final Logger LOG = LogManager.getLogger(ScapAction.class);

    private static String xccdfResumeXsl = "/usr/share/susemanager/scap/xccdf-resume.xslt.in";

    /**
     * Used only for testing
     * @param xccdfResumeXslIn to set
     */
    public static void setXccdfResumeXsl(String xccdfResumeXslIn) {
        xccdfResumeXsl = xccdfResumeXslIn;
    }

    private ScapActionDetails scapActionDetails;

    /**
     * @return Returns the scapActionDetails.
     */
    public ScapActionDetails getScapActionDetails() {
        return scapActionDetails;
    }

    /**
     * @param scapActionDetailsIn The scapActionDetails to set.
     */
    public void setScapActionDetails(ScapActionDetails scapActionDetailsIn) {
        scapActionDetailsIn.setParentAction(this);
        scapActionDetails = scapActionDetailsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHistoryDetails(Server server, User currentUser) {
        LocalizationService ls = LocalizationService.getInstance();
        StringBuilder retval = new StringBuilder();
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.scapPath"));
        retval.append(StringEscapeUtils.escapeHtml4(scapActionDetails.getPath()));
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.scapParams"));
        retval.append(scapActionDetails.getParameters() == null ? "" :
            StringEscapeUtils.escapeHtml4(scapActionDetails.getParametersContents()));
        if (scapActionDetails.getOvalfiles() != null) {
            retval.append("</br>");
            retval.append(ls.getMessage("system.event.scapOvalFiles"));
            retval.append(StringEscapeUtils.escapeHtml4(scapActionDetails.getOvalfiles()));
        }
        return retval.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {

       Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        Map<String, Object> pillar = new HashMap<>();
        Matcher profileMatcher = Pattern.compile("--profile (([\\w.-])+)")
                .matcher(scapActionDetails.getParametersContents());
        Matcher ruleMatcher = Pattern.compile("--rule (([\\w.-])+)")
                .matcher(scapActionDetails.getParametersContents());
        Matcher tailoringFileMatcher = Pattern.compile("--tailoring-file (([\\w./-])+)")
                .matcher(scapActionDetails.getParametersContents());
        Matcher tailoringIdMatcher = Pattern.compile("--tailoring-id (([\\w.-])+)")
                .matcher(scapActionDetails.getParametersContents());

        String oldParameters = "eval " +
                scapActionDetails.getParametersContents() + " " + scapActionDetails.getPath();
        pillar.put("old_parameters", oldParameters);

        pillar.put("xccdffile", scapActionDetails.getPath());
        if (scapActionDetails.getOvalfiles() != null) {
            pillar.put("ovalfiles", Arrays.stream(scapActionDetails.getOvalfiles().split(","))
                    .map(String::trim).collect(toList()));
        }
        if (profileMatcher.find()) {
            pillar.put("profile", profileMatcher.group(1));
        }
        if (ruleMatcher.find()) {
            pillar.put("rule", ruleMatcher.group(1));
        }
        if (tailoringFileMatcher.find()) {
            pillar.put("tailoring_file", tailoringFileMatcher.group(1));
        }
        if (tailoringIdMatcher.find()) {
            pillar.put("tailoring_id", tailoringIdMatcher.group(1));
        }
        if (scapActionDetails.getParametersContents().contains("--fetch-remote-resources")) {
            pillar.put("fetch_remote_resources", true);
        }
        if (scapActionDetails.getParametersContents().contains("--remediate")) {
            pillar.put("remediate", true);
        }

        ret.put(State.apply(singletonList("scap"),
                        Optional.of(singletonMap("mgr_scap_params", (Object)pillar))),
                minionSummaries);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        Openscap.OpenscapResult openscapResult;
        try {
            TypeToken<Map<String, StateApplyResult<Ret<Openscap.OpenscapResult>>>> typeToken =
                    new TypeToken<>() {
                    };
            Map<String, StateApplyResult<Ret<Openscap.OpenscapResult>>> stateResult = Json.GSON.fromJson(
                    jsonResult, typeToken.getType());
            openscapResult = stateResult.entrySet().stream().findFirst().map(e -> e.getValue().getChanges().getRet())
                    .orElseThrow(() -> new RuntimeException("missing scap result"));
        }
        catch (JsonSyntaxException e) {
            serverAction.setResultMsg("Error parsing minion response: " + jsonResult);
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
            return;
        }
        if (openscapResult.isSuccess()) {
            serverAction.getServer().asMinionServer().ifPresent(
                    minion -> {
                        try {
                            Map<Boolean, String> moveRes = auxArgs.getSaltApi().storeMinionScapFiles(
                                    minion, openscapResult.getUploadDir(), getId());
                            moveRes.entrySet().stream().findFirst().ifPresent(moved -> {
                                if (moved.getKey()) {
                                    Path resultsFile = Paths.get(moved.getValue(),
                                            "results.xml");
                                    try (InputStream resultsFileIn =
                                                 new FileInputStream(
                                                         resultsFile.toFile())) {
                                        ScapManager.xccdfEval(
                                                minion, this,
                                                openscapResult.getReturnCode(),
                                                openscapResult.getError(),
                                                resultsFileIn,
                                                new File(xccdfResumeXsl));
                                        serverAction.setResultMsg("Success");
                                    }
                                    catch (Exception e) {
                                        LOG.error("Error processing SCAP results file {}", resultsFile, e);
                                        serverAction.setStatus(ActionFactory.STATUS_FAILED);
                                        serverAction.setResultMsg(
                                                "Error processing SCAP results file " +
                                                        resultsFile + ": " +
                                                        e.getMessage());
                                    }
                                }
                                else {
                                    serverAction.setStatus(ActionFactory.STATUS_FAILED);
                                    serverAction.setResultMsg(
                                            "Could not store SCAP files on server: " +
                                                    moved.getValue());
                                }
                            });
                        }
                        catch (Exception e) {
                            serverAction.setStatus(ActionFactory.STATUS_FAILED);
                            serverAction.setResultMsg(
                                    "Error saving SCAP result: " + e.getMessage());
                        }
                    });
        }
        else {
            serverAction.setResultMsg(openscapResult.getError());
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
        }
    }

}
