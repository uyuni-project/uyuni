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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.audit.CVEAuditImage;
import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;
import com.redhat.rhn.manager.audit.CVEAuditServer;
import com.redhat.rhn.manager.audit.CVEAuditSystem;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.SystemCVEAuditManager;
import com.redhat.rhn.manager.audit.SystemCVEResult;
import com.redhat.rhn.manager.audit.UnknownCVEIdentifierException;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import com.suse.manager.utils.EnumNameParser;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.SystemCVEJson;
import com.suse.oval.OvalSeverity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
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

        get("/manager/systems/details/audit/cve",
                withCsrfToken(withDocsLocale(withUserAndServer(CVEAuditController::systemCveView))), jade);
        get("/manager/api/systems/:sid/details/audit/cve/:statuses/:severities",
                asJson(withUserAndServer(CVEAuditController::systemCves)));
    }

    /**
     * Returns the system CVE list page
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @param server the server
     * @return the model and view
     */
    public static ModelAndView systemCveView(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("ovalState", SystemCVEAuditManager.getOvalState(server).name());
        return new ModelAndView(data, "templates/minion/system-cve.jade");
    }

    /**
     * Returns a page of the CVEs affecting the system, as paged JSON.
     * Path param 'statuses' is a comma separated list of {@link PatchStatus} names ('none' for no status).
     * Path param 'severities' is a comma separated list of {@link OvalSeverity} names ('all' for any severity).
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @param server the server
     * @return the JSON page
     */
    public static String systemCves(Request request, Response response, User user, Server server) {
        Set<PatchStatus> statuses = parseCommaSeparated(request.params("statuses"), PatchStatus.class);
        Set<OvalSeverity> severities = parseCommaSeparated(request.params("severities"), OvalSeverity.class);
        PageControlHelper pageHelper = new PageControlHelper(request, "cveName");
        PageControl pc = pageHelper.getPageControl();

        DataResult<SystemCVEResult> cves = SystemCVEAuditManager.listCves(server, statuses, severities, pc);
        List<SystemCVEJson> items = cves.stream().map(SystemCVEJson::new).collect(Collectors.toList());

        return json(response, new PagedDataResultJson<>(items, cves.getTotalSize(), Collections.emptySet()),
                new TypeToken<>() { });
    }

    private static <E extends Enum<E>> Set<E> parseCommaSeparated(String value, Class<E> type) {
        List<String> names = value == null ? null : Arrays.stream(value.split(",")).map(String::trim).toList();
        return EnumNameParser.parse(names, type,
                e -> log.debug("Ignoring unknown {} value: {}", type.getSimpleName(), e.getMessage()));
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

        public Set<PatchStatus> getStatuses() {
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
                    return result(res, ResultJson.success(cveAuditServers), new TypeToken<>() { });
                case IMAGE:
                    List<CVEAuditImage> cveAuditImages = CVEAuditManagerOVAL
                    .listImagesByPatchStatus(user, cveAuditRequest.cveIdentifier,
                            cveAuditRequest.statuses);
                    return result(res, ResultJson.success(cveAuditImages), new TypeToken<>() { });
                    default: throw new RuntimeException("unreachable");
            }
        }
        catch (UnknownCVEIdentifierException e) {
            return result(res, ResultJson.error(LOC.getMessage("cveaudit.notfound")), new TypeToken<>() { });
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
