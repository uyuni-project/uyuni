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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SaltFileUtils.stripExtension;
import static com.suse.manager.webui.utils.SaltFileUtils.defaultExtension;

/**
 * Manages the custom Salt states files on disk.
 */
public enum SaltCustomStateStorageManager {

    // Singleton instance of this class
    INSTANCE;

    private String baseDirPath;

    private String encoding;

    /**
     * No arg constructor. Will initialize {@link #baseDirPath} with
     * '/srv/susemanager/salt' and {@link #encoding} with US-ASCII.
     */
    SaltCustomStateStorageManager() {
        this.baseDirPath = SaltConstants.SUMA_STATE_FILES_ROOT_PATH;
        this.encoding = SaltConstants.SLS_FILE_ENCODING;
    }

    /**
     * Get the base directory where .sls files are stored.
     * Does not include the organization directory.
     * @return the path of the base directory
     */
    public String getBaseDirPath() {
        return baseDirPath;
    }

    /**
     * Set the base directory where .sls files are stored.
     * Does not include the organization directory.
     * @param baseDirPathIn the path of the base directory
     */
    public void setBaseDirPath(String baseDirPathIn) {
        this.baseDirPath = baseDirPathIn;
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

        boolean exists = exists(orgId, name);
        if (StringUtils.isNotBlank(oldName)) {
            oldName = stripExtension(oldName);
            if (!oldName.equals(name) && exists) {
                throw new SaltStateExistsException("Cannot rename '" + oldName +
                        "' to '" + name + "'. A custom state '" + name +
                        "' already exists");
            }
        }
        else if (exists) {
            throw new SaltStateExistsException("Cannot create. A custom state '" +
                    name + "' already exists");
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

        String stateName = stripExtension(name);

        if (isRename(oldName, name)) {
            Files.move(orgPath.resolve(defaultExtension(oldName)), stateFile.toPath());

            final String oldNameToGet = oldName;
            Optional<CustomState> customState = StateFactory.
                    getCustomStateByName(orgId, oldNameToGet);
            CustomState state = customState.orElseThrow(() ->
                    new IllegalArgumentException("CustomState name=" + oldNameToGet +
                            " orgId=" + orgId + " not found in the database"));
            state.setStateName(stateName);
            StateFactory.save(state);
        }
        else if (!exists) {
            // create a new custom state in the db
            CustomState customState = new CustomState();
            customState.setOrg(OrgFactory.lookupById(orgId));
            customState.setStateName(stateName);
            StateFactory.save(customState);
        }
        // else no db changes, just update the state content

        FileUtils.writeStringToFile(stateFile, content, encoding);

    }

    /**
     * @param oldName the old state name
     * @param newName the new state name
     * @return true if the oldName is not blank and they're not equal
     */
    public boolean isRename(String oldName, String newName) {
        return StringUtils.isNotBlank(oldName) && !oldName.equals(newName);
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
        StateFactory.removeCustomState(orgId, name);
        File orgDir = new File(getBaseDirPath(), getOrgNamespace(orgId));
        File stateFile = new File(orgDir, defaultExtension(name));
        assertStateInOrgDir(orgDir, stateFile);
        Files.deleteIfExists(stateFile.toPath());
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
        return Optional.of(FileUtils.readFileToString(slsFile, encoding));

    }

    /**
     * List all files belonging to the given organization.
     * @param orgId the organization id
     * @return a list containing all .sls files names (without .sls extension)
     */
    public List<String> listByOrg(long orgId) {
        List<CustomState> orgStates = StateFactory.getCustomStatesByOrg(orgId);
        return orgStates.stream().map(CustomState::getStateName)
                .collect(Collectors.toList());
    }

    /**
     * Check if a .sls file with the give name exists.
     * @param orgId the organization id
     * @param name the sls file name
     * @return true if the file exists, false otherwise
     */
    public boolean exists(long orgId, String name) {
        return StateFactory.getCustomStateByName(orgId, name).isPresent();
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
