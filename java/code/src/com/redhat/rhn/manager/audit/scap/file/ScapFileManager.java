/**
 * Copyright (c) 2013 Red Hat, Inc.
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
package com.redhat.rhn.manager.audit.scap.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

/**
 * ScapFileManager - Handling of full SCAP results assigned to a rhnXccdfTestResult
 */
public class ScapFileManager {
    private ScapFileManager() {
    }

//    public static void storeMinionTestResult() {
//
//
//
//        Path folder = Paths.get(getStoragePath(testResult));
//        if (Files.exists(folder)) {
//            try {
//                Files.createDirectories(folder); // TODO permissions ?
//                Files.copy(resultsFile, folder.resolve(resultsFile.getFileName()));
//            } catch (IOException e) {
//                throw new RuntimeException("Could not store SCAP test result", e);
//            }
//        }
//    }

    /**
     * Find SCAP Result files assigned with the given testResult
     * @param testResult XccdfTestResult
     * @return the list of files
     */
    public static List<ScapResultFile> lookupFilesForTestResult(
            XccdfTestResult testResult) {
        List<ScapResultFile> result = new ArrayList<ScapResultFile>();
        File folder = new File(getStoragePath(testResult));

        // Some results may not have any files - let's not blow up
        if (!folder.exists() || !folder.isDirectory()) {
            return result;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    result.add(new ScapResultFile(testResult, file.getName()));
                }
            }
        }
        return result;
    }

    /**
     * Remove any files assigned with the given testResult
     * @param tr XccdfTestResult
     */
    public static void deleteFilesForTestResult(XccdfTestResult tr) {
        File folder = new File(getStoragePath(tr));
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            folder.delete();
        }
    }

    /**
     * Get file path to the storage directory assigned with a given testResult
     * @param tr TestResult
     * @return the path
     */
    public static String getStoragePath(XccdfTestResult tr) {
        return Config.get().getString(ConfigDefaults.MOUNT_POINT) +
            "/" + getStorageRelativePath(tr);
    }

    public static String getStoragePath(Long orgId, Long systemId, Long actionId) {
        return Config.get().getString(ConfigDefaults.MOUNT_POINT) +
            "/" + getActionPath(orgId, systemId, actionId);
    }

    private static String getStorageRelativePath(XccdfTestResult tr) {
        return getActionPath(tr.getServer().getOrg().getId(),
            tr.getServer().getId(), tr.getScapActionDetails().getParentAction().getId());
    }

    private static String getActionPath(Long orgId, Long systemId, Long actionId) {
        // an equivalent of rhnLib.get_action_path()
        return "systems/" + orgId + "/" + systemId + "/actions/" + actionId;
    }

}
