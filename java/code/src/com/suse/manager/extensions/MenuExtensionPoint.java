/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.extensions;

import com.suse.manager.webui.menu.MenuItem;

import org.pf4j.ExtensionPoint;

import java.util.List;
import java.util.Map;

/**
 * Interface to implement to extend the menu
 */
public interface MenuExtensionPoint extends ExtensionPoint {

    /**
     * Get the list of entries to add to the side menu.
     *
     * The returned tree items are mapped using the parent tree item path. This means
     * that:
     * <ul>
     *   <li>to add a top level menu the key will need to be the empty string</li>
     *   <li>to add an entry in "Level One" > "Sublevel One", the key will need to be
     *       <code>Level One/Sublevel One</code></li>
     * </ul>
     *
     * @param adminRoles admin roles the user has. Keys are: <code>org</code>, <code>config</code>,
     *      <code>satellite</code>, <code>activationKey</code> and <code>image</code>
     * @param authenticated true to get the menu items when a user is authenticated, false for anonymous.
     *
     * @return mapped menu items
     */
    Map<String, List<MenuItem>> getMenuItems(Map<String, Boolean> adminRoles, boolean authenticated);
}
