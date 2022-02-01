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

import static com.suse.manager.webui.services.impl.SaltSSHService.SSH_PUSH_PORT;
import static com.suse.manager.webui.services.impl.SaltSSHService.getSSHUser;

import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.InputValidator;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.manager.webui.utils.gson.BootstrapParameters;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Code for bootstrapping salt-ssh systems using salt-ssh.
 */
public class SSHMinionBootstrapper extends AbstractMinionBootstrapper {

    private static SSHMinionBootstrapper instance;
    private static final Logger LOG = Logger.getLogger(SSHMinionBootstrapper.class);

    /**
     * Standard constructor. For testing only - to obtain instance of this class, use
     * getInstance.
     * @param systemQueryIn systemQuery to use
     */
    public SSHMinionBootstrapper(SystemQuery systemQueryIn) {
        super(systemQueryIn);
    }

    /**
     * Get instance of the SSHMinionBootstrapper
     * @param systemQuery systemQuery to use
     * @return the instance of the SSHMinionBootstrapper
     */
    public static synchronized SSHMinionBootstrapper getInstance(SystemQuery systemQuery) {
        if (instance == null) {
            instance = new SSHMinionBootstrapper(systemQuery);
        }
        return instance;
    }

    @Override
    protected List<String> validateParamsPerContactMethod(BootstrapParameters params) {
        return InputValidator.INSTANCE.validateBootstrapSSHManagedInput(params);
    }

    @Override
    protected List<String> getBootstrapMods() {
        return Arrays.asList(
                ApplyStatesEventMessage.CERTIFICATE,
                "ssh_bootstrap");
    }

    @Override
    protected Optional<String> validateContactMethod(ContactMethod desiredContactMethod) {
        boolean isIncompatible = Stream.of(
                ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH),
                ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH_TUNNEL)
        ).noneMatch(cm -> cm.getId().equals(desiredContactMethod.getId()));

        if (isIncompatible) {
            return Optional.of("Selected activation key cannot be used as its contact" +
                    " method is not compatible with the salt-ssh systems.");
        }

        return Optional.empty();
    }

    @Override
    protected BootstrapResult bootstrapInternal(BootstrapParameters params, User user,
                                                String defaultContactMethod) {
        Optional<MgrUtilRunner.ExecResult> res = systemQuery.generateSSHKey(SaltSSHService.SSH_KEY_PATH);
        if (!res.isPresent()) {
            LOG.error("Could not generate salt-ssh public key.");
            return new BootstrapResult(false, Optional.empty(), "Could not generate salt-ssh public key.");
        }
        if (!(res.get().getReturnCode() == 0 || res.get().getReturnCode() == -1)) {
            LOG.error("Generating salt-ssh public key failed: " + res.get().getStderr());
            return new BootstrapResult(false, Optional.empty(),
                    "Generating salt-ssh public key failed: " + res.get().getStderr());
        }

        BootstrapResult result = super.bootstrapInternal(params, user, defaultContactMethod);
        LOG.info("salt-ssh system bootstrap success: " + result.isSuccess() +
                ", proceeding with registration.");
        String minionId = params.getHost();
        try {
            if (result.isSuccess()) {
                Optional<List<String>> proxyPath = params.getProxyId()
                        .map(proxyId -> ServerFactory.lookupById(proxyId))
                        .map(proxy -> SaltSSHService.proxyPathToHostnames(
                                proxy.getServerPaths(), proxy));
                MinionPendingRegistrationService.addMinion(user, minionId,
                        result.getContactMethod().orElse(defaultContactMethod),
                        proxyPath);
                getRegisterAction().registerSSHMinion(
                        minionId, params.getProxyId(),
                        params.getFirstActivationKey());
            }
        }
        finally {
            MinionPendingRegistrationService.removeMinion(minionId);
        }
        return result;
    }

    // we want to override this in tests
    protected RegisterMinionEventMessageAction getRegisterAction() {
        return new RegisterMinionEventMessageAction(systemQuery);
    }

    /**
     * Create the bootstrap parameters specific for salt-ssh push minions.
     * @param input json input
     * @return the bootstrap parameters
     */
    @Override
    public BootstrapParameters createBootstrapParams(BootstrapHostsJson input) {
        final BootstrapParameters params = BootstrapParameters.createFromJson(input);
        if (StringUtils.isEmpty(input.getUser())) {
            params.setUser(getSSHUser());
        }

        params.setPort(Optional.of(SSH_PUSH_PORT));

        return params;
    }

    @Override
    protected Map<String, Object> createPillarData(User user, BootstrapParameters input,
                                                   String contactMethod) {
        Map<String, Object> pillarData = super.createPillarData(user, input, contactMethod);
        input.getProxyId().ifPresent(
                proxyId -> {
                    String key = SaltSSHService.retrieveSSHPushProxyPubKey(proxyId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Could not retrieve ssh-push public key from proxy. " +
                                            "Check if proxy is up and can be reached."));
                    pillarData.put("proxy_pub_key", key);
                });
        return pillarData;
    }

}
