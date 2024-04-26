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
package com.suse.coco.module.secureboot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.suse.coco.model.AttestationResult;
import com.suse.coco.model.AttestationStatus;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class SecureBootWorkerTest {

    private AttestationResult result;

    @Mock
    private SqlSession session;

    private SecureBootWorker worker;

    private Map<Long, String> reportMessages;

    @BeforeEach
    public void setup() {
        result = new AttestationResult();
        result.setId(1L);
        result.setStatus(AttestationStatus.PENDING);
        result.setReportId(5L);

        reportMessages = Map.of(1L, "SecureBoot enabled\n",
                2L, "SecureBoot enabled\nSecureBoot validation is disabled in shim\n",
                3L, "SecureBoot disabled\n",
                4L, "SecureBoot disabled\nPlatform is in Setup Mode\n",
                5L, "Cannot determine secure boot state.\n",
                6L, "Failed to read \"SetupMode\" variable: XYZ\n",
                7L, "Failed to read \"SecureBoot\" variable: ABC\n"
                );
        // Common mocking
        when(session.selectOne(eq("SecureBootModule.retrieveReport"), anyLong()))
                .thenAnswer(invocation -> reportMessages.get(invocation.getArgument(1, Long.class)));

        worker = new SecureBootWorker();
    }

    @Test
    @DisplayName("Test secure boot messages")
    void testSecureBootMessages() {

        reportMessages.forEach((k, v) -> {
            result.setReportId(k);
            if (k.equals(1L)) {
                // success
                assertTrue(worker.process(session, result));
            }
            else {
                // errors
                assertFalse(worker.process(session, result));
            }
        });
    }
}
