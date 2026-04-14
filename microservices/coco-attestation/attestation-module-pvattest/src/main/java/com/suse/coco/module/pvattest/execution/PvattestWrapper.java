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

import com.suse.coco.module.pvattest.model.AttestationRequest;
import com.suse.coco.module.pvattest.model.AttestationResponse;
import com.suse.common.io.process.ProcessOutput;
import com.suse.common.io.process.ShellCommandExecutor;
import com.suse.common.security.CertificateHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class PvattestWrapper {
    private static final Logger LOGGER = LogManager.getLogger(PvattestWrapper.class);
    private static final String PVATTEST_PACKAGE = "s390-tools";

    //The CA certificate, here from DigiCert, in DigiCertCA.crt
    public static final String DIGICERT_CA_CERTIFICATE =
            "https://www.ibm.com/support/resourcelink/api/content/public/DigiCertCA.crt";
    //The IBM Z signing-key certificate in SigningKey.crt
    public static final String IBM_Z_HOST_KEY_SIGNING_CERTIFICATE =
            "https://www.ibm.com/support/resourcelink/api/content/public/ibm-z-host-key-signing-gen2.crt";
    //The IBM Z certificate-revocation lists
    public static final String IBM_Z_HOST_KEY_CERTIFICATE_REVOCATION_LISTS =
            "https://www.ibm.com/support/resourcelink/api/content/public/ibm-z-host-key-gen2.crl";


    private final FileAttribute<Set<PosixFilePermission>> tempDirAttributes =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
    private final FileAttribute<Set<PosixFilePermission>> tempFileAttributes =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));

    private final List<Path> tempDirs = new ArrayList<>();
    private final List<Path> tempFiles = new ArrayList<>();

    private final ShellCommandExecutor commandExecutor;

    /**
     * default constructor
     */
    public PvattestWrapper() {
        this(new ShellCommandExecutor());
    }

    /**
     *  Constructor
     *  @param commandExecutorIn the command executor object
     */
    public PvattestWrapper(ShellCommandExecutor commandExecutorIn) {
        commandExecutor = commandExecutorIn;
    }

    //package private
    String getLastExecutedCommand() {
        return commandExecutor.getLastExecutedCommand();
    }

    /**
     * Finds the package version
     * @return the package version as string
     * @throws ExecutionException if something goes wrong
     */
    public String findPackageVersion() throws ExecutionException {
        try {
            ProcessOutput output = commandExecutor.executeProcess(
                    "/usr/bin/rpm", "-q", "--queryformat", "%{VERSION}", PVATTEST_PACKAGE);
            checkCommandNotFailed(output);
            return getFirstLine(output.standardOutput());
        }
        catch (ExecutionException e) {
            logErrorRunningCommand(e);
            throw e;
        }
    }

    /**
     * Returns the pvattest tool version
     * @return the version as string
     * @throws ExecutionException if something goes wrong
     */
    public String version() throws ExecutionException {
        try {
            ProcessOutput output = commandExecutor.executeProcess("pvattest", "--version");
            checkCommandNotFailed(output);
            return getFirstLine(output.standardOutput()).replaceFirst("pvattest version ", "");
        }
        catch (ExecutionException e) {
            logErrorRunningCommand(e);
            throw e;
        }
    }

    /**
     * Creates an attestation request, with no certificate verification
     * @param hostKeyDocumentContent the IBM host key document
     * @return the attestation request
     * @throws ExecutionException   if something goes wrong
     * @throws CertificateException if some certificate could not be parsed or is invalid
     */
    public AttestationRequest createNoVerify(String hostKeyDocumentContent)
            throws ExecutionException, CertificateException {
        X509Certificate hostKeyDocument = CertificateHelper.parse(hostKeyDocumentContent);
        return createCore(hostKeyDocument, false, null, null);
    }

    /**
     * Creates an attestation request, with certificate verification, downloading the certificates from IBM website
     * @param hostKeyDocumentContent the IBM host key document
     * @return the attestation request
     * @throws ExecutionException   if something goes wrong
     * @throws CertificateException if some certificate could not be parsed or is invalid
     * @throws IOException          if something goes wrong
     */
    public AttestationRequest createVerifyDownloadCertificates(String hostKeyDocumentContent)
            throws ExecutionException, CertificateException, IOException {
        X509Certificate hostKeyDocument = CertificateHelper.parse(hostKeyDocumentContent);
        X509Certificate digiCertCACertificate = downloadDigiCertCACertificate();
        X509Certificate ibmZHostKeySigningCertificate = downloadIbmZHostKeySigningCertificate();
        return createCore(hostKeyDocument, true, digiCertCACertificate, ibmZHostKeySigningCertificate);
    }

    /**
     * Creates an attestation request, with certificate verification, given the certificate contents
     * @param hostKeyDocumentContent  the IBM host key document
     * @param digiCertCACertificateContent         the root CA certificate
     * @param ibmZHostKeySigningCertificateContent the IBM host key signing certificate
     * @return the attestation request
     * @throws ExecutionException   if something goes wrong
     * @throws CertificateException if some certificate could not be parsed or is invalid
     */
    public AttestationRequest createVerify(String hostKeyDocumentContent,
                                                        String digiCertCACertificateContent,
                                                        String ibmZHostKeySigningCertificateContent)
            throws ExecutionException, CertificateException {
        X509Certificate hostKeyDocument = CertificateHelper.parse(hostKeyDocumentContent);
        X509Certificate digiCertCACertificate = CertificateHelper.parse(digiCertCACertificateContent);
        X509Certificate ibmZHostKeySigningCertificate = CertificateHelper.parse(ibmZHostKeySigningCertificateContent);
        return createCore(hostKeyDocument, true, digiCertCACertificate, ibmZHostKeySigningCertificate);
    }


    private AttestationRequest createCore(X509Certificate hostKeyDocument,
                                                       boolean doVerify,
                                                       X509Certificate digiCertCACertificate,
                                                       X509Certificate ibmZHostKeySigningCertificateX)
            throws ExecutionException {
        resetTempDirsFiles();
        try {
            Path tempDir = createTempDir();

            Path hostKeyDocumentFile = createTempFile(tempDir,
                    "host_key_document_", ".crt",
                    CertificateHelper.getPemCertificate(hostKeyDocument));
            Path attestationRequestFile = createTempFile(tempDir,
                    "attestation_request_", ".bin");
            Path attestationProtectionKeyFile = createTempFile(tempDir,
                    "attestation_protection_key_", ".key");

            Path digiCertCACertificateFile = null;
            Path ibmZHostKeySigningCertificateFile = null;
            if (doVerify) {
                digiCertCACertificateFile = createTempFile(tempDir,
                        "DigiCertCA_", ".crt",
                        CertificateHelper.getPemCertificate(digiCertCACertificate));
                ibmZHostKeySigningCertificateFile = createTempFile(tempDir,
                        "ibm-z-host-key-signing-gen2_", ".crt",
                        CertificateHelper.getPemCertificate(ibmZHostKeySigningCertificateX));
            }

            List<String> commandLines = new ArrayList<>();
            commandLines.add("pvattest");
            commandLines.add("create"); //create an attestation measurement request
            commandLines.add("-v"); //verbose: provide more detailed output

            commandLines.add("-k"); //-k <FILE>: use FILE as a host-key document
            commandLines.add(hostKeyDocumentFile.toAbsolutePath().toString());

            commandLines.add("-o"); //-o <FILE>: write the generated request to FILE
            commandLines.add(attestationRequestFile.toAbsolutePath().toString());

            commandLines.add("-a"); //-a <FILE>: save the protection key as unencrypted GCM-AES256 key in FILE
            commandLines.add(attestationProtectionKeyFile.toAbsolutePath().toString());

            if (doVerify) {
                //-C <FILE>: ise FILE as a certificate to verify the host-key or keys.
                // The certificates are used to establish a chain of trust for the verification
                // of the host-key documents. Specify this option twice to specify
                // the IBM Z signing key and the intermediate CA certificate (signed by the root CA).

                commandLines.add("-C");
                commandLines.add(digiCertCACertificateFile.toAbsolutePath().toString());

                commandLines.add("-C");
                commandLines.add(ibmZHostKeySigningCertificateFile.toAbsolutePath().toString());
            }
            else {
                // --no-verify: disable the host-key document verification.
                // does not require the host-key documents to be valid.
                // do not use for a production request unless you verified
                // the host-key document beforehand.

                commandLines.add("--no-verify");
            }

            commandLines.add("--add-data"); // --add-data <FLAGS>: specify additional data for the request
            commandLines.add("phkh-img");   // phkh-img: request the public host-key-hash of the key
            // that decrypted the SE-image as additional-data

            commandLines.add("--add-data"); // --add-data <FLAGS>: specify additional data for the request
            commandLines.add("phkh-att");   //phkh-att: request the public host-key-hash of the key
            // that decrypted the attestation request as additional-data

            ProcessOutput output =
                    commandExecutor.executeProcess(commandLines.toArray(new String[0]));

            checkCommandNotFailed(output);

            return createAttestationRequest(attestationRequestFile, attestationProtectionKeyFile);
        }
        catch (ExecutionException e) {
            logErrorRunningCommand(e);
            throw e;
        }
        catch (IOException e) {
            logErrorRunningCommand(e);
        }
        catch (CertificateEncodingException eIn) {
            throw new RuntimeException(eIn);
        }
        finally {
            removeAllTempDirsFiles();
        }

        return new AttestationRequest(null, null);
    }

    /**
     * @return the root CA certificate
     * @throws IOException if something goes wrong
     */
    public X509Certificate downloadDigiCertCACertificate() throws IOException {
        return CertificateHelper.downloadCertificate(DIGICERT_CA_CERTIFICATE);
    }

    /**
     * Downloads the IBM host key signing certificate
     * @return the IBM host key signing certificate
     * @throws IOException if something goes wrong
     */
    public X509Certificate downloadIbmZHostKeySigningCertificate() throws IOException {
        return CertificateHelper.downloadCertificate(IBM_Z_HOST_KEY_SIGNING_CERTIFICATE);
    }

    /**
     * Downloads the IBM host key signing certificate revocation lists
     * @return the IBM host key signing certificate revocation lists
     * @throws IOException if something goes wrong
     */
    public X509CRL downloadIbmZHostKeySigningRevocationLists() throws IOException {
        return CertificateHelper.downloadCertificateRevocationList(IBM_Z_HOST_KEY_CERTIFICATE_REVOCATION_LISTS);
    }


    private AttestationRequest createAttestationRequest(Path attestationRequestFile, Path attestationProtectionKeyFile)
            throws IOException {
        byte[] attestationRequestFileBuffer = Files.readAllBytes(attestationRequestFile);
        byte[] attestationProtectionKeyFileBuffer = Files.readAllBytes(attestationProtectionKeyFile);

        return new AttestationRequest(Base64.getEncoder().encodeToString(attestationRequestFileBuffer),
                Base64.getEncoder().encodeToString(attestationProtectionKeyFileBuffer));
    }

    private AttestationResponse createAttestationResult(Path attestationResultFile)
            throws IOException {
        String attestationResultContent = Files.readString(attestationResultFile);

        return new AttestationResponse(attestationResultContent);
    }


    private void checkCommandNotFailed(ProcessOutput output) {
        if (output.failed()) {
            LOGGER.error("Failure: exit code %d when running command [%s]%n%s%n"
                    .formatted(output.exitCode(), commandExecutor.getLastExecutedCommand(), output.getErrorMessage()));

            throw new RuntimeException(output.getErrorMessage());
        }
    }


    private void logErrorRunningCommand(Exception e) {
        LOGGER.error("Failure when running command {}: {}",
                commandExecutor.getLastExecutedCommand(), e.getMessage());
    }


    private void resetTempDirsFiles() {
        tempDirs.clear();
        tempFiles.clear();
    }

    private void removeAllTempDirsFiles() {
        for (Path tempFile : tempFiles) {
            try {
                Files.deleteIfExists(tempFile);
            }
            catch (IOException e) {
                LOGGER.error("Failed to delete: {}", tempFile);
            }
        }
        for (Path tempDir : tempDirs) {
            try {
                Files.deleteIfExists(tempDir);
            }
            catch (IOException e) {
                LOGGER.error("Failed to delete: {}", tempDir);
            }
        }
        resetTempDirsFiles();
    }

    private Path createTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("cocotmp", tempDirAttributes);
        tempDirs.add(tempDir);
        return tempDir;
    }

    private Path createTempFile(Path tempDir, String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(tempDir, prefix, suffix, tempFileAttributes);
        tempFiles.add(tempFile);
        return tempFile;
    }

    private Path createTempFile(Path tempDir, String prefix, String suffix, String content) throws IOException {
        Path tempFile = Files.createTempFile(tempDir, prefix, suffix, tempFileAttributes);
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
        tempFiles.add(tempFile);
        return tempFile;
    }

    private Path createTempBinaryFile(Path tempDir, String prefix, String suffix, String base64EncodedContent)
            throws IOException {
        Path tempFile = Files.createTempFile(tempDir, prefix, suffix, tempFileAttributes);
        Files.write(tempFile, Base64.getDecoder().decode(base64EncodedContent.replace("\n", "")));
        tempFiles.add(tempFile);
        return tempFile;
    }

    private Path createTempBinaryFile(Path tempDir, String prefix, String suffix, byte[] content)
            throws IOException {
        Path tempFile = Files.createTempFile(tempDir, prefix, suffix, tempFileAttributes);
        Files.write(tempFile, content);
        tempFiles.add(tempFile);
        return tempFile;
    }

    private String getFirstLine(String str) {
        if (null != str) {
            return str.split("\n")[0];
        }
        return null;
    }

    /**
     * Verifies the validity of an attestation response
     * @param attestationResponseContentBase64      the attestation response in base64
     * @param secureExecutionHeaderContentBase64    the secure execution header in base64
     * @param attestationProtectionKeyContentBase64 the attestation protection key in base64
     * @return the attestation response
     * @throws ExecutionException if something goes wrong
     */
    public AttestationResponse verifyAttestationResponse(String attestationResponseContentBase64,
                                                                      String secureExecutionHeaderContentBase64,
                                                                      String attestationProtectionKeyContentBase64)
            throws ExecutionException {
        return verifyAttestationResponse(
                Base64.getDecoder().decode(attestationResponseContentBase64.replace("\n", "")),
                Base64.getDecoder().decode(secureExecutionHeaderContentBase64.replace("\n", "")),
                Base64.getDecoder().decode(attestationProtectionKeyContentBase64.replace("\n", "")));
    }

    /**
     * Verifies the validity of an attestation response
     * @param attestationResponseContent      the attestation response as byte array
     * @param secureExecutionHeaderContent    the secure execution header as byte array
     * @param attestationProtectionKeyContent the attestation protection key as byte array
     * @return the attestation response
     * @throws ExecutionException if something goes wrong
     */
    public AttestationResponse verifyAttestationResponse(byte[] attestationResponseContent,
                                                                      byte[] secureExecutionHeaderContent,
                                                                      byte[] attestationProtectionKeyContent)
            throws ExecutionException {
        resetTempDirsFiles();
        try {
            Path tempDir = createTempDir();

            Path attestationResponseFile = createTempBinaryFile(tempDir,
                    "attestation_response_", ".bin", attestationResponseContent);
            Path secureExecutionHeaderFile = createTempBinaryFile(tempDir,
                    "secure_extension_header_", ".bin", secureExecutionHeaderContent);
            Path attestationProtectionKeyFile = createTempBinaryFile(tempDir,
                    "attestation_protection_key_", ".key", attestationProtectionKeyContent);

            Path attestationResultFile = createTempFile(tempDir, "attestation_result_", ".yaml");

            List<String> commandLines = new ArrayList<>();
            commandLines.add("pvattest");
            commandLines.add("verify"); //Verify an attestation response
            commandLines.add("-v"); //verbose: provide more detailed output

            commandLines.add("-i"); //-i <FILE> Specify the attestation response to be verified
            commandLines.add(attestationResponseFile.toAbsolutePath().toString());

            commandLines.add("--hdr"); //--hdr <FILE> Specifies the IBM Secure Execution header of the guest image.
            commandLines.add(secureExecutionHeaderFile.toAbsolutePath().toString());

            commandLines.add("--arpk"); //--arpk <FILE> Use FILE as the protection key to decrypt the request
            commandLines.add(attestationProtectionKeyFile.toAbsolutePath().toString());

            commandLines.add("-o"); //-o <FILE> Specify the output for the verification result
            commandLines.add(attestationResultFile.toAbsolutePath().toString());

            ProcessOutput output =
                    commandExecutor.executeProcess(commandLines.toArray(new String[0]));

            checkCommandNotFailed(output);

            return createAttestationResult(attestationResultFile);
        }
        catch (ExecutionException e) {
            logErrorRunningCommand(e);
            throw e;
        }
        catch (IOException e) {
            logErrorRunningCommand(e);
        }
        finally {
            removeAllTempDirsFiles();
        }

        return new AttestationResponse(null);
    }


}
