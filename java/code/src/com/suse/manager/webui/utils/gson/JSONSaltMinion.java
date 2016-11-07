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

package com.suse.manager.webui.utils.gson;

import com.suse.salt.netapi.calls.wheel.Key;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

/**
 * JSON representation of a Salt Minion.
 */
public class JSONSaltMinion {
    public static final String STATE_ACCEPTED = "accepted";
    public static final String STATE_PENDING = "pending";
    public static final String STATE_REJECTED = "rejected";
    public static final String STATE_DENIED = "denied";

    private String id;
    private String fingerprint;
    private String state;
    private Long sid;

    /**
     * All-arg constructor for JSONSaltMinion.
     *
     * @param idIn minion id
     * @param fingerprintIn minion fingerprint
     * @param stateIn minion state
     * @param sidIn associated server id (only if exists, null otherwise)
     */
    public JSONSaltMinion(String idIn, String fingerprintIn, String stateIn, Long sidIn) {
        this.id = idIn;
        this.fingerprint = fingerprintIn;
        this.state = stateIn;
        this.sid = sidIn;
    }

    private static Stream<JSONSaltMinion> fromFingerprints(Map<String, String> fingerprints,
            Map<String, Long> serverIds, String state) {

        return fingerprints.entrySet().stream()
                .map(m -> new JSONSaltMinion(m.getKey(), m.getValue(), state,
                        serverIds.get(m.getKey())));
    }

    /**
     * Creates a list of {@link JSONSaltMinion} objects from a {@link Key.Fingerprints}
     * instance.
     *
     * @param fingerprints result of a {@code salt.wheel.key.finger} call
     * @param sids map of sids with minion ids as keys, used to associate mininons to
     *             corresponding server records
     * @return a list of Salt minions
     */
    public static List<JSONSaltMinion> fromFingerprints(Key.Fingerprints fingerprints,
            Map<String, Long> sids) {

        Stream<JSONSaltMinion> minionList = fromFingerprints(fingerprints.getMinions(),
                sids, STATE_ACCEPTED);

        minionList = concat(minionList, fromFingerprints(fingerprints.getDeniedMinions(),
                sids, STATE_DENIED));

        minionList = concat(minionList, fromFingerprints(fingerprints.getRejectedMinions(),
                sids, STATE_REJECTED));

        minionList = concat(minionList, fromFingerprints(
                fingerprints.getUnacceptedMinions(), sids, STATE_PENDING));

        return minionList.collect(Collectors.toList());
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets fingerprint.
     *
     * @return the fingerprint
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Gets sid.
     *
     * @return the sid
     */
    public Long getSid() {
        return sid;
    }
}
