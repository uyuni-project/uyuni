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

package com.redhat.rhn.manager.access;

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.access.NamespaceFactory;
import com.redhat.rhn.domain.org.Org;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Class to manage RBAC access groups
 */
public class AccessGroupManager {

    private static final Logger LOG = LogManager.getLogger(AccessGroupManager.class);

    /**
     * Initialize a new instance
     */
    public AccessGroupManager() {
    }

    /**
     * Creates a new access group with the given parameters.
     * @param label the label of the group
     * @param description the description of the group
     * @param org the org to which the group belongs
     * @return the created {@code AccessGroup}
     */
    public AccessGroup create(String label, String description, Org org) {
        return this.create(label, description, org, Collections.emptyList());
    }

    /**
     * Creates a new access group with the given parameters.
     * @param label the label of the group
     * @param description the description of the group
     * @param org the org to which the group belongs
     * @param inheritFrom a collection of {@code AccessGroup}s to inherit permissions from
     * @return the created {@code AccessGroup}
     */
    public AccessGroup create(String label, String description, Org org, Collection<AccessGroup> inheritFrom) {
        AccessGroup group = new AccessGroup(label, description, org);
        for (AccessGroup parent : inheritFrom) {
            addPermissionsFromRole(group, parent);
        }

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
     * Lists all access groups belonging to the given org.
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
     * @return a set of granted namespaces
     * @throws DefaultRoleException if trying to revoke access for a default access group.
     */
    public Set<Namespace> grantAccess(String label, Org org, List<String> namespaces) throws DefaultRoleException {
        return grantAccess(label, org, namespaces, Set.of(Namespace.AccessMode.R, Namespace.AccessMode.W));
    }

    /**
     * Grants access to the given namespaces with the specified access modes for the access group.
     * <p>
     * The asterisk (*) character can be used as a wildcard in namespace strings.
     * @param label the label of the group
     * @param org the org to which the group belongs
     * @param namespaces a list of namespace strings to grant access to
     * @param modes a set of {@link com.redhat.rhn.domain.access.Namespace.AccessMode}s
     * @return a set of granted namespaces
     * @throws DefaultRoleException if trying to revoke access for a default access group.
     */
    public Set<Namespace> grantAccess(String label, Org org, List<String> namespaces, Set<Namespace.AccessMode> modes)
            throws DefaultRoleException {
        AccessGroup group = lookup(label, org).orElseThrow();
        List<Namespace> granted = new ArrayList<>();

        for (String ns : namespaces) {
            granted.addAll(grantAccess(group, ns, modes));
        }
        return new HashSet<>(granted);
    }

    /**
     * Revokes access to the given namespaces for the specified access group.
     * @param label the label of the group
     * @param org the org to which the group belongs
     * @param namespaces a list of namespace strings to revoke access from
     * @return A set of revoked namespaces
     * @throws DefaultRoleException if trying to revoke access for a default access group.
     */
    public Set<Namespace> revokeAccess(String label, Org org, List<String> namespaces) throws DefaultRoleException {
        return revokeAccess(label, org, namespaces, Set.of(Namespace.AccessMode.R, Namespace.AccessMode.W));
    }

    /**
     * Revokes access to the given namespaces with the specified access modes for the access group.
     * @param label the label of the group
     * @param org the org to which the group belongs
     * @param namespaces a list of namespace strings to revoke access from
     * @param modes a set of {@link com.redhat.rhn.domain.access.Namespace.AccessMode}s
     * @return A set of revoked namespaces
     * @throws DefaultRoleException if trying to revoke access for a default access group.
     */
    public Set<Namespace> revokeAccess(String label, Org org, List<String> namespaces, Set<Namespace.AccessMode> modes)
            throws DefaultRoleException {
        AccessGroup group = lookup(label, org).orElseThrow();
        List<Namespace> revoked = new ArrayList<>();

        for (String ns : namespaces) {
            revoked.addAll(revokeAccess(group, ns, modes));
        }
        return new HashSet<>(revoked);
    }

    protected List<Namespace> grantAccess(AccessGroup group, String namespace, Set<Namespace.AccessMode> modes)
            throws DefaultRoleException {
        if (group.getOrg() == null) {
            throw new DefaultRoleException("Default groups cannot be altered.");
        }
        List<Namespace> namespaces = NamespaceFactory.find(namespace, modes);
        group.getNamespaces().addAll(namespaces);
        AccessGroupFactory.save(group);
        return namespaces;
    }

    protected List<Namespace> revokeAccess(AccessGroup group, String namespace, Set<Namespace.AccessMode> modes)
            throws DefaultRoleException {
        if (group.getOrg() == null) {
            throw new DefaultRoleException("Default groups cannot be altered.");
        }
        List<Namespace> namespaces = NamespaceFactory.find(namespace, modes);
        namespaces.forEach(group.getNamespaces()::remove);
        AccessGroupFactory.save(group);
        return namespaces;
    }

    private void addPermissionsFromRole(AccessGroup addTo, AccessGroup inheritFrom) {
        addTo.getNamespaces().addAll(inheritFrom.getNamespaces());
    }
}