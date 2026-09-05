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
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringHighstateHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecurringHighstateHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "recurring.highstate";
    }

    @Override
    protected Class<RecurringHighstateHandler> getHandlerClass() {
        return RecurringHighstateHandler.class;
    }

    private RecurringHighstateHandler handler() {
        return (RecurringHighstateHandler) handlerMock;
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
        props.put("name", "test-highstate-action");
        props.put("cron_expr", "0 0 3 ? * *");
        props.put("test", false);
        return props;
    }

    @Test
    public void testCreate() throws Exception {
        var props = createProps();

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(props));
            will(returnValue(1));
        }});

        validateApiContract("/recurring.highstate/create", "POST")
                .withBody(Map.of("actionProps", props))
                .onHandlerMethod("create", User.class, Map.class);
    }

    /**
     * test is optional, so a request carrying only the required properties must still satisfy the
     * documented schema.
     */
    @Test
    public void testCreateWithRequiredPropsOnly() throws Exception {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("entity_type", "org");
        props.put("entity_id", 1000010000);
        props.put("name", "test-highstate-action");
        props.put("cron_expr", "0 0 3 ? * *");

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(props));
            will(returnValue(1));
        }});

        validateApiContract("/recurring.highstate/create", "POST")
                .withBody(Map.of("actionProps", props))
                .onHandlerMethod("create", User.class, Map.class);
    }

    @Test
    public void testUpdate() throws Exception {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("id", 10);
        props.put("name", "renamed-highstate-action");
        props.put("cron_expr", "0 0 4 ? * *");
        props.put("active", true);

        context.checking(new Expectations() {{
            oneOf(handler()).update(with(mockUser), with(props));
            will(returnValue(10));
        }});

        validateApiContract("/recurring.highstate/update", "POST")
                .withBody(Map.of("actionProps", props))
                .onHandlerMethod("update", User.class, Map.class);
    }
}
