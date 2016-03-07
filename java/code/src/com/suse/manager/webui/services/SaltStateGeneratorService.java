package com.suse.manager.webui.services;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateRevision;
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
public enum SaltStateGeneratorService {

    // Singleton instance of this class
    INSTANCE;

    /** Logger */
    private static final Logger LOG = Logger.getLogger(SaltStateGeneratorService.class);

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

    public void removeServerCustomState(Server server) {

    }

    public void removeGroupCustomState(ServerGroup group) {

    }

    public void removeOrgCustomState(Org org) {

    }



    public void generateServerCustomState(ServerStateRevision stateRevision) {
        Server server = stateRevision.getServer();
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
        LOG.debug("Generating custom state SLS file for server: " + server.getId());

        generateCustomStates(server.getOrg().getId(), stateRevision,
                defaultExtension("custom_" + server.getDigitalServerId()));
    }

    public void generateGroupCustomState(ServerGroupStateRevision stateRevision) {
        ServerGroup group = stateRevision.getGroup();
        LOG.debug("Generating custom state SLS file for server group: " + group.getId());

        generateCustomStates(group.getOrg().getId(), stateRevision,
                defaultExtension("group_" + group.getId()));
    }


    public void generateOrgCustomState(OrgStateRevision stateRevision) {
        Org org = stateRevision.getOrg();
        LOG.debug("Generating custom state SLS file for organization: " + org.getId());

        generateCustomStates(org.getId(), stateRevision,
                defaultExtension("org_" + org.getId()));
    }

    private void generateCustomStates(long orgId, StateRevision stateRevision, String fileName) {
        Set<String> stateNames = stateRevision.getCustomStates()
                .stream().map(s -> s.getStateName())
                .collect(Collectors.toSet());

        stateNames = SaltAPIService.INSTANCE.resolveOrgStates(
                orgId, stateNames);


        Path baseDir = Paths.get(
                RepoFileUtils.GENERATED_SLS_ROOT, SALT_CUSTOM_STATES);
        try {
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(fileName);
            com.suse.manager.webui.utils.SaltStateGenerator saltStateGenerator =
                    new com.suse.manager.webui.utils.SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(new SaltCustomState(stateNames));
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
