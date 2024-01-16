/*
 * Copyright (c) 2016--2022 SUSE LLC
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
package com.suse.manager.webui.controllers.bootstrap;

import static com.suse.manager.webui.services.SaltConstants.SALT_SSH_DIR_PATH;
import static java.util.Optional.of;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.manager.token.ActivationKeyManager;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.controllers.utils.CommandExecutionException;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService.KeyStatus;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.utils.Opt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Base for bootstrapping systems using salt-ssh.
 */
public abstract class AbstractMinionBootstrapper {

    protected final SaltApi saltApi;
    protected final SystemQuery systemQuery;
    protected final CloudPaygManager paygManager;

    private static final int KEY_LENGTH_LIMIT = 1_000_000;

    private static final Logger LOG = LogManager.getLogger(AbstractMinionBootstrapper.class);
    private static final LocalizationService LOC = LocalizationService.getInstance();

    /**
     * Constructor
     * @param systemQueryIn salt service
     * @param saltApiIn salt service
     * @param paygMgrIn cloudPaygManager
     */
    protected AbstractMinionBootstrapper(SystemQuery systemQueryIn, SaltApi saltApiIn, CloudPaygManager paygMgrIn) {
        this.saltApi = saltApiIn;
        this.systemQuery = systemQueryIn;
        this.paygManager = paygMgrIn;
    }

    /**
     * Bootstrap a regular salt minion system (as in master-minion).
     * @param params data about the bootstrapped system
     * @param user user performing the procedure
     * @param defaultContactMethod contact method to use in case the activation
     *                             key does not specify any other
     * @return map containing success flag and error messages.
     */
    public BootstrapResult bootstrap(BootstrapParameters params, User user, String defaultContactMethod) {
        List<String> errors = validateBootstrap(params);
        if (!errors.isEmpty()) {
            return new BootstrapResult(false, errors.stream().map(BootstrapError::new).collect(Collectors.toList()));
        }

        return bootstrapInternal(params, user, defaultContactMethod);
    }

    /**
     * Create bootstrap parameters based on the json input.
     * This is implementation specific.
     * @param input json input
     * @return bootstrap parameters
     */
    public BootstrapParameters createBootstrapParams(BootstrapHostsJson input) {
        return BootstrapParameters.createFromJson(input);
    }

    /**
     * Check if salt is able to store ssh-key.
     * The ownership gets lost when a user manually changes the file.
     *
     * @return boolean about salt having correct file ownership
     * @throws IOException if running the ownership check command fails on IO
     * @throws CommandExecutionException if the ownership check command returns failure
     */
    private boolean hasCorrectSSHFileOwnership() throws IOException, CommandExecutionException {
        File dotSSHDir = new File(SALT_SSH_DIR_PATH);
        //Directory gets created the first time a bootstrap happens - its absence is fine.
        if (!dotSSHDir.exists()) {
            return true;
        }

        File knownHostsFile = new File(SALT_SSH_DIR_PATH + "/known_hosts");
        String cmd = "sudo /usr/bin/ls -la " + knownHostsFile.getPath();
        Process prc = Runtime.getRuntime().exec(cmd);

        try {
            if (prc.waitFor() != 0) {
                String error = new String(prc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                if (error.contains("No such file or directory")) {
                    return true;
                }
                throw new CommandExecutionException("Error running command: " + cmd, prc);
            }
        }
        catch (InterruptedException e) {
            LOG.warn(e);
        }

        InputStream inStream = prc.getInputStream();
        StringBuilder sb = new StringBuilder();
        int character;
        while ((character = inStream.read()) != -1) {
            sb.append((char) character);
        }
        String commandOutput = sb.toString();
        LOG.debug("Salt SSH Dir test output: {}", commandOutput);

        try {
            boolean ownerCanReadWrite = ("rw").equals(commandOutput.substring(1, 3));
            boolean userAndGroupSet = commandOutput.contains("salt salt");

            LOG.debug("User can read/write: {} user and group correct: {}", ownerCanReadWrite, userAndGroupSet);
            return userAndGroupSet && ownerCanReadWrite;
        }
        catch (StringIndexOutOfBoundsException e) {
            throw new CommandExecutionException("Unexpected output from command: " + cmd, prc);
        }
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

        try {
            if (!hasCorrectSSHFileOwnership()) {
                String responseMessage = "Cannot read/write '" + SALT_SSH_DIR_PATH + "/known_hosts'. " +
                        "Please check permissions.";
                LOG.error("Error during bootstrap: {}", responseMessage);
                return new BootstrapResult(false, contactMethod,
                        LOC.getMessage("bootstrap.minion.error.noperm", SALT_SSH_DIR_PATH + "/known_hosts"));
            }
        }
        catch (CommandExecutionException | IOException e) {
            LOG.error(e.getMessage(), e);
            return new BootstrapResult(false, contactMethod,
                    LOC.getMessage("bootstrap.minion.error.permcmdexec", SALT_SSH_DIR_PATH + "/known_hosts"));
        }

        try {
            handleAnsiblePreAuthentication(params, user);

            Map<String, Object> pillarData = createPillarData(user, params, contactMethod);
            return saltApi.bootstrapMinion(params, bootstrapMods, pillarData)
                    .fold(error -> {
                            BootstrapError parsedError = SaltUtils.decodeSaltErr(error);
                            LOG.error("Error during bootstrap: {}", parsedError);

                            return new BootstrapResult(false, contactMethod, List.of(parsedError));
                    },
                    result -> {
                        // We have results, check if result = true
                        // for all the single states
                        Optional<String> errMessage = getApplyStateErrorMessage(params.getHost(), result);
                        // Clean up the generated key pair in case of failure
                        boolean success = errMessage.isEmpty() && result.getRetcode() == 0;
                        if (!success) {
                            errMessage.ifPresent(msg -> LOG.error("States failed during bootstrap: {}", msg));

                            BootstrapError error = SaltUtils.decodeBootstrapSSHResult(result);
                            return new BootstrapResult(false, contactMethod, List.of(error));
                        }

                        return new BootstrapResult(true, contactMethod, Collections.emptyList());
                    }
            );
        }
        catch (Exception e) {
            LOG.error("Exception during bootstrap: {}", e.getMessage(), e);
            return new BootstrapResult(false,
                    e.getMessage() != null ? LOC.getMessage("bootstrap.minion.error.salt", e.getMessage()) :
                            LOC.getMessage("bootstrap.minion.error.salt.unexpected"));
        }
    }

    /**
     * Authenticate the Uyuni ssh public key on the bootstrapped host.
     *
     * @param params bootstrap params
     * @param user the user
     * @throws RuntimeException in case the action was not successful
     */
    private void handleAnsiblePreAuthentication(BootstrapParameters params, User user) {
        LOG.info("Pre-authenticating system using Ansible inventory ID: {}", params.getAnsibleInventoryId());
        params.getAnsibleInventoryId()
                .flatMap(pathId -> AnsibleManager.lookupAnsiblePathById(pathId, user))
                .filter(path -> path instanceof InventoryPath)
                .ifPresent(inventoryPath -> {
                    Map<String, Object> pillar = Map.of(
                            "user", params.getUser(),
                            "inventory", inventoryPath.getPath().toString(),
                            "target_host", params.getHost(),
                            "ssh_pubkey", FileUtils.readStringFromFile(SaltSSHService.SSH_PUBKEY_PATH));

                    LocalCall<Map<String, ApplyResult>> call =
                            State.apply(List.of("ansible.mgr-ssh-pubkey-authorized"), of(pillar));
                    String minionId = inventoryPath.getMinionServer().getMinionId();
                    saltApi.callSync(call, minionId).ifPresentOrElse(
                            res -> {
                                // all results must be successful, otherwise we throw an exception
                                List<Object> failedStates = res.entrySet().stream()
                                        .filter(r -> !r.getValue().isResult())
                                        .map(r -> r.getValue().getChanges())
                                        .collect(Collectors.toList());

                                if (!failedStates.isEmpty()) {
                                    LOG.error("Ansible pre-authentication failed: {}", failedStates);
                                    throw new RuntimeException("Ansible pre-authentication state failed");
                                }
                                LOG.debug("Ansible pre-authentication successful");
                            },
                            () -> {
                                LOG.error("Minion '{}' did not respond", minionId);
                                throw new RuntimeException("Minion '" + minionId + "' did not respond");
                            });
                });
    }

    /**
     * Implementation-specific Validation of the json input.
     * @param params the bootstrap params
     * @return the result of the validation
     */
    protected abstract List<String> validateParamsPerContactMethod(BootstrapParameters params);

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
                .map(ServerFactory::lookupById)
                .map(Server::getHostname)
                .orElse(ConfigDefaults.get().getCobblerHost());

        pillarData.put("mgr_server", mgrServer);
        if ("ssh-push-tunnel".equals(contactMethod)) {
            pillarData.put("mgr_server_https_port", Config.get().getInt("ssh_push_port_https"));
        }
        pillarData.put("mgr_origin_server", ConfigDefaults.get().getCobblerHost());
        pillarData.put("minion_id", input.getHost());
        pillarData.put("contact_method", contactMethod);
        pillarData.put("mgr_sudo_user", SaltSSHService.getSSHUser());
        input.getReactivationKey().ifPresent(r -> pillarData.put("management_key", r));
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
            SSHResult<Map<String, ApplyResult>> res) {
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
            SSHResult<Map<String, ApplyResult>> r) {
        return r.getStdout()
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> r.getStderr()
                        .filter(s -> !s.isEmpty())
                        .orElseGet(() -> "No result for " + host));
    }

    private List<String> validateBootstrap(BootstrapParameters params) {
        List<String> errors = validateParamsPerContactMethod(params);
        if (!errors.isEmpty()) {
            return errors;
        }

        if (params.getPrivateKey().map(pk -> pk.length() > KEY_LENGTH_LIMIT).orElse(false)) {
            return Collections.singletonList("Key string is too long.");
        }

        Optional<String> activationKeyErrorMessage = params.getFirstActivationKey()
                .flatMap(this::validateActivationKey);
        if (activationKeyErrorMessage.isPresent()) {
            return Collections.singletonList(activationKeyErrorMessage.get());
        }

        Optional<String> reactivationKeyError = validateReactivationKey(params.getReactivationKey());
        if (reactivationKeyError.isPresent()) {
            return Collections.singletonList(reactivationKeyError.get());
        }

        if (saltApi.keyExists(params.getHost(), KeyStatus.ACCEPTED, KeyStatus.DENIED, KeyStatus.REJECTED)) {
            return Collections.singletonList("A salt key for this" +
                    " host (" + params.getHost() +
                    ") seems to already exist, please check!");
        }

        if (params.getReactivationKey().isPresent()) {
            return Collections.emptyList();
        }

        return MinionServerFactory.findByMinionId(params.getHost())
                .map(m -> Collections.singletonList("A system '" +
                        m.getName() + "' with minion id " + params.getHost() +
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

    /**
     * Checks whether the reactivation key exists
     *
     * @param reactivationKeyLabel desired reactivation key label
     * @return Optional with error message or empty if validation succeeds
     */
    private Optional<String> validateReactivationKey(Optional<String> reactivationKeyLabel) {
        if (reactivationKeyLabel.isEmpty()) {
            return Optional.empty();
        }
        String rLabel = reactivationKeyLabel.get();
        ActivationKey reactivationKey = ActivationKeyFactory.lookupByKey(rLabel);

        if (reactivationKey == null) {
            return Optional.of(String.format("Selected reactivation '%s' key not found.", rLabel));
        }
        if (reactivationKey.getServer() == null) {
            return Optional.of(String.format("Selected reactivation key '%s' has no server set for reactivation.",
                    rLabel));
        }
        return Optional.empty();
    }

    protected abstract Optional<String> validateContactMethod(ContactMethod desiredContactMethod);

}
