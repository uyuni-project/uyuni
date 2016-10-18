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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.utils.Opt;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.of;

/**
 * Base for bootstrapping systems using salt-ssh.
 */
public abstract class AbstractMinionBootstrapper {

    protected final SaltService saltService;

    private static final Logger LOG = Logger.getLogger(AbstractMinionBootstrapper.class);

    /**
     * Constructor
     * @param saltServiceIn salt service
     */
    protected AbstractMinionBootstrapper(SaltService saltServiceIn) {
        this.saltService = saltServiceIn;
    }

    /**
     * Bootstrap a regular salt minion system (as in master-minion).
     * @param input data about the bootstrapped system
     * @param user user performing the procedure
     * @return map containing success flag and error messages.
     */
    public Map<String, Object> bootstrap(JSONBootstrapHosts input, User user) {
        BootstrapResult validation = validateBootstrap(input);
        if (!validation.isSuccess()) {
            return validation.asMap();
        }

        return bootstrapInternal(createBootstrapParams(input), user).asMap();
    }

    /**
     * Create bootstrap parameters based on the json input.
     * This is implementation specific.
     * @param input json input
     * @return bootstrap parameters
     */
    protected BootstrapParameters createBootstrapParams(JSONBootstrapHosts input) {
        return new BootstrapParameters(input);
    }

    /**
     * Common code for performing the bootstrap.
     * Override to adjust the behavior.
     *
     * @param user user performing the bootstrap
     * @return object that indicates success/failure of the bootstrap and bears with
     * messages from the bootstrap procedure.
     */
    protected BootstrapResult bootstrapInternal(BootstrapParameters params, User user) {
        List<String> bootstrapMods = getBootstrapMods();
        Map<String, Object> pillarData = createPillarData(user, params);
        try {
            return saltService.bootstrapMinion(params, bootstrapMods, pillarData)
                    .fold(error -> {
                        LOG.error("Error during bootstrap: " + error.toString());
                        return new BootstrapResult(false, error.toString());
                    },
                    result -> {
                        // We have results, check if result = true
                        // for all the single states
                        Optional<String> errMessage =
                                getApplyStateErrorMessage(params.getHost(), result);
                        // Clean up the generated key pair in case of failure
                        boolean success = !errMessage.isPresent() &&
                                result.getRetcode() == 0;
                        return new BootstrapResult(success, errMessage.orElse(null));
                    }
            );
        }
        catch (SaltException e) {
            return new BootstrapResult(false, "Error during applying the bootstrap" +
                    " state, message: " + e.getMessage());
        }
    }

    /**
     * Implementation-specific Validation of the json input.
     * @param input the json input
     * @return the result of the validation
     */
    protected abstract List<String> validateJsonInput(JSONBootstrapHosts input);

    /**
     * Implementation-specific salt state modules that should be applied during bootstrap.
     * @return the bootstrap salt state modules
     */
    protected abstract List<String> getBootstrapMods();

    /**
     * Return the pillar data that should be used during bootstrap.
     * Overriden in the specific implementations.
     * @param user user performing the operation (will be used to find the activation keys)
     * @param input the bootstrap parameters
     * @return pillar data
     */
    protected Map<String, Object> createPillarData(User user, BootstrapParameters input) {
        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("master", ConfigDefaults.get().getCobblerHost());
        pillarData.put("minion_id", input.getHost());
        ActivationKeyManager.getInstance().findAll(user)
                .stream()
                .filter(ak -> input.getActivationKeys().contains(ak.getKey()))
                .findFirst()
                .ifPresent(ak -> pillarData.put("activation_key", ak.getKey()));

        return pillarData;
    }

    /**
     * Returns optional string saying with a state apply error or an empty optional when no
     * error.
     */
    private static Optional<String> getApplyStateErrorMessage(String host,
            SSHResult<Map<String, State.ApplyResult>> res) {
        return Opt.fold(
                res.getReturn(),
                () ->  of(extractErrorMessage(host, res)),
                retVal -> retVal.entrySet().stream()
                        .filter(e -> !e.getValue().isResult())
                        .map(failed -> failed.getKey() +
                                "(retcode=" + res.getRetcode() + "): " +
                                failed.getValue().getComment())
                        .reduce((e1, e2) -> e1 + "\n" + e2)
        );
    }

    private static String extractErrorMessage(String host,
            SSHResult<Map<String, State.ApplyResult>> r) {
        return r.getStdout()
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> r.getStderr()
                        .filter(s -> !s.isEmpty())
                        .orElseGet(() -> "No result for " + host));
    }

    private BootstrapResult validateBootstrap(JSONBootstrapHosts input) {
        List<String> errors = validateJsonInput(input);
        if (!errors.isEmpty()) {
            return new BootstrapResult(false, errors.toArray(new String[errors.size()]));
        }

        if (saltService.keyExists(input.getHost())) {
            return new BootstrapResult(false, "A salt key for this" +
                    " host (" + input.getHost() +
                    ") seems to already exist, please check!");
        }

        return MinionServerFactory.findByMinionId(input.getHost())
                .map(m -> new BootstrapResult(false, "A system '" +
                        m.getName() + "' with minion id " + input.getHost() +
                        " seems to already exist,  please check!"))
                .orElseGet(
                        () -> new BootstrapResult(true)
                );
    }

    /**
     * Internal representation of the status of bootstrap and possibly error messages.
     */
    protected static class BootstrapResult {

        private final boolean success;
        private final String[] messages;

        public BootstrapResult(boolean successIn, String ... messagesIn) {
            this.success = successIn;
            this.messages = messagesIn;
        }

        public boolean isSuccess() {
            return success;
        }

        public Map<String, Object> asMap() {
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("success", success);
            ret.put("messages", messages);
            return ret;
        }
    }
}
