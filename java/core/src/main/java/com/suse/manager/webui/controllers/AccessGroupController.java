/*
 * Copyright (c) 2025 SUSE LLC
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static spark.Spark.get;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.access.AccessGroupManager;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.AccessGroupJson;
import com.suse.manager.webui.utils.gson.AccessGroupUserJson;
import com.suse.manager.webui.utils.gson.NamespaceJson;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

import spark.Request;
import spark.Response;

/**
 * Controller class providing the backend for API calls to work with role based access control.
 */
public class AccessGroupController {

    private static final Logger LOG = LogManager.getLogger(AccessGroupController.class);
    private static final Gson GSON = new GsonBuilder().create();
    private static final AccessGroupManager MANAGER = GlobalInstanceHolder.ACCESS_GROUP_MANAGER;

    private AccessGroupController() { }

    /**
     * Invoked from Router. Initialize routes for Access Group management.
     */
    public static void initRoutes() {
        get("/manager/api/admin/access-group/roles", withProductAdmin(AccessGroupController::listRoles));
        get("/manager/api/admin/access-group/namespaces", withProductAdmin(AccessGroupController::listNamespaces));
        get("/manager/api/admin/access-group/users", withProductAdmin(AccessGroupController::listAccessGroupUsers));
    }

    /**
     * Processes a GET request to get a paginated list of access groups
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the result JSON object
     */
    public static String listRoles(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request, "name");
        PageControl pc = pageHelper.getPageControl();
        DataResult<AccessGroupJson> roles = MANAGER.list(pc, PagedSqlQueryBuilder::parseFilterAsText);
        TypeToken<PagedDataResultJson<AccessGroupJson, Long>> type = new TypeToken<>() { };
        return json(GSON, response, new PagedDataResultJson<>(roles, roles.getTotalSize(),
                Collections.emptySet()), type);
    }

    /**
     * Processes a GET request to get a paginated list of namespaces
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the result JSON object
     */
    public static String listNamespaces(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request, "namespace");
        PageControl pc = pageHelper.getPageControl();
        DataResult<NamespaceJson> namespaces = MANAGER.listNamespaces(pc, PagedSqlQueryBuilder::parseFilterAsText);
        TypeToken<PagedDataResultJson<NamespaceJson, Long>> type = new TypeToken<>() { };
        return json(GSON, response, new PagedDataResultJson<>(namespaces, namespaces.getTotalSize(),
                Collections.emptySet()), type);
    }

    /**
     * Processes a GET request to get a list of all users
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the result JSON object
     */
    public static String listAccessGroupUsers(Request request, Response response, User user) {
        List<AccessGroupUserJson> users = MANAGER.listUsers();
        return json(GSON, response, users, new TypeToken<>() { });
    }
}
