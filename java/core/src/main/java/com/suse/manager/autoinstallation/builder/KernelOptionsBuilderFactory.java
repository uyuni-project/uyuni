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

import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;

import com.suse.manager.autoinstallation.KernelOptionsList;
import com.suse.manager.autoinstallation.installer.agama.AgamaKernelOptionsBuilder;
import com.suse.manager.autoinstallation.installer.autoyast.AutoYastKernelOptionsBuilder;
import com.suse.manager.autoinstallation.installer.rhel.RhelKernelOptionsBuilder;

import org.cobbler.Profile;
import org.cobbler.SystemRecord;

/**
 * Factory for retrieving the correct KernelOptionsBuilder based on the Uyuni breed.
 */
public class KernelOptionsBuilderFactory {

    private KernelOptionsBuilderFactory() { }

    private static final KernelOptionsBuilder DEFAULT_BUILDER = new KernelOptionsBuilder() {
        @Override
        public KernelOptionsList networkBoot(KickstartableTree ksTree, SystemRecord system) {
            return new KernelOptionsList();
        }

        @Override
        public KernelOptionsList localBoot(KickstartableTree ksTree, SystemRecord system) {
            return new KernelOptionsList();
        }

        @Override
        public KernelOptionsList distroOptions(KickstartableTree ksTree) {
            return new KernelOptionsList();
        }

        @Override
        public KernelOptionsList profileOptions(Profile profile) {
            return new KernelOptionsList();
        }

        @Override
        public KernelOptionsList systemOptions(SystemRecord system) {
            return new KernelOptionsList();
        }
    };

    /**
     * Returns the appropriate KernelOptionsBuilder for the given breed.
     *
     * @param installType the installer install type
     * @return the kernel options builder (never null)
     */
    public static KernelOptionsBuilder getBuilder(KickstartInstallType installType) {
        if (installType == null) {
            throw new KernelOptionsBuilderException("InstallType cannot be null");
        }
//      // TODO: add SLES16 type
//        if (installType.isSLES16OrGreater()) {
//            return new AgamaKernelOptionsBuilder();
//        }
        if (installType.isSUSE()) {
            return new AutoYastKernelOptionsBuilder();
        }
        else if (installType.isRhel()) {
            return new RhelKernelOptionsBuilder();
        }
        return DEFAULT_BUILDER;
    }
}
