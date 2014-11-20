/**
 * Copyright (c) 2014 SUSE
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

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * @param connection the connection
     * @param user the user name
     * @param logDir where to save the log file
     * @return the logging reader
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static BufferedReader getLoggingReader(URLConnection connection, String user,
            String logDir) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
            if (GZIP_ENCODING.equals(connection.getContentEncoding())) {
                inputStream = new GZIPInputStream(inputStream);
            }
        }
        catch (IOException e) {
            closeQuietly(inputStream);
            throw e;
        }

        FileUtils.forceMkdir(new File(logDir));

        String path = logDir + File.separator + getLogFilename(connection.getURL(), user);
        OutputStream fileOutputStream = new FileOutputStream(path);
        TeeInputStream tis = new TeeInputStream(inputStream, fileOutputStream);

        Reader inputStreamReader = new InputStreamReader(tis);
        return new BufferedReader(inputStreamReader);
    }

    /**
     * Returns a log file name from an SCC url.
     * @param url the url
     * @param user the SCC user name
     * @return the filename
     */
    public static String getLogFilename(URL url, String user) {
        Pattern pattern = Pattern.compile(".*/connect/(.*)");
        Matcher matcher = pattern.matcher(url.toString());
        matcher.matches();
        String urlFragment = matcher.group(1);
        String name = user + "_" + urlFragment + ".json";

        return name.replaceAll("[^a-zA-Z0-9\\._]+", "_");
    }

    /**
     * Returns a type which is a list of the specified type.
     * @param elementType the element type
     * @return the List type
     */
    public static Type toListType(final Type elementType) {
        Type resultListType = new ParameterizedType() {

            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {elementType};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return resultListType;
    }
}
