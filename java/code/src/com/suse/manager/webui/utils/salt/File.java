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
package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
/**
 * Basic operations on files and directories on minions
 */
public class File {
    /**
     *
     */
    private File() { }
    /**
     * File module result object
     */
    public static class Result {
        private boolean result;
        private String comment;

        /**
         * @return True/False based on success/failure
         */
        public boolean getResult() {
            return result;
        }

        /**
         * @return Additional Information from Salt
         */
        public String getComment() {
            return comment;
        }
    }

    /**
     * Copy the file from Master to the minions
     * Note: root user/group will be used as owner and mode will be to 755
     * @param name Location to place the file
     * @param source File reference on the master
     * @return LocalCall<Result>
     */
    public static LocalCall<Result> manageFile(String name, String source) {
        String sfn = "";
        String ret = "";
        Map sourceSum = Collections.EMPTY_MAP;
        String user = "root";
        String group = "root";
        String mode = "755";
        String saltenv = "base";
        String backup = "";
        return manageFile(name, sfn, ret, source, sourceSum, user, group, mode, saltenv,
                         backup);
    }

    /**
     *
     * @param name Location to place the file
     * @param source File reference on the master
     * @param mode   mode
     * @param user   user owner
     * @param group  group owner
     * @return LocalCall<Result>
     */
    public static LocalCall<Result> manageFile(String name, String source, String mode,
                                                               String user, String group) {
        String sfn = "";
        String ret = "";
        Map sourceSum = Collections.EMPTY_MAP;
        String saltenv = "base";
        String backup = "";

        return manageFile(name, sfn, ret, source, sourceSum, user, group, mode,
                         saltenv, backup);
    }

    /**
     *
     * @param name location to place the file
     * @param sfn  Location of cached file on the minion
     * @param ret  Initial state return data structure.
     * @param source File reference on the master
     * @param sourceSum sum hash for source
     * @param user   user owner
     * @param group  group owner
     * @param mode   mode
     * @param saltenv Salt environment used to resolve source files
     * @param backup  backup_mode
     * @return LocalCall<Result>
     */
    public static LocalCall<Result> manageFile(String name, String sfn,  String ret,
                                               String source, Map sourceSum, String user,
                                               String group, String mode, String saltenv,
                                               String backup) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("name", name);
        args.put("sfn", sfn);
        args.put("ret", ret);
        args.put("source", source);
        args.put("source_sum", sourceSum);
        args.put("user", user);
        args.put("group", group);
        args.put("mode", mode);
        args.put("saltenv", saltenv);
        args.put("backup", backup);
        args.put("makedirs", true);
        return new LocalCall("file.manage_file", Optional.empty(), Optional.of(args),
                            new TypeToken<Result>() { });
    }
}
