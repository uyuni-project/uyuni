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

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Class to execute a check of the available disk space with an external bash script.
 */
public class DiskCheckHelper {

    private static final Logger LOG = Logger.getLogger(DiskCheckHelper.class);

    /**
     * Default path of the bash script to check the available space.
     */
    public static final String DISKCHECK_SCRIPT = "/usr/bin/spacewalk-diskcheck";

    /**
     * Invoke the external script and execute the check.
     *
     * @return the {@link DiskCheckSeverity} reported by the script or {@link DiskCheckSeverity#UNDEFINED} if it was
     * not possible to evaluate the available disk space.
     */
    public DiskCheckSeverity executeDiskCheck() {
        try {
            final int result = invokeExternalScript();
            return DiskCheckSeverity.valueOf(result);
        }
        catch (IOException | InterruptedException ex) {
            LOG.warn("Unable to execute disk space check", ex);
            return DiskCheckSeverity.UNDEFINED;
        }
        catch (RuntimeException ex) {
            LOG.warn("Unable to evaluate disk check severity", ex);
            return DiskCheckSeverity.UNDEFINED;
        }
    }

    /**
     * Executes the external script and returns the exit code. This method can be overridden during unit test to mock a
     * specific result.
     * @return the exit value as returned from the execution of the script.
     * @throws IOException          when an I/O error occurs during the execution of the script.
     * @throws InterruptedException when the process is interrupted while waiting for a result from the script.
     */
    protected int invokeExternalScript() throws IOException, InterruptedException {
        final Process process = Runtime.getRuntime().exec(new String[]{DISKCHECK_SCRIPT, "-c"});
        return process.waitFor();
    }
}
