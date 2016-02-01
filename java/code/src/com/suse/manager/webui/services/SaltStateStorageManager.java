package com.suse.manager.webui.services;

import com.suse.manager.webui.utils.RepoFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by matei on 1/29/16.
 */
public class SaltStateStorageManager {

    private static final Logger LOG = Logger.getLogger(SaltStateStorageManager.class);

    private String getBaseDirPath() {
        return RepoFileUtils.GENERATED_SLS_ROOT;
    }

    // TODO should it be synchronized ?
    public synchronized void storeState(long orgId, String name, String content) throws IOException {
        // TODO sanitize name
        Path orgPath = Paths.get(getBaseDirPath(), "manager_org_" + orgId);
        File orgDir = orgPath.toFile();
        if (!orgDir.exists()) {
            orgDir.mkdir();
        }
        File stateFile = new File(orgDir, name + ".sls");
        // TODO clarify encoding
        FileUtils.writeStringToFile(stateFile, content, "US-ASCII");
    }

    public void deleteState(long orgId, String name) throws IOException {
        Path slsPath = Paths.get(getBaseDirPath(), "manager_org_" + orgId, name);
        Files.delete(slsPath);
    }

    public Optional<String> get(long orgId, String name) throws IOException {
        Path slsPath = Paths.get(getBaseDirPath(), "manager_org_" + orgId, name);
        File slsFile = slsPath.toFile();
        if (!slsFile.exists()) {
            return Optional.empty();
        }
        return Optional.of(FileUtils.readFileToString(slsFile, "US-ASCII"));

    }

    public List<String> listByOrg(long orgId) {
        Path orgPath = Paths.get(getBaseDirPath(), "manager_org_" + orgId);
        File orgDir = orgPath.toFile();
        if (!orgDir.exists()) {
            return Collections.emptyList();
        }
        return Arrays.asList(orgDir.list((dir, name) -> name.endsWith(".sls")));

    }

}
