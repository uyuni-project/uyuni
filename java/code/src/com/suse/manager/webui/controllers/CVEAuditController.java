/*
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.audit.CVEAuditImage;
import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;
import com.redhat.rhn.manager.audit.CVEAuditServer;
import com.redhat.rhn.manager.audit.CVEAuditSystem;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.UnknownCVEIdentifierException;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class the CVE Audit page.
 */
public class CVEAuditController {
    private static final LocalizationService LOC = LocalizationService.getInstance();

    private static final Gson GSON = new GsonBuilder().create();

    private static Logger log = LogManager.getLogger(CVEAuditController.class);

    private CVEAuditController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/audit/cve",
                withUserPreferences(withCsrfToken(withUser(CVEAuditController::cveAuditView))), jade);

        post("/manager/api/audit/cve", withUser(CVEAuditController::cveAudit));
        get("/manager/api/audit/cve.csv", withUser(CVEAuditController::cveAuditCSV));
    }

    /**
     * Returns the cve audit page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView cveAuditView(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView(model, "templates/audit/cve.jade");
    }

    private enum AuditTarget {
        IMAGE,
        SERVER
    }

    /**
     * CVEAuditRequest
     */
    static class CVEAuditRequest {

        private final String cveIdentifier;
        private final AuditTarget target;
        private final EnumSet<PatchStatus> statuses;

        CVEAuditRequest(String cveIdentifierIn, EnumSet<PatchStatus> statusesIn,
                AuditTarget targetIn) {
            this.cveIdentifier = cveIdentifierIn;
            this.statuses = statusesIn;
            this.target = targetIn;
        }

        public EnumSet<PatchStatus> getStatuses() {
            return statuses;
        }

        public String getCveIdentifier() {
            return cveIdentifier;
        }

        public AuditTarget getTarget() {
            return target;
        }
    }

    /**
     * Gets a JSON list of Container Build Host entitled systems
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object cveAudit(Request req, Response res, User user) {
        CVEAuditRequest cveAuditRequest = GSON.fromJson(req.body(), CVEAuditRequest.class);
        try {

            switch (cveAuditRequest.getTarget()) {
                case SERVER:
                    Set<Long> systemSet = RhnSetDecl.SYSTEMS.get(user).getElementValues();
                    List<CVEAuditServer> cveAuditServers = CVEAuditManagerOVAL
                    .listSystemsByPatchStatus(user, cveAuditRequest.cveIdentifier,
                            cveAuditRequest.statuses);
                    cveAuditServers.forEach(serv -> serv.setSelected(systemSet.contains(serv.getId())));
                    return json(res, ResultJson.success(cveAuditServers));
                case IMAGE:
                    List<CVEAuditImage> cveAuditImages = CVEAuditManagerOVAL
                    .listImagesByPatchStatus(user, cveAuditRequest.cveIdentifier,
                            cveAuditRequest.statuses);
                    return json(res, ResultJson.success(cveAuditImages));
                    default: throw new RuntimeException("unreachable");
            }
        }
        catch (UnknownCVEIdentifierException e) {
            return json(res, ResultJson.error(LOC.getMessage("cveaudit.notfound")));
        }
    }

    private static List<CVEAuditSystem> handleRequest(CVEAuditRequest request, User user)
            throws UnknownCVEIdentifierException {
        switch (request.getTarget()) {
        case SERVER:
            List<CVEAuditServer> cveAuditServers = CVEAuditManagerOVAL
            .listSystemsByPatchStatus(user, request.cveIdentifier,
                    request.statuses);
            return cveAuditServers.stream().map(x -> (CVEAuditSystem)x)
                    .collect(Collectors.toList());
        case IMAGE:
            List<CVEAuditImage> cveAuditImages = CVEAuditManagerOVAL
            .listImagesByPatchStatus(user, request.cveIdentifier,
                    request.statuses);
            return cveAuditImages.stream().map(x -> (CVEAuditSystem)x)
                    .collect(Collectors.toList());
        default: throw new RuntimeException("unreachable");
        }
    }

    /**
     * Return CSV report
     * @param req the request
     * @param res the response
     * @param user the user
     * @return CSV report
     */
    public static Object cveAuditCSV(Request req, Response res, User user) {
        String cveIdentifier = req.queryParams("cveIdentifier");
        AuditTarget target = AuditTarget.valueOf(req.queryParams("target"));
        List<PatchStatus> psList = Stream.of(
                req.queryParams("statuses").split(","))
                .flatMap(ps -> {
                    try {
                        return Stream.of(PatchStatus.valueOf(ps));
                    }
                    catch (IllegalArgumentException e) {
                        return Stream.empty();
                    }
                }).collect(Collectors.toList());
        List<CVEAuditSystem> cveAuditSystems = Collections.emptyList();
        if (!psList.isEmpty()) {
            EnumSet<PatchStatus> statuses = EnumSet.copyOf(psList);
            CVEAuditRequest cveAuditRequest = new CVEAuditRequest(cveIdentifier, statuses,
                    target);
            try {
                cveAuditSystems = handleRequest(cveAuditRequest, user);
            }
            catch (UnknownCVEIdentifierException e) {
                log.warn("Unknown CVE Identifier '{}'", cveIdentifier);
            }
        }
        String result = cveAuditSystems.stream().map(
                system -> "" + system.getPatchStatus() + "," + system.getName() + "," +
                        system.getPatchAdvisory() + "," + system.getChannelName()
        ).collect(Collectors.joining("\n",
                "Patch Status,System Name,Patch Advisory,Channel Name\n", ""));
        res.header("Content-Disposition", "attachment; filename=\"" +
                cveIdentifier + ".csv\"");
        res.raw().setContentType("application/csv");
        return result;
    }
}
