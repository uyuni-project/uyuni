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
package com.suse.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.testing.RhnBaseTestCase;

import com.suse.utils.Exceptions;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

public class ExceptionTest extends RhnBaseTestCase {

    private static final Logger LOGGER = Logger.getLogger(ExceptionTest.class);

    @Test
    public void testCanReturnEmptyWhenEverythingIsOk() {

        final Optional<? extends Exception> result = Exceptions.handleByReturning(() -> LOGGER.info("This is fine."));

        assertTrue(result.isEmpty());
    }

    @Test
    public void testCanReturnExceptionThrown() {

        final Optional<? extends Exception> result = Exceptions.handleByReturning(() -> {
            throw new IOException("This is NOT fine.");
        });

        assertTrue(result.isPresent());
        assertEquals(IOException.class, result.get().getClass());
        assertEquals("This is NOT fine.", result.get().getMessage());
    }

    @Test
    public void testCanWrapExceptionIntoRuntime() {
        try {
            Exceptions.handleByWrapping(() -> {
                throw new IOException("This is NOT fine.");
            });

            fail("No exceptions have been thrown.");
        }
        catch (RuntimeException ex) {
            assertEquals("Unable to execute operation", ex.getMessage());
            assertEquals(IOException.class, ex.getCause().getClass());
            assertEquals("This is NOT fine.", ex.getCause().getMessage());
        }
    }

    @Test
    public void testCanDoesNotThrowExceptionWhenExecutionIsOk() {
        try {
            Exceptions.handleByWrapping(() -> LOGGER.info("This is fine."));
        }
        catch (Exception ex) {
            LOGGER.error(ex);
            fail("An exception has been thrown.");
        }
    }

}
