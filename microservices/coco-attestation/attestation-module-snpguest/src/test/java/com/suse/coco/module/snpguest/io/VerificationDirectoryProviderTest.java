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

package com.suse.coco.module.snpguest.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.suse.coco.module.snpguest.TestHelper;
import com.suse.coco.module.snpguest.model.AttestationReport;
import com.suse.coco.module.snpguest.model.EpycGeneration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class VerificationDirectoryProviderTest {

    private Path destPath;

    @Mock
    private AttestationReport attestationReport;

    private VerificationDirectoryProvider directoryProvider;

    @BeforeEach
    public void setUp() throws Exception {
        destPath = Files.createTempDirectory("verification-directory-provider-test");

        URL resource = VerificationDirectoryProviderTest.class.getResource("/testCerts");
        assertNotNull(resource, "Cannot find resource for test certificates folder");
        Path sourcePath = Path.of(Objects.requireNonNull(resource).toURI());

        directoryProvider = new VerificationDirectoryProvider(destPath, sourcePath);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Delete test files
        try (Stream<Path> fileStream = Files.walk(destPath)) {
            fileStream
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @ParameterizedTest(name = TestHelper.CPU_USING_VLEK_NAME)
    @MethodSource("com.suse.coco.module.snpguest.TestHelper#listCpuAndUsingVlek")
    @DisplayName("Verification directory is created by the provider and destroyed on resource closure" +
            " for each cpu generation")
    void canCreateAndDestroyVerificationDirectory(EpycGeneration cpuGeneration, boolean usingVlek) throws IOException {
        Path verificationDirectory;

        when(attestationReport.getCpuGeneration())
            .thenReturn(cpuGeneration);
        when(attestationReport.getReport())
            .thenReturn("This is a dummy report for unit test".getBytes(StandardCharsets.UTF_8));
        when(attestationReport.isUsingVlekAttestation())
                .thenReturn(usingVlek);
        lenient().when(attestationReport.getVlekCertificate())
                .thenReturn(usingVlek ? "This is a dummy VLEK certificaate" : null);

        try (VerificationDirectory directory = directoryProvider.createDirectoryFor(5L, attestationReport)) {
            verificationDirectory = directory.getBasePath();

            // Check the path was created with the expected prefix
            String directoryName = verificationDirectory.getFileName().toString();
            assertTrue(directoryName.startsWith("snp-guest-worker-5-"));

            // Verify the certs directory has been created
            assertTrue(Files.isReadable(directory.getCertsPath()));
            assertTrue(Files.isDirectory(directory.getCertsPath()));
            assertEquals(destPath.resolve(directoryName).resolve("certs"), directory.getCertsPath());

            // Check the certificates are copied
            Path arkCert = directory.getCertsPath().resolve("ark.pem");
            assertTrue(Files.exists(arkCert));

            Path askCert = directory.getCertsPath().resolve("ask.pem");
            assertTrue(Files.exists(askCert));

            Path asvkCert = directory.getCertsPath().resolve("asvk.pem");
            assertTrue(Files.exists(asvkCert));

            Path vlekCert = directory.getCertsPath().resolve("vlek.pem");
            assertEquals(usingVlek, Files.exists(vlekCert));

            // Check they contain the correct value
            String cpuName = cpuGeneration.name().toLowerCase();
            cpuName = cpuName.substring(0, 1).toUpperCase() + cpuName.substring(1);
            assertEquals("%s ROOT fake certificate".formatted(cpuName), Files.readString(arkCert).strip());
            assertEquals("%s INTERMEDIATE fake certificate".formatted(cpuName), Files.readString(askCert).strip());
            assertEquals("%s INTERMEDIATE VLEK fake certificate".formatted(cpuName),
                    Files.readString(asvkCert).strip());

            // Verify the report is present
            assertTrue(Files.isReadable(directory.getReportPath()));
            assertEquals(destPath.resolve(directoryName).resolve("report.bin"), directory.getReportPath());

            // Check the report is correct
            assertEquals(
                "This is a dummy report for unit test",
                Files.readString(directory.getReportPath(), StandardCharsets.UTF_8)
            );
        }

        assertFalse(Files.exists(verificationDirectory));
    }

    @Test
    @DisplayName("An exception is thrown if the certificate files are missing for the given generation")
    void throwsExceptionIfTheGenerationIsNotAvailable() throws IOException {
        long totalFiles = countTotalFileInFolder(destPath);

        when(attestationReport.getCpuGeneration()).thenReturn(EpycGeneration.UNKNOWN);

        IOException ex = assertThrows(IOException.class,
            () -> directoryProvider.createDirectoryFor(3L, attestationReport)
        );

        // Check the exception is the expected one
        assertEquals("Cannot find certificate for cpu generation UNKNOWN", ex.getMessage());
        // Check no files have been created
        assertEquals(totalFiles, countTotalFileInFolder(destPath),
                "Some files have been created and left behind");
    }

    private long countTotalFileInFolder(Path path) throws IOException {
        try (Stream<Path> files = Files.list(path)) {
            return files.count();
        }
    }

}
