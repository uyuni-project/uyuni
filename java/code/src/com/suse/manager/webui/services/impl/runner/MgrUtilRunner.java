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
package com.suse.manager.webui.services.impl.runner;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.RunnerCall;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Runner calls specific to SUSE Manager.
 */
public class MgrUtilRunner {

    private MgrUtilRunner() { }


    /**
     * Moves a directory from
     * {@code /var/cache/salt/master/minions/[minion]/files/[dirToMove]}
     * to {@code scapStorePath}
     *
     * @param minion minion id
     * @param dirToMove path of directory to move. Relative to Salt's cache dir.
     * @param basePath absolute base path where to action dir is located
     * @param actionPath relative path of the action dir
     * @return a {@link RunnerCall} to pass to the SaltClient
     */
    public static RunnerCall<Map<Boolean, String>> moveMinionUploadedFiles(
            String minion, String dirToMove, String basePath, String actionPath) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("minion", minion);
        args.put("dirtomove", dirToMove);
        args.put("basepath", basePath);
        args.put("actionpath", actionPath);
        RunnerCall<Map<Boolean, String>> call =
                new RunnerCall<>("mgrutil.move_minion_uploaded_files", Optional.of(args),
                        new TypeToken<Map<Boolean, String>>() { });
        return call;
    }

}
