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

package com.suse.manager.webui.services;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigInfo;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.utils.Opt;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.redhat.rhn.domain.config.ConfigFileState.NORMAL;
import static com.suse.manager.webui.utils.SaltFileUtils.defaultExtension;
import static java.util.Collections.emptySortedSet;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Singleton class, renders salt files corresponding to a configuration channel on the disk.
 *
 * todo extract common parts with this and SaltConfigurationStateStorageManager into some
 * helper
 */
public class ConfigChannelSaltManager {

    private String baseDirPath;
    private String encoding;
    private static ConfigChannelSaltManager instance;
    private static final Logger LOG = Logger.getLogger(ConfigChannelSaltManager.class);

    /**
     * Gets the instance.
     *
     * @return instance
     */
    public static synchronized ConfigChannelSaltManager getInstance() {
        if (instance == null) {
            instance = new ConfigChannelSaltManager();
        }
        return instance;
    }

    /**
     * No arg constructor. Will initialize {@link #baseDirPath} with
     * '/srv/susemanager/salt' and {@link #encoding} with US-ASCII.
     */
    private ConfigChannelSaltManager() {
        this.baseDirPath = SaltConstants.SUMA_STATE_FILES_ROOT_PATH;
        this.encoding = SaltConstants.SLS_FILE_ENCODING;
    }

    /**
     * Store the salt file structure for a config channel on the disk.
     *
     * @param channel - the config channel
     * @throws IOException in case of an IO error
     */
    public void generateConfigChannelFiles(ConfigChannel channel) {
        generateConfigChannelFiles(channel, empty());
    }

    /**
     * Store the salt file structure for a config channel on the disk.
     * Removes the old hierarchy in case the label has been changed.
     *
     * @param channel - the config channel
     * @param oldChannelLabel - the old label of the channel
     * @throws IOException in case of an IO error
     */
    public synchronized void generateConfigChannelFiles(ConfigChannel channel,
            Optional<String> oldChannelLabel) {
        try {
            LOG.debug("Generating file structure for configuration channel: " +
                    channel.getLabel() + " (old channel label: " +
                    oldChannelLabel.orElse("<empty>") + ").");
            doGenerateConfigChannelFiles(channel, oldChannelLabel);
        }
        catch (IOException e) {
            LOG.error("Error when handling salt file structure for channel: " +
                    channel + " (old channel label: " +
                    oldChannelLabel.orElse("<empty>") + "). " +
                    "Removing files from disk.", e);
            removeConfigChannelFiles(channel.getOrgId(), channel.getConfigChannelType(),
                    channel.getLabel());
            oldChannelLabel.ifPresent(oldLabel -> removeConfigChannelFiles(
                    channel.getOrgId(), channel.getConfigChannelType(), oldLabel));
        }
    }

    /**
     * Removes the salt file structure for a config channel from the disk.
     *
     * @param channel the channel
     */
    public synchronized void removeConfigChannelFiles(ConfigChannel channel) {
        removeConfigChannelFiles(channel.getOrgId(), channel.getConfigChannelType(),
                channel.getLabel());
    }

    /**
     * Perform actual disk actions to reflect the configuration channel state on the disk.
     *
     * @param channel - the config channel
     * @param oldChannelLabel - the old label of the channel
     * @throws IOException in case of an IO error
     */
    private void doGenerateConfigChannelFiles(ConfigChannel channel,
            Optional<String> oldChannelLabel) throws IOException {
        // TODO synchronize at file level not on the class instance
        if (!ConfigChannelType.NORMAL.equals(channel.getConfigChannelType().getLabel())) {
            throw new IllegalArgumentException("Only 'normal' configuration channels are " +
                    "supported");
        }
        File channelDir = Paths.get(baseDirPath).resolve(getChannelRelativePath(channel))
                .toFile();
        if (channelDir.exists()) {
            FileUtils.cleanDirectory(channelDir);
        }

        for (ConfigFile file : ofNullable(channel.getConfigFiles())
                .orElse(emptySortedSet())) {
            generateConfigFile(channelDir, file);
        }

        File stateFile = new File(channelDir, defaultExtension("init.sls"));
        assertStateInOrgDir(channelDir, stateFile);
        FileUtils.writeStringToFile(stateFile, configChannelInitSLSContent(channel),
                encoding);

        // channel has been renamed - trash the file structure associated with the old label
        if (Opt.fold(oldChannelLabel,
                () -> false,
                label -> !label.equals(channel.getLabel()))) {
            removeConfigChannelFiles(channel.getOrgId(), channel.getConfigChannelType(),
                    oldChannelLabel.get());
        }
    }

    /**
     * Generate file for ConfigFile on the disk
     * @param channelDir - the channel namespace on the disk
     * @param file the config file
     * @throws IOException if there is an error when writing on the disk
     */
    private void generateConfigFile(File channelDir, ConfigFile file) throws IOException {
        ConfigRevision latestRev = file.getLatestConfigRevision();
        if (!latestRev.getConfigFileType().getLabel().equals(ConfigFileType.FILE)) {
            // we only generate files, no symlinks/directories
            return;
        }
        File fileOnDisk = new File(channelDir, file.getConfigFileName().getPath());
        fileOnDisk.getParentFile().mkdirs();
        assertStateInOrgDir(channelDir, fileOnDisk);
        LOG.trace("Generating configuration file: " + file.getConfigFileName().getPath());
        FileUtils.writeStringToFile(fileOnDisk,
                latestRev.getConfigContent().getContentsString(),
                encoding);
    }

    private void removeConfigChannelFiles(Long orgId, ConfigChannelType channelType,
            String channelLabel) {
        LOG.trace("Deleting unused file structure for configuration channel: " +
                channelLabel);
        try {
            File channelDirectory = Paths.get(baseDirPath).resolve(
                    getChannelRelativePath(orgId, channelType, channelLabel)).toFile();
            FileUtils.deleteDirectory(channelDirectory);
        }
        catch (IOException e) {
            LOG.error("Error when deleting salt file structure for channel: " +
                    channelLabel + " in org ID: " + orgId, e);
        }
    }

    /**
     * Creates the contents of the init.sls for a configuration channel.
     *
     * Public for testing.
     *
     * @param channel the channel
     * @return the init.sls contents
     */
    public String configChannelInitSLSContent(ConfigChannel channel) {
        StringBuilder builder = new StringBuilder();

        ofNullable(channel.getConfigFiles()).orElse(emptySortedSet()).stream()
                .filter(f -> f.getConfigFileState().getLabel().equals(NORMAL))
                .map(f -> {
                    // todo handle permissions
                    switch (f.getLatestConfigRevision().getConfigFileType().getLabel()) {
                        case ConfigFileType.FILE:
                            return fileState(f);
                        case ConfigFileType.DIR:
                            return directoryState(f);
                        case ConfigFileType.SYMLINK:
                            return symlinkState(f);
                        default:
                            return "";
                    }
                })
                .forEach(s -> builder.append(YamlHelper.INSTANCE.dump(s)));

        return builder.toString();
    }

    private Map<String, Map<String, List<Map<String, Object>>>> fileState(ConfigFile f) {
        Path filePath = Paths.get(f.getConfigFileName().getPath());
        if (filePath.getRoot() != null) {
            filePath = filePath.getRoot().relativize(filePath);
        }
        String saltUri = "salt://" + getChannelRelativePath(f.getConfigChannel())
                .resolve(filePath);

        List<Map<String, Object>> fileParams = new LinkedList<>();
        fileParams.add(singletonMap("name", f.getConfigFileName().getPath()));
        fileParams.add(singletonMap("source", singletonList(saltUri)));
        fileParams.addAll(getModeParams(f.getLatestConfigRevision().getConfigInfo()));

        return singletonMap(
                getFileStateName(f),
                singletonMap("file.managed", fileParams));
    }

    private Map<String, Map<String, List<Map<String, Object>>>> directoryState(
            ConfigFile f) {
        List<Map<String, Object>> fileParams = new LinkedList<>();
        fileParams.add(singletonMap("name", f.getConfigFileName().getPath()));
        fileParams.add(singletonMap("makedirs", true));
        fileParams.addAll(getModeParams(f.getLatestConfigRevision().getConfigInfo()));

        return singletonMap(
                getFileStateName(f),
                singletonMap("file.directory", fileParams));
    }

    private Map<String, Map<String, List<Map<String, Object>>>> symlinkState(ConfigFile f) {
        List<Map<String, Object>> fileParams = new LinkedList<>();
        fileParams.add(singletonMap("name", f.getConfigFileName().getPath()));
        fileParams.add(singletonMap("target", f.getLatestConfigRevision().getConfigInfo()
                .getTargetFileName().getPath()));

        return singletonMap(
                getFileStateName(f),
                singletonMap(
                        "file.symlink",
                        fileParams));
    }

    private List<Map<String, Object>> getModeParams(ConfigInfo configInfo) {
        List<Map<String, Object>> modeParams = new LinkedList<>();
        modeParams.add(singletonMap("user", configInfo.getUsername()));
        modeParams.add(singletonMap("group", configInfo.getGroupname()));
        modeParams.add(singletonMap("mode", configInfo.getFilemode()));
        return modeParams;
    }

    private void assertStateInOrgDir(File orgDir, File stateFile) throws IOException {
        if (!stateFile.getCanonicalFile().getParentFile().getAbsolutePath()
                .startsWith(orgDir.getCanonicalFile().getAbsolutePath())) {
            throw new IllegalArgumentException("Trying to write state " +
                    "into another directory");
        }
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
     * Get the Salt namespace of the organization.
     * @param orgId the organization id
     * @return the Salt namespace
     */
    public String getOrgNamespace(long orgId) {
        return "manager_configuration_org_" + orgId;
    }

    /**
     * Get the channel relative path which consists of:
     * - organization namespace directory
     * - label of the channel type
     * - label of the channel
     *
     * @param channel the channel
     * @return the channel relative path
     */
    private Path getChannelRelativePath(ConfigChannel channel) {
        return getChannelRelativePath(
                channel.getOrgId(),
                channel.getConfigChannelType(),
                channel.getLabel());
    }

    /**
     * Get the channel relative path which consists of:
     * - organization namespace directory
     * - label of the channel type
     * - label of the channel
     *
     * @param orgId the channel organization id
     * @param channelType the channel type
     * @param channelLabel the channel label
     * @return the channel relative path
     */
    private Path getChannelRelativePath(Long orgId, ConfigChannelType channelType,
            String channelLabel) {
        return Paths.get((getOrgNamespace(orgId)))
                .resolve(channelType.getLabel())
                .resolve(channelLabel);
    }

    /**
     * Get the name of the salt state corresponding to the given channel.
     *
     * @param channel the channel
     * @return the name of the channel salt state
     */
    public String getChannelStateName(ConfigChannel channel) {
        return getOrgNamespace(channel.getOrgId()) + "." +
                channel.getConfigChannelType().getLabel() + "." +
                channel.getLabel();
    }

    /**
     * Get the unique state name for a configuration file.
     * Public for testing purposes.
     *
     * @param file config file
     * @return the unique state name
     */
    public String getFileStateName(ConfigFile file) {
        return getChannelStateName(file.getConfigChannel()) + "." +
                file.getConfigFileName().getPath();
    }
}
