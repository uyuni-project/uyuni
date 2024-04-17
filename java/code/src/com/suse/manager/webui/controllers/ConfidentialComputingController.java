/*
 * Copyright (c) 2024 SUSE LLC
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.CoCoAttestationReportJson;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

public class ConfidentialComputingController {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    private final AttestationManager attestationManager;

    /**
     * Default constructor
     * @param attestationManagerIn the attestation manager
     */
    public ConfidentialComputingController(AttestationManager attestationManagerIn) {
        this.attestationManager = attestationManagerIn;
    }

    /**
     * Initialize routes for Confidential Computing.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/audit/confidential-computing",
            withUserPreferences(withCsrfToken(withUser(this::show))), jade);

        get("/manager/api/audit/confidential-computing/listAttestations",
            asJson(withUser(this::listAllAttestations)));
    }

    /**
     * Displays the confidential computing global reports page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView show(Request request, Response response, User user) {
        return new ModelAndView(new HashMap<>(), "templates/audit/confidential-computing.jade");
    }

    /**
     * List all the attestation reports available for this user
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public String listAllAttestations(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request);
        PageControl pc = pageHelper.getPageControl();

        long totalSize = attestationManager.countCoCoAttestationReports(user);

        List<CoCoAttestationReportJson> reportsJson = attestationManager.listCoCoAttestationReports(user, pc)
            .stream()
            .map(CoCoAttestationReportJson::new)
            .collect(Collectors.toList());

        return json(GSON, response, new PagedDataResultJson<>(reportsJson, totalSize, Collections.emptySet()),
            new TypeToken<>() { });
    }
}
