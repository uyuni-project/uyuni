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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class NamespaceNodeJson implements Comparable<NamespaceNodeJson> {
    private final Long id;
    private final String namespace;
    private final String name;
    private final String description;
    private final String accessMode;
    private final Set<NamespaceNodeJson> children = new TreeSet<>();
    private final boolean isAPI;

    private final transient Map<String, NamespaceNodeJson> childrenMap = new HashMap<>();

    /**
     * Constructor to be used for non-leaf node namespaces
     *
     * @param nodeIn the node
     * @param isAPIIn whether this is an API node
     */
    public NamespaceNodeJson(String nodeIn, boolean isAPIIn) {
        this.name = nodeIn;
        this.namespace = nodeIn;
        this.description = "";
        this.accessMode = "";
        this.isAPI = isAPIIn;
        this.id = null;
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

    public String getNamespace() {
        return namespace;
    }

    /**
     * Adds a child node to this namespace node.
     * @param child - the child namespace node to add
     */
    public void addChild(NamespaceNodeJson child) {
        if (!childrenMap.containsKey(child.getNamespace())) {
            childrenMap.put(child.getNamespace(), child);
            children.add(child);
        }
    }

    /**
     * Gets the child nodes of this namespace node.
     * @param nameIn - the name of the child node to retrieve
     * @return - the child namespace node
     */
    public NamespaceNodeJson getChild(String nameIn) {
        return childrenMap.get(nameIn);
    }

    @Override
    public int compareTo(NamespaceNodeJson other) {
        return this.name.compareToIgnoreCase(other.name);
    }
}
