/**
 * Copyright (c) 2016 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.caasp;

import com.suse.manager.extensions.MenuExtensionPoint;
import com.suse.manager.webui.menu.MenuItem;
import org.pf4j.Extension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
public class CaaspMainMenu implements MenuExtensionPoint {

    @Override
    public Map<String, List<MenuItem>> getMenuItems(Map<String, Boolean> adminRoles, boolean authenticated) {

        if (authenticated) {
            Map<String, List<MenuItem>> items = new HashMap<>();
            items.put("Clusters", Arrays.asList(
                    new MenuItem("caasp").withPrimaryUrl("/rhn/manager/cluster/caasp")));
            return items;
        }

        return Collections.emptyMap();
    }
}
