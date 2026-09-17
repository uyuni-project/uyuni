/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.xmlrpc.iss;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.util.http.HttpClientAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objenesis.ObjenesisStd;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Tests for {@link RestHubExternalClient}, focusing on the response-handling
 * behaviour shared by {@code generateAccessToken} and {@code storeAccessToken}.
 */
class RestHubExternalClientTest {

    private static final String PERIPHERAL_FQDN = "peripheral.example.test";

    private RestHubExternalClient client;
    private StubHttpClientAdapter stubAdapter;

    /**
     * {@link RestHubExternalClient} only exposes a constructor that immediately performs a real
     * login HTTP call, so a real instance cannot be built in a unit test. An instance is created
     * without running that constructor and its internal {@link HttpClientAdapter} is replaced
     * with a stub, so the response-handling logic can be exercised directly.
     */
    @BeforeEach
    void setUp() throws Exception {
        client = (RestHubExternalClient) new ObjenesisStd().newInstance(RestHubExternalClient.class);
        stubAdapter = new StubHttpClientAdapter();
        setField("remoteHost", "hub.example.test");
        setField("httpClientAdapter", stubAdapter);
    }

    private void setField(String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = RestHubExternalClient.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(client, value);
    }

    @Test
    void generateAccessTokenReturnsTokenOnOkResponse() throws Exception {
        stubAdapter.nextResponse = jsonResponse(200, "OK", "{\"result\":\"abc123\"}");

        String token = client.generateAccessToken(PERIPHERAL_FQDN);

        assertEquals("abc123", token);
    }

    @Test
    void generateAccessTokenThrowsOnUnauthorizedResponse() {
        stubAdapter.nextResponse = emptyResponse(401, "Unauthorized");

        IOException ex = assertThrows(IOException.class, () -> client.generateAccessToken(PERIPHERAL_FQDN));

        assertEquals("Unexpected response code 401", ex.getMessage());
    }

    @Test
    void generateAccessTokenThrowsOnServerErrorResponse() {
        stubAdapter.nextResponse = emptyResponse(500, "Internal Server Error");

        IOException ex = assertThrows(IOException.class, () -> client.generateAccessToken(PERIPHERAL_FQDN));

        assertEquals("Unexpected response code 500", ex.getMessage());
    }

    @Test
    void storeAccessTokenSucceedsOnOkResponse() {
        stubAdapter.nextResponse = emptyResponse(200, "OK");

        assertDoesNotThrow(() -> client.storeAccessToken(PERIPHERAL_FQDN, "sometoken"));
    }

    @Test
    void storeAccessTokenThrowsOnUnauthorizedResponse() {
        stubAdapter.nextResponse = emptyResponse(401, "Unauthorized");

        IOException ex = assertThrows(IOException.class,
            () -> client.storeAccessToken(PERIPHERAL_FQDN, "sometoken"));

        assertEquals("Unexpected response code 401", ex.getMessage());
    }

    private static HttpResponse emptyResponse(int statusCode, String reason) {
        return new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, reason));
    }

    private static HttpResponse jsonResponse(int statusCode, String reason, String body) {
        BasicHttpResponse response = new BasicHttpResponse(
                new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, reason));
        response.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return response;
    }

    /**
     * Stands in for the real {@link HttpClientAdapter}, returning a canned response instead of
     * performing an actual HTTP request.
     */
    private static final class StubHttpClientAdapter extends HttpClientAdapter {

        private HttpResponse nextResponse;

        StubHttpClientAdapter() {
            super(List.of(), false);
        }

        @Override
        public HttpResponse executeRequest(HttpRequestBase request) {
            return nextResponse;
        }
    }
}
