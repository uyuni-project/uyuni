/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.common.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.util.FileLocks;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileLocksTest {

    public static class FileLocksClass {
        private FileLocks theFileLocks;

        public FileLocksClass(String path) {
            theFileLocks = new FileLocks(path);
        }

        public String lockingMethod1() {
            return theFileLocks.withFileLock(this::method1Core);
        }

        private String method1Core() {
            return "method1";
        }

        public String lockingMethod2() {
            return theFileLocks.withFileLock(this::method2Core);
        }

        private String method2Core() {
            return "method2";
        }

        public String recursiveLockingMethod3() {
            return theFileLocks.withFileLock(
                    () -> {
                        String r1 = lockingMethod1();
                        String r2 = lockingMethod2();
                        return r1 + r2;
                    }
            );
        }

        public String lockingMethod3() {
            return theFileLocks.withFileLock(
                    () -> {
                        String r1 = method1Core();
                        String r2 = method2Core();
                        return r1 + r2;
                    }
            );
        }
    }

    @Test
    public void testRecursiveLocking() throws IOException {
        Path tempFile = Files.createTempFile(System.currentTimeMillis() + TestUtils.randomString(), ".lock");

        FileLocksClass testClass = new FileLocksClass(tempFile.toString());

        assertEquals("method1", testClass.lockingMethod1());
        assertEquals("method2", testClass.lockingMethod2());
        assertThrows(OverlappingFileLockException.class, testClass::recursiveLockingMethod3);

        assertEquals("method1method2", testClass.lockingMethod3());

        Files.deleteIfExists(tempFile);
    }
}
