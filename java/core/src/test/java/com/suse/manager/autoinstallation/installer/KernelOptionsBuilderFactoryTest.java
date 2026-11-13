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
package com.suse.manager.autoinstallation.installer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.domain.kickstart.KickstartInstallType;

import com.suse.manager.autoinstallation.builder.AgamaKernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.AutoYastKernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.KernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.KernelOptionsBuilderException;
import com.suse.manager.autoinstallation.builder.RhelKernelOptionsBuilder;

import org.junit.jupiter.api.Test;

/**
 * Tests the install type routing of KernelOptionsBuilderFactory.
 */
public class KernelOptionsBuilderFactoryTest {

    private static final String FQDN = "server.example.com";

    private KickstartInstallType installType(String label) {
        KickstartInstallType type = new KickstartInstallType();
        type.setLabel(label);
        return type;
    }

    /**
     * SLE16 install types route to the Agama builder, before the SUSE branch:
     * opensuse16.1 satisfies isSUSE() as well.
     */
    @Test
    public void testSles16RoutesToAgamaBuilder() {
        KernelOptionsBuilder builder = KernelOptionsBuilderFactory.getBuilder(
                installType("opensuse16.1"), FQDN);
        assertInstanceOf(AgamaKernelOptionsBuilder.class, builder);
    }

    /**
     * SLES 15 and the bare suse install type route to the AutoYaST builder.
     */
    @Test
    public void testSles15AndSuseRouteToAutoYastBuilder() {
        assertInstanceOf(AutoYastKernelOptionsBuilder.class,
                KernelOptionsBuilderFactory.getBuilder(installType("sles15generic"), FQDN));
        assertInstanceOf(AutoYastKernelOptionsBuilder.class,
                KernelOptionsBuilderFactory.getBuilder(installType("suse"), FQDN));
        assertInstanceOf(AutoYastKernelOptionsBuilder.class,
                KernelOptionsBuilderFactory.getBuilder(installType("sles10generic"), FQDN));
    }

    /**
     * All RHEL install types route to the RHEL builder.
     */
    @Test
    public void testRhelRoutesToRhelBuilder() {
        assertInstanceOf(RhelKernelOptionsBuilder.class,
                KernelOptionsBuilderFactory.getBuilder(installType("rhel_6"), FQDN));
        assertInstanceOf(RhelKernelOptionsBuilder.class,
                KernelOptionsBuilderFactory.getBuilder(installType("rhel_10"), FQDN));
    }

    /**
     * Fedora and generic RPM install types route to the RHEL builder.
     */
    @Test
    public void testFedoraAndGenericRouteToRhelBuilder() {
        assertInstanceOf(RhelKernelOptionsBuilder.class,
                KernelOptionsBuilderFactory.getBuilder(installType("fedora18"), FQDN));
        assertInstanceOf(RhelKernelOptionsBuilder.class,
                KernelOptionsBuilderFactory.getBuilder(installType("generic_rpm"), FQDN));
    }

    /**
     * An install type matching no builder is rejected, not defaulted.
     */
    @Test
    public void testUnknownInstallTypeThrows() {
        KernelOptionsBuilderException e = assertThrows(KernelOptionsBuilderException.class,
                () -> KernelOptionsBuilderFactory.getBuilder(installType("foobar"), FQDN));
        assertEquals("No kernel options builder for install type: foobar", e.getMessage());
    }

    /**
     * A null install type is rejected.
     */
    @Test
    public void testNullInstallTypeThrows() {
        assertThrows(KernelOptionsBuilderException.class,
                () -> KernelOptionsBuilderFactory.getBuilder(null, FQDN));
    }

    /**
     * A null or empty server FQDN is rejected.
     */
    @Test
    public void testMissingServerFqdnThrows() {
        KickstartInstallType type = installType("sles15generic");
        assertThrows(KernelOptionsBuilderException.class,
                () -> KernelOptionsBuilderFactory.getBuilder(type, null));
        assertThrows(KernelOptionsBuilderException.class,
                () -> KernelOptionsBuilderFactory.getBuilder(type, ""));
    }
}
