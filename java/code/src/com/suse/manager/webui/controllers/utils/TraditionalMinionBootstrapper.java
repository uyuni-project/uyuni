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

import com.redhat.rhn.domain.user.User;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.InputValidator;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Code for bootstrapping salt minions using salt-ssh.
 */
public class TraditionalMinionBootstrapper extends AbstractMinionBootstrapper {

    private static TraditionalMinionBootstrapper instance;
    private static final Logger LOG = Logger.getLogger(TraditionalMinionBootstrapper.class);

    /**
     * Standard constructor. For testing only - to obtain instance of this class, use
     * getInstance.
     * @param saltService salt service to use
     */
    public TraditionalMinionBootstrapper(SaltService saltService) {
        super(saltService);
    }

    /**
     * Get instance of the TraditionalMinionBootstrapper
     * @return instance of the TraditionalMinionBootstrapper
     */
    public static synchronized TraditionalMinionBootstrapper getInstance() {
        if (instance == null) {
            instance = new TraditionalMinionBootstrapper(SaltService.INSTANCE);
        }
        return instance;
    }

    @Override
    protected List<String> validateJsonInput(JSONBootstrapHosts input) {
        return InputValidator.INSTANCE.validateBootstrapInput(input);
    }

    @Override
    protected List<String> getBootstrapMods() {
        return Arrays.asList(
                ApplyStatesEventMessage.CERTIFICATE,
                "bootstrap");
    }

    @Override
    protected Map<String, Object> createPillarData(User user, BootstrapParameters input) {
        Map<String, Object> pillarData = super.createPillarData(user, input);

        com.suse.manager.webui.utils.salt.Key.Pair keyPair =
                saltService.generateKeysAndAccept(input.getHost(), false);
        if (keyPair.getPub().isPresent() && keyPair.getPriv().isPresent()) {
            pillarData.put("minion_pub",  keyPair.getPub().get());
            pillarData.put("minion_pem", keyPair.getPriv().get());
        }

        return pillarData;
    }

    @Override
    protected BootstrapResult bootstrapInternal(BootstrapParameters input, User user) {
        BootstrapResult result = super.bootstrapInternal(input, user);
        if (!result.isSuccess()) {
            saltService.deleteKey(input.getHost());
        }
        LOG.info("Minion bootstrap success: " + result.isSuccess());
        return result;
    }
}
