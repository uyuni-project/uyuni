/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.autoinstallation.builder;

import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.SystemRecord;

/**
 * Kernel options builder for SUSE install types up to SLES 15
 * (AutoYaST-based installers).
 */
public class AutoYastKernelOptionsBuilder extends AbstractKernelOptionsBuilder {

    /**
     * Constructor
     * @param serverFqdnIn the kickstart host
     */
    public AutoYastKernelOptionsBuilder(String serverFqdnIn) {
        this(serverFqdnIn, null);
    }

    /**
     * Constructor
     * @param serverFqdnIn the kickstart host
     * @param userIn optional user context scoping the installer-updates
     *               child channel lookup, null for org-global lookup
     */
    public AutoYastKernelOptionsBuilder(String serverFqdnIn, User userIn) {
        super(serverFqdnIn, userIn);
    }

    /**
     * {@inheritDoc}
     *
     * install= pointing at the dist tree plus the YaST self update option.
     */
    @Override
    public KernelOptionsList distroOptions(KickstartableTree tree) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("install", distTreeUrl(require(tree, "tree")));
        options.addOptions(selfUpdateOption(tree));
        return options;
    }

    /**
     * {@inheritDoc}
     *
     * self_update= with the installer-updates child URL, or 0 disabling the
     * self update when no such child is accessible.
     */
    @Override
    public KernelOptionsList selfUpdateOption(KickstartableTree tree) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("self_update", installerUpdatesValue(require(tree, "tree")));
        return options;
    }

    /**
     * {@inheritDoc}
     *
     * autoyast= pointing at the Cobbler system autoinstall URL.
     */
    @Override
    public KernelOptionsList systemOptions(SystemRecord system) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("autoyast", cobblerSystemAutoinstallUrl(require(system, "system")));
        return options;
    }
}
