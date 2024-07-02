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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.suse.coco.model.AttestationResult;
import com.suse.coco.model.AttestationStatus;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SecureBootWorkerTest {

    private AttestationResult result;

    @Mock
    private SqlSession session;

    private SecureBootWorker worker;

    @BeforeEach
    public void setup() {
        result = new AttestationResult();
        result.setId(1L);
        result.setStatus(AttestationStatus.PENDING);
        result.setReportId(5L);

        worker = new SecureBootWorker();
    }

    @ParameterizedTest(name = "Expecting {1} with message {2}")
    @DisplayName("Test secure boot messages")
    @MethodSource("messagesProvider")
    void testSecureBootMessages(long reportId, boolean expectedOutcome, String message) {
        // Setting the report id we're retrieving
        result.setReportId(reportId);

        // Mock the report retrieval
        when(session.selectOne("SecureBootModule.retrieveReport", reportId))
            .thenReturn(message);

        // Ensure the result is correct
        assertEquals(expectedOutcome, worker.process(session, result));
    }

    static Stream<Arguments> messagesProvider() {
        return Stream.of(
            Arguments.of(1L, true, "SecureBoot enabled\n"),
            Arguments.of(2L, false, "SecureBoot enabled\nSecureBoot validation is disabled in shim\n"),
            Arguments.of(3L, false, "SecureBoot disabled\n"),
            Arguments.of(4L, false, "SecureBoot disabled\nPlatform is in Setup Mode\n"),
            Arguments.of(5L, false, "Cannot determine secure boot state.\n"),
            Arguments.of(6L, false, "Failed to read \"SetupMode\" variable: XYZ\n"),
            Arguments.of(7L, false, "Failed to read \"SecureBoot\" variable: ABC\n"),
            Arguments.of(8L, false, "EFI variables are not supported on this system\n"),
            Arguments.of(9L, false, "This system doesn't support Secure Boot\n"),
            Arguments.of(10L, false, "Could not allocate space: reasons\n"),
            Arguments.of(11L, false, "Totally unexpected error message\n")
        );
    }
}
