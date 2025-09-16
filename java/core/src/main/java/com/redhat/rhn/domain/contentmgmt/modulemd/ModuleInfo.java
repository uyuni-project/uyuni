/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt.modulemd;

/**
 * Detailed information on a unique AppStream module entry
 */
public class ModuleInfo {

    private String name;
    private String stream;
    private String version;
    private String context;
    private String arch;

    /**
     * All args constructor
     *
     * @param nameIn the module name
     * @param streamIn the stream name
     * @param versionIn the stream version
     * @param contextIn the stream context hash value
     * @param archIn the architecture
     */
    public ModuleInfo(String nameIn, String streamIn, String versionIn, String contextIn, String archIn) {
        name = nameIn;
        stream = streamIn;
        version = versionIn;
        context = contextIn;
        arch = archIn;
    }

    public String getName() {
        return name;
    }

    public String getStream() {
        return stream;
    }

    public String getVersion() {
        return version;
    }

    public String getContext() {
        return context;
    }

    public String getArch() {
        return arch;
    }
}
