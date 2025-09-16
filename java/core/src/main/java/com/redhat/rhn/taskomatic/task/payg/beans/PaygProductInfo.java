/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.payg.beans;

public class PaygProductInfo {
    private String name;
    private String version;
    private String arch;

    /**
     * constructor with all fields
     * @param nameIn
     * @param versionIn
     * @param archIn
     */
    public PaygProductInfo(String nameIn, String versionIn, String archIn) {
        this.name = nameIn;
        this.version = versionIn;
        this.arch = archIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String archIn) {
        this.arch = archIn;
    }

    @Override
    public String toString() {
        return "PaygProductInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", arch='" + arch + '\'' +
                '}';
    }
}
