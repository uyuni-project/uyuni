/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.salt.netapi.event.EventListener;
import com.suse.salt.netapi.event.EventStream;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;

/**
 * Unit test for {@link SaltReactor}.
 */
class SaltReactorTest {

    @Test
    void testAsynchronousStart() throws Exception {
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
    void testAsynchronousStartWithRunningTaskomatic() throws Exception {
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

    @Test
    void testInitializerCommitsAndClosesSession() throws Exception {
        AtomicReference<Session> initializerSession = new AtomicReference<>();
        AtomicInteger transactionStatus = new AtomicInteger(Status.STATUS_UNKNOWN);

        SaltReactor reactor = createReactorWithRunningTaskomatic(new TestSaltApi() {
            @Override
            public EventStream getEventStream() {
                openSessionAndTrackTransaction(initializerSession, transactionStatus);
                return new DummyEventStream();
            }
        });

        startAndWaitForInitializer(reactor);
        reactor.stop();

        assertNotNull(initializerSession.get(), "Initializer did not open a Hibernate session");
        assertEquals(Status.STATUS_COMMITTED, transactionStatus.get(), "Initializer transaction was not committed");
        assertFalse(initializerSession.get().isOpen(), "Initializer Hibernate session was not closed");
    }

    @Test
    void testInitializerRollsBackAndClosesSessionOnFailure() throws Exception {
        AtomicReference<Session> initializerSession = new AtomicReference<>();
        AtomicInteger transactionStatus = new AtomicInteger(Status.STATUS_UNKNOWN);

        SaltReactor reactor = createReactorWithRunningTaskomatic(new TestSaltApi() {
            @Override
            public EventStream getEventStream() {
                openSessionAndTrackTransaction(initializerSession, transactionStatus);
                throw new IllegalStateException("Simulated event stream failure");
            }
        });

        startAndWaitForInitializer(reactor);
        reactor.stop();

        assertNotNull(initializerSession.get(), "Initializer did not open a Hibernate session");
        assertEquals(Status.STATUS_ROLLEDBACK, transactionStatus.get(),
                "Initializer transaction was not rolled back");
        assertFalse(initializerSession.get().isOpen(), "Initializer Hibernate session was not closed");
    }

    private static void openSessionAndTrackTransaction(AtomicReference<Session> sessionOut,
                                                       AtomicInteger statusOut) {
        // like PGEventStream, which runs SaltEventFactory queries in the session of the initializer thread
        Session session = HibernateFactory.getSession();
        sessionOut.set(session);
        session.getTransaction().registerSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                // nothing to do
            }

            @Override
            public void afterCompletion(int status) {
                statusOut.set(status);
            }
        });
    }

    private static SaltReactor createReactorWithRunningTaskomatic(SaltApi saltApi) {
        SaltReactor reactor = new SaltReactor(saltApi, null, null, null, null, null);
        reactor.setTaskomaticApi(new TaskomaticApi() {
            @Override
            public boolean isRunning() {
                return true;
            }
        });
        return reactor;
    }

    private static void startAndWaitForInitializer(SaltReactor reactor)
            throws InterruptedException, ReflectiveOperationException {
        reactor.start();
        Field initializerField = SaltReactor.class.getDeclaredField("initializerThread");
        initializerField.setAccessible(true);
        Thread initializerThread = (Thread) initializerField.get(reactor);
        assertNotNull(initializerThread, "Initializer thread was not created");
        initializerThread.join(10000);
        assertFalse(initializerThread.isAlive(), "Initializer thread did not terminate");
    }

    private static class DummyEventStream implements EventStream {
        @Override
        public void addEventListener(EventListener listener) {
            // nothing to do
        }

        @Override
        public void removeEventListener(EventListener listener) {
            // nothing to do
        }

        @Override
        public int getListenerCount() {
            return 0;
        }

        @Override
        public boolean isEventStreamClosed() {
            return false;
        }

        @Override
        public void close() {
            // nothing to do
        }
    }
}
