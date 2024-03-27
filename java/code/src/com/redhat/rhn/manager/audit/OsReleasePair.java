/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.manager.audit;

public class OsReleasePair {
    private final String os;
    private final String osRelease;

    /**
     * Default Constructor
     * @param osIn the name of the OS
     * @param osReleaseIn the OS release version
     * */
    public OsReleasePair(String osIn, String osReleaseIn) {
        os = osIn;
        osRelease = osReleaseIn;
    }

    public String getOs() {
        return os;
    }

    public String getOsRelease() {
        return osRelease;
    }


}
