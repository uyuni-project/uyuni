/**
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

package com.suse.manager.webui.utils.salt.custom;

public class MgrClustersResult {

    private String stdout;
    private String stderr;
    private boolean success;
    private int retcode;

    /**
     * @return stdout to get
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * @param stdoutIn to set
     */
    public void setStdout(String stdoutIn) {
        this.stdout = stdoutIn;
    }

    /**
     * @return stderr to get
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * @param stderrIn to set
     */
    public void setStderr(String stderrIn) {
        this.stderr = stderrIn;
    }

    /**
     * @return success to get
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param successIn to set
     */
    public void setSuccess(boolean successIn) {
        this.success = successIn;
    }

    /**
     * @return retcode to get
     */
    public int getRetcode() {
        return retcode;
    }

    /**
     * @param retcodeIn to set
     */
    public void setRetcode(int retcodeIn) {
        this.retcode = retcodeIn;
    }
}
