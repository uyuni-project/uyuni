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

import static com.suse.proxy.ProxyConfigUtils.EMAIL_FIELD;
import static com.suse.proxy.ProxyConfigUtils.MAX_CACHE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PARENT_FQDN_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PILLAR_CATEGORY;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PORT_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.action.ProxyConfigurationApplyAction;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.testing.RhnBaseTestCase;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.utils.Xor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * ProxyConfigurationApplyActionTest
 */
public class ProxyConfigurationApplyActionTest extends RhnBaseTestCase {

    private final Org org = new Org();
    private Pillar pillar;
    private Map<String, Object> proxyConfigFiles;
    private ProxyConfigurationApplyAction action;

    @Override
    @BeforeEach
    public void setUp() {
        pillar = new Pillar(PROXY_PILLAR_CATEGORY, new HashMap<>());
        proxyConfigFiles = new HashMap<>();
        action = new ProxyConfigurationApplyAction(pillar, proxyConfigFiles, org);
    }

    @Test
    public void testGetPillar() {
        assertEquals(pillar, action.getPillar());
    }

    @Test
    public void testGetProxyConfigFiles() {
        assertEquals(proxyConfigFiles, action.getProxyConfigFiles());
    }

    @Test
    public void testGetApplyProxyConfigCall() {
        final Map<String, Object> pillarMap = Map.of(
                PROXY_PORT_FIELD, "3128",
                MAX_CACHE_FIELD, "456",
                PARENT_FQDN_FIELD, "parent.suse.com",
                EMAIL_FIELD, "admin@suse.com"
        );

        this.proxyConfigFiles = Map.of(
                "some", "more",
                "additional", "configs"
        );

        this.pillar = new Pillar(PROXY_PILLAR_CATEGORY, pillarMap);
        this.action = new ProxyConfigurationApplyAction(pillar, proxyConfigFiles, org);

        //
        Pillar actionPillar = this.action.getPillar();
        assertNotNull(actionPillar);
        assertEquals(pillarMap, actionPillar.getPillar());
        assertEquals(PROXY_PILLAR_CATEGORY, actionPillar.getCategory());

        LocalCall<Xor<String, Map<String, State.ApplyResult>>> call = this.action.getApplyProxyConfigCall();
        assertNotNull(call);

        Object kwarg = call.getPayload().get("kwarg");
        assertNotNull(kwarg);
        assertInstanceOf(Map.class, kwarg);

        Object pillarObj = ((Map<?, ?>) kwarg).get("pillar");
        assertNotNull(pillarObj);
        assertInstanceOf(Map.class, pillarObj);

        Map<String, Object> pillarFromCall = (Map<String, Object>) pillarObj;
        pillarMap.forEach((key, value) -> assertEquals(value, pillarFromCall.get(key)));
        this.proxyConfigFiles.forEach((key, value) -> assertEquals(value, pillarFromCall.get(key)));

    }

    @Test
    public void testEqualsAndHashCode() {
        ProxyConfigurationApplyAction sameAction = new ProxyConfigurationApplyAction(pillar, proxyConfigFiles, org);
        assertEquals(action, sameAction);
        assertEquals(action.hashCode(), sameAction.hashCode());

        ProxyConfigurationApplyAction differentAction = new ProxyConfigurationApplyAction(
                new Pillar("different", new HashMap<>()), new HashMap<>(), new Org()
        );
        assertNotEquals(action, differentAction);
        assertNotEquals(action.hashCode(), differentAction.hashCode());
    }

}
