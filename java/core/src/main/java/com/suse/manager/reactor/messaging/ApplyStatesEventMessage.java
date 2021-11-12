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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;

import org.hibernate.Transaction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An event to signal that a set of states is dirty and needs
 * to be applied to a particular server
 */
public class ApplyStatesEventMessage implements EventDatabaseMessage {

    public static final String CERTIFICATE = "certs";
    public static final String PACKAGES = "packages";
    public static final String PACKAGES_PROFILE_UPDATE = "packages.profileupdate";
    public static final String HARDWARE_PROFILE_UPDATE = "hardware.profileupdate";
    public static final String CHANNELS = "channels";
    public static final String CHANNELS_DISABLE_LOCAL_REPOS = "channels.disablelocalrepos";
    public static final String SALT_MINION_SERVICE = "services.salt-minion";
    public static final String SYNC_CUSTOM_ALL = "util.synccustomall";
    public static final String SYNC_STATES = "util.syncstates";
    public static final String DISTUPGRADE = "distupgrade";
    public static final String SALTBOOT = "saltboot";
    public static final String SYSTEM_INFO = "util.systeminfo";

    private final long serverId;
    private final Long userId;
    private final List<String> stateNames;
    private final boolean forcePackageListRefresh;
    private final Transaction txn;

    /**
     * Constructor for creating a {@link ApplyStatesEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param forcePackageListRefreshIn set true to request a package list refresh
     * @param stateNamesIn state module names to be applied to the server
     */
    public ApplyStatesEventMessage(long serverIdIn, boolean forcePackageListRefreshIn,
            String... stateNamesIn) {
        this(serverIdIn, null, forcePackageListRefreshIn, stateNamesIn);
    }

    /**
     * Constructor for creating a {@link ApplyStatesEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param forcePackageListRefreshIn set true to request a package list refresh
     * @param stateNamesIn state module names to be applied to the server
     */
    public ApplyStatesEventMessage(long serverIdIn, boolean forcePackageListRefreshIn,
            List<String> stateNamesIn) {
        this(serverIdIn, forcePackageListRefreshIn,
                stateNamesIn.toArray(new String[stateNamesIn.size()]));
    }

    /**
     * Constructor for creating a {@link ApplyStatesEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param userIdIn the user id
     * @param forcePackageListRefreshIn set true to request a package list refresh
     * @param stateNamesIn state module names to be applied to the server
     */
    public ApplyStatesEventMessage(long serverIdIn, Long userIdIn,
            boolean forcePackageListRefreshIn, List<String> stateNamesIn) {
        this(serverIdIn, userIdIn, forcePackageListRefreshIn,
                stateNamesIn.toArray(new String[stateNamesIn.size()]));
    }

    /**
     * Constructor for creating a {@link ApplyStatesEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param userIdIn the user id
     * @param forcePackageListRefreshIn set true to request a package list refresh
     * @param stateNamesIn state module names to be applied to the server
     */
    public ApplyStatesEventMessage(long serverIdIn, Long userIdIn,
            boolean forcePackageListRefreshIn, String... stateNamesIn) {
        serverId = serverIdIn;
        userId = userIdIn;
        stateNames = Arrays.asList(stateNamesIn);
        forcePackageListRefresh = forcePackageListRefreshIn;
        txn = HibernateFactory.getSession().getTransaction();
    }

    /**
     * Return the server id.
     *
     * @return the server id
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * Return the list of states that need to be updated.
     *
     * @return the server id
     */
    public List<String> getStateNames() {
        return stateNames;
    }

    /**
     * Return true if a package list refresh is requested, otherwise false.
     *
     * @return true if a package list refresh is requested, otherwise false
     */
    public boolean isForcePackageListRefresh() {
        return forcePackageListRefresh;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return "ApplyStatesEventMessage[serverId: " + serverId + ", stateNames: " +
                stateNames.stream().collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public Transaction getTransaction() {
        return txn;
    }
}
