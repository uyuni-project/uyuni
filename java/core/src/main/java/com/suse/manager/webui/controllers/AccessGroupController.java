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

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.access.NamespaceFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.access.AccessGroupManager;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.AccessGroupJson;
import com.suse.manager.webui.utils.gson.AccessGroupUserJson;
import com.suse.manager.webui.utils.gson.NamespaceJson;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;
import spark.Spark;

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
        get("/manager/api/admin/access-group/roles",
                asJson(withProductAdmin(AccessGroupController::listRoles)));
        get("/manager/api/admin/access-group/namespaces",
                asJson(withProductAdmin(AccessGroupController::listNamespaces)));
        get("/manager/api/admin/access-group/users",
                asJson(withProductAdmin(AccessGroupController::listAccessGroupUsers)));
        post("/manager/api/admin/access-group/save",
                asJson(withProductAdmin(AccessGroupController::save)));
        delete("/manager/api/admin/access-group/delete/:id",
                asJson(withProductAdmin(AccessGroupController::remove)));
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

    /**
     * Processes a POST request to create or update access groups
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the JSON response
     */
    public static String save(Request request, Response response, User user) {
        AccessGroupJson json = GSON.fromJson(request.body(), AccessGroupJson.class);
        if (json.getName() == null || json.getName().isEmpty()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST,
                    GSON.toJson(ResultJson.error("Access group name is required")));
        }
        if (json.getDescription() == null || json.getDescription().isEmpty()) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST,
                    GSON.toJson(ResultJson.error("Access group description is required")));
        }
            try {
                AccessGroup accessGroup;
                if (json.getId() == null) {
                    Set<AccessGroup> groups = json.getAccessGroups().stream()
                            .map(group -> MANAGER.lookup(group, user.getOrg())
                                    .orElseThrow(() -> new EntityNotExistsException(group)))
                            .collect(Collectors.toUnmodifiableSet());
                    accessGroup = MANAGER.create(json.getName(), json.getDescription(), user.getOrg(), groups);
                }
                else {
                    accessGroup = MANAGER.lookupById(json.getId()).orElseThrow();
                }
                MANAGER.setAccess(accessGroup, getNamespacesFromPermissions(json.getPermissions()));
                handleAccessGroupUsers(json, accessGroup);
            }
            catch (Exception e) {
                Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(e.getMessage())));
            }
        return json(GSON, response, ResultJson.success(), new TypeToken<>() { });
    }

    /**
     * Processes a DELETE request to delete an access group
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the JSON response
     */
    public static String remove(Request request, Response response, User user) {
        long id = Long.parseLong(request.params("id"));
        try {
            MANAGER.remove(id);
        }
        catch (NoSuchElementException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(
                    ResultJson.error("Access Group with id: " + id + " does not exists")));
        }
        return json(GSON, response, ResultJson.success(), new TypeToken<>() { });
    }

    private static Set<Namespace> getNamespacesFromPermissions(List<NamespaceJson> permissions) {
        Map<Boolean, List<NamespaceJson>> permissionsMap = permissions.stream().collect(
                Collectors.partitioningBy(permission ->
                        !permission.getAccessMode().equals("RW") || permission.getAccessMode().equals("WR"))
        );
        Set<Namespace> namespaces = new HashSet<>(NamespaceFactory.listByIds(permissionsMap.get(true)
                .stream().map(NamespaceJson::getId).collect(Collectors.toList())));
        // Permissions with RW need special treatment
        permissionsMap.get(false).forEach(p -> {
            if (p.getView() && !p.getModify()) {
                namespaces.addAll(NamespaceFactory.find(p.getNamespace(),
                        Set.of(Namespace.AccessMode.R)));
            }
            else if (p.getModify() && !p.getView()) {
                namespaces.addAll(NamespaceFactory.find(p.getNamespace(),
                        Set.of(Namespace.AccessMode.W)));
            }
            else if (p.getView() && p.getModify()) {
                namespaces.addAll(NamespaceFactory.find(p.getNamespace(),
                        Set.of(Namespace.AccessMode.R, Namespace.AccessMode.W)));
            }
        });
        return namespaces;
    }

    private static void handleAccessGroupUsers(AccessGroupJson json, AccessGroup accessGroup) {
        List<User> users = UserFactory.lookupByIds(json.getUsers().stream().map(AccessGroupUserJson::getId).toList());
        if (json.getId() == null) {
            users.forEach(user -> user.addToGroup(accessGroup));
        }
        else {
            List<User> accessGroupUsers = AccessGroupFactory.listAccessGroupUsers(accessGroup);
            // Users that need to be added to the access group
            List<User> toAdd = users.stream().filter(user -> !accessGroupUsers.contains(user)).toList();
            toAdd.forEach(user -> user.addToGroup(accessGroup));
            // users that need to be removed from the access group
            accessGroupUsers.removeAll(users);
            accessGroupUsers.forEach(user -> user.removeFromGroup(accessGroup));
        }
    }
}
