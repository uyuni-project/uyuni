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
package com.suse.manager.utils;

public enum DiskCheckSeverity {
    /**
     * Due to an error it was not possible to define a disk check severity level
     */
    UNDEFINED,

    /**
     * All the required space is available, no problems detected.
     */
    OK,

    /**
     * The configuration is not correct. It must be reviewed in order to obtain a sensible result.
     */
    MISCONFIGURATION,

    /**
     * The services are running out of disk space.
     */
    ALERT,

    /**
     * The disk space is almost exhausted. An immediate action is needed in order to keep the system running.
     */
    CRITICAL;

    /**
     * Returns the enum constant corresponding to the exit value of the bash script.
     * @param exitValue the exit value reported by the bash script.
     * @return the enum constant equivalent to the specified exit value.
     * @throws IllegalArgumentException if the exit value is invalid.
     */
    public static DiskCheckSeverity valueOf(int exitValue) {
        switch (exitValue) {
            case 0:
                return OK;
            case 1:
                return MISCONFIGURATION;
            case 2:
                return ALERT;
            case 3:
                return CRITICAL;
            default:
                throw new IllegalArgumentException("Exit value " + exitValue + " is not valid");
        }

    }
}
