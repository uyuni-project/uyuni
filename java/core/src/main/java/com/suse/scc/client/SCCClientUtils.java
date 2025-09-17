/*
 * Copyright (c) 2014--2015 SUSE LLC
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
package com.suse.scc.client;

import com.suse.utils.ParameterizedTypeImpl;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Utilities for {@link SCCClient}.
 */
public class SCCClientUtils {

    /** The gzip encoding string. */
    static final String GZIP_ENCODING = "gzip";

    /**
     * Private constructor
     */
    private SCCClientUtils() {
        // nothing to do
    }

    /**
     * Quietly close a given closeable object, suppressing exceptions.
     * @param closeable a closeable object
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        }
        catch (IOException e) {
            // ignore
        }
    }

    /**
     * Returns a buffered reader for data in the connection that will also log
     * any read data to a file in path.
     * @param requestUri the URI of the request
     * @param response the HTTP response
     * @param user the user name
     * @param logDir where to save the log file
     * @param setOwner false to skip owner setting
     * @return the logging reader
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static BufferedReader getLoggingReader(URI requestUri, HttpResponse response,
            String user, String logDir, boolean setOwner) throws IOException {
        InputStream inputStream = null;
        try {
            if (response.getEntity() == null) {
                inputStream = InputStream.nullInputStream();
            }
            else {
                inputStream = response.getEntity().getContent();
            }
            Header encodingHeader = response.getFirstHeader("Content-Encoding");
            String encoding = encodingHeader != null ? encodingHeader.getValue() : null;
            if (GZIP_ENCODING.equals(encoding)) {
                inputStream = new GZIPInputStream(inputStream);
            }
        }
        catch (IOException e) {
            closeQuietly(inputStream);
            throw e;
        }

        UserPrincipal tomcatUser = null;
        UserPrincipal rootUser = null;
        if (setOwner) {
            FileSystem fileSystem = FileSystems.getDefault();
            UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
            tomcatUser = service.lookupPrincipalByName("tomcat");
            rootUser = service.lookupPrincipalByName("root");
        }

        File logDirFile = new File(logDir);
        if (!logDirFile.exists()) {
            FileUtils.forceMkdir(logDirFile);
            Path logDirPath = logDirFile.toPath();
            if (setOwner && Files.getOwner(logDirPath, LinkOption.NOFOLLOW_LINKS).equals(rootUser)) {
                Files.setOwner(logDirPath, tomcatUser);
            }
        }

        String logFilename = getLogFilename(requestUri, user);
        File logFile = new File(logDir + File.separator + logFilename);
        if (!logFile.exists()) {
            FileUtils.touch(logFile);
        }
        Path logPath = logFile.toPath();
        if (setOwner && Files.getOwner(logPath, LinkOption.NOFOLLOW_LINKS).equals(rootUser)) {
            Files.setOwner(logPath, tomcatUser);
        }

        OutputStream fileOutputStream = new FileOutputStream(logFile);
        TeeInputStream tis = new TeeInputStream(inputStream, fileOutputStream);

        Reader inputStreamReader = new InputStreamReader(tis);
        return new BufferedReader(inputStreamReader);
    }

    /**
     * Returns a log file name from an SCC url.
     * @param uri the SCC uri
     * @param user the SCC user name
     * @return the filename
     */
    public static String getLogFilename(URI uri, String user) {
        String[] parts = uri.toString().split("/(connect|suma)/");

        String urlFragment = parts[1];
        String name = user + "_" + urlFragment + (urlFragment.endsWith(".json") ? "" : ".json");

        return name.replaceAll("[^a-zA-Z0-9\\._]+", "_");
    }

    /**
     * Returns a type which is a list of the specified type.
     * @param elementType the element type
     * @return the List type
     */
    public static Type toListType(final Type elementType) {
        return new ParameterizedTypeImpl(null, List.class, elementType);
    }
}
