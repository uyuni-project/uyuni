/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

package com.redhat.rhn.common.finder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * An interface to find classes that implement a given interface.
 *
 */
class JarFinder implements Finder {

    private static Logger log = LogManager.getLogger(JarFinder.class);
    private final URL url;

    JarFinder(URL packageUrl) {
        url = packageUrl;
    }

    @Override
    public List<String> findExcluding(String[] excludes, String endStr) {
        try {
            JarURLConnection conn = (JarURLConnection)url.openConnection();
            String starts = conn.getEntryName();
            JarFile jfile = conn.getJarFile();

            List<String> result = new LinkedList<>();

            Enumeration<JarEntry> e = jfile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                String entryName = entry.getName();

                if (log.isDebugEnabled()) {
                    log.debug("Current entry: {}", entryName);
                }

                if (entryName.startsWith(starts) &&
                        !entry.isDirectory()) {
                    // Now we know that we have a file from the jar.  We need
                    // to parse the file to get the actual filename so that we
                    // can exclude the appropriate files.
                    if (entryName.endsWith(endStr)) {
                        if (excludes != null) {
                            boolean exclude = false;
                            for (String excludeIn : excludes) {
                                String excludesEnds = excludeIn + "." + endStr;
                                if (entryName.endsWith(excludesEnds)) {
                                    exclude = true;
                                    break;
                                }
                            }
                            if (!exclude) {
                                result.add(entryName);
                            }
                        }
                        else {
                            result.add(entryName);
                        }
                    }
                }
            }
            return result;
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Couldn't open jar file " + url);
        }
    }
}
