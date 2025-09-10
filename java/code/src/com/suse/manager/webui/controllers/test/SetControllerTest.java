/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.manager.webui.controllers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.SetLabels;
import com.redhat.rhn.frontend.struts.SessionSetHelper;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.SparkTestUtils;

import com.suse.manager.webui.controllers.SetController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import spark.Request;

public class SetControllerTest extends BaseControllerTestCase {

    private static final Gson GSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                                                      .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                                                      .create();


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        initializeSet(SetLabels.SYSTEM_LIST, user);
    }

    @Test
    public void canUpdateExistingDeclSet() throws UnsupportedEncodingException {
        Server s0 = ServerFactoryTest.createTestServer(user, true);
        Server s1 = ServerFactoryTest.createTestServer(user, true);
        Server s2 = ServerFactoryTest.createTestServer(user, true);

        RhnSetDecl testSetDecl = initializeSet(SetLabels.SYSTEM_LIST, user, s0.getId(), s1.getId());

        Request request = SparkTestUtils.createMockRequestWithBodyAndParams(
            "/manager/api/sets/:label",
            Map.of(),
            GSON.toJson(Map.of(s1.getId().toString(), false, s2.getId().toString(), true)),
            "system_list"
        );

        RhnMockHttpServletResponse mockResponse = (RhnMockHttpServletResponse) response.raw();

        mockResponse.setContentType("application/json");

        String result = SetController.updateSet(request, response, user);

        assertEquals("2", result);
        assertEquals(2, testSetDecl.get(user).size());
        assertTrue(testSetDecl.get(user).contains(s0.getId()));
        assertTrue(testSetDecl.get(user).contains(s2.getId()));
    }

    @Test
    public void canClearExistingDeclSet() {
        Server s0 = ServerFactoryTest.createTestServer(user, true);
        Server s1 = ServerFactoryTest.createTestServer(user, true);
        Server s2 = ServerFactoryTest.createTestServer(user, true);

        RhnSetDecl testSetDecl = initializeSet(SetLabels.SYSTEM_LIST, user, s0.getId(), s1.getId());

        Request request = SparkTestUtils.createMockRequestWithParams(
            "/manager/api/sets/:label/clear",
            Map.of(),
            "system_list"
        );

        RhnMockHttpServletResponse mockResponse = (RhnMockHttpServletResponse) response.raw();

        mockResponse.setContentType("application/json");

        String result = SetController.clearSet(request, response, user);

        assertEquals("0", result);
        assertEquals(0, testSetDecl.get(user).size());
    }

    @Test
    public void canUpdatedExistingSessionSet() throws UnsupportedEncodingException {
        Request request = SparkTestUtils.createMockRequestWithBodyAndParams(
            "/manager/api/sets/:label",
            Map.of(),
            GSON.toJson(Map.of("selection-key-1", false, "selection-key-2", true)),
            "list-selection"
        );

        RhnMockHttpServletResponse mockResponse = (RhnMockHttpServletResponse) response.raw();

        mockResponse.setContentType("application/json");

        Set<String> selectionList = SessionSetHelper.lookupAndBind(request.raw(), "list-selection");
        selectionList.add("selection-key-0");
        selectionList.add("selection-key-1");

        String result = SetController.updateSet(request, response, user);

        assertEquals("2", result);
        assertEquals(Set.of("selection-key-0", "selection-key-2"), selectionList);
    }

    @Test
    public void canClearExistingSessionSet() {
        Request request = SparkTestUtils.createMockRequestWithParams(
            "/manager/api/sets/:label/clear",
            Map.of(),
            "list-selection"
        );

        RhnMockHttpServletResponse mockResponse = (RhnMockHttpServletResponse) response.raw();

        mockResponse.setContentType("application/json");

        Set<String> selectionList = SessionSetHelper.lookupAndBind(request.raw(), "list-selection");
        selectionList.add("selection-key-0");
        selectionList.add("selection-key-1");

        String result = SetController.clearSet(request, response, user);

        assertEquals("0", result);
        assertEquals(Set.of(), selectionList);
    }
    @Test
    public void returnsErrorWhenUpdatingNonExistingSet() throws UnsupportedEncodingException {
        Request request = SparkTestUtils.createMockRequestWithBodyAndParams(
            "/manager/api/sets/:label",
            Map.of(),
            GSON.toJson(Map.of("selection-key-1", true, "selection-key-2", false)),
            "nonExisting"
        );

        RhnMockHttpServletResponse mockResponse = (RhnMockHttpServletResponse) response.raw();

        mockResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        mockResponse.setContentType("application/json");

        String result = SetController.updateSet(request, response, user);

        assertEquals(GSON.toJson(Map.of("messages", List.of("Failed to change set"))), result);
    }

    @Test
    public void returnsErrorWhenClearingNonExistingSet() {
        Request request = SparkTestUtils.createMockRequestWithParams(
            "/manager/api/sets/:label",
            Map.of(),
            "nonExisting"
        );

        RhnMockHttpServletResponse mockResponse = (RhnMockHttpServletResponse) response.raw();

        mockResponse.setStatus(HttpStatus.SC_NOT_FOUND);
        mockResponse.setContentType("application/json");

        String result = SetController.clearSet(request, response, user);

        assertEquals(GSON.toJson(Map.of("error", "No such set: nonExisting")), result);
    }

    private static RhnSetDecl initializeSet(String label, User user, Long... values) {
        RhnSetDecl testSetDecl = RhnSetDecl.find(label);
        RhnSet rhnSet = testSetDecl.get(user);

        rhnSet.clear();
        if (values != null && values.length != 0) {
            Stream.of(values).forEach(rhnSet::addElement);
        }

        RhnSetManager.store(rhnSet);
        return testSetDecl;
    }
}
