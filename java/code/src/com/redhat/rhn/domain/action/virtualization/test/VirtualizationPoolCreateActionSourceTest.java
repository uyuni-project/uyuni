/*
 * Copyright (c) 2020--2021 SUSE LLC
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
package com.redhat.rhn.domain.action.virtualization.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateActionSource;

import com.suse.manager.virtualization.PoolSourceDevice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class VirtualizationPoolCreateActionSourceTest  {

    @Test
    public void testNfsDir() {
        VirtualizationPoolCreateActionSource src = new VirtualizationPoolCreateActionSource();
        src.setDir("/var/lib/libvirt/images");
        src.setFormat("nfs");
        src.setHosts(Arrays.asList("localhost"));

        String serialized = src.toString();
        assertEquals(
               "{\"dir\":\"/var/lib/libvirt/images\"," +
               "\"hosts\":[\"localhost\"]," +
               "\"format\":\"nfs\"" +
               "}", serialized);

        VirtualizationPoolCreateActionSource src2 = VirtualizationPoolCreateActionSource.parse(serialized);
        assertEquals(src.getDir(), src2.getDir());
        assertEquals(src.getFormat(), src2.getFormat());
        assertEquals("localhost", src2.getHosts().get(0));
    }

    @Test
    public void testDevice() {
        VirtualizationPoolCreateActionSource src = new VirtualizationPoolCreateActionSource();
        List<PoolSourceDevice> devices = new ArrayList<>();
        devices.add(new PoolSourceDevice("/dev/mapper/dev0"));
        devices.add(new PoolSourceDevice("/dev/mapper/dev1", Optional.of(true)));
        src.setDevices(devices);

        String serialized = src.toString();
        assertEquals("{\"devices\":[" +
                "{\"path\":\"/dev/mapper/dev0\"}," +
                "{\"path\":\"/dev/mapper/dev1\",\"separator\":true}]}", serialized);
    }
}
