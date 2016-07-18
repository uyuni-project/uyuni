/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.utils.salt.Zypper;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.RunnerCall;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.Smbios.RecordType;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Network;
import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for accessing salt via the API.
 */
public interface SaltService {

    /**
     * Executes a salt function on a single minion.
     *
     * @param call salt function to call
     * @param minionId minion id to target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function
     * or none if the minion did not respond.
     */
    <R> Optional<R> callSync(LocalCall<R> call, String minionId);

    /**
     * Executes a salt runner module function
     *
     * @param call salt function to call
     * @param <R> result type of the salt function
     * @return the result of the function
     */
    <R> R callSync(RunnerCall<R> call);

    /**
     * Get the minion keys from salt with their respective status.
     *
     * @return the keys with their respective status as returned from salt
     */
    Key.Names getKeys();

    /**
     * For a given id check if there is a minion key in any status.
     *
     * @param id the id to check for
     * @return true if there is a key with the given id, false otherwise
     */
    boolean keyExists(String id);

    /**
     * Get the minion keys from salt with their respective status and fingerprint.
     *
     * @return the keys with their respective status and fingerprint as returned from salt
     */
    Key.Fingerprints getFingerprints();

    /**
     * Generate a key pair for the given id and accept the public key.
     *
     * @param id the id to use
     * @param force set true to overwrite an already existing key
     * @return the generated key pair
     */
    com.suse.manager.webui.utils.salt.Key.Pair generateKeysAndAccept(String id,
            boolean force);

    /**
     * Get the grains for a given minion.
     *
     * @param minionId id of the target minion
     * @return map containing the grains
     */
    Optional<Map<String, Object>> getGrains(String minionId);

    /**
     * Get the machine id for a given minion.
     *
     * @param minionId id of the target minion
     * @return the machine id as a string
     */
    Optional<String> getMachineId(String minionId);

    /**
     * Get the timezone offsets for a target, e.g. a list of minions.
     *
     * @param target the targeted minions
     * @return the timezone offsets of the targeted minions
     */
    Map<String, Result<String>> getTimezoneOffsets(Target<?> target);

    /**
     * Accept all keys matching the given pattern
     *
     * @param pattern for minion ids
     */
    void acceptKey(String pattern);

    /**
     * Delete a given minion's key.
     *
     * @param minionId id of the minion
     */
    void deleteKey(String minionId);

    /**
     * Reject a given minion's key.
     *
     * @param minionId id of the minion
     */
    void rejectKey(String minionId);

    /**
     * Return the stream of events happening in salt.
     *
     * @return the event stream
     */
    EventStream getEventStream();

    /**
     * Run a remote command on a given minion.
     *
     * @param target the target
     * @param cmd the command
     * @return the output of the command
     */
    Map<String, Result<String>> runRemoteCommand(Target<?> target, String cmd);


    /**
     * Returns the currently running jobs on the target
     *
     * @param target the target
     * @return list of running jobs
     */
    Map<String, Result<List<SaltUtil.RunningInfo>>> running(Target<?> target);


    /**
     * Return the jobcache filtered by metadata
     *
     * @param metadata search metadata
     * @return list of running jobs
     */
    Map<String, Jobs.ListJobsEntry> jobsByMetadata(Object metadata);

    /**
     * Return the result for a jobId
     *
     * @param jid the job id
     * @return map from minion to result
     */
    Jobs.Info listJob(String jid);

    /**
     * Match the salt minions against a target glob.
     *
     * @param target the target glob
     * @return a map from minion name to boolean representing if they matched the target
     */
    Map<String, Result<Boolean>> match(String target);

    /**
     * Get the CPU info from a minion.
     * @param minionId the minion id
     * @return the CPU data as a map.
     */
    Optional<Map<String, Object>> getCpuInfo(String minionId);

    /**
     * Call 'saltutil.sync_beacons' to sync the beacons to the target minion(s).
     * @param target a target glob
     */
    void syncBeacons(String target);

    /**
     * Call 'saltutil.sync_grains' to sync the grains to the target minion(s).
     * @param target a target glob
     */
    void syncGrains(String target);

    /**
     * Call 'saltutil.sync_modules' to sync the grains to the target minion(s).
     * @param target a target glob
     */
    void syncModules(String target);

    /**
     * Get DMI records from a minion.
     * @param minionId the minion id
     * @param recordType the record type to get
     * @return the DMI data as a map. An empty
     * imutable map is returned if there is no data.
     */
    Optional<Map<String, Object>> getDmiRecords(String minionId, RecordType recordType);

    /**
     * Get the udev database from a minion.
     * @param minionId the minion id
     * @return the udev db as a list of maps, where each map is a db entry.
     */
    Optional<List<Map<String, Object>>> getUdevdb(String minionId);

    /**
     * Get the content of file from a minion.
     * @param minionId the minion id
     * @param path the path of the file
     * @return the content of a file as a string
     */
    Optional<String> getFileContent(String minionId, String path);

    /**
     * Get the output of the '/usr/bin/read_values' if available.
     * @param minionId the minion id
     * @return the output of command as a string.
     */
    Optional<String> getMainframeSysinfoReadValues(String minionId);

    /**
     * Get the network interfaces from a minion.
     * @param minionId the minion id
     * @return a map containing information about each network interface
     */
    Optional<Map<String, Network.Interface>> getNetworkInterfacesInfo(String minionId);

    /**
     * Schedule a function call for a given target.
     *
     * @param name the name to use for the scheduled job
     * @param call the module call to schedule
     * @param target the target
     * @param scheduleDate schedule date
     * @param metadata metadata to pass to the salt job
     * @return the result of the schedule call
     * @throws SaltException in case there is an error scheduling the job
     */
    Map<String, Result<Schedule.Result>> schedule(String name, LocalCall<?> call,
            Target<?> target, ZonedDateTime scheduleDate, Map<String, ?> metadata)
            throws SaltException;

    /**
     * Remove a scheduled job from the minion
     *
     * @param name the name of the job to delete from the schedule
     * @param target the target
     * @return the result
     */
    Map<String, Result<Schedule.Result>> deleteSchedule(String name, Target<?> target);

    /**
     * Remove a scheduled task (referenced via action id) on a list of servers.
     *
     * @param sids server ids
     * @param aid action id
     * @return the list of server ids that successfully removed the action
     */
    List<Long> deleteSchedulesForActionId(List<Long> sids, long aid);

    /**
     * Execute a LocalCall synchronously on the default Salt client.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call
     * @return the result of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    <T> Map<String, Result<T>> callSync(LocalCall<T> call, Target<?> target)
            throws SaltException;

    /**
     * Execute a LocalCall synchronously using salt-ssh.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call
     * @param rosterFile alternative roster file to use (default: /etc/salt/roster)
     * @param ignoreHostKeys use this option to disable 'StrictHostKeyChecking'
     * @param sudo run command via sudo (default: false)
     * @return result of the call
     */
    <T> Map<String, Result<SSHResult<T>>> callSyncSSH(LocalCall<T> call, Target<?> target,
            boolean ignoreHostKeys, String rosterFile, boolean sudo);

    /**
     * Execute a LocalCall asynchronously on the default Salt client.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call
     * @return the LocalAsyncResult of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    <T> LocalAsyncResult<T> callAsync(LocalCall<T> call, Target<?> target)
            throws SaltException;

    /**
     * Get the IP routing that the minion uses to connect to the master.
     * @param minionId the minion id
     * @return a map of IPv4 and IPv6 (if available) {@link SumaUtil.IPRoute}
     */
    Optional<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>> getPrimaryIps(String minionId);

    /**
     * Get the kernel modules used for each network interface.
     * @param minionId the minion id
     * @return a map with the network interface name as key and
     * the kernel module name or null as a value
     */
    Optional<Map<String, Optional<String>>> getNetModules(String minionId);

    /**
     * Find all minions matching the target expression and
     * retain only those allowed for the given user.
     *
     * @param user the user
     * @param target the Salt target expression
     * @return a set of minion ids
     */
    Set<String> getAllowedMinions(User user, String target);


    public Optional<List<Zypper.ProductInfo>> getListProducts(String minionId);

    /**
     * Save a Salt .sls file.
     * @param orgId the organization id
     * @param name the name of the file
     * @param content the content of the file
     * @param oldName the previous name of the file,
     *                when the file already exists
     * @param oldChecksum the checksum of the file at
     *                    the time of showing it to the user
     */
    void saveCustomState(long orgId, String name, String content,
        String oldName, String oldChecksum);

    /**
     * Delete a Salt .sls file.
     * @param orgId the organization id
     * @param name the name of the file
     */
    void deleteCustomState(long orgId, String name);

    /**
     * Get a list of all Salt .sls files
     * for a given organization.
     *
     * @param orgId the organization id
     * @return a list of names without the .sls extension
     */
    List<String> getCatalogStates(long orgId);

    /**
     * Get the content of the give Salt .sls file.
     * @param orgId the organization id
     * @param name the name of the file
     * @return the content of the file if the file exists
     */
    Optional<String> getOrgStateContent(long orgId, String name);

    /**
     * Add the organization namespace to the given states.
     * @param orgId the organization id
     * @param states the states names
     * @return a set of names that included the organization namespace
     */
    Set<String> resolveOrgStates(long orgId, Set<String> states);

    /**
     * Pings a target set of minions.
     * @param target the target
     * @return a Map from minion ids which responded to the ping to Boolean.TRUE
     * @throws SaltException if we get a failure from Salt
     */
    Map<String, Result<Boolean>> ping(Target<?> target) throws SaltException;

    /**
     * Get the directory where custom state files are stored on disk.
     * @param orgId the organization id
     * @return the path where .sls files are stored
     */
    String getCustomStateBaseDir(long orgId);

}
