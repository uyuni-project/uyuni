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
import com.redhat.rhn.domain.config.ConfigContent;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.task.TaskFactory;
import com.redhat.rhn.manager.BaseTransactionCommand;
import com.redhat.rhn.manager.kickstart.KickstartSessionCreateCommand;

import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltConstants;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final Path saltRootPath;
    private final Path legacyStatesBackupDirectory;
    private static final String ORG_STATE_DIR_PREFIX = "manager_org_";

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
     * Migrates the legacy custom states stored in the salt root on the filesystem to the database.
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
     * After the import, the directories of the legacy states are deleted.
     *
     * If the process of backing up fails, neither the import nor the clean up will happen.
     *
     * (This method can be safely removed when ... TODO complete this comment)
     */

    private void processCustomStates() {
        backupLegacyStates();
        importLegacyStatesToDb();
        cleanUpLegacyStates();
    }

    /**
     * Backs up the directories with legacy custom states.
     *
     * @throws java.lang.RuntimeException if some IO error happens during the process
     */
    private void backupLegacyStates() {
        try {
            Set<Path> orgStateDirs = listOrgStateDirs();
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
                    .resolve(ORG_STATE_DIR_PREFIX + orgId)
                    .resolve(channelLabel + ".sls");

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

        // Let's generate the channels content on the disk
        candidates.stream()
                .map(row -> (ConfigRevision) row[2])
                .map(revision -> revision.getConfigFile().getConfigChannel())
                .distinct()
                .forEach(channel -> {
                    if (!ConfigChannelSaltManager.getInstance().areFilesGenerated(channel)) {
                        ConfigChannelSaltManager.getInstance().generateConfigChannelFiles(channel);
                    }
                    SaltStateGeneratorService.INSTANCE.regenerateConfigStates(channel);
                });
    }

    /**
     * Delete the directories with the legacy states.
     */
    private void cleanUpLegacyStates() {
        try {
            for (Path stateDir : listOrgStateDirs()) {
                FileUtils.deleteDirectory(stateDir.toFile());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            log.error("Error cleaning up directory with legacy custom states. Ignoring.");
        }
    }

    // list of state directories of organizations
    private Set<Path> listOrgStateDirs() throws IOException {
        return Files.list(saltRootPath)
                .filter(path -> path.getFileName().toString().matches("^" + ORG_STATE_DIR_PREFIX + "\\d*$") &&
                        path.toFile().isDirectory())
                .collect(Collectors.toSet());
    }
}

