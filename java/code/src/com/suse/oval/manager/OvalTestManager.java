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

    public OvalTestManager(List<TestType> tests) {
        for (TestType test : tests) {
            testsMap.put(test.getId(), test);
        }
    }

    public TestType get(String testId) {
        TestType test = testsMap.get(testId);
        if (test == null) {
            throw new IllegalArgumentException("The test id is invalid: " + testId);
        }
        return test;
    }

    public boolean exists(String testId) {
        return testsMap.containsKey(testId);
    }

    public void add(TestType testType) {
        testsMap.put(testType.getId(), testType);
    }
}
