/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.webui.utils.salt.custom;

/**
 * The Kiwi image build bundle info.
 */
public class KiwiBundleInfo {

    private String basename;
    private String filename;
    private String filepath;
    private String hash;
    private String id;
    private String suffix;

    /**
     * @return the basename
     */
    public String getBasename() {
        return basename;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the filepath
     */
    public String getFilepath() {
        return filepath;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @return the build id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the suffix
     */
    public String getSuffix() {
        return suffix;
    }
}
