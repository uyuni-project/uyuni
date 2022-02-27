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
package com.suse.manager.reactor.messaging.test;

import static com.suse.manager.webui.services.SaltConstants.PILLAR_IMAGE_DATA_FILE_EXT;
import static com.suse.manager.webui.services.SaltConstants.SUMA_PILLAR_IMAGES_DATA_PATH;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;

import com.suse.manager.reactor.messaging.ImageSyncedEventMessage;
import com.suse.manager.reactor.messaging.ImageSyncedEventMessageAction;
import com.suse.manager.webui.utils.salt.custom.ImageSyncedEvent;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.parser.JsonParser;

import com.google.gson.reflect.TypeToken;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Test for {@link ImageDeployedEvent}
 */
public class ImageSyncedEventMessageActionTest extends JMockBaseTestCaseWithUser {
    // Fixed test parameters
    private MinionServer testMinion;
    private ManagedServerGroup testGroup1, testGroup2;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // setup a minion
        testMinion = MinionServerFactoryTest.createTestMinionServer(user);
        testMinion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));

        testGroup1 = ServerGroupTestUtils.createManaged(user);
        testGroup2 = ServerGroupTestUtils.createManaged(user);
        testMinion.addGroup(testGroup1);
        testMinion.addGroup(testGroup2);
    }

    /**
     * Happy path scenario: machine_id grain is present.
     * In this case we test that at the end of the Action, the minion has correct channels
     * (based on its product) assigned.
     */
    public void testImageSyncedPillarCreated() throws Exception {
        JsonParser<Event> jsonParser = new JsonParser<>(new TypeToken<>() {
        });
        Event event = jsonParser.parse(
        "{                                                       " +
        "    'tag': 'suse/manager/image_synced',                 " +
        "    'data': {                                           " +
        "        'id': '" + testMinion.getMinionId() + "',       " +
        "        'cmd': '_minion_event',                         " +
        "        'pretag': None,                                 " +
        "        'data': {                                       " +
        "            'branch': '" + testGroup1.getName() + "',   " +
        "            'action': 'add',                            " +
        "            'image_name': 'ImageTest',                  " +
        "            'image_version': '7.0.0'                    " +
        "        }                                               " +
        "    }                                                   " +
        "}");

        Optional<ImageSyncedEvent> imageSyncedEventOpt = ImageSyncedEvent.parse(event);
        assertTrue(imageSyncedEventOpt.isPresent());
        assertEquals("add", imageSyncedEventOpt.get().getAction());
        EventMessage message = new ImageSyncedEventMessage(imageSyncedEventOpt.get());
        ImageSyncedEventMessageAction action = new ImageSyncedEventMessageAction();
        action.execute(message);

        Path filePath = tmpSaltRoot.resolve(SUMA_PILLAR_IMAGES_DATA_PATH)
            .resolve("group" + testGroup1.getId().toString())
            .resolve("ImageTest-7.0.0." + PILLAR_IMAGE_DATA_FILE_EXT);

        assertTrue(Files.exists(filePath));
        Files.deleteIfExists(filePath);
    }
}
