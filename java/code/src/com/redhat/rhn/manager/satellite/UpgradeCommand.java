/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigContent;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.task.TaskFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.manager.BaseTransactionCommand;
import com.redhat.rhn.manager.kickstart.KickstartSessionCreateCommand;

import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltConstants;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.suse.manager.webui.services.SaltConstants.ORG_STATES_DIRECTORY_PREFIX;


/**
 * Class responsible for executing one-time upgrade logic
 */
public class UpgradeCommand extends BaseTransactionCommand {

    /**
     * Logger for this class
     */
    private static Logger log = Logger.getLogger(UpgradeCommand.class);

    public static final String UPGRADE_TASK_NAME = "upgrade_satellite_";
    public static final String UPGRADE_KS_PROFILES =
            UPGRADE_TASK_NAME + "kickstart_profiles";
    public static final String UPGRADE_CUSTOM_STATES =
            UPGRADE_TASK_NAME + "custom_states";
    public static final String UPGRADE_REFRESH_CUSTOM_SLS_FILES =
            UPGRADE_TASK_NAME + "refresh_custom_sls_files";

    private final Path saltRootPath;
    private final Path legacyStatesBackupDirectory;
    private static final String ORG_CFG_CHANNEL_LEGACY_PREFIX = "mgr_cfg_org_";

    /**
     * Constructor
     */
    public UpgradeCommand() {
        this(
                Paths.get(SaltConstants.SUMA_STATE_FILES_ROOT_PATH),
                Paths.get(SaltConstants.LEGACY_STATES_BACKUP));
    }

    /**
     * Constructor allowing parameters mocking.
     *
     * @param saltRootPathIn - custom salt root path
     * @param legacyStatesBackupDirectoryIn - custom legacy statates backup directory
     */
    public UpgradeCommand(Path saltRootPathIn, Path legacyStatesBackupDirectoryIn) {
        super(log);
        this.saltRootPath = saltRootPathIn;
        this.legacyStatesBackupDirectory = legacyStatesBackupDirectoryIn;
    }


    /**
     * Executes the upgrade step in an own transaction
     */
    public void store() {
        try {
            upgrade();
        }
        catch (Exception e) {
            log.error("Problem upgrading!", e);
            HibernateFactory.rollbackTransaction();

        }
        finally {
            handleTransaction();
        }
    }


    /**
     * Executes the upgrade step
     */
    public void upgrade() {
        List upgradeTasks = TaskFactory.getTaskListByNameLike(UPGRADE_TASK_NAME);
        // Loop over upgrade tasks and execute the steps.
        for (int i = 0; i < upgradeTasks.size(); i++) {
            Task t = (Task) upgradeTasks.get(i);
            // Use WARN because we want this logged.
            if (t != null) {
                log.warn("got upgrade task: " + t.getName());
                switch(t.getName()) {
                    case UPGRADE_KS_PROFILES:
                        processKickstartProfiles();
                        break;
                    case UPGRADE_CUSTOM_STATES:
                        processCustomStates();
                        break;
                    case UPGRADE_REFRESH_CUSTOM_SLS_FILES:
                        refreshCustomSlsFiles();
                        break;
                    default:
                }
                // always run this
                TaskFactory.remove(t);
            }
        }
    }

    private void processKickstartProfiles() {
        // Use WARN here because we want this operation logged.
        log.warn("Processing ks profiles.");
        List allKickstarts = KickstartFactory.listAllKickstartData();
        for (int i = 0; i < allKickstarts.size(); i++) {
            KickstartData ksdata = (KickstartData) allKickstarts.get(i);
            KickstartSession ksession =
                KickstartFactory.lookupDefaultKickstartSessionForKickstartData(ksdata);
            if (ksession == null) {
                log.warn("Kickstart does not have a session: id: " + ksdata.getId() +
                        " label: " + ksdata.getLabel());
                KickstartSessionCreateCommand kcmd = new KickstartSessionCreateCommand(
                        ksdata.getOrg(), ksdata);
                kcmd.store();
                log.warn("Created kickstart session and key");
            }

        }
    }

    /**
     * Migrates the legacy custom states stored in the salt root on the filesystem to the database
     * and regenerates the contents of the (normal + state) configuration channels + their assignment
     * to the systems, groups and orgs on the disk.
     *
     * The custom states now make use of the {@link ConfigChannel} and related classes.
     *
     * Database migration ensured that for each legacy custom state there is a {@link ConfigChannel}
     * with {@link ConfigFile} with path='/init.sls', single {@link ConfigRevision} pointing to
     * {@link ConfigContent} with empty content.
     *
     * This method is responsible of populating that {@link ConfigContent} based on the contents
     * of the state file on the disk.
     *
     * Before the import, the files corresponding to the legacy states are backed up to a separate directory.
     * After the import, the legacy state files are deleted.
     *
     * If the process of backing up fails, neither the import nor the clean up will happen.
     */
    private void processCustomStates() {
        backupLegacyStates();
        importLegacyStatesToDb();
        cleanUpLegacyStates();

        // Re-generate the configuration channels
        cleanUpLegacyConfigChannelDirectory();
        regenerateConfigChannelFiles();
    }

    /**
     * Backs up the directories with legacy custom states.
     *
     * @throws java.lang.RuntimeException if some IO error happens during the process
     */
    private void backupLegacyStates() {
        try {
            Set<Path> orgStateDirs = listDirsWithPrefix(ORG_STATES_DIRECTORY_PREFIX);
            legacyStatesBackupDirectory.toFile().mkdirs();
            for (Path stateDir : orgStateDirs) {
                FileUtils.copyDirectory(
                        stateDir.toFile(),
                        legacyStatesBackupDirectory.resolve(stateDir.getFileName()).toFile());
            }
        }
        catch (IOException e) {
            log.error("Error backing up legacy custom states. Not importing them to the database.");
            // when backup failed, we don't want to continue
            throw new RuntimeException(e);
        }
    }

    /**
     * Populates the state file contents in newly created state channels.
     *
     * @throws java.lang.RuntimeException if some IO error happens during the process
     */
    private void importLegacyStatesToDb() {
        List<Object[]> candidates = HibernateFactory.getSession()
                .getNamedQuery("ConfigRevision.stateContentMigrationCandidates").list();
        // Use WARN here because we want this operation logged.
        log.warn("Migrating content of " + candidates.size() + " custom states from disk to database.");
        candidates.forEach(row -> {
            Long orgId = (Long) row[0];
            String channelLabel = (String) row[1];
            ConfigRevision revision = (ConfigRevision) row[2];

            Path statePath = saltRootPath
                    .resolve(ORG_STATES_DIRECTORY_PREFIX + orgId)
                    .resolve(channelLabel + ".sls");

            log.info("Migrating " + channelLabel + " from path " + statePath + ".");

            try {
                byte[] bytes = FileUtils.readFileToByteArray(statePath.toFile());
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                ConfigContent content = ConfigurationFactory.createNewContentFromStream(stream,
                        (long) bytes.length, false, "{|", "|}");
                revision.setConfigContent(content);
                ConfigurationFactory.getSession().save(revision);
            }
            catch (IOException e) {
                log.error("Error when importing state '" + channelLabel +
                        "' from file '" + statePath + "'. Skipping this state.", e);
                // when import failed, we don't want to continue
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Delete the directories with the legacy states.
     */
    private void cleanUpLegacyStates() {
        try {
            for (Path stateDir : listDirsWithPrefix(ORG_STATES_DIRECTORY_PREFIX)) {
                Collection<File> legacySlsFiles = FileUtils.listFiles(
                        stateDir.toFile(),
                        new String[]{"sls"},
                        false);
                for (File file : legacySlsFiles) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
        catch (IOException e) {
            log.error("Error cleaning up directory with legacy custom states. Ignoring.", e);
        }
    }

    private void cleanUpLegacyConfigChannelDirectory() {
        try {
            for (Path dir : listDirsWithPrefix(ORG_CFG_CHANNEL_LEGACY_PREFIX)) {
                FileUtils.deleteDirectory(dir.toFile());
            }
        }
        catch (IOException e) {
            log.error("Error when cleaning legacy config channel directory. Ignoring.", e);
        }
    }

    // re-generates config channels (state + normal) + their assignments on the disk
    private void regenerateConfigChannelFiles() {
        List<ConfigChannel> globalChannels = ConfigurationFactory.listGlobalChannels();
        ConfigChannelSaltManager.getInstance().generateConfigChannelFiles(globalChannels);
        globalChannels.forEach(SaltStateGeneratorService.INSTANCE::regenerateConfigStates);
    }

    // list of directories with given prefix and natural number suffix in the salt root
    private Set<Path> listDirsWithPrefix(String prefix) throws IOException {
        return Files.list(saltRootPath)
                .filter(path -> path.getFileName().toString().matches("^" + prefix + "\\d*$") &&
                        path.toFile().isDirectory())
                .collect(Collectors.toSet());
    }

    /**
     * Regenerate all minion custom SLS files (/srv/susemanager/salt/custom/custom_*.sls) according to
     * the information stored on the database.
     */
    private void refreshCustomSlsFiles() {
        try {
            List<Org> orgs = OrgFactory.lookupAllOrgs();
            for (Org org : orgs) {
                List<MinionServer> minions = MinionServerFactory
                        .lookupByOrg(org.getId());
                for (MinionServer minion : minions) {
                    ServerStateRevision serverRev = StateFactory
                            .latestStateRevision(minion)
                            .orElseGet(() -> {
                                ServerStateRevision rev =
                                        new ServerStateRevision();
                                rev.setServer(minion);
                                return rev;
                            });
                    SaltStateGeneratorService.INSTANCE.generateConfigState(serverRev, saltRootPath);
                }
            }
            log.info("Regenerated minion, org and group .sls files in " + saltRootPath);
        }
        catch (Exception e) {
            log.error("Error refreshing custom SLS files. Ignoring.", e);
        }
    }
}
