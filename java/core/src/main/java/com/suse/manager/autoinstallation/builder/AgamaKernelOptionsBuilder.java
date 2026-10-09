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
 * Kernel options builder for SLES 16 and greater install types
 * (Agama-based installers).
 */
public class AgamaKernelOptionsBuilder extends AbstractKernelOptionsBuilder {

    private static final String LIVE_ROOT_SUFFIX = "/LiveOS/squashfs.img";

    /**
     * Constructor
     * @param serverFqdnIn the kickstart host
     */
    public AgamaKernelOptionsBuilder(String serverFqdnIn) {
        this(serverFqdnIn, null);
    }

    /**
     * Constructor
     * @param serverFqdnIn the kickstart host
     * @param userIn optional user context scoping the installer-updates
     *               child channel lookup, null for org-global lookup
     */
    public AgamaKernelOptionsBuilder(String serverFqdnIn, User userIn) {
        super(serverFqdnIn, userIn);
    }

    /**
     * {@inheritDoc}
     *
     * root=live: pointing at the Agama live root on the dist tree plus the
     * installer self update option.
     */
    @Override
    public KernelOptionsList distroOptions(KickstartableTree tree) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("root", "live:" + distTreeUrl(require(tree, "tree")) + LIVE_ROOT_SUFFIX);
        options.addOptions(selfUpdateOption(tree));
        return options;
    }

    /**
     * {@inheritDoc}
     *
     * inst.self_update= with the installer-updates child URL, or 0 disabling
     * the self update when no such child is accessible.
     */
    @Override
    public KernelOptionsList selfUpdateOption(KickstartableTree tree) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("inst.self_update", installerUpdatesValue(require(tree, "tree")));
        return options;
    }

    /**
     * {@inheritDoc}
     *
     * inst.install_url= pointing at the session-tracked media URL,
     * overriding the product's default installation_url.
     */
    @Override
    public KernelOptionsList mediaOptions(String mediaUrl) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("inst.install_url", mediaUrl);
        return options;
    }

    /**
     * {@inheritDoc}
     *
     * inst.auto= pointing at the Cobbler system autoinstall URL.
     */
    @Override
    public KernelOptionsList systemOptions(SystemRecord system) {
        KernelOptionsList options = new KernelOptionsList();
        options.setOptionIfNotPresent("inst.auto", cobblerSystemAutoinstallUrl(require(system, "system")));
        return options;
    }
}
