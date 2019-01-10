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
package com.suse.manager.reactor.messaging;

import static com.suse.manager.webui.services.SaltConstants.SALT_CONFIG_STATES_DIR;
import static com.suse.manager.webui.services.SaltConstants.SALT_FILE_GENERATION_TEMP_PATH;
import static com.suse.manager.webui.services.SaltConstants.SALT_SERVER_STATE_FILE_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.SUMA_PILLAR_DATA_PATH;
import static com.suse.manager.webui.services.SaltConstants.SUMA_STATE_FILES_ROOT_PATH;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.StateFactory;

import com.suse.manager.webui.services.SaltStateGeneratorService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Regenerate all state assignment .sls files for orgs and groups.
 */
public class RefreshGeneratedSaltFilesEventMessageAction implements MessageAction {

    private static Logger log = Logger
            .getLogger(RefreshGeneratedSaltFilesEventMessageAction.class);

    private Path suseManagerStatesFilesRoot;

    private Path saltGenerationTempDir;

    /**
     * No arg constructor.
     */
    public RefreshGeneratedSaltFilesEventMessageAction() {
        this.suseManagerStatesFilesRoot = Paths.get(SUMA_STATE_FILES_ROOT_PATH);
        this.saltGenerationTempDir = Paths.get(SALT_FILE_GENERATION_TEMP_PATH);
    }

    /**
     * @param suseManagerStatesFileRootIn dir where the Salt state files are stored
     * @param saltGenerationTempDirIn the temp dir used to generate the state files
     */
    public RefreshGeneratedSaltFilesEventMessageAction(String suseManagerStatesFileRootIn,
                                                       String saltGenerationTempDirIn) {
        this.suseManagerStatesFilesRoot = Paths.get(suseManagerStatesFileRootIn);
        this.saltGenerationTempDir = Paths.get(saltGenerationTempDirIn);
    }

    @Override
    public void execute(EventMessage msg) {
        try {
            refreshFiles();
        }
        catch (IOException e) {
            log.error("Could not regenerate org and group sls files in " +
                    saltGenerationTempDir, e);
        }
    }

    /**
     * Regenerate all state assignment .sls files for orgs and groups.
     * Is public to allow testing.
     * @throws IOException in case files could not be written
     */
    public void refreshFiles() throws IOException {
        Path tempSaltRootPath = null;
        try {
            // generate org and group files to temp dir /srv/susemanager/tmp/saltXXXX
            Files.createDirectories(saltGenerationTempDir);
            tempSaltRootPath = Files
                    .createTempDirectory(saltGenerationTempDir, "salt");
            log.debug("Created temporary dir " + tempSaltRootPath);

            List<Org> orgs = OrgFactory.lookupAllOrgs();
            for (Org org : orgs) {
                OrgStateRevision orgRev = StateFactory.latestStateRevision(org)
                        .orElseGet(() -> {
                            OrgStateRevision rev = new OrgStateRevision();
                            rev.setOrg(org);
                            return rev;
                        });
                SaltStateGeneratorService.INSTANCE.generateConfigState(orgRev, tempSaltRootPath);

                List<ManagedServerGroup> groups = ServerGroupFactory
                        .listManagedGroups(org);
                for (ManagedServerGroup group : groups) {
                    ServerGroupStateRevision groupRev = StateFactory
                            .latestStateRevision(group)
                            .orElseGet(() -> {
                                ServerGroupStateRevision rev =
                                        new ServerGroupStateRevision();
                                rev.setGroup(group);
                                return rev;
                            });
                    SaltStateGeneratorService.INSTANCE.generateConfigState(groupRev, tempSaltRootPath);
                }
            }

            SaltStateGeneratorService.INSTANCE.generateMgrConfPillar(Paths.get(SUMA_PILLAR_DATA_PATH));

            Path saltPath = suseManagerStatesFilesRoot.resolve(SALT_CONFIG_STATES_DIR);
            Path oldSaltPath = saltGenerationTempDir.resolve(
                    SALT_CONFIG_STATES_DIR + "_todelete");
            Path tempCustomPath = tempSaltRootPath
                    .resolve(SALT_CONFIG_STATES_DIR);

            // copy /srv/susemanager/salt/custom/custom_*.sls
            // to /srv/susemanager/tmpXXXX/salt/custom
            if (Files.exists(saltPath)) {
                for (Path serverSls : Files.newDirectoryStream(saltPath,
                        SALT_SERVER_STATE_FILE_PREFIX + "*.sls")) {
                    Files.copy(serverSls, tempCustomPath.resolve(serverSls.getFileName()));
                }
            }

            // rm -rf /srv/susemanager/tmp/custom_todelete
            FileUtils.deleteDirectory(oldSaltPath.toFile());
            // mv /srv/susemanager/salt/custom -> /srv/susemanager/tmp/custom_todelete
            if (Files.exists(saltPath)) {
                Files.move(saltPath, oldSaltPath, StandardCopyOption.ATOMIC_MOVE);
            }
            // mv /srv/susemanager/tmp/saltXXXX/custom -> /srv/susemanager/salt/custom
            if (Files.exists(tempCustomPath)) {
                // this condition is needed only at setup time when there are no orgs yet
                Files.move(tempCustomPath, saltPath, StandardCopyOption.ATOMIC_MOVE);
            }
            // rm -rf /srv/susemanager/tmp/custom_todelete
            if (Files.exists(oldSaltPath)) {
                FileUtils.deleteDirectory(oldSaltPath.toFile());
            }

            log.info("Regenerated org and group .sls files in " + saltPath);
        }
        finally {
            if (tempSaltRootPath != null) {
                try {
                    log.debug("Removing temporary dir " + tempSaltRootPath);
                    FileUtils.deleteDirectory(tempSaltRootPath.toFile());
                }
                catch (IOException e) {
                    log.error("Could not remove temporary directory " +
                            tempSaltRootPath, e);
                }
            }
        }
    }

}
