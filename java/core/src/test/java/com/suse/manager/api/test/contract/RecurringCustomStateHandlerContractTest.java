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

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecurringCustomStateHandlerContractTest extends BaseOpenApiTest {

    private static final Integer ACTION_ID = 10;

    @Override
    protected String getApiNamespace() {
        return "recurring.custom";
    }

    @Override
    protected Class<RecurringCustomStateHandler> getHandlerClass() {
        return RecurringCustomStateHandler.class;
    }

    private RecurringCustomStateHandler handler() {
        return (RecurringCustomStateHandler) handlerMock;
    }

    /**
     * The documented property names are snake_case, so the map the handler receives uses them
     * rather than the JavaBeans names of the request interface.
     *
     * @return the action properties for a create call
     */
    private Map<String, Object> createProps() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("entity_type", "minion");
        props.put("entity_id", 1000010000);
        props.put("name", "test-custom-state-action");
        props.put("cron_expr", "0 0 3 ? * *");
        props.put("states", List.of("apache", "postfix"));
        props.put("test", false);
        return props;
    }

    @Test
    public void testCreate() throws Exception {
        var props = createProps();

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(props));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/recurring.custom/create", "POST")
                .withBody(Map.of("actionProps", props))
                .onHandlerMethod("create", User.class, Map.class);
    }

    /**
     * test is documented as optional, so a request that leaves it out must still satisfy the
     * documented schema.
     */
    @Test
    public void testCreateWithRequiredPropsOnly() throws Exception {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("entity_type", "org");
        props.put("entity_id", 1000010000);
        props.put("name", "test-custom-state-action");
        props.put("cron_expr", "0 0 3 ? * *");
        props.put("states", List.of("apache"));

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(props));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/recurring.custom/create", "POST")
                .withBody(Map.of("actionProps", props))
                .onHandlerMethod("create", User.class, Map.class);
    }

    @Test
    public void testUpdate() throws Exception {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("id", ACTION_ID);
        props.put("name", "renamed-custom-state-action");
        props.put("cron_expr", "0 0 4 ? * *");
        props.put("states", List.of("apache"));
        props.put("test", true);
        props.put("active", false);

        context.checking(new Expectations() {{
            oneOf(handler()).update(with(mockUser), with(props));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/recurring.custom/update", "POST")
                .withBody(Map.of("actionProps", props))
                .onHandlerMethod("update", User.class, Map.class);
    }

    /**
     * Every property of an update but the ID is documented as optional, so the smallest documented
     * update request has to be accepted too.
     */
    @Test
    public void testUpdateWithIdOnly() throws Exception {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("id", ACTION_ID);

        context.checking(new Expectations() {{
            oneOf(handler()).update(with(mockUser), with(props));
            will(returnValue(ACTION_ID));
        }});

        validateApiContract("/recurring.custom/update", "POST")
                .withBody(Map.of("actionProps", props))
                .onHandlerMethod("update", User.class, Map.class);
    }

    @Test
    public void testListAvailable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAvailable(with(mockUser));
            will(returnValue(List.of("apache", "postfix", "sshd")));
        }});

        validateApiContract("/recurring.custom/listAvailable", "GET")
                .onHandlerMethod("listAvailable", User.class);
    }
}
