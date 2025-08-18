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

import static com.redhat.rhn.GlobalInstanceHolder.ACCESS_CONTROL_NAMESPACE_TREE_HELPER;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.access.NamespaceFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.access.AccessGroupManager;
import com.redhat.rhn.manager.org.OrgManager;

import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.webui.utils.gson.AccessGroupJson;
import com.suse.manager.webui.utils.gson.AccessGroupUserJson;
import com.suse.manager.webui.utils.gson.NamespaceJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Controller class providing the backend for API calls to work with role based access control.
 */
public class AccessGroupController {

    private static final Gson GSON = new GsonBuilder().create();
    private static final AccessGroupManager MANAGER = GlobalInstanceHolder.ACCESS_GROUP_MANAGER;

    private AccessGroupController() { }

    /**
     * Invoked from Router. Initialize routes for Access Group management.
     */
    public static void initRoutes() {
        get("/manager/api/admin/access-control/access-group/list_custom",
                asJson(withProductAdmin(AccessGroupController::listCustomAccessGroups)));
        get("/manager/api/admin/access-control/access-group/list_namespaces",
                asJson(withProductAdmin(AccessGroupController::listNamespaces)));
        get("/manager/api/admin/access-control/access-group/organizations/:orgId/users",
                asJson(withProductAdmin(AccessGroupController::listOrgUsers)));
        get("/manager/api/admin/access-control/access-group/organizations",
                asJson(withProductAdmin(AccessGroupController::listOrganizations)));
        get("/manager/api/admin/access-control/access-group/organizations/:orgId/access-groups",
                asJson(withProductAdmin(AccessGroupController::listAccessGroups)));
        post("/manager/api/admin/access-control/access-group/save",
                asJson(withProductAdmin(AccessGroupController::save)));
        delete("/manager/api/admin/access-control/access-group/delete/:id",
                asJson(withProductAdmin(AccessGroupController::remove)));
    }

    /**
     * Processes a GET request to get a list of custom access groups
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the result JSON object
     */
    public static String listCustomAccessGroups(Request request, Response response, User user) {
        var roles = MANAGER.listCustom(user.getOrg());
        return json(GSON, response, roles, new TypeToken<>() { });
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
        String copyFromParam = request.queryParams("copyFrom");
        List<Long> copyFromIds = Collections.emptyList();
        if (copyFromParam != null && !copyFromParam.isBlank()) {
            copyFromIds = Arrays.stream(copyFromParam.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();
        }
        var copyFrom = copyFromIds.stream()
            .flatMap(id -> NamespaceFactory.getAccessGroupNamespaces(id).stream())
            .collect(Collectors.toMap(
                NamespaceJson::getId,
                Function.identity(),
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .toList();
        var namespaces = ACCESS_CONTROL_NAMESPACE_TREE_HELPER.buildTree(NamespaceFactory.list());
        var result = Map.of("namespaces", namespaces, "toCopy", copyFrom);
        return json(GSON, response, result, new TypeToken<>() { });
    }

    /**
     * Processes a GET request to get a list of all users of an organization
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the result JSON object
     */
    public static String listOrgUsers(Request request, Response response, User user) {
        Long orgId = Long.parseLong(request.params("orgId"));
        List<AccessGroupUserJson> users = MANAGER.listUsers(orgId);
        return json(GSON, response, users, new TypeToken<>() { });
    }

    /**
     * Processes a GET request to get a list of all organizations
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the result JSON object
     */
    public static String listOrganizations(Request request, Response response, User user) {
        List<OrgInfoJson> organizations = OrgManager.allOrgs(user).stream().map(org ->
                new OrgInfoJson(org.getId(), org.getName())).toList();
        return json(GSON, response, organizations, new TypeToken<>() { });
    }

    /**
     * Processes a GET request to get a list of all access groups for a specific organization
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the result JSON object
     */
    public static String listAccessGroups(Request request, Response response, User user) {
        Long orgId = Long.parseLong(request.params("orgId"));
        var org = OrgFactory.lookupById(orgId);
        if (org == null) {
            Spark.halt(HttpStatus.SC_NOT_FOUND, GSON.toJson(
                    ResultJson.error("Organization with id: " + orgId + " does not exist")));
        }
        var accessGroups = MANAGER.list(org).stream()
                .sorted(Comparator.comparing(AccessGroup::getDescription)).
                map(it -> Map.<String, Object>of(
                        "id", it.getId(),
                        "description", it.getDescription()
                )).toList();

        return json(GSON, response, accessGroups, new TypeToken<>() { });
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
                Org org = OrgFactory.lookupById(json.getOrgId());
                accessGroup = MANAGER.create(json.getName(), json.getDescription(), org, Collections.emptyList());
            }
            else {
                accessGroup = MANAGER.lookupById(json.getId()).orElseThrow();
                accessGroup.setDescription(json.getDescription());
                if (!accessGroup.getLabel().equals(json.getName())) {
                    if (MANAGER.lookup(json.getName(), accessGroup.getOrg()).isPresent()) {
                        throw new EntityExistsException("Access Group: " + json.getName() +
                                " already exists on " + "organization: " + accessGroup.getOrg().getName() + ".");
                    }
                    accessGroup.setLabel(json.getName());
                }
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
        Long id = Long.parseLong(request.params("id"));
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
            List<User> accessGroupUsers = AccessGroupFactory.listAccessGroupUsers(accessGroup.getId());
            // Users that need to be added to the access group
            List<User> toAdd = users.stream().filter(user -> !accessGroupUsers.contains(user)).toList();
            toAdd.forEach(user -> user.addToGroup(accessGroup));
            // users that need to be removed from the access group
            accessGroupUsers.removeAll(users);
            accessGroupUsers.forEach(user -> user.removeFromGroup(accessGroup));
        }
    }
}
