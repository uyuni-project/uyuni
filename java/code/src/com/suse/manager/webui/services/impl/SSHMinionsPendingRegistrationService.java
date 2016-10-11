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

import java.util.HashSet;
import java.util.Set;

/**
 * POC
 * in-memory database of minions pending registration
 * todo: move to database and come up with a sane name!
 */
public class SSHMinionsPendingRegistrationService {

    private static Set<String> minionIds = new HashSet<>();

    /**
     * Prevent instantiation.
     */
    private SSHMinionsPendingRegistrationService() {
    }

    /**
     * Adds minion id to the database.
     * @param minionId minion id to be added
     */
    public static void addMinion(String minionId) {
        minionIds.add(minionId);
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
        return minionIds.contains(minionId);
    }
}
