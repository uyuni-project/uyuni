package com.suse.manager.webui.services;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.RepoFileUtils;
import com.suse.manager.webui.utils.SaltCustomState;
import com.suse.manager.webui.utils.SaltPillar;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SaltFileUtils.defaultExtension;

/**
 * Created by matei on 3/4/16.
 */
public enum SaltStateGeneratorFacade {

    // Singleton instance of this class
    INSTANCE;

    /** Logger */
    private static final Logger LOG = Logger.getLogger(SaltStateGeneratorFacade.class);

    public static final String SALT_CUSTOM_STATES = "custom";

    public static final String GENERATED_PILLAR_ROOT = "/srv/susemanager/pillar";

    /**
     * Generate server specific pillar
     * @param server
     */
    public void generatePillarForServer(Server server) {
        LOG.debug("Generating pillar file for server name= " + server.getName()
                + " digitalId=" + server.getDigitalServerId());

        List<ManagedServerGroup> groups = ServerGroupFactory.listManagedGroups(server);
        List<Long> groupIds = groups.stream()
                .map(g -> g.getId()).collect(Collectors.toList());
        SaltPillar pillar = new SaltPillar();
        pillar.add("org_id", server.getOrg().getId());
        pillar.add("group_id", groupIds.toArray(new Long[groupIds.size()]));

        try {
            Path baseDir = Paths.get(GENERATED_PILLAR_ROOT);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(
                    defaultExtension("server_" + server.getDigitalServerId()));
            com.suse.manager.webui.utils.SaltStateGenerator saltStateGenerator =
                    new com.suse.manager.webui.utils.SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(pillar);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void generateServerCustomState(ServerStateRevision stateRevision) {
        Server server = stateRevision.getServer();
        LOG.debug("Generating custom state SLS file for server: " + server.getId());

        Set<String> stateNames = stateRevision.getCustomStates()
                .stream().map(s -> s.getStateName())
                .collect(Collectors.toSet());

        stateNames = SaltAPIService.INSTANCE.resolveOrgStates(
                server.getOrg().getId(), stateNames);

        try {
            Path baseDir = Paths.get(
                    RepoFileUtils.GENERATED_SLS_ROOT, SALT_CUSTOM_STATES);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(
                    "custom_" + server.getDigitalServerId() + ".sls");
            com.suse.manager.webui.utils.SaltStateGenerator saltStateGenerator =
                    new com.suse.manager.webui.utils.SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(new SaltCustomState(server.getId(), stateNames));
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    public void generateGroupCustomState(ServerGroupStateRevision stateRevision) {
        ServerGroup group = stateRevision.getGroup();
        LOG.debug("Generating custom state SLS file for server group: " + group.getId());

        Set<String> stateNames = stateRevision.getCustomStates()
                .stream().map(s -> s.getStateName())
                .collect(Collectors.toSet());

        stateNames = SaltAPIService.INSTANCE.resolveOrgStates(
                group.getOrg().getId(), stateNames);

        try {
            Path baseDir = Paths.get(
                    RepoFileUtils.GENERATED_SLS_ROOT, SALT_CUSTOM_STATES);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(defaultExtension("group_" + group.getId()));
            com.suse.manager.webui.utils.SaltStateGenerator saltStateGenerator =
                    new com.suse.manager.webui.utils.SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(new SaltCustomState(group.getId(), stateNames));
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void generateOrgCustomState(OrgStateRevision stateRevision) {
        Org org = stateRevision.getOrg();
        LOG.debug("Generating custom state SLS file for organization: " + org.getId());

        Set<String> stateNames = stateRevision.getCustomStates()
                .stream().map(s -> s.getStateName())
                .collect(Collectors.toSet());

        stateNames = SaltAPIService.INSTANCE.resolveOrgStates(
                org.getId(), stateNames);

        try {
            Path baseDir = Paths.get(
                    RepoFileUtils.GENERATED_SLS_ROOT, SALT_CUSTOM_STATES);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(defaultExtension("org_" + org.getId()));
            com.suse.manager.webui.utils.SaltStateGenerator saltStateGenerator =
                    new com.suse.manager.webui.utils.SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(new SaltCustomState(org.getId(), stateNames));
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
