/**
 * Copyright (c) 2016 SUSE LLC
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

import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.FlashScopeHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Controller class for the formula catalog page.
 */
public class FormulaCatalogController {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private FormulaCatalogController() { }

    /**
     * Show the list of formulas.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the page that will show the list of formulas
     */
    public static ModelAndView list(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("info", FlashScopeHelper.flash(request));
        return new ModelAndView(data, "templates/formula_catalog/list.jade");
    }

    /**
     * Return the JSON data to render the formula list page.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String data(Request request, Response response, User user) {
        response.type("application/json");
        return GSON.toJson(FormulaFactory.listFormulaNames());
    }

    /**
     * Show the details page for an existing formula.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the details page
     */
    public static ModelAndView details(Request request, Response response, User user) {
        String formulaName = request.params("name");

        if (!FormulaFactory.listFormulaNames().contains(formulaName)) {
            throw new NotFoundException();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("formulaName", formulaName);
        return new ModelAndView(data, "templates/formula_catalog/formula.jade");
    }

    /**
     * Return the JSON data to render the details for a formula.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String detailsData(Request request, Response response, User user) {
        String formulaName = request.params("name");

        response.type("application/json");
        return GSON.toJson(FormulaFactory.getMetadata(formulaName));
    }
}
