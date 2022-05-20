package com.suse.manager.webui.services.test;

import com.suse.manager.webui.services.ThrottlingService;
import com.suse.manager.webui.services.TooManyCallsException;

import junit.framework.TestCase;

public class ThrottlingServiceTest extends TestCase {
    private ThrottlingService service;
    private final long throttle_period = 60;

    @Override
    public void setUp() {
        service = new ThrottlingService();
    }

    public void testThrottle() {
        long callsAllowed = 2;
        assertSuccess(1, "/my/resource", callsAllowed, throttle_period, "First call must be allowed");
        assertSuccess(1, "/my/resource", callsAllowed, throttle_period, "Second call must be allowed");
        assertFailure(1, "/my/resource", callsAllowed, throttle_period, "Third call must not be allowed");
    }

    public void testTimeout() throws InterruptedException {
        assertSuccess(1, "/my/resource", 1, 1, "Call must be allowed");
        assertFailure(1, "/my/resource", 1, 1, "Call must not be allowed");

        Thread.sleep(1000);

        assertSuccess(1, "/my/resource", 1, 1, "Call must be allowed after timeout");
    }

    public void testMultipleResources() {
        assertSuccess(1, "/resource/one", 1, throttle_period, "Call must be allowed");
        assertFailure(1, "/resource/one", 1, throttle_period, "Call must not be allowed");
        assertSuccess(1, "/resource/two", 1, throttle_period, "Call to a different resource must be allowed");
    }

    public void testMultipleUsers() {
        assertSuccess(1, "/my/resource", 1, throttle_period, "Call must be allowed");
        assertFailure(1, "/my/resource", 1, throttle_period, "Call must not be allowed");
        assertSuccess(2, "/my/resource", 1, throttle_period, "Call from a different user must be allowed");
    }

    private void assertSuccess(long uid, String resource, long maxCalls, long period, String message) {
        try {
            service.call(uid, resource, maxCalls, period);
        }
        catch (TooManyCallsException eIn) {
            fail(message);
        }
    }

    private void assertFailure(long uid, String resource, long maxCalls, long period, String message) {
        try {
            service.call(uid, resource, maxCalls, period);
            fail(message);
        }
        catch (TooManyCallsException eIn) {
            // pass
        }
    }
}
