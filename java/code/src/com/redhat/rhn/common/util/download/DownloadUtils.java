/**
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
package com.redhat.rhn.common.util.download;


import org.apache.log4j.Logger;
import org.jfree.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * DownloadUtils
 * @version $Rev$
 */
public class DownloadUtils {

    private static final Logger LOGGER = Logger.getLogger(DownloadUtils.class);

    private DownloadUtils() {

    }

    /**
     * Downloads text from the URL and returns it as a string
     * @param url the url
     * @return the text downloaded
     */
    public static String downloadUrl(String url) {
        URL u;
        InputStream is = null;
        StringBuilder toReturn = new StringBuilder();
        try {
           u = new URL(url);
           HttpURLConnection conn = (HttpURLConnection)u.openConnection();
           if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
               throw new DownloadException(url,
                               toString(conn.getErrorStream()),
                               conn.getResponseCode());
           }
           toReturn.append(toString(conn.getInputStream()));
        }
        catch (MalformedURLException mue) {
            toReturn.append(mue.getLocalizedMessage());
        }
        catch (IOException ioe) {
            toReturn.append(ioe.getLocalizedMessage());
        }
        finally {
           try {
              if (is != null) {
                  is.close();
              }
           }
           catch (IOException ioe) {
               toReturn.append(ioe.getLocalizedMessage());
           }
        }
        return toReturn.toString();
    }

    public static boolean downloadToFile(String url, String path) {
	URL u;
        InputStream is = null;
        FileOutputStream out = null;
	try {
           u = new URL(url);
           HttpURLConnection conn = (HttpURLConnection)u.openConnection();
           if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
	       LOGGER.error(toString(conn.getErrorStream()));
               throw new DownloadException(url,
                               toString(conn.getErrorStream()),
                               conn.getResponseCode());
           }
           File f = new File(path);
	   if( ! f.getParentFile().exists() && ! f.getParentFile().mkdirs() ) {
               LOGGER.error("Creating directories failed");
               return false;
           }
           boolean exist = f.createNewFile();
           if (!exist) {
	       LOGGER.error("File already exists");
               throw new DownloadException(url, "File already exists.", 500);
           }
           else {
             out = new FileOutputStream(f);
             int read = 0;
             byte[] bytes = new byte[1024];
             is = conn.getInputStream();
             while ((read = is.read(bytes)) != -1) {
               out.write(bytes, 0, read);
             }
           }
        }
        catch (MalformedURLException mue) {
	    LOGGER.error(mue.getMessage(), mue);
            return false;
        }
        catch (IOException ioe) {
	    LOGGER.error(ioe.getMessage(), ioe);
            return false;
        }
        finally {
           try {
              if (is != null) {
                  is.close();
              }
              if (out != null) {
                  out.flush();
                  out.close();
              }
           }
           catch (IOException ioe) {
	       LOGGER.error(ioe.getMessage(), ioe);
               return false;
           }
        }
        return true;
    }


    private static String toString(InputStream stream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.getInstance().copyWriter(
                new BufferedReader(new InputStreamReader(stream)), writer);
        return writer.toString();
    }
}
