package com.suse.manager.reactor.messaging.test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.reactor.messaging.RefreshHardwareEventMessage;
import com.suse.manager.reactor.messaging.RefreshHardwareEventMessageAction;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.manager.webui.utils.salt.custom.Udevdb;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Network;
import com.suse.salt.netapi.calls.modules.Smbios;
import com.suse.salt.netapi.calls.modules.Status;
import org.apache.commons.io.IOUtils;
import org.jmock.Mock;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Test for RefreshHardwareEventMessageAction.
 */
public class RefreshHardwareEventMessageActionTest extends JMockBaseTestCaseWithUser {

    private Gson gson = new Gson();

    public void testDmiRuntimeException() throws Exception {
        doTest(new RuntimeException("test exception"),
            (server, action) -> {
                assertNotNull(server);
                assertNotNull(server.getCpu());
                assertNotNull(server.getVirtualInstance());
                assertNull(server.getDmi()); // getDmiRecords() threw exception so it was not populated
                assertTrue(!server.getNetworkInterfaces().isEmpty());
                assertTrue(!server.getDevices().isEmpty());
                ServerAction serverAction = action.getServerActions().stream().findFirst().get();
                assertEquals(new Long(-1L), serverAction.getResultCode());
                assertThat(serverAction.getResultMsg(),
                    stringContains("DMI: An error occurred: test exception")
                );
            });
    }

    public void testDmiJsonSyntaxException() throws Exception {
        doTest(new JsonSyntaxException("test exception"),
            (server, action) -> {
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
                ServerAction serverAction = action.getServerActions().stream().findFirst().get();
                assertEquals(new Long(-1L), serverAction.getResultCode());
                assertThat(serverAction.getResultMsg(),
                    stringContains("DMI: Could not retrieve DMI records: test exception")
                );
            });
    }

    public void testRefreshHardware() throws Exception {
        doTest(null,
                (server, action) -> {
                    assertNotNull(server);
                    assertNotNull(server.getCpu());
                    assertEquals("fpu vme de pse tsc msr pae mce cx8 apic sep mtrr " +
                            "pge mca cmov pat pse36 clflush mmx fxsr sse sse2 ss " +
                            "syscall nx pdpe1gb rdtscp lm constant_tsc rep_good " +
                            "nopl eagerfpu pni pclmulqdq vmx ssse3 fma cx16 pcid " +
                            "sse4_1 sse4_2 x2apic movbe popcnt tsc_deadline_timer " +
                            "aes xsave avx f16c rdrand hypervisor lahf_lm abm xsaveopt " +
                            "vnmi ept fsgsbase bmi1 avx2 smep bmi2 erms invpcid",
                            server.getCpu().getFlags());
                    assertNotNull(server.getVirtualInstance());
                    assertNotNull(server.getDmi());
                    assertNotNull(server.getDmi().getSystem());
                    assertNotNull(server.getDmi().getProduct());
                    assertNotNull(server.getDmi().getBios());
                    assertNotNull(server.getDmi().getVendor());
                    assertTrue(!server.getDevices().isEmpty());
                    assertTrue(!server.getNetworkInterfaces().isEmpty());
                    ServerAction serverAction = action.getServerActions().stream().findFirst().get();
                    assertEquals(new Long(0L), serverAction.getResultCode());
                    assertEquals(serverAction.getResultMsg(), "hardware list refreshed");
                    assertEquals(serverAction.getStatus(), ActionFactory.STATUS_COMPLETED);
                });
    }

    public void testPPC64() throws Exception {
        MinionServer server = (MinionServer) ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        String minionId = server.getMinionId();

        Mock apiMock = mock(SaltService.class);

        apiMock.stubs().method("getFileContent").with(isA(String.class), isA(String.class)).will(returnValue(Optional.empty()));

        Map<String, Object> grains = parse("grains.items.ppc64", Grains.items(false).getReturnType());
        apiMock.stubs().method("getGrains").with(eq(minionId)).will(returnValue(Optional.of(grains)));

        List<Map<String, Object>> udevdb = parse("udevdb.exportdb.ppc64", Udevdb.exportdb().getReturnType());
        apiMock.stubs().method("getUdevdb").with(eq(minionId)).will(returnValue(Optional.of(udevdb)));

        Map<String, Object> cpuinfo = parse("status.cpuinfo.ppc64", Status.cpuinfo().getReturnType());
        apiMock.stubs().method("getCpuInfo").with(eq(minionId)).will(returnValue(Optional.of(cpuinfo)));

        Map<String, Network.Interface> netif = parse("network.interfaces", Network.interfaces().getReturnType());
        apiMock.stubs().method("getNetworkInterfacesInfo").with(eq(minionId)).will(returnValue(Optional.of(netif)));

        Map<SumaUtil.IPVersion, SumaUtil.IPRoute> ips = parse("sumautil.primary_ips", SumaUtil.primaryIps().getReturnType());
        apiMock.stubs().method("getPrimaryIps").with(eq(minionId)).will(returnValue(Optional.of(ips)));

        Map<String, String> netmodules = parse("sumautil.get_net_modules", SumaUtil.getNetModules().getReturnType());
        apiMock.stubs().method("getNetModules").with(eq(minionId)).will(returnValue(Optional.of(netmodules)));

        Map<String, Boolean> ping = new HashMap<>();
        ping.put(minionId, true);
        apiMock.stubs().method("ping").will(returnValue(ping));

        RefreshHardwareEventMessageAction action = new RefreshHardwareEventMessageAction((SaltService)apiMock.proxy());

        Action scheduledAction = ActionManager.scheduleHardwareRefreshAction(server.getOrg(), server, new Date());
        RefreshHardwareEventMessage msg = new RefreshHardwareEventMessage(minionId, scheduledAction);
        action.execute(msg);

        this.commitHappened(); // force cleanup on tearDown()

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ServerAction serverAction = scheduledAction.getServerActions().stream().findFirst().get();
        assertEquals(serverAction.getResultMsg(), "hardware list refreshed");
        assertEquals(serverAction.getStatus(), ActionFactory.STATUS_COMPLETED);

        server = MinionServerFactory.findByMinionId(minionId).orElse(null);
        assertEquals("CHRP IBM pSeries (emulated by qe", server.getCpu().getVendor());
    }

    private void doTest(Exception exception, BiConsumer<MinionServer, Action> assertions) throws Exception {
        MinionServer server = (MinionServer) ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        String minionId = server.getMinionId();

        Mock apiMock = mock(SaltService.class);
        if (exception != null) {
            apiMock.stubs().method("getDmiRecords").will(throwException(exception));
        } else {
            List<Smbios.Record> smbiosSystem = parse("smbios.records.system",
                    Smbios.records(Smbios.RecordType.SYSTEM).getReturnType());
            apiMock.stubs().method("getDmiRecords").with(eq(minionId), eq(Smbios.RecordType.SYSTEM)).
                    will(returnValue(Optional.of(smbiosSystem.get(0).getData())));

            List<Smbios.Record> smbiosBios = parse("smbios.records.bios",
                    Smbios.records(Smbios.RecordType.BIOS).getReturnType());
            apiMock.stubs().method("getDmiRecords").with(eq(minionId), eq(Smbios.RecordType.BIOS)).
                    will(returnValue(Optional.of(smbiosBios.get(0).getData())));

            List<Smbios.Record> smbiosChassis = parse("smbios.records.chassis",
                    Smbios.records(Smbios.RecordType.CHASSIS).getReturnType());
            apiMock.stubs().method("getDmiRecords").with(eq(minionId), eq(Smbios.RecordType.CHASSIS)).
                    will(returnValue(Optional.of(smbiosChassis.get(0).getData())));

            List<Smbios.Record> smbiosBaseboard = parse("smbios.records.chassis",
                    Smbios.records(Smbios.RecordType.BASEBOARD).getReturnType());
            apiMock.stubs().method("getDmiRecords").with(eq(minionId), eq(Smbios.RecordType.BASEBOARD)).
                    will(returnValue(Optional.of(smbiosBaseboard.get(0).getData())));
        }

        Map<String, Object> grains = parse("grains.items", Grains.items(false).getReturnType());
        apiMock.stubs().method("getGrains").with(eq(minionId)).will(returnValue(Optional.of(grains)));

        List<Map<String, Object>> udevdb = parse("udevdb.exportdb", Udevdb.exportdb().getReturnType());
        apiMock.stubs().method("getUdevdb").with(eq(minionId)).will(returnValue(Optional.of(udevdb)));

        Map<String, Object> cpuinfo = parse("status.cpuinfo", Status.cpuinfo().getReturnType());
        apiMock.stubs().method("getCpuInfo").with(eq(minionId)).will(returnValue(Optional.of(cpuinfo)));

        Map<String, Network.Interface> netif = parse("network.interfaces", Network.interfaces().getReturnType());
        apiMock.stubs().method("getNetworkInterfacesInfo").with(eq(minionId)).will(returnValue(Optional.of(netif)));

        Map<SumaUtil.IPVersion, SumaUtil.IPRoute> ips = parse("sumautil.primary_ips", SumaUtil.primaryIps().getReturnType());
        apiMock.stubs().method("getPrimaryIps").with(eq(minionId)).will(returnValue(Optional.of(ips)));

        Map<String, String> netmodules = parse("sumautil.get_net_modules", SumaUtil.getNetModules().getReturnType());
        apiMock.stubs().method("getNetModules").with(eq(minionId)).will(returnValue(Optional.of(netmodules)));

        Map<String, Boolean> ping = new HashMap<>();
        ping.put(minionId, true);
        apiMock.stubs().method("ping").will(returnValue(ping));

        RefreshHardwareEventMessageAction action = new RefreshHardwareEventMessageAction((SaltService)apiMock.proxy());

        Action scheduledAction = ActionManager.scheduleHardwareRefreshAction(server.getOrg(), server, new Date());
        RefreshHardwareEventMessage msg = new RefreshHardwareEventMessage(minionId, scheduledAction);
        action.execute(msg);

        this.commitHappened(); // force cleanup on tearDown()

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        server = MinionServerFactory.findByMinionId(minionId).orElse(null);

        assertions.accept(server, scheduledAction);
    }

    private <T> T parse(String name, TypeToken<T> returnType) throws IOException {
        String str = IOUtils.toString(getClass().getResourceAsStream(name + ".json"));
        return gson.fromJson(str, returnType.getType());
    }

}
