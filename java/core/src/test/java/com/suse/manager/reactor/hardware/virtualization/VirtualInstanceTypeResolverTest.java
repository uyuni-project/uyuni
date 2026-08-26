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
package com.suse.manager.reactor.hardware.virtualization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Tests for {@link VirtualInstanceTypeResolver}
 */
public class VirtualInstanceTypeResolverTest {

    /**
     * The factory shortcuts must agree with the types the resolver yields for Xen.
     */
    @Test
    public void testResolveMatchesFactoryShortcuts() {
        assertEquals(VirtualInstanceFactory.getInstance().getParaVirtType(),
                VirtualInstanceTypeResolver.resolve("xen", "Xen PV DomU", "dummy.suse"));
        assertEquals(VirtualInstanceFactory.getInstance().getFullyVirtType(),
                VirtualInstanceTypeResolver.resolve("xen", "Xen HVM DomU", "dummy.suse"));
    }

    @ParameterizedTest
    @MethodSource("resolveData")
    public void testResolve(String virtType, String virtSubtype, String expectedLabel) {
        VirtualInstanceType resolved = VirtualInstanceTypeResolver.resolve(virtType, virtSubtype, "dummy.suse");

        assertNotNull(resolved);
        assertEquals(expectedLabel, resolved.getLabel());
    }
    private static Stream<Arguments> resolveData() {
        return Stream.of(
            Arguments.of("xen", "Xen PV DomU", "para_virtualized"),
            Arguments.of("xen", "Xen HVM DomU", "fully_virtualized"),
            Arguments.of("qemu", "", "qemu"),
            Arguments.of("kvm", "", "qemu"),
            Arguments.of("nitro", "", "aws_nitro"),
            // passed through by the switch and seeded in the database
            Arguments.of("vmware", "", "vmware"),
            Arguments.of("hyperv", "Hyper-V", "hyperv"),
            Arguments.of("virtualbox", "", "virtualbox"),
            // an "Amazon EC2" subtype overrides the plain hypervisor mapping
            Arguments.of("xen", "Amazon EC2", "aws_xen"),
            Arguments.of("kvm", "Amazon EC2 t2.micro", "aws_nitro"),
            Arguments.of("nitro", "Amazon EC2", "aws_nitro"),
            Arguments.of("vmware", "Amazon EC2", "aws")
        );
    }

    @ParameterizedTest
    @MethodSource("unknownTypeData")
    public void testResolveFallsBackToFullyVirtualized(String virtType, String virtSubtype) {
        assertEquals(VirtualInstanceFactory.getInstance().getFullyVirtType(),
                VirtualInstanceTypeResolver.resolve(virtType, virtSubtype, "dummy.suse"));
    }

    private static Stream<Arguments> unknownTypeData() {
        return Stream.of(
            Arguments.of("bhyve", ""),
            Arguments.of("powervm", "PowerVM Lx86"),
            // no 'virtual' grain
            Arguments.of("", "")
        );
    }

    @ParameterizedTest
    @MethodSource("resolveLabelData")
    public void testResolveLabel(String virtType, String virtSubtype, String expected) {
        assertEquals(expected, VirtualInstanceTypeResolver.resolveLabel(virtType, virtSubtype, "dummy.suse"));
    }

    private static Stream<Arguments> resolveLabelData() {
        return Stream.of(
            // Xen: only the PV DomU subtype is para virtualized
            Arguments.of("xen", "Xen PV DomU", "para_virtualized"),
            Arguments.of("xen", "Xen HVM DomU", "fully_virtualized"),
                Arguments.of("xen", "something else", "fully_virtualized"),
                Arguments.of("xen", "", "fully_virtualized"),
            // qemu and kvm are both stored as qemu
            Arguments.of("qemu", "", "qemu"),
            Arguments.of("kvm", "", "qemu"),
            Arguments.of("kvm", "doesnt matter", "qemu"),
            // nitro is always AWS
                Arguments.of("nitro", "", "aws_nitro"),
                Arguments.of("nitro", "also doesnt matter", "aws_nitro"),
            // unknown hypervisors are passed through unchanged
            Arguments.of("vmware", "", "vmware"),
            Arguments.of("hyperv", "Hyper-V", "hyperv"),
            Arguments.of("", "", ""),
            Arguments.of("anything", "not ec2", "anything"),
            // an "Amazon EC2" subtype overrides the plain hypervisor mapping
            Arguments.of("xen", "Amazon EC2", "aws_xen"),
            Arguments.of("xen", "Amazon EC2 t2.micro", "aws_xen"),
            Arguments.of("xen", "Amazon EC2 PV DomU", "aws_xen"),
            Arguments.of("qemu", "Amazon EC2", "aws_nitro"),
            Arguments.of("kvm", "Amazon EC2", "aws_nitro"),
            Arguments.of("nitro", "Amazon EC2", "aws_nitro"),
            Arguments.of("vmware", "Amazon EC2", "aws"),
            Arguments.of("", "Amazon EC2", "aws"),
            // the prefix must match at the beginning of the subtype
            Arguments.of("xen", "Not Amazon EC2", "fully_virtualized")
        );
    }
}
