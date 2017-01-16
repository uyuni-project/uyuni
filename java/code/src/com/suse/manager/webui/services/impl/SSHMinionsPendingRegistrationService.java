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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * POC
 * in-memory database of minions pending registration
 * todo: move to database and come up with a sane name!
 */
public class SSHMinionsPendingRegistrationService {

    /**
     * Information about the minion to bootstrap
     * (contect method, proxy path)
     */
    public static class PendingMinion {

        private String contactMethod;

        private Optional<List<String>> proxyPath;

        /**
         * @param contactMethodIn the contect method
         * @param proxyPathIn the proxy path
         */
        public PendingMinion(String contactMethodIn, Optional<List<String>> proxyPathIn) {
            this.contactMethod = contactMethodIn;
            this.proxyPath = proxyPathIn;
        }

        /**
         * @return the contect method
         */
        public String getContactMethod() {
            return contactMethod;
        }

        /**
         * @return the proxy path
         */
        public Optional<List<String>> getProxyPath() {
            return proxyPath;
        }
    }


    private static Map<String, PendingMinion> minionIds = new ConcurrentHashMap<>();

    /**
     * Prevent instantiation.
     */
    private SSHMinionsPendingRegistrationService() {
    }

    /**
     * Adds minion id to the database.
     * @param minionId minion id to be added
     * @param contactMethod the contact method of the minion
     * @param proxyPath list of proxies hostnames in the order they connect through
     */
    public static void addMinion(String minionId, String contactMethod,
                                 Optional<List<String>> proxyPath) {
        minionIds.put(minionId, new PendingMinion(contactMethod, proxyPath));
    }

    /**
     * Remove minion id from the database.
     * @param minionId minion id to be removed
     */
    public static void removeMinion(String minionId) {
        minionIds.remove(minionId);
    }

    /**
     * Checks whether minion id is contained in the db.
     * @param minionId minion id to be checked for.
     * @return true if the minion is in the database, false otherwise.
     */
    public static boolean containsMinion(String minionId) {
        return minionIds.containsKey(minionId);
    }

    /**
     * @param minionId the minion id
     * @return the {@link PendingMinion} if any for the given id
     */
    public static Optional<PendingMinion> get(String minionId) {
        return Optional.ofNullable(minionIds.get(minionId));
    }

    /**
     * Get all minion ids in the database.
     * @return all minion ids
     */
    public static Map<String, PendingMinion> getMinions() {
        return Collections.unmodifiableMap(minionIds);
    }

    /**
     * @param minionId the minion id
     * @return contact method of the given minion
     */
    public static Optional<String> getContactMethod(String minionId) {
        return Optional.ofNullable(minionIds.get(minionId))
                .map(minion -> minion.getContactMethod());
    }

}
