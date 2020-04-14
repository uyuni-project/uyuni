/**
 * Copyright (c) 2016 SUSE LLC
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

package com.suse.manager.webui.controllers.utils;

import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltService.KeyStatus;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.InputValidator;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.salt.netapi.calls.wheel.Key;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Code for bootstrapping salt minions using salt-ssh.
 */
public class RegularMinionBootstrapper extends AbstractMinionBootstrapper {

    private static RegularMinionBootstrapper instance;
    private static final Logger LOG = Logger.getLogger(RegularMinionBootstrapper.class);

    /**
     * Standard constructor. For testing only - to obtain instance of this class, use
     * getInstance.
     * @param systemQueryIn systemQuery to use
     */
    public RegularMinionBootstrapper(SystemQuery systemQueryIn) {
        super(systemQueryIn);
    }

    /**
     * Get instance of the RegularMinionBootstrapper
     * @param systemQuery instance to get system information.
     * @return instance of the RegularMinionBootstrapper
     */
    public static synchronized RegularMinionBootstrapper getInstance(SystemQuery systemQuery) {
        if (instance == null) {
            instance = new RegularMinionBootstrapper(systemQuery);
        }
        return instance;
    }

    @Override
    protected List<String> validateJsonInput(BootstrapHostsJson input) {
        return InputValidator.INSTANCE.validateBootstrapInput(input);
    }

    @Override
    protected List<String> getBootstrapMods() {
        return Arrays.asList(
                ApplyStatesEventMessage.CERTIFICATE,
                "bootstrap");
    }

    @Override
    protected Map<String, Object> createPillarData(User user, BootstrapParameters input,
                                                   String contactMethod) {
        Map<String, Object> pillarData = super.createPillarData(user, input, contactMethod);

        Key.Pair keyPair = systemQuery.generateKeysAndAccept(input.getHost(), false);
        if (keyPair.getPub().isPresent() && keyPair.getPriv().isPresent()) {
            pillarData.put("minion_pub",  keyPair.getPub().get());
            pillarData.put("minion_pem", keyPair.getPriv().get());
        }

        return pillarData;
    }

    @Override
    protected Optional<String> validateContactMethod(ContactMethod desiredContactMethod) {
        if (ServerFactory.findContactMethodByLabel("default").getId()
                .equals(desiredContactMethod.getId())) {
            return Optional.empty();
        }
        return Optional.of("Selected activation key cannot be used as its contact" +
                " method is not compatible with the regular salt minions.");
    }

    @Override
    protected BootstrapResult bootstrapInternal(BootstrapParameters input, User user,
                                                String defaultContactMethod) {
        String minionId = input.getHost();
        MinionPendingRegistrationService.addMinion(
                user, minionId, defaultContactMethod, Optional.empty());

        // If a key is pending for this minion, temporarily reject it
        boolean weRejectedIt = false;
        if (systemQuery.keyExists(minionId, KeyStatus.UNACCEPTED)) {
            LOG.info("Pending key exists for " + minionId + ", rejecting...");
            systemQuery.rejectKey(minionId);
            weRejectedIt = true;
        }

        BootstrapResult result = super.bootstrapInternal(input, user, defaultContactMethod);
        if (!result.isSuccess()) {
            systemQuery.deleteKey(minionId);
            MinionPendingRegistrationService.removeMinion(minionId);
        }
        else if (weRejectedIt) {
            LOG.info("Removing key that was temporarily rejected for " + minionId);
            systemQuery.deleteRejectedKey(minionId);
        }
        LOG.info("Minion bootstrap success: " + result.isSuccess());
        return result;
    }
}
