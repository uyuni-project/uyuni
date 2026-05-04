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
package com.suse.manager.reactor.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for {@link HardwareDeviceSynchronizer}
 */
class HardwareDeviceSynchronizerTest extends BaseTestCaseWithUser {

    private MinionServer testServer;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.testServer = MinionServerFactoryTest.createTestMinionServer(user);
    }

    /**
     * Tests {@link HardwareDeviceSynchronizer#mapDevices(List)} with a null udevdb
     */
    @Test
    void testMapDevicesWithEmptyUdevdb() {
        HardwareDeviceSynchronizer mapper = new HardwareDeviceSynchronizer(testServer);
        Optional<String> result = mapper.mapDevices(null);

        assertTrue(result.isPresent());
        assertTrue(result.get().contains("Salt module 'udevdb.exportdb' returned an empty list"));
    }

    /**
     * Tests {@link HardwareDeviceSynchronizer#mapDevices(List)} with an empty list udevdb
     */
    @Test
    void testMapDevicesWithEmptyList() {
        HardwareDeviceSynchronizer mapper = new HardwareDeviceSynchronizer(testServer);
        Optional<String> result = mapper.mapDevices(new ArrayList<>());

        assertTrue(result.isPresent());
        assertTrue(result.get().contains("Salt module 'udevdb.exportdb' returned an empty list"));
    }

    /**
     * Tests {@link HardwareDeviceSynchronizer#mapDevices(List)} with a existing devices and providing a
     * different set of devices from the udevdb.
     * Expects:
     * - The existing devices to be removed
     * - The new devices to be added
     */
    @Test
    void testMapDevices() {
        // Setup
        Device device1 = new Device();
        Device device2 = new Device();
        testServer.getDevices().add(device1);
        testServer.getDevices().add(device2);

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(createValidPciDevice());
        udevdb.add(createValidUsbDevice());
        udevdb.add(createValidBlockDevice());

        // Execute
        HardwareDeviceSynchronizer mapper = new HardwareDeviceSynchronizer(testServer);
        Optional<String> result = mapper.mapDevices(udevdb);

        // Assertions
        assertTrue(result.isEmpty());

        Set<Device> devices = testServer.getDevices();
        assertEquals(3, devices.size());
        // Both initially associated devices were removed
        assertFalse(devices.contains(device1));
        assertFalse(devices.contains(device2));
        // And replaced by the new ones
        assertTrue(devices.stream().anyMatch(d -> "pci".equals(d.getBus()) && "ahci".equals(d.getDriver())));
        assertTrue(devices.stream().anyMatch(d -> "ata".equals(d.getBus()) && "sd".equals(d.getDriver())));
        assertTrue(devices.stream().anyMatch(d -> "usb".equals(d.getBus()) && "usb".equals(d.getDriver())));
    }

    /**
     * Tests {@link HardwareDeviceSynchronizer#mapDevices(List)} with a device having an unknown subsystem.
     * Expects the device to be skipped and not added to the server.
     */
    @Test
    void testMapDevicesSkipsUnknownSubsystems() {
        List<Map<String, Object>> udevdb = new ArrayList<>();
        Map<String, Object> device = new HashMap<>();
        device.put("P", "/devices/virtual/net/eth0");

        Map<String, Object> entries = new HashMap<>();
        entries.put("SUBSYSTEM", "net");
        entries.put("DRIVER", "e1000");
        device.put("E", entries);

        udevdb.add(device);

        HardwareDeviceSynchronizer mapper = new HardwareDeviceSynchronizer(testServer);
        Optional<String> result = mapper.mapDevices(udevdb);

        assertTrue(result.isEmpty());
        assertTrue(testServer.getDevices().isEmpty());
    }

    // dummy devices
    private Map<String, Object> createValidPciDevice() {
        Map<String, Object> device = new HashMap<>();
        device.put("P", "/devices/pci0000:00/0000:00:1f.2");

        Map<String, Object> entries = new HashMap<>();
        entries.put("SUBSYSTEM", "pci");
        entries.put("DRIVER", "ahci");
        entries.put("PCI_ID", "8086:A102");
        entries.put("PCI_CLASS", "10600");
        entries.put("PCI_SUBSYS_ID", "1028:06D6");
        entries.put("ID_VENDOR_FROM_DATABASE", "Intel Corporation");
        entries.put("ID_MODEL_FROM_DATABASE", "Q170 Chipset SATA Controller");
        device.put("E", entries);

        return device;
    }

    private Map<String, Object> createValidUsbDevice() {
        Map<String, Object> device = new HashMap<>();
        device.put("P", "/devices/pci0000:00/0000:00:14.0/usb1/1-1");

        Map<String, Object> entries = new HashMap<>();
        entries.put("SUBSYSTEM", "usb");
        entries.put("DRIVER", "usb");
        entries.put("DEVTYPE", "usb_device");
        entries.put("ID_VENDOR_ID", "046d");
        entries.put("ID_MODEL_ID", "c52b");
        entries.put("PRODUCT", "46d/c52b/1111");
        entries.put("ID_VENDOR_FROM_DATABASE", "Logitech, Inc.");
        entries.put("ID_MODEL_FROM_DATABASE", "Unifying Receiver");
        device.put("E", entries);

        return device;
    }

    private Map<String, Object> createValidBlockDevice() {
        Map<String, Object> device = new HashMap<>();
        device.put("P", "/devices/pci0000:00/0000:00:1f.2/ata1/host0/target0:0:0/0:0:0:0/block/sda");

        Map<String, Object> entries = new HashMap<>();
        entries.put("SUBSYSTEM", "block");
        entries.put("DRIVER", "sd");
        entries.put("DEVTYPE", "disk");
        entries.put("ID_BUS", "ata");
        entries.put("ID_MODEL", "SAMSUNG SSD");
        entries.put("MAJOR", "8");
        device.put("E", entries);

        return device;
    }
}
