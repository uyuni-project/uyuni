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

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static com.suse.manager.webui.services.SaltConstants.SUMA_STATE_FILES_ROOT_PATH;
import static com.suse.manager.webui.services.SaltConstants.SALT_CUSTOM_STATES_DIR;
import static com.suse.manager.webui.services.SaltConstants.SALT_SERVER_STATE_FILE_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.SALT_FILE_GENERATION_TEMP_PATH;

/**
 * Regenerate all state assignment .sls files for orgs and groups.
 */
public class RefreshGeneratedSaltFilesEventMessageAction extends AbstractDatabaseAction {

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
    protected void doExecute(EventMessage msg) {
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
                SaltStateGeneratorService.INSTANCE.generateOrgCustomState(orgRev,
                        tempSaltRootPath);

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
                    SaltStateGeneratorService.INSTANCE.generateGroupCustomState(
                            groupRev, tempSaltRootPath);
                }
            }

            Path saltPath = suseManagerStatesFilesRoot.resolve(
                    SALT_CUSTOM_STATES_DIR);
            Path oldSaltPath = saltGenerationTempDir.resolve(
                    SALT_CUSTOM_STATES_DIR + "_todelete");
            Path tempCustomPath = tempSaltRootPath
                    .resolve(SALT_CUSTOM_STATES_DIR);

            // copy /srv/susemanager/salt/custom/custom_*.sls
            // to /srv/susemanager/tmp/salt
            for (Path serverSls : Files.newDirectoryStream(saltPath,
                    SALT_SERVER_STATE_FILE_PREFIX + "*.sls")) {
                Files.copy(serverSls, tempCustomPath.resolve(serverSls.getFileName()));
            }

            // rm -rf /srv/susemanager/tmp/custom_todelete
            FileUtils.deleteDirectory(oldSaltPath.toFile());
            // mv /srv/susemanager/salt/custom -> /srv/susemanager/tmp/custom_todelete
            Files.move(saltPath, oldSaltPath, StandardCopyOption.ATOMIC_MOVE);
            // mv /srv/susemanager/tmp/saltXXXX -> /srv/susemanager/salt/custom
            Files.move(tempCustomPath, saltPath, StandardCopyOption.ATOMIC_MOVE);
            // rm -rf /srv/susemanager/tmp/custom_todelete
            FileUtils.deleteDirectory(oldSaltPath.toFile());

            log.info("Regenerated org and group .sls files in " + saltPath);
        }
        catch (IOException e) {
            log.error("Could not regenerate org and group sls files in " +
                    saltGenerationTempDir, e);
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
