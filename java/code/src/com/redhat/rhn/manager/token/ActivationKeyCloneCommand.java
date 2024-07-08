/*
 * Copyright (c) 2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.token;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelListProcessor;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.TokenChannelAppStream;
import com.redhat.rhn.domain.token.TokenPackage;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyAlreadyExistsException;
import com.redhat.rhn.frontend.xmlrpc.activationkey.XmlRpcActivationKeysHelper;
import com.redhat.rhn.frontend.xmlrpc.configchannel.XmlRpcConfigChannelHelper;

import org.hibernate.NonUniqueObjectException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ActivationKeyCloneCommand
 */
public class ActivationKeyCloneCommand {

    private ActivationKey cak;

    /**
     * Construct a ActivationKeyCloneCommand
     * @param userIn user doing the cloning
     * @param key Activation key to be cloned
     * @param cloneDescription The description of the cloned key.
     */
    public ActivationKeyCloneCommand(User userIn, String key,
            String cloneDescription) {

        ActivationKeyManager akm = ActivationKeyManager.getInstance();
        ActivationKey ak = lookupKey(key, userIn);

        try {
            cak = akm.createNewActivationKey(userIn, "", cloneDescription,
                    ak.getUsageLimit(), ak.getBaseChannel(), false);
            // only one akey can be Universal default
        }
        catch (ValidatorException ve) { // the user is not allowed to create AK
            throw FaultException.create(1091, "activationkey", ve.getResult());
        }
        catch (NonUniqueObjectException e) {
            throw new ActivationKeyAlreadyExistsException();
        }

        // enable/disable
        cak.setDisabled(ak.isDisabled());

        // Entitlements
        Set<ServerGroupType> cloneEnt = new HashSet<>();
        cloneEnt.addAll(ak.getEntitlements());
        cak.setEntitlements(cloneEnt);

        // child channels
        Set<Channel> channels = new HashSet<>();
        channels.addAll(ak.getChannels());
        cak.setChannels(channels);

        // Configuration File Deployment
        cak.setDeployConfigs(ak.getDeployConfigs());

        // packages
        for (TokenPackage temp : ak.getPackages()) {
            cak.addPackage(temp.getPackageName(), temp.getPackageArch());
        }

        // Configuration channels
        List<String> lcloneConfigChannels = new ArrayList<>();
        for (ConfigChannel configChannelIn : ak.getConfigChannelsFor(userIn)) {
            lcloneConfigChannels.add(configChannelIn.getLabel());
        }

        List<String> lcak = new ArrayList<>();
        lcak.add(cak.getKey());
        setConfigChannels(userIn, lcak, lcloneConfigChannels);

        // Groups
        Set<ServerGroup> cloneServerGroups = new HashSet<>();
        cloneServerGroups.addAll(ak.getServerGroups());
        cak.setServerGroups(cloneServerGroups);

        // Contact method
        cak.setContactMethod(ak.getContactMethod());

        // AppStreams
        Map<Channel, List<TokenChannelAppStream>> tcasMap = akm.listTokenChannelAppStreams(ak).stream().collect(
            Collectors.groupingBy(TokenChannelAppStream::getChannel)
        );
        tcasMap.forEach((channel, streams) -> {
            var toInclude = streams.stream().map(TokenChannelAppStream::getAppStream).collect(Collectors.toList());
            akm.saveChannelAppStreams(
                cak, channel, toInclude, Collections.emptyList()
            );
        });
    }

    /**
     * lookup a ActivationKey object from key, and throws a FaultException
     * @param user user doing the searchg
     * @param key The key value of the ActivationKey
     * @return Returns the ActivationKey Object corresponding to the key value.
     */
    private ActivationKey lookupKey(String key, User user) {
        return XmlRpcActivationKeysHelper.getInstance().lookupKey(user, key);
    }

    /**
     * replaces the existing set of config channels for a given activation key.
     * Note: it ranks these channels according to the array order of
     * configChannelIds method parameter
     * @param loggedInUser The current user
     * @param keys a lsit of activation keys.
     * @param configChannelLabels sets channels labels
     * @return 1 on success 0 on failure
     */
    public int setConfigChannels(User loggedInUser, List<String> keys,
            List<String> configChannelLabels) {
        XmlRpcActivationKeysHelper helper = XmlRpcActivationKeysHelper
                .getInstance();
        List<ActivationKey> activationKeys = helper.lookupKeys(loggedInUser,
                keys);
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper
                .getInstance();
        List channels = configHelper.lookupGlobals(loggedInUser,
                configChannelLabels);
        ConfigChannelListProcessor proc = new ConfigChannelListProcessor();
        for (ActivationKey activationKey : activationKeys) {
            proc.replace(activationKey.getConfigChannelsFor(loggedInUser),
                    channels);
        }
        return 1;
    }

    /**
     * Get the key value of the ActivationKey
     * @return The key value of the ActivationKey
     */
    public String getclonedkey() {
        return cak.getKey();
    }

    /**
     * Get the token id value of the ActivationKey
     * @return The token id of the ActivationKey
     */
    public Long getId() {
            return cak.getId();
    }

}
