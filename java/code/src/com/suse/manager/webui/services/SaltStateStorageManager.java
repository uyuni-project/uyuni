package com.suse.manager.webui.services;

import com.suse.manager.webui.utils.RepoFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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

    public String getBaseDirPath() {
        return RepoFileUtils.GENERATED_SLS_ROOT;
    }

    public synchronized void storeState(long orgId, String name, String oldName, String content) throws IOException {
        // TODO synchronize at file level not on the class instance
        // TODO sanitize name
        Path orgPath = Paths.get(getBaseDirPath(), "manager_org_" + orgId);
        File orgDir = orgPath.toFile();
        if (!orgDir.exists()) {
            orgDir.mkdir();
        }
        File stateFile = new File(orgDir, ext(name));
        assertStateInOrgDir(orgDir, stateFile);

        if (StringUtils.isNotBlank(oldName)) {
            Files.move(orgPath.resolve(ext(oldName)), stateFile.toPath());
        }

        // TODO clarify encoding
        FileUtils.writeStringToFile(stateFile, content, "US-ASCII");
    }

    private void assertStateInOrgDir(File orgDir, File stateFile) throws IOException {
        if (!stateFile.getCanonicalFile().getParentFile().equals(orgDir.getCanonicalFile())) {
            throw new IllegalArgumentException("Trying to write state into another directory");
        }
    }

    private String ext(String name) {
        return StringUtils.endsWith(name, ".sls") ? name : name + ".sls";
    }

    public void deleteState(long orgId, String name) throws IOException {
        File orgDir = new File(getBaseDirPath(), "manager_org_" + orgId);
        File stateFile = new File(orgDir, ext(name));
        assertStateInOrgDir(orgDir, stateFile);
        Files.delete(stateFile.toPath());
    }

    public Optional<String> getContent(long orgId, String name) throws IOException {
        Path slsPath = Paths.get(getBaseDirPath(), "manager_org_" + orgId, ext(name));
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

    public boolean exists(long orgId, String name) {
        Path slsPath = Paths.get(getBaseDirPath(), "manager_org_" + orgId, ext(name));
        return slsPath.toFile().exists();
    }

}
