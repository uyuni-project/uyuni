/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.testing.RhnMockHttpServletResponse;

import com.suse.common.errors.UyuniError;
import com.suse.common.errors.UyuniErrorCategory;
import com.suse.common.errors.UyuniGeneralException;

import com.google.gson.JsonParser;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import spark.RequestResponseFactory;

public class SparkApplicationHelperTest {

    private static Stream<Arguments> uyuniErrorStatuses() {
        return Stream.of(
                Arguments.of(UyuniErrorCategory.INVALID_ARGUMENT, HttpStatus.SC_BAD_REQUEST),
                Arguments.of(UyuniErrorCategory.FAILED_PRECONDITION, HttpStatus.SC_BAD_REQUEST),
                Arguments.of(UyuniErrorCategory.NOT_FOUND, HttpStatus.SC_NOT_FOUND),
                Arguments.of(UyuniErrorCategory.ALREADY_EXISTS, HttpStatus.SC_CONFLICT),
                Arguments.of(UyuniErrorCategory.UNKNOWN, HttpStatus.SC_INTERNAL_SERVER_ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("uyuniErrorStatuses")
    public void testUyuniErrorsStatusByCategory(UyuniErrorCategory category, int expectedStatus) {
        RhnMockHttpServletResponse servletResponse = new RhnMockHttpServletResponse();
        UyuniGeneralException e = new UyuniGeneralException(List.of(new UyuniError(category, "label", "Bad label")));

        SparkApplicationHelper.uyuniErrors(RequestResponseFactory.create(servletResponse), e);

        assertEquals(expectedStatus, servletResponse.getStatus());
    }

    @Test
    public void testUyuniErrorsBody() {
        RhnMockHttpServletResponse servletResponse = new RhnMockHttpServletResponse();
        UyuniGeneralException e = new UyuniGeneralException(List.of(
                new UyuniError(UyuniErrorCategory.INVALID_ARGUMENT, "label", "Label too short"),
                new UyuniError(UyuniErrorCategory.INVALID_ARGUMENT, "label", "Label has spaces"),
                new UyuniError(UyuniErrorCategory.INVALID_ARGUMENT, null, "Something is wrong"),
                new UyuniError(UyuniErrorCategory.INVALID_ARGUMENT, null, "Something else is wrong")
        ));

        String json = SparkApplicationHelper.uyuniErrors(RequestResponseFactory.create(servletResponse), e);

        String expected = """
                {
                  "success": false,
                  "messages": ["Something is wrong", "Something else is wrong"],
                  "errors": {"label": ["Label too short", "Label has spaces"]}
                }
                """;

        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(json));
        assertEquals("application/json", servletResponse.getContentType());
    }
}
