package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.webui.services.SaltCustomStateStorageManager;
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
 * Created by matei on 4/7/16.
 */
public class RefreshGeneratedSaltFilesEventMessageAction extends AbstractDatabaseAction {

    private static Logger log = Logger.getLogger(RefreshGeneratedSaltFilesEventMessageAction.class);

    private static final String TEMP_DIR = "/srv/susemanager/tmp";


    @Override
    protected void doExecute(EventMessage msg) {

        try {
            // TODO do not allow multiple executions of this action

            Path tempSaltPath = Paths.get(getTempDir(), "salt");
            FileUtils.deleteDirectory(tempSaltPath.toFile());
            Files.createDirectories(tempSaltPath);

            SaltStateGeneratorService generatorService = new SaltStateGeneratorService();
            generatorService.setGeneratedSlsRoot(tempSaltPath.toString());

            List<Org> orgs = OrgFactory.lookupAllOrgs();
            for (Org org : orgs) {
                OrgStateRevision orgRev = StateFactory.latestStateRevision(org)
                    .orElseGet(() -> {
                        OrgStateRevision rev = new OrgStateRevision();
                        rev.setOrg(org);
                        return rev;
                    });
                generatorService.generateOrgCustomState(orgRev);

                List<ManagedServerGroup> groups = ServerGroupFactory.listManagedGroups(org);
                for (ManagedServerGroup group : groups) {
                    ServerGroupStateRevision groupRev = StateFactory.latestStateRevision(group)
                        .orElseGet(() -> {
                            ServerGroupStateRevision rev = new ServerGroupStateRevision();
                            rev.setGroup(group);
                            return rev;
                        });
                    SaltStateGeneratorService.instance().generateGroupCustomState(groupRev);
                }
            }

            Path saltPath = Paths.get(SaltCustomStateStorageManager.GENERATED_SLS_ROOT,
                    SaltStateGeneratorService.SALT_CUSTOM_STATES);
            Path saltOldPath = Paths.get(SaltCustomStateStorageManager.GENERATED_SLS_ROOT,
                    SaltStateGeneratorService.SALT_CUSTOM_STATES + "_todelete");

            // TODO COPY custom/custom_*.sls to tempSaltPath

            FileUtils.deleteDirectory(saltOldPath.toFile());
            Files.move(saltPath, saltOldPath, StandardCopyOption.ATOMIC_MOVE);
            Files.move(tempSaltPath, saltPath, StandardCopyOption.ATOMIC_MOVE);
            FileUtils.deleteDirectory(saltOldPath.toFile());

        } catch (IOException e) {
            log.error("Could not regenerate org and group sls files in " + getTempDir(), e);
        }
    }

    private String getTempDir() {
        return Config.get().getString("salt.generation.temp.dir", TEMP_DIR);
    }


}
