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

import java.nio.file.Files;
import java.nio.file.Path;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.suse.manager.reactor.messaging.RefreshGeneratedSaltFilesEventMessage;
import com.suse.manager.reactor.messaging.RefreshGeneratedSaltFilesEventMessageAction;
import com.suse.manager.webui.services.SaltStateGeneratorService;

import static com.suse.manager.webui.services.SaltConstants.SALT_CUSTOM_STATES_DIR;
import static com.suse.manager.webui.services.SaltConstants.SALT_SERVER_STATE_FILE_PREFIX;

/**
 * Test for {@link RefreshGeneratedSaltFilesEventMessageAction}
 */
public class RefreshGeneratedSaltFilesEventMessageActionTest extends BaseTestCaseWithUser {

    public void testDoExecute() throws Exception {
        Path tmpFileRoot = Files.createTempDirectory("refgensalt");

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        // create a group to make sure we have at least one
        ServerGroupTestUtils.createManaged(user);

        ServerStateRevision serverRev = new ServerStateRevision();
        serverRev.setServer(server);
        SaltStateGeneratorService.INSTANCE.generateServerCustomState(serverRev);

        assertTrue(Files.exists(tmpSaltRoot.resolve(SALT_CUSTOM_STATES_DIR)
                .resolve(SALT_SERVER_STATE_FILE_PREFIX + server.getDigitalServerId() + ".sls")));

        RefreshGeneratedSaltFilesEventMessageAction action = new RefreshGeneratedSaltFilesEventMessageAction(
                tmpSaltRoot.toString(), tmpFileRoot.toString());
        action.execute(new RefreshGeneratedSaltFilesEventMessage());

        Path customPath = tmpSaltRoot.resolve(SALT_CUSTOM_STATES_DIR);
        assertTrue(Files.exists(customPath.resolve(
                SALT_SERVER_STATE_FILE_PREFIX + server.getDigitalServerId() + ".sls")));

        for (Org org : OrgFactory.lookupAllOrgs()) {
            assertTrue(Files.exists(customPath.resolve(
                    "org_" + user.getOrg().getId() + ".sls")));

            for(ServerGroup group : ServerGroupFactory.listManagedGroups(org)) {
                assertTrue(Files.exists(customPath.resolve(
                        "group_" + group.getId() + ".sls")));
            }
        }
    }


}
