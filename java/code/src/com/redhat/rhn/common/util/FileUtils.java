/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.common.util;

import com.redhat.rhn.common.RhnRuntimeException;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;
import java.util.stream.Stream;


/**
 * Simple file utilities to read/write strings to a file on disk.
 *
 */
public class FileUtils {

    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * Save a String to a file on disk using specified path.
     *
     * WARNING:  This deletes the original file before it writes.
     *
     * @param contents to save to file on disk
     * @param location to save file to.
     */
    public static void writeStringToFile(String contents, String location) {
        Path path = Path.of(location);

        try {
            Files.deleteIfExists(path);
            Files.writeString(path, contents);
        }
        catch (Exception e) {
            LOGGER.error("Error trying to write file to disk: [{}]", location, e);
            throw new RhnRuntimeException("Error trying to write file to disk", e);
        }
    }

    /**
     * convenient method to set user/group and permissions of a file in one go.
     *
     * @param path path to file
     * @param user username
     * @param group groupname
     * @param permissions set of permissions
     * @throws IOException when any of the file operations fail
     */
    public static void setAttributes(Path path, String user, String group, Set<PosixFilePermission> permissions)
            throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(
            path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS
        );
        UserPrincipalLookupService service = FileSystems.getDefault().getUserPrincipalLookupService();

        view.setOwner(service.lookupPrincipalByName(user));
        view.setGroup(service.lookupPrincipalByGroupName(group));
        view.setPermissions(permissions);
    }


    /**
     * Read a file off disk into a String and return it.
     *
     * Expect weird stuff if the file is not textual.
     *
     * @param path of file to read in
     * @return String containing file.
     */
    public static String readStringFromFile(String path) {
        return readStringFromFile(path, false);
    }


    /**
     * Read a file off disk into a String and return it.
     *
     * Expect weird stuff if the file is not textual.
     *
     * @param path of file to read in
     * @param noLog don't log the content of the file
     * @return String containing file.
     */
    public static String readStringFromFile(String path, boolean noLog) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readStringFromFile: {}", StringUtil.sanitizeLogInput(path));
        }

        try {
            String contents = Files.readString(Path.of(path));
            if (noLog && LOGGER.isDebugEnabled()) {
                LOGGER.debug("contents: {}", contents);
            }
            return contents;
        }
        catch (IOException e) {
            throw new RhnRuntimeException("Unable to load file content", e);
        }
    }

    /**
     * Read a file off disk into a byte array with specified range
     *
     * This can use lots of memory if you read a large file
     *
     * @param fileToRead File to read part of into byte array
     * @param start index of read
     * @param end index of read
     * @return byte[] array from file.
     */
    public static byte[] readByteArrayFromFile(File fileToRead, long start, long end) {
        LOGGER.debug("readByteArrayFromFile: {} start: {} end: {}", fileToRead.getAbsolutePath(), start, end);

        int size = (int) (end - start);
        LOGGER.debug("size of array: {}", size);

        try (SeekableByteChannel byteChannel = Files.newByteChannel(fileToRead.toPath(), StandardOpenOption.READ)) {
            // Skip ahead
            byteChannel.position(start);

            // Create the byte array to hold the data
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            byteChannel.read(byteBuffer);

            return byteBuffer.array();
        }
        catch (IOException e) {
            LOGGER.error("Could not read from: {}", fileToRead.getAbsolutePath());
            throw new RhnRuntimeException("Could not read bytes from file", e);
        }
    }

    /**
     * Reads and returns the last n lines from a given file as string.
     * @param pathToFile path to file
     * @param lines size of the tail
     * @return tail of file as string
     */
    public static String getTailOfFile(String pathToFile, Integer lines) {
        CircularFifoBuffer buffer = new CircularFifoBuffer(lines);

        try (Stream<String> linesStream = Files.lines(Path.of(pathToFile))) {
            linesStream.forEach(line -> buffer.add(line));
        }
        catch (IOException e) {
            LOGGER.error("File not found: {}", pathToFile);
            throw new RhnRuntimeException(e);
        }

        // Construct a string from the buffered lines
        StringBuilder sb = new StringBuilder();
        for (Object s : buffer) {
            sb.append(s);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    /**
     * Delete file with a given path.
     * @param path path of the file to be deleted
     */
    public static void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        }
        catch (IOException e) {
            LOGGER.warn("Could not delete file: {}", path, e);
        }
    }
}
