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

package com.suse.manager.webui.services.impl;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.utils.ContactMethodUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory database of minions pending registration
 */
public class MinionPendingRegistrationService {

    /**
     * Information about the minion to bootstrap
     * (contact method, creator user, proxy path and ssh port)
     */
    public static class PendingMinion {

        private final String contactMethod;
        private final User creator;
        private final List<String> proxyPath;
        private final Integer sshPushPort;

        /**
         * @param creatorIn user who accepted the key or bootstrapped the system
         * @param contactMethodIn the contact method
         */
        public PendingMinion(User creatorIn, String contactMethodIn) {
            this(creatorIn, contactMethodIn, Collections.emptyList(), null);
        }

        /**
         * @param creatorIn user who accepted the key or bootstrapped the system
         * @param contactMethodIn the contact method
         * @param proxyPathIn the proxy path
         * @param portIn the ssh port
         */
        public PendingMinion(User creatorIn, String contactMethodIn, List<String> proxyPathIn, Integer portIn) {
            this.contactMethod = contactMethodIn;
            this.creator = creatorIn;
            this.proxyPath = proxyPathIn;
            this.sshPushPort = portIn;
        }

        /**
         * @return the contact method
         */
        public String getContactMethod() {
            return contactMethod;
        }

        /**
         * @return the creator
         */
        public User getCreator() {
            return creator;
        }

        /**
         * @return the proxy path
         */
        public List<String> getProxyPath() {
            return proxyPath;
        }

        /**
         * @return an optional wrapping the ssh port or empty if not supported.
         */
        public Optional<Integer> getSSHPushPort() {
            return Optional.ofNullable(sshPushPort);
        }
    }


    private static Map<String, PendingMinion> minionIds = new ConcurrentHashMap<>();

    /**
     * Prevent instantiation.
     */
    private MinionPendingRegistrationService() {
    }

    /**
     * Adds minion id to the database.
     * @param creator user who accepted the key or bootstrapped the system
     * @param minionId minion id to be added
     * @param contactMethod the contact method of the minion
     */
    public static void addMinion(User creator, String minionId, String contactMethod) {
        minionIds.put(minionId, new PendingMinion(creator, contactMethod));
    }

    /**
     * Adds minion id to the database.
     * @param creator user who accepted the key or bootstrapped the system
     * @param minionId minion id to be added
     * @param contactMethod the contact method of the minion
     * @param proxyPath list of proxies hostnames in the order they connect through
     * @param sshPort the ssh port for ssh only minions
     */
    public static void addMinion(User creator, String minionId, String contactMethod, List<String> proxyPath,
                                 Integer sshPort) {
        minionIds.put(minionId, new PendingMinion(creator, contactMethod, proxyPath, sshPort));
    }

    /**
     * Remove minion id from the database.
     * @param minionId minion id to be removed
     */
    public static void removeMinion(String minionId) {
        minionIds.remove(minionId);
    }

    /**
     * Checks whether minion id is contained in the db with default contact method.
     * @param minionId minion id to be checked for.
     * @return true if the minion is in the database, false otherwise.
     */
    public static boolean containsMinion(String minionId) {
        return minionIds.containsKey(minionId) && minionIds.get(minionId)
                .contactMethod.equals(ContactMethodUtil.DEFAULT);
    }

    /**
     * Checks whether minion id is contained in the db with SSH contact method.
     * @param minionId minion id to be checked for.
     * @return true if the minion is in the database, false otherwise.
     */
    public static boolean containsSSHMinion(String minionId) {
        return minionIds.containsKey(minionId) && ContactMethodUtil
                .isSSHPushContactMethod(minionIds.get(minionId).contactMethod);
    }

    /**
     * @param minionId the minion id
     * @return the {@link PendingMinion} if any for the given id
     */
    public static Optional<PendingMinion> get(String minionId) {
        return Optional.ofNullable(minionIds.get(minionId));
    }

    /**
     * @param minionId the minion id
     * @return creator of the given minion
     */
    public static Optional<User> getCreator(String minionId) {
        return Optional.ofNullable(minionIds.get(minionId))
                .map(minion -> minion.getCreator());
    }

    /**
     * Get all SSH minion ids in the database.
     * @return all SSH minion ids
     */
    public static Map<String, PendingMinion> getSSHMinions() {
        return Collections.unmodifiableMap(minionIds.entrySet().stream()
                .filter(e -> ContactMethodUtil.isSSHPushContactMethod(
                        e.getValue().contactMethod))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

}
