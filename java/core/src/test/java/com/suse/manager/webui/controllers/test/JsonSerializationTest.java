/*
 * Copyright (c) 2024--2026 SUSE LLC
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
package com.suse.manager.webui.controllers.test;

import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.message;
import static com.suse.manager.webui.utils.SparkApplicationHelper.notFound;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.testing.RhnMockHttpServletResponse;

import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InaccessibleObjectException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import spark.RequestResponseFactory;
import spark.Response;


public class JsonSerializationTest {

    /**
     * This test demonstrates that serializing certain internal JDK classes will fail.
     * Due to Java's module system (Java 9+), reflection on such internal classes is
     * restricted, causing Gson to throw an InaccessibleObjectException.
     */
    @Test
    public void testEncodingFail() {
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        Optional<String> optString = Optional.of("test");
        Set<Object> emptySet = Collections.emptySet();
        List<Object> emptyList = Collections.emptyList();
        LocalDate localDate = LocalDate.of(2021, 6, 25);

        // When serializing without type information, Gson must use reflection on the actual runtime class.
        // For JDK internal classes like {@code java.util.Optional} or with internal implementations like
        // {@code Collections.emptySet()}, Java's module system blocks this reflection
        // causing {@code InaccessibleObjectException}.
        assertThrows(InaccessibleObjectException.class, () -> json(response, optString));
        assertThrows(InaccessibleObjectException.class, () -> json(response, emptySet));
        assertThrows(InaccessibleObjectException.class, () -> json(response, emptyList));
        // This will fail because there is a type adapter registered for LocalDateTime, but not for LocalDate
        // specifically
        assertThrows(InaccessibleObjectException.class, () -> json(response, localDate));
    }

    /**
     * This test demonstrates how providing explicit type information via {@code TypeToken} allows serialization of
     * JDK internal classes (that would otherwise fail see {@link #testEncodingFail()}).
     */
    @Test
    public void testEncodingSuccessWithTypeInfo() {
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        Optional<String> optString = Optional.of("test");
        Set<Object> emptySet = Collections.emptySet();
        List<Object> emptyList = Collections.emptyList();
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2021, 6, 25), LocalTime.of(0, 56));

        //
        assertEquals("\"test\"", json(response, optString, new TypeToken<>() { }));
        assertEquals("[]", json(response, emptySet, new TypeToken<>() { }));
        assertEquals("[]", json(response, emptyList, new TypeToken<>() { }));
        assertEquals("\"2021-06-25T00:56\"", json(response, localDateTime, new TypeToken<>() { }));
    }

    /**
     * This test verifies successful serialization of various common data types.
     * @param input The input object to serialize
     * @param expectedJson The expected JSON string output
     */
    @ParameterizedTest
    @MethodSource("successfulEncodingCases")
    public void testEncodingSuccess(Object input, String expectedJson) {
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        assertEquals(expectedJson, json(response, input));
    }

    static Stream<Arguments> successfulEncodingCases() {
        return Stream.of(
                Arguments.of(null, "null"),
                Arguments.of("", "\"\""),
                Arguments.of("text", "\"text\""),
                Arguments.of(999, "999"),
                Arguments.of(List.of(), "[]"),
                Arguments.of(List.of(1, 2, 3), "[1,2,3]"),
                Arguments.of(new HashSet<>(), "[]"),
                Arguments.of(new HashSet<>(List.of("a", "b", "c")), "[\"a\",\"b\",\"c\"]")
        );
    }

    private void assertEqualResponse(Function<Response, String> oldFn, Function<Response, String> newFn) {
        SimpleTestingResponse oldMockRequest = new SimpleTestingResponse();
        SimpleTestingResponse newMockRequest = new SimpleTestingResponse();
        Response oldResponse = RequestResponseFactory.create(oldMockRequest);
        Response newResponse = RequestResponseFactory.create(newMockRequest);
        String oldJson = oldFn.apply(oldResponse);
        String newJson = newFn.apply(newResponse);
        assertEquals(newJson, oldJson);

        assertEquals(newMockRequest.getStatus(), oldMockRequest.getStatus());
        assertEquals(newMockRequest.getContentType(), oldMockRequest.getContentType());
    }

    @Test
    public void testMessageJson() {
        assertEqualResponse(
            r -> message(r, "404 Not found"),
            r -> json(r, Collections.singletonMap("message", "404 Not found"))
        );
    }

    @Test
    public void testResultJsonError() {
        assertEqualResponse(
                r -> result(r, ResultJson.error("not_found"), new TypeToken<>() { }),
                r -> json(r, ResultJson.error("not_found"))
        );
    }

    @Test
    public void testResultJsonSuccess() {
        assertEqualResponse(
                r -> result(r, ResultJson.success("successful result"), new TypeToken<>() { }),
                r -> json(r, ResultJson.success("successful result"))
        );
    }

    @Test
    public void testISE() {
        assertEqualResponse(
                r -> internalServerError(r, "internal server error test"),
                r -> json(r, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ResultJson.error("internal server error test"))
        );
    }

    @Test
    public void testNotFound() {
        assertEqualResponse(
                r -> notFound(r, "server_not_found"),
                r -> json(r,
                        HttpStatus.SC_NOT_FOUND,
                        ResultJson.error("server_not_found"))
        );
    }

    @Test
    public void testBadRequest() {
        assertEqualResponse(
                r -> badRequest(r, "invalid_activation_key_id"),
                r -> json(r,
                        HttpStatus.SC_BAD_REQUEST,
                        ResultJson.error("invalid_activation_key_id"))
        );
    }
}
