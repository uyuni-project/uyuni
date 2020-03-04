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
package com.suse.manager.caasp;

import com.redhat.rhn.frontend.nav.NavNode;
import com.redhat.rhn.frontend.nav.NavTree;
import com.redhat.rhn.frontend.nav.extensions.SystemDetailNavTreeExtensionPoint;
import com.suse.manager.extensions.AbstractNavTreeExtensionPoint;
import org.pf4j.Extension;

import java.util.Optional;

@Extension
public class CaaspSystemDetailNavTreeMenu extends AbstractNavTreeExtensionPoint implements SystemDetailNavTreeExtensionPoint {

    @Override
    public void addNodes(NavTree tree) {

        NavNode caaspUpgradeTab = new NavNode();
        caaspUpgradeTab.addPrimaryURL("/rhn/manager/systems/details/packages/caasp-upgrade");
        caaspUpgradeTab.setName("CaaSP Upgrade");
        caaspUpgradeTab.setAcl("has_product_installed(caasp)");

        Optional<NavNode> packageNode = tree.getNodes().stream()
                .filter(n -> "Software".equals(n.getName()))
                .flatMap(n -> n.getNodes().stream())
                .filter(sn -> "Packages".equals(sn.getName()))
                .findFirst();

        packageNode
                .flatMap(pn -> pn.getNodes().stream().filter(n -> "Upgrade".equals(n.getName())).findFirst())
                .ifPresent(un -> {
                    un.addAcl("not has_product_installed(caasp)");
                });

        packageNode.ifPresent(pn -> {
            pn.addNode(caaspUpgradeTab);
        });
    }
}
