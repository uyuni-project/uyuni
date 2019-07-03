/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.frontend.nav;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * NavTreeIndex
 * @version $Rev$
 */

public class NavTreeIndex {
    private static Logger log = Logger.getLogger(NavTreeIndex.class);

    private Map<String, NavNode> nodesByLabel;
    private Map<NavNode, NavNode> childToParentMap;
    private Map<NavNode, Integer> depthMap;
    private Map<String, List<NavNode>> nodeDirMap; //tab dir is key, value is List of NavNodes
    private Map<String, List<NavNode>> nodeURLMap; //url is key, value is List of NavNodes
    private Map<String, NavNode> primaryURLMap; //url is key, value is NavNode
    private ArrayList<List<NavNode>> nodeLevels;
    private Set<NavNode> activeNodes; //The best node and its parents (all highlighted in UI)
    private NavNode bestNode; //The node best corresponding with the url
    private NavTree tree;

    /**
     * Public constructor
     * @param treeIn the tree to index
     */
    public NavTreeIndex(NavTree treeIn) {
        nodesByLabel = new HashMap<String, NavNode>();
        childToParentMap = new HashMap<NavNode, NavNode>();
        depthMap = new HashMap<NavNode, Integer>();
        nodeDirMap = new HashMap<String, List<NavNode>>();
        nodeURLMap = new HashMap<String, List<NavNode>>();
        primaryURLMap = new HashMap<String, NavNode>();
        nodeLevels = new ArrayList<List<NavNode>>();
        activeNodes = new HashSet<NavNode>();

        tree = treeIn;
        indexTree();
    }

    /**
     * get the tree of that this index indexes
     * @return NavTree the tree in question
     */
    public NavTree getTree() {
        return tree;
    }

    /**
     * get the "best" node of that this index indexes.  "best" is
     * defined as the single node most fitting the request as passed
     * into computeActiveNodes.
     * @return NavNode the best node
     */
    public NavNode getBestNode() {
        return bestNode;
    }

    private void indexTree() {
        int depth = 0;
        List<NavNode> nodesAtCurrentDepth = new ArrayList<NavNode>(tree.getNodes());
        nodeLevels.add(depth, nodesAtCurrentDepth);

        Iterator<NavNode> i = nodesAtCurrentDepth.iterator();
        while (i.hasNext()) {
            NavNode n = i.next();
            indexNode(n, depth + 1);
        }
    }

    private void indexNode(NavNode parent, int depth) {
        depthMap.put(parent, depth);
        if (log.isDebugEnabled()) {
            log.debug("adding primaryurl to map [" + parent.getPrimaryURL() + "]");
        }
        primaryURLMap.put(parent.getPrimaryURL(), parent);

        List<NavNode> nodesAtCurrentDepth = new ArrayList<NavNode>(parent.getNodes());
        nodeLevels.add(depth, nodesAtCurrentDepth);

        addURLMaps(parent);
        addDirMaps(parent);

        if (parent.getLabel() != null) {
            nodesByLabel.put(parent.getLabel(), parent);
        }

        Iterator<NavNode> i = nodesAtCurrentDepth.iterator();
        while (i.hasNext()) {
            NavNode child = i.next();
            childToParentMap.put(child, parent);

            indexNode(child, depth + 1);
        }
    }
    private void addURLMaps(NavNode node) {
        Iterator<String> i = node.getURLs().iterator();

        while (i.hasNext()) {
            String url = i.next();

            List<NavNode> currentNodes = nodeURLMap.get(url);
            if (currentNodes == null) {
                currentNodes = new ArrayList<NavNode>();
                if (log.isDebugEnabled()) {
                    log.debug("adding url map [" + url + "]");
                }
                nodeURLMap.put(url, currentNodes);
            }
            currentNodes.add(node);
        }
    }

    private void addDirMaps(NavNode node) {
        Iterator<String> i = node.getDirs().iterator();

        while (i.hasNext()) {
            String dir = i.next();

            List<NavNode> currentNodes = nodeDirMap.get(dir);
            if (currentNodes == null) {
                currentNodes = new ArrayList<NavNode>();
                if (log.isDebugEnabled()) {
                    log.debug("adding dir map [" + dir + "]");
                }
                nodeDirMap.put(dir, currentNodes);
            }
            currentNodes.add(node);
        }
    }

    /**
     * Splits the given url string at the /. Returns the
     * parts in an array.  For example, given "/network/users/details"
     * this method will return {"/network", "/users", "/details"}
     * @param urlIn url string to be split.
     * @return the parts in an array.
     */
    public static String[] splitUrlPrefixes(String urlIn) {
        String url = StringUtils.strip(urlIn, "/");
        String[] splitPath = StringUtils.split(url, "/");

        List<String> pathPrefixes = new ArrayList<String>(splitPath.length + 1);

        // loop through the path parts of URL, creating a new split
        // URL for each pass, starting with longest, going to shortest
        for (int i = splitPath.length - 1; i >= 0; i--) {
            StringBuilder sb = new StringBuilder("/");

            for (int j = 0; j <= i; j++) {
                sb.append(splitPath[j]);
                sb.append("/");
            }
            // strip off trailing / from last pass in loop
            sb.deleteCharAt(sb.length() - 1);

            pathPrefixes.add(sb.toString());
        }

        pathPrefixes.add("/");

        return pathPrefixes.toArray(new String[] {});
    }

    /**
     * Given a URL, compute the active nodes for the URL, altering the
     * state of the class.  Pass in the most recently Active  URL to be
     * used as a fallback if necessary.
     *
     * @param url string of form /foo/bar/baz
     * @param lastActive the last computed ActiveNode URL
     * @return String the URL computed
     */
    public String computeActiveNodes(String url, String lastActive) {
        String[] prefixes = splitUrlPrefixes(url);

        // If we have a lastActive URL we
        // will add it to the end of the list of URLs to
        // use it as a last resort.
        if (lastActive != null) {
            String[] urls = new String[prefixes.length + 1];

            // Add the lastActive to the end
            for (int i = 0; i < prefixes.length; i++) {
                urls[i] = prefixes[i];
            }
            urls[prefixes.length] = lastActive;
            prefixes = urls;
        }

        return computeActiveNodes(prefixes);
    }

    /**
     * does the real work for computeActiveNodes
     * @param urls list of URLs, in order of preference, to match
     * @return String the URL computed
     */
    private String computeActiveNodes(String[] urls) {
        bestNode = findBestNode(urls);
        if (bestNode == null) {
            // can't find an best node. assume topmost leftmost node is best
            List<NavNode> depthZero = nodeLevels.get(0);
            bestNode = depthZero.get(0);
        }

        NavNode walker = bestNode;

        activeNodes = new HashSet<NavNode>();
        while (walker != null) {
            activeNodes.add(walker);
            walker = childToParentMap.get(walker);
        }

        if (log.isDebugEnabled()) {
            log.debug("returning [" + bestNode.getPrimaryURL() +
                      "] as the url of the active node");
        }
        return bestNode.getPrimaryURL();
    }

    private NavNode findBestNode(String[] urls) {

        for (int i = 0; i < urls.length; i++) {

            if (log.isDebugEnabled()) {
                log.debug("Url being searched [" + urls[i] + "]");
            }
            // first match by the primary url which is the
            // first rhn-tab-url definition in the sitenav.xml.
            String url = urls[i];
            Optional<NavNode> result = primaryURLMap.entrySet().stream()
                    .filter(entry -> {
                        String key = entry.getKey().replaceAll("\\$\\{[^}]*\\}", ".*");
                        return entry.getKey().equals(url) || !key.equals(entry.getKey()) && Pattern.matches(key, url);
                    }).map(entry -> entry.getValue()).findFirst();
            if (result.isPresent()) {
                if (log.isDebugEnabled()) {
                    log.debug("Primary node for [" + url + "] is [" +
                            result.get() + "]");
                }

                // we found a match, now let's make sure it is accessible
                // we need to do this because sometimes there are multiple
                // nodes with the same url.  At that point they are
                // distinguishable only by acls.

                if (canViewUrl(result.get(), 0)) {
                    return result.get();
                }
            }

            // either we couldn't find a primary url match OR it isn't
            // accessible.  Let's go through the other url mappings (if any)
            // looking for an accessible url.

            List<NavNode> nodesByUrl = nodeURLMap.get(urls[i]);
            if (nodesByUrl != null) {
                Iterator<NavNode> nodeItr = nodesByUrl.iterator();
                while (nodeItr.hasNext()) {
                    NavNode next = nodeItr.next();
                    if (canViewUrl(next, 1)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Best node for [" + urls[i] + "] is [" +
                                    primaryURLMap.get(urls[i]) + "]");
                        }
                        return next;
                    }
                }
            }

            // finally, we couldn't find a match by primary url, nor by
            // any of the other mappings.  At this point we will attempt
            // to match by directory if there was an rhn-tab-directory
            // definition.  Otherwise, we're just going to bail and return
            // null.

            if (nodeDirMap.get(urls[i]) != null) {
                List<NavNode> nodes = nodeDirMap.get(urls[i]);
                // what do we do with a list that contains
                // more than one.
                if (log.isDebugEnabled()) {
                    log.debug("Best node for [" + urls[i] + "] is [" +
                            nodes.get(0) + "]");
                }
                return nodes.get(0);
            }
        }

        return null;
    }

    private boolean canViewUrl(NavNode node, int depth) {
        AclGuard guard = tree.getGuard();
        // purposefully an or, not an and
        return (guard == null || guard.canRender(node, depth));
    }

    /**
     * simple method to ask if a given node is in the active set
     * @param node to test
     * @return boolean if the node is active or not
     */
    public boolean isNodeActive(NavNode node) {
        return activeNodes.contains(node);
    }

}


