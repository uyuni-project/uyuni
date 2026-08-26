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
import com.suse.manager.autoinstallation.installer.debian.DebianKernelOptionsBuilder;
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
     * Returns the appropriate KernelOptionsBuilder for the given install type.
     *
     * @param installType the installer install type
     * @return the kernel options builder (never null)
     */
    public static KernelOptionsBuilder getBuilder(KickstartInstallType installType) {
        if (installType == null) {
            throw new KernelOptionsBuilderException("InstallType cannot be null");
        }
        if (installType.isSLES16OrGreater()) {
            return new AgamaKernelOptionsBuilder();
        }
        if (installType.isSUSE()) {
            return new AutoYastKernelOptionsBuilder();
        }
        if (installType.isRhel() || installType.isFedora() || installType.isGeneric()) {
            return new RhelKernelOptionsBuilder();
        }
        return DEFAULT_BUILDER;
    }

    /**
     * Returns the appropriate KernelOptionsBuilder for the given cobbler breed and os version.
     *
     * @param breed the cobbler breed
     * @param osVersion the os version
     * @return the kernel options builder (never null)
     */
    public static KernelOptionsBuilder getBuilderForBreed(String breed, String osVersion) {
        if (breed == null || breed.isBlank()) {
            return new RhelKernelOptionsBuilder();
        }
        return switch (breed) {
            case "generic" -> {
                if (osVersion.startsWith(KickstartInstallType.SLES_PREFIX + "16")) {
                    yield new AgamaKernelOptionsBuilder();
                }
                yield new RhelKernelOptionsBuilder();
            }
            case "redhat" -> new RhelKernelOptionsBuilder();
            case "suse" -> new AutoYastKernelOptionsBuilder();
            case "debian", "ubuntu" -> new DebianKernelOptionsBuilder();
            default -> DEFAULT_BUILDER;
        };
    }
}
