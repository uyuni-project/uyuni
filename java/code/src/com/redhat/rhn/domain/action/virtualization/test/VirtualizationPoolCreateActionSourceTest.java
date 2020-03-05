package com.redhat.rhn.domain.action.virtualization.test;

import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateActionSource;

import com.suse.manager.virtualization.PoolSourceDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import junit.framework.TestCase;

public class VirtualizationPoolCreateActionSourceTest extends TestCase {

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
