/*
 * Copyright (c) 2023 SUSE LLC
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
package com.suse.manager.webui.controllers.image;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.manager.satellite.SystemCommandThreadedExecutor;

import com.suse.manager.webui.controllers.image.beans.ListImage;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkopeoCommandManager {

    private static Logger log = LogManager.getLogger(SkopeoCommandManager.class);
    private static final Gson GSON = Json.GSON;

    private SkopeoCommandManager() { }

    /**
     * Get the list of available images
     * @param store to collect iamges from
     * @return list of images available in the stores
     */
    public static List<ListImage> getImageList(ImageStore store) {
        List<String> cmd = new ArrayList<>();
        cmd.add("skopeo");
        cmd.add("list-repos");
        cmd.add("--tls-verify=false");
        cmd.add("--limit=5000");
        cmd.add(store.getUri());

        String[] args = cmd.toArray(new String[cmd.size()]);

        String rawData = executeExtCmd(args);
        Type collectionType = new TypeToken<List<ListImage>>() { }.getType();
        List<ListImage> data = GSON.fromJson(rawData, collectionType);
        return data;
    }


    private static String executeExtCmd(String[] args) {
        SystemCommandThreadedExecutor ce = new SystemCommandThreadedExecutor(log);
        int exitCode = ce.execute(args);

        if (exitCode != 0) {
            String msg = ce.getLastCommandErrorMessage();
            if (msg.isBlank()) {
                msg = ce.getLastCommandOutput();
            }
            if (msg.length() > 2300) {
                msg = "... " + msg.substring(msg.length() - 2300);
            }
            throw new RuntimeException(
                    "Command '" + Arrays.asList(args) +
                            "' exited with error code " + exitCode +
                            (msg.isBlank() ? "" : ": " + msg));
        }
        else {
            return ce.getLastCommandOutput();
        }
    }
}
