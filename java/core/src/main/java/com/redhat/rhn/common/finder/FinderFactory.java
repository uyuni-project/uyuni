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

package com.redhat.rhn.common.finder;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A factory that returns the correct type of finder.
 *
 */
public class FinderFactory {

    private static final Logger LOGGER = LogManager.getLogger(FinderFactory.class);

    private FinderFactory() {
    }

    /**
     * Return the correct finder for finding classes in the given package.
     * @param packageName Name of package to be searched.
     * @return Finder to use for given package name.
     */
    public static Finder getFinder(String packageName) {
        // Extract all the possible URLs for a package
        List<URL> possibleUrls = getAllUrlsForPackage(packageName);
        if (possibleUrls.isEmpty()) {
            throw new IllegalArgumentException("Not a well-formed jar file");
        }

        if (possibleUrls.size() == 1) {
            return getFinderForPackage(packageName, possibleUrls.get(0));
        }

        return possibleUrls.stream()
                           .map(packageUrl -> getFinderForPackage(packageName, packageUrl))
                           .collect(Collectors.collectingAndThen(Collectors.toList(), CompositeFinder::new));
    }

    private static List<URL> getAllUrlsForPackage(String packageName) {
        try {
            ClassLoader classLoader = FinderFactory.class.getClassLoader();
            Enumeration<URL> systemResources = classLoader.getResources(packageName.replace('.', '/'));
            if (systemResources == null || !systemResources.hasMoreElements()) {
                LOGGER.warn("No valid URLs found for package {}", packageName);
                return Collections.emptyList();
            }

            return Collections.list(systemResources);
        }
        catch (SecurityException ex) {
            throw new IllegalStateException("Unable to access the class loader for searching " + packageName, ex);
        }
        catch (IOException ex) {
            LOGGER.warn("Unable to find URLs for package {}", packageName, ex);
            return Collections.emptyList();
        }
    }

    private static Finder getFinderForPackage(String packageName, URL packageUrl) {
        File directory = new File(packageUrl.getFile());

        if (!directory.isFile() && !directory.isDirectory()) {
            // This is a jar file that we are dealing with.
            return new JarFinder(packageUrl);
        }

        return new FileFinder(directory, getAbsolutePath(packageName));
    }

    private static String getAbsolutePath(String packageName) {
        return StringUtils.prependIfMissing(packageName, "/").replace('.', '/');
    }
}
