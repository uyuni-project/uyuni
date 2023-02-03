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
package com.suse.manager.utils.skopeo;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.manager.satellite.SystemCommandThreadedExecutor;

import com.suse.manager.utils.skopeo.beans.ImageTags;
import com.suse.manager.utils.skopeo.beans.RepositoryImageList;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkopeoCommandManager {

    private static Logger logger = LogManager.getLogger(SkopeoCommandManager.class);
    private static final Gson GSON = Json.GSON;

    private SkopeoCommandManager() { }

    /**
     * Get the list of available images
     * @param store to collect iamges from
     * @param filter filter the results base on the parameter
     * @return list of images available in the stores
     */
    public static List<RepositoryImageList> getStoreImages(ImageStore store, String filter) {
        List<String> cmd = new ArrayList<>();
        cmd.add("skopeo");
        cmd.add("list-repos");
        cmd.add("--limit=5000");
        if (!StringUtils.isEmpty(filter)) {
            // FIXME this will change, and should be a parameter at the end, after the URL
            cmd.add("--search=" + filter);
        }
        if (store.getCreds() != null) {
            cmd.add(String.format("--username=%s", store.getCreds().getUsername()));
            cmd.add(String.format("--password%s", store.getCreds().getPassword()));
        }
        cmd.add(store.getUri());

        String[] args = cmd.toArray(new String[cmd.size()]);

        String rawData = executeExtCmd(logger, args);
        Type collectionType = new TypeToken<List<RepositoryImageList>>() { }.getType();
        List<RepositoryImageList> data = GSON.fromJson(rawData, collectionType);
        return data;
    }

    /**
     * Get the list of available images
     * @param store to collect iamges from
     * @param image image name to obtain tags from
     * @return list of images available in the stores
     */
    public static ImageTags getImageTags(ImageStore store, String image) {
        List<String> cmd = new ArrayList<>();
        cmd.add("skopeo");
        cmd.add("list-tags");

        if (store.getCreds() != null) {
            cmd.add(String.format("--username=%s", store.getCreds().getUsername()));
            cmd.add(String.format("--password%s", store.getCreds().getPassword()));
        }
        cmd.add(String.format("docker://%s/%s", store.getUri(), image));

        String[] args = cmd.toArray(new String[cmd.size()]);

        String rawData = executeExtCmd(logger, args);
        Type collectionType = new TypeToken<ImageTags>() { }.getType();
        ImageTags data = GSON.fromJson(rawData, ImageTags.class);
        return data;
    }

    /**
     *
     * @param log
     * @param projectIn
     * @param ymlFilePathIn
     */
    public static void runSyncFromYaml(Logger log, ImageSyncProject projectIn, String ymlFilePathIn) {
        List<String> cmd = new ArrayList<>();
        cmd.add("skopeo");
        cmd.add("sync");
        // FIXME needed to sync all archs, and should be solved when we manage archs correctly
        cmd.add("-a");
        cmd.add("--keep-going=true");

        if (projectIn.isScoped()) {
            cmd.add("--scoped=true");
        }
        // is hardcoded with docker but we can have more
        cmd.add("--dest=docker");

        cmd.add("--src=yaml");
        // yaml file to be synced
        cmd.add(ymlFilePathIn);
        if (projectIn.getDestinationImageStore().getCreds() != null) {
            cmd.add(String.format("--dest-username=%s", projectIn.getDestinationImageStore().getCreds().getUsername()));
            cmd.add(String.format("--dest-password%s", projectIn.getDestinationImageStore().getCreds().getPassword()));
        }
        cmd.add(projectIn.getDestinationImageStore().getUri());

        String[] args = cmd.toArray(new String[cmd.size()]);

        log.info(executeExtCmd(log, args));
    }

    private static String executeExtCmd(Logger log, String[] args) {
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
            log.info("Command out: " + ce.getLastCommandOutput());
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
