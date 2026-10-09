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

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.Profile;
import org.cobbler.SystemRecord;

/**
 * Builds kernel command line options for a given OS class (install type).
 * Implementations produce freshly computed options only; merging with
 * user-typed options (which take precedence) is the caller's job via
 * {@link KernelOptionsList#addMissingOptions(KernelOptionsList)}.
 */
public interface KernelOptionsBuilder {

    /**
     * Distribution related options, persisted into KickstartableTree.kernelOptions(_post)
     * and recomputed on every tree create/edit.
     *
     * @param tree the distribution tree
     * @return the computed distro options, empty for unsupported distros
     */
    KernelOptionsList distroOptions(KickstartableTree tree);

    /**
     * Only the installer-updates option (self_update= URL or 0,
     * inst.self_update= for Agama); empty for RHEL.
     *
     * @param tree the distribution tree
     * @return the computed installer-updates option, possibly empty
     */
    KernelOptionsList selfUpdateOption(KickstartableTree tree);

    /**
     * Profile related options.
     * Cobbler's tftpgen owns the installer-selection option on
     * generated PXE entries, so Uyuni should not duplicate it on profile
     * records.
     *
     * @param profile the autoinstallation profile
     * @return the computed profile options
     */
    KernelOptionsList profileOptions(Profile profile);

    /**
     * Installation-source override pointing at the session-tracked media
     * URL built by the caller as http://&lt;kickstartHost&gt;&lt;mediaPath&gt;.
     * The option key is OS-class specific: install= (AutoYaST),
     * inst.repo= (RHEL-like), inst.install_url= (Agama product-default
     * override).
     *
     * @param mediaUrl the session-tracked media URL
     * @return the computed installation-source override option
     */
    KernelOptionsList mediaOptions(String mediaUrl);

    /**
     * Per-system options including the installer-selection option
     * (ks=, autoyast= or inst.auto=). Only for boot lines Uyuni composes
     * entirely itself (initiate path); never written to Cobbler records.
     *
     * @param system the system record to boot
     * @return the computed system options
     */
    KernelOptionsList systemOptions(SystemRecord system);
}
