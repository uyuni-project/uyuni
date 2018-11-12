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
import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.utils.Opt;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collections;
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
     * @param defaultContactMethod contact method to use in case the activation
     *                             key does not specify any other
     * @return map containing success flag and error messages.
     */
    public BootstrapResult bootstrap(BootstrapHostsJson input, User user,
                                     String defaultContactMethod) {
        List<String> errMessages = validateBootstrap(input);
        if (!errMessages.isEmpty()) {
            return new BootstrapResult(false, Optional.empty(),
                    errMessages.toArray(new String[errMessages.size()]));
        }

        return bootstrapInternal(createBootstrapParams(input), user, defaultContactMethod);
    }

    /**
     * Create bootstrap parameters based on the json input.
     * This is implementation specific.
     * @param input json input
     * @return bootstrap parameters
     */
    protected BootstrapParameters createBootstrapParams(BootstrapHostsJson input) {
        return new BootstrapParameters(input);
    }

    /**
     * Check if salt is able to store ssh-key.
     * The permissions get lost when a user manually changes the file.
     * @return boolean about salt having correct file permissions
     */
    private boolean hasCorrectSSHFilePermissions() {
        File dotSSHDir = new File("/var/lib/salt/.ssh");
        //Directory gets created the first time a bootstrap happens - its absence is fine.
        if (!dotSSHDir.exists()) {
            return true;
        }
        File knownHostsFile = new File("/var/lib/salt/.ssh/known_hosts");
        return knownHostsFile.exists() && knownHostsFile.canRead() && knownHostsFile.canWrite();
    }

    /**
     * Common code for performing the bootstrap.
     * Override to adjust the behavior.
     *
     * @param user user performing the bootstrap
     * @return object that indicates success/failure of the bootstrap and bears with
     * messages from the bootstrap procedure.
     */
    protected BootstrapResult bootstrapInternal(BootstrapParameters params, User user,
                                                String defaultContactMethod) {
        List<String> bootstrapMods = getBootstrapMods();
        String contactMethod = ContactMethodUtil.getContactMethod(
                params.getFirstActivationKey(), defaultContactMethod);

        if (!hasCorrectSSHFilePermissions()) {
            String responseMessage = "Cannot read/write '/var/lib/salt/.ssh/known_hosts'. Please check permissions.";
            LOG.error("Error during bootstrap: " + responseMessage);
            return new BootstrapResult(false, Optional.of(contactMethod), responseMessage);
        }

        try {
            Map<String, Object> pillarData = createPillarData(user, params, contactMethod);
            return saltService.bootstrapMinion(params, bootstrapMods, pillarData)
                    .fold(error -> {
                        String responseMessage = SaltUtils.decodeSaltErr(error);
                        LOG.error("Error during bootstrap: " + responseMessage);
                        return new BootstrapResult(false, Optional.of(contactMethod),
                                responseMessage);
                    },
                    result -> {
                        // We have results, check if result = true
                        // for all the single states
                        Optional<String> errMessage =
                                getApplyStateErrorMessage(params.getHost(), result);
                        // Clean up the generated key pair in case of failure
                        boolean success = !errMessage.isPresent() &&
                                result.getRetcode() == 0;
                        errMessage.ifPresent(msg -> {
                            LOG.error("States failed during bootstrap: " + msg);
                        });
                        return new BootstrapResult(success, Optional.of(contactMethod),
                                errMessage.orElse(null));
                    }
            );
        }
        catch (SaltException e) {
            LOG.error("Exception during bootstrap: " + e.getMessage(), e);
            return new BootstrapResult(false, Optional.empty(),
                    "Error during applying the bootstrap" +
                    " state, message: " + e.getMessage());
        }
        catch (Exception e) {
            return new BootstrapResult(false, Optional.empty(),
                    e.getMessage());
        }
    }

    /**
     * Implementation-specific Validation of the json input.
     * @param input the json input
     * @return the result of the validation
     */
    protected abstract List<String> validateJsonInput(BootstrapHostsJson input);

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
    protected Map<String, Object> createPillarData(User user, BootstrapParameters input,
                                                   String contactMethod) {
        Map<String, Object> pillarData = new HashMap<>();
        String mgrServer = input.getProxyId()
                .map(proxyId -> ServerFactory.lookupById(proxyId))
                .map(proxy -> proxy.getHostname())
                .orElse(ConfigDefaults.get().getCobblerHost());

        pillarData.put("mgr_server", mgrServer);
        pillarData.put("minion_id", input.getHost());
        pillarData.put("contact_method", contactMethod);
        pillarData.put("mgr_sudo_user", SaltSSHService.getSSHUser());
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

    private List<String> validateBootstrap(BootstrapHostsJson input) {
        List<String> errors = validateJsonInput(input);
        if (!errors.isEmpty()) {
            return errors;
        }

        Optional<String> activationKeyErrorMessage = input.getFirstActivationKey()
                .flatMap(this::validateActivationKey);
        if (activationKeyErrorMessage.isPresent()) {
            return Collections.singletonList(activationKeyErrorMessage.get());
        }

        if (saltService.keyExists(input.getHost())) {
            return Collections.singletonList("A salt key for this" +
                    " host (" + input.getHost() +
                    ") seems to already exist, please check!");
        }

        return MinionServerFactory.findByMinionId(input.getHost())
                .map(m -> Collections.singletonList("A system '" +
                        m.getName() + "' with minion id " + input.getHost() +
                        " seems to already exist,  please check!"))
                .orElseGet(Collections::emptyList);
    }

    /**
     * Checks whether the contact method of the desired activation key is compatible with
     * the selected method of managing the system (either a regular minion or
     * a salt-ssh system).
     *
     * @param activationKeyLabel desired activation key label
     * @return Optional with error message or empty if validation succeeds
     */
    private Optional<String> validateActivationKey(String activationKeyLabel) {
        ActivationKey activationKey = ActivationKeyFactory.lookupByKey(activationKeyLabel);

        if (activationKey == null) {
            return Optional.of("Selected activation key not found.");
        }

        return validateContactMethod(activationKey.getContactMethod());
    }

    protected abstract Optional<String> validateContactMethod(
            ContactMethod desiredContactMethod);



    /**
     * Representation of the status of bootstrap and possibly error messages.
     */
    public static class BootstrapResult {

        private final boolean success;
        private final String[] messages;
        private final Optional<String> contactMethod;

        /**
         * @param successIn success
         * @param contactMethodIn contact method
         * @param messagesIn messages
         */
        public BootstrapResult(boolean successIn, Optional<String> contactMethodIn,
                               String ... messagesIn) {
            this.success = successIn;
            this.messages = messagesIn;
            this.contactMethod = contactMethodIn;
        }

        /**
         * @return success
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * @return messages
         */
        public String[] getMessages() {
            return messages;
        }

        /**
         * @return contactMethod
         */
        public Optional<String> getContactMethod() {
            return contactMethod;
        }

        /**
         * @return bootstrap result converted to a map
         */
        public Map<String, Object> asMap() {
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("success", success);
            ret.put("messages", messages);
            return ret;
        }
    }
}
