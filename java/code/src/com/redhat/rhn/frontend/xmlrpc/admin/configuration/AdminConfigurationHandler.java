/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.admin.configuration;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.TokenPackage;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChannelTreeNode;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.LookupServerGroupException;
import com.redhat.rhn.frontend.xmlrpc.UserLoginException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;
import com.redhat.rhn.frontend.xmlrpc.org.OrgHandler;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler;
import com.redhat.rhn.frontend.xmlrpc.user.UserHandler;
import com.redhat.rhn.frontend.xmlrpc.user.XmlRpcUserHelper;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.webui.services.iface.SaltApi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

/**
 * AdminConfigurationHandler
 * @apidoc.namespace admin.configuration
 * @apidoc.doc Provides methods to configure the #product() server.
 */
public class AdminConfigurationHandler extends BaseHandler {
    private static final Logger LOG = LogManager.getLogger(AdminConfigurationHandler.class);
    private final OrgHandler orgHandler;
    private final ServerGroupHandler serverGroupHandler;
    private final UserHandler userHandler;
    private final ActivationKeyHandler activationKeyHandler;
    private final SystemHandler systemHandler;
    private final ChannelHandler channelHandler;
    private final ChannelSoftwareHandler channelSoftwareHandler;
    private final SaltApi saltApi;

    /**
     * @param orgHandlerIn OrgHandler
     * @param serverGroupHandlerIn ServerGroupHandler
     * @param userHandlerIn UserHandler
     * @param activationKeyHandlerIn ActivationKeyHandler
     * @param systemHandlerIn SystemHandler
     * @param channelHandlerIn ChannelHandler
     * @param channelSoftwareHandlerIn ChannelSoftwareHandler
     * @param saltApiIn SaltApi
     */
    public AdminConfigurationHandler(OrgHandler orgHandlerIn,
                        ServerGroupHandler serverGroupHandlerIn,
                        UserHandler userHandlerIn,
                        ActivationKeyHandler activationKeyHandlerIn,
                        SystemHandler systemHandlerIn,
                        ChannelHandler channelHandlerIn,
                        ChannelSoftwareHandler channelSoftwareHandlerIn,
                        SaltApi saltApiIn) {
         orgHandler = orgHandlerIn;
         serverGroupHandler = serverGroupHandlerIn;
         userHandler = userHandlerIn;
         activationKeyHandler = activationKeyHandlerIn;
         systemHandler = systemHandlerIn;
         channelHandler = channelHandlerIn;
         channelSoftwareHandler = channelSoftwareHandlerIn;
         saltApi = saltApiIn;
    }



    private Long createOrUpdateOrg(User loggedInUser, String orgName, String adminLogin,
            String adminPassword, String firstName, String lastName,
            String email) {

        Org org = OrgFactory.lookupByName(orgName);
        if (org == null) {
            LOG.debug("Org {} not found", orgName);
            Long orgId = orgHandler.create(loggedInUser, orgName, adminLogin, adminPassword,
                 "Mr.", firstName, lastName, email, false).getId();
            LOG.debug("Organization {}, id: {} created, admin: {}", orgName, orgId, adminLogin);
            return orgId;
        }
        LOG.debug("Org {} found", orgName);
        return org.getId();
    }

    private User updateAdminUser(String adminLogin,
            String adminPassword, String firstName, String lastName,
            String email) {
        try {
            User user = UserManager.loginReadOnlyUser(adminLogin, adminPassword);

            if (!firstName.equals(user.getFirstNames()) ||
                !lastName.equals(user.getLastName()) || !email.equals(user.getEmail())) {
                Map<String, String> details = new HashMap<>();
                details.put("password", adminPassword);
                details.put("first_name", firstName);
                details.put("last_name", lastName);
                details.put("email", email);
                userHandler.setDetails(user, adminLogin, details);
                LOG.debug("User {} details updated: {} {}, {}", adminLogin, firstName, lastName, email);
            }
            return user;
        }
        catch (LoginException e) {
            // Convert to fault exception
            throw new UserLoginException(e.getMessage());
        }
    }

    private void createOrUpdateGroup(User loggedInUser, String name, String description,
                                     String target, String targetType) {
        try {
            ServerGroup currentGroup = serverGroupHandler.getDetails(loggedInUser, name);
            LOG.debug("Group {} already exists", currentGroup.getName());
        }
        catch (LookupServerGroupException e) {
            serverGroupHandler.create(loggedInUser, name, description);
            LOG.debug("Group {} created", name);
        }
        if (target == null || targetType == null || target.isEmpty() || targetType.isEmpty()) {
            // do not call salt to get system list
            return;
        }

        List<Long> currentSystems = serverGroupHandler.listSystemsMinimal(loggedInUser, name).stream()
                .map(system -> system.getId())
                .collect(Collectors.toList());

        Map<String, Long> minionIdMap = systemHandler.getMinionIdMap(loggedInUser);
        LOG.debug("Minion map {}", minionIdMap);
        List<String> minions = saltApi.selectMinions(target, targetType);
        LOG.debug("Selected minions {}", minions);
        List<Long> selectedMinionIds = minions.stream()
                .map(minionIdMap::get)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        List<Long> toAdd = new ArrayList<>();
        for (Long id: selectedMinionIds) {
            if (!currentSystems.contains(id)) {
                toAdd.add(id);
            }
        }
        LOG.debug("System IDs to add: {}", toAdd);
        if (!toAdd.isEmpty()) {
            serverGroupHandler.addOrRemoveSystems(loggedInUser, name, toAdd, true);
        }

        List<Long> toRemove = new ArrayList<>();
        for (Long id: currentSystems) {
            if (!selectedMinionIds.contains(id)) {
                toRemove.add(id);
            }
        }
        LOG.debug("System IDs to remove: {}", toRemove);
        if (!toRemove.isEmpty()) {
            serverGroupHandler.addOrRemoveSystems(loggedInUser, name, toRemove, false);
        }
    }

    private void createOrUpdateUser(User loggedInUser, String name, String password, String email,
                                    String firstName, String lastName, List<String> roles,
                                    List<String> systemGroups, List<String> manageableChannels,
                                    List<String> subscribableChannels) {
        try {
            Map<String, Object> existing = userHandler.getDetails(loggedInUser, name);

            LOG.debug("User {} found", name);
            if (!password.equals(existing.get("password")) || !firstName.equals(existing.get("first_name")) ||
                !lastName.equals(existing.get("last_name")) || !email.equals(existing.get("email"))) {
                Map<String, String> details = new HashMap<>();
                details.put("password", password);
                details.put("first_name", firstName);
                details.put("last_name", lastName);
                details.put("email", email);
                userHandler.setDetails(loggedInUser, name, details);
            }
        }
        catch (Exception e) {
            userHandler.create(loggedInUser, name, password, firstName, lastName, email, 0);
            LOG.debug("User {} created", name);
        }

        List<Object> existingRoles = Arrays.asList(userHandler.listRoles(loggedInUser, name));
        LOG.debug("existing roles: {}", existingRoles);
        LOG.debug("requested roles: {}", roles);
        for (Object role: existingRoles) {
            if (!roles.contains(role)) {
                LOG.debug("Removing role {}", role);
                userHandler.removeRole(loggedInUser, name, (String)role);
            }
        }
        for (String role: roles) {
            if (!existingRoles.contains(role)) {
                LOG.debug("Adding role {}", role);
                userHandler.addRole(loggedInUser, name, role);
            }
        }

        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, name);
        List<String> existingGroups = ServerGroupFactory.listAdministeredServerGroups(target).stream()
                 .map(group -> group.getName())
                 .collect(Collectors.toList());
        LOG.debug("existing groups: {}", existingGroups);
        LOG.debug("requested groups: {}", systemGroups);
        List<String> toAdd = new ArrayList<>();
        for (String group: systemGroups) {
            if (!existingGroups.contains(group)) {
                toAdd.add(group);
            }
        }
        LOG.debug("groups to add: {}", toAdd);
        if (!toAdd.isEmpty()) {
            userHandler.addAssignedSystemGroups(loggedInUser, name, toAdd, false);
        }

        List<String> toRemove = new ArrayList<>();
        for (String group: existingGroups) {
            if (!systemGroups.contains(group)) {
                toRemove.add(group);
            }
        }
        LOG.debug("groups to remove: {}", toRemove);
        if (!toRemove.isEmpty()) {
            userHandler.removeAssignedSystemGroups(loggedInUser, name, toRemove, false);
        }

        List<String> existingManageableChannels = Arrays.stream(channelHandler.listManageableChannels(target))
                 .map(c -> ((ChannelTreeNode)c).getChannelLabel())
                 .collect(Collectors.toList());

        LOG.debug("existing manageable channels: {}", existingManageableChannels);
        LOG.debug("requested manageable channels: {}", manageableChannels);

        for (String channel: existingManageableChannels) {
            if (!manageableChannels.contains(channel)) {
                LOG.debug("Removing manageable channel {}", channel);
                channelSoftwareHandler.setUserManageable(loggedInUser, channel, name, false);
            }
        }
        for (String channel: manageableChannels) {
            if (!existingManageableChannels.contains(channel)) {
                LOG.debug("Adding manageable channel {}", channel);
                channelSoftwareHandler.setUserManageable(loggedInUser, channel, name, true);
            }
        }

        List<String> existingSubscribableChannels = Arrays.stream(channelHandler.listMyChannels(target))
                 .map(c -> ((ChannelTreeNode)c).getChannelLabel())
                 .collect(Collectors.toList());

        LOG.debug("existing subscribable channels: {}", existingSubscribableChannels);
        LOG.debug("requested subscribable channels: {}", subscribableChannels);

        for (String channel: existingSubscribableChannels) {
            if (!subscribableChannels.contains(channel)) {
                LOG.debug("Removing subscribable channel {}", channel);
                channelSoftwareHandler.setUserSubscribable(loggedInUser, channel, name, false);
            }
        }
        for (String channel: subscribableChannels) {
            if (!existingSubscribableChannels.contains(channel)) {
                LOG.debug("Adding subscribable channel {}", channel);
                channelSoftwareHandler.setUserSubscribable(loggedInUser, channel, name, true);
            }
        }
    }

    private void createOrUpdateActivationKey(User loggedInUser, Long orgId, String name, String description,
                                             String baseChannel, List<String> childChannels, List<Map<String,
                                             String>> packages, List<String> serverGroups, Integer usageLimit,
                                             List<String> systemTypes, String contactMethod,
                                             Boolean configureAfterRegistration, List<String> configurationChannels) {

        String key = String.format("%d-%s", orgId, name);
        try {
            ActivationKey details = activationKeyHandler.getDetails(loggedInUser, key);
            LOG.debug("Activation Key found: {} {}", name, details.getNote());

            if (!description.equals(details.getNote()) ||
                !baseChannel.equals(details.getBaseChannel().getLabel()) ||
                usageLimit != details.getUsageLimit().intValue() ||
                !contactMethod.equals(details.getContactMethod().getName())) {

                LOG.debug("Updating Activation Key details: {}", name);
                Map<String, Object> newDetails = new HashMap<>();
                newDetails.put("description", description);
                newDetails.put("base_channel_label", baseChannel);
                newDetails.put("usage_limit", usageLimit);
                newDetails.put("contact_method", contactMethod);
                activationKeyHandler.setDetails(loggedInUser, key, newDetails);
            }
        }
        catch (Exception e) {
            LOG.debug("Activation Key not found");
            activationKeyHandler.create(loggedInUser, name, description,
                baseChannel, usageLimit, systemTypes, configureAfterRegistration);
            LOG.debug("Activation Key created");
        }

        ActivationKey details = activationKeyHandler.getDetails(loggedInUser, key);
        LOG.debug("base channel: {}", details.getBaseChannel().getLabel());
        LOG.debug("requested child channels: {}", childChannels);
        List<String> channelsToAdd = new ArrayList<>(childChannels);
        List<String> channelsToRemove = new ArrayList<>();
        for (Channel c : details.getChannels()) {
            if (!c.isBaseChannel()) {
                 if (!childChannels.contains(c.getLabel())) {
                     channelsToRemove.add(c.getLabel());
                 }
                 else {
                     channelsToAdd.remove(c.getLabel());
                 }
            }
        }
        LOG.debug("channels to remove: {}", channelsToRemove);
        LOG.debug("channels to add: {}", channelsToAdd);
        activationKeyHandler.removeChildChannels(loggedInUser, key, channelsToRemove);
        activationKeyHandler.addChildChannels(loggedInUser, key, channelsToAdd);


        LOG.debug("requested packages: {}", packages);
        List<Map<String, String>> packagesToAdd = new ArrayList<>(packages);
        List<Map<String, String>> packagesToRemove = new ArrayList<>();
        for (TokenPackage pkg : details.getPackages()) {
            Map<String, String> pkgMap = new HashMap<>();
            pkgMap.put("name", pkg.getPackageName().getName());

            if (pkg.getPackageArch() != null) {
                pkgMap.put("arch", pkg.getPackageArch().getLabel());
            }
            if (!packages.contains(pkgMap)) {
                packagesToRemove.add(pkgMap);
            }
            else {
                packagesToAdd.remove(pkgMap);
            }
        }
        LOG.debug("packages to remove: {}", packagesToRemove);
        LOG.debug("packages to add: {}", packagesToAdd);
        activationKeyHandler.removePackages(loggedInUser, key, packagesToRemove);
        activationKeyHandler.addPackages(loggedInUser, key, packagesToAdd);


        LOG.debug("requested server groups: {}", serverGroups);
        List<String> serverGroupsToAdd = new ArrayList<>(serverGroups);
        List<Integer> serverGroupsToRemove = new ArrayList<>();
        for (ServerGroup group : details.getServerGroups()) {
            if (!serverGroups.contains(group.getName())) {
                serverGroupsToRemove.add(group.getId().intValue());
            }
            else {
                serverGroupsToAdd.remove(group.getName());
            }
        }

        LOG.debug("server groups to remove: {}", serverGroupsToRemove);
        LOG.debug("server groups to add: {}", serverGroupsToAdd);
        activationKeyHandler.removeServerGroups(loggedInUser, key, serverGroupsToRemove);
        activationKeyHandler.addServerGroups(loggedInUser, key,
                 serverGroupsToAdd.stream()
                 .map(groupName -> serverGroupHandler.getDetails(loggedInUser, groupName).getId().intValue())
                 .collect(Collectors.toList()));
        LOG.debug("Configuration channels: {}", configurationChannels);
        activationKeyHandler.setConfigChannels(loggedInUser, Arrays.asList(key), configurationChannels);
    }

    /**
     * Configure the server.
     * @param loggedInUser the current user
     * @param content the Uyuni configuration formula data
     * @return 1 on success
     *
     * @apidoc.doc Configure server.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("map", "content", "the Uyuni configuration formula data")
     * @apidoc.returntype #return_int_success()
     */
    public int configure(User loggedInUser, Map<String, Object> content) {
        ensureSatAdmin(loggedInUser);

        Map<String, Object> uyuniData = (Map<String, Object>)content.get("uyuni");
        List<Map<String, Object>> orgList = (List<Map<String, Object>>)
                uyuniData.getOrDefault("orgs", Collections.emptyList());
        if (orgList == null) {
            throw new ValidationException("Invalid org list");
        }

        for (Map<String, Object> orgData : orgList) {
            String orgName = (String)orgData.get("org_id");
            String adminLogin = (String)orgData.get("org_admin_user");
            String adminPassword = (String)orgData.get("org_admin_password");
            String firstName = (String)orgData.get("first_name");
            String lastName = (String)orgData.get("last_name");
            String email = (String)orgData.get("email");
            List<Map<String, Object>> groupList = (List<Map<String, Object>>)
                    orgData.getOrDefault("system_groups", Collections.emptyList());
            List<Map<String, Object>> userList = (List<Map<String, Object>>)
                    orgData.getOrDefault("users", Collections.emptyList());
            List<Map<String, Object>> activationKeyList = (List<Map<String, Object>>)
                    orgData.getOrDefault("activation_keys", Collections.emptyList());

            if (orgName == null || adminLogin == null || adminPassword == null ||
                firstName == null || lastName == null || email == null) {
                throw new ValidationException("Invalid org data");
            }
            if (groupList == null) {
                throw new ValidationException("Invalid group list");
            }
            if (userList == null) {
                throw new ValidationException("Invalid user list");
            }
            if (activationKeyList == null) {
                throw new ValidationException("Invalid activation key list");
            }

            Long orgId = createOrUpdateOrg(loggedInUser, orgName, adminLogin, adminPassword,
                                           firstName, lastName, email);
            User orgAdmin = updateAdminUser(adminLogin, adminPassword, firstName, lastName, email);

            for (Map<String, Object> groupData : groupList) {
                String groupName = (String)groupData.get("name");
                String groupDescription = (String)groupData.get("description");
                String groupTarget = (String)groupData.get("target");
                String groupTargetType = (String)groupData.getOrDefault("target_type", "glob");
                if (groupName == null || groupDescription == null) {
                    throw new ValidationException("Invalid group data");
                }
                createOrUpdateGroup(orgAdmin, groupName, groupDescription, groupTarget, groupTargetType);
            }
            for (Map<String, Object> userData : userList) {
                String userName = (String) userData.get("name");
                String userPassword = (String) userData.get("password");
                String userEmail = (String) userData.get("email");
                String userFirstName = (String) userData.get("first_name");
                String userLastName = (String) userData.get("last_name");
                List<String> userRoles = (List<String>) userData.getOrDefault("roles", Collections.emptyList());
                List<String> userSystemGroups = (List<String>)
                        userData.getOrDefault("system_groups", Collections.emptyList());
                List<String> manageableChannels = (List<String>)
                        userData.getOrDefault("manageable_channels", Collections.emptyList());
                List<String> subscribableChannels = (List<String>)
                        userData.getOrDefault("subscribable_channels", Collections.emptyList());

                if (userName == null || userPassword == null || userEmail == null ||
                    userFirstName == null || userLastName == null ||
                    userRoles == null || userSystemGroups == null ||
                    manageableChannels == null || subscribableChannels == null) {
                    throw new ValidationException("Invalid user data");
                }

                createOrUpdateUser(orgAdmin, userName, userPassword, userEmail, userFirstName, userLastName,
                                   userRoles, userSystemGroups, manageableChannels, subscribableChannels);
            }

            for (Map<String, Object> activationKeyData : activationKeyList) {
                String activationKeyName = (String) activationKeyData.get("name");
                String activationKeyDescription = (String) activationKeyData.get("description");
                String baseChannel = (String) activationKeyData.get("base_channel");
                List<String> childChannels = (List<String>)
                        activationKeyData.getOrDefault("child_channels", Collections.emptyList());
                List<Map<String, String>> packages = (List<Map<String, String>>)
                        activationKeyData.getOrDefault("packages", Collections.emptyList());
                List<String> serverGroups = (List<String>)
                        activationKeyData.getOrDefault("server_groups", Collections.emptyList());
                Integer usageLimit = (Integer) activationKeyData.get("usage_limit");
                List<Map<String, String>> systemTypes = (List<Map<String, String>>)
                        activationKeyData.getOrDefault("system_types", Collections.emptyList());
                List<String> systemTypesList = systemTypes.stream()
                        .map(entry -> entry.get("type"))
                        .collect(Collectors.toList());
                String contactMethod = (String) activationKeyData.get("contact_method");
                Boolean configureAfterRegistration = (Boolean) activationKeyData.get("configure_after_registration");
                List<String> configurationChannels = (List<String>)
                        activationKeyData.getOrDefault("configuration_channels", Collections.emptyList());

                if (activationKeyName == null || activationKeyDescription == null || baseChannel == null ||
                    childChannels == null || packages == null || serverGroups == null || usageLimit == null ||
                    systemTypes == null || systemTypesList == null || contactMethod == null ||
                    configureAfterRegistration == null || configurationChannels == null) {
                    throw new ValidationException("Invalid activation key data");
                }

                createOrUpdateActivationKey(orgAdmin, orgId, activationKeyName, activationKeyDescription,
                                            baseChannel, childChannels, packages, serverGroups, usageLimit,
                                            systemTypesList, contactMethod, configureAfterRegistration,
                                            configurationChannels);
            }

        }
        return 1;
    }
}
