/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.supportconfig;


import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * Result of the supportdata/init.sls state.
 */
public class SupportDataStateResult {

    /**
     * {@code supportdata.get} result.
     */
    public static class SupportDataResult {

        @SerializedName("error")
        private String error;

        @SerializedName("success")
        private boolean success;

        @SerializedName("supportdata_dir")
        private String supportDataDir;

        @SerializedName("returncode")
        private int returnCode;

        /**
         * @return command stderr
         */
        public String getError() {
            return error;
        }

        /**
         * @return command success flag
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * @return dir uploaded by the openscap module
         */
        public String getSupportDataDir() {
            return supportDataDir;
        }

        /**
         * @return command return code
         */
        public int getReturnCode() {
            return returnCode;
        }
    }

    @SerializedName("mgrcompat_|-gather-supportdata_|-supportdata.get_|-module_run")
    private Optional<StateApplyResult<Ret<SupportDataResult>>> supportData = Optional.empty();

    public Optional<StateApplyResult<Ret<SupportDataResult>>> getSupportData() {
        return supportData;
    }
}


