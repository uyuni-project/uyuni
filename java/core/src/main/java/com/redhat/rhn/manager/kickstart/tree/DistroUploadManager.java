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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.manager.kickstart.tree;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.manager.satellite.SystemCommandThreadedExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stores uploaded autoinstallation distribution media.
 */
public class DistroUploadManager {

    private static final Logger LOG = LogManager.getLogger(DistroUploadManager.class);

    private static final int MAX_COMMAND_OUTPUT_LENGTH = 2300;

    /** Web root used by the existing mgradm distro copy workflow. */
    public static final Path DEFAULT_DISTRIBUTIONS_PATH = Paths.get("/srv/www/distributions");

    /**
     * Staging directory below the distributions path, on the same filesystem so the extracted tree
     * can be renamed into place, but not readable by the web server.
     */
    private static final String STAGING_DIRECTORY_NAME = ".upload";

    /** Fixed prefix for staged files, so the name length is independent of the distribution name. */
    private static final String TEMP_PREFIX = "distro.";

    private static final AtomicBoolean UPLOAD_IN_PROGRESS = new AtomicBoolean();

    private static final Set<PosixFilePermission> STAGING_PERMISSIONS = PosixFilePermissions.fromString("rwx------");
    private static final Set<PosixFilePermission> DISTRO_TREE_PERMISSIONS =
            PosixFilePermissions.fromString("rwxr-xr-x");

    private final Path distributionsPath;
    private final IsoExtractor isoExtractor;

    /**
     * Materializes the uploaded ISO media at a given location.
     */
    @FunctionalInterface
    public interface IsoWriter {
        /**
         * Write the uploaded ISO to the given path, replacing the empty placeholder file already
         * present there.
         * @param isoPath the path the ISO must be written to
         * @throws IOException if the ISO cannot be written
         */
        void writeTo(Path isoPath) throws IOException;
    }

    /**
     * Extracts uploaded ISO media into an installation tree.
     */
    @FunctionalInterface
    public interface IsoExtractor {
        /**
         * Extract ISO media into a directory.
         * @param isoPath the uploaded ISO path
         * @param destinationPath the destination directory
         * @throws IOException if the ISO cannot be extracted
         */
        void extract(Path isoPath, Path destinationPath) throws IOException;
    }

    /**
     * Create a manager using the default distribution web root.
     */
    public DistroUploadManager() {
        this(DEFAULT_DISTRIBUTIONS_PATH);
    }

    /**
     * Create a manager using a custom distribution web root.
     * @param distributionsPathIn the directory where distribution media is stored
     */
    public DistroUploadManager(Path distributionsPathIn) {
        this(distributionsPathIn, new XorrisoIsoExtractor());
    }

    /**
     * Create a manager using a custom distribution web root and ISO extractor.
     * @param distributionsPathIn the directory where distribution trees are stored
     * @param isoExtractorIn the extractor used to unpack ISO media
     */
    public DistroUploadManager(Path distributionsPathIn, IsoExtractor isoExtractorIn) {
        this.distributionsPath = Objects.requireNonNull(distributionsPathIn).toAbsolutePath().normalize();
        this.isoExtractor = Objects.requireNonNull(isoExtractorIn);
    }

    /**
     * Create the distribution storage directory if needed and return it.
     * @return the directory where distribution media is stored
     * @throws IOException if the directory cannot be created or is not a directory
     */
    public Path prepareDistributionsPath() throws IOException {
        Files.createDirectories(distributionsPath);
        if (!Files.isDirectory(distributionsPath)) {
            throw new IOException("Distribution upload path is not a directory: " + distributionsPath);
        }
        return distributionsPath;
    }

    /**
     * Create the private staging directory used for in-progress uploads if needed and return it.
     * @return the directory where uploads are stored until they are extracted
     * @throws IOException if the directory cannot be created
     */
    public Path prepareStagingPath() throws IOException {
        Path stagingPath = prepareDistributionsPath().resolve(STAGING_DIRECTORY_NAME);
        return Files.createDirectories(stagingPath, PosixFilePermissions.asFileAttribute(STAGING_PERMISSIONS));
    }

    /**
     * Mark the start of an upload. Only one upload can run at a time on the server, to bound the
     * disk space and extraction load it causes.
     * @return true if the upload can start, false if another upload is already in progress
     */
    public boolean tryStartUpload() {
        return UPLOAD_IN_PROGRESS.compareAndSet(false, true);
    }

    /**
     * Mark the end of an upload started with {@link #tryStartUpload()}.
     */
    public void finishUpload() {
        UPLOAD_IN_PROGRESS.set(false);
    }

    /**
     * Check that an upload of the given size fits into the staging directory together with its
     * extracted tree. The maximum upload size is enforced by LimitRequestBody in zz-spacewalk-www.conf.
     * @param uploadSize the upload size in bytes, or a negative value if it is unknown
     * @throws IOException if the size is unknown or there is not enough free space
     */
    public void checkUploadSize(long uploadSize) throws IOException {
        if (uploadSize < 0) {
            throw new IOException("Distribution upload requires a Content-Length header");
        }
        ensureUsableSpace(2 * uploadSize);
    }

    private void ensureUsableSpace(long requiredBytes) throws IOException {
        long usableBytes = Files.getFileStore(prepareStagingPath()).getUsableSpace();
        if (usableBytes < requiredBytes) {
            throw new IOException("Not enough free space in " + distributionsPath + ": " + requiredBytes +
                    " bytes required, " + usableBytes + " bytes available");
        }
    }

    /**
     * Store a distribution upload as an extracted installation tree.
     * @param filename the distribution name or original ISO file name
     * @param distroStream the uploaded content
     * @return the destination path
     * @throws IOException if the upload cannot be stored
     */
    public Path uploadDistro(String filename, InputStream distroStream) throws IOException {
        return uploadDistro(filename, isoPath -> {
            try (InputStream stream = distroStream) {
                Files.copy(stream, isoPath, StandardCopyOption.REPLACE_EXISTING);
            }
        });
    }

    /**
     * Store a distribution upload as an extracted installation tree.
     * @param filename the distribution name or original ISO file name
     * @param isoWriter writes the uploaded ISO to the temporary location it is extracted from
     * @return the destination path
     * @throws IOException if the upload cannot be stored
     */
    public Path uploadDistro(String filename, IsoWriter isoWriter) throws IOException {
        String safeDistroName = getSafeDistroName(filename);

        Path stagingPath = prepareStagingPath();

        Path destination = distributionsPath.resolve(safeDistroName).normalize();
        if (!destination.startsWith(distributionsPath)) {
            throw new IOException("Invalid distribution upload filename: " + filename);
        }
        if (Files.exists(destination)) {
            throw new FileAlreadyExistsException(destination.toString(), null, "Distribution already exists");
        }

        Path tempIso = Files.createTempFile(stagingPath, TEMP_PREFIX, ".iso");
        Path tempDirectory = Files.createTempDirectory(stagingPath, TEMP_PREFIX);
        try {
            isoWriter.writeTo(tempIso);
            // The extracted tree takes about as much space as the ISO itself
            ensureUsableSpace(Files.size(tempIso));
            isoExtractor.extract(tempIso, tempDirectory);
            // createTempDirectory uses 0700, the published tree must be readable by the web server
            Files.setPosixFilePermissions(tempDirectory, DISTRO_TREE_PERMISSIONS);
            moveIntoPlace(tempDirectory, destination);
            return destination;
        }
        catch (IOException | RuntimeException e) {
            deleteDirectoryIfExists(tempDirectory);
            throw e;
        }
        finally {
            deleteFileIfExists(tempIso);
        }
    }

    private static void moveIntoPlace(Path tempDirectory, Path destination) throws IOException {
        try {
            Files.move(tempDirectory, destination, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e) {
            Files.move(tempDirectory, destination);
        }
    }

    private static String getSafeDistroName(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IOException("Distribution upload filename is required");
        }

        String trimmedFilename = filename.trim();
        if (trimmedFilename.contains("/") || trimmedFilename.contains("\\")) {
            throw new IOException("Distribution upload filename must not contain path segments: " + filename);
        }

        String safeFilename = Paths.get(trimmedFilename).getFileName().toString().trim();
        if (safeFilename.toLowerCase(Locale.ROOT).endsWith(".iso")) {
            safeFilename = safeFilename.substring(0, safeFilename.length() - ".iso".length()).trim();
        }
        if (safeFilename.isEmpty() || safeFilename.startsWith(".")) {
            throw new IOException("Invalid distribution upload filename: " + filename);
        }
        if (!safeFilename.equals(trimmedFilename) && !(safeFilename + ".iso").equalsIgnoreCase(trimmedFilename)) {
            throw new IOException("Distribution upload filename must not contain path segments: " + filename);
        }
        return safeFilename;
    }

    private static void deleteDirectoryIfExists(Path directory) {
        if (!Files.exists(directory)) {
            return;
        }
        try {
            // ISO media usually contains read-only directories, which prevent deleting their content
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.setPosixFilePermissions(dir, STAGING_PERMISSIONS);
                    return FileVisitResult.CONTINUE;
                }
            });
            FileUtils.deleteDirectory(directory.toFile());
        }
        catch (IOException e) {
            LOG.warn("Unable to delete temporary distribution extraction directory {}", directory, e);
        }
    }

    private static void deleteFileIfExists(Path file) {
        try {
            Files.deleteIfExists(file);
        }
        catch (IOException e) {
            LOG.warn("Unable to delete temporary distribution upload file {}", file, e);
        }
    }

    private static class XorrisoIsoExtractor implements IsoExtractor {
        @Override
        public void extract(Path isoPath, Path destinationPath) throws IOException {
            executeExtCmd(new String[] {
                    "xorriso", "-osirrox", "on", "-indev", isoPath.toString(), "-extract", "/",
                    destinationPath.toString()
            });
        }

        private static void executeExtCmd(String[] args) throws IOException {
            SystemCommandThreadedExecutor commandExecutor = new SystemCommandThreadedExecutor(LOG, true);
            int exitCode;
            try {
                exitCode = commandExecutor.execute(args);
            }
            catch (RhnRuntimeException e) {
                throw new IOException("Unable to execute command " + Arrays.asList(args), e);
            }

            if (exitCode != 0) {
                String message = commandExecutor.getLastCommandErrorMessage();
                if (message == null || message.isBlank()) {
                    message = commandExecutor.getLastCommandOutput();
                }
                message = truncateCommandOutput(message);
                throw new IOException("Command '" + Arrays.asList(args) + "' exited with error code " + exitCode +
                        (message.isBlank() ? "" : ": " + message));
            }
        }

        private static String truncateCommandOutput(String message) {
            if (message == null) {
                return "";
            }
            if (message.length() > MAX_COMMAND_OUTPUT_LENGTH) {
                return "... " + message.substring(message.length() - MAX_COMMAND_OUTPUT_LENGTH);
            }
            return message;
        }
    }
}
