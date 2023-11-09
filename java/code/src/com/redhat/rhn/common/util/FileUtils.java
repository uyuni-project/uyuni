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
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;



/**
 * Simple file utilities to read/write strings to a file on disk.
 *
 */
public class FileUtils {

    private static Logger log = LogManager.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * Save a String to a file on disk using specified path.
     *
     * WARNING:  This deletes the original file before it writes.
     *
     * @param contents to save to file on disk
     * @param path to save file to.
     */
    public static void writeStringToFile(String contents, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                Files.delete(file.toPath());
            }
            file.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(file);
                 FileWriter fw = new FileWriter(fos.getFD());
                 BufferedWriter output = new BufferedWriter(fw)) {
                output.write(contents);
                output.flush();
                fw.flush();
                fos.getFD().sync();
            }
        }
        catch (Exception e) {
            log.error("Error trying to write file to disk: [{}]", path, e);
            throw new RhnRuntimeException(e);
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
        FileSystem fileSystem = FileSystems.getDefault();
        UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
        PosixFileAttributeView view  = Files.getFileAttributeView(
                path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
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
        if (log.isDebugEnabled()) {
            log.debug("readStringFromFile: {}", StringUtil.sanitizeLogInput(path));
        }

        try {
            String contents = Files.readString(Path.of(path));
            if (noLog && log.isDebugEnabled()) {
                log.debug("contents: {}", contents);
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
        log.debug("readByteArrayFromFile: {} start: {} end: {}", fileToRead.getAbsolutePath(), start, end);

        int size = (int) (end - start);
        log.debug("size of array: {}", size);
        // Create the byte array to hold the data
        byte[] bytes = new byte[size];
        try (InputStream is = new FileInputStream(fileToRead)) {
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            // Skip ahead
            is.skip(start);
            // start reading
            while (offset < bytes.length &&
                    (numRead) >= 0) {
                numRead = is.read(bytes, offset,
                        bytes.length - offset);
                offset += numRead;
            }
        }
        catch (IOException fnf) {
            log.error("Could not read from: {}", fileToRead.getAbsolutePath());
            throw new RhnRuntimeException(fnf);
        }
        return bytes;
    }

    /**
     * Reads and returns the last n lines from a given file as string.
     * @param pathToFile path to file
     * @param lines size of the tail
     * @return tail of file as string
     */
    public static String getTailOfFile(String pathToFile, Integer lines) {
        CircularFifoBuffer buffer = new CircularFifoBuffer(lines);
        try (InputStream fileStream = new FileInputStream(pathToFile)) {
            LineIterator it = org.apache.commons.io.IOUtils.lineIterator(fileStream,
                                                                         (String) null);
            while (it.hasNext()) {
                buffer.add(it.nextLine());
            }
        }
        catch (FileNotFoundException e) {
            log.error("File not found: {}", pathToFile);
            throw new RhnRuntimeException(e);
        }
        catch (IOException e) {
            log.error(String.format("Failed to close file %s", pathToFile));
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
            log.warn("Could not delete file: {}", path, e);
        }
    }
}
