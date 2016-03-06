package com.suse.manager.webui.services;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.MinionServerUtils;
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
     * Generate server specific pillar if the given server is a minion.
     * @param server
     */
    public void generatePillarForServer(Server server) {
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
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

    /**
     * Remove the corresponding pillar data if the server is a minion.
     * @param server
     */
    public void removePillarForServer(Server server) {
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
        LOG.debug("Removing pillar file for server name= " + server.getName()
                + " digitalId=" + server.getDigitalServerId());
        Path baseDir = Paths.get(GENERATED_PILLAR_ROOT);
        Path filePath = baseDir.resolve(
                defaultExtension("server_" + server.getDigitalServerId()));
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            LOG.error("Could not remove pillar file " + filePath);
        }
    }

//    public void removePillarForServerGroup(ServerGroup group) {
//        List<Server> groupServers = ServerGroupFactory.listServers(group);
//        removePillarFromServers(groupServers);
//    }
//
//    public void removePillarFromOrg(Org org) {
//        List<Server> orgServers = ServerFactory.lookupByOrg(org.getId());
//        removePillarFromServers(orgServers);
//    }
//
//
//    private void removePillarFromServers(List<Server> servers) {
//        List<Server> minionServers = MinionServerUtils
//                .filterSaltMinionIds(servers, (s) -> s);
//        for (Server server : minionServers) {
//            generatePillarForServer(server);
//        }
//    }

    public void generateServerCustomState(ServerStateRevision stateRevision) {
        Server server = stateRevision.getServer();
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
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
