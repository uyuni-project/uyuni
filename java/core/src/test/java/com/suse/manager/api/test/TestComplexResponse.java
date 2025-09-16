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
package com.suse.manager.api.test;

/**
 * Class representing an example of a complex API response type that includes other objects
 */
public class TestComplexResponse {
    private final String myString;
    private final TestResponse myObject;

    /**
     * Constructs a {@link TestComplexResponse} with nested objects as an API response for the {@link TestHandler}
     * @param myStringIn the string property
     * @param myObjectIn the nested object
     */
    public TestComplexResponse(String myStringIn, TestResponse myObjectIn) {
        this.myString = myStringIn;
        this.myObject = myObjectIn;
    }

    public String getMyString() {
        return myString;
    }

    public TestResponse getMyObject() {
        return myObject;
    }
}
