/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.reactor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.BaseTestCase;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.salt.netapi.event.EventStream;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SaltReactor}.
 */
public class SaltReactorTest extends BaseTestCase {

    @Test
    public void testAsynchronousStart() throws Exception {
        // We will create a SaltReactor with dummy parameters.
        // It should start the wait-thread asynchronously and not block.
        SaltApi saltApi = null;
        SystemQuery systemQuery = null;
        SaltServerActionService saltServerActionService = null;
        SaltUtils saltUtils = null;
        CloudPaygManager paygMgr = null;
        AttestationManager attestationMgr = null;

        SaltReactor reactor = new SaltReactor(
            saltApi,
            systemQuery,
            saltServerActionService,
            saltUtils,
            paygMgr,
            attestationMgr
        );

        // Call start() - it should return immediately without blocking.
        long startTime = System.currentTimeMillis();
        reactor.start();
        long duration = System.currentTimeMillis() - startTime;

        // Since the check loop has a 5000ms delay, if it was synchronous, it would take >5000ms.
        // If it's asynchronous, it will complete in less than 1000ms (typically a few ms).
        assertTrue(duration < 2000, "start() took too long: " + duration + "ms");

        // Verify that the background thread "salt-event-stream-initializer" is running or was created.
        Thread waitThread = null;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if ("salt-event-stream-initializer".equals(t.getName())) {
                waitThread = t;
                break;
            }
        }

        assertNotNull(waitThread, "Asynchronous wait thread 'salt-event-stream-initializer' was not found");

        // Stop the reactor, which should terminate the thread.
        reactor.stop();

        // Wait a brief moment for the thread to exit.
        waitThread.join(3000);
        assertTrue(!waitThread.isAlive(), "Background wait thread did not terminate after stopping the reactor");
    }

    @Test
    public void testAsynchronousStartWithRunningTaskomatic() throws Exception {
        final boolean[] getEventStreamCalled = { false };

        SaltApi saltApi = new TestSaltApi() {
            @Override
            public EventStream getEventStream() {
                getEventStreamCalled[0] = true;
                return null; // Return null to trigger NPE on listener addition and terminate the thread
            }
        };
        SystemQuery systemQuery = null;
        SaltServerActionService saltServerActionService = null;
        SaltUtils saltUtils = null;
        CloudPaygManager paygMgr = null;
        AttestationManager attestationMgr = null;

        SaltReactor reactor = new SaltReactor(
            saltApi,
            systemQuery,
            saltServerActionService,
            saltUtils,
            paygMgr,
            attestationMgr
        );

        // Inject mock TaskomaticApi that is already running
        reactor.setTaskomaticApi(new TaskomaticApi() {
            @Override
            public boolean isRunning() {
                return true;
            }
        });

        // Call start() - it should immediately attempt connection
        reactor.start();

        // Verify that the background thread was created
        Thread waitThread = null;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if ("salt-event-stream-initializer".equals(t.getName())) {
                waitThread = t;
                break;
            }
        }

        assertNotNull(waitThread, "Asynchronous wait thread 'salt-event-stream-initializer' was not found");

        // Wait a brief moment for the thread to run and exit (either successfully or with NPE due to null SaltApi)
        waitThread.join(3000);
        assertTrue(!waitThread.isAlive(), "Background wait thread did not terminate after Taskomatic became online");

        // Verify that getEventStream() was indeed invoked, explicitly checking that the connection was attempted!
        assertTrue(getEventStreamCalled[0], "Stream connection was not attempted");
    }
}
