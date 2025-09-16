/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.manager.org;

import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.org.SystemMigration;
import com.redhat.rhn.domain.org.SystemMigrationFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.token.TokenFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.UpdateChildChannelsCommand;

import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.webui.services.SaltStateGeneratorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * MigrationManager
 *
 * Handles the migration of systems from one organization to another.
 *
 */
public class MigrationManager extends BaseManager {

    private final ServerGroupManager groupManager;

    /**
     * Constructor
     *
     * @param groupManagerIn the server group manager
     */
    public MigrationManager(ServerGroupManager groupManagerIn) {
        groupManager = groupManagerIn;
    }

    /**
     * Migrate a set of servers to the organization specified
     * @param user Org admin that is performing the migration
     * @param toOrg The destination org
     * @param servers List of servers to be migrated
     * @return the list of server ids successfully migrated.
     */
    public List<Long> migrateServers(User user, Org toOrg, List<Server> servers) {

        List<Long> serversMigrated = new ArrayList<>();

        for (Server server : servers) {

            Org fromOrg = server.getOrg();
            Set<Entitlement> entitlements = server.getEntitlements();

            removeOrgRelationships(user, server);
            MigrationManager.updateAdminRelationships(fromOrg, toOrg, server);
            MigrationManager.moveServerToOrg(toOrg, server);
            MigrationManager.updateServerEntitlements(toOrg, server, entitlements);
            serversMigrated.add(server.getId());
            OrgFactory.save(toOrg);
            OrgFactory.save(fromOrg);
            ServerFactory.save(server);
            server.asMinionServer().ifPresent(minion ->
                    SaltStateGeneratorService.INSTANCE.migrateServer(minion, user));
            if (user.getOrg().equals(toOrg)) {
                server.setCreator(user);
            }
            else {
                server.setCreator(UserFactory.findRandomOrgAdmin(toOrg));
            }
            // remove old channels from system
            MessageQueue.publish(new ChannelsChangedEventMessage(server.getId(), user.getId(), true));

            // update server history to record the migration.
            ServerHistoryEvent event = new ServerHistoryEvent();
            event.setCreated(new Date());
            event.setServer(server);
            event.setSummary(String.format("System migration scheduled by %s", user.getLogin()));
            String details = String.format("From organization: %s, To organization: %s. " +
                    "User that initiated the transfer: %s", fromOrg.getName(), toOrg.getName(), user.getLogin());
            event.setDetails(details);
            server.getHistory().add(event);

            SystemMigration migration = new SystemMigration();
            migration.setToOrg(toOrg);
            migration.setFromOrg(fromOrg);
            migration.setServer(server);
            migration.setMigrated(new Date());
            SystemMigrationFactory.save(migration);
        }
        return serversMigrated;
    }

    /**
     * Remove a server's relationships with it's current org.
     *
     * Used to clean the servers associations in the database in preparation for migration
     * before the server profile is moved to the migration queue.
     *
     * @param user Org admin performing the migration.
     * @param server Server to be migrated.
     */
    public void removeOrgRelationships(User user, Server server) {

        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionException(RoleFactory.ORG_ADMIN);
        }

        // Update the server to ignore entitlement checking... This is needed to ensure
        // that things such as configuration files are moved with the system, even if
        // the system currently has provisioning entitlements removed.
        server.setIgnoreEntitlementsForMigration(Boolean.TRUE);

        // Unsubscribe from all channels to change channel entitlements
        UpdateChildChannelsCommand cmd = new UpdateChildChannelsCommand(user, server,
                new ArrayList<>());
        cmd.store();
        SystemManager.unsubscribeServerFromChannel(server, server.getBaseChannel());

        // Remove from all system groups:
        for (ManagedServerGroup group : server.getManagedGroups()) {
            List<Server> tempList = new LinkedList<>();
            tempList.add(server);
            groupManager.removeServers(group, tempList);
        }

        // Remove from entitlement groups
        for (EntitlementServerGroup oldEnt : server.getEntitledGroups()) {
            ServerFactory.removeServerFromGroup(server, oldEnt);
        }

        // Remove custom data values (aka System->CustomInfo)
        ServerFactory.removeCustomDataValues(server);

        // Remove existing config channels
        if (server.getConfigChannelCount() > 0) {
            server.setConfigChannels(Collections.emptyList(), user);
        }

        // If the server has a reactivation keys, remove them...
        // They will not be valid once the server is in the new org.
        List<Token> tokenList = TokenFactory.listByServer(server);
        for (Token token : tokenList) {
            TokenFactory.removeToken(token);
        }

        // Remove the errata and package cache
        ErrataCacheManager.deleteNeededErrataCache(server.getId());
        ErrataCacheManager.deleteNeededCache(server.getId());

        // Remove snapshots
        List<ServerSnapshot> snapshots = ServerFactory.listSnapshots(
                server.getOrg(), server, null, null);
        for (ServerSnapshot snapshot : snapshots) {
            ServerFactory.deleteSnapshot(snapshot);
        }
    }

    /**
     * Update the org admin to server relationships in the originating and destination
     * orgs.
     *
     * @param fromOrg originating org where the server currently exists
     * @param toOrg destination org where the server will be migrated to
     * @param server Server to be migrated.
     */
    public static void updateAdminRelationships(Org fromOrg, Org toOrg, Server server) {
        // TODO: In some scenarios this appears to be somewhat slow, for an org with
        // around a thousand org admins and a dozen or so servers, this can take about a
        // minute to run. Probably a much more efficient way to do this. (i.e. delete
        // from rhnUserServerPerms where server_id = blah. Add a huge number of servers to
        // the mix and it could take quite some time.
        for (User admin : fromOrg.getActiveOrgAdmins()) {
            admin.removeServer(server);
            UserFactory.save(admin);
        }

        // add the server to all org admins in the destination org
        for (User admin : toOrg.getActiveOrgAdmins()) {
            admin.addServer(server);
            UserFactory.save(admin);
        }
    }

    /**
     * Updates Entitlements for system migration
     *
     * @param server the server to migrate
     * @param toOrg the organization to migrate to
     * @param entitlements the set of server entitlements
     */
    public static void updateServerEntitlements(Org toOrg, Server server, Set<Entitlement> entitlements) {
        entitlements.forEach(ent -> {
            ServerGroup newEnt = ServerGroupFactory.lookupEntitled(ent, toOrg);
            ServerFactory.addServerToGroup(server, newEnt);
        });
    }

    /**
     * Move the server to the destination org.
     *
     * @param toOrg destination org where the server will be migrated to
     * @param server Server to be migrated.
     */
    public static void moveServerToOrg(Org toOrg, Server server) {

        // if the server has any "Locally-Managed" config files associated with it, then
        // a config channel was created for them... that channel needs to be moved to
        // the new org...
        if (server.getLocalOverrideNoCreate() != null) {
            server.getLocalOverrideNoCreate().setOrg(toOrg);
        }

        // if the server has any "Local Sandbox" config files associated with it, then
        // a config channel was created for them... that channel needs to be moved to
        // the new org...
        if (server.getSandboxOverrideNoCreate() != null) {
            server.getSandboxOverrideNoCreate().setOrg(toOrg);
        }

        // Move the server
        server.setOrg(toOrg);
    }
}
