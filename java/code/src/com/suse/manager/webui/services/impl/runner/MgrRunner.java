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

package com.suse.manager.webui.services.impl.runner;

import com.suse.salt.netapi.calls.RunnerCall;

import com.google.gson.reflect.TypeToken;

import java.util.Map;
import java.util.Optional;

/**
 * Runner calls useful in SUSE Manager
 */
public class MgrRunner {

    private MgrRunner() { }


    /**
     * {@link RunnerCall} for writing into a text file.
     * The result of the call is a text description with how many lines were written.
     *
     * @param absolutePath the file path
     * @param contents the file text contents
     * @return the runner call
     */
    public static RunnerCall<String> writeTextFile(String absolutePath, String contents) {
        return new RunnerCall<>("salt.cmd", Optional.of(Map.of(
                "fun", "file.write",
                "path", absolutePath,
                "args", contents)),
                new TypeToken<String>() { });
    }

    /**
     * {@link RunnerCall} for setting mode of given file.
     * The result of the call is a text description of the file after the operation.
     *
     * @param absolutePath the absolute path of the file
     * @param modeString the desired mode
     * @return the runner call
     */
    public static RunnerCall<String> setFileMode(String absolutePath, String modeString) {
        return new RunnerCall<>("salt.cmd", Optional.of(Map.of(
                "fun", "file.set_mode",
                "path", absolutePath,
                "mode", modeString)),
                new TypeToken<String>() { }
        );
    }

    /**
     * {@link RunnerCall} for removing a file
     * The result of the call is the boolean, which is true, if path was found and the file deletion succeeded.
     *
     * @param absolutePath the absolute path of the file
     * @return the runner call
     */
    public static RunnerCall<Boolean> removeFile(String absolutePath) {
        return new RunnerCall<>("salt.cmd", Optional.of(Map.of(
                "fun", "file.remove",
                "path", absolutePath)),
                new TypeToken<Boolean>() { });
    }

    /**
     * {@link RunnerCall} for copying a file
     * The result of the call is the boolean, which is true, if the path was successfully copied.
     *
     * @param src the source path of the file
     * @param dst the destination path of the file
     * @param recurse recurse into subdirectories
     * @param removeExisting remove all files in the target directory, and then copy files from the source
     * @return the runner call
     */
    public static RunnerCall<Boolean> copyFile(String src, String dst, boolean recurse, boolean removeExisting) {
        return new RunnerCall<>("salt.cmd", Optional.of(Map.of(
                "fun", "file.copy",
                "src", src,
                "dst", dst,
                "recurse", recurse,
                "remove_existing", removeExisting)),
                new TypeToken<Boolean>() { });
    }

    /**
     * {@link RunnerCall} for creating a directory with the given path.
     * The result of the call is a boolean, which is true, if the path was successfully created.
     *
     * @param absolutePath the absolute path of the directory
     * @param modeString the desired mode
     * @return the runner call
     */
    public static RunnerCall<Boolean> mkDir(String absolutePath, String modeString) {
        return new RunnerCall<>("salt.cmd", Optional.of(Map.of(
                "fun", "file.mkdir",
                "dir_path", absolutePath,
                "mode", modeString)),
                new TypeToken<Boolean>() { });
    }
}

