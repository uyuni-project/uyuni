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

package com.suse.coco.module.pvattest.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.coco.module.pvattest.PvattestTestHelper;
import com.suse.coco.module.pvattest.model.AttestationRequest;
import com.suse.coco.module.pvattest.model.AttestationResponse;
import com.suse.common.io.process.ProcessOutput;
import com.suse.common.io.process.ShellCommandExecutor;
import com.suse.common.security.CertificateHelper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
public class PvattestWrapperTest {

    public static class MockShellCommandExecutor extends ShellCommandExecutor {
        private ProcessOutput output;
        private boolean doFillFilesForCreate;
        private boolean doFillFilesForVerifyMeasurement;

        public void forceOutput(String standardOutput) {
            output = new ProcessOutput(0, standardOutput, "");
        }

        private static void fillFile(String filename, int numBytes) {
            try {
                FileOutputStream outputStream = new FileOutputStream(filename);
                byte[] buffer = new byte[numBytes];
                outputStream.write(buffer);
                outputStream.close();
            }
            catch (IOException eIn) {
                throw new RuntimeException(eIn);
            }
        }

        private static void fillFile(String filename, String content) {
            try {
                Files.writeString(Path.of(filename), content, StandardCharsets.UTF_8);
            }
            catch (IOException eIn) {
                throw new RuntimeException(eIn);
            }
        }

        private void fillFilesForCreate(String... command) {
            for (String commandChunk : command) {
                if (commandChunk.contains("attestation_request_")) {
                    fillFile(commandChunk, 464);
                }
                if (commandChunk.contains("attestation_protection_key_")) {
                    fillFile(commandChunk, 32);
                }
            }
        }

        private void fillFilesForVerifyMeasurement(String... command) {
            for (String commandChunk : command) {
                if (commandChunk.contains("attestation_result_")) {
                    fillFile(commandChunk, PvattestTestHelper.testAttestationResultYaml());
                }
            }
        }

        @Override
        public ProcessOutput executeProcess(String... command) throws ExecutionException {
            lastExecutedCommand = String.join(" ", command);

            if (doFillFilesForCreate) {
                fillFilesForCreate(command);
                doFillFilesForCreate = false;
            }

            if (doFillFilesForVerifyMeasurement) {
                fillFilesForVerifyMeasurement(command);
                doFillFilesForVerifyMeasurement = false;
            }

            return output;
        }
    }

    private final MockShellCommandExecutor mockCommandExecutor = new MockShellCommandExecutor();
    private final PvattestWrapper testPvattestWrapper = new PvattestWrapper(mockCommandExecutor);

    @Test
    @DisplayName("command check: findPackageVersion")
    public void testFindPackageVersion() throws ExecutionException {
        mockCommandExecutor.forceOutput("2.41.0\n");
        checkFindPackageVersion(testPvattestWrapper);
    }

    private void checkFindPackageVersion(PvattestWrapper pvaw) throws ExecutionException {
        assertEquals("2.41.0", pvaw.findPackageVersion());
        assertEquals("/usr/bin/rpm -q --queryformat %{VERSION} s390-tools", pvaw.getLastExecutedCommand());
    }

    @Test
    @DisplayName("command check: pvattest version")
    public void testVersion() throws ExecutionException {
        mockCommandExecutor.forceOutput("pvattest version 2.41.0-6\nCopyright IBM Corp. 2024\n");
        checkVersion(testPvattestWrapper);
    }

    private void checkVersion(PvattestWrapper pvaw) throws ExecutionException {
        assertEquals("2.41.0-6", pvaw.version());
        assertEquals("pvattest --version", pvaw.getLastExecutedCommand());
    }

    @Test
    @DisplayName("command check: pvattest create (no verify)")
    public void testCreateNoVerify() throws ExecutionException, CertificateException {
        mockCommandExecutor.forceOutput("""
                Host-key document verification is disabled. The attestation request may not be protected.\n
                Use host-key document at 'input/host_key_document.crt'\n
                Successfully generated the request\n""");
        mockCommandExecutor.doFillFilesForCreate = true;
        checkCreateNoVerify(testPvattestWrapper);
        checkCreateNoVerifyCommand(testPvattestWrapper);
    }

    private void checkCreateNoVerify(PvattestWrapper pvaw) throws ExecutionException, CertificateException {
        AttestationRequest result = pvaw.createNoVerify(PvattestTestHelper.testCertificate20240714());
        assertTrue(result.succeeded());
        assertEquals(44, result.base64AttestationProtectionKey().length());
        assertEquals(620, result.base64AttestationRequest().length());
    }

    private void checkCreateNoVerifyCommand(PvattestWrapper pvaw) {
        String command = pvaw.getLastExecutedCommand();

        assertTrue(command.startsWith("pvattest create -v "));
        assertTrue(command.contains(" -k "));
        assertTrue(command.contains(" -o "));
        assertTrue(command.contains(" -a "));
        assertTrue(command.contains(" --add-data phkh-img "));
        assertTrue(command.contains(" --add-data phkh-att"));

        assertTrue(command.contains(" --no-verify "));
        assertFalse(command.contains(" -C "));
    }

    @Test
    @DisplayName("command check: pvattest create (and verify) with certificate downloading")
    public void testCreateVerifyDownloadCertificates() throws ExecutionException, CertificateException,
            IOException, URISyntaxException {
        mockCommandExecutor.forceOutput("""
                Host-key document verification is disabled. The attestation request may not be protected.\n
                Use host-key document at 'input/host_key_document.crt'\n
                Successfully generated the request\n""");
        mockCommandExecutor.doFillFilesForCreate = true;

        Path digiCertCaPath = Paths.get(PvattestTestHelper.class.
                getResource("test_digi_cert_ca_2036_04_29.crt").toURI());
        PvattestWrapper localPvattestWrapper = new PvattestWrapper(mockCommandExecutor, digiCertCaPath);

        checkCreateVerifyDownloadCertificatesOK(localPvattestWrapper);
        checkCreateVerifyDownloadCertificatesCommand(localPvattestWrapper);
    }

    private void checkCreateVerifyDownloadCertificatesOK(PvattestWrapper pvaw)
            throws ExecutionException, CertificateException, IOException {
        AttestationRequest result =
                pvaw.createVerifyDownloadCertificates(PvattestTestHelper.testCertificate20240714());
        assertTrue(result.succeeded());
        assertEquals(44, result.base64AttestationProtectionKey().length());
        assertEquals(620, result.base64AttestationRequest().length());
    }

    private void checkCreateVerifyDownloadCertificatesKO(PvattestWrapper pvaw) {
        assertThrows(RuntimeException.class,
                () -> pvaw.createVerifyDownloadCertificates(PvattestTestHelper.testCertificate20240714()),
                "error: Host-key verification failed: After validity period");
    }

    private void checkCreateVerifyDownloadCertificatesCommand(PvattestWrapper pvaw) {
        String command = pvaw.getLastExecutedCommand();

        assertTrue(command.startsWith("pvattest create -v "));
        assertTrue(command.contains(" -k "));
        assertTrue(command.contains(" -o "));
        assertTrue(command.contains(" -a "));
        assertTrue(command.contains(" --add-data phkh-img "));
        assertTrue(command.contains(" --add-data phkh-att"));

        assertFalse(command.contains(" --no-verify "));
        assertTrue(command.contains(" -C "));
    }


    @Test
    @DisplayName("command check: pvattest verify")
    public void testVerifyAttestationResponse() throws ExecutionException {
        mockCommandExecutor.forceOutput(PvattestTestHelper.testAttestationResultOutput());
        mockCommandExecutor.doFillFilesForVerifyMeasurement = true;
        checkVerifyAttestationResponseOK(testPvattestWrapper);
        checkVerifyAttestationResponseCommand(testPvattestWrapper);
    }

    private void checkVerifyAttestationResponseOK(PvattestWrapper pvaw) throws ExecutionException {
        AttestationResponse result =
                pvaw.verifyAttestationResponse(
                        PvattestTestHelper.testAttestationResponseContentBase64()
                                .replace("\n", ""),
                        PvattestTestHelper.secureExecutionHeaderContentBase64()
                                .replace("\n", ""),
                        PvattestTestHelper.testAttestationProtectionKeyContentBase64()
                                .replace("\n", ""));
        assertTrue(result.succeeded());
        assertEquals(PvattestTestHelper.testAttestationResultYaml(), result.attestationResponseContent());
    }

    private void checkVerifyAttestationResponseKO(PvattestWrapper pvaw)  {
        assertThrowsExactly(RuntimeException.class,
                () -> pvaw.verifyAttestationResponse(PvattestTestHelper.testAttestationResponseContentBase64(),
                        PvattestTestHelper.secureExecutionHeaderContentBase64(),
                        PvattestTestHelper.testAttestationProtectionKeyContentBase64()),
                "error: Host-key verification failed: After validity period");
    }

    private void checkVerifyAttestationResponseCommand(PvattestWrapper pvaw) {
        String command = pvaw.getLastExecutedCommand();

        assertTrue(command.startsWith("pvattest verify -v "));
        assertTrue(command.contains(" -i "));
        assertTrue(command.contains(" --hdr "));
        assertTrue(command.contains(" --arpk "));
        assertTrue(command.contains(" -o "));
    }

    @Test
    @Disabled("disabled: run only in local to test real PvattestWrapper (must have s390-tools installed)")
    public void testRealPvattestWrapper() throws ExecutionException, CertificateException {
        PvattestWrapper realPvattestWrapper = new PvattestWrapper();

        checkFindPackageVersion(realPvattestWrapper);
        checkVersion(realPvattestWrapper);

        checkCreateNoVerify(realPvattestWrapper);
        checkCreateNoVerifyCommand(realPvattestWrapper);

        checkCreateVerifyDownloadCertificatesKO(realPvattestWrapper);
        checkCreateVerifyDownloadCertificatesCommand(realPvattestWrapper);

        checkVerifyAttestationResponseOK(realPvattestWrapper);
        checkVerifyAttestationResponseCommand(realPvattestWrapper);
    }

    @Test
    @Disabled("disabled: run only in local to test digiCert certificates loading")
    public void testGettingDigiCertCertificate() throws IOException, CertificateEncodingException {
        //must have file /usr/share/coco-attestation/certs/pvattest/DigiCertCA.pem present
        X509Certificate digiCert = testPvattestWrapper.getDigiCertCACertificate();
        assertNotNull(digiCert);
        assertEquals(PvattestTestHelper.testDigiCertCa20360429().replace("\n", ""),
                CertificateHelper.getPemCertificate(digiCert).replace("\n", ""));
    }

    @Test
    @Disabled("disabled: run only in local to test certificates download")
    public void testDownloadingIbmCertificates() throws IOException, CertificateEncodingException {
        X509Certificate ibmCert = testPvattestWrapper.downloadIbmZHostKeySigningCertificate();
        assertNotNull(ibmCert);

        X509CRL ibmCrl = testPvattestWrapper.downloadIbmZHostKeySigningRevocationLists();
        assertNotNull(ibmCrl);
    }

}
