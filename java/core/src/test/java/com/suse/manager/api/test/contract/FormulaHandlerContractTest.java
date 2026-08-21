/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.dto.FormulaData;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FormulaHandlerContractTest extends BaseOpenApiTest {

    private static final String FORMULA_NAME = "locale";
    private static final Integer SYSTEM_ID = 1000010000;
    private static final Integer GROUP_ID = 42;

    @Override
    protected String getApiNamespace() {
        return "formula";
    }

    @Override
    protected Class<FormulaHandler> getHandlerClass() {
        return FormulaHandler.class;
    }

    private FormulaHandler handler() {
        return (FormulaHandler) handlerMock;
    }

    private Map<String, Object> formulaValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("timezone_name", "Europe/Berlin");
        values.put("hardware_clock_set_to_utc", true);
        return values;
    }

    @Test
    public void testListFormulas() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listFormulas(with(mockUser));
            will(returnValue(List.of("locale", "branch-network")));
        }});

        validateApiContract("/formula/listFormulas", "GET")
                .onHandlerMethod("listFormulas", User.class);
    }

    @Test
    public void testGetFormulasByGroupId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getFormulasByGroupId(with(mockUser), with(GROUP_ID));
            will(returnValue(List.of("locale")));
        }});

        validateApiContract("/formula/getFormulasByGroupId", "GET")
                .withParams(Map.of("systemGroupId", new String[] {GROUP_ID.toString()}))
                .onHandlerMethod("getFormulasByGroupId", User.class, Integer.class);
    }

    @Test
    public void testGetFormulasByServerId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getFormulasByServerId(with(mockUser), with(SYSTEM_ID));
            will(returnValue(List.of("locale")));
        }});

        validateApiContract("/formula/getFormulasByServerId", "GET")
                .withParams(Map.of("sid", new String[] {SYSTEM_ID.toString()}))
                .onHandlerMethod("getFormulasByServerId", User.class, Integer.class);
    }

    @Test
    public void testGetCombinedFormulasByServerId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getCombinedFormulasByServerId(with(mockUser), with(SYSTEM_ID));
            will(returnValue(List.of("locale", "branch-network")));
        }});

        validateApiContract("/formula/getCombinedFormulasByServerId", "GET")
                .withParams(Map.of("sid", new String[] {SYSTEM_ID.toString()}))
                .onHandlerMethod("getCombinedFormulasByServerId", User.class, Integer.class);
    }

    @Test
    public void testGetSystemFormulaData() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getSystemFormulaData(with(mockUser), with(SYSTEM_ID), with(FORMULA_NAME));
            will(returnValue(formulaValues()));
        }});

        validateApiContract("/formula/getSystemFormulaData", "GET")
                .withParams(Map.of(
                        "systemId", new String[] {SYSTEM_ID.toString()},
                        "formulaName", new String[] {FORMULA_NAME}))
                .onHandlerMethod("getSystemFormulaData", User.class, Integer.class, String.class);
    }

    @Test
    public void testGetGroupFormulaData() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getGroupFormulaData(with(mockUser), with(GROUP_ID), with(FORMULA_NAME));
            will(returnValue(formulaValues()));
        }});

        validateApiContract("/formula/getGroupFormulaData", "GET")
                .withParams(Map.of(
                        "groupId", new String[] {GROUP_ID.toString()},
                        "formulaName", new String[] {FORMULA_NAME}))
                .onHandlerMethod("getGroupFormulaData", User.class, Integer.class, String.class);
    }

    @Test
    public void testGetCombinedFormulaDataByServerIds() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getCombinedFormulaDataByServerIds(with(mockUser), with(FORMULA_NAME),
                    with(any(List.class)));
            will(returnValue(List.of(new FormulaData(SYSTEM_ID.longValue(), "minion-1", formulaValues()))));
        }});

        validateApiContract("/formula/getCombinedFormulaDataByServerIds", "GET")
                .withParams(Map.of(
                        "formulaName", new String[] {FORMULA_NAME},
                        "sids", new String[] {SYSTEM_ID.toString()}))
                .onHandlerMethod("getCombinedFormulaDataByServerIds", User.class, String.class, List.class);
    }

    @Test
    public void testSetFormulasOfGroup() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemGroupId", GROUP_ID);
        body.put("formulas", List.of("locale"));

        context.checking(new Expectations() {{
            oneOf(handler()).setFormulasOfGroup(with(mockUser), with(GROUP_ID), with(any(List.class)));
            will(returnValue(1));
        }});

        validateApiContract("/formula/setFormulasOfGroup", "POST")
                .withBody(body)
                .onHandlerMethod("setFormulasOfGroup", User.class, Integer.class, List.class);
    }

    @Test
    public void testSetFormulasOfServer() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemId", SYSTEM_ID);
        body.put("formulas", List.of("locale"));

        context.checking(new Expectations() {{
            oneOf(handler()).setFormulasOfServer(with(mockUser), with(SYSTEM_ID), with(any(List.class)));
            will(returnValue(1));
        }});

        validateApiContract("/formula/setFormulasOfServer", "POST")
                .withBody(body)
                .onHandlerMethod("setFormulasOfServer", User.class, Integer.class, List.class);
    }

    @Test
    public void testSetSystemFormulaData() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemId", SYSTEM_ID);
        body.put("formulaName", FORMULA_NAME);
        body.put("content", formulaValues());

        context.checking(new Expectations() {{
            oneOf(handler()).setSystemFormulaData(with(mockUser), with(SYSTEM_ID), with(FORMULA_NAME),
                    with(any(Map.class)));
            will(returnValue(1));
        }});

        validateApiContract("/formula/setSystemFormulaData", "POST")
                .withBody(body)
                .onHandlerMethod("setSystemFormulaData", User.class, Integer.class, String.class, Map.class);
    }

    @Test
    public void testSetGroupFormulaData() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("groupId", GROUP_ID);
        body.put("formulaName", FORMULA_NAME);
        body.put("content", formulaValues());

        context.checking(new Expectations() {{
            oneOf(handler()).setGroupFormulaData(with(mockUser), with(GROUP_ID), with(FORMULA_NAME),
                    with(any(Map.class)));
            will(returnValue(1));
        }});

        validateApiContract("/formula/setGroupFormulaData", "POST")
                .withBody(body)
                .onHandlerMethod("setGroupFormulaData", User.class, Integer.class, String.class, Map.class);
    }
}
