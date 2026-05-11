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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.server.Device;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Tests for {@link DeviceMapper}
 */
class DeviceMapperTest {

    @Test
    void testMapDeviceReturnsNullForEmptySubsystem() {
        Device result = new DeviceMapper().mapDevice(new ArrayList<>(), new HashMap<>());

        assertNull(result);
    }

    @Test
    void testMapDeviceReturnsNullForUnsupportedSubsystem() {
        Map<String, Object> dbdev = new HashMap<>();

        Map<String, Object> entries = new HashMap<>();
        entries.put("SUBSYSTEM", "dummy");
        dbdev.put("E", entries);

        Device result = new DeviceMapper().mapDevice(new ArrayList<>(), dbdev);

        assertNull(result);
    }

    /**
     * Tests {@link DeviceMapper#mapDevice(List, Map)} for a PCI device with complete information
     */
    @Test
    void testMapPciDevice() {
        Map<String, Object> dbdev = new HashMap<>();
        dbdev.put("P", "/devices/pci0000:00/0000:00:1f.2");

        Map<String, Object> entries = new HashMap<>();
        entries.put("SUBSYSTEM", "pci");
        entries.put("DRIVER", "sym53c8xx");
        entries.put("PCI_ID", "1000:2000");
        entries.put("PCI_CLASS", "10000");
        entries.put("PCI_SUBSYS_ID", "3000:4000");
        entries.put("ID_VENDOR_FROM_DATABASE", "LSI Logic / Symbios Logic");
        entries.put("ID_MODEL_FROM_DATABASE", "53c895a");
        dbdev.put("E", entries);

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("pci", device.getBus());
        assertEquals(1L, device.getPcitype());
        assertEquals("sym53c8xx", device.getDriver());
        assertEquals("1000", device.getProp1());
        assertEquals("2000", device.getProp2());
        assertEquals("3000", device.getProp3());
        assertEquals("4000", device.getProp4());
        assertEquals("LSI Logic / Symbios Logic|53c895a", device.getDescription());
        assertEquals(Device.CLASS_SCSI, device.getDeviceClass());
    }

    /**
     * Tests {@link DeviceMapper#mapDevice(List, Map)} for a PCI device without:
     * - vendor/model information in the database, it should use the PCI_ID for description
     * - PCI_CLASS, it should default to Device.CLASS_OTHER
     * - driver information, it should default to "unknown"
     */
    @Test
    void testMapPciDeviceWithMissingInfo() {
        Map<String, Object> dbdev = new HashMap<>();
        dbdev.put("P", "/devices/pci0000:00/0000:00:1f.2");

        Map<String, Object> entries = new HashMap<>();
        entries.put("SUBSYSTEM", "pci");
        entries.put("PCI_ID", "1000:2000");
        entries.put("PCI_SUBSYS_ID", "3000:4000");
        dbdev.put("E", entries);


        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("1000|2000", device.getDescription());
        assertEquals(Device.CLASS_OTHER, device.getDeviceClass());
        assertEquals("unknown", device.getDriver());
    }

    @Test
    void testMapUsbDevice() {
        Map<String, Object> dbdev = createUsbDeviceData();
        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("usb", device.getBus());
        assertEquals(-1L, device.getPcitype());
        assertEquals("usb", device.getDriver());
        assertEquals("046d", device.getProp1());
        assertEquals("c52b", device.getProp2());
        assertEquals("Logitech, Inc.|Unifying Receiver", device.getDescription());
        assertEquals(Device.CLASS_OTHER, device.getDeviceClass());
    }

    @Test
    void testMapBlockDevice() {
        Map<String, Object> dbdev = createBlockDeviceData();
        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("ata", device.getBus());
        assertEquals("sda", device.getDevice());
        assertEquals(Device.CLASS_HD, device.getDeviceClass());
        assertEquals("SAMSUNG SSD", device.getDescription());
    }

    /**
     * Tests that a block device with DEVTYPE=partition is skipped
     */
    @Test
    void testMapBlockDeviceSkipsPartition() {
        Map<String, Object> dbdev = createBlockDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("DEVTYPE", "partition");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNull(device);
    }

    /**
     * Tests that a block device with a DM_NAME is skipped
     */
    @Test
    void testMapBlockDeviceSkipsLvm() {
        Map<String, Object> dbdev = createBlockDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("DM_NAME", "vg0-lv0");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNull(device);
    }

    /**
     * Tests that a block device with MAJOR=1 (RAM disk) is skipped
     */
    @Test
    void testMapBlockDeviceSkipsRamDevice() {
        Map<String, Object> dbdev = createBlockDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("MAJOR", "1");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNull(device);
    }

    /**
     * Tests that a block device with MAJOR=7 (virtual terminal) is skipped
     */
    @Test
    void testMapBlockDeviceSkipsVtDevice() {
        Map<String, Object> dbdev = createBlockDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("MAJOR", "7");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNull(device);
    }

    /**
     * Tests that a SCSI device with a block device with the same path is skipped, to avoid duplicates
     */
    @Test
    void testMapScsiDeviceSkipsWhenBlockDeviceExists() {
        Map<String, Object> scsiDev = createScsiDeviceData();
        Map<String, Object> blockDev = createBlockDeviceData();
        blockDev.put("P", "/devices/pci0000:00/0000:00:1f.2/ata1/host0/target0:0:0/0:0:0:0/block/sda");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(scsiDev);
        udevdb.add(blockDev);

        Device device = new DeviceMapper().mapDevice(udevdb, scsiDev);

        assertNull(device);
    }

    @Test
    void testMapScsiDevice() {
        Map<String, Object> dbdev = createScsiDeviceData();
        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("scsi", device.getBus());
        assertEquals("0", device.getProp1());
        assertEquals("0", device.getProp2());
        assertEquals("0", device.getProp3());
        assertEquals("0", device.getProp4());
    }

    @Test
    void testMapScsiDeviceSkipsNonScsiDevice() {
        Map<String, Object> dbdev = createScsiDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("DEVTYPE", "scsi_host");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNull(device);
    }

    @Test
    void testGetDeviceDescForUsbWithoutDatabase() {
        Map<String, Object> dbdev = createUsbDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.remove("ID_VENDOR_FROM_DATABASE");
        entries.remove("ID_MODEL_FROM_DATABASE");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("046d|c52b", device.getDescription());
    }

    @Test
    void testGetDeviceDescForUsbInterface() {
        Map<String, Object> dbdev = createUsbDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.remove("ID_VENDOR_ID");
        entries.put("DEVTYPE", "usb_interface");
        entries.put("DRIVER", "usbhid");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("USB HID Interface", device.getDescription());
    }

    @Test
    void testGetDeviceDescForUsbHub() {
        Map<String, Object> dbdev = createUsbDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.remove("ID_VENDOR_ID");
        entries.put("DEVTYPE", "usb_interface");
        entries.put("DRIVER", "hub");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("USB Hub Interface", device.getDescription());
    }

    @Test
    void testGetDeviceDescForUsbDeviceWithProductField() {
        Map<String, Object> dbdev = createUsbDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.remove("ID_VENDOR_ID");
        entries.remove("ID_MODEL_ID");
        entries.remove("ID_VENDOR_FROM_DATABASE");
        entries.remove("ID_MODEL_FROM_DATABASE");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals("046d|c52b", device.getDescription());
    }

    @ParameterizedTest
    @MethodSource("pciClassData")
    void testParsePciBaseClass(String pciClass, String expectedBase, String expectedSub, String expectedDevice) {
        DeviceMapper mapper = new DeviceMapper();
        assertEquals(expectedBase, mapper.parsePciBaseClass(pciClass));
        assertEquals(expectedSub, mapper.parsePciSubClass(pciClass));
        assertEquals(expectedDevice, mapper.classifyPciDevices(pciClass));
    }

    private static Stream<Arguments> pciClassData() {
        return Stream.of(
                // null/invalid cases
                Arguments.of(null, null, null, null),
                Arguments.of("", null, null, null),
                Arguments.of("Z0000", "Z", "00", null),
                // not matching cases
                Arguments.of("10600", "1", "06", null), // it is a storage but doesn't have a default
                Arguments.of("20600", "2", "06", Device.CLASS_OTHER), // all network pci have a default
                // expected cases
                // Storage
                Arguments.of("10000", "1", "00", Device.CLASS_SCSI), // PCI_CLASS_STORAGE_SCSI
                Arguments.of("10100", "1", "01", Device.CLASS_IDE), // PCI_CLASS_STORAGE_IDE
                Arguments.of("10200", "1", "02", Device.CLASS_FLOPPY), // PCI_CLASS_STORAGE_FLOPPY
                Arguments.of("10300", "1", "03", null), // PCI_CLASS_STORAGE_IPI
                Arguments.of("10400", "1", "04", Device.CLASS_RAID), // PCI_CLASS_STORAGE_RAID
                Arguments.of("18000", "1", "80", null), // PCI_CLASS_STORAGE_OTHER
                // Network
                Arguments.of("20000", "2", "00", Device.CLASS_OTHER), // PCI_CLASS_NETWORK_ETHERNET
                Arguments.of("20100", "2", "01", Device.CLASS_OTHER), // PCI_CLASS_NETWORK_TOKEN_RING
                Arguments.of("20200", "2", "02", Device.CLASS_OTHER), // PCI_CLASS_NETWORK_FDDI
                Arguments.of("20300", "2", "03", Device.CLASS_OTHER), // PCI_CLASS_NETWORK_ATM
                Arguments.of("28000", "2", "80", Device.CLASS_OTHER), // PCI_CLASS_NETWORK_OTHER
                // Display
                Arguments.of("30000", "3", "00", Device.CLASS_VIDEO), // PCI_CLASS_DISPLAY_VGA
                Arguments.of("30100", "3", "01", Device.CLASS_VIDEO), // PCI_CLASS_DISPLAY_XGA
                Arguments.of("30200", "3", "02", Device.CLASS_VIDEO), // PCI_CLASS_DISPLAY_3D
                Arguments.of("38000", "3", "80", Device.CLASS_VIDEO), // PCI_CLASS_DISPLAY_OTHER
                // Multimedia
                Arguments.of("40000", "4", "00", Device.CLASS_CAPTURE), // PCI_CLASS_MULTIMEDIA_VIDEO
                Arguments.of("40100", "4", "01", Device.CLASS_AUDIO), // PCI_CLASS_MULTIMEDIA_AUDIO
                Arguments.of("40200", "4", "02", null), // PCI_CLASS_MULTIMEDIA_PHONE
                Arguments.of("48000", "4", "80", null), // PCI_CLASS_MULTIMEDIA_OTHER
                // Bridge
                Arguments.of("60000", "6", "00", null), // PCI_CLASS_BRIDGE_HOST
                Arguments.of("60100", "6", "01", null), // PCI_CLASS_BRIDGE_ISA
                Arguments.of("60200", "6", "02", null), // PCI_CLASS_BRIDGE_EISA
                Arguments.of("60300", "6", "03", null), // PCI_CLASS_BRIDGE_MC
                Arguments.of("60400", "6", "04", null), // PCI_CLASS_BRIDGE_PCI
                Arguments.of("60500", "6", "05", Device.CLASS_SOCKET), // PCI_CLASS_BRIDGE_PCMCIA
                Arguments.of("60600", "6", "06", null), // PCI_CLASS_BRIDGE_NUBUS
                Arguments.of("60700", "6", "07", Device.CLASS_SOCKET), // PCI_CLASS_BRIDGE_CARDBUS
                Arguments.of("60800", "6", "08", null), // PCI_CLASS_BRIDGE_RACEWAY
                Arguments.of("68000", "6", "80", null), // PCI_CLASS_BRIDGE_OTHER
                // Communication
                Arguments.of("70000", "7", "00", null), // PCI_CLASS_COMMUNICATION_SERIAL
                Arguments.of("70100", "7", "01", null), // PCI_CLASS_COMMUNICATION_PARALLEL
                Arguments.of("70200", "7", "02", null), // PCI_CLASS_COMMUNICATION_MULTISERIAL
                Arguments.of("70300", "7", "03", Device.CLASS_MODEM), // PCI_CLASS_COMMUNICATION_MODEM
                Arguments.of("78000", "7", "80", null), // PCI_CLASS_COMMUNICATION_OTHER
                // Input
                Arguments.of("90000", "9", "00", Device.CLASS_KEYBOARD), // PCI_CLASS_INPUT_KEYBOARD
                Arguments.of("90100", "9", "01", null), // PCI_CLASS_INPUT_PEN
                Arguments.of("90200", "9", "02", Device.CLASS_MOUSE), // PCI_CLASS_INPUT_MOUSE
                Arguments.of("90300", "9", "03", Device.CLASS_SCANNER), // PCI_CLASS_INPUT_SCANNER
                Arguments.of("90400", "9", "04", null), // PCI_CLASS_INPUT_GAMEPORT
                Arguments.of("98000", "9", "80", null), // PCI_CLASS_INPUT_OTHER
                // Serial
                Arguments.of("C0000", "C", "00", Device.CLASS_FIREWIRE), // PCI_CLASS_SERIAL_FIREWIRE
                Arguments.of("C0100", "C", "01", null), // PCI_CLASS_SERIAL_ACCESS
                Arguments.of("C0200", "C", "02", null), // PCI_CLASS_SERIAL_SSA
                Arguments.of("C0300", "C", "03", Device.CLASS_USB), // PCI_CLASS_SERIAL_USB
                Arguments.of("C0400", "C", "04", null), // PCI_CLASS_SERIAL_FIBER
                Arguments.of("C0500", "C", "05", null) // PCI_CLASS_SERIAL_SMBUS
        );
    }

    // Tests targeting SCSI device types
    @Test
    void testClassifyScsiDisk() {
        Map<String, Object> dbdev = createScsiDeviceData();
        Map<String, Object> extraEntries = new HashMap<>();
        extraEntries.put("SCSI_SYS_TYPE", 0L);
        dbdev.put("X-Mgr", extraEntries);

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_HD, device.getDeviceClass());
    }

    @Test
    void testClassifyScsiRbc() {
        Map<String, Object> dbdev = createScsiDeviceData();
        Map<String, Object> extraEntries = new HashMap<>();
        extraEntries.put("SCSI_SYS_TYPE", 14L);
        dbdev.put("X-Mgr", extraEntries);

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_HD, device.getDeviceClass());
    }

    @Test
    void testClassifyScsiTape() {
        Map<String, Object> dbdev = createScsiDeviceData();
        Map<String, Object> extraEntries = new HashMap<>();
        extraEntries.put("SCSI_SYS_TYPE", 1L);
        dbdev.put("X-Mgr", extraEntries);

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_TAPE, device.getDeviceClass());
    }

    @Test
    void testClassifyScsiCdrom() {
        Map<String, Object> dbdev = createScsiDeviceData();
        Map<String, Object> extraEntries = new HashMap<>();
        extraEntries.put("SCSI_SYS_TYPE", 5L);
        dbdev.put("X-Mgr", extraEntries);

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_CDROM, device.getDeviceClass());
    }

    @Test
    void testClassifyBlockCdrom() {
        Map<String, Object> dbdev = createBlockDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("ID_CDROM", "1");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_CDROM, device.getDeviceClass());
    }

    @Test
    void testClassifyBlockCdromByType() {
        Map<String, Object> dbdev = createBlockDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("ID_TYPE", "cd");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_CDROM, device.getDeviceClass());
    }

    @Test
    void testClassifyKeyboardBySerial() {
        Map<String, Object> dbdev = createUsbDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("ID_SERIAL", "Logitech_USB_Keyboard");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_KEYBOARD, device.getDeviceClass());
    }

    @Test
    void testClassifyMouseBySerial() {
        Map<String, Object> dbdev = createUsbDeviceData();
        Map<String, Object> entries = (Map<String, Object>) dbdev.get("E");
        entries.put("ID_SERIAL", "Logitech_USB_Mouse");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_MOUSE, device.getDeviceClass());
    }

    @Test
    void testClassifyPrinterBySyspath() {
        Map<String, Object> dbdev = createUsbDeviceData();
        dbdev.put("P", "/devices/pci0000:00/usb1/1-1/lp0");

        List<Map<String, Object>> udevdb = new ArrayList<>();
        udevdb.add(dbdev);

        Device device = new DeviceMapper().mapDevice(udevdb, dbdev);

        assertNotNull(device);
        assertEquals(Device.CLASS_PRINTER, device.getDeviceClass());
    }

    // Auxiliary methods to create base devices

    private Map<String, Object> createUsbDeviceData() {
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

    private Map<String, Object> createBlockDeviceData() {
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

    private Map<String, Object> createScsiDeviceData() {
        Map<String, Object> device = new HashMap<>();
        device.put("P", "/devices/pci0000:00/0000:00:1f.2/ata1/host0/target0:0:0/0:0:0:0");

        Map<String, Object> entries = new HashMap<>();
        entries.put("SUBSYSTEM", "scsi");
        entries.put("DRIVER", "sd");
        entries.put("DEVTYPE", "scsi_device");
        entries.put("ID_BUS", "scsi");
        entries.put("ID_PATH", "pci-0000:00:1f.2-scsi-0:0:0:0");
        entries.put("DEVPATH", "/devices/pci0000:00/0000:00:1f.2/ata1/host0/target0:0:0/0:0:0:0/block/sda");
        device.put("E", entries);

        return device;
    }
}
