package com.suse.manager.webui.services.impl.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.state.CustomState;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.StateRevisionService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.Optional;

import static org.apache.commons.lang.StringUtils.contains;

/**
 * Test for {@link SaltAPIService}
 */
public class SaltAPIServiceTest extends BaseTestCaseWithUser {

    private static final String THE_STATE_CONTENT = "#the state content";
    public static final long ORG_ID = 1;

    public void testDelete() throws Exception {
        SaltAPIService service = SaltAPIService.INSTANCE;

        String random = TestUtils.randomString();
        String name = "state-" + random;

        Server server = MinionServerFactoryTest.createTestMinionServer(user);

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

        Path generatedSlsPath = tmpSaltRoot.resolve("custom").resolve("custom_" + server.getDigitalServerId() + ".sls");
        String slsContent = FileUtils.readFileToString(generatedSlsPath.toFile());

        assertTrue(contains(slsContent, stateFullName));

        service.deleteCustomState(ORG_ID, name);
        slsContent = FileUtils.readFileToString(generatedSlsPath.toFile());
        assertFalse(contains(slsContent, stateFullName));
    }


}
