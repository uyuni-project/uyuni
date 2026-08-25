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
 * Strategy interface for building and decorating boot kernel options
 * for different autoinstallation types.
 */
public interface KernelOptionsBuilder {

    /**
     * Builds the kernel options required for a successful boot of a distribution
     * Depending on the installer generation (cobbler breed), cobbler may add additional options
     *
     * @param ksTree        the kickstart tree, cobbler distro precursor
     * @return the complete kernel options for a network boot
     * @throws KernelOptionsBuilderException on incorrect options
     */
    KernelOptionsList distroOptions(KickstartableTree ksTree) throws KernelOptionsBuilderException;

    /**
     * Builds the kernel options required for a successful boot of a profile
     * Depending on the installer generation (cobbler breed), cobbler may add additional options
     *
     * @param profile the profile
     * @return the complete kernel options for a network boot
     * @throws KernelOptionsBuilderException on incorrect options
     */
    KernelOptionsList profileOptions(Profile profile) throws KernelOptionsBuilderException;

    /**
     * Builds the kernel options required for a successful boot of a profile
     * Depending on the installer generation (cobbler breed), cobbler may add additional options
     *
     * @param system the system record
     * @return the complete kernel options for a network boot
     * @throws KernelOptionsBuilderException on incorrect options
     */
    KernelOptionsList systemOptions(SystemRecord system) throws KernelOptionsBuilderException;

    /**
     * Builds the complete kernel options required for a network (PXE) boot environment of a system.
     * Depending on the installer generation (cobbler breed), cobbler may add additional options
     *
     * @param ksTree the kickstart tree, cobbler distro precursor
     * @param system the Cobbler system record name
     * @return the complete kernel options for a network boot
     * @throws KernelOptionsBuilderException on incorrect options
     */
    KernelOptionsList networkBoot(KickstartableTree ksTree, SystemRecord system) throws KernelOptionsBuilderException;;

    /**
     * Builds the complete kernel options required for a local system reboot (Salt initiation).
     *
     * @param ksTree the kickstart tree, cobbler distro precursor
     * @param system the URL pointing to the autoinstallation recipe
     * @return the complete kernel options for non-PXE initiation
     * @throws KernelOptionsBuilderException on incorrect options
     */
    KernelOptionsList localBoot(KickstartableTree ksTree, SystemRecord system) throws KernelOptionsBuilderException;;
}
