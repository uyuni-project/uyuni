/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.Label;

import java.util.Optional;


/**
 * VirtualInstanceType
 */
public class VirtualInstanceType extends Label {

    VirtualInstanceType() {
    }

    public Optional<String> getCloudProvider() {
        switch(getLabel()) {
            case "azure": return Optional.of("Microsoft");
            case "aws": return Optional.of("Amazon");
            case "gce": return Optional.of("Google");
            default: return Optional.empty();
        }
    }

    public Optional<String> getHypervisor() {
        switch(getLabel()) {
            case "fully_virtualized": return Optional.of("Xen");
            case "para_virtualized": return Optional.of("Xen");
            case "qemu": return Optional.of("KVM");
            case "vmware":return Optional.of("VMware");
            case "hyperv": return Optional.of("Hyper-V");
            case "nutanix": return Optional.of("Nutanix");
            case "virtualbox": return Optional.of("VirtualBox");
            case "virtualpc": return Optional.of("VirtualPC");
            case "virtage": return Optional.of("Virtage");
            default: return Optional.empty();
        }
    }
}


