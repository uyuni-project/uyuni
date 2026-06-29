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

package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.RhnMockHttpServletRequest;

import com.suse.manager.webui.services.iface.SaltApi;

import org.jmock.Expectations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import spark.Request;
import spark.RequestResponseFactory;
import spark.routematch.RouteMatch;

public class ImageUploadControllerTest extends BaseControllerTestCase {

    private SaltApi mockSaltApi;

    @BeforeEach
    public void setUp() throws Exception {
        mockSaltApi = context.mock(SaltApi.class);
        ImageUploadController.setSaltApi(mockSaltApi);
        ImageUploadController.setTempPath(System.getProperty("java.io.tmpdir"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Restore default SaltApi dependency
        ImageUploadController.setSaltApi(com.redhat.rhn.GlobalInstanceHolder.SALT_API);
        ImageUploadController.setTempPath(com.suse.manager.webui.services.SaltConstants.SALT_FILE_GENERATION_TEMP_PATH);
    }

    @Test
    public void testUploadImageSuccess() throws Exception {
        String boundary = "---MultipartBoundary" + System.currentTimeMillis();
        String part = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"unit_test_img_" +
                System.currentTimeMillis() + ".img\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n" +
                "Hello, unit test content!" + "\r\n" +
                "--" + boundary + "--\r\n";
        byte[] body = part.getBytes(StandardCharsets.UTF_8);

        final String requestUrl = "http://localhost:8080/rhn/manager/upload/image";
        final RouteMatch match = new RouteMatch(new Object(), "/manager/upload/image", requestUrl, "");

        RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setRequestURL(requestUrl);
        mockRequest.setMethod("POST");
        mockRequest.setContentType("multipart/form-data; boundary=" + boundary);
        mockRequest.setContentLength(body.length);

        com.redhat.rhn.testing.MockServletInputStream msis = new com.redhat.rhn.testing.MockServletInputStream();
        msis.setupRead(body);
        mockRequest.setInputStream(msis);

        Request request = RequestResponseFactory.create(match, mockRequest);

        context.checking(new Expectations() {{
            oneOf(mockSaltApi).mkDir(with(any(Path.class)), with(equal("0755")));
            will(returnValue(Optional.of(true)));
            oneOf(mockSaltApi).copyFile(with(any(Path.class)), with(any(Path.class)));
            will(returnValue(Optional.of(true)));
        }});

        String responseStr = ImageUploadController.uploadImage(request, response, user);
        assertNotNull(responseStr);
        assertTrue(responseStr.contains("\"success\":true"));
    }

    @Test
    public void testUploadImageDirectoryTraversal() throws Exception {
        String boundary = "---MultipartBoundary" + System.currentTimeMillis();
        String part = "--" + boundary + "\r\n" +
                      "Content-Disposition: form-data; name=\"file\"; filename=\"../../traversal_attempt.img\"\r\n" +
                      "Content-Type: application/octet-stream\r\n\r\n" +
                      "Hello, malicious content!" + "\r\n" +
                      "--" + boundary + "--\r\n";
        byte[] body = part.getBytes(StandardCharsets.UTF_8);

        final String requestUrl = "http://localhost:8080/rhn/manager/upload/image";
        final RouteMatch match = new RouteMatch(new Object(), "/manager/upload/image", requestUrl, "");

        RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setRequestURL(requestUrl);
        mockRequest.setMethod("POST");
        mockRequest.setContentType("multipart/form-data; boundary=" + boundary);
        mockRequest.setContentLength(body.length);

        com.redhat.rhn.testing.MockServletInputStream msis = new com.redhat.rhn.testing.MockServletInputStream();
        msis.setupRead(body);
        mockRequest.setInputStream(msis);

        Request request = RequestResponseFactory.create(match, mockRequest);

        // We expect NO Salt API calls because the controller should reject the request before reaching them.

        String responseStr = ImageUploadController.uploadImage(request, response, user);
        assertNotNull(responseStr);
        assertTrue(responseStr.contains("\"success\":false"));
        assertTrue(responseStr.contains("Invalid filename"));
    }
}
