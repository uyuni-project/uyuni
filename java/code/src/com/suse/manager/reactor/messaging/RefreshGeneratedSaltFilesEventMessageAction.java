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

import com.redhat.rhn.common.conf.ConfigDefaults;
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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Regenerate all state assignment .sls files for orgs and groups.
 */
public class RefreshGeneratedSaltFilesEventMessageAction extends AbstractDatabaseAction {

    private static Logger log = Logger
            .getLogger(RefreshGeneratedSaltFilesEventMessageAction.class);

    private String suseManagerStatesFileRoot;

    private String saltGenerationTempDir;

    private ReentrantLock lock = new ReentrantLock();

    /**
     * No arg constructor.
     */
    public RefreshGeneratedSaltFilesEventMessageAction() {
        this.suseManagerStatesFileRoot = ConfigDefaults.get()
                .getSaltSuseManagerStatesFileRoot();
        this.saltGenerationTempDir = ConfigDefaults.get()
                .getSaltGenerationTempDir();
    }

    /**
     * @param suseManagerStatesFileRootIn dir where the Salt state files are stored
     * @param saltGenerationTempDirIn the temp dir used to generate the state files
     */
    public RefreshGeneratedSaltFilesEventMessageAction(String suseManagerStatesFileRootIn,
                                                       String saltGenerationTempDirIn) {
        this.suseManagerStatesFileRoot = suseManagerStatesFileRootIn;
        this.saltGenerationTempDir = saltGenerationTempDirIn;
    }

    @Override
    protected void doExecute(EventMessage msg) {
        if (lock.tryLock()) {
            try {
                // generate org and group files to /srv/susemanager/tmp/salt
                Path tempSaltRootPath = Paths.get(saltGenerationTempDir, "salt");
                FileUtils.deleteDirectory(tempSaltRootPath.toFile());
                Files.createDirectories(tempSaltRootPath);

                SaltStateGeneratorService generatorService =
                        new SaltStateGeneratorService();
                generatorService.setGeneratedSlsRoot(tempSaltRootPath.toString());

                List<Org> orgs = OrgFactory.lookupAllOrgs();
                for (Org org : orgs) {
                    OrgStateRevision orgRev = StateFactory.latestStateRevision(org)
                            .orElseGet(() -> {
                                OrgStateRevision rev = new OrgStateRevision();
                                rev.setOrg(org);
                                return rev;
                            });
                    generatorService.generateOrgCustomState(orgRev);

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
                        generatorService.generateGroupCustomState(groupRev);
                    }
                }

                Path saltPath = Paths.get(suseManagerStatesFileRoot,
                        SaltStateGeneratorService.SALT_CUSTOM_STATES);
                Path oldSaltPath = Paths.get(saltGenerationTempDir,
                        SaltStateGeneratorService.SALT_CUSTOM_STATES + "_todelete");
                Path tempCustomPath = tempSaltRootPath
                        .resolve(SaltStateGeneratorService.SALT_CUSTOM_STATES);

                // copy /srv/susemanager/salt/custom/custom_*.sls
                // to /srv/susemanager/tmp/salt
                for (Path serverSls : Files.newDirectoryStream(saltPath,
                        SaltStateGeneratorService.SERVER_SLS_PREFIX + "*.sls")) {
                    Files.copy(serverSls, tempCustomPath.resolve(serverSls.getFileName()));
                }

                // rm -rf /srv/susemanager/tmp/custom_todelete
                FileUtils.deleteDirectory(oldSaltPath.toFile());
                // mv /srv/susemanager/salt/custom -> /srv/susemanager/tmp/custom_todelete
                Files.move(saltPath, oldSaltPath, StandardCopyOption.ATOMIC_MOVE);
                // mv /srv/susemanager/tmp/salt -> /srv/susemanager/salt/custom
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
                lock.unlock();
            }
        }
        else {
            log.warn("Refreshing generated Salt files is already executing");
        }
    }

}
