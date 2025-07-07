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
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import org.apache.commons.text.StringEscapeUtils;

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
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> scapXccdfEvalAction(
            List<MinionSummary> minionSummaries, ScapAction action) {

        ScapActionDetails details = action.getScapActionDetails();

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        Map<String, Object> pillar = new HashMap<>();
        Matcher profileMatcher = Pattern.compile("--profile (([\\w.-])+)")
                .matcher(details.getParametersContents());
        Matcher ruleMatcher = Pattern.compile("--rule (([\\w.-])+)")
                .matcher(details.getParametersContents());
        Matcher tailoringFileMatcher = Pattern.compile("--tailoring-file (([\\w./-])+)")
                .matcher(details.getParametersContents());
        Matcher tailoringIdMatcher = Pattern.compile("--tailoring-id (([\\w.-])+)")
                .matcher(details.getParametersContents());

        String oldParameters = "eval " +
                details.getParametersContents() + " " + details.getPath();
        pillar.put("old_parameters", oldParameters);

        pillar.put("xccdffile", details.getPath());
        if (details.getOvalfiles() != null) {
            pillar.put("ovalfiles", Arrays.stream(details.getOvalfiles().split(","))
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
        if (details.getParametersContents().contains("--fetch-remote-resources")) {
            pillar.put("fetch_remote_resources", true);
        }
        if (details.getParametersContents().contains("--remediate")) {
            pillar.put("remediate", true);
        }

        ret.put(State.apply(singletonList("scap"),
                        Optional.of(singletonMap("mgr_scap_params", (Object)pillar))),
                minionSummaries);
        return ret;
    }
}
