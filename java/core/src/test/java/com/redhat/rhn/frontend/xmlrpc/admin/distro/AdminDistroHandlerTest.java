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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.admin.distro;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.kickstart.tree.DistroUploadManager;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

public class AdminDistroHandlerTest extends BaseHandlerTestCase {

    @TempDir
    private Path tempDir;

    @Test
    public void testUploadDistroStoresMultipartPayload() throws Exception {
        byte[] distro = "test distro ISO content".getBytes(StandardCharsets.UTF_8);
        AdminDistroHandler handler = new AdminDistroHandler(new DistroUploadManager(tempDir,
            (isoPath, destinationPath) ->
                Files.write(destinationPath.resolve("media.1"), Files.readAllBytes(isoPath))));
        MultipartRequest request = new MultipartRequest("test.iso", distro);

        int result = handler.uploadDistro(satAdmin, request);

        assertEquals(1, result);
        assertArrayEquals(distro, Files.readAllBytes(tempDir.resolve("test/media.1")));
    }

    @Test
    public void testUploadDistroRejectsNonMultipartPayload() {
        AdminDistroHandler handler = new AdminDistroHandler(new DistroUploadManager(tempDir));

        assertThrows(RhnRuntimeException.class, () -> handler.uploadDistro(satAdmin, new RhnMockHttpServletRequest()));
    }

    @Test
    public void testUploadDistroNoPermission() {
        AdminDistroHandler handler = new AdminDistroHandler(new DistroUploadManager(tempDir));

        assertThrows(PermissionCheckFailureException.class,
                () -> handler.uploadDistro(regular, new RhnMockHttpServletRequest()));
    }

    private static class MultipartRequest extends RhnMockHttpServletRequest {
        private static final String BOUNDARY = "boundary";
        private final byte[] body;

        MultipartRequest(String filename, byte[] distro) {
            body = ("--" + BOUNDARY + "\r\n" +
                    "Content-Disposition: form-data; name=\"filename\"\r\n\r\n" +
                    filename + "\r\n" +
                    "--" + BOUNDARY + "\r\n" +
                    "Content-Disposition: form-data; name=\"distro\"; filename=\"" + filename + "\"\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n" +
                    new String(distro, StandardCharsets.UTF_8) + "\r\n" +
                    "--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String getContentType() {
            return "multipart/form-data; boundary=" + BOUNDARY;
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new TestServletInputStream(body);
        }
    }

    private static class TestServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        TestServletInputStream(byte[] body) {
            inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // Not needed for synchronous tests.
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }
}
