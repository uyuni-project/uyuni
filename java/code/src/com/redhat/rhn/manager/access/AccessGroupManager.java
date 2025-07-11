/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.manager.access;

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static com.redhat.rhn.domain.role.RoleFactory.SAT_ADMIN;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.access.NamespaceFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.gson.AccessGroupJson;
import com.suse.manager.webui.utils.gson.AccessGroupUserJson;
import com.suse.manager.webui.utils.gson.NamespaceJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Class to manage RBAC access groups
 */
public class AccessGroupManager {

    private static final Logger LOG = LogManager.getLogger(AccessGroupManager.class);

    /**
     * Creates a new access group with the given parameters.
     * @param label the label of the group
     * @param description the description of the group
     * @param org the org to which the group belongs
     * @return the created {@code AccessGroup}
     */
    public AccessGroup create(String label, String description, Org org) throws DefaultRoleException {
        return this.create(label, description, org, Collections.emptyList());
    }

    /**
     * Creates a new access group with the given parameters.
     * @param label the label of the group
     * @param description the description of the group
     * @param org the org to which the group belongs
     * @param copyFrom a collection of {@code AccessGroup}s to copy permissions from
     * @return the created {@code AccessGroup}
     */
    public AccessGroup create(String label, String description, Org org, Collection<AccessGroup> copyFrom)
            throws DefaultRoleException {
        if (ORG_ADMIN.getLabel().equals(label) || SAT_ADMIN.getLabel().equals(label) ||
                lookup(label, null).isPresent()) {
            throw new DefaultRoleException(label + " already exists.");
        }
        AccessGroup group = new AccessGroup(label, description, org);
        for (AccessGroup parent : copyFrom) {
            addPermissionsFromRole(group, parent);
            LOG.debug("Copying permissions from {}.", parent.getLabel());
        }

        LOG.info("Access group {} created.", label);
        AccessGroupFactory.save(group);
        return group;
    }

    /**
     * Removes an access group with the given label and org.
     * @param label the label of the access group to remove
     * @param org the org to which the access group belongs
     * @throws java.util.NoSuchElementException if the {@code AccessGroup} is not found.
     */
    public void remove(String label, Org org) {
        AccessGroup group = lookup(label, org).orElseThrow();
        if (group.getOrg() == null) {
            throw new IllegalArgumentException("Default groups cannot be altered.");
        }
        LOG.info("Access group {} removed.", label);
        AccessGroupFactory.remove(group);
    }

    /**
     * Lists all access groups defined in MLM.
     * @return a list of access groups
     */
    public List<AccessGroup> list() {
        return AccessGroupFactory.listAll();
    }

    /**
     * Lists s paginated list of access groups
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the list of access groups
     */
    public DataResult<AccessGroupJson> list(
            PageControl pc, Function<Optional<PageControl>, PagedSqlQueryBuilder.FilterWithValue> parser) {
        return AccessGroupFactory.listAll(pc, parser);
    }

    /**
     * Lists s paginated list of namespaces
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the list of access groups
     */
    public DataResult<NamespaceJson> listNamespaces(
            PageControl pc, Function<Optional<PageControl>, PagedSqlQueryBuilder.FilterWithValue> parser) {
        return NamespaceFactory.list(pc, parser);
    }

    /**
     * Lists all the users
     * @return the list of all users
     */
    public List<AccessGroupUserJson> listUsers() {
        return AccessGroupFactory.listUsers();
    }

    /**
     * Lists all access groups that are available to the given org.
     * @param org the org to list access groups from
     * @return a list of access groups
     */
    public List<AccessGroup> list(Org org) {
        return AccessGroupFactory.list(org);
    }

    /**
     * Looks up an access group with the given label and org
     * @param label the label of the group to look up
     * @param org the org to which the group belongs
     * @return an {@code Optional} containing the access group, or an empty {@code Optional} if not found
     */
    public Optional<AccessGroup> lookup(String label, Org org) {
        return AccessGroupFactory.lookupByLabelAndOrg(label, org);
    }

    /**
     * Grants access to the given namespaces for the specified access group.
     * <p>
     * The asterisk (*) character can be used as a wildcard in namespace strings.
     * @param label the label of the group
     * @param org the org to which the group belongs
     * @param namespaces a list of namespace strings to grant access to
     * @throws DefaultRoleException if trying to revoke access for a default access group.
     */
    public void grantAccess(String label, Org org, List<String> namespaces) throws DefaultRoleException {
        grantAccess(label, org, namespaces, Set.of(Namespace.AccessMode.R, Namespace.AccessMode.W));
    }

    /**
     * Grants access to the given namespaces with the specified access modes for the access group.
     * <p>
     * The asterisk (*) character can be used as a wildcard in namespace strings.
     * @param label the label of the group
     * @param org the org to which the group belongs
     * @param namespaces a list of namespace strings to grant access to
     * @param modes a set of {@link com.redhat.rhn.domain.access.Namespace.AccessMode}s
     * @throws DefaultRoleException if trying to revoke access for a default access group.
     */
    public void grantAccess(String label, Org org, List<String> namespaces, Set<Namespace.AccessMode> modes)
            throws DefaultRoleException {
        AccessGroup group = lookup(label, org).orElseThrow();
        for (String ns : namespaces) {
            grantAccess(group, ns, modes);
            LOG.debug("Access group {} is granted access to namespace {}.", label, ns);
        }
        LOG.info("Access group {} is granted access to {} namespace(s).", label, namespaces.size());
    }

    /**
     * Revokes access to the given namespaces for the specified access group.
     * @param label the label of the group
     * @param org the org to which the group belongs
     * @param namespaces a list of namespace strings to revoke access from
     * @throws DefaultRoleException if trying to revoke access for a default access group.
     */
    public void revokeAccess(String label, Org org, List<String> namespaces) throws DefaultRoleException {
        revokeAccess(label, org, namespaces, Set.of(Namespace.AccessMode.R, Namespace.AccessMode.W));
    }

    /**
     * Revokes access to the given namespaces with the specified access modes for the access group.
     * @param label the label of the group
     * @param org the org to which the group belongs
     * @param namespaces a list of namespace strings to revoke access from
     * @param modes a set of {@link com.redhat.rhn.domain.access.Namespace.AccessMode}s
     * @throws DefaultRoleException if trying to revoke access for a default access group.
     */
    public void revokeAccess(String label, Org org, List<String> namespaces, Set<Namespace.AccessMode> modes)
            throws DefaultRoleException {
        AccessGroup group = lookup(label, org).orElseThrow();
        for (String ns : namespaces) {
            revokeAccess(group, ns, modes);
            LOG.debug("Access group {} is revoked access to namespace {}.", label, ns);
        }
        LOG.info("Access group {} is revoked access to {} namespace(s).", label, namespaces.size());
    }

    protected void grantAccess(AccessGroup group, String namespace, Set<Namespace.AccessMode> modes)
            throws DefaultRoleException {
        if (group.getOrg() == null) {
            throw new DefaultRoleException("Default groups cannot be altered.");
        }
        List<Namespace> namespaces = NamespaceFactory.find(namespace, modes);
        group.getNamespaces().addAll(namespaces);
        AccessGroupFactory.save(group);
    }

    protected void revokeAccess(AccessGroup group, String namespace, Set<Namespace.AccessMode> modes)
            throws DefaultRoleException {
        if (group.getOrg() == null) {
            throw new DefaultRoleException("Default groups cannot be altered.");
        }
        List<Namespace> namespaces = NamespaceFactory.find(namespace, modes);
        namespaces.forEach(group.getNamespaces()::remove);
        AccessGroupFactory.save(group);
    }

    private void addPermissionsFromRole(AccessGroup addTo, AccessGroup copyFrom) {
        addTo.getNamespaces().addAll(copyFrom.getNamespaces());
    }
}
