/*
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

import static java.util.Optional.of;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.AnsibleManager;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService.KeyStatus;
import com.suse.manager.webui.utils.InputValidator;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.calls.wheel.Key;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Code for bootstrapping salt minions using salt-ssh.
 */
public class RegularMinionBootstrapper extends AbstractMinionBootstrapper {

    private static final Logger LOG = Logger.getLogger(RegularMinionBootstrapper.class);

    /**
     * Standard constructor. For testing only - to obtain instance of this class, use
     * getInstance.
     * @param systemQueryIn systemQuery to use
     * @param saltApiIn saltApi to use
     */
    public RegularMinionBootstrapper(SystemQuery systemQueryIn, SaltApi saltApiIn) {
        super(systemQueryIn, saltApiIn);
    }

    @Override
    protected List<String> validateParamsPerContactMethod(BootstrapParameters params) {
        return InputValidator.INSTANCE.validateBootstrapInput(params);
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

        Key.Pair keyPair = saltApi.generateKeysAndAccept(input.getHost(), false);
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
        MinionPendingRegistrationService.addMinion(user, minionId, defaultContactMethod);

        // If a key is pending for this minion, temporarily reject it
        boolean weRejectedIt = false;
        if (saltApi.keyExists(minionId, KeyStatus.UNACCEPTED)) {
            LOG.info("Pending key exists for " + minionId + ", rejecting...");
            saltApi.rejectKey(minionId);
            weRejectedIt = true;
        }

        BootstrapResult result = super.bootstrapInternal(input, user, defaultContactMethod);
        if (!result.isSuccess()) {
            saltApi.deleteKey(minionId);
            MinionPendingRegistrationService.removeMinion(minionId);
        }
        else if (weRejectedIt) {
            LOG.info("Removing key that was temporarily rejected for " + minionId);
            saltApi.deleteRejectedKey(minionId);
        }
        LOG.info("Minion bootstrap success: " + result.isSuccess());
        return result;
    }

    /**
     * We want to cleanup the authorized ssh key, which was copied by Ansible and was used for authentication of the
     * primal bootstrap call.
     *
     * Since we don't have the minion id of the bootstrap minion yet, we need to use Ansible for removing the authorized
     * key.
     *
     * @param params bootstrap params
     * @param user the user
     */
    @Override
    protected void handleAnsibleCleanup(BootstrapParameters params, User user) {
        LOG.info("Ansible authentication cleanup for " + params.getHost());
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
                            State.apply(List.of("ansible.mgr-ssh-pubkey-removed"), of(pillar));
                    String minionId = inventoryPath.getMinionServer().getMinionId();
                    saltApi.callSync(call, minionId).ifPresentOrElse(
                            res -> {
                                // all results must be successful, otherwise we throw an exception
                                List<String> failedStates = res.entrySet().stream()
                                        .filter(r -> !r.getValue().isResult())
                                        .map(r -> r.getKey())
                                        .collect(Collectors.toList());

                                if (!failedStates.isEmpty()) {
                                    throw new RuntimeException("Ansible cleanup states failed: " + failedStates);
                                }
                                LOG.debug("Ansible cleanup successful");
                            },
                            () -> {
                                throw new RuntimeException("Minion '" + minionId + "' did not respond");
                            });
                });
    }
}
