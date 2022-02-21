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
package com.redhat.rhn.domain.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Helper class to hold the number of files and directories
 * Mainly used by ConfigManagger and OverviewAction page.
 * ConfigFileCount
 */
public class ConfigFileCount {
    private long files;
    private long slsFiles;
    private long directories;
    private long symlinks;

    /**
     * Creates a new instance of this class
     * @param files the files to set
     * @param slsFiles the files to set
     * @param directories dirs to set
     * @param symlinks symlinks to set
     * @return a new ConfigFileCount object using the params passed in
     */
    public static ConfigFileCount create(long files, long slsFiles, long directories,
                                         long symlinks) {
        ConfigFileCount  cf = new  ConfigFileCount();
        cf.setFiles(files);
        cf.setSlsFiles(slsFiles);
        cf.setDirectories(directories);
        cf.setSymlinks(symlinks);
        return cf;
    }
    /**
     *
     * @return the number of dirs
     */
    public long getDirectories() {
        return directories;
    }
    /**
     *
     * @param dirs the number dirs to set
     */
    private void setDirectories(long dirs) {
        this.directories = dirs;
    }
    /**
     *
     * @return the number of files
     */
    public long getFiles() {
        return files;
    }
    /**
     *
     * @param numFiles the number of files to set
     */
    private void setFiles(long numFiles) {
        this.files = numFiles;
    }
    /**
     *
     * @return the number of sls files
     */
    public long getSlsFiles() {
        return slsFiles;
    }
    /**
     *
     * @param numSlsFiles the number of files to set
     */
    private void setSlsFiles(long numSlsFiles) {
        this.slsFiles = numSlsFiles;
    }
    /**
     *
     * @return the number of symlinks
     */
    public long getSymlinks() {
        return symlinks;
    }
    /**
     *
     * @param numLinks the number of symlinks to set
     */
    private void setSymlinks(long numLinks) {
        this.symlinks = numLinks;
    }

    /**
     *
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ConfigFileCount)) {
            return false;
        }
        ConfigFileCount that = (ConfigFileCount) obj;
        return new EqualsBuilder().
                    append(files, that.files).
                    append(directories, that.directories).
                    append(symlinks, that.symlinks).
                    isEquals();

    }

    /**
     *
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder().
                        append(files).
                        append(directories).
                        append(symlinks).
                        toHashCode();
    }
}
