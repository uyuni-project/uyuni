/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.webui.services.iface;


import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SaltSSHApi extends SaltApi {

    private final SaltSSHService saltSSHService;

    /**
     * Default Constructor
     *
     */
    public SaltSSHApi() {
        super();
        saltSSHService = new SaltSSHService(saltClient, SaltActionChainGeneratorService.INSTANCE);
    }

    /**
     * @deprecated this function is too general and should be replaced by more specific functionality.
     * @return saltSSHService to get
     */
    @Deprecated
    public SaltSSHService getSaltSSHService() {
        return saltSSHService;
    }

    /**
     * Executes match.glob in another thread and returns a {@link CompletionStage}.
     * @param target the target to pass to match.glob
     * @param cancel a future used to cancel waiting
     * @return a future or Optional.empty if there's no ssh-push minion in the db
     */
    public Optional<CompletionStage<Map<String, Result<Boolean>>>> getMatchAsyncSSH(
            String target, CompletableFuture<GenericError> cancel) {
        return saltSSHService.matchAsyncSSH(target, cancel);
    }


    @Override
    public List<String> getMinions(MinionList target) {

        HashSet<String> uniqueMinionIds = new HashSet<>(target.getTarget());
        Map<Boolean, List<String>> minionPartitions =
                partitionMinionsByContactMethod(uniqueMinionIds);

        return minionPartitions.get(true);
    }

    /**
     * Run a remote command on a given minion asynchronously.
     * @param target the target
     * @param cmd the command to execute
     * @param cancel a future used to cancel waiting on return events
     * @return a map holding a {@link CompletionStage}s for each minion
     */
    @Override
    public Map<String, CompletionStage<Result<String>>> runRemoteCommandAsync(
            MinionList target, String cmd, CompletableFuture<GenericError> cancel) {

        List<String> minionIds = this.getMinions(target);

        Map<String, CompletionStage<Result<String>>> results = new HashMap<>();
        LocalCall<String> call = Cmd.run(cmd);
        if (!minionIds.isEmpty()) {
            results.putAll(saltSSHService.callAsyncSSH(call, new MinionList(minionIds),
                    cancel));
        }

        return results;
    }

    /**
     * Bootstrap a system using salt-ssh.
     *
     * @param parameters - bootstrap parameters
     * @param bootstrapMods - state modules to be applied during the bootstrap
     * @param pillarData - pillar data used salt-ssh call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     * @return the result of the underlying ssh call for given host
     */
    public Result<SSHResult<Map<String, ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData)
        throws SaltException {
        return saltSSHService.bootstrapMinion(parameters, bootstrapMods, pillarData);
    }

    /**
     * Remove SUSE Manager specific configuration from a Salt minion.
     *
     * @param minion the minion.
     * @param timeout operation timeout
     * @return list of error messages or empty if no error
     */
    public Optional<List<String>> cleanupMinion(MinionServer minion, int timeout) {
        return saltSSHService.cleanupSSHMinion(minion, timeout);
    }

    /**
     * Synchronously executes a salt function on a single minion. If a
     * SaltException is thrown, re-throw a RuntimeException.
     *
     * @param call salt function to call
     * @param minionId minion id to target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function or empty if the
     * minion did not respond.
     */
    @Override
    <T> Map<String, Result<T>> callSync(LocalCall<T> callIn, MinionList target)
        throws SaltException {

        List<String> minionIds = this.getMinions(target);

        Map<String, Result<T>> results = new HashMap<>();

        if (!minionIds.isEmpty()) {
            results.putAll(
                    saltSSHService.callSyncSSH(callIn, new MinionList(minionIds)));
        }

        return results;
    }

}
