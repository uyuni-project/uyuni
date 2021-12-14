/*
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Utilities for working with Salt .sls files.
 */
public class SaltFileUtils {

    private SaltFileUtils() { }

    /**
     * Check if a files has the .sls extension
     * @param fileName the file name
     * @return true if the file has a .sls extension
     */
    public static boolean hasExtension(String fileName) {
        return fileName.endsWith(".sls");
    }

    /**
     * Remove the .sls extension from a file
     * @param fileName the file name
     * @return file name without extension
     */
    public static String stripExtension(String fileName) {
        return StringUtils.removeEnd(fileName, ".sls");
    }

    /**
     * Add the .sls extension if not already present
     * @param stateName the filename
     * @return a filename with the .sls extension
     */
    public static String defaultExtension(String stateName) {
        return SaltFileUtils.hasExtension(stateName) ? stateName : stateName + ".sls";
    }
}
