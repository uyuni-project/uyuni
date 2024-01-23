/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.reactor.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.reactor.SaltReactor;
import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.event.BeaconEvent;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class RebootInfoBeaconTest extends RhnJmockBaseTestCase {

    private SaltReactor reactor;
    private User user;
    private JsonParser<Event> eventParser;

    @BeforeEach
    public void setUp() {
        user = UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName());
        this.eventParser = new JsonParser<>(new TypeToken<>() {
        });
        SaltService saltService = createSaltService();
        SaltServerActionService saltServerActionService = createSaltServerActionService(saltService, saltService);
        SaltUtils saltUtils = new SaltUtils(saltService, saltService);
        CloudPaygManager paygMgr = new CloudPaygManager();
        reactor = new SaltReactor(
            saltService,
            saltService,
            saltServerActionService,
            saltUtils,
            paygMgr
        );
    }

    @Test
    public void testRebootInfoEvent() throws Exception {
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setMinionId("slemicro100001");
        minion1.setLastBoot(System.currentTimeMillis() / 1000);
        minion1.setRebootRequiredAfter(null);

        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setMinionId("slemicro100002");
        minion1.setLastBoot(System.currentTimeMillis() / 1000);
        minion2.setRebootRequiredAfter(null);

        // Event indicating that reboot is needed for minion 1
        BeaconEvent event = buildRebootInfoEvent(minion1.getMinionId(), true);
        reactor.eventToMessages(event);
        assertTrue(minion1.isRebootNeeded());
        assertFalse(minion2.isRebootNeeded());

        // Events indicating that reboot is needed for minion 2, but is not for minion 1
        event = buildRebootInfoEvent(minion2.getMinionId(), true);
        reactor.eventToMessages(event);
        event = buildRebootInfoEvent(minion1.getMinionId(), false);
        reactor.eventToMessages(event);

        assertFalse(minion1.isRebootNeeded());
        assertTrue(minion2.isRebootNeeded());
    }

    private BeaconEvent buildRebootInfoEvent(String minionId, boolean rebootNeeded) {
        StringBuilder jsonEvent = new StringBuilder();
        jsonEvent.append("{");
        jsonEvent.append("  \"tag\": \"salt/beacon/" + minionId + "/reboot_info/1234\", ");
        jsonEvent.append("  \"data\": { ");
        jsonEvent.append("    \"reboot_needed\": " + rebootNeeded);
        jsonEvent.append("   } ");
        jsonEvent.append("}");
        Event e = eventParser.parse(jsonEvent.toString());
        return BeaconEvent.parse(e).get();
    }

    private SaltService createSaltService() {
        return new SaltService() {
            @Override
            public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
                return Optional.of(Result.success(new JsonObject()));
            }

            @Override
            public void refreshPillar(MinionList minionList) {
            }
        };
    }

    private SaltServerActionService createSaltServerActionService(SystemQuery systemQuery, SaltApi saltApi) {
        SaltUtils saltUtils = new SaltUtils(systemQuery, saltApi);
        SaltServerActionService service = new SaltServerActionService(saltApi, saltUtils, new SaltKeyUtils(saltApi));
        service.setSkipCommandScriptPerms(true);
        return service;
    }
}

