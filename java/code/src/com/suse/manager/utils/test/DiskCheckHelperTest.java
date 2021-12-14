/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.utils.test;

import com.suse.manager.utils.DiskCheckHelper;
import com.suse.manager.utils.DiskCheckSeverity;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * Test for {@link DiskCheckHelper}
 */
public class DiskCheckHelperTest extends TestCase {

    /**
     * Enforces the handling of any exception inside the execution of the script.
     */
    public void testReturnsUndefinedWhenAnExceptionIsThrown() {
        final DiskCheckHelper diskCheckHelper = new DiskCheckHelper() {
            @Override
            protected int invokeExternalScript() throws IOException {
                throw new IOException("Fake unexpected exception during the execution of the script");
            }
        };

        assertEquals(DiskCheckSeverity.UNDEFINED, diskCheckHelper.executeDiskCheck());
    }

    /**
     * Enforce behaviour for wrong exit value parsing
     */
    public void testThrowsIllegalArgumentWhenExitCodeIsNotValid() {
        assertEquals(DiskCheckSeverity.UNDEFINED, new FixedResultDiskCheckHelper(256).executeDiskCheck());
    }

    /**
     * Ensures that the script return values are converted correctly.
     */
    public void testConvertExitValueToSeverity() {
        assertEquals(DiskCheckSeverity.OK, new FixedResultDiskCheckHelper(0).executeDiskCheck());
        assertEquals(DiskCheckSeverity.MISCONFIGURATION, new FixedResultDiskCheckHelper(1).executeDiskCheck());
        assertEquals(DiskCheckSeverity.ALERT, new FixedResultDiskCheckHelper(2).executeDiskCheck());
        assertEquals(DiskCheckSeverity.CRITICAL, new FixedResultDiskCheckHelper(3).executeDiskCheck());
    }

    /**
     * DiskCheckHelper that mocks the execution of the script and always returns the same exit value.
     */
    private static class FixedResultDiskCheckHelper extends DiskCheckHelper {

        private final int scriptResult;

        private FixedResultDiskCheckHelper(int scriptResultIn) {
            this.scriptResult = scriptResultIn;
        }

        @Override
        protected int invokeExternalScript() throws IOException, InterruptedException {
            return scriptResult;
        }
    }


}
