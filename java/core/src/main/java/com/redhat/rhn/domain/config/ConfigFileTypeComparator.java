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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.config;

import java.util.Comparator;

/**
 * Comparator used in ConfigChannel.getConfigFiles
 * ConfigFileTypeComparator
 */
public class ConfigFileTypeComparator implements Comparator<ConfigFile> {
    /**
     * Compares 2 ConfigFiles with respect to their latest revision's file type
     * Sorts file ahead of directories
     * @param arg0 config file 1
     * @param arg1 config file 2
     * @return 0 if the 2 files are equal, other wise  1/-1
     *  based on the file type of the latest revision
     */
    @Override
    public int compare(ConfigFile arg0, ConfigFile arg1) {
        if (arg0 == null && arg1 == null) {
            return 0;
        }
        if (arg0 == null) {
            return -1;
        }
        if (arg1 == null) {
            return 1;
        }
        if (arg0.equals(arg1)) {
            return 0;
        }

        ConfigFileType fileTypeOne =  arg0.getLatestConfigRevision().
                                                    getConfigFileType();
        ConfigFileType fileTypeOther =  arg1.getLatestConfigRevision().
                                                            getConfigFileType();

        if (compareTypes(fileTypeOne, fileTypeOther) != 0) {
            return compareTypes(fileTypeOne, fileTypeOther);
        }
        String pathOne = arg0.getConfigFileName().getPath();
        String pathOther = arg1.getConfigFileName().getPath();

        if (pathOne.compareTo(pathOther) != 0) {
            return pathOne.compareTo(pathOther);
        }
        // here we know that the file types are equal
        // And the paths are the same, but the Config Files are not equal
        // So we certainly do not want to return 0.. We also don't care abt
        // the ordering here, so just pick an arbitrary value != 0

        return 1;
    }

    /**
     * We want files to come ahead of directories
     * When we sort. So have a special compare method
     * @param one file type 1
     * @param other file type to compare with
     * @return 0, -1, 1 obeys the regular compare contract
     */
    private int compareTypes(ConfigFileType one, ConfigFileType other) {
        if (one.equals(other)) {
            return 0;
        }
        if (ConfigFileType.file().equals(one)) {
            return -1;
        }
        return 1;
    }
}
