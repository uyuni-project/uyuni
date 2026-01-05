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
 * Class representing a custom API response type
 */
public class TestResponse {
    private final Integer myInteger;
    private final String myString;

    /**
     * Constructs a {@link TestResponse} object as an API response for the {@link TestHandler}
     * @param myIntegerIn the integer property
     * @param myStringIn the string property
     */
    public TestResponse(Integer myIntegerIn, String myStringIn) {
        this.myInteger = myIntegerIn;
        this.myString = myStringIn;
    }

    public Integer getMyInteger() {
        return myInteger;
    }

    public String getMyString() {
        return myString;
    }
}
