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

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Class representing a serializer for both XMLRPC and the HTTP APIs to be used with unit tests
 */
public class TestSerializer extends ApiResponseSerializer<TestResponse> {

    @Override
    public Class<TestResponse> getSupportedClass() {
        return TestResponse.class;
    }

    @Override
    public SerializedApiResponse serialize(TestResponse src) {
        return new SerializationBuilder()
                .add("myInteger", src.getMyInteger())
                .add("myString", src.getMyString())
                .add("isCustomSerialized", true)
                .build();
    }
}
