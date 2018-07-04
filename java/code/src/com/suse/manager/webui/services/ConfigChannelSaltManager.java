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
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigInfo;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.utils.Opt;
import org.apache.commons.codec.CharEncoding;
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
import java.util.Collections;
import java.util.HashMap;

import static com.redhat.rhn.domain.config.ConfigFileState.NORMAL;
import static com.suse.manager.webui.services.SaltConstants.SALT_FS_PREFIX;
import static com.suse.manager.webui.utils.SaltFileUtils.defaultExtension;
import static java.util.Collections.emptySortedSet;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Singleton class, renders salt files corresponding to a configuration channel on the disk.
 */
public class ConfigChannelSaltManager {

    private String baseDirPath;
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
     * '/srv/susemanager/salt'.
     */
    private ConfigChannelSaltManager() {
        this.baseDirPath = SaltConstants.SUMA_STATE_FILES_ROOT_PATH;
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
     * Store the salt file structure for a list of config channels on the disk.
     *
     * @param channelList - the list of config channels
     * @throws IOException in case of an IO error
     */
    public void generateConfigChannelFiles(List<ConfigChannel> channelList) {
        channelList.forEach(cc -> {
            // Generate config files in the filesystem for salt state application
            if (!this.areFilesGenerated(cc)) {
                this.generateConfigChannelFiles(cc);
            }
        });
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
            doGenerateConfigChannelFiles(channel);
        }
        catch (IOException e) {
            LOG.error("Error when handling salt file structure for channel: " +
                    channel + " (old channel label: " +
                    oldChannelLabel.orElse("<empty>") + "). " +
                    "Removing files from disk.", e);
            removeConfigChannelFiles(channel.getOrgId(), channel.getLabel());
        }
        finally {
            // channel has been renamed - trash the file structure associated with the old
            // label
            if (Opt.fold(oldChannelLabel,
                    () -> false,
                    label -> !label.equals(channel.getLabel()))) {
                removeConfigChannelFiles(channel.getOrgId(), oldChannelLabel.get());
            }
        }
    }

    /**
     * Removes the salt file structure for a config channel from the disk.
     *
     * @param channel the channel
     */
    public synchronized void removeConfigChannelFiles(ConfigChannel channel) {
        removeConfigChannelFiles(channel.getOrgId(), channel.getLabel());
    }

    /**
     * Perform actual disk actions to reflect the configuration channel state on the disk.
     *
     * @param channel - the config channel
     * @throws IOException in case of an IO error
     */
    private void doGenerateConfigChannelFiles(ConfigChannel channel) throws IOException {
        // TODO synchronize at file level not on the class instance
        if (!(channel.isNormalChannel() || channel.isStateChannel())) {
            LOG.debug("Trying to generate salt files for incompatible channel type " +
                    "(channel: " + channel + "). Skipping. (Only 'normal' and 'state' " +
                    "configuration channels are supported.)");
            return;
        }
        File channelDir = getChannelDir(channel);
        if (channelDir.exists()) {
            FileUtils.cleanDirectory(channelDir);
        }

        for (ConfigFile file : ofNullable(channel.getConfigFiles())
                .orElse(emptySortedSet())) {
            generateConfigFile(channelDir, file);
        }
        if (channel.isNormalChannel()) {
            File stateFile = new File(channelDir, defaultExtension("init.sls"));
            writeTextFile(configChannelInitSLSContent(channel), channelDir, stateFile);
        }
    }

    private File getChannelDir(ConfigChannel channel) {
        return Paths.get(baseDirPath).resolve(getChannelRelativePath(channel)).toFile();
    }

    /**
     * Generate file for ConfigFile on the disk
     * @param channelDir - the channel namespace on the disk
     * @param file the config file
     * @throws IOException if there is an error when writing on the disk
     */
    private void generateConfigFile(File channelDir, ConfigFile file) throws IOException {
        ConfigRevision latestRev = file.getLatestConfigRevision();
        if (!(latestRev.isFile() || latestRev.isSls())) {
            // we only generate files/sls, no symlinks/directories
            return;
        }
        LOG.trace("Generating configuration file: " + file.getConfigFileName().getPath());
        File fileOnDisk = new File(channelDir, file.getConfigFileName().getPath());

        if (latestRev.getConfigContent().isBinary()) {
            writeBinaryFile(latestRev.getConfigContent().getContents(), channelDir, fileOnDisk);
        }
        else {
            writeTextFile(latestRev.getConfigContent().getContentsString(), channelDir, fileOnDisk);
        }
    }

    /**
     * Checks that the outFile is inside the channel directory and writes the contents to
     * it.
     *
     * @param content string to be written
     * @param channelDir the channel directory
     * @param outFile the output file
     * @throws IllegalArgumentException if there is an attempt to write file outside channel
     * directory
     * @throws IOException if there is an error when writing on the disk
     */
    private void writeTextFile(String content, File channelDir, File outFile)
            throws IOException {
        assertStateInOrgDir(channelDir, outFile);
        outFile.getParentFile().mkdirs();
        FileUtils.writeStringToFile(outFile, content, CharEncoding.UTF_8);
     }

    /**
     * Checks that the outFile is inside the channel directory and writes the contents to
     * it.
     *
     * @param content byte[] ConfigContent to be written
     * @param channelDir the channel directory
     * @param outFile the output file
     * @throws IllegalArgumentException if there is an attempt to write file outside channel
     * directory
     * @throws IOException if there is an error when writing on the disk
     */
    private void writeBinaryFile(byte[] content, File channelDir, File outFile)  throws IOException {
        assertStateInOrgDir(channelDir, outFile);
        outFile.getParentFile().mkdirs();
        FileUtils.writeByteArrayToFile(outFile, content);

    }

    private void removeConfigChannelFiles(Long orgId, String channelLabel) {
        LOG.trace("Deleting unused file structure for configuration channel: " +
                channelLabel);
        try {
            File channelDirectory = Paths.get(baseDirPath).resolve(
                    getChannelRelativePath(orgId, channelLabel)).toFile();
            FileUtils.deleteDirectory(channelDirectory);
        }
        catch (IOException e) {
            LOG.error("Error when deleting salt file structure for channel: " +
                    channelLabel + " in org ID: " + orgId, e);
        }
    }

    /**
     * Generates the contents of the init.sls for a configuration channel.
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

    private Map<String, Map<String, List<Map<String, Object>>>> fileState(ConfigFile file) {
        return singletonMap(
                getFileStateName(file),
                singletonMap("file.managed", getFileStateParams(file)));
    }

    /**
     * Get the file params list for salt
     * @param file file
     * @return List of params map
     */
    private List<Map<String, Object>> getFileStateParams(ConfigFile file) {
        Path filePath = Paths.get(file.getConfigFileName().getPath());
        if (filePath.getRoot() != null) {
            filePath = filePath.getRoot().relativize(filePath);
        }

        String saltUri = SALT_FS_PREFIX + getChannelRelativePath(file.getConfigChannel())
                .resolve(filePath);
        List<Map<String, Object>> fileParams = new LinkedList<>();
        fileParams.add(singletonMap("name", file.getConfigFileName().getPath()));
        fileParams.add(singletonMap("source", getSaltUriForConfigFile(file)));
        fileParams.add(singletonMap("makedirs", true));
        fileParams.addAll(getModeParams(file.getLatestConfigRevision().getConfigInfo()));
        return fileParams;
    }

    private ConfigFile getInitSlsFileForChannel(ConfigChannel channel) {
        return channel.getConfigFiles().stream()
                .filter(f -> "/init.sls".equals(f.getConfigFileName().getPath()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("init.sls file not found."));
    }

    /**
     * Gets init.sls file content for a config channel.
     *
     * @param channel the channel
     * @return the channel state content
     */
    public String getChannelStateContent(ConfigChannel channel) {
        String content;
        if (channel.isStateChannel()) {
            content = getInitSlsFileForChannel(channel)
                    .getLatestConfigRevision()
                    .getConfigContent()
                    .getContentsString();
        }
        else if (channel.isNormalChannel()) {
            content = this.configChannelInitSLSContent(channel);
        }
        else {
            throw new IllegalArgumentException("Invalid ConfigChannel type.");
        }

        return content;
    }

    /**
     * Gets salt base URI for a given channel.
     *
     * @param channel the channel
     * @return the salt base URI for channel (starting with salt://)
     */
    public String getSaltBaseUriForChannel(ConfigChannel channel) {
        return SALT_FS_PREFIX + getChannelRelativePath(channel);
    }

    /**
     * Gets the salt URI for given file.
     *
     * @param file the file
     * @return the salt uri for the file (starting with salt://)
     */
    public String getSaltUriForConfigFile(ConfigFile file) {
        Path filePath = Paths.get(file.getConfigFileName().getPath());
        if (filePath.getRoot() != null) {
            filePath = filePath.getRoot().relativize(filePath);
        }
        return SALT_FS_PREFIX + getChannelRelativePath(file.getConfigChannel())
                .resolve(filePath);
    }

    private Map<String, Map<String, List<Map<String, Object>>>> directoryState(
            ConfigFile f) {
        return singletonMap(
                getFileStateName(f),
                singletonMap("file.directory", getDirectoryStateParams(f)));
    }

    /**
     * Get the directory params list for salt
     * @param file file
     * @return List of params map
     */
    private List<Map<String, Object>> getDirectoryStateParams(ConfigFile file) {
        List<Map<String, Object>> fileParams = new LinkedList<>();
        fileParams.add(singletonMap("name", file.getConfigFileName().getPath()));
        fileParams.add(singletonMap("makedirs", true));
        fileParams.addAll(getModeParams(file.getLatestConfigRevision().getConfigInfo()));
        return fileParams;
    }

    private Map<String, Map<String, List<Map<String, Object>>>> symlinkState(ConfigFile f) {
        return singletonMap(
                getFileStateName(f),
                singletonMap(
                        "file.symlink",
                        getSymLinkStateParams(f)));
    }

    /**
     * Get the symlink params list for salt
     * @param file file
     * @return List of params map
     */
    private List<Map<String, Object>> getSymLinkStateParams(ConfigFile file) {
        List<Map<String, Object>> fileParams = new LinkedList<>();
        fileParams.add(singletonMap("name", file.getConfigFileName().getPath()));
        fileParams.add(singletonMap("target", file.getLatestConfigRevision().getConfigInfo()
                .getTargetFileName().getPath()));
        fileParams.add(singletonMap("makedirs", true));
        return fileParams;
    }

    /**
     * Get the revision parameters based on its type
     * @param revision ConfigRevision revision
     * @return Map containing all the parameters + the type
     */
    public Map<String, Object> getStateParameters(ConfigRevision revision) {
        List<Map<String, Object>> fileParams = Collections.EMPTY_LIST;
        if (revision.isFile()) {
            fileParams = getFileStateParams(revision.getConfigFile());
        }
        else if (revision.isDirectory()) {
            fileParams = getDirectoryStateParams(revision.getConfigFile());
        }
        else if (revision.isSymlink()) {
            fileParams =  getSymLinkStateParams(revision.getConfigFile());
        }
        Map<String, Object> stateParameters = new HashMap<>();
        stateParameters.put("type", revision.getConfigFileType().getLabel());
        fileParams.stream().forEach(v -> stateParameters.putAll(v));
        return stateParameters;
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
        return SaltConstants.ORG_STATES_DIRECTORY_PREFIX + orgId;
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
                channel.getLabel());
    }

    /**
     * Get the channel relative path which consists of:
     * - organization namespace directory
     * - label of the channel type
     * - label of the channel
     *
     * @param orgId the channel organization id
     * @param channelLabel the channel label
     * @return the channel relative path
     */
    private Path getChannelRelativePath(Long orgId, String channelLabel) {
        return Paths.get((getOrgNamespace(orgId)))
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

    /**
     * Returns true depending on whether salt files structure for given channel exists or
     * false otherwise (internally it just checks whether the channel directory exists).
     *
     * @param channel the channel
     * @return true if the salt files structure for the channel exists, false otherwise
     */
    public boolean areFilesGenerated(ConfigChannel channel) {
        return getChannelDir(channel).exists();
    }
}
