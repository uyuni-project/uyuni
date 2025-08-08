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
package com.suse.manager.webui.utils;

import com.redhat.rhn.domain.access.Namespace;

import com.suse.manager.webui.utils.gson.NamespaceNodeJson;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helper class to build a tree structure of namespaces for access control.
 * This is used to help show namespaces hierarchically in the UI.
 */
public class AccessControlNamespaceTreeHelper {

    /**
     * Builds a tree structure of namespaces from the provided list.
     * The namespaces are grouped into root nodes based on their hierarchy.
     *
     * @param namespaces List of namespaces to build the tree from
     * @return A collection of root nodes representing the namespace tree
     */
    public Collection<NamespaceNodeJson> buildTree(List<Namespace> namespaces) {
        Map<Pair<String, Boolean>, NamespaceNodeJson> rootNodes = new TreeMap<>();
        for (Namespace namespace : namespaces) {
            addNamespaceToTree(namespace, rootNodes);
        }
        return rootNodes.values();
    }

    /**
     * Adds a namespace to the tree structure.
     * @param namespace - the namespace
     * @param rootNodes - the map of root nodes
     */
    private void addNamespaceToTree(Namespace namespace, Map<Pair<String, Boolean>, NamespaceNodeJson> rootNodes) {
        final boolean isApi = namespace.getNamespace().startsWith("api.");

        if (isApi) {
            handleApiNamespace(namespace, rootNodes);
        }
        else {
            handleWebNamespace(namespace, rootNodes);
        }
    }

    /**
     * Handles the addition of an API namespace to the tree structure.
     * It splits the namespace into parent and child nodes based on the last dot.
     *
     * @param namespace - the API namespace
     * @param rootNodes - the map of root nodes
     */
    private void handleApiNamespace(Namespace namespace, Map<Pair<String, Boolean>, NamespaceNodeJson> rootNodes) {
        String apiPath = namespace.getNamespace().substring(4);
        int lastDotIndex = apiPath.lastIndexOf('.');
        Pair<String, Boolean> key;

        if (lastDotIndex == -1) {
            key = Pair.of(apiPath, true);
            NamespaceNodeJson leafNode = rootNodes.computeIfAbsent(
                key, k -> new NamespaceNodeJson(namespace, k.getLeft())
            );
            leafNode.mergeAccessMode(namespace);
        }
        else {
            String parentName = apiPath.substring(0, lastDotIndex);
            String childName = apiPath.substring(lastDotIndex + 1);

            key = Pair.of(parentName, true);
            NamespaceNodeJson parentNode = rootNodes.computeIfAbsent(
                key, k -> new NamespaceNodeJson(k.getLeft(), true)
            );

            NamespaceNodeJson childLeaf = parentNode.getChildLeaf(childName);
            if (childLeaf == null) {
                childLeaf = new NamespaceNodeJson(namespace, childName);
                parentNode.getChildren().add(childLeaf);
            }
            else {
                childLeaf.mergeAccessMode(namespace);
            }
        }
    }

    /**
     * Handles the addition of a web namespace to the tree structure.
     * It splits the namespace into root, intermediate, and leaf nodes based on dots.
     *
     * @param namespace - the web namespace
     * @param rootNodes - the map of root nodes
     */
    private void handleWebNamespace(Namespace namespace, Map<Pair<String, Boolean>, NamespaceNodeJson> rootNodes) {
        int firstDotIndex = namespace.getNamespace().indexOf('.');
        if (firstDotIndex == -1) {
            Pair<String, Boolean> key = Pair.of(namespace.getNamespace(), false);
            NamespaceNodeJson leafNode = rootNodes.computeIfAbsent(
                key, k -> new NamespaceNodeJson(namespace, k.getLeft())
            );
            leafNode.mergeAccessMode(namespace);
            return;
        }

        String rootName = namespace.getNamespace().substring(0, firstDotIndex);
        NamespaceNodeJson parentNode = rootNodes.computeIfAbsent(
            Pair.of(rootName, false),
            key -> new NamespaceNodeJson(key.getLeft(), false)
        );

        int secondDotIndex = namespace.getNamespace().indexOf('.', firstDotIndex + 1);
        if (secondDotIndex == -1) {
            String childName = namespace.getNamespace().substring(firstDotIndex + 1);
            NamespaceNodeJson existingLeaf = parentNode.getChildLeaf(childName);
            if (existingLeaf == null) {
                parentNode.getChildren().add(new NamespaceNodeJson(namespace, childName));
            }
            else {
                existingLeaf.mergeAccessMode(namespace);
            }
        }
        else {
            String intermediateName = namespace.getNamespace().substring(firstDotIndex + 1, secondDotIndex);
            NamespaceNodeJson intermediateNode = parentNode.getChildBranch(intermediateName);
            if (intermediateNode == null) {
                intermediateNode = new NamespaceNodeJson(intermediateName, false);
                parentNode.getChildren().add(intermediateNode);
            }

            String leafName = namespace.getNamespace().substring(secondDotIndex + 1);
            NamespaceNodeJson finalLeaf = intermediateNode.getChildLeaf(leafName);
            if (finalLeaf == null) {
                intermediateNode.getChildren().add(new NamespaceNodeJson(namespace, leafName));
            }
            else {
                finalLeaf.mergeAccessMode(namespace);
            }
        }
    }
}
