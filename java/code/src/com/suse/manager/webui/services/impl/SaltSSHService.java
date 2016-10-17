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
package com.suse.manager.webui.services.impl;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.suse.manager.webui.utils.MinionServerUtils;
import com.suse.manager.webui.utils.SaltRoster;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.SaltSSHConfig;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.datatypes.target.Glob;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.salt.netapi.utils.Xor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Code for calling salt-ssh functions.
 */
public class SaltSSHService {

    private static final String CONFIG_KEY_SUDO_USER = "ssh_push_sudo_user";
    private static final String SSH_KEY_PATH = "/srv/susemanager/salt/salt_ssh/mgr_ssh_id";
    private static final String ROOT = "root";
    public static final int SSH_PUSH_PORT = 22;

    private static final Logger LOG = Logger.getLogger(SaltSSHService.class);

    // Shared salt client instance
    private final SaltClient saltClient;

    /**
     * Standard constructor.
     * @param saltClientIn salt client to use for the underlying salt calls
     */
    public SaltSSHService(SaltClient saltClientIn) {
        this.saltClient = saltClientIn;
    }

    /**
     * Returns the user that should be used for the ssh calls done by salt-ssh.
     * @return the user
     */
    public static String getSSHUser() {
        String sudoUser = Config.get().getString(CONFIG_KEY_SUDO_USER);
        return StringUtils.isBlank(sudoUser) ? ROOT : sudoUser;
    }

    /**
     * Synchronously executes a salt function on given minion list using salt-ssh.
     *
     * Before the execution, this method creates an one-time roster corresponding to targets
     * in given minion list.
     *
     * @param call the salt call
     * @param target the minion list target
     * @param <R> result type of the salt function
     * @return the result of the call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     */
    public <R> Map<String, Result<R>> callSyncSSH(LocalCall<R> call, MinionList target)
            throws SaltException {
        SaltRoster roster = new SaltRoster();
        // these values are mostly fixed, which should change when we allow configuring
        // per-minionserver
        target.getTarget().stream().forEach(mid ->
                roster.addHost(mid, getSSHUser(), Optional.empty(),
                        Optional.of(SSH_PUSH_PORT)));

        return unwrapSSHReturn(
                callSyncSSHInternal(call, target, roster, false, isSudoUser(getSSHUser())));
    }

    /**
     * Synchronously executes a salt function on given glob using salt-ssh.
     *
     * Before the execution, this method creates an one-time roster corresponding all
     * minions with the ssh contact method and minions being currently bootstrapped.
     *
     * @param call the salt call
     * @param target the minion list target
     * @param <R> result type of the salt function
     * @return the result of the call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     */
    public <R> Map<String, Result<R>> callSyncSSH(LocalCall<R> call, Glob target)
            throws SaltException {
        SaltRoster roster = createAllServersRoster();
        return unwrapSSHReturn(
                callSyncSSHInternal(call, target, roster, false, isSudoUser(getSSHUser())));
    }

    /**
     * Helper method for creating a salt roster containing all minions with ssh contact
     * method and all minions being currently bootstrapped.
     * @return roster
     */
    private SaltRoster createAllServersRoster() {
        SaltRoster roster = new SaltRoster();
        Stream.concat(
                MinionServerFactory.listMinions().stream()
                        .filter(minion -> MinionServerUtils.isSshPushMinion(minion))
                        .map(minion -> minion.getMinionId()),
                SSHMinionsPendingRegistrationService.getMinions().stream())
                .distinct()
                .forEach(mid -> roster.addHost(mid, getSSHUser(), Optional.empty(),
                        Optional.of(SSH_PUSH_PORT)));
        return roster;
    }

    /**
     * Bootstrap a system using salt-ssh.
     *
     * The call internally uses ssh identity key/cert on a hardcoded path.
     * If the key/cert doesn't exist, it's created by salt and copied to the target host.
     * Copying is implemented via a salt state (mgr_ssh_identity), as ssh_key_deploy
     * is ignored by the api.)
     *
     * @param parameters - bootstrap parameters
     * @param bootstrapMods - state modules to be applied during the bootstrap
     * @param pillarData - pillar data used in the salt-ssh call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     * @return the result of the underlying ssh call for given host
     */
    public Result<SSHResult<Map<String, State.ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData) throws SaltException {
        LOG.info("Bootstrapping host: " + parameters.getHost());
        LocalCall<Map<String, State.ApplyResult>> call = State.apply(
                bootstrapMods,
                Optional.of(pillarData),
                Optional.of(true));

        SaltRoster roster = new SaltRoster();
        roster.addHost(parameters.getHost(), parameters.getUser(), parameters.getPassword(),
                parameters.getPort());

        Map<String, Result<SSHResult<Map<String, State.ApplyResult>>>> result =
                callSyncSSHInternal(call,
                        new MinionList(parameters.getHost()),
                        roster,
                        parameters.isIgnoreHostKeys(),
                        isSudoUser(parameters.getUser()));
        return result.get(parameters.getHost());
    }

    private boolean isSudoUser(String user) {
        return !ROOT.equals(user);
    }

    /**
     * Return the Map of Result objects that contain either the error from SSHResult or the
     * unwrapped return value from SSHResult.
     */
    private <T> Map<String, Result<T>> unwrapSSHReturn(Map<String,
            Result<SSHResult<T>>> sshResults) {
         return sshResults.entrySet().stream()
                .collect(Collectors.toMap(
                        kv -> kv.getKey(),
                        kv -> kv.getValue().fold(
                                err -> new Result<T>(Xor.left(kv.getValue().error().get())),
                                succ -> new Result<T>(
                                        Xor.right(kv.getValue().result().get().getReturn()
                                                .get())))));
    }

    /**
     * Boilerplate for executing a synchronous salt-ssh code. This involves:
     * - generating the salt config,
     * - generating the roster, storing it on the disk,
     * - calling the salt-ssh via salt-api,
     * - cleaning up the roster file after the job is done.
     *
     * Note on the SSH identity (key/cert pair):
     * This call uses the SSH key stored on SSH_KEY_PATH. If such file doesn't
     * exist, salt automatically generates a key/cert pair on this path.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call, only Glob and MinionList is supported
     * @param roster salt-ssh roster
     * @param ignoreHostKeys use this option to disable 'StrictHostKeyChecking'
     * @param sudo run command via sudo (default: false)
     *
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     *
     * @return result of the call
     */
    private <T> Map<String, Result<SSHResult<T>>> callSyncSSHInternal(LocalCall<T> call,
            Target target, SaltRoster roster, boolean ignoreHostKeys, boolean sudo)
            throws SaltException {
        if (!(target instanceof MinionList || target instanceof Glob)) {
            throw new UnsupportedOperationException("Only MinionList and Glob supported.");
        }

        Path rosterPath = null;
        try {
            rosterPath = roster.persistInTempFile();
            SaltSSHConfig sshConfig = new SaltSSHConfig.Builder()
                    .ignoreHostKeys(ignoreHostKeys)
                    .rosterFile(rosterPath.toString())
                    .priv(SSH_KEY_PATH)
                    .sudo(sudo)
                    .wipe(true)
                    .refreshCache(true)
                    .build();

            return call.callSyncSSH(saltClient, target, sshConfig);
        }
        catch (IOException e) {
            LOG.error("Error operating on roster file: " + e.getMessage());
            throw new SaltException(e);
        }
        finally {
            if (rosterPath != null) {
                try {
                    Files.deleteIfExists(rosterPath);
                }
                catch (IOException e) {
                    LOG.error("Can't delete roster file: " + e.getMessage());
                }
            }
        }
    }
}
