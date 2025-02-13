/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.domain.action.test;

import static com.redhat.rhn.domain.action.ActionFactory.TYPE_PROXY_CONFIGURATION_APPLY;
import static com.suse.proxy.ProxyConfigUtils.EMAIL_FIELD;
import static com.suse.proxy.ProxyConfigUtils.MAX_CACHE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PARENT_FQDN_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PILLAR_CATEGORY;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PORT_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.action.ProxyConfigurationApplyAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.salt.netapi.calls.LocalCall;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ProxyConfigurationApplyActionTest extends RhnBaseTestCase {

    @Test
    public void testGetApplyProxyConfigAction() {
        final Map<String, Object> pillarMap = Map.of(
                PROXY_PORT_FIELD, "3128",
                MAX_CACHE_FIELD, "456",
                PARENT_FQDN_FIELD, "parent.suse.com",
                EMAIL_FIELD, "admin@suse.com"
        );
        final Map<String, Object> proxyConfigFilesMap = Map.of(
                "some", "more",
                "additional", "configs"
        );
        final User user = UserTestUtils.createUser("testUser",
                UserTestUtils.createOrg("testOrg" + this.getClass().getSimpleName()));
        final Pillar pillar = new Pillar(PROXY_PILLAR_CATEGORY, pillarMap);
        final ProxyConfigurationApplyAction action =
                new ProxyConfigurationApplyAction(pillar, proxyConfigFilesMap, user.getOrg());

        //
        assertEquals(TYPE_PROXY_CONFIGURATION_APPLY, action.getActionType());
        assertEquals(proxyConfigFilesMap, action.getProxyConfigFiles());

        Pillar actionPillar = action.getPillar();
        assertNotNull(actionPillar);
        assertEquals(pillarMap, actionPillar.getPillar());
        assertEquals(PROXY_PILLAR_CATEGORY, actionPillar.getCategory());

        //
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        MinionSummary testMinionSummary = new MinionSummary(testMinionServer);

        Map<LocalCall<?>, List<MinionSummary>> result = action.getApplyProxyConfigAction(List.of(testMinionSummary));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMinionSummary, result.values().iterator().next().get(0));

        // check pillar result (from call) contains all the values provided in pillarMap and proxyConfigFilesMap
        LocalCall<?> call = result.keySet().iterator().next();
        assertNotNull(call);

        Object kwarg = call.getPayload().get("kwarg");
        assertNotNull(kwarg);
        assertInstanceOf(Map.class, kwarg);

        Object pillarObj = ((Map<?, ?>) kwarg).get("pillar");
        assertNotNull(pillarObj);
        assertInstanceOf(Map.class, pillarObj);

        Map<String, Object> pillarFromCall = (Map<String, Object>) pillarObj;
        pillarMap.forEach((key, value) -> assertEquals(value, pillarFromCall.get(key)));
        proxyConfigFilesMap.forEach((key, value) -> assertEquals(value, pillarFromCall.get(key)));
    }
}
