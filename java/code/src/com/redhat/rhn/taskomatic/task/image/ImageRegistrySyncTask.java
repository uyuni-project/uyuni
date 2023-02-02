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

package com.redhat.rhn.taskomatic.task.image;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageSyncFactory;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;

import com.suse.manager.utils.skopeo.SkopeoCommandManager;
import com.suse.manager.webui.utils.YamlHelper;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.yaml.snakeyaml.TypeDescription;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

public class ImageRegistrySyncTask extends RhnJavaJob {

    private static final String KEY_ID = "project_id";
    private final ImageSyncFactory syncFactory;
    private static final String YAML_SYNC_CACHE = "/var/cache/rhn/imgsync";
    private TypeDescription exportDesc;

    /**
     * default constructor
     */
    public ImageRegistrySyncTask() {
        syncFactory = new ImageSyncFactory();
        exportDesc = new TypeDescription(SkopeoImageSync.class);
        exportDesc.substituteProperty("images-by-tag-regex", SkopeoImageSync.class, "getImagesRegex", "setImagesRegex");
        exportDesc.substituteProperty("tls-verify", SkopeoImageSync.class, "isTlsVerify", "setTlsVerify");
        exportDesc.setExcludes("imagesRegex", "tlsVerify");

    }

    @Override
    public String getConfigNamespace() {
        return "imageRegistrySync";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.debug("Running ImageRegistrySyncTask");
        if (jobExecutionContext != null && jobExecutionContext.getJobDetail().getJobDataMap().containsKey(KEY_ID)) {
            Optional<ImageSyncProject> paygData = syncFactory.lookupProjectById(
                    Long.parseLong((String) jobExecutionContext.getJobDetail().getJobDataMap().get(KEY_ID)));
            paygData.ifPresent(this::syncProject);
        }
        else {
            syncFactory.listAll()
                    .forEach(this::syncProject);
        }
    }

    private void syncProject(ImageSyncProject project) {
        if (project.getSyncItems().size() == 0) {
            return;
        }
        String ymlFilePath = generateYmlConfig(project);
        log.info(String.format("yml file to generated for project %s located at: %s", project.getId(), ymlFilePath));

        SkopeoCommandManager.runSyncFromYaml(log, project, ymlFilePath);
    }

    private String generateYmlConfig(ImageSyncProject project) {
        // source store fqdn, images to sync
        Map<String, SkopeoImageSync> storesSync = new ConcurrentHashMap();
        // store
        ImageStore store = project.getSrcStore();
        SkopeoImageSync syncStore = storesSync.getOrDefault(store.getUri(), new SkopeoImageSync());
        if (store.getCreds() != null) {
            syncStore.setCredentials(
                    new SkopeoCredential(
                            store.getCreds().getUsername(),
                            store.getCreds().getPassword()));
        }
        project.getSyncItems().stream().forEach(syncItem -> {
            // image on store
            if (!StringUtils.isEmpty(syncItem.getSrcTagsRegexp())) {
                syncStore.getImagesRegex().put(syncItem.getSrcRepository(), syncItem.getSrcTagsRegexp());
            }

            if (syncItem.getSrcTags() != null && !syncItem.getSrcTags().isEmpty()) {
                syncStore.getImages().put(syncItem.getSrcRepository(), syncItem.getSrcTags());
            }
            else if (StringUtils.isEmpty(syncItem.getSrcTagsRegexp())) {
                syncStore.getImages().put(syncItem.getSrcRepository(), new ArrayList<>());
            }

            storesSync.put(store.getUri(), syncStore);
        });

        String filePath = String.format("%s/sync_project_%s_%s.yml",
                YAML_SYNC_CACHE, project.getId(), project.getName());
        FileUtils.writeStringToFile(YamlHelper.INSTANCE.dump(exportDesc, storesSync), filePath);
        return filePath;
    }
}
