/**
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.menu.test;

import com.suse.manager.webui.menu.MenuItem;
import com.suse.manager.webui.menu.MenuTree;
import com.suse.manager.webui.menu.MenuTree.MenuItemList;

import java.util.List;


import junit.framework.TestCase;

public class MenuTreeTest extends TestCase {

    public void testActiveNode() {
        // the requested url
        String url = "/rhn/account/UserDetails.do";

        // the menu tree
        MenuItemList nodes = new MenuItemList();
        nodes.add(new MenuItem("Home")
                .addChild(new MenuItem("Overview")
                    .withPrimaryUrl("/rhn/YourRhn.do"))
                .addChild(new MenuItem("User Account")
                    .addChild(new MenuItem("My Account")
                        .withPrimaryUrl("/rhn/account/UserDetails.do"))));

        // the TESTED method
        MenuItem activeNode = MenuTree.getActiveNode(nodes, url);

        // check if the active node has the expected label and it is active flagged
        assertTrue(activeNode.getActive());
        assertTrue(activeNode.getLabel().equalsIgnoreCase("My Account"));
        assertFalse(activeNode.getLabel().equalsIgnoreCase("Home"));
    }

    public void testBestActiveDir() {
        // the requested url
        String url = "/rhn/errata/manage/Create.do";

        // the menu tree
        MenuItemList nodes = new MenuItemList();
        nodes.add(new MenuItem("Patches")
                .addChild(new MenuItem("Patches")
                    .withPrimaryUrl("/rhn/errata/RelevantErrata.do")
                    .withDir("/rhn/errata")
                    .addChild(new MenuItem("Relevant")
                        .withPrimaryUrl("/rhn/errata/RelevantErrata.do")
                        .withAltUrl("/rhn/errata/RelevantBugErrata.do")
                        .withAltUrl("/rhn/errata/RelevantEnhancementErrata.do")
                        .withAltUrl("/rhn/errata/RelevantSecurityErrata.do"))
                    .addChild(new MenuItem("All")
                        .withPrimaryUrl("/rhn/errata/AllErrata.do")
                        .withAltUrl("/rhn/errata/AllBugErrata.do")
                        .withAltUrl("/rhn/errata/AllEnhancementErrata.do")
                        .withAltUrl("/rhn/errata/AllSecurityErrata.do")))
                .addChild(new MenuItem("Advanced Search")
                    .withPrimaryUrl("/rhn/errata/Search.do"))
                .addChild(new MenuItem("Manage Errata")
                        .withDir("/rhn/errata/manage")
                        .addChild(new MenuItem("Published")
                            .withPrimaryUrl("/rhn/errata/manage/PublishedErrata.do"))
                        .addChild(new MenuItem("Unpublished")
                            .withPrimaryUrl("/rhn/errata/manage/UnpublishedErrata.do")))
                .addChild(new MenuItem("Clone Errata")
                    .withPrimaryUrl("/rhn/errata/manage/CloneErrata.do")
                    .withDir("/rhn/errata/manage/clone")));

        // the TESTED method
        List<MenuItem> bestActiveDir = MenuTree.getBestActiveDirs(nodes, url);

        // check if all returned levels are flagged as active
        for (MenuItem menuItem : bestActiveDir) {
            assertTrue(menuItem.getActive());
        }

        // check if deepest active level has expected dir value
        Integer lastIndex = bestActiveDir.size()-1;
        assertTrue(bestActiveDir.get(lastIndex).getDirectories().get(0)
                .equalsIgnoreCase("/rhn/errata/manage"));
    }

    /**
     * Test the correct functionality of {@link MenuItemList} regarding visibility.
     */
    public void testVisibility() {
        MenuItem home = new MenuItem("Home");
        MenuItem systems = new MenuItem("Systems").withVisibility(true);
        MenuItem admin = new MenuItem("Admin").withVisibility(false);

        MenuItemList nodes = new MenuItemList();
        nodes.add(home);
        nodes.add(systems);
        nodes.add(admin);

        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(home));
        assertTrue(nodes.contains(systems));
        assertFalse(nodes.contains(admin));
    }
}


