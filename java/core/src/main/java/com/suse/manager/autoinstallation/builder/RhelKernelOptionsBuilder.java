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
 * Kernel options builder for RHEL, Fedora and generic RPM install types.
 */
public class RhelKernelOptionsBuilder extends AbstractKernelOptionsBuilder {

    /**
     * Constructor
     * @param serverFqdnIn the kickstart host
     * @param userIn optional user context scoping the installer-updates
     *               child channel lookup, null for org-global lookup
     */
    public RhelKernelOptionsBuilder(String serverFqdnIn, User userIn) {
        super(serverFqdnIn, userIn);
    }

    /**
     * {@inheritDoc}
     *
     * inst.repo= pointing at the dist tree.
     */
    @Override
    public KernelOptionsList distroOptions(KickstartableTree tree) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("inst.repo", distTreeUrl(require(tree, "tree")));
        return options;
    }

    /**
     * {@inheritDoc}
     *
     * Empty: RHEL installer updates currently unsupported.
     */
    @Override
    public KernelOptionsList selfUpdateOption(KickstartableTree tree) {
        require(tree, "tree");
        return new KernelOptionsList();
    }

    /**
     * {@inheritDoc}
     *
     * inst.repo= pointing at the session-tracked media URL.
     */
    @Override
    public KernelOptionsList mediaOptions(String mediaUrl) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("inst.repo", mediaUrl);
        return options;
    }

    /**
     * {@inheritDoc}
     *
     * inst.ks= pointing at the Cobbler system autoinstall URL plus
     * inst.ks.sendmac.
     */
    @Override
    public KernelOptionsList systemOptions(SystemRecord system) {
        KernelOptionsList options = new KernelOptionsList();
        options.setFlagIfNotPresent("inst.ks.sendmac");
        options.setOptionIfNotPresent("inst.ks", cobblerSystemAutoinstallUrl(require(system, "system")));
        return options;
    }
}
