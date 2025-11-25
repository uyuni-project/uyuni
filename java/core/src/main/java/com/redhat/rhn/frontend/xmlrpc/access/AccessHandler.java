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
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.frontend.xmlrpc.access;

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.access.NamespaceFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.RoleCannotBeAlteredException;
import com.redhat.rhn.manager.access.AccessGroupManager;
import com.redhat.rhn.manager.access.DefaultRoleException;

import com.suse.manager.api.ReadOnly;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RBAC XML-RPC handler
 * @apidoc.namespace access
 * @apidoc.doc Provides methods to manage Role-Based Access Control
 */
public class AccessHandler extends BaseHandler {

    private final AccessGroupManager manager;

    /**
     * Instantiate a new XML-RPC handler
     * @param accessGroupManager the access group manager to use
     */
    public AccessHandler(AccessGroupManager accessGroupManager) {
        manager = accessGroupManager;
    }

    /**
     * Create a new role.
     * @param loggedInUser the current user
     * @param label the unique label of the new role
     * @param description the description of the new role
     * @param permissionsFrom the list of roles to inherit permissions from
     * @return the newly created role
     *
     * @apidoc.doc Create a new role.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "the unique label of the new role")
     * @apidoc.param #param_desc("string", "description", "the description of the new role")
     * @apidoc.param #array_single_desc("string", "permissionsFrom", "the list of roles to inherit permissions from")
     * @apidoc.returntype $AccessGroupSerializer
     */
    public AccessGroup createRole(User loggedInUser, String label, String description, List<String> permissionsFrom) {
        try {
            Set<AccessGroup> groups = permissionsFrom.stream()
                    .map(group -> manager.lookup(group, loggedInUser.getOrg())
                            .orElseThrow(() -> new EntityNotExistsFaultException(group)))
                    .collect(Collectors.toUnmodifiableSet());

            return manager.create(label, description, loggedInUser.getOrg(), groups);
        }
        catch (DefaultRoleException e) {
            throw new RoleCannotBeAlteredException(e);
        }
        catch (NoSuchElementException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Create a new role.
     * @param loggedInUser the current user
     * @param label the unique label of the new role
     * @param description the description of the new role
     * @return the newly created role
     *
     * @apidoc.doc Create a new role.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "the unique label of the new role")
     * @apidoc.param #param_desc("string", "description", "the description of the new role")
     * @apidoc.returntype $AccessGroupSerializer
     */
    public AccessGroup createRole(User loggedInUser, String label, String description) {
        return createRole(loggedInUser, label, description, Collections.emptyList());
    }

    /**
     * List existing roles.
     * @param loggedInUser the current user
     * @return the list of existing roles
     *
     * @apidoc.doc List existing roles.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *     $AccessGroupSerializer
     * #array_end()
     */
    @ReadOnly
    public List<AccessGroup> listRoles(User loggedInUser) {
        return manager.list(loggedInUser.getOrg());
    }

    /**
     * List available namespaces.
     * @param loggedInUser the current user
     * @return the list of all available namespaces
     *
     * @apidoc.doc List available namespaces.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *     $NamespaceSerializer
     * #array_end()
     */
    @ReadOnly
    public List<Namespace> listNamespaces(User loggedInUser) {
        return NamespaceFactory.list();
    }

    /**
     * Delete a role.
     * @param loggedInUser the current user
     * @param label the unique label of the role
     * @return 1 if successful
     *
     * @apidoc.doc Delete a role.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "the unique label of the new role")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteRole(User loggedInUser, String label) {
        try {
            manager.remove(label, loggedInUser.getOrg());
        }
        catch (NoSuchElementException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (IllegalArgumentException e) {
            throw new RoleCannotBeAlteredException(e);
        }

        return 1;
    }

    /**
     * List permissions granted by a role.
     * @param loggedInUser the current user
     * @param label the unique label of the role
     * @return the set of permissions granted by the role
     *
     * @apidoc.doc List permissions granted by a role.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "the unique label of the role")
     * @apidoc.returntype
     * #return_array_begin()
     *     $NamespaceSerializer
     * #array_end()
     */
    @ReadOnly
    public Set<Namespace> listPermissions(User loggedInUser, String label) {
        AccessGroup group = manager.lookup(label, loggedInUser.getOrg())
                .orElseThrow(() -> new EntityNotExistsFaultException(label));

        return group.getNamespaces();
    }

    /**
     * Grant full access to the given namespaces for the specified role.
     * @param loggedInUser the current user
     * @param label the unique label of the role
     * @param namespaces the list of namespaces to grant access to
     * @return 1 if successful
     *
     * @apidoc.doc Grant full access to the given namespace for the specified role.
     * Returns the expanded list of namespaces granted by the call.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "the unique label of the role")
     * @apidoc.param #array_single_desc("string", "namespaces", "the list of namespaces to grant access to")
     * @apidoc.returntype #return_int_success()
     */
    public int grantAccess(User loggedInUser, String label, List<String> namespaces) {
        try {
            manager.grantAccess(label, loggedInUser.getOrg(), namespaces);
            return 1;
        }
        catch (DefaultRoleException e) {
            throw new RoleCannotBeAlteredException(e);
        }
    }

    /**
     * Grant access to the given namespaces for the specified role.
     * @param loggedInUser the current user
     * @param label the unique label of the role
     * @param namespaces the list of namespaces to grant access to
     * @param modes the access modes to be granted
     * @return 1 if successful
     *
     * @apidoc.doc Grant access to the given namespace for the specified role.
     * Returns the expanded list of namespaces granted by the call.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "the unique label of the role")
     * @apidoc.param #array_single_desc("string", "namespaces", "the list of namespaces to grant access to")
     * @apidoc.param #array_single_desc("string", "modes", "the access modes (R for read/view, W for write/modify)")
     * @apidoc.returntype #return_int_success()
     */
    public int grantAccess(User loggedInUser, String label, List<String> namespaces, List<String> modes) {
        Set<Namespace.AccessMode> accessModes = modes.stream().map(Namespace.AccessMode::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        try {
            manager.grantAccess(label, loggedInUser.getOrg(), namespaces, accessModes);
            return 1;
        }
        catch (DefaultRoleException e) {
            throw new RoleCannotBeAlteredException(e);
        }
    }

    /**
     * Revoke access to the given namespaces for the specified role.
     * @param loggedInUser the current user
     * @param label the unique label of the role
     * @param namespaces the list of namespaces to grant access to
     * @return 1 if successful
     *
     * @apidoc.doc Revoke access to the given namespace for the specified role.
     * Returns the expanded list of namespaces revoked by the call.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "the unique label of the role")
     * @apidoc.param #array_single_desc("string", "namespaces", "the list of namespaces to revoke access to")
     * @apidoc.returntype #return_int_success()
     */
    public int revokeAccess(User loggedInUser, String label, List<String> namespaces) {
        try {
            manager.revokeAccess(label, loggedInUser.getOrg(), namespaces);
            return 1;
        }
        catch (DefaultRoleException e) {
            throw new RoleCannotBeAlteredException(e);
        }
    }

    /**
     * Revoke access to the given namespaces for the specified role.
     * @param loggedInUser the current user
     * @param label the unique label of the role
     * @param namespaces the list of namespaces to grant access to
     * @param modes the access modes to be granted
     * @return 1 if successful
     *
     * @apidoc.doc Revoke access to the given namespace for the specified role.
     * Returns the expanded list of namespaces revoked by the call.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "the unique label of the role")
     * @apidoc.param #array_single_desc("string", "namespaces", "the list of namespaces to revoke access to")
     * @apidoc.param #array_single_desc("string", "modes", "the access modes (R for read/view, W for write/modify)")
     * @apidoc.returntype #return_int_success()
     */
    public int revokeAccess(User loggedInUser, String label, List<String> namespaces, List<String> modes) {
        Set<Namespace.AccessMode> accessModes = modes.stream().map(Namespace.AccessMode::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        try {
            manager.revokeAccess(label, loggedInUser.getOrg(), namespaces, accessModes);
            return 1;
        }
        catch (DefaultRoleException e) {
            throw new RoleCannotBeAlteredException(e);
        }
    }
}
