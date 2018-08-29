/**
 * Copyright (c) 2018 SUSE LLC
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Runner calls for Kiwi image build
 */
public class MgrKiwiImageRunner {

    private MgrKiwiImageRunner() { }

    /**
     * Upload built Kiwi image to SUSE Manager
     *
     * @param minionIPAddress the minion IP address
     * @param filepath      the filepath
     * @param imageStoreDir the image store location
     * @return the execution result
     */
    public static RunnerCall<MgrUtilRunner.ExecResult> collectImage(String minionIPAddress, String filepath,
            String imageStoreDir) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("minion", minionIPAddress);
        args.put("filepath", filepath);
        args.put("image_store_dir", imageStoreDir);

        return new RunnerCall<>("kiwi-image-collect.kiwi_collect_image",
                Optional.of(args), new TypeToken<MgrUtilRunner.ExecResult>() { });
    }
}
