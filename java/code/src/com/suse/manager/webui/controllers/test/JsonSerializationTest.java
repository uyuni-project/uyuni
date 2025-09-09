/*
 * Copyright (c) 2024 SUSE LLC
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InaccessibleObjectException;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import spark.RequestResponseFactory;
import spark.Response;


public class JsonSerializationTest {

    @BeforeEach
    public void setUp() throws Exception {
    }

    @Test
    public void testOptionalEncodingFail() {
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        Optional<String> optString = Optional.of("test");
        assertThrows(InaccessibleObjectException.class, () -> {
            json(response, optString);
        });
    }

    @Test
    public void testOptionalEncodingSuccess() {
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        Optional<String> optString = Optional.of("test");
        json(response, optString, new TypeToken<>() { });
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
