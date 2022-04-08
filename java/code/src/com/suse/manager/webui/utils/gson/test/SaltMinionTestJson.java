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

package com.suse.manager.webui.utils.gson.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.webui.utils.gson.SaltMinionJson;
import com.suse.salt.netapi.calls.wheel.Key;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SaltMinionTestJson extends MockObjectTestCase {

    @BeforeEach
    public void setUp() throws Exception {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void testFromFingerprints() {
        Map<String, String> accepted = new HashMap<>();
        accepted.put("m1", "fingerprint1");
        accepted.put("m2", "fingerprint2");

        Map<String, String> pending = new HashMap<>();
        pending.put("m3", "fingerprint3");

        Map<String, String> denied = new HashMap<>();
        denied.put("m4", "fingerprint4");

        Key.Fingerprints fingerprints = context().mock(Key.Fingerprints.class);
        context().checking(new Expectations() {{
            oneOf(fingerprints).getMinions(); will(returnValue(accepted));
            oneOf(fingerprints).getUnacceptedMinions(); will(returnValue(pending));
            oneOf(fingerprints).getDeniedMinions(); will(returnValue(denied));
            oneOf(fingerprints).getRejectedMinions(); will(returnValue(new HashMap<>()));
        }});

        Map<String, Long> visibleToUser = new HashMap<>();
        visibleToUser.put("m1", 1L); //registered and visible
        visibleToUser.put("m2", 2L); //registered and visible

        Map<String, Long> registered = new HashMap<>();
        registered.put("m1", 1L);
        registered.put("m2", 2L);
        registered.put("m4", 4L); // registered not visible

        Predicate<String> isVisible = (minionId) -> visibleToUser.containsKey(minionId) ||
                !registered.containsKey(minionId);

        List<SaltMinionJson> minions = SaltMinionJson.fromFingerprints(fingerprints, visibleToUser, isVisible);

        assertNotNull(minions);
        assertEquals(3, minions.size());

        Map<String, SaltMinionJson> minionMap = minions.stream()
                .collect(Collectors.toMap(SaltMinionJson::getId, Function.identity()));

        SaltMinionJson minion = minionMap.get("m1");
        assertEquals("fingerprint1", minion.getFingerprint());
        assertEquals(1L, (long)minion.getSid());
        assertEquals("accepted", minion.getState());

        minion = minionMap.get("m2");
        assertEquals("fingerprint2", minion.getFingerprint());
        assertEquals(2L, (long)minion.getSid());
        assertEquals("accepted", minion.getState());

        minion = minionMap.get("m3");
        assertEquals("fingerprint3", minion.getFingerprint());
        assertNull(minion.getSid());
        assertEquals("pending", minion.getState());

        assertNull(minionMap.get("m4"));
    }
}
