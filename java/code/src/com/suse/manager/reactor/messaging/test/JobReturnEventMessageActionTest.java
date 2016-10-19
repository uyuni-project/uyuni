/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.reactor.messaging.test;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.server.*;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.reactor.utils.test.RhelUtilsTest;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.parser.JsonParser;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests for {@link JobReturnEventMessageAction}.
 */
public class JobReturnEventMessageActionTest extends BaseTestCaseWithUser {

    // JsonParser for parsing events from files
    public static final JsonParser<Event> EVENTS =
            new JsonParser<>(new TypeToken<Event>(){});

    /**
     * Test the processing of packages.profileupdate job return event.
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdate() throws Exception {
        // Prepare test objects: minion server, products and action
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");
        SUSEProductTestUtils.createVendorSUSEProducts();
        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("packages.profileupdate.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.doExecute(message);

        // Verify the results
        for (InstalledPackage pkg : minion.getPackages()) {
            if (pkg.getName().getName().equals("aaa_base")) {
                assertEquals("13.2+git20140911.61c1681", pkg.getEvr().getVersion());
                assertEquals("12.1", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("bash")) {
                assertEquals("4.2", pkg.getEvr().getVersion());
                assertEquals("75.2", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("timezone-java")) {
                assertEquals("2016c", pkg.getEvr().getVersion());
                assertEquals("0.37.1", pkg.getEvr().getRelease());
                assertEquals("noarch", pkg.getArch().getName());
            }

            // All packages have epoch null
            assertNull(pkg.getEvr().getEpoch());
        }
        assertEquals(3, minion.getPackages().size());

        minion.getInstalledProducts().stream().forEach(product -> {
            assertEquals("sles", product.getName().toLowerCase());
            assertEquals("sles",  product.getSUSEProduct().getName());
            assertEquals("12.1", product.getVersion());
            assertEquals("12.1",  product.getSUSEProduct().getVersion());
            assertEquals("0", product.getRelease());
            assertEquals(null, product.getSUSEProduct().getRelease());
            assertEquals("x86_64", product.getArch().getName());
            assertEquals("x86_64", product.getSUSEProduct().getArch().getName());
            assertEquals(true, product.isBaseproduct());
        });
        assertEquals(1, minion.getInstalledProducts().size());

        // Verify the action status
        assertTrue(action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServer().equals(minion))
                .findAny().get().getStatus().equals(ActionFactory.STATUS_COMPLETED));
    }

    /**
     * Test the processing of packages.profileupdate job return event
     * for RHEL7 with RES.
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdateRhel7RES() throws Exception {
        RhelUtilsTest.createResChannel(user, "7");
        // Prepare test objects: minion server, products and action
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");
        SUSEProductTestUtils.createVendorSUSEProducts();
        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("packages.profileupdate.rhel7res.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.doExecute(message);

        // Verify the results
        for (InstalledPackage pkg : minion.getPackages()) {
            if (pkg.getName().getName().equals("aaa_base")) {
                assertEquals("13.2+git20140911.61c1681", pkg.getEvr().getVersion());
                assertEquals("12.1", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("bash")) {
                assertEquals("4.2", pkg.getEvr().getVersion());
                assertEquals("75.2", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("timezone-java")) {
                assertEquals("2016c", pkg.getEvr().getVersion());
                assertEquals("0.37.1", pkg.getEvr().getRelease());
                assertEquals("noarch", pkg.getArch().getName());
            }

            // All packages have epoch null
            assertNull(pkg.getEvr().getEpoch());
        }
        assertEquals(3, minion.getPackages().size());

        assertEquals(1, minion.getInstalledProducts().size());
        minion.getInstalledProducts().stream().forEach(product -> {
            assertEquals("res", product.getName());
            assertEquals("7", product.getVersion());
            assertEquals(null, product.getRelease());
            // in the case of RES the product arch is taken from the server arch
            assertEquals("x86_64", product.getArch().getName());
        });

        // Verify the action status
        assertTrue(action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServer().equals(minion))
                .findAny().get().getStatus().equals(ActionFactory.STATUS_COMPLETED));
    }


    public void testHardwareProfileUpdateX86NoDmi()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.nodmi.x86.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());
            assertNotNull(server.getVirtualInstance());
            assertNotNull(server.getDmi());
            assertNull(server.getDmi().getSystem());
            assertNull(server.getDmi().getProduct());
            assertNull(server.getDmi().getBios());
            assertNull(server.getDmi().getVendor());
            assertTrue(!server.getDevices().isEmpty());
            assertTrue(!server.getNetworkInterfaces().isEmpty());
        });
    }

    public void testHardwareProfileUpdateX86() throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.x86.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());
            assertEquals(new Long(1), server.getCpu().getNrsocket());
            assertEquals(new Long(1), server.getCpu().getNrCPU());
            assertEquals("Intel Xeon E312xx (Sandy Bridge)", server.getCpu().getModel());
            assertEquals("3492.164", server.getCpu().getMHz());
            assertEquals("GenuineIntel", server.getCpu().getVendor());
            assertEquals("1", server.getCpu().getStepping());
            assertEquals("6", server.getCpu().getFamily());
            assertEquals("4096 KB", server.getCpu().getCache());
            assertEquals("6984.32", server.getCpu().getBogomips());
            assertEquals("fpu vme de pse tsc msr pae mce cx8 apic sep mtrr " +
                            "pge mca cmov pat pse36 clflush mmx fxsr sse sse2 ss " +
                            "syscall nx pdpe1gb rdtscp lm constant_tsc rep_good " +
                            "nopl eagerfpu pni pclmulqdq vmx ssse3 fma cx16 pcid " +
                            "sse4_1 sse4_2 x2apic movbe popcnt tsc_deadline_timer " +
                            "aes xsave avx f16c rdrand hypervisor lahf_lm abm xsaveopt " +
                            "vnmi ept fsgsbase bmi1 avx2 smep bmi2 erms invpcid",
                    server.getCpu().getFlags());
            assertEquals("42", server.getCpu().getVersion());
            assertNotNull(server.getVirtualInstance());
            assertNotNull(server.getDmi());
            assertNotNull(server.getDmi().getSystem());
            assertNotNull(server.getDmi().getProduct());
            assertNotNull(server.getDmi().getBios());
            assertNotNull(server.getDmi().getVendor());
            assertTrue(!server.getDevices().isEmpty());
            assertTrue(!server.getNetworkInterfaces().isEmpty());

            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));

            assertEquals("00:00:00:00:00:00", ethNames.get("lo").getHwaddr());
            assertEquals("52:54:00:af:7f:30", ethNames.get("eth0").getHwaddr());
            assertEquals("52:54:00:eb:51:3d", ethNames.get("eth1").getHwaddr());

            assertEquals("::1", ethNames.get("lo").getIPv6Addresses().get(0).getAddress());
            assertEquals("fe80::5054:ff:fed0:91", ethNames.get("eth0").getIPv6Addresses().get(0).getAddress());
            assertEquals("fe80::5054:ff:fefc:19a4", ethNames.get("eth1").getIPv6Addresses().get(0).getAddress());

            assertEquals("128", ethNames.get("lo").getIPv6Addresses().get(0).getNetmask());
            assertEquals("64", ethNames.get("eth0").getIPv6Addresses().get(0).getNetmask());
            assertEquals("64", ethNames.get("eth1").getIPv6Addresses().get(0).getNetmask());

            assertEquals("host", ethNames.get("lo").getIPv6Addresses().get(0).getScope());
            assertEquals("link", ethNames.get("eth0").getIPv6Addresses().get(0).getScope());
            assertEquals("link", ethNames.get("eth1").getIPv6Addresses().get(0).getScope());

            assertEquals("127.0.0.1", ethNames.get("lo").getIpaddr());
            assertEquals("192.168.121.155", ethNames.get("eth0").getIpaddr());
            assertEquals("172.24.108.98", ethNames.get("eth1").getIpaddr());

            assertEquals("255.0.0.0", ethNames.get("lo").getNetmask());
            assertEquals("255.255.255.0", ethNames.get("eth0").getNetmask());
            assertEquals("255.240.0.0", ethNames.get("eth1").getNetmask());

            assertEquals("127.255.255.255", ethNames.get("lo").getBroadcast());
            assertEquals("192.168.121.255", ethNames.get("eth0").getBroadcast());
            assertEquals("172.31.255.255", ethNames.get("eth1").getBroadcast());

            assertEquals(null, ethNames.get("lo").getModule());
            assertEquals("virtio_net", ethNames.get("eth0").getModule());
            assertEquals("virtio_net", ethNames.get("eth1").getModule());

            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("eth1").getPrimary());
        });
    }

    public void testHardwareProfileUpdateX86LongCPUValues()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.cpulongval.x86.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());

            assertEquals(32, server.getCpu().getModel().length());
            assertEquals(16, server.getCpu().getMHz().length());
            assertEquals(32, server.getCpu().getVendor().length());
            assertEquals(16, server.getCpu().getStepping().length());
            assertEquals(32, server.getCpu().getFamily().length());
            assertEquals(16, server.getCpu().getCache().length());
            assertEquals(16, server.getCpu().getBogomips().length());
            assertEquals(32, server.getCpu().getVersion().length());
            assertEquals(2048, server.getCpu().getFlags().length());
        });
    }

    public void testHardwareProfileUpdatePPC64()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.ppc64.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());

            server = MinionServerFactory.findByMinionId(server.getMinionId()).orElse(null);
            assertEquals("CHRP IBM pSeries (emulated by qe", server.getCpu().getVendor());
            assertEquals("POWER8E (raw), altivec supported", server.getCpu().getModel());
            assertEquals(null, server.getCpu().getBogomips());
            assertEquals("3425.000000", server.getCpu().getMHz());

            assertNull(server.getDmi());
            assertTrue(!server.getDevices().isEmpty());

            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));

            assertEquals("00:00:00:00:00:00", ethNames.get("lo").getHwaddr());
            assertEquals("52:54:00:d7:4f:20", ethNames.get("eth0").getHwaddr());

            assertEquals(1, ethNames.get("eth0").getIPv6Addresses().size());
            assertEquals("::1", ethNames.get("lo").getIPv6Addresses().get(0).getAddress());
            assertEquals("2620:113:80c0:8000:10:161:25:49", ethNames.get("eth0").getIPv6Addresses().get(0).getAddress());

            assertEquals("128", ethNames.get("lo").getIPv6Addresses().get(0).getNetmask());
            assertEquals("64", ethNames.get("eth0").getIPv6Addresses().get(0).getNetmask());

            assertEquals("host", ethNames.get("lo").getIPv6Addresses().get(0).getScope());
            assertEquals("global", ethNames.get("eth0").getIPv6Addresses().get(0).getScope());

            assertEquals("127.0.0.1", ethNames.get("lo").getIpaddr());
            assertEquals("10.161.25.49", ethNames.get("eth0").getIpaddr());

            assertEquals("255.0.0.0", ethNames.get("lo").getNetmask());
            assertEquals("255.255.192.0", ethNames.get("eth0").getNetmask());

            assertEquals(null, ethNames.get("lo").getBroadcast());
            assertEquals("10.161.63.255", ethNames.get("eth0").getBroadcast());

            assertEquals(null, ethNames.get("lo").getModule());
            assertEquals("ibmveth", ethNames.get("eth0").getModule());

            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals("Y", ethNames.get("eth0").getPrimary());
        });
    }


    public void testHardwareProfileUpdateScsiDevices()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.scsi.x86.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());

            assertEquals(1, server.getDevices().stream().filter(d -> "HD".equals(d.getDeviceClass()) && "scsi".equals(d.getBus())).count());
            assertEquals(1, server.getDevices().stream().filter(d -> "CDROM".equals(d.getDeviceClass()) && "ata".equals(d.getBus())).count());
            assertEquals(1, server.getDevices().stream().filter(d -> "scsi".equals(d.getBus())).count());
        });
    }

    public void testHardwareProfileUpdatePrimaryIPv4Only()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv4only.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("eth1").getPrimary());

        });
    }

    public void testHardwareProfileUpdatetPrimaryIPv6Only()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv6only.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals("Y", ethNames.get("eth0").getPrimary());
            assertEquals(null, ethNames.get("eth1").getPrimary());

        });
    }

    public void testHardwareProfileUpdatetPrimaryIPV4IPv6()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv4ipv6.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("eth1").getPrimary());
        });
    }

    public void testHardwareProfileUpdateS390() throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.s390.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());

            assertNull(server.getCpu().getNrsocket());
            assertEquals(new Long(0), server.getCpu().getNrCPU());
            assertEquals("s390x", server.getCpu().getModel());
            assertEquals("0", server.getCpu().getMHz());
            assertEquals("IBM/S390", server.getCpu().getVendor());
            assertNull(server.getCpu().getStepping());
            assertNull(server.getCpu().getFamily());
            assertNull(server.getCpu().getCache());
            assertEquals("2913.00", server.getCpu().getBogomips());
            assertEquals("esan3 zarch stfle msa ldisp eimm dfp etf3eh highgprs",
                    server.getCpu().getFlags());
            assertNull(server.getCpu().getVersion());

            assertNotNull(server.getVirtualInstance());
            assertNotNull(server.getVirtualInstance().getHostSystem());
            assertEquals("z/OS", server.getVirtualInstance().getHostSystem().getOs());
            assertEquals("IBM Mainframe 2827 0000000000069A27", server.getVirtualInstance().getHostSystem().getName());
            assertEquals(new Long(45), server.getVirtualInstance().getHostSystem().getCpu().getNrCPU());
            assertEquals(new Long(45), server.getVirtualInstance().getHostSystem().getCpu().getNrsocket());

            assertEquals(VirtualInstanceFactory.getInstance().getFullyVirtType(),
                    server.getVirtualInstance().getType());
            assertNotNull(server.getVirtualInstance().getUuid());
            assertEquals(VirtualInstanceFactory.getInstance().getUnknownState(),
                    server.getVirtualInstance().getState());
            assertEquals(new Long(1),
                    server.getVirtualInstance().getConfirmed());
            assertNull(server.getDmi());

            assertTrue(!server.getDevices().isEmpty());
            assertTrue(!server.getNetworkInterfaces().isEmpty());

            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));

            assertEquals("00:00:00:00:00:00", ethNames.get("lo").getHwaddr());
            assertEquals("02:00:00:00:42:8e", ethNames.get("eth0").getHwaddr());

            assertEquals("::1", ethNames.get("lo").getIPv6Addresses().get(0).getAddress());
            assertEquals("fe80::ff:fe00:428e", ethNames.get("eth0").getIPv6Addresses().get(0).getAddress());

            assertEquals("128", ethNames.get("lo").getIPv6Addresses().get(0).getNetmask());
            assertEquals("64", ethNames.get("eth0").getIPv6Addresses().get(0).getNetmask());

            assertEquals("host", ethNames.get("lo").getIPv6Addresses().get(0).getScope());
            assertEquals("link", ethNames.get("eth0").getIPv6Addresses().get(0).getScope());

            assertEquals("127.0.0.1", ethNames.get("lo").getIpaddr());
            assertEquals("10.161.155.142", ethNames.get("eth0").getIpaddr());

            assertEquals("255.0.0.0", ethNames.get("lo").getNetmask());
            assertEquals("255.255.240.0", ethNames.get("eth0").getNetmask());

            assertEquals("127.255.255.255", ethNames.get("lo").getBroadcast());
            assertEquals(null, ethNames.get("eth0").getBroadcast());

            assertEquals(null, ethNames.get("lo").getModule());
            assertEquals("qeth", ethNames.get("eth0").getModule());

            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals("Y", ethNames.get("eth0").getPrimary());

        });
    }


    private void testHardwareProfileUpdate(String jsonFile, Consumer<MinionServer> assertions) throws Exception{
        // Prepare test objects: minion server and action
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("minionsles12-suma3pg.vagrant.local");

        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(server, action));

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent(jsonFile, action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.doExecute(message);

        assertions.accept(server);
    }


    /**
     * Read a Salt job return event while substituting the corresponding action id.
     *
     * @param filename the filename to read from
     * @param actionId the id of the action to correlate this Salt job with
     * @return event object parsed from the json file
     */
    private Event getJobReturnEvent(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        return EVENTS.parse(eventString);
    }

    public void testUpdateServerAction() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("abcdefg.vagrant.local");
        SUSEProductTestUtils.createVendorSUSEProducts();

        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Arrays.asList(minion.getId()),
                Arrays.asList(ApplyStatesEventMessage.CHANNELS),
                new Date());

        ServerAction sa = ActionFactoryTest.createServerAction(minion, action);

        action.addServerAction(sa);

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("state.apply.with.failures.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.doExecute(message);

        assertEquals(
            Arrays.asList(sa),
            action.getServerActions().stream().filter(
                serverAction -> serverAction.getServer().equals(minion)
            ).collect(java.util.stream.Collectors.toList()));

        // Verify the action status
        assertTrue(sa.getStatus().equals(ActionFactory.STATUS_FAILED));
    }
}
