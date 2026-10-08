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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartInstallType;

import com.suse.manager.autoinstallation.installer.agama.AgamaKernelOptionsBuilder;
import com.suse.manager.autoinstallation.installer.autoyast.AutoYastKernelOptionsBuilder;
import com.suse.manager.autoinstallation.installer.debian.DebianKernelOptionsBuilder;
import com.suse.manager.autoinstallation.installer.rhel.RhelKernelOptionsBuilder;

import org.junit.jupiter.api.Test;

/**
 * Tests for KernelOptionsBuilderFactory.
 */
public class KernelOptionsBuilderFactoryTest {

    @Test
    public void testGetBuilderNull() {
        assertThrows(KernelOptionsBuilderException.class, () -> {
            KernelOptionsBuilderFactory.getBuilder(null);
        });
    }

    @Test
    public void testGetBuilderAgama() {
        KickstartInstallType installType = new KickstartInstallType() {
            @Override
            public boolean isSLES16OrGreater() {
                return true;
            }
        };
        KernelOptionsBuilder builder = KernelOptionsBuilderFactory.getBuilder(installType);
        assertNotNull(builder);
        assertTrue(builder instanceof AgamaKernelOptionsBuilder);
    }

    @Test
    public void testGetBuilderAutoYast() {
        KickstartInstallType installType = new KickstartInstallType() {
            @Override
            public boolean isSLES16OrGreater() {
                return false;
            }

            @Override
            public boolean isSUSE() {
                return true;
            }
        };
        KernelOptionsBuilder builder = KernelOptionsBuilderFactory.getBuilder(installType);
        assertNotNull(builder);
        assertTrue(builder instanceof AutoYastKernelOptionsBuilder);
    }

    @Test
    public void testGetBuilderRhel() {
        KickstartInstallType installType = new KickstartInstallType() {
            @Override
            public boolean isSLES16OrGreater() {
                return false;
            }

            @Override
            public boolean isSUSE() {
                return false;
            }

            @Override
            public boolean isRhel() {
                return true;
            }
        };
        KernelOptionsBuilder builder = KernelOptionsBuilderFactory.getBuilder(installType);
        assertNotNull(builder);
        assertTrue(builder instanceof RhelKernelOptionsBuilder);
    }

    @Test
    public void testGetBuilderForBreed() {
        KernelOptionsBuilder debianBuilder = KernelOptionsBuilderFactory.getBuilderForBreed("debian", "generic");
        assertTrue(debianBuilder instanceof DebianKernelOptionsBuilder);

        KernelOptionsBuilder ubuntuBuilder = KernelOptionsBuilderFactory.getBuilderForBreed("ubuntu", "generic");
        assertTrue(ubuntuBuilder instanceof DebianKernelOptionsBuilder);

        KernelOptionsBuilder suseBuilder = KernelOptionsBuilderFactory.getBuilderForBreed("suse", "sles15");
        assertTrue(suseBuilder instanceof AutoYastKernelOptionsBuilder);

        KernelOptionsBuilder redhatBuilder = KernelOptionsBuilderFactory.getBuilderForBreed("redhat", "rhel8");
        assertTrue(redhatBuilder instanceof RhelKernelOptionsBuilder);

        KernelOptionsBuilder genericBuilder = KernelOptionsBuilderFactory.getBuilderForBreed("generic", "rhel8");
        assertTrue(genericBuilder instanceof RhelKernelOptionsBuilder);

        KernelOptionsBuilder unknownBuilder = KernelOptionsBuilderFactory.getBuilderForBreed("unknown", "generic");
        assertNotNull(unknownBuilder);
    }
}
