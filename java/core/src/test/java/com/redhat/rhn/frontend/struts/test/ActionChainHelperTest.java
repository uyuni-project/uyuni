/*
 * Copyright (c) 2014 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
/**
 * Copyright (c) 2014 Red Hat, Inc.
 */
package com.redhat.rhn.frontend.struts.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.TestUtils;

import com.suse.utils.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.struts.action.DynaActionForm;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Silvio Moioli {@literal <smoioli@suse.de>}
 */
public class ActionChainHelperTest extends BaseTestCaseWithUser {

    /**
     * Tests readActionChain().
     */
    @Test
    public void testReadActionChain() {
        ActionChain chain = ActionChainFactory.createActionChain(TestUtils.randomString(),
            user);

        // poor-man's DynaActionForm mocking
        final Map<String, Object> formMap = new HashMap<>();
        DynaActionForm form = new DynaActionForm() {
            @Override
            public Object get(String nameIn) {
                return formMap.get(nameIn);
            }
        };

        assertNull(ActionChainHelper.readActionChain(form, user));

        formMap.put(DatePicker.SCHEDULE_TYPE, DatePicker.ScheduleType.ACTION_CHAIN.asString());
        formMap.put(ActionChainHelper.LABEL_PROPERTY_NAME, chain.getLabel());

        ActionChain retrievedChain = ActionChainHelper.readActionChain(form, user);
        assertNotNull(retrievedChain);
        assertEquals(chain.getId(), retrievedChain.getId());

        formMap.put(ActionChainHelper.LABEL_PROPERTY_NAME, TestUtils.randomString());
        ActionChain newChain = ActionChainHelper.readActionChain(form, user);
        assertNotNull(newChain);
        assertFalse(chain.getId().equals(newChain.getId()));
    }

    /**
     * Tests prepopulateActionChains().
     */
    @Test
    public void testPrepopulateActionChains() {
        RhnMockHttpServletRequest request = TestUtils.getRequestWithSessionAndUser();
        user = new RequestContext(request).getCurrentUser();

        List<ActionChain> actionChains = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            actionChains.add(ActionChainFactory.createActionChain(TestUtils.randomString(),
                user));
        }

        List<Map<String, String>> result = new LinkedList<>();

        for (ActionChain actionChain : ActionChainFactory.getActionChains(user)) {
            Map<String, String> map = new HashMap<>();
            map.put("id", actionChain.getLabel());
            map.put("text", actionChain.getLabel());
            result.add(map);
        }

        ActionChainHelper.prepopulateActionChains(request);

        Object attribute = request.getAttribute(ActionChainHelper.EXISTING_ACTION_CHAINS_PROPERTY_NAME);
        assertNotNull(attribute);
        assertInstanceOf(String.class, attribute);

        // assert jsons are equivalent
        JsonArray arr1 = JsonParser.parseString(Json.GSON.toJson(result)).getAsJsonArray();
        JsonArray arr2 = JsonParser.parseString((String)attribute).getAsJsonArray();

        Set<JsonElement> set1 = new HashSet<>();
        arr1.forEach(set1::add);
        Set<JsonElement> set2 = new HashSet<>();
        arr2.forEach(set2::add);
        assertEquals(set1, set2);
    }

}
