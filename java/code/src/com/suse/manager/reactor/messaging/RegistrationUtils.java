/**
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Common registration logic that can be used from multiple places
 */
public class RegistrationUtils {

    private static final Logger LOG = Logger.getLogger(RegistrationUtils.class);

    /**
     * Perform the final registration steps for the minion.
     *
     * @param minion the minion
     * @param activationKey the activation key
     * @param creator user performing the registration
     * @param enableMinionService true if salt-minion service should be enabled and running
     */
    public static void finishRegistration(MinionServer minion, Optional<ActivationKey> activationKey, Optional<User> creator,
            boolean enableMinionService) {
        String minionId = minion.getMinionId();
        // get hardware and network async
        triggerHardwareRefresh(minion);

        LOG.info("Finished minion registration: " + minionId);

        StatesAPI.generateServerPackageState(minion);

        // Asynchronously get the uptime of this minion
        MessageQueue.publish(new MinionStartEventDatabaseMessage(minionId));

        // Generate pillar data
        try {
            SaltStateGeneratorService.INSTANCE.generatePillar(minion);

            // Subscribe to config channels assigned to the activation key or initialize empty channel profile
            minion.subscribeConfigChannels(
                    activationKey.map(ActivationKey::getAllConfigChannels).orElse(Collections.emptyList()),
                    creator.orElse(null));
        }
        catch (RuntimeException e) {
            LOG.error("Error generating Salt files for minion '" + minionId +
                    "':" + e.getMessage());
        }

        // Should we apply the highstate?
        boolean applyHighstate = activationKey.isPresent() && activationKey.get().getDeployConfigs();

        // Apply initial states asynchronously
        List<String> statesToApply = new ArrayList<>();
        statesToApply.add(ApplyStatesEventMessage.CERTIFICATE);
        statesToApply.add(ApplyStatesEventMessage.CHANNELS);
        statesToApply.add(ApplyStatesEventMessage.CHANNELS_DISABLE_LOCAL_REPOS);
        statesToApply.add(ApplyStatesEventMessage.PACKAGES);
        if (enableMinionService) {
            statesToApply.add(ApplyStatesEventMessage.SALT_MINION_SERVICE);
        }
        MessageQueue.publish(new ApplyStatesEventMessage(
                minion.getId(),
                minion.getCreator() != null ? minion.getCreator().getId() : null,
                !applyHighstate, // Refresh package list if we're not going to apply the highstate afterwards
                statesToApply
        ));

        // Call final highstate to deploy config channels if required
        if (applyHighstate) {
            MessageQueue.publish(new ApplyStatesEventMessage(minion.getId(), true, Collections.emptyList()));
        }
    }

    public static void triggerHardwareRefresh(MinionServer server) {
        try {
            ActionManager.scheduleHardwareRefreshAction(server.getOrg(), server,
                    new Date());
        }
        catch (TaskomaticApiException e) {
            LOG.error("Could not schedule hardware refresh for system: " + server.getId());
            throw new RuntimeException(e);
        }
    }
}
