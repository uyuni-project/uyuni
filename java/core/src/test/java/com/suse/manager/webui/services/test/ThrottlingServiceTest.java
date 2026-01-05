/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.webui.services.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.suse.manager.webui.services.ThrottlingService;
import com.suse.manager.webui.services.TooManyCallsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingConsumer;

public class ThrottlingServiceTest {
    private ThrottlingService service;

    @BeforeEach
    public void setUp() {
        service = new ThrottlingService();
    }

    @Test
    public void testThrottle() {
        long callsAllowed = 2;
        Executable call = () ->
                service.call(1, "/my/resource", callsAllowed, ThrottlingService.DEF_THROTTLE_PERIOD_SECS);

        assertDoesNotThrow(call, "First call must be allowed");
        assertDoesNotThrow(call, "Second call must be allowed");
        assertThrows(TooManyCallsException.class, call, "Third call must not be allowed");
    }

    @Test
    public void testTimeout() throws InterruptedException {
        Executable call = () -> service.call(1, "/my/resource", 1, 1);

        assertDoesNotThrow(call, "Call must be allowed");
        assertThrows(TooManyCallsException.class, call, "Call must not be allowed");

        Thread.sleep(1000);

        assertDoesNotThrow(call, "Call must be allowed after timeout");
    }

    @Test
    public void testMultipleResources() {
        ThrowingConsumer<String> call = (res) -> service.call(1, res, 1, ThrottlingService.DEF_THROTTLE_PERIOD_SECS);

        assertDoesNotThrow(() -> call.accept("/resource/one"), "Call must be allowed");
        assertThrows(TooManyCallsException.class, () -> call.accept("/resource/one"), "Call must not be allowed");
        assertDoesNotThrow(() -> call.accept("/resource/two"), "Call to a different resource must be allowed");
    }

    @Test
    public void testMultipleUsers() {
        ThrowingConsumer<Long> call = (uid) -> service.call(uid, "/my/resource", 1,
                ThrottlingService.DEF_THROTTLE_PERIOD_SECS);

        assertDoesNotThrow(() -> call.accept(1L), "Call must be allowed");
        assertThrows(TooManyCallsException.class, () -> call.accept(1L), "Call must not be allowed");
        assertDoesNotThrow(() -> call.accept(2L), "Call from a different user must be allowed");
    }
}
