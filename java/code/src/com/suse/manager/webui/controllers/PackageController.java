/*
 * Copyright (c) 2023 SUSE LLC
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.jsonError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.utils.Json;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Provides backend for the packages pages
 */
public class PackageController {

    private PackageController() {
    }

    /**
     * Initialize the spark routes
     *
     * @param jade the jade template
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/packages/list",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(PackageController::listPage)))), jade);
        get("/manager/api/packages/list/:binary/:kind", asJson(withUser(PackageController::data)));
        get("/manager/api/packages/list/:binary/channel/:cid", asJson(withUser(PackageController::channelPackages)));
        post("/manager/api/packages/delete", asJson(withUser(PackageController::delete)));
    }

    private static ModelAndView listPage(Request request, Response response, User user) {
        RhnSet set =  RhnSetDecl.DELETABLE_PACKAGE_LIST.get(user);
        Map<String, Object> data = new HashMap<>();
        data.put("selectedChannel", request.queryParams("selected_channel"));

        String selected = "[]";
        if (request.queryParams("forwarded") != null && !set.isEmpty()) {
            selected = Json.GSON.toJson(set.getElementValues());
        }
        data.put("selected", selected);
        set.clear();
        RhnSetManager.store(set);
        return new ModelAndView(data, "templates/packages/list.jade");
    }

    private static Object data(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request, "server_name");

        boolean source = request.params(":binary").equals("source");
        String kind = request.params(":kind");

        if ("all".equals(kind)) {
            return allPackages(pageHelper, response, user, source);
        }
        else if ("orphans".equals(kind)) {
            return orphanedPackages(pageHelper, response, user, source);
        }
        throw Spark.halt(HttpStatus.SC_NOT_FOUND, "Invalid request");
    }

    private static Object allPackages(PageControlHelper pch, Response response, User user, boolean source) {
        PageControl pc = pch.getPageControl();

        // When getting ids for the select all we just get all package ID matching the filter, no paging
        if ("id".equals(pch.getFunction())) {
            pc.setStart(1);
            pc.setPageSize(0); // Setting to zero means getting them all

            DataResult<PackageOverview> packages = new PagedSqlQueryBuilder("id")
                    .select("id")
                    .from(source ? "rhnPackageSource" : "rhnPackage")
                    .where("org_id = :org_id")
                    .run(Map.of("org_id", user.getOrg().getId()), pc,
                            PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);

            return json(response, packages.stream()
                    .map(PackageOverview::getId)
                    .collect(Collectors.toList()), new TypeToken<>() { });
        }

        DataResult<PackageOverview> packages = PackageManager.listCustomPackages(user.getOrg().getId(), source, pc);

        return json(response, new PagedDataResultJson<>(packages, packages.getTotalSize(), null),
                new TypeToken<>() { });
    }

    private static Object orphanedPackages(PageControlHelper pch, Response response, User user, boolean source) {
        PageControl pc = pch.getPageControl();

        // When getting ids for the select all we just get all package ID matching the filter, no paging
        if ("id".equals(pch.getFunction())) {
            pc.setStart(1);
            pc.setPageSize(0); // Setting to zero means getting them all

            String packageSourceFrom = "rhnPackageSource PS " +
                    "inner join rhnSourceRPM SRPM on PS.source_rpm_id = SRPM.id " +
                    "left join rhnPackage P on SRPM.id = P.source_rpm_id " +
                    "left join rhnChannelPackage CP on CP.package_id = P.id";

            String packageFrom = "rhnPackage P left join rhnChannelPackage CP on CP.package_id = P.id";

            DataResult<PackageOverview> packages = new PagedSqlQueryBuilder("id")
                    .select("id")
                    .from(source ? packageSourceFrom : packageFrom)
                    .where("org_id = :org_id AND CP.package_id is null")
                    .run(Map.of("org_id", user.getOrg().getId()), pc,
                            PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);

            return json(response, packages.stream()
                    .map(PackageOverview::getId)
                    .collect(Collectors.toList()), new TypeToken<>() { });
        }

        DataResult<PackageOverview> packages = PackageManager.listOrphanPackages(user.getOrg().getId(), source, pc);

        return json(response, new PagedDataResultJson<>(packages, packages.getTotalSize(), null),
                new TypeToken<>() { });
    }

    private static Object channelPackages(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request, "server_name");
        PageControl pc = pageHelper.getPageControl();

        boolean source = request.params(":binary").equals("source");

        try {
            Long cid = Long.parseLong(request.params(":cid"));

            // When getting ids for the select all we just get all package ID matching the filter, no paging
            if ("id".equals(pageHelper.getFunction())) {
                pc.setStart(1);
                pc.setPageSize(0); // Setting to zero means getting them all

                String packageSourceFrom = "rhnPackageSource PS " +
                        "inner join rhnSourceRPM SRPM on PS.source_rpm_id = SRPM.id " +
                        "left join rhnPackage P on SRPM.id = P.source_rpm_id " +
                        "left join rhnChannelPackage CP on CP.package_id = P.id";

                String packageFrom = "rhnPackage P left join rhnChannelPackage CP on CP.package_id = P.id";

                DataResult<PackageOverview> packages = new PagedSqlQueryBuilder("id")
                        .select("id")
                        .from(source ? packageSourceFrom : packageFrom)
                        .where("org_id = :org_id AND CP.channel_id = :cid")
                        .run(Map.of("org_id", user.getOrg().getId(), "cid", cid), pc,
                                PagedSqlQueryBuilder::parseFilterAsText, PackageOverview.class);

                return json(response, packages.stream()
                        .map(PackageOverview::getId)
                        .collect(Collectors.toList()), new TypeToken<>() { });
            }

            DataResult<PackageOverview> packages = PackageManager.listCustomPackageForChannel(
                    cid, user.getOrg().getId(), source, pc);

            return json(response, new PagedDataResultJson<>(packages, packages.getTotalSize(), null),
                    new TypeToken<>() { });
        }
        catch (NumberFormatException e) {
            throw Spark.halt(HttpStatus.SC_NOT_FOUND, "Invalid channel id: " + request.params("id"));
        }
    }

    private static Object delete(Request request, Response response, User user) {
        Set<Long> ids = new GsonBuilder().create().fromJson(request.body(), new TypeToken<Set<Long>>() { }.getType());

        try {
            PackageManager.deletePackages(ids, user);
        }
        catch (PermissionException e) {
            return jsonError(response, HttpStatus.SC_FORBIDDEN, e.getLocalizedMessage());
        }
        catch (RhnRuntimeException e) {
            return jsonError(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }

        return json(response, "success");
    }
}
