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
 * SPDX-License-Identifier: GPL-2.0-only
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class AbstractSNPGuestWrapperTest {

    @Mock
    private Process process;

    @Mock
    private Runtime runtime;

    private AbstractSNPGuestWrapper wrapperVer07Below;
    private AbstractSNPGuestWrapper wrapperVer09Above;

    private AbstractSNPGuestWrapper getWrapperToTest(SNPGuestWrapperFactory.SNPGuestVersion type) {
        return switch (type) {
            case VER_07_BELOW -> wrapperVer07Below;
            case VER_09_ABOVE -> wrapperVer09Above;
        };
    }

    @BeforeEach
    void setup() throws Exception {

        when(runtime.exec(any(String[].class))).thenReturn(process);

        // Basic mocking of process
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(process.waitFor()).thenReturn(0);

        wrapperVer07Below = new SNPGuestWrapperVer07Below(runtime);
        wrapperVer09Above = new SNPGuestWrapperVer09Above(runtime);
    }

    @Test
    @DisplayName("Ver 0.7 and below: Generates the correct command line for downloading VCEK")
    void canDownloadCorrectVCEKVer07Below() throws Exception {
        wrapperVer07Below.fetchVCEK(EpycGeneration.MILAN, Path.of("certs"), Path.of("report.bin"));

        String expectedCommandLine = "/usr/bin/snpguest fetch vcek DER milan certs report.bin";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }

    @Test
    @DisplayName("Ver 0.9 and above: Generates the correct command line for downloading VCEK")
    void canDownloadCorrectVCEKVer09Above() throws Exception {
        wrapperVer09Above.fetchVCEK(EpycGeneration.GENOA, Path.of("certs"), Path.of("report.bin"));

        String expectedCommandLine = "/usr/bin/snpguest fetch vcek -p genoa DER certs report.bin";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }

    @ParameterizedTest
    @EnumSource(SNPGuestWrapperFactory.SNPGuestVersion.class)
    @DisplayName("Generates the correct command line for verifying certificates")
    void canVerifyCertificates(SNPGuestWrapperFactory.SNPGuestVersion type) throws Exception {
        AbstractSNPGuestWrapper wrapper = getWrapperToTest(type);
        wrapper.verifyCertificates(Path.of("/usr/share/certificates"));

        String expectedCommandLine = "/usr/bin/snpguest verify certs /usr/share/certificates";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }

    @Test
    @DisplayName("Ver 0.7 and below: Generates the correct command line for verifying an attestation report")
    void canVerifyAttestationVer07Below() throws Exception {
        wrapperVer07Below.verifyAttestation(EpycGeneration.BERGAMO, Path.of("/srv/attestation/certs"),
                Path.of("/root/report.bin"));

        String expectedCommandLine = "/usr/bin/snpguest verify attestation /srv/attestation/certs /root/report.bin";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }

    @Test
    @DisplayName("Ver 0.9 and above: Generates the correct command line for verifying an attestation report")
    void canVerifyAttestationVer09Above() throws Exception {
        wrapperVer09Above.verifyAttestation(EpycGeneration.TURIN, Path.of("/srv/attestation/certs"),
                Path.of("/root/report.bin"));

        String expectedCommandLine = "/usr/bin/snpguest verify attestation -p turin /srv/attestation/certs " +
                "/root/report.bin";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }

    @ParameterizedTest
    @EnumSource(SNPGuestWrapperFactory.SNPGuestVersion.class)
    @DisplayName("Generates the correct command line for displaying an attestation report")
    void canDisplayReport(SNPGuestWrapperFactory.SNPGuestVersion type) throws Exception {
        AbstractSNPGuestWrapper wrapper = getWrapperToTest(type);
        wrapper.displayReport(Path.of("/root/report.bin"));

        String expectedCommandLine = "/usr/bin/snpguest display report /root/report.bin";
        verify(runtime).exec(expectedCommandLine.split(" "));
    }
}
