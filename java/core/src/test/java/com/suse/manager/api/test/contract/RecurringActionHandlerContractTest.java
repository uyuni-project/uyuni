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

import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringPlaybook;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RecurringActionHandlerContractTest extends BaseOpenApiTest {

    private static final Integer ACTION_ID = 10;
    private static final Integer MINION_ID = 1000010000;

    @Override
    protected String getApiNamespace() {
        return "recurring";
    }

    @Override
    protected Class<RecurringActionHandler> getHandlerClass() {
        return RecurringActionHandler.class;
    }

    private RecurringActionHandler handler() {
        return (RecurringActionHandler) handlerMock;
    }

    /**
     * The serializer reads the action through its type, so the fixture is a real action rather
     * than a map. The type decides which of the documented properties appear, which is why the
     * tests use both a highstate and a playbook action.
     *
     * @param type the recurring action type to attach
     * @return a recurring action targeting a minion
     */
    private MinionRecurringAction recurringAction(RecurringActionType type) {
        MinionServer minion = new MinionServer();
        minion.setId(MINION_ID.longValue());

        MinionRecurringAction action = new MinionRecurringAction(type, true, minion, mockUser);
        action.setId(ACTION_ID.longValue());
        action.setName("test-recurring-action");
        action.setCronExpr("0 0 3 ? * *");
        action.setCreated(new Date());
        action.setCreator(mockUser);
        return action;
    }

    private MinionRecurringAction highstateAction() {
        return recurringAction(new RecurringHighstate(false));
    }

    /**
     * A playbook action is the one that carries the ansible specific properties.
     *
     * @return a recurring playbook action
     */
    private MinionRecurringAction playbookAction() {
        RecurringPlaybook playbook = new RecurringPlaybook(false);
        playbook.setExtraVars("target: webserver".getBytes(StandardCharsets.UTF_8));
        playbook.setFlushCache(true);
        playbook.setInventoryPath("/etc/ansible/hosts");
        playbook.setPlaybookPath("/etc/ansible/playbook.yml");
        return recurringAction(playbook);
    }

    @Test
    public void testListByEntity() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listByEntity(with(mockUser), with("minion"), with(MINION_ID));
            will(returnValue(List.of(highstateAction(), playbookAction())));
        }});

        validateApiContract("/recurring/listByEntity", "GET")
                .withParams(Map.of("type", new String[] {"minion"}, "id", new String[] {MINION_ID.toString()}))
                .onHandlerMethod("listByEntity", User.class, String.class, Integer.class);
    }

    @Test
    public void testLookupById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).lookupById(with(mockUser), with(ACTION_ID));
            will(returnValue(highstateAction()));
        }});

        validateApiContract("/recurring/lookupById", "GET")
                .withParams(Map.of("id", new String[] {ACTION_ID.toString()}))
                .onHandlerMethod("lookupById", User.class, Integer.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(ACTION_ID));
            will(returnValue(1));
        }});

        validateApiContract("/recurring/delete", "POST")
                .withBody(Map.of("id", ACTION_ID))
                .onHandlerMethod("delete", User.class, Integer.class);
    }
}
