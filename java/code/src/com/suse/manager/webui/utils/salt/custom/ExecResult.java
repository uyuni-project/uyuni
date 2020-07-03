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

import com.google.gson.annotations.SerializedName;

public class ExecResult {

    @SerializedName("retcode")
    private int returnCode;
    private String stdout;
    private String stderr;

    private Boolean success;

    /**
     * @return command return code
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * @return command stdout
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * @return command stderr
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * @return success to get
     */
    public Boolean getSuccess() {
        return success;
    }
}
