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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.ImageSyncedEventMessage;
import com.suse.manager.reactor.messaging.ImageSyncedEventMessageAction;
import com.suse.manager.webui.utils.salt.custom.ImageSyncedEvent;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.parser.JsonParser;

import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * Test for {@link ImageDeployedEvent}
 */
public class ImageSyncedEventMessageActionTest extends JMockBaseTestCaseWithUser {
    // Fixed test parameters
    private MinionServer testMinion;
    private ManagedServerGroup testGroup1, testGroup2;

    @Override
    @BeforeEach
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
    @Test
    public void testImageSyncedPillarCreatedRemoved() throws Exception {
        ImageInfo img1 = ImageTestUtils.createImageInfo("ImageTest", "8.0.0", user);
        img1.setRevisionNumber(5);

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
        "            'image_version': '8.0.0-5'                  " +
        "        }                                               " +
        "    }                                                   " +
        "}");

        Optional<ImageSyncedEvent> imageSyncedEventOpt = ImageSyncedEvent.parse(event);
        assertTrue(imageSyncedEventOpt.isPresent());
        assertEquals("add", imageSyncedEventOpt.get().getAction());
        EventMessage message = new ImageSyncedEventMessage(imageSyncedEventOpt.get());
        ImageSyncedEventMessageAction action = new ImageSyncedEventMessageAction();
        action.execute(message);

        testGroup1 = TestUtils.reload(testGroup1);

        String category = "SyncedImage" + img1.getId();
        assertTrue(testGroup1.getPillarByCategory(category).isPresent());


        HibernateFactory.getSession().delete(img1);
        HibernateFactory.getSession().flush();
        testGroup1 = TestUtils.reload(testGroup1);

        assertFalse(testGroup1.getPillarByCategory(category).isPresent());
    }

    /**
     * Happy path scenario: machine_id grain is present.
     * In this case we test that at the end of the Action, the minion has correct channels
     * (based on its product) assigned.
     */
    @Test
    public void testLegacyImageSyncedPillarCreatedRemoved() throws Exception {
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

        testGroup1 = TestUtils.reload(testGroup1);

        assertTrue(testGroup1.getPillarByCategory("LegacySyncedImage-ImageTest-7.0.0").isPresent());

        Event event2 = jsonParser.parse(
        "{                                                       " +
        "    'tag': 'suse/manager/image_synced',                 " +
        "    'data': {                                           " +
        "        'id': '" + testMinion.getMinionId() + "',       " +
        "        'cmd': '_minion_event',                         " +
        "        'pretag': None,                                 " +
        "        'data': {                                       " +
        "            'branch': '" + testGroup1.getName() + "',   " +
        "            'action': 'remove',                         " +
        "            'image_name': 'ImageTest',                  " +
        "            'image_version': '7.0.0'                  " +
        "        }                                               " +
        "    }                                                   " +
        "}");

        Optional<ImageSyncedEvent> imageSyncedEventOpt2 = ImageSyncedEvent.parse(event2);
        assertTrue(imageSyncedEventOpt2.isPresent());
        assertEquals("remove", imageSyncedEventOpt2.get().getAction());
        EventMessage message2 = new ImageSyncedEventMessage(imageSyncedEventOpt2.get());
        ImageSyncedEventMessageAction action2 = new ImageSyncedEventMessageAction();
        action2.execute(message2);

        testGroup1 = TestUtils.reload(testGroup1);

        assertFalse(testGroup1.getPillarByCategory("LegacySyncedImage-ImageTest-7.0.0").isPresent());
    }
}
