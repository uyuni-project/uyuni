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

import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of Finder that searches the file system
 *
 */
class FileFinder implements Finder {

    private final File startDir;
    private static Logger log = Logger.getLogger(FileFinder.class);
    private String path;

    FileFinder(File directory, String relativeDir) {
        startDir = directory;
        path = relativeDir;
        if (relativeDir.startsWith("/")) {
            path = path.substring(1);
        }
    }

    /** {@inheritDoc} */
    public List<String> find(String endStr) {
        return findExcluding(null, endStr);
    }

    /** {@inheritDoc} */
    public List<String> findExcluding(String[] excludes, String endStr) {
        List<String> results = new LinkedList<>();

        if (!startDir.exists()) {
            // Shouldn't ever happen, because the FinderFactory should only
            // return a FileFinder.
            return null;
        }
        String[] fileList = startDir.list();

        if (log.isDebugEnabled()) {
            log.debug("Starting search " + startDir);
            log.debug("File Array: " + Arrays.asList(fileList));
        }
        for (String sIn : fileList) {
            File current = new File(startDir, sIn);

            if (current.isDirectory()) {
                List<String> subdirList = new FileFinder(current,
                        path + File.separator +
                                sIn).findExcluding(excludes, endStr);
                if (log.isDebugEnabled()) {
                    log.debug("adding: " + subdirList);
                }
                results.addAll(subdirList);
                continue;
            }
            if (sIn.endsWith(endStr)) {
                if (excludes != null) {
                    boolean exclude = false;
                    for (String excludeIn : excludes) {
                        String excludesEnds = excludeIn + "." + endStr;
                        if (sIn.endsWith(excludesEnds)) {
                            exclude = true;
                            break;
                        }
                    }

                    if (!exclude) {
                        results.add(path + File.separator + sIn);
                    }
                }
                else {
                    results.add(path + File.separator + sIn);
                }
            }
        }
        return results;
    }
}
