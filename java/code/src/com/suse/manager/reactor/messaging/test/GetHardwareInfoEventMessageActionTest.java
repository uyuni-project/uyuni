package com.suse.manager.reactor.messaging.test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.reactor.messaging.GetHardwareInfoEventMessage;
import com.suse.manager.reactor.messaging.GetHardwareInfoEventMessageAction;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.utils.salt.custom.Udevdb;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Status;
import org.apache.commons.io.IOUtils;
import org.jmock.Mock;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Test for GetHardwareInfoEventMessageAction.
 */
public class GetHardwareInfoEventMessageActionTest extends JMockBaseTestCaseWithUser {

    private Gson gson = new Gson();

    public void testRuntimeException() throws Exception {

        MinionServer server = doTest(new RuntimeException("test exception"));

        assertNotNull(server);
        assertNotNull(server.getCpu());
        assertNotNull(server.getVirtualInstance());
        assertNull(server.getDmi()); // getDmiRecords() threw exception so it was not populated
        assertTrue(!server.getDevices().isEmpty());

    }

    public void testJsonSyntaxException() throws Exception {

        MinionServer server = doTest(new JsonSyntaxException("test exception"));

        assertNotNull(server);
        assertNotNull(server.getCpu());
        assertNotNull(server.getVirtualInstance());
        assertNotNull(server.getDmi());
        assertNull(server.getDmi().getSystem());
        assertNull(server.getDmi().getProduct());
        assertNull(server.getDmi().getBios());
        assertNull(server.getDmi().getVendor());
        assertTrue(!server.getDevices().isEmpty());

    }

    private MinionServer doTest(Exception exception) throws Exception {
        MinionServer server = (MinionServer) ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        String minionId = server.getMinionId();

        Mock apiMock = mock(SaltService.class);
        apiMock.stubs().method("getDmiRecords").will(throwException(exception));

        Map<String, Object> grains = parse("grains.items", Grains.items(false).getReturnType());
        apiMock.stubs().method("getGrains").with(eq(minionId)).will(returnValue(grains));

        List<Map<String, Object>> udevdb = parse("udevdb.exportdb", Udevdb.exportdb().getReturnType());
        apiMock.stubs().method("getUdevdb").with(eq(minionId)).will(returnValue(udevdb));

        Map<String, Object> cpuinfo = parse("status.cpuinfo", Status.cpuinfo().getReturnType());
        apiMock.stubs().method("getCpuInfo").with(eq(minionId)).will(returnValue(cpuinfo));

        GetHardwareInfoEventMessageAction action = new GetHardwareInfoEventMessageAction((SaltService)apiMock.proxy());

        GetHardwareInfoEventMessage msg = new GetHardwareInfoEventMessage(server.getId(), minionId);
        action.execute(msg);

        this.commitHappened(); // force cleanup on tearDown()

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        server = MinionServerFactory.findByMinionId(minionId).orElse(null);
        return server;
    }

    private <T> T parse(String name, TypeToken<T> returnType) throws IOException {
        String str = IOUtils.toString(getClass().getResourceAsStream(name + ".json"));
        return gson.fromJson(str, returnType.getType());
    }

}
