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

import com.suse.manager.webui.utils.salt.Zypper;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Smbios.RecordType;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.modules.Network;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for accessing salt via the API.
 */
public interface SaltService {

    /**
     * Get the minion keys from salt with their respective status.
     *
     * @return the keys with their respective status as returned from salt
     */
    Key.Names getKeys();

    /**
     * Get the grains for a given minion.
     *
     * @param minionId id of the target minion
     * @return map containing the grains
     */
    Map<String, Object> getGrains(String minionId);

    /**
     * Get the machine id for a given minion.
     *
     * @param minionId id of the target minion
     * @return the machine id as a string
     */
    String getMachineId(String minionId);

    /**
     * Query all present minions according to salt's presence detection.
     *
     * @return the list of minion keys that are present
     */
    List<String> present();

    /**
     * Get all installed packages from a given minion.
     *
     * @param minionId id of the target minion
     * @return a map from package names to list of version strings
     */
    Map<String, List<String>> getPackages(String minionId);

    /**
     * Get the installed packages from a minion with package info.
     *
     * @param minionId id of the target minion
     * @param attributes package attributes that should be returned
     * @return a map from package names to package info objects
     */
    Map<String, Pkg.Info> getInstalledPackageDetails(
            String minionId, List<String> attributes);

    /**
     * Accept a given minion's key.
     *
     * @param minionId id of the minion
     */
    void acceptKey(String minionId);

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
     * Apply states on a given target
     *
     * @param target the target to apply the states to
     * @param mods a list of states to apply. If empty list is provided all
     *             states will be applied.
     * @return the result of applying the states
     */
    LocalAsyncResult<Map<String, Object>> applyState(Target<?> target, List<String> mods);

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
    Map<String, String> runRemoteCommand(Target<?> target, String cmd);

    /**
     * Match the salt minions against a target glob.
     *
     * @param target the target glob
     * @return a map from minion name to boolean representing if they matched the target
     */
    Map<String, Boolean> match(String target);

    /**
     * Injects an event into the salt event bus
     * @param tag salt event tag
     * @param data salt event data
     * @return true if successful otherwise false
     */
    boolean sendEvent(String tag, Object data);

    /**
     * Get the CPU info from a minion.
     * @param minionId the minion id
     * @return the CPU data as a map.
     */
    Map<String, Object> getCpuInfo(String minionId);

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
    Map<String, Object> getDmiRecords(String minionId, RecordType recordType);

    /**
     * Get the udev database from a minion.
     * @param minionId the minion id
     * @return the udev db as a list of maps, where each map is a db entry.
     */
    List<Map<String, Object>> getUdevdb(String minionId);

    /**
     * Get the content of file from a minion.
     * @param minionId the minion id
     * @param path the path of the file
     * @return the content of a file as a string
     */
    String getFileContent(String minionId, String path);

    /**
     * Get the output of the '/usr/bin/read_values' if available.
     * @param minionId the minion id
     * @return the output of command as a string.
     */
    String getMainframeSysinfoReadValues(String minionId);

    /**
     * Get the network interfaces from a minion.
     * @param minionId the minion id
     * @return a map containing information about each network interface
     */
    Map<String, Network.Interface> getNetworkInterfacesInfo(String minionId);

    /**
     * Schedule patch installations for a given target.
     *
     * @param name the name to use for the scheduled job
     * @param target the target
     * @param patches the patches to install
     * @param scheduleDate schedule date
     * @param metadata metadata to pass to the salt job
     * @return the result of the schedule call
     * @throws SaltException in case there is an error scheduling the job
     */
    Map<String, Schedule.Result> schedulePatchInstallation(String name, Target<?> target,
            Set<String> patches, LocalDateTime scheduleDate, Map<String, ?> metadata)
            throws SaltException;

    /**
     * Remove a scheduled job from the minion
     *
     * @param name the name of the job to delete from the schedule
     * @param target the target
     * @return the result
     */
    Map<String, Schedule.Result> deleteSchedule(String name, Target<?> target);


    /**
     * Get the IP routing that the minion uses to connect to the master.
     * @param minionId the minion id
     * @return a map of IPv4 and IPv6 (if available) {@link SumaUtil.IPRoute}
     */
    Map<SumaUtil.IPVersion, SumaUtil.IPRoute> getPrimaryIps(String minionId);

    /**
     * Get the kernel modules used for each network interface.
     * @param minionId the minion id
     * @return a map with the network interface name as key and
     * the kernel module name or null as a value
     */
    Map<String, String> getNetModules(String minionId);

    /**
     * Gets the installed products of the minion
     * @param minionId the minion id
     * @return a list of installed products
     */
    List<Zypper.RealProductInfo> getInstalledProducts(String minionId);
    /**
     * Find all minions matching the target expression and
     * retain only those allowed for the given user.
     *
     * @param user the user
     * @param target the Salt target expression
     * @return a set of minion ids
     */
    Set<String> getAllowedMinions(User user, String target);

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
    void storeOrgState(long orgId, String name, String content,
                       String oldName, String oldChecksum);

    /**
     * Delete a Salt .sls file.
     * @param orgId the organization id
     * @param name the name of the file
     */
    void deleteOrgState(long orgId, String name);

    /**
     * Get a list of all Salt .sls files
     * for a given organization.
     *
     * @param orgId the organization id
     * @return a list of names without the .sls extension
     */
    List<String> getOrgStates(long orgId);

    /**
     * Get the content of the give Salt .sls file.
     * @param orgId the organization id
     * @param name the name of the file
     * @return the content of the file if the file exists
     */
    Optional<String> getOrgStateContent(long orgId, String name);
}
