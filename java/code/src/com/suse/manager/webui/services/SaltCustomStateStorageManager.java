/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.state.CustomState;
import com.redhat.rhn.domain.state.StateFactory;
import com.suse.manager.webui.utils.RepoFileUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SaltFileUtils.hasExtension;
import static com.suse.manager.webui.utils.SaltFileUtils.stripExtension;
import static com.suse.manager.webui.utils.SaltFileUtils.defaultExtension;

/**
 * Manages the Salt .sls files on disk.
 */
public class SaltCustomStateStorageManager {

    private static final Logger LOG = Logger.getLogger(SaltCustomStateStorageManager.class);

    /**
     * Get the base directory where .sls files are stored.
     * Does not include the organization directory.
     * @return the path of the base directory
     */
    public String getBaseDirPath() {
        return RepoFileUtils.GENERATED_SLS_ROOT;
    }

    /**
     * Store the .sls file on disk with the given name and content.
     * @param orgId the organization id to which this SLS files belongs to
     * @param name the name of the file
     * @param content the content of the file
     * @param oldName the previous name of the file, if any. This is used to
     *                check overwriting in case of name change.
     * @param oldChecksum the checksum of the file at the time it was read
     * @throws IOException in case of an IO error
     * @throws SaltStateExistsException in case the state with the same name
     * already exists
     * @throws StaleSaltStateException if the oldChecksum and current
     * checksum are not the same
     */
    public synchronized void storeState(long orgId, String name, String content,
                                        String oldName, String oldChecksum)
            throws IOException {
        // TODO synchronize at file level not on the class instance

        if (StringUtils.isNotBlank(oldName)) {
            oldName = stripExtension(oldName);
            if (!oldName.equals(name) && exists(orgId, name)) {
                throw new SaltStateExistsException();
            }
        }
        else if (exists(orgId, name)) {
            throw new SaltStateExistsException();
        }

        Path orgPath = Paths.get(getBaseDirPath(), getOrgNamespace(orgId));
        File orgDir = orgPath.toFile();
        if (!orgDir.exists()) {
            orgDir.mkdir();
        }
        File stateFile = new File(orgDir, defaultExtension(name));
        assertStateInOrgDir(orgDir, stateFile);

        if (stateFile.exists()) {
            String currentChecksum;
            try (InputStream is = new FileInputStream(stateFile)) {
                currentChecksum = DigestUtils.md5Hex(is);
            }
            if (oldChecksum != null && !currentChecksum.equals(oldChecksum)) {
                throw new StaleSaltStateException();
            }
        }

        boolean move = false;
        if (StringUtils.isNotBlank(oldName)) {
            Files.move(orgPath.resolve(defaultExtension(oldName)), stateFile.toPath());
            move = true;
        }

        // TODO clarify encoding
        FileUtils.writeStringToFile(stateFile, content, "US-ASCII");

        String stateName = stripExtension(name);
        if (!move) {
            CustomState customState = new CustomState();
            customState.setOrg(OrgFactory.lookupById(orgId));
            customState.setStateName(stateName);
            StateFactory.save(customState);
        }
        else {
            final String oldNameToGet = oldName;
            Optional<CustomState> customState = StateFactory.
                    getCustomStateByName(oldNameToGet);
            CustomState state = customState.orElseThrow(() ->
                    new IllegalArgumentException("CustomState name=" + oldNameToGet +
                            " not found"));
            state.setStateName(stateName);
            StateFactory.save(state);
        }
    }

    private void assertStateInOrgDir(File orgDir, File stateFile) throws IOException {
        if (!stateFile.getCanonicalFile().getParentFile()
                .equals(orgDir.getCanonicalFile())) {
            throw new IllegalArgumentException("Trying to write state " +
                    "into another directory");
        }
    }

    /**
     * Delete the .sls file with the give name.
     * @param orgId the organization id
     * @param name the sls file name
     * @throws IOException in case of an IO error
     */
    public void deleteState(long orgId, String name) throws IOException {
        File orgDir = new File(getBaseDirPath(), getOrgNamespace(orgId));
        File stateFile = new File(orgDir, defaultExtension(name));
        assertStateInOrgDir(orgDir, stateFile);
        Files.delete(stateFile.toPath());
    }

    /**
     * Get the content of the .sls file with the given name.
     * @param orgId the organization id
     * @param name the sls file name
     * @return the content of the file as a string if the file exists
     * @throws IOException in case of an IO error
     */
    public Optional<String> getContent(long orgId, String name)
            throws IOException {
        Path slsPath = Paths.get(getBaseDirPath(), getOrgNamespace(orgId),
                defaultExtension(name));
        File slsFile = slsPath.toFile();
        if (!slsFile.exists()) {
            return Optional.empty();
        }
        return Optional.of(FileUtils.readFileToString(slsFile, "US-ASCII"));

    }

    /**
     * List all files belonging to the given organization.
     * @param orgId the organization id
     * @return a list containing all .sls files names (without .sls extension)
     */
    public List<String> listByOrg(long orgId) {
        Path orgPath = Paths.get(getBaseDirPath(), getOrgNamespace(orgId));
        File orgDir = orgPath.toFile();
        if (!orgDir.exists()) {
            return Collections.emptyList();
        }
        return Arrays.asList(orgDir.list((dir, name) -> hasExtension(name)))
                .stream().map(name -> stripExtension(name))
                .collect(Collectors.toList());
    }

    /**
     * Check if a .sls file with the give name exists.
     * @param orgId the organization id
     * @param name the sls file name
     * @return true if the file exists, false otherwise
     */
    public boolean exists(long orgId, String name) {
        Path slsPath = Paths.get(getBaseDirPath(), getOrgNamespace(orgId),
                defaultExtension(name));
        return slsPath.toFile().exists();
    }

    /**
     * Get the Salt namespace of the organization.
     * @param orgId the organization id
     * @return the Salt namespace
     */
    public String getOrgNamespace(long orgId) {
        return "manager_org_" + orgId;
    }

}
