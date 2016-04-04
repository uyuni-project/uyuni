package com.suse.manager.webui.services.impl.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.state.CustomState;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.StateRevisionService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import org.apache.commons.io.FileUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.apache.commons.lang.StringUtils.contains;

/**
 * Test for {@link SaltAPIService}
 */
public class SaltAPIServiceTest extends BaseTestCaseWithUser {

    private static final String THE_STATE_CONTENT = "#the state content";
    public static final long ORG_ID = 1;

    private Path slsDir;
    private Path pillarDir;

    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();

        slsDir = Files.createTempDirectory("salt-" + TestUtils.randomString());
        pillarDir = Files.createTempDirectory("pillar-" + TestUtils.randomString());

        SaltAPIService.INSTANCE.setCustomStateBaseDir(slsDir.toString());
        SaltStateGeneratorService.INSTANCE.setGeneratedSlsRoot(slsDir.toString());
        SaltStateGeneratorService.INSTANCE.setGeneratedPillarRoot(pillarDir.toString());
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(slsDir.toFile());
        FileUtils.deleteDirectory(pillarDir.toFile());
    }

    public void testDelete() throws Exception {
        SaltAPIService service = SaltAPIService.INSTANCE;

        String random = TestUtils.randomString();
        String name = "state-" + random;

        Server server = createTestMinionServer();

        service.saveCustomState(ORG_ID, name, THE_STATE_CONTENT, null, null);

        ServerStateRevision serverState = StateRevisionService.INSTANCE
                .cloneLatest(server, user, true, false);

        Optional<CustomState> customStateOpt = StateFactory.getCustomStateByName(ORG_ID, name);
        serverState.getCustomStates().add(customStateOpt.get());

        StateFactory.save(serverState);
        HibernateFactory.getSession().flush();

        SaltStateGeneratorService.INSTANCE
                .generateServerCustomState(serverState);

        String stateFullName = "- manager_org_" + user.getOrg().getId() + "." + name;

        Path generatedSlsPath = slsDir.resolve("custom").resolve("custom_" + server.getDigitalServerId() + ".sls");
        String slsContent = readContent(generatedSlsPath);

        assertTrue(contains(slsContent, stateFullName));

        service.deleteCustomState(ORG_ID, name);
        slsContent = readContent(generatedSlsPath);
        assertFalse(contains(slsContent, stateFullName));
    }

    private String readContent(Path generatedSlsPath) throws IOException {
        String slsContent;
        try (FileInputStream fin = new FileInputStream(generatedSlsPath.toFile())) {
            slsContent = TestUtils.readAll(fin);
        }
        return slsContent;
    }

    private MinionServer createTestMinionServer() throws Exception {
        return (MinionServer) ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
    }

}
