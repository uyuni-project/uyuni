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
package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.access.Namespace;

import java.util.Set;
import java.util.TreeSet;

public class NamespaceNodeJson implements Comparable<NamespaceNodeJson> {
    private final Long id;
    private final String namespace;
    private final String name;
    private final String description;
    private String accessMode;
    private final Set<NamespaceNodeJson> children = new TreeSet<>();
    private final boolean isAPI;

    /**
     * Constructor to be used for non-leaf node namespaces
     *
     * @param nodeIn the node
     * @param isAPIIn whether this is an API node
     */
    public NamespaceNodeJson(String nodeIn, boolean isAPIIn) {
        this.id = null;
        this.name = nodeIn;
        this.namespace = nodeIn;
        this.description = "";
        this.accessMode = "";
        this.isAPI = isAPIIn;
    }

    /**
     * Constructor to be used for leaf node namespaces
     *
     * @param namespaceIn the namespace
     * @param node the node
     */
    public NamespaceNodeJson(Namespace namespaceIn, String node) {
        this.id = namespaceIn.getId();
        this.namespace = namespaceIn.getNamespace();
        this.name = node;
        this.description = namespaceIn.getDescription();
        this.accessMode = namespaceIn.getAccessMode().name();
        this.isAPI = namespaceIn.getNamespace().startsWith("api.");
    }

    /**
     * Finds a child node that is a BRANCH (placeholder with no ID).
     *
     * @param nameIn the name of the branch node to find
     * @return the NamespaceNodeJson representing the branch, or null if not found
     */
    public NamespaceNodeJson getChildBranch(String nameIn) {
        for (NamespaceNodeJson child : children) {
            if (child.name.equals(nameIn) && child.id == null) {
                return child;
            }
        }
        return null;
    }

    /**
     * Finds a child node that is a LEAF (has an ID).
     * @param nameIn the name of the leaf node to find
     * @return the NamespaceNodeJson representing the leaf, or null if not found
     */
    public NamespaceNodeJson getChildLeaf(String nameIn) {
        for (NamespaceNodeJson child : children) {
            if (child.name.equals(nameIn) && child.id != null) {
                return child;
            }
        }
        return null;
    }

    /**
     * Merges the access mode for an existing leaf node (e.g., to handle R + W = RW).
     * @param namespaceIn the Namespace object to merge access mode from
     */
    public void mergeAccessMode(Namespace namespaceIn) {
        String newAccessMode = namespaceIn.getAccessMode().name();
        if (!this.accessMode.equals(newAccessMode) && !"RW".equals(this.accessMode)) {
            this.accessMode = "RW";
        }
    }

    /**
     * Gets the ID of this node.
     * @return the ID, or null if this is a branch node
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the namespace of this node.
     * @return the namespace string
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the description of this node.
     * @return the description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the access mode of this node.
     * @return the access mode string
     */
    public String getAccessMode() {
        return accessMode;
    }

    /**
     * Gets the children of this node.
     * @return children nodes as a set
     */
    public Set<NamespaceNodeJson> getChildren() {
        return children;
    }

    /**
     * Gets if the node is API.
     * @return whether the node is API
     */
    public Boolean isAPI() {
        return isAPI;
    }

    @Override
    public int compareTo(NamespaceNodeJson other) {
        int nameCompare = this.name.compareToIgnoreCase(other.name);
        if (nameCompare != 0) {
            return nameCompare;
        }
        return this.namespace.compareTo(other.namespace);
    }

    @Override
    public boolean equals(Object objectIn) {
        if (this == objectIn) {
            return true;
        }
        if (!(objectIn instanceof NamespaceNodeJson other)) {
            return false;
        }
        return this.name.equalsIgnoreCase(other.name) &&
                this.namespace.equals(other.namespace);
    }

    @Override
    public int hashCode() {
        int result = name.toLowerCase().hashCode();
        result = 31 * result + namespace.hashCode();
        return result;
    }
}
