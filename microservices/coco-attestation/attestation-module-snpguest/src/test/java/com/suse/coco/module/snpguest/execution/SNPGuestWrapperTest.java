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

package com.suse.coco.module.snpguest.execution;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suse.coco.module.snpguest.model.EpycGeneration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class SNPGuestWrapperTest {

    @Mock
    private Process process;

    @Mock
    private Runtime runtime;

    private SNPGuestWrapper wrapper;

    @BeforeEach
    public void setup() throws Exception {

        when(runtime.exec(any(String[].class))).thenReturn(process);

        // Basic mocking of process
        when(process.waitFor()).thenReturn(0);

        wrapper = new SNPGuestWrapper(runtime);
    }

    @Test
    @DisplayName("Generates the correct command line for downloading VCEK")
    void canDownloadCorrectVCEK() throws Exception {
        wrapper.fetchVCEK(EpycGeneration.MILAN, Path.of("certs"), Path.of("report.bin"));

        String expectedCommandLine = "/usr/bin/snpguest fetch vcek DER milan certs report.bin";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }

    @Test
    @DisplayName("Generates the correct command line for verifying certificates")
    void canVerifyCertificates() throws Exception {
        wrapper.verifyCertificates(Path.of("/usr/share/certificates"));

        String expectedCommandLine = "/usr/bin/snpguest verify certs /usr/share/certificates";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }

    @Test
    @DisplayName("Generates the correct command line for verifying an attestation report")
    void canVerifyAttestation() throws Exception {
        wrapper.verifyAttestation(Path.of("/srv/attestation/certs"), Path.of("/root/report.bin"));

        String expectedCommandLine = "/usr/bin/snpguest verify attestation /srv/attestation/certs /root/report.bin";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }
}
