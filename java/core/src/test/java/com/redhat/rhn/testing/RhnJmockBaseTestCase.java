/*
 * Copyright (c) 2015--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.testing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

/**
 * RhnJmockBaseTestCase - This is the same thing as {@link RhnBaseTestCase}
 * but it extends from {@link MockObjectTestCase}.
 */
public abstract class RhnJmockBaseTestCase extends MockObjectTestCase implements SaltTestCaseUtils {

    protected Path tmpSaltRoot;

    @BeforeEach
    public void setUp() throws Exception {
        tmpSaltRoot = setupSaltConfigurationForTests();
    }

    /**
     * Called once per test method to clean up.
     *
     * @throws Exception if an error occurs during tear down
     */
    @AfterEach
    public void tearDown() throws Exception {
        TestCaseHelper.tearDownHelper();

        cleanupSaltConfiguration(tmpSaltRoot);
    }
}
