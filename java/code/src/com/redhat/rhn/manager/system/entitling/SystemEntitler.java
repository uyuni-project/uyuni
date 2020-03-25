/**
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.manager.system.entitling;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.common.validator.ValidatorWarning;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.channel.MultipleChannelsWithPackageException;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.iface.SystemQuery;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Class for adding entitlements to servers
 */
public class SystemEntitler {

    private static final Logger LOG = Logger.getLogger(SystemEntitler.class);

    private SystemQuery systemQuery;
    private VirtManager virtManager;

    /**
     * @param systemQueryIn instance for gathering data from a system.
     * @param virtManagerIn instance for managing virtual machines.
     */
    public SystemEntitler(SystemQuery systemQueryIn, VirtManager virtManagerIn) {
        this.systemQuery = systemQueryIn;
        this.virtManager = virtManagerIn;
    }

    /**
     * Checks whether or not a given server can be entitled with a specific entitlement
     * @param server The server in question
     * @param ent The entitlement to test
     * @return Returns true or false depending on whether or not the server can be
     * entitled to the passed in entitlement.
     */
    public boolean canEntitleServer(Server server, Entitlement ent) {
        return findServerGroupToEntitleServer(server, ent).isPresent();
    }

    /**
     * Entitles the given server to the given Entitlement.
     * @param server Server to be entitled.
     * @param ent Level of Entitlement.
     * @return ValidatorResult of errors and warnings.
     */
    public ValidatorResult addEntitlementToServer(Server server, Entitlement ent) {
        LOG.debug("Entitling: " + ent.getLabel());
        ValidatorResult result = new ValidatorResult();

        if (server.hasEntitlement(ent)) {
            LOG.debug("server already entitled.");
            result.addError(new ValidatorError("system.entitle.alreadyentitled",
                    ent.getHumanReadableLabel()));
            return result;
        }

        boolean wasVirtEntitled = server.hasEntitlement(EntitlementManager.VIRTUALIZATION);
        if (EntitlementManager.VIRTUALIZATION.equals(ent)) {
            if (server.isVirtualGuest()) {
                result.addError(new ValidatorError("system.entitle.guestcantvirt"));
                return result;
            }

            LOG.debug("setting up system for virt.");
            ValidatorResult virtSetupResults = setupSystemForVirtualization(server.getOrg(), server.getId());
            result.append(virtSetupResults);
            if (virtSetupResults.getErrors().size() > 0) {
                LOG.debug("error trying to setup virt ent: " + virtSetupResults.getMessage());
                return result;
            }
        }
        else if (EntitlementManager.OSIMAGE_BUILD_HOST.equals(ent)) {
            systemQuery.generateSSHKey(SaltSSHService.SSH_KEY_PATH);
        }

        entitleServer(server, ent);

        server.asMinionServer().ifPresent(minion -> {
            ServerGroupManager.getInstance().updatePillarAfterGroupUpdateForServers(Arrays.asList(minion));

            if (wasVirtEntitled && !EntitlementManager.VIRTUALIZATION.equals(ent) ||
                    !wasVirtEntitled && EntitlementManager.VIRTUALIZATION.equals(ent)) {
                this.updateLibvirtEngine(minion);
            }

            if (EntitlementManager.MONITORING.equals(ent)) {
                try {
                    // Assign the monitoring formula to the system
                    // unless the system belongs to a group having monitoring already enabled
                    if (!FormulaFactory.isMemberOfGroupHavingMonitoring(server)) {
                        List<String> formulas = FormulaFactory.getFormulasByMinionId(minion.getMinionId());
                        if (!formulas.contains(FormulaFactory.PROMETHEUS_EXPORTERS)) {
                            formulas.add(FormulaFactory.PROMETHEUS_EXPORTERS);
                            FormulaFactory.saveServerFormulas(minion.getMinionId(), formulas);
                        }
                    }
                }
                catch (UnsupportedOperationException | IOException e) {
                    LOG.error("Error assigning formula: " + e.getMessage(), e);
                    result.addError(new ValidatorError("system.entitle.formula_error"));
                }
            }
        });

        LOG.debug("done.  returning null");
        return result;
    }

    private void entitleServer(Server server, Entitlement ent) {
        Optional<ServerGroup> serverGroup = findServerGroupToEntitleServer(server, ent);

        if (serverGroup.isPresent()) {
            ServerFactory.addServerHistoryWithEntitlementEvent(server, ent, "added system entitlement ");
            ServerFactory.addServerToGroup(server, serverGroup.get());
        }
        else {
            LOG.error("Cannot add entitlement: " + ent.getLabel() + " to system: " + server.getId());
        }
    }

    private Optional<ServerGroup> findServerGroupToEntitleServer(Server server, Entitlement ent) {
        Set<Entitlement> entitlements = server.getEntitlements();

        if (entitlements.isEmpty()) {
            return findServerGroupToEntitleAnUnentitledServer(server.getId(), ent);
        }
        return findServerGroupToEntitleAnEntitledServer(server, ent);
    }

    private Optional<ServerGroup> findServerGroupToEntitleAnUnentitledServer(Long serverId, Entitlement ent) {
        if (ent.isBase()) {
            Optional<ServerGroup> serverGroup = ServerGroupFactory
                    .findCompatibleServerGroupForBaseEntitlement(serverId, ent);
            if (!serverGroup.isPresent()) {
                LOG.warn("Could not find a compatible ServerGroup for base entitlement: " + ent.getLabel() +
                        ", and server: " + serverId);
            }
            return serverGroup;
        }
        return Optional.empty();
    }

    private Optional<ServerGroup> findServerGroupToEntitleAnEntitledServer(Server server, Entitlement ent) {
        if (ent.isBase()) {
            LOG.warn("Cannot set a base entitlement: " + ent.getLabel() + " as an addon entitlement for server: " +
                    server.getId());
            return Optional.empty();
        }

        Optional<Long> baseEntitlementId = server.getBaseEntitlementId();

        if (baseEntitlementId.isEmpty()) {
            LOG.warn("Cannot set a entitlement: " + ent.getLabel() + " as an addon entitlement for server: " +
                    server.getId() + ". The server has no base entitlement yet.");
            return Optional.empty();
        }

        Optional<ServerGroup> serverGroup = ServerGroupFactory
                .findCompatibleServerGroupForAddonEntitlement(server.getId(), ent, baseEntitlementId.get());
        if (!serverGroup.isPresent()) {
            LOG.warn("Cannot set a entitlement: " + ent.getLabel() + " as an addon entitlement for server: " +
                    server.getId() + ". The server base entitlement is not compatible.");
        }
        return serverGroup;
    }

    private void updateLibvirtEngine(MinionServer minion) {
        virtManager.updateLibvirtEngine(minion);
    }

    // Need to do some extra logic here
    // 1) Subscribe system to rhel-i386-server-vt-5 channel
    // 2) Subscribe system to rhn-tools-rhel-i386-server-5
    // 3) Schedule package install of rhn-virtualization-host
    // Return a map with errors and warnings:
    //      warnings -> list of ValidationWarnings
    //      errors -> list of ValidationErrors
    private ValidatorResult setupSystemForVirtualization(Org orgIn, Long sid) {

        Server server = ServerFactory.lookupById(sid);
        User user = UserFactory.findRandomOrgAdmin(orgIn);
        ValidatorResult result = new ValidatorResult();

        // If this is a Satellite
        if (!ConfigDefaults.get().isSpacewalk()) {
            // just install libvirt for RHEL6 base channel
            Channel base = server.getBaseChannel();

            if (base != null && base.isCloned()) {
                base = base.getOriginal();
            }

            if ((base != null) &&
                    (!base.isRhelChannel() || base.isReleaseXChannel(5))) {
                // Do not automatically subscribe to virt channels (bnc#768856)
                // subscribeToVirtChannel(server, user, result);
            }
        }

        if (server.hasEntitlement(EntitlementManager.MANAGEMENT)) {
            // Before we start looking to subscribe to a 'tools' channel for
            // rhn-virtualization-host, check if the server already has a package by this
            // name installed and leave it be if so.
            InstalledPackage rhnVirtHost = PackageFactory.lookupByNameAndServer(
                    ChannelManager.RHN_VIRT_HOST_PACKAGE_NAME, server);
            if (rhnVirtHost != null) {
                // System already has the package, we can stop here.
                LOG.debug("System already has " +
                        ChannelManager.RHN_VIRT_HOST_PACKAGE_NAME + " installed.");
                return result;
            }
            try {
                scheduleVirtualizationHostPackageInstall(server, user, result);
            }
            catch (TaskomaticApiException e) {
                result.addError(new ValidatorError("taskscheduler.down"));
            }
        }

        return result;
    }

    /**
     * Schedule installation of rhn-virtualization-host package.
     *
     * Implies that we locate a child channel with this package and automatically
     * subscribe the system to it if possible. If multiple child channels have the package
     * and the server is not already subscribed to one, we report the discrepancy and
     * instruct the user to deal with this manually.
     *
     * @param server Server to schedule install for.
     * @param user User performing the operation.
     * @param result Validation result we'll be returning for the UI to render.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    private void scheduleVirtualizationHostPackageInstall(Server server,
            User user, ValidatorResult result) throws TaskomaticApiException {
        // Now subscribe to a child channel with rhn-virtualization-host (RHN Tools in the
        // case of Satellite) and schedule it for installation, or warn if we cannot find
        // a child with the package:
        Channel toolsChannel = null;
        try {
            toolsChannel = ChannelManager.subscribeToChildChannelWithPackageName(
                    user, server, ChannelManager.RHN_VIRT_HOST_PACKAGE_NAME);

            // If this is a Satellite and no RHN Tools channel is available
            // report the error
            if (!ConfigDefaults.get().isSpacewalk() && toolsChannel == null) {
                LOG.warn("no tools channel found");
                result.addError(new ValidatorError("system.entitle.notoolschannel"));
            }
            // If Spacewalk and no channel has the rhn-virtualization-host package,
            // warn but allow the operation to proceed.
            else if (toolsChannel == null) {
                result.addWarning(new ValidatorWarning("system.entitle.novirtpackage",
                        ChannelManager.RHN_VIRT_HOST_PACKAGE_NAME));
            }
            else {
                List<Map<String, Object>> packageResults =
                        ChannelManager.listLatestPackagesEqual(
                        toolsChannel.getId(), ChannelManager.RHN_VIRT_HOST_PACKAGE_NAME);
                if (packageResults.size() > 0) {
                    Map<String, Object> row = packageResults.get(0);
                    Long nameId = (Long) row.get("name_id");
                    Long evrId = (Long) row.get("evr_id");
                    Long archId = (Long) row.get("package_arch_id");
                    ActionManager.schedulePackageInstall(
                            user, server, nameId, evrId, archId);
                }
                else {
                    result.addError(new ValidatorError("system.entitle.novirtpackage",
                            ChannelManager.RHN_VIRT_HOST_PACKAGE_NAME));
                }
            }
        }
        catch (MultipleChannelsWithPackageException e) {
            LOG.warn("Found multiple child channels with package: " +
                    ChannelManager.RHN_VIRT_HOST_PACKAGE_NAME);
            result.addWarning(new ValidatorWarning(
                    "system.entitle.multiplechannelswithpackagepleaseinstall",
                    ChannelManager.RHN_VIRT_HOST_PACKAGE_NAME));
        }
    }
}
