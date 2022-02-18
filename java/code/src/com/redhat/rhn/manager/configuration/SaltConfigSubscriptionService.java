/*
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.manager.configuration;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.StateRevisionService;

import java.util.List;

/**
 * Service class for handling config channel subscriptions for Salt systems, system groups or orgs.
 */
public class SaltConfigSubscriptionService {

    private SaltConfigSubscriptionService() { }

    /**
     * Subscribe a {@link SaltConfigurable} to given config channels.
     *
     * @param entity   the entity
     * @param channels the channels
     * @param user     the user
     * @return the created state revision
     */
    public static StateRevision subscribeChannels(SaltConfigurable entity, List<ConfigChannel> channels, User user) {
        StateRevision revision = StateRevisionService.INSTANCE.cloneLatest(entity, user, true, true);
        revision.getConfigChannels().removeAll(channels);
        revision.getConfigChannels().addAll(channels);
        SaltStateGeneratorService.INSTANCE.generateConfigState(revision);
        StateFactory.save(revision);
        ConfigChannelSaltManager.getInstance().generateConfigChannelFiles(channels);
        return revision;
    }

    /**
     * Unsubscribe a {@link SaltConfigurable} from given config channels.
     *
     * @param entity   the entity
     * @param channels the channels
     * @param user     the user
     * @return the created state revision
     */
    public static StateRevision unsubscribeChannels(SaltConfigurable entity, List<ConfigChannel> channels, User user) {
        StateRevision revision = StateRevisionService.INSTANCE.cloneLatest(entity, user, true, true);
        revision.getConfigChannels().removeAll(channels);
        SaltStateGeneratorService.INSTANCE.generateConfigState(revision);
        StateFactory.save(revision);
        return revision;
    }

    /**
     * Sets subscribed config channels for a {@link SaltConfigurable} to given channels.
     *
     * @param entity   the entity
     * @param channels the channels
     * @param user     the user
     * @return the config channels
     */
    public static StateRevision setConfigChannels(SaltConfigurable entity, List<ConfigChannel> channels, User user) {
        StateRevision revision = StateRevisionService.INSTANCE.cloneLatest(entity, user, true, false);
        revision.getConfigChannels().addAll(channels);
        SaltStateGeneratorService.INSTANCE.generateConfigState(revision);
        StateFactory.save(revision);
        ConfigChannelSaltManager.getInstance().generateConfigChannelFiles(channels);
        return revision;
    }
}
