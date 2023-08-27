/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.manager;


import com.suse.oval.ovaltypes.TestType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A cache for {@link TestType} to access OVAL tests quickly
 */
public class OvalTestManager {
    private final Map<String, TestType> testsMap = new HashMap<>();

    /**
     * Standard constructor
     *
     * @param tests the tests to store and lookup later
     * */
    public OvalTestManager(List<TestType> tests) {
        for (TestType test : tests) {
            testsMap.put(test.getId(), test);
        }
    }

    /**
     * Looks up an OVAL test with an id of {@code testId} or throws an exception if none is found.
     *
     * @param testId the id of test to lookup
     * @return the test
     * */
    public TestType get(String testId) {
        TestType test = testsMap.get(testId);
        if (test == null) {
            throw new IllegalArgumentException("The test id is invalid: " + testId);
        }
        return test;
    }

    /**
     * Check if an OVAL test with an id of {@code testId} exists
     *
     * @param testId the state id to check if exists
     * @return whether a test with {@code testId} exist or not
     * */
    public boolean exists(String testId) {
        return testsMap.containsKey(testId);
    }
}
