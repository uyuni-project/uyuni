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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * JSON representation of a Salt Minion.
 */
public class SaltMinionJson {
    public static final String STATE_ACCEPTED = "accepted";
    public static final String STATE_PENDING = "pending";
    public static final String STATE_REJECTED = "rejected";
    public static final String STATE_DENIED = "denied";

    private String id;
    private String fingerprint;
    private String state;
    private Long sid;

    /**
     * All-arg constructor for SaltMinionJson.
     *
     * @param idIn minion id
     * @param fingerprintIn minion fingerprint
     * @param stateIn minion state
     * @param sidIn associated server id (only if exists, null otherwise)
     */
    public SaltMinionJson(String idIn, String fingerprintIn, String stateIn, Long sidIn) {
        this.id = idIn;
        this.fingerprint = fingerprintIn;
        this.state = stateIn;
        this.sid = sidIn;
    }

    private static Stream<SaltMinionJson> fromFingerprints(Map<String, String> fingerprints,
        Map<String, Long> serverIds, String state, Predicate<String> isVisible) {
        return fingerprints.entrySet().stream()
                .filter(s -> isVisible.test(s.getKey()))
                .map(m -> new SaltMinionJson(m.getKey(), m.getValue(), state,
                        serverIds.get(m.getKey())));
    }

    /**
     * Creates a list of {@link SaltMinionJson} objects from a {@link Key.Fingerprints}
     * instance.
     *
     * @param fp result of a {@code salt.wheel.key.finger} call
     * @param isVisible predicate to check if minion is visible
     * @param sids map of sids with minion ids as keys, used to associate mininons to
     *             corresponding server records
     * @return a list of Salt minions
     */
    public static List<SaltMinionJson> fromFingerprints(Key.Fingerprints fp,
                                                        Map<String, Long> sids, Predicate<String> isVisible) {
        return Stream.of(
                fromFingerprints(fp.getMinions(), sids, STATE_ACCEPTED, isVisible),
                fromFingerprints(fp.getDeniedMinions(), sids, STATE_DENIED, isVisible),
                fromFingerprints(fp.getRejectedMinions(), sids, STATE_REJECTED, isVisible),
                fromFingerprints(fp.getUnacceptedMinions(), sids, STATE_PENDING, isVisible)
        ).flatMap(Function.identity()).collect(Collectors.toList());
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
