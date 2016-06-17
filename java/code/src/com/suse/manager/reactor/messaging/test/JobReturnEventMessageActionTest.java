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
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.parser.JsonParser;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
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
            assertEquals("sles", product.getName());
            assertEquals("12.1", product.getVersion());
            assertEquals(null, product.getRelease());
            assertEquals("x86_64", product.getArch().getName());
        });
        assertEquals(1, minion.getInstalledProducts().size());

        // Verify the action status
        assertTrue(action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServer().equals(minion))
                .findAny().get().getStatus().equals(ActionFactory.STATUS_COMPLETED));
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
}
