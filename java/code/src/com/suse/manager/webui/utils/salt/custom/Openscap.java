/**
 * Copyright (c) 2017 SUSE LLC
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
import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Custom Salt module openscap.
 */
public class Openscap {

    /**
     * {@code openscap.xccdf} result.
     */
    public static class OpenscapResult {

        @SerializedName("error")
        private String error;

        @SerializedName("success")
        private boolean success;

        @SerializedName("upload_dir")
        private String uploadDir;

        @SerializedName("returnCode") //TODO
        private int returnCode;

        /**
         * @return Openscap command stderr
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
        public String getUploadDir() {
            return uploadDir;
        }

        /**
         * @return Openscp command return code
         */
        public int getReturnCode() {
            return returnCode;
        }
    }

    private Openscap() { }

    /**
     * Call openscap.xccdf
     * @param parameters to pass to openscap.xccdf
     * @return a {@link LocalCall} to pass to the SaltClient
     */
    public static LocalCall<OpenscapResult> xccdf(String parameters) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("params", parameters);
        return new LocalCall<>("openscap.xccdf", Optional.empty(), Optional.of(args),
                new TypeToken<OpenscapResult>() { });
    }

}
